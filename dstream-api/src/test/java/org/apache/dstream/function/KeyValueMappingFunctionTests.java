package org.apache.dstream.function;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.Assert;

import org.apache.dstream.function.KeyValueMappingFunction;
import org.apache.dstream.function.SerializableFunctionConverters.SerBinaryOperator;
import org.apache.dstream.support.Aggregators;
import org.apache.dstream.utils.KVUtils;
import org.junit.Test;

public class KeyValueMappingFunctionTests {

	@Test
	public void validateKVMapper(){
		KeyValueMappingFunction<String, String, Integer> kvFunc = new KeyValueMappingFunction<String, String, Integer>(s -> s, s -> 1);
		List<Entry<String, Integer>> result = kvFunc.apply(Stream.of("hello")).collect(Collectors.toList());
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(KVUtils.kv("hello", 1), result.get(0));
	}
	
	@Test
	public void validateKVMapperWithValuesReducer(){
		KeyValueMappingFunction<String, String, Integer> kvFunc = new KeyValueMappingFunction<String, String, Integer>(s -> s, s -> 1, Integer::sum);
		List<Entry<String, Integer>> result = kvFunc.apply(Stream.of("hello", "bye", "hello")).collect(Collectors.toList());
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(KVUtils.kv("hello", 2), result.get(0));
		Assert.assertEquals(KVUtils.kv("bye", 1), result.get(1));
	}
	
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void validateKVMapperWithValuesGrouper(){	
		KeyValueMappingFunction<String, String, Object> kvFunc = new KeyValueMappingFunction<String, String, Object>(s -> s, s -> 1, (SerBinaryOperator)Aggregators::aggregateFlatten);
		List<Entry<String, Object>> result = kvFunc.apply(Stream.of("hello", "bye", "hello")).collect(Collectors.toList());
		Assert.assertEquals(2, result.size());
		
		Entry<String, Object> firstResult = result.get(0);
		assertEquals("hello", firstResult.getKey());
		assertArrayEquals(new Integer[]{1,1}, ((List<Integer>)firstResult.getValue()).toArray(new Integer[]{}));
		
		Entry<String, Object> secondResult = result.get(1);
		assertEquals("bye", secondResult.getKey());
		assertEquals(1, secondResult.getValue());
	}
}
