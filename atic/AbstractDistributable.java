package org.apache.dstream;

import java.util.Map.Entry;

import org.apache.dstream.utils.Pair;
import org.apache.dstream.utils.Partitioner;
import org.apache.dstream.utils.SerializableBiFunction;
import org.apache.dstream.utils.SerializableBinaryOperator;
import org.apache.dstream.utils.SerializableFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of {@link Distributable}
 * 
 * @param <K>
 * @param <V>
 */
public abstract class AbstractDistributable<K, V> implements Distributable<K,V> {
	
	private static final long serialVersionUID = 7020089231859026667L;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private transient final AbstractDataPipelineExecutionProvider<Entry<K, V>> executionContext;
	
	private int partitionSize;

	private SerializableBinaryOperator<V> combineFunction;
	
	private SerializableFunction<Entry<K, V>, Integer> partitionerFunction;
	
	/**
	 * 
	 * @param context
	 */
	protected AbstractDistributable(AbstractDataPipelineExecutionProvider<Entry<K,V>> executionContext){
		this.executionContext = executionContext;
	}

	@Override
	public Triggerable<Entry<K, V>> combine(int partitionSize, SerializableBinaryOperator<V> combineFunction) {
		if (logger.isDebugEnabled()){
			logger.debug("Accepted 'aggregate' request for " + partitionSize + " partitions.");
		}
		this.partitionSize = partitionSize;
		this.combineFunction = combineFunction;
		Partitioner<Entry<K, V>> defaultPartitioner = new DefaultPartitioner(this.partitionSize);
		this.partitionerFunction = new SerializableFunction<Entry<K, V>, Integer>() {
			private static final long serialVersionUID = -8996083508793084950L;
			@Override
			public Integer apply(Entry<K, V> t) {
				return defaultPartitioner.getPartition(t);
			}
		};
		this.executionContext.getAssembly().getLastStage().setMerger(this);
		return new DefaultTriggerable<Entry<K,V>>(this.executionContext);
	}

	@Override
	public Triggerable<Entry<K, V>> combine(Partitioner<Entry<K,V>> partitioner, SerializableBinaryOperator<V> combineFunction) {
		if (logger.isDebugEnabled()){
			logger.debug("Accepted 'aggregate' request with " + partitioner + ".");
		}
		this.partitionerFunction = new SerializableFunction<Entry<K, V>, Integer>() {
			private static final long serialVersionUID = 6530880100257370609L;
			@Override
			public Integer apply(Entry<K, V> t) {
				return partitioner.getPartition(t);
			}
		};
		this.combineFunction = combineFunction;
		this.executionContext.getAssembly().getLastStage().setMerger(this);
		return new DefaultTriggerable<Entry<K,V>>(this.executionContext);
	}

	@Override
	public Triggerable<Entry<K, V>> combine(SerializableFunction<Entry<K, V>, Integer> partitionerFunction,
			SerializableBinaryOperator<V> combineFunction) {
		if (logger.isDebugEnabled()){
			logger.debug("Accepted 'aggregate' request with partitioner function.");
		}
		this.partitionerFunction = partitionerFunction;
		this.combineFunction = combineFunction;
		this.executionContext.getAssembly().getLastStage().setMerger(this);
		return new DefaultTriggerable<Entry<K,V>>(this.executionContext);
	}
	
	public int getPartitionSize() {
		return this.partitionSize;
	}

	public SerializableBinaryOperator<V> getCombineFunction() {
		return this.combineFunction;
	}

	public SerializableFunction<Entry<K, V>, Integer> getPartitionerFunction() {
		return this.partitionerFunction;
	}
	
	/**
	 * 
	 */
	private class DefaultPartitioner extends Partitioner<Entry<K,V>> {
		private static final long serialVersionUID = -7960042449579121912L;

		public DefaultPartitioner(int partitionSize) {
			super(partitionSize);
		}

		@Override
		public int getPartition(Entry<K,V> input) {
			return (input.getKey().hashCode() & Integer.MAX_VALUE) % partitionSize;
		}
	}

	@Override
	public <R> Distributable<K, Pair<V,R>> join(Distributable<K, R> intermediateResult) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <W,R> Distributable<K,R> join(Distributable<K, W> intermediateResult, SerializableBiFunction<V, W, R> valueCombiner){
		return null;
	}

	@Override
	public Distributable<K, Iterable<V>> groupByKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Triggerable<Entry<K, V>> partition(int partitionSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Triggerable<Entry<K, V>> partition(
			Partitioner<Entry<K, V>> partitioner) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Triggerable<Entry<K, V>> partition(
			SerializableFunction<Entry<K, V>, Integer> partitionerFunction) {
		// TODO Auto-generated method stub
		return null;
	}
}