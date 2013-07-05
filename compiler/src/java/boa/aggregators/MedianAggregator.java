package boa.aggregators;

import java.io.IOException;
import java.util.TreeMap;
import java.util.SortedMap;

import boa.io.EmitKey;

/**
 * A Boa aggregator to calculate a median of the values in a dataset.
 * 
 * @author rdyer
 */
@AggregatorSpec(name = "median", type = "int")
public class MedianAggregator extends Aggregator {
	private SortedMap<Long, Long> map;
	private long count;

	/** {@inheritDoc} */
	@Override
	public void start(EmitKey key) {
		super.start(key);

		map = new TreeMap<Long, Long>();
		count = 0;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssociative() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCommutative() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(String data, String metadata) throws IOException, InterruptedException {
		for (final String s : data.split(";")) {
			final int idx = s.indexOf(":");
			if (idx > 0) {
				final long item = Long.valueOf(s.substring(0, idx));
				final long count = Long.valueOf(s.substring(idx + 1));
				for (int i = 0; i < count; i++)
					aggregate(item, metadata);
			} else
				aggregate(Long.valueOf(s), metadata);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(long data, String metadata) {
		if (map.containsKey(data))
			map.put(data, map.get(data) + 1L);
		else
			map.put(data, 1L);
		count++;
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(double data, String metadata) {
		this.aggregate(Double.valueOf(data).longValue(), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		if (this.isCombining()) {
			String s = "";
			for (final Long key : map.keySet())
				s += key + ":" + map.get(key) + ";";
			this.collect(s, null);
			return;
		}

		float median = 0;

		long medianPos = count / 2L;
		long curPos = 0;
		long prevPos = 0;
		long prevKey = 0;

		for (final Long key : map.keySet()) {
			curPos = prevPos + map.get(key);

			if (prevPos <= medianPos && medianPos < curPos) {
				if (curPos % 2 == 0 && prevPos == medianPos)
					median = (float) (key + prevKey) / 2.0f;
				else
					median = key;
				break;
			}

			prevKey = key;
			prevPos = curPos;
		}

		this.collect(median);
	}
}
