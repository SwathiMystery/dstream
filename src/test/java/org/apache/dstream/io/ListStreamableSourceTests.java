package org.apache.dstream.io;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ListStreamableSourceTests {

//	@Test
//	public void validateSplitList(){
//		List<Integer> intList = Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3});
//		ListStreamableSource<Integer> source =  ListStreamableSource.<Integer>create(intList, 5);
//		
//		Integer[] result = source.toStream(0).toArray(Integer[]::new);
//		Assert.assertArrayEquals(new Integer[]{1, 2, 3}, result);
//		
//		result = source.toStream(1).toArray(Integer[]::new);
//		Assert.assertArrayEquals(new Integer[]{4, 5, 6}, result);
//		
//		result = source.toStream(2).toArray(Integer[]::new);
//		Assert.assertArrayEquals(new Integer[]{7,8,9}, result);
//		
//		result = source.toStream(3).toArray(Integer[]::new);
//		Assert.assertArrayEquals(new Integer[]{1, 2, 3}, result);
//	}
//	
//	@Test(expected=IllegalArgumentException.class)
//	public void validateIllegalArgumentExceptionWhenIllegalPartition(){
//		List<Integer> intList = Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3});
//		ListStreamableSource<Integer> source =  ListStreamableSource.<Integer>create(intList, 5);
//
//		source.toStream(10).toArray(Integer[]::new);
//	}
	
	/**
	 * The algorithm guarantees that it will create at most 5 splits, but could be less which is in this case.
	 */
//	@Test(expected=IllegalStateException.class)
//	public void validateIllegalStateExceptionWhenSplitCanNotBeDetermined(){
//		List<Integer> intList = Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3});
//		ListStreamableSource<Integer> source =  ListStreamableSource.<Integer>create(intList, 5);
//
//		source.toStream(4).toArray(Integer[]::new);
//	}
}
