package org.apache.dstream.local;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.apache.dstream.MergerImpl;
import org.apache.dstream.assembly.ShuffleWriter;
import org.apache.dstream.assembly.Stage;
import org.apache.dstream.assembly.StreamAssembly;
import org.apache.dstream.assembly.Task;
import org.apache.dstream.exec.StreamExecutor;
import org.apache.dstream.io.StreamableSource;
import org.apache.dstream.utils.Assert;
import org.apache.dstream.utils.SerializableFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Emulator of the distributed execution environment which will utilize Java threads to 
 * parallelize processing. Not intended for performance testing, although natural performance 
 * improvements could be observed due to multi-threading especially in the compute intensive processes.
 * 
 *
 * @param <T>
 */
public class StreamExecutorImpl<R> extends StreamExecutor<R> {

	private final Logger logger = LoggerFactory.getLogger(StreamExecutorImpl.class);
	
	private ExecutorService executor;
	
	public StreamExecutorImpl(StreamAssembly streamAssembly) {
		super(streamAssembly);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Stream<R> execute() {
		if (logger.isInfoEnabled()){
			logger.info("Executing " + this.streamAssembly.getJobName());
		}
		
		StreamableSource<T> source = (StreamableSource<T>) this.streamAssembly.getSource();
		this.executor = Executors.newCachedThreadPool();
		ShuffleWriterImpl finalShuffle = null;
		
		for (Stage<T,R> stage : this.streamAssembly) {
			MergerImpl<?,?> merger = (MergerImpl<?,?>)stage.getMerger();
			int partitionSize = merger.getPartitionSize();
			Map<Integer, ConcurrentHashMap<?, ?>> partitions = new HashMap<>();
			for (int i = 0; i < partitionSize; i++) {
				partitions.put(i, new ConcurrentHashMap());
			}
			
			ShuffleWriterImpl shuffleWriter = new ShuffleWriterImpl(partitions, merger.getPartitionerFunction(), merger.getMergeFunction());
			
			Split<T>[] splits = SplitGenerationUtil.generateSplits(source);
			Assert.notEmpty(splits, "Failed to generate splits from " + source);
			CountDownLatch taskCompletionLatch = new CountDownLatch(splits.length);
			SerializableFunction<Stream<T>, R> function = stage.getStageFunction();
			Task<T, R> task = new Task<T, R>(function);
			for (Split<T> split : splits) {
				this.executor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							task.execute(split.toStream(), shuffleWriter);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							taskCompletionLatch.countDown();
						}
					}
				});
			}
			try {
				taskCompletionLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
			source = shuffleWriter.toStreamableSource();
			finalShuffle = shuffleWriter;
		}
		
		return finalShuffle.toStreamableSource().toStream();
	}
}
