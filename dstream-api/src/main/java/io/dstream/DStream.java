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
package io.dstream;

import java.util.stream.Stream;

import io.dstream.DStream.DStream2.DStream2WithPredicate;
import io.dstream.DStream.DStream3.DStream3WithPredicate;
import io.dstream.DStream.DStream4.DStream4WithPredicate;
import io.dstream.DStream.DStream5.DStream5WithPredicate;
import io.dstream.DStream.DStream6.DStream6WithPredicate;
import io.dstream.SerializableStreamAssets.SerPredicate;
import io.dstream.utils.Assert;

/**
 * A type-safe {@link Stream}-like strategy that exposes <i>distributable data</i> as the sequence of
 * elements that support sequential and parallel aggregate operations.<br>
 * <br>
 * Below is the example of rudimentary <i>Word Count</i> written using the API:<br>
 * <pre>
 * DStream.ofType(String.class, "wc")
 *     .flatMap(line -&gt; Stream.of(line.split("\\s+")))
 *     .reduceValues(word -&gt; word, word -&gt; 1, Integer::sum)
 *  .executeAs("WordCount");
 * </pre>
 *
 * It defines operations with the following classifications:<br>
 * <ol>
 * <li>
 * <i>intermediate</i> - similar to <a href="https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#StreamOps">intermediate operations</a>
 *    of Java {@link Stream}.<br>
 *    </li>
 * <li>
 * <i>terminal</i> - similar to <a href="https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#StreamOps">terminal operations</a>
 *    of Java {@link Stream}.<br>
 *    </li>
 * <li>
 * <i>shuffle</i> - an operation which typically results in synchronization (typically data is written to  shuffle output).<br>
 *    </li>
 * <li>
 * <i>composable-shuffle</i> - an operation which typically results in synchronization if previous operation is <i>composable-transformation</i>.
 *    However, if previous operation is <i>composable-shuffle</i>, each subsequent operation that is also <i>composable-shuffle</i> could be composed
 *    into a single operation.<br>
 *    </li>
 * <li>
 * <i>composable-transformation</i> - an operation that defines a simple transformation (e.g., map, flatMap, filter etc.) and could be composed
 *    with subsequent operations of the same type.<br>
 *    </li>
 * </ol>
 * <br>
 * @param <A> the type of the stream element
 */
public interface DStream<A> extends BaseDStream<A, DStream<A>> {

	/**
	 * Factory method which creates an instance of the {@code DStream} of type A.
	 *
	 * @param sourceElementType the type of the elements of this stream
	 * @param name the value which represents the name of this stream and is also used
	 * 			   in configuration to resolve stream specific configuration values.
	 *             For example, one such configuration properties is the source of this stream
	 *             (e.g., dstream.source.foo=file://foo.txt where 'foo' is the <i>name</i>).
	 *             See {@link DStreamConstants#SOURCE} for more details.<br>
	 *             <b>Must be unique!</b>. If you simply want to point to the same source, map it
	 *              through configuration (e.g., dstream.source.foo=file://foo.txt, dstream.source.bar=file://foo.txt)
	 * @return the new {@link DStream} of type A
	 *
	 */
	@SuppressWarnings("unchecked")
	public static <A> DStream<A> ofType(Class<A> sourceElementType, String name) {
		Assert.notNull(sourceElementType, "'sourceElementType' must not be null");
		Assert.notEmpty(name, "'name' must not be null or empty");
		return DStreamExecutionGraphsBuilder.as(sourceElementType, name, DStream.class);
	}

	/**
	 * Will join two {@link DStream}s returning an instance of {@link DStream2}.
	 * <br>
	 * The actual instance of returned {@link DStream2} will be {@link DStream2WithPredicate}
	 * allowing predicate to be provided via {@link DStream2WithPredicate#on(SerPredicate)} operation.<br>
	 * <br>
	 * This is an <i>intermediate</i> operation.
	 * <br>
	 * This is a <i>composable-shuffle</i> operation.
	 *
	 * @param ds stream that is joined with this stream
	 * @return new {@link DStream2} representing the result of the join.
	 */
	<_A> DStream2WithPredicate<A,_A> join(DStream<_A> ds);

	/**
	 * Will join {@link DStream} with {@link DStream2} returning an instance of {@link DStream3}.
	 * <br>
	 * The actual instance of returned {@link DStream3} will be {@link DStream3WithPredicate}
	 * allowing predicate to be provided via {@link DStream3WithPredicate#on(SerPredicate)} operation.<br>
	 * <br>
	 * This is an <i>intermediate</i> operation.
	 * <br>
	 * This is a <i>composable-shuffle</i> operation.
	 *
	 * @param ds stream that is joined with this stream
	 * @return new {@link DStream3} representing the result of the join.
	 */
	<_A,_B> DStream3WithPredicate<A,_A,_B> join(DStream2<_A,_B> ds);

