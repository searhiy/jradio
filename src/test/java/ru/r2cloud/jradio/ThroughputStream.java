package ru.r2cloud.jradio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ThroughputStream implements FloatInput {

	private final ScheduledExecutorService executor;
	private final AtomicLong numberOfBytes = new AtomicLong(0);
	private final List<Long> samples = new ArrayList<Long>();
	private long total = 0l;

	private final RepeatedWavSource impl;

	public ThroughputStream(RepeatedWavSource impl) {
		this.impl = impl;
		executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				long curSample = numberOfBytes.getAndSet(0);
				samples.add(curSample);
				total += curSample;
			}
		}, 1000, 1000, TimeUnit.MILLISECONDS);
	}
	
	public List<Long> getSamples() {
		return samples;
	}
	
	public Long getAverage() {
		return total / samples.size();
	}
	
	@Override
	public float readFloat() throws IOException {
		float result = impl.readFloat();
		//wavsource outputs 2bytes
		numberOfBytes.addAndGet(2);
		return result;
	}

	@Override
	public void close() throws IOException {
		impl.close();
		executor.shutdown();
	}
}
