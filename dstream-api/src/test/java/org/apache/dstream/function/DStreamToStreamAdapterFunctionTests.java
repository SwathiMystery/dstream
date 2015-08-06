package org.apache.dstream.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.dstream.function.DStreamToStreamAdapterFunction;
import org.apache.dstream.function.SerializableFunctionConverters.SerFunction;
import org.apache.dstream.function.SerializableFunctionConverters.SerPredicate;
import org.junit.Test;

public class DStreamToStreamAdapterFunctionTests {
	
	@Test(expected=IllegalArgumentException.class)
	public void unsupportedOperation(){
		new DStreamToStreamAdapterFunction("foo", (SerFunction<?,?>)s -> s);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void nullOperation(){
		new DStreamToStreamAdapterFunction("map", null);
	}

	@Test
	public void validateMap(){
		SerFunction<String, String> mapFunction = s -> s.toUpperCase();
		DStreamToStreamAdapterFunction f = new DStreamToStreamAdapterFunction("map", mapFunction);
		String result = (String) f.apply(Stream.of("foo")).findFirst().get();
		assertEquals("FOO", result);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void validateflatMap(){
		SerFunction<String, Stream<String>> flatMapFunction = s -> Stream.of(s.split(" "));
		DStreamToStreamAdapterFunction f = new DStreamToStreamAdapterFunction("flatMap", flatMapFunction);	
		Stream<String> resultStream = (Stream<String>) f.apply(Stream.of("foo bar"));
		List<String> result = resultStream.collect(Collectors.toList());
		assertEquals("foo", result.get(0));
		assertEquals("bar", result.get(1));
	}
	
	@Test
	public void validateFilter(){
		SerPredicate<String> filterFunction = s -> s.equals("foo");
		DStreamToStreamAdapterFunction f = new DStreamToStreamAdapterFunction("filter", filterFunction);
		assertFalse(f.apply(Stream.of("bar")).findFirst().isPresent());
		assertTrue(f.apply(Stream.of("foo")).findFirst().isPresent());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void validateSequence(){
		SerFunction<String, Stream<String>> flatMapFunction = s -> Stream.of(s.split(" "));
		DStreamToStreamAdapterFunction f0 = new DStreamToStreamAdapterFunction("flatMap", flatMapFunction);	
		
		SerPredicate<String> filterFunction = s -> s.equals("foo");
		DStreamToStreamAdapterFunction f1 = new DStreamToStreamAdapterFunction("filter", filterFunction);
		
		SerFunction<String, String> mapFunction = s -> s.toUpperCase();
		DStreamToStreamAdapterFunction f2 = new DStreamToStreamAdapterFunction("map", mapFunction);
		
		SerFunction f =  f0.andThen(f1).andThen(f2);
		assertEquals("FOO", ((Stream<String>)f.apply(Stream.of("foo bar"))).findFirst().get());
	}
}