	/**
	 * Will join {@link DStream} with {@link DStream3} returning an instance of {@link DStream4}.
	 * <br>
	 * The actual instance of returned {@link DStream4} will be {@link DStream4WithPredicate}
	 * allowing predicate to be provided via {@link DStream4WithPredicate#on(SerPredicate)} operation.<br>
	 * <br>
	 * This is an <i>intermediate</i> operation.
	 * <br>
	 * This is a <i>composable-shuffle</i> operation.
	 *
	 * @param ds stream that is joined with this stream
	 * @return new {@link DStream4} representing the result of the join.
	 */
	<_A,_B,_C> DStream4WithPredicate<A,_A,_B,_C> join(DStream3<_A,_B,_C> ds);

	/**
	 * Will join {@link DStream} with {@link DStream4} returning an instance of {@link DStream5}.
	 * <br>
	 * The actual instance of returned {@link DStream5} will be {@link DStream5WithPredicate}
	 * allowing predicate to be provided via {@link DStream5WithPredicate#on(SerPredicate)} operation.<br>
	 * <br>
	 * This is an <i>intermediate</i> operation.
	 * <br>
	 * This is a <i>composable-shuffle</i> operation.
	 *
	 * @param ds stream that is joined with this stream
	 * @return new {@link DStream5} representing the result of the join.
	 */
	<_A,_B,_C,_D> DStream5WithPredicate<A,_A,_B,_C,_D> join(DStream4<_A,_B,_C,_D> ds);

	/**
	 * Will join {@link DStream} with {@link DStream5} returning an instance of {@link DStream6}.
	 * <br>
	 * The actual instance of returned {@link DStream6} will be {@link DStream6WithPredicate}
	 * allowing predicate to be provided via {@link DStream6WithPredicate#on(SerPredicate)} operation.<br>
	 * <br>
	 * This is an <i>intermediate</i> operation.
	 * <br>
	 * This is a <i>composable-shuffle</i> operation.
	 *
	 * @param ds stream that is joined with this stream
	 * @return new {@link DStream6} representing the result of the join.
	 */
	<_A,_B,_C,_D,_E> DStream6WithPredicate<A,_A,_B,_C,_D,_E> join(DStream5<_A,_B,_C,_D,_E> ds);


	/**
	 * Strategy which defines the type of {@link DStream} that contains two types of elements.
	 *
	 * @param <A> - first element type
	 * @param <B> - second element type
	 */
	interface DStream2<A,B> extends BaseDStream<io.dstream.utils.Tuples.Tuple2<A,B>, DStream2<A,B>> {

		/**
		 * Strategy which defines the type of {@link DStream2} that allows for predicate to be applied.
		 *
		 * @param <A> - first element type
		 * @param <B> - second element type
		 */
		public interface DStream2WithPredicate<A,B> extends DStream2<A,B>{
			DStream2<A,B> on(SerPredicate<? super io.dstream.utils.Tuples.Tuple2<A,B>> predicate);
		}

		/**
		 * Will join {@link DStream2} with {@link DStream} returning an instance of {@link DStream3}.
		 * <br>
		 * The actual instance of returned {@link DStream3} will be {@link DStream3WithPredicate}
		 * allowing predicate to be provided via {@link DStream3WithPredicate#on(SerPredicate)} operation.<br>
		 * <br>
		 * This is an <i>intermediate</i> operation.
		 * <br>
		 * This is a <i>composable-shuffle</i> operation.
		 *
		 * @param ds stream that is joined with this stream
		 * @return new {@link DStream3} representing the result of the join.
		 */
		<_A> DStream3WithPredicate<A,B,_A> join(DStream<_A> ds);

		/**
		 * Will join {@link DStream2} with {@link DStream2} returning an instance of {@link DStream4}.
		 * <br>
		 * The actual instance of returned {@link DStream4} will be {@link DStream4WithPredicate}
		 * allowing predicate to be provided via {@link DStream4WithPredicate#on(SerPredicate)} operation.<br>
		 * <br>
		 * This is an <i>intermediate</i> operation.
		 * <br>
		 * This is a <i>composable-shuffle</i> operation.
		 *
		 * @param ds stream that is joined with this stream
		 * @return new {@link DStream4} representing the result of the join.
		 */
		<_A,_B> DStream4WithPredicate<A,B,_A,_B> join(DStream2<_A,_B> ds);

