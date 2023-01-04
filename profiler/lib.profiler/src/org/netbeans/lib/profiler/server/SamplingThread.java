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

package org.netbeans.lib.profiler.server;

import org.netbeans.lib.profiler.global.Platform;
import org.netbeans.lib.profiler.server.system.Timers;


/**
 * @author Tomas Hurka
 * @author Misha Dmitriev
 */
abstract class SamplingThread extends Thread {
    //~ Static fields/initializers -------------------------------------------------------------------------------------------

    private static final boolean isSolaris = Platform.isSolaris();
    private static final boolean isLinux = Platform.isLinux();
    private static final boolean isUnix = isSolaris || isLinux;
    private static final int VIOLATION_THRESHOLD = 10;
    private static final boolean DEBUG = false;

    //~ Instance fields ------------------------------------------------------------------------------------------------------

    private volatile boolean terminated;
    private int count;
    private int samplingInterval;
    
    //~ Methods --------------------------------------------------------------------------------------------------------------

    SamplingThread(int interval) {
        ThreadInfo.addProfilerServerThread(this);
        setPriority(Thread.MAX_PRIORITY);
        setDaemon(true);
        samplingInterval = interval;
        if (isSolaris) {
            samplingInterval *= 1000000; // Convert into nanos - the Solaris hires timer resolution
        } else if (isLinux) {
            samplingInterval *= 1000; // Convert into microseconds - the Linux hires timer resolution
        }
    }

    @Override
    public void run() {
        int adjustedSamplingInterval = samplingInterval;
        int upperBound = (samplingInterval * 5) / 4;
        int lowerBound = samplingInterval / 10;
        int violationCount = VIOLATION_THRESHOLD;

        long startTime = Timers.getCurrentTimeInCounts();

        while (!terminated) {
            if (!isUnix) {
                try {
                    Thread.sleep(samplingInterval);
                }  catch (InterruptedException ex) { /* Should not happen */
                }
            }  else { // Solaris and Linux

                long time = Timers.getCurrentTimeInCounts();
                // On Solaris, the resolution of Thread.sleep(), which boils down to the select(3C) system call, seems to be
                // around 20 ms. So we have to use our own call, which eventually calls nanosleep() and takes an argument in nanos.
                // On Linux (at least version 7.3 + patches, which I tried), nanosleep() seems to have a 20 ms resolution (or even
                // give 20 ms no matter what?), which is a documented bug (see 'man nanosleep'). Well, maybe it improves in future...
                Timers.osSleep(adjustedSamplingInterval);
                time = Timers.getCurrentTimeInCounts()  - time;

                if ((time > upperBound) && (adjustedSamplingInterval > lowerBound)) {
                    if (violationCount > 0) {
                        violationCount--;
                    } else {
                        adjustedSamplingInterval = (adjustedSamplingInterval * 95) / 100;
                        violationCount = VIOLATION_THRESHOLD;
                    }
                }
            }
            
            sample();

            if (DEBUG) {
                count++;
            }
        }

        if (DEBUG && isUnix) {
            long time = ((Timers.getCurrentTimeInCounts() - startTime) * 1000) / Timers.getNoOfCountsInSecond();
            System.out.println("JFluid sampling thread: elapsed time: " + time + " ms, avg interval: " + (((double) time) / count) + "ms, adjusted interval: " + adjustedSamplingInterval + " OS units"); // NOI18N
        }
        ThreadInfo.removeProfilerServerThread(this);
    }

    abstract void sample();

    void terminate() {
        terminated = true;

        try {
            Thread.sleep(100);
        }  catch (InterruptedException ex) { /* Should not happen */
        }
    }
}