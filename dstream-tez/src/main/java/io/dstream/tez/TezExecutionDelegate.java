/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dstream.tez;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.Credentials;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.tez.dag.api.TezConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dstream.AbstractDStreamExecutionDelegate;
import io.dstream.DStreamConstants;
import io.dstream.DStreamExecutionDelegate;
import io.dstream.DStreamExecutionGraph;
import io.dstream.tez.utils.HadoopUtils;
import io.dstream.tez.utils.SequenceFileOutputStreamsBuilder;

/**
 * Implementation of {@link DStreamExecutionDelegate} for Apache Tez.
 *
 */
public class TezExecutionDelegate extends AbstractDStreamExecutionDelegate {

	private final Logger logger = LoggerFactory.getLogger(TezExecutionDelegate.class);

	private final List<List<TaskDescriptor>> taskChains;

	private ExecutionContextAwareTezClient tezClient;

	/**
	 *
	 */
	public TezExecutionDelegate(){
		this.taskChains = new ArrayList<>();
	}

	/**
	 *
	 */
	@Override
	public Runnable getCloseHandler() {
		return new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Stopping TezClient");
					tezClient.clearAppMasterLocalFiles();
					tezClient.stop();
				}
				catch (Exception e) {
					logger.warn("Failed to stop TezClient", e);
				}
			}
		};
	}

	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected List<Stream<Stream<?>>>  doExecute(String executionName, Properties executionConfig, DStreamExecutionGraph... executionGraphs) {
		for (DStreamExecutionGraph executionGraph : executionGraphs) {
			TaskDescriptorChainBuilder builder = new TaskDescriptorChainBuilder(executionName, executionGraph, executionConfig);
			List<TaskDescriptor> taskDescriptors = builder.build();
			this.taskChains.add(taskDescriptors);
		}

		TezConfiguration tezConfiguration = new TezConfiguration(new Configuration());
		FileSystem fs = HadoopUtils.getFileSystem(tezConfiguration);

		if (this.tezClient == null){
			this.createAndTezClient(executionName, fs, tezConfiguration);
		}

		TezDAGBuilder dagBuilder = new TezDAGBuilder(executionName, this.tezClient, executionConfig);
		List<String> outputURIs  = new ArrayList<String>();

		String output = (String) executionConfig.getOrDefault(DStreamConstants.OUTPUT, this.tezClient.getClientName() + "/out/");
		for (int i = 0; i < this.taskChains.size(); i++) {
			List<TaskDescriptor> taskChain = this.taskChains.get(i);
			taskChain.forEach(task -> dagBuilder.addTask(task));
			output += (this.taskChains.size() > 1 ? "/" + i : "");
			dagBuilder.addDataSink(output);
			outputURIs.add(output);
		}

		Runnable executable = dagBuilder.build();

		try {
			executable.run();
			Stream<Stream<?>>[] resultStreams = outputURIs.stream().map(uri -> {
				SequenceFileOutputStreamsBuilder<?> ob = new SequenceFileOutputStreamsBuilder<>(this.tezClient.getFileSystem(), uri, this.tezClient.getTezConfiguration());
				return Stream.of(ob.build());
			}).collect(Collectors.toList()).toArray(new Stream[]{});

			return Arrays.asList(resultStreams);
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to execute DAG for " + executionName, e);
		}
	}

	/**
	 *
	 * @return
	 */
	//TODO needs some integration with extarnal conf to get credentials
	protected Credentials getCredentials(){
		return null;
	}

	/**
	 *
	 * @param pipelineSpecification
	 */
	private void createAndTezClient(String executionName, FileSystem fs, TezConfiguration tezConfiguration){
		Map<String, LocalResource> localResources = HadoopUtils.createLocalResources(fs, executionName +
				"/" + TezConstants.CLASSPATH_PATH);
		this.tezClient = new ExecutionContextAwareTezClient(executionName,
				tezConfiguration,
				localResources,
				this.getCredentials(),
				fs);
		try {
			this.tezClient.start();
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to start TezClient", e);
		}
	}
}