		/**
		 * Will join {@link DStream2} with {@link DStream3} returning an instance of {@link DStream5}.
		 * <br>
		 * The actual instance of returned {@link DStream5} will be {@link DStream5WithPredicate}
		 * allowing predicate to be provided via {@link DStream5WithPredicate#on(SerPredicate)} operation.<br>
		 * <br>
		 * This is an <i>intermediate</i> operation.
		 * <br>
		 * This is a <i>composable-shuffle</i> operation.
		 *
		 * @param ds stream that is joined with this stream
		 * @return new {@link DStream5} representing the result of the join.
		 */
		<_A,_B,_C> DStream5WithPredicate<A,B,_A,_B,_C> join(DStream3<_A,_B,_C> ds);

		/**
		 * Will join {@link DStream2} with {@link DStream4} returning an instance of {@link DStream6}.
		 * <br>
		 * The actual instance of returned {@link DStream6} will be {@link DStream6WithPredicate}
		 * allowing predicate to be provided via {@link DStream6WithPredicate#on(SerPredicate)} operation.<br>
		 * <br>
		 * This is an <i>intermediate</i> operation.
		 * <br>
		 * This is a <i>composable-shuffle</i> operation.
		 *
		 * @param ds stream that is joined with this stream
		 * @return new {@link DStream6} representing the result of the join.
		 */
		<_A,_B,_C,_D> DStream6WithPredicate<A,B,_A,_B,_C,_D> join(DStream4<_A,_B,_C,_D> ds);
	}

	/**
	 * Strategy which defines the type of {@link DStream} that contains three types of elements.
	 *
	 * @param <A> - first element type
	 * @param <B> - second element type
	 * @param <C> - third element type
	 */
	interface DStream3<A,B,C> extends BaseDStream<io.dstream.utils.Tuples.Tuple3<A,B,C>, DStream3<A,B,C>> {

		/**
		 * Strategy which defines the type of {@link DStream3} that allows for predicate to be applied.
		 *
		 * @param <A> - first element type
		 * @param <B> - second element type
		 * @param <C> - third element type
		 */
		interface DStream3WithPredicate<A,B,C> extends DStream3<A,B,C>{
			DStream3<A,B,C> on(SerPredicate<? super io.dstream.utils.Tuples.Tuple3<A,B,C>> predicate);
		}

		/**
		 * Will join {@link DStream3} with {@link DStream} returning an instance of {@link DStream4}.
		 * <br>
		 * The actual instance of returned {@link DStream4} will be {@link DStream4WithPredicate}
		 * allowing predicate to be provided via {@link DStream4WithPredicate#on(SerPredicate)} operation.<br>
		 * <br>
		 * This is an <i>intermediate</i> operation.
		 * <br>
		 * This is a <i>composable-shuffle</i> operation.
		 *
		 * @param ds stream that is joined with this stream
		 * @return new {@link DStream4} representing the result of the join.
		 */
		<_A> DStream4WithPredicate<A,B,C,_A> join(DStream<_A> ds);

		/**
		 * Will join {@link DStream3} with {@link DStream2} returning an instance of {@link DStream5}.
		 * <br>
		 * The actual instance of returned {@link DStream5} will be {@link DStream5WithPredicate}
		 * allowing predicate to be provided via {@link DStream5WithPredicate#on(SerPredicate)} operation.<br>
		 * <br>
		 * This is an <i>intermediate</i> operation.
		 * <br>
		 * This is a <i>composable-shuffle</i> operation.
		 *
		 * @param ds stream that is joined with this stream
		 * @return new {@link DStream5} representing the result of the join.
		 */
		<_A,_B> DStream5WithPredicate<A,B,C,_A,_B> join(DStream2<_A,_B> ds);

		/**
		 * Will join {@link DStream3} with {@link DStream3} returning an instance of {@link DStream6}.
		 * <br>
		 * The actual instance of returned {@link DStream6} will be {@link DStream6WithPredicate}
		 * allowing predicate to be provided via {@link DStream6WithPredicate#on(SerPredicate)} operation.<br>
		 * <br>
		 * This is an <i>intermediate</i> operation.
		 * <br>
		 * This is a <i>composable-shuffle</i> operation.
		 *
		 * @param ds stream that is joined with this stream
		 * @return new {@link DStream6} representing the result of the join.
		 */
		<_A,_B,_C> DStream6WithPredicate<A,B,C,_A,_B,_C> join(DStream3<_A,_B,_C> ds);
	}

	/**
	 * Strategy which defines the type of {@link DStream} that contains four types of elements.
	 *
	 * @param <A> - first element type
	 * @param <B> - second element type
	 * @param <C> - third element type
	 * @param <D> - fourth element type
	 */
	interface DStream4<A,B,C,D> extends BaseDStream<io.dstream.utils.Tuples.Tuple4<A,B,C,D>, DStream4<A,B,C,D>> {

