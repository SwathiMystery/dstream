package org.apache.dstream;

import org.apache.dstream.DStream.DStream2.DStream2WithPredicate;
import org.apache.dstream.DStream.DStream3.DStream3WithPredicate;
import org.apache.dstream.DStream.DStream4.DStream4WithPredicate;
import org.apache.dstream.DStream.DStream5.DStream5WithPredicate;
import org.apache.dstream.function.SerializableFunctionConverters.Predicate;
import org.apache.dstream.utils.Assert;
import org.apache.dstream.utils.Tuples.Tuple2;
import org.apache.dstream.utils.Tuples.Tuple3;
import org.apache.dstream.utils.Tuples.Tuple4;
import org.apache.dstream.utils.Tuples.Tuple5;

public interface DStream<A> extends BaseDStream<A, DStream<A>> {
		
	/**
	 * Factory method which creates an instance of the {@code DStream} of type T.
	 * 
	 * @param sourceItemType the type of the elements of this pipeline
	 * @param sourceIdentifier the value which will be used in conjunction with 
	 *                       {@link DistributableConstants#SOURCE} in configuration 
	 *                       to point to source of this stream 
	 *                       (e.g., dstream.source.foo=file://foo.txt where 'foo' is the <i>sourceIdentifier</i>)
	 *                       <b>Must be unique!</b>. If you simply want to point to the same source, map it 
	 *                       through configuration (e.g., dstream.source.foo=file://foo.txt, dstream.source.bar=file://foo.txt)
	 * @return the new {@link DStream} of type T
	 * 
	 * @param <T> the type of pipeline elements
	 */
	@SuppressWarnings("unchecked")
	public static <A> DStream<A> ofType(Class<A> sourceElementType, String sourceIdentifier) {	
		Assert.notNull(sourceElementType, "'sourceElementType' must not be null");
		Assert.notEmpty(sourceIdentifier, "'sourceIdentifier' must not be null or empty");
		return DStreamOperationsCollector.as(sourceElementType, sourceIdentifier, DStream.class);
	}
	
	<_A> DStream2WithPredicate<A,_A> join(DStream<_A> ds);
	
	<_A,_B> DStream3WithPredicate<A,_A,_B> join(DStream2<_A,_B> ds);
	
	<_A,_B,_C> DStream4WithPredicate<A,_A,_B,_C> join(DStream3<_A,_B,_C> ds);
	
	<_A,_B,_C,_D> DStream5WithPredicate<A,_A,_B,_C,_D> join(DStream4<_A,_B,_C,_D> ds);
	
	/**
	 * 
	 * @param <A>
	 * @param <B>
	 */
	interface DStream2<A,B> extends BaseDStream<Tuple2<A,B>, DStream2<A,B>> {
		
		public interface DStream2WithPredicate<A,B> extends DStream2<A,B>{
			DStream2<A,B> on(Predicate<? super Tuple2<A,B>> predicate);	
		}

		<_A> DStream3WithPredicate<A,B,_A> join(DStream<_A> ds);
		<_A,_B> DStream4WithPredicate<A,B,_A,_B> join(DStream2<_A,_B> ds);
		<_A,_B,_C> DStream5WithPredicate<A,B,_A,_B,_C> join(DStream3<_A,_B,_C> ds);
	}
	
	/**
	 * 
	 * @param <A>
	 * @param <B>
	 * @param <C>
	 */
	interface DStream3<A,B,C> extends BaseDStream<Tuple3<A,B,C>, DStream3<A,B,C>> {
		
		interface DStream3WithPredicate<A,B,C> extends DStream3<A,B,C>{
			DStream3<A,B,C> on(Predicate<? super Tuple3<A,B,C>> predicate);	
		}

		<_A> DStream4WithPredicate<A,B,C,_A> join(DStream<_A> ds);
		<_A,_B> DStream5WithPredicate<A,B,C,_A,_B> join(DStream2<_A,_B> ds);
	}
	
	/**
	 * 
	 * @param <A>
	 * @param <B>
	 * @param <C>
	 * @param <D>
	 */
	interface DStream4<A,B,C,D> extends BaseDStream<Tuple4<A,B,C,D>, DStream4<A,B,C,D>> {
	
		interface DStream4WithPredicate<A,B,C,D> extends DStream4<A,B,C,D>{
			DStream4<A,B,C,D> on(Predicate<? super Tuple4<A,B,C,D>> predicate);	
		}

		<_A> DStream5WithPredicate<A,B,C,D,_A> join(DStream<_A> ds);
	}
	
	/**
	 * 
	 * @param <A>
	 * @param <B>
	 * @param <C>
	 * @param <D>
	 * @param <E>
	 */
	interface DStream5<A,B,C,D,E> extends BaseDStream<Tuple5<A,B,C,D,E>, DStream5<A,B,C,D,E>> {
		
		interface DStream5WithPredicate<A,B,C,D,E> extends DStream5<A,B,C,D,E>{
			DStream5<A,B,C,D,E> on(Predicate<? super Tuple5<A,B,C,D,E>> predicate);	
		}
	}
}
