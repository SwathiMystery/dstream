package org.apache.dstream.assembly;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.dstream.utils.SerializableFunction;
import org.apache.dstream.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @param <T>
 * @param <R>
 */
public class Task<T,R> implements Serializable {
	private static final long serialVersionUID = -1917576454386721759L;
	
	private Logger logger = LoggerFactory.getLogger(Task.class);
	
	private final SerializableFunction<Stream<T>, ?> function;
	/**
	 * 
	 * @param function
	 * @param preProcessFunction
	 */
	public Task(SerializableFunction<Stream<T>, ?> function, SerializableFunction<Stream<?>, Stream<?>> preProcessFunction) {
		if (preProcessFunction == null){
			this.function = function;
		} else {
			this.function = new SerializableFunction<Stream<T>, R>() {
				private static final long serialVersionUID = -1235381577031239367L;
				@Override
				public R apply(Stream<T> t) {
					@SuppressWarnings({"unchecked", "rawtypes"})
					Function<Stream<T>, R> f = (Function<Stream<T>, R>) function.compose((Function) preProcessFunction);
					return f.apply(t);
				}
			};
		}
		
	}

	/**
	 * 
	 * @param stream
	 * @param writer
	 */
	@SuppressWarnings("unchecked")
	public <K,V> void execute(Stream<T> stream, ShuffleWriter<K, V> writer) {
		if (logger.isDebugEnabled()){
			logger.debug("Executing task");
		}
		// executes user function
		Object result = this.function.apply(stream);
		//
		if (result instanceof Map){
			if (logger.isDebugEnabled()){
				logger.debug("Result is Map: " + result);
			}
			Set<Entry<K,V>> entry = ((Map<K,V>)result).entrySet();	
			entry.forEach(s -> writer.write(s));
		} else {
			if (logger.isDebugEnabled()){
				logger.debug("Result is terminal value: " + result);
			}
			writer.write((Entry<K, V>) Utils.toEntry(null, result));
		}
	}
}
