package org.apache.dstream.local;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.dstream.DistributableSource;
import org.apache.dstream.IntermediateResult;
import org.apache.dstream.OutputSpecification;
import org.apache.dstream.StreamExecutionContext;
import org.apache.dstream.io.TextSource;

/**
 * This test simply validates the type-safety and the API, so its successful compilation
 * implies overall success of this test.
 */
@SuppressWarnings("unused")
public class StreamExecutionContextAPIValidatorTests { 
	
	public void computePairs() throws Exception {
		OutputSpecification outputSpec = null;
		Path path = FileSystems.getFileSystem(new URI("file:///")).getPath("src/test/java/org/apache/dstream/sample.txt");
		DistributableSource<String> source = TextSource.create(path).forJob("foo");
		
		source.computePairs(stream -> stream
					.flatMap(s -> Stream.of(s.split("\\s+")))
					.collect(Collectors.toMap(s -> s, s -> 1, Integer::sum))
		  ).aggregate(3, Integer::sum)
		   .save(outputSpec);
	}
	
	public void computePairsWithContinuation() throws Exception {
		OutputSpecification outputSpec = null;
		Path path = FileSystems.getFileSystem(new URI("file:///")).getPath("src/test/java/org/apache/dstream/sample.txt");
		DistributableSource<String> source = TextSource.create(path).forJob("foo");
		
		source.<Integer>computePairs(stream -> stream
					.flatMap(s -> Stream.of(s.split("\\s+")))
					.collect(Collectors.toMap(s -> s, s -> 1, Integer::sum))
		  ).aggregate(3, Integer::sum)
		   .computePairs(stream -> stream
				   	 .filter(s -> true)
				   	 .collect(Collectors.groupingBy(s -> s))
		  ).aggregate(1, (a, b) -> a);
	}
	
	public void computeBoolean() throws Exception {
		Path path = FileSystems.getFileSystem(new URI("file:///")).getPath("src/test/java/org/apache/dstream/sample.txt");
		boolean result = TextSource.create(path).forJob("foo")
				.computeBoolean(stream -> !stream
					.flatMap(s -> Stream.of(s.split("\\s+")))
					.collect(Collectors.toMap(s -> s, s -> 1, Integer::sum))
					.isEmpty()
				);
	}
	
	public void computeInt() throws Exception {
		Path path = FileSystems.getFileSystem(new URI("file:///")).getPath("src/test/java/org/apache/dstream/sample.txt");
		int result = TextSource.create(path).forJob("foo")
				.computeInt(stream -> stream
					.flatMap(s -> Stream.of(s.split("\\s+")))
					.collect(Collectors.toMap(s -> s, s -> 1, Integer::sum))
					.size()
				);
	}
	
	public void computeLong() throws Exception {
		Path path = FileSystems.getFileSystem(new URI("file:///")).getPath("src/test/java/org/apache/dstream/sample.txt");
		long result = TextSource.create(path).forJob("foo")
				.computeLong(stream -> stream
					.flatMap(s -> Stream.of(s.split("\\s+")))
					.count()
				);
	}
	
	public void partitionSourceWithFunction() throws Exception {
		OutputSpecification outputSpec = null;
		Path path = FileSystems.getFileSystem(new URI("file:///")).getPath("src/test/java/org/apache/dstream/sample.txt");
		DistributableSource<String> source = TextSource.create(path).forJob("foo");
		
		source.partition(s -> s.hashCode()).save();
	}
	
	public void partitionSourceWithDefaultPartitioner() throws Exception {
		OutputSpecification outputSpec = null;
		Path path = FileSystems.getFileSystem(new URI("file:///")).getPath("src/test/java/org/apache/dstream/sample.txt");
		DistributableSource<String> source = TextSource.create(path).forJob("foo");
		
		source.partition(4).save(outputSpec);
	}
	
	public void partitionSourceWithFunctionAfterComputation() throws Exception {
		OutputSpecification outputSpec = null;
		Path path = FileSystems.getFileSystem(new URI("file:///")).getPath("src/test/java/org/apache/dstream/sample.txt");
		DistributableSource<String> source = TextSource.create(path).forJob("foo");
		
		source.computePairs(stream -> stream
				.flatMap(s -> Stream.of(s.split("\\s+")))
				.collect(Collectors.toMap(s -> s, s -> 1, Integer::sum))
	  ).partition(s -> s.hashCode())
	   .save(outputSpec);
	}
}