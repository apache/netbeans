/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.util.ArrayList;

/**
 * A timing utiltiy. Use as follows:
 <pre>
    StopWatch sw = new StopWatch("parsing");
    sw.start();
     ... prepare
    sw.mark("preparation")
    ... pass1
    sw.mark("pass1")
    ... pass2
    sw.mark("pass2")
    sw.stop();
    sw.dump();
 </pre>
 */
public final class StopWatch {

    private static final class Mark {
	private final String name;
	private final long time;

	public Mark(String name) {
	    this.name = name;
	    this.time = System.currentTimeMillis();
	}

	public String name() {
	    return name;
	}

	public long time() {
	    return time;
	}
    }

    private final String name;
    private final ArrayList<Mark> marks = new ArrayList<Mark>();

    private boolean running;

    public StopWatch(String name) {
	this.name = name;
    }

    public void start() {
	marks.clear();
	marks.add(new Mark("start"));			// NOI18N
	running = true;
    }

    public void stop() {
	marks.add(new Mark("stop"));			// NOI18N
	running = false;
    }

    public void mark(String name) {
	if (running)
	    marks.add(new Mark(name));
    }

    private long totalElapsedTime() {
	if (marks.size() <= 1) {
	    return 0;
	} else {
	    long startTime = marks.get(0).time();
	    long endTime = marks.get(marks.size()-1).time();
	    return endTime - startTime;
	}
    }

    /**
     * Dump the contents of the stopwatch unto stdout.
     * If 'thresholdMillis' is 0 will always dump, otherwise will dump
     * only if the total elapsed time exceeds the threshold.
     */

    public void dump(int thresholdMillis) {

	// If we care about the threshold but elapsed time is below it
	// don't bother printing

	if (thresholdMillis > 0 && totalElapsedTime() < thresholdMillis)
	    return;

	System.out.printf("StopWatch[%s]\n", name);	// NOI18N
	if (marks.size() == 0) {
	    System.out.printf("\tnever started\n");	// NOI18N
	    return;
	} else if (running) {
	    System.out.printf("\tstill running\n");	// NOI18N
	    return;
	}
	long startTime = marks.get(0).time();
	for (Mark mark : marks) {
	    System.out.printf("%-10s: %d\n",		// NOI18N
		mark.name(), mark.time() - startTime);
	}
    }

    public void dump() {
	dump(0);
    }
}