		/**
		 * Strategy which defines the type of {@link DStream4} that allows for predicate to be applied.
		 *
		 * @param <A> - first element type
		 * @param <B> - second element type
		 * @param <C> - third element type
		 * @param <D> - fourth element type
		 */
		interface DStream4WithPredicate<A,B,C,D> extends DStream4<A,B,C,D>{
			DStream4<A,B,C,D> on(SerPredicate<? super io.dstream.utils.Tuples.Tuple4<A,B,C,D>> predicate);
		}

		/**
		 * Will join {@link DStream4} with {@link DStream} returning an instance of {@link DStream5}.
		 * <br>
		 * The actual instance of returned {@link DStream5} will be {@link DStream5WithPredicate}
		 * allowing predicate to be provided via {@link DStream5WithPredicate#on(SerPredicate)} operation.<br>
		 * <br>
		 * This is an <i>intermediate</i> operation.
		 * <br>
		 * This is a <i>composable-shuffle</i> operation.
		 *
		 * @param ds stream that is joined with this stream
		 * @return new {@link DStream5} representing the result of the join.
		 */
		<_A> DStream5WithPredicate<A,B,C,D,_A> join(DStream<_A> ds);

		/**
		 * Will join {@link DStream4} with {@link DStream2} returning an instance of {@link DStream6}.
		 * <br>
		 * The actual instance of returned {@link DStream6} will be {@link DStream6WithPredicate}
		 * allowing predicate to be provided via {@link DStream6WithPredicate#on(SerPredicate)} operation.<br>
		 * <br>
		 * This is an <i>intermediate</i> operation.
		 * <br>
		 * This is a <i>composable-shuffle</i> operation.
		 *
		 * @param ds stream that is joined with this stream
		 * @return new {@link DStream6} representing the result of the join.
		 */
		<_A,_B> DStream6WithPredicate<A,B,C,D,_A,_B> join(DStream2<_A,_B> ds);
	}

	/**
	 * Strategy which defines the type of {@link DStream} that contains five types of elements.
	 *
	 * @param <A> - first element type
	 * @param <B> - second element type
	 * @param <C> - third element type
	 * @param <D> - fourth element type
	 * @param <E> - fifth element type
	 */
	interface DStream5<A,B,C,D,E> extends BaseDStream<io.dstream.utils.Tuples.Tuple5<A,B,C,D,E>, DStream5<A,B,C,D,E>> {
		/**
		 * Strategy which defines the type of {@link DStream5} that allows for predicate to be applied.
		 *
		 * @param <A> - first element type
		 * @param <B> - second element type
		 * @param <C> - third element type
		 * @param <D> - fourth element type
		 * @param <E> - fifth element type
		 */
		interface DStream5WithPredicate<A,B,C,D,E> extends DStream5<A,B,C,D,E>{
			DStream5<A,B,C,D,E> on(SerPredicate<? super io.dstream.utils.Tuples.Tuple5<A,B,C,D,E>> predicate);
		}

		/**
		 * Will join {@link DStream5} with {@link DStream} returning an instance of {@link DStream6}.
		 * <br>
		 * The actual instance of returned {@link DStream6} will be {@link DStream6WithPredicate}
		 * allowing predicate to be provided via {@link DStream6WithPredicate#on(SerPredicate)} operation.<br>
		 * <br>
		 * This is an <i>intermediate</i> operation.
		 * <br>
		 * This is a <i>composable-shuffle</i> operation.
		 *
		 * @param ds stream that is joined with this stream
		 * @return new {@link DStream6} representing the result of the join.
		 */
		<_A> DStream6WithPredicate<A,B,C,D,E,_A> join(DStream<_A> ds);
	}

	/**
	 * Strategy which defines the type of {@link DStream} that contains six
	 * types of elements.
	 *
	 * @param <A> - first element type
	 * @param <B> - second element type
	 * @param <C> - third element type
	 * @param <D> - fourth element type
	 * @param <E> - fifth element type
	 * @param <F> - sixth element type
	 */
	interface DStream6<A,B,C,D,E,F> extends BaseDStream<io.dstream.utils.Tuples.Tuple6<A,B,C,D,E,F>, DStream6<A,B,C,D,E,F>> {

		/**
		 * Strategy which defines the type of {@link DStream5} that allows for predicate to be applied.
		 *
		 * @param <A> - first element type
		 * @param <B> - second element type
		 * @param <C> - third element type
		 * @param <D> - fourth element type
		 * @param <E> - fifth element type
		 * @param <F> - sixth element type
		 */
		interface DStream6WithPredicate<A,B,C,D,E,F> extends DStream6<A,B,C,D,E,F>{
			DStream6<A,B,C,D,E,F> on(SerPredicate<? super io.dstream.utils.Tuples.Tuple6<A,B,C,D,E,F>> predicate);
		}
	}
}
