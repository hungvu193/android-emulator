// Copyright (c) 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 Radek Lzicar & Ales Lanik
//
// This file is part of Nostalgia Emulator Framework.
//
// Nostalgia Emulator Framework is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nostalgia Emulator Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Nostalgia Emulator Framework. If not, see <http://www.gnu.org/licenses/>.

package com.nostalgiaemulators.framework.base;

public class Benchmark {
	public Benchmark(String name, int numSteps, BenchmarkCallback callback) {
		this.name = name;
		this.callback = callback;
		this.numSteps = numSteps;
	}

	public void reset() {
		startTime = -1;
		steps = 0;
		totalTime = 0;
		callback.onBenchmarkReset(this);
	}

	public void notifyFrameStart() {
		if (!isRunning) {
			return;
		}
		startTime = System.currentTimeMillis();
		steps++;
	}

	public void notifyFrameEnd() {
		if (!isRunning) {
			return;
		}
		if (startTime != -1) {
			totalTime += System.currentTimeMillis() - startTime;
		}
		if (steps == numSteps) {
			callback.onBenchmarkEnded(this, steps, totalTime);
			stop();
		}
	}

	public void stop() {
		isRunning = false;
	}

	public interface BenchmarkCallback {
		void onBenchmarkReset(Benchmark benchmark);

		void onBenchmarkEnded(Benchmark benchmark, int steps, long totalTime);
	}

	public String getName() {
		return name;
	}

	private boolean isRunning = true;
	private BenchmarkCallback callback;
	private long startTime = -1;
	private long totalTime = 0;
	private int steps = 0;
	private int numSteps = 0;
	private String name;
}
