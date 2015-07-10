package org.apache.dstream.support;

import static org.apache.dstream.utils.KVUtils.kv;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.dstream.DistributableConstants;
import org.apache.dstream.DistributableStream;
import org.apache.dstream.ExecutionGroup;
import org.apache.dstream.tez.BaseTezTests;
import org.apache.dstream.utils.Pair;
import org.apache.dstream.utils.TypeUtils;
import org.junit.After;
import org.junit.Test;

public class ExecutionGroupTests extends BaseTezTests {
	
	private final String applicationName = this.getClass().getSimpleName();
	
	@After
	public void after(){
		clean(this.applicationName);
	}

	@Test
	public void validateWithDefaultOutput() throws Exception {
		
		DistributableStream<String> hash = DistributableStream.ofType(String.class, "hash").compute(stream -> stream
				.map(line -> line.toUpperCase())
		);
		
		DistributableStream<Entry<Integer, String>> probe = DistributableStream.ofType(String.class, "probe").<Entry<Integer, String>>compute(stream -> stream
				.map(line -> {
					String[] split = line.trim().split("\\s+");
					return kv(Integer.parseInt(split[2]), split[0] + " " + split[1]);
				})
		).reduceGroups(keyVal -> keyVal.getKey(), keyVal -> keyVal.getValue(), (a, b) -> a + ", " + b);
		
		
		DistributableStream<Pair<String, Entry<Integer, String>>> joined = hash.join(probe, 
				hashElement -> Integer.parseInt(hashElement.substring(0, hashElement.indexOf(" ")).trim()), 
				probeElement -> probeElement.getKey()
			);
		
		ExecutionGroup jg = ExecutionGroup.create("group_defaultOut", hash, joined, probe);
		
		Future<Stream<Stream<Stream<? extends Object>>>> resultFuture = jg.executeAs(this.applicationName);
		List<Stream<Stream<? extends Object>>> result = resultFuture.get(10000, TimeUnit.MILLISECONDS).collect(Collectors.toList());
		assertEquals(3, result.size());
		
		//0
		Stream<Stream<String>> hashResult = TypeUtils.cast(result.get(0));
		List<String> coNames = hashResult.findFirst().get().collect(Collectors.toList());
		assertEquals("1 ORACLE", coNames.get(0));
		assertEquals("2 AMAZON", coNames.get(1));
		assertEquals("3 HORTONWORKS", coNames.get(2));
		
		//1
		Stream<Stream<Entry<Integer, Pair<String, String>>>> joinResult = TypeUtils.cast(result.get(1));
		List<Entry<Integer, Pair<String, String>>> joins = joinResult.findFirst().get().collect(Collectors.toList());
		assertEquals(kv(1, Pair.of("ORACLE", "Thomas Kurian, Larry Ellison")), joins.get(0));
		assertEquals(kv(2, Pair.of("AMAZON", "Jeffrey Blackburn, Jeff Bezos")), joins.get(1));
		assertEquals(kv(3, Pair.of("HORTONWORKS", "Tom McCuch, Herb Cunitz, Rob Bearden, Arun Murthy, Oleg Zhurakousky")), joins.get(2));

		//2
		Stream<Stream<Entry<Integer, String>>> probeResult = TypeUtils.cast(result.get(2));
		List<Entry<Integer, String>> peopleNameGroups = probeResult.findFirst().get().collect(Collectors.toList());
		assertEquals(kv(1, "Thomas Kurian, Larry Ellison"), peopleNameGroups.get(0));
		assertEquals(kv(2, "Jeffrey Blackburn, Jeff Bezos"), peopleNameGroups.get(1));
		assertEquals(kv(3, "Tom McCuch, Herb Cunitz, Rob Bearden, Arun Murthy, Oleg Zhurakousky"), peopleNameGroups.get(2));
	}
	
	@Test
	public void validateWithProvidedOutput() throws Exception {
		String executionPipelineName = "group_providedOut";
		DistributableStream<String> hash = DistributableStream.ofType(String.class, "hash").compute(stream -> stream
				.map(line -> line.toUpperCase())
		);
		
		DistributableStream<Entry<Integer, String>> probe = DistributableStream.ofType(String.class, "probe").<Entry<Integer, String>>compute(stream -> stream
				.map(line -> {
					String[] split = line.trim().split("\\s+");
					return kv(Integer.parseInt(split[2]), split[0] + " " + split[1]);
				})
		).reduceGroups(keyVal -> keyVal.getKey(), keyVal -> keyVal.getValue(), (a, b) -> a + ", " + b);
		
		
		DistributableStream<Pair<String, Entry<Integer, String>>> joined = hash.join(probe, 
				hashElement -> Integer.parseInt(hashElement.substring(0, hashElement.indexOf(" ")).trim()), 
				probeElement -> probeElement.getKey()
			);
		
		ExecutionGroup jg = ExecutionGroup.create(executionPipelineName, hash, joined, probe);
		
		Future<Stream<Stream<Stream<? extends Object>>>> resultFuture = jg.executeAs(this.applicationName);
		List<Stream<Stream<? extends Object>>> result = resultFuture.get(10000, TimeUnit.MILLISECONDS).collect(Collectors.toList());
		assertEquals(3, result.size());
		
		Properties prop = PipelineConfigurationHelper.loadExecutionConfig(this.applicationName);
		String output = prop.getProperty(DistributableConstants.OUTPUT + "." + executionPipelineName);
		
		assertTrue(new File(new File(new URI(output)), "/0").exists());
		assertTrue(new File(new File(new URI(output)), "/1").exists());
		assertTrue(new File(new File(new URI(output)), "/2").exists());
		
		//0
		Stream<Stream<String>> hashResult = TypeUtils.cast(result.get(0));
		List<String> coNames = hashResult.findFirst().get().collect(Collectors.toList());
		assertEquals("1 ORACLE", coNames.get(0));
		assertEquals("2 AMAZON", coNames.get(1));
		assertEquals("3 HORTONWORKS", coNames.get(2));
		
		//1
		Stream<Stream<Entry<Integer, Pair<String, String>>>> joinResult = TypeUtils.cast(result.get(1));
		List<Entry<Integer, Pair<String, String>>> joins = joinResult.findFirst().get().collect(Collectors.toList());
		assertEquals(kv(1, Pair.of("ORACLE", "Thomas Kurian, Larry Ellison")), joins.get(0));
		assertEquals(kv(2, Pair.of("AMAZON", "Jeffrey Blackburn, Jeff Bezos")), joins.get(1));
		assertEquals(kv(3, Pair.of("HORTONWORKS", "Tom McCuch, Herb Cunitz, Rob Bearden, Arun Murthy, Oleg Zhurakousky")), joins.get(2));

		//2
		Stream<Stream<Entry<Integer, String>>> probeResult = TypeUtils.cast(result.get(2));
		List<Entry<Integer, String>> peopleNameGroups = probeResult.findFirst().get().collect(Collectors.toList());
		assertEquals(kv(1, "Thomas Kurian, Larry Ellison"), peopleNameGroups.get(0));
		assertEquals(kv(2, "Jeffrey Blackburn, Jeff Bezos"), peopleNameGroups.get(1));
		assertEquals(kv(3, "Tom McCuch, Herb Cunitz, Rob Bearden, Arun Murthy, Oleg Zhurakousky"), peopleNameGroups.get(2));
		
		clean("group");
	}
}