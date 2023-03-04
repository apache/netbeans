/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * Contributor(s): Thomas Ball
 */

/**
 * A simple class for calculating and reporting elapsed system time.
 * Each timer has a start time and may have an end time.  If an
 * elapsed time value is requested of a timer which doesn't have its
 * stop time set, the current system time is used.
 *
 * @author Tom Ball
 */
public class ElapsedTimer {
    // System times in milliseconds; see System.currentTimeMillis();
    private long startTime;
    private long stopTime;

    /**
     * Create a new timer.
     */
    public ElapsedTimer() {
	reset();
    }

    /**
     * Stop the current timer; that is, set its stopTime.
     */
    public void stop() {
	stopTime = System.currentTimeMillis();
    }

    /**
     * Reset the starting time to the current system time.
     */
    public final void reset() {
	startTime = System.currentTimeMillis();
    }

    public long getElapsedMilliseconds() {
	long st = (stopTime == 0) ? System.currentTimeMillis() : stopTime;
	return st - startTime;
    }

    public int getElapsedSeconds() {
	return (int)(getElapsedMilliseconds() / 1000L);
    }

    public String toString() {
	long ms = getElapsedMilliseconds();
	int sec = (int)(ms / 1000L);
	int frac = (int)(ms % 1000L);
	StringBuffer sb = new StringBuffer();
	sb.append(ms / 1000L);
	sb.append('.');
	sb.append(ms % 1000L);
	sb.append(" seconds");
	return sb.toString();
    }
}
