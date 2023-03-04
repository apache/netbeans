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
package org.netbeans.modules.turbo;

import java.io.*;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Statistics support for {@link Turbo}.
 *
 * <p>Results analysis allows to determine if cache
 * have any effect.
 *
 * @author Petr Kuzel
 */
class Statistics {

    private static final Statistics NOP_INSTANCE = new NOP();
    private static final int REMOVED_BATCH_INTERVAL = 1000 *10; // 10s

    private long requests = 0;
    private long memoryHits = 0;
    private long diskHits = 0;

    // background loading
    private long threads = 0;
    private long duplicates = 0;
    private long maxQueueSize = 0;

    // Memory.liveentitiesMap utilization
    private long maxMemoryEntries = 0;
    private long gcCounter = 0;
    private long newCounter = 0;
    private int removedInvocaionCounter = 0;
    private long removedInvocationTime = System.currentTimeMillis() - REMOVED_BATCH_INTERVAL;
    /** Holds keys string reprentation to avoid memory leaks. */
    private Set recentKeys;

    // cache instance identification fields
    private static volatile int idPool = 1;
    private final int id;
    private final Exception origin;

    private PrintWriter out;

    /** Creates new statistics instance according to
     * <tt>netbeans.experimental.vcsTurboStatistics</tt> system
     * property:
     * <ul>
     *   <li><tt>none</tt> (default) no-op implementaion
     *   <li><tt>mini</tt> logs fast events
     *   <li><tt>performance</tt> logs also heavy events slowing down Turbo and increasing it's memory requirements.
     * </ul>
     */
    public static Statistics createInstance() {
        if ("none".equalsIgnoreCase(System.getProperty("netbeans.experimental.vcsTurboStatistics", "none"))) {  // NOI18N
            return NOP_INSTANCE;
        } else {
            return new Statistics();
        }
    }

    private Statistics() {
        origin = new RuntimeException();
        id = idPool++;
    }

    /**
     * Checks if additional logging required for detailed performance evaluation is required.
     */
    private static boolean logPerformance() {
        return System.getProperty("netbeans.experimental.vcsTurboStatistics", "mini").equalsIgnoreCase("performance");  // NOI18N
    }

    /** Key created adding permision to store it in memory layer. */
    public void keyAdded(Object key) {
        if (key != null) println("EK+ " + key);  // NOi18N
        newCounter++;
        long allocated = newCounter - gcCounter;
        if (allocated > maxMemoryEntries) {
            maxMemoryEntries = allocated;
        }
        if (recentKeys != null) {
            assert recentKeys.add(key.toString()) : "Key added for the second time: " + key;
        }
    }

    /** Key was removed from memory. */
    public void keyRemoved(Object key) {
        if (key != null) println("EK- " + key);  // NOi18N
        gcCounter++;
        if (recentKeys != null) {
            recentKeys.remove(key.toString());
        }
    }

    /**
     * Detect reclaimed keys. It lods first results on the second call.
     *
     * <p>It's heavy event.
     */
    public void computeRemoved(Set keys) {
        if (logPerformance() == false) return;

        if (System.currentTimeMillis() - removedInvocationTime > REMOVED_BATCH_INTERVAL) return;

        removedInvocaionCounter++;
        Iterator it = keys.iterator();
        Set currentKeys = new HashSet(keys.size());
        while (it.hasNext()) {
            String stringKey = it.next().toString();
            currentKeys.add(stringKey);
        }

        if (recentKeys != null) {
            recentKeys.removeAll(currentKeys);
            int reclaimed = recentKeys.size();
            gcCounter += reclaimed;

            if (reclaimed > 0) {
                println("MSG [" + new Date().toString() + "] reclaimed keys:" ); // NOI18N
                Iterator itr = recentKeys.iterator();
                while (itr.hasNext()) {
                    String next =  itr.next().toString();
                    println("EK- " + next); // NOI18N
                }
                println("MSG EOL reclaimed keys" ); // NOI18N
            }
        }

        removedInvocationTime = System.currentTimeMillis();
        recentKeys = currentKeys;
    }

    /** Turbo request arrived */
    public void attributeRequest() {
        requests++;
        if (requests % 1000 == 0) {
            printCacheStatistics();
        }
    }

    /** The client request was resolved by memory layer */
    public void memoryHit() {
        memoryHits++;
    }

    /** new background thread spawned */
    public void backgroundThread() {
        threads++;
    }

    /** Duplicate request eliminated from queue. */
    public void duplicate() {
        duplicates++;
    }

    public void queueSize(int size) {
        if (size > maxQueueSize) {
            maxQueueSize = size;
        }
    }

    /** The client request was resolved at providers layer */
    public void providerHit() {
        diskHits++;
    }

    public void shutdown() {
        printCacheStatistics();
        out.flush();
        out.close();
//        System.out.println("  Statistics goes to " + Statistics.logPath()); // NOI18N        
    }

    private String logPath() {
        return System.getProperty("java.io.tmpdir") + File.separator + "netbeans-versioning-turbo-" + id + ".log"; // NOI18N
    }
    
    private void printCacheStatistics() {
        println("CS  turbo.requests=" + requests); // NOI18N
        println("CS  memory.hits=" + memoryHits + " " + (((float)memoryHits/(float)requests) * 100) + "%"); // NOI18N
        println("CS  provider.hits=" + diskHits + " " + (((float)diskHits/(float)requests) * 100) + "%");  // NOI18N
        if (removedInvocaionCounter >= 2) {
            println("CS  memory.max=" + maxMemoryEntries); // NOI18N
            println("CS  memory.entries=" + (newCounter - gcCounter)); // NOI18N
            println("CS  memory.entiresReclaimingRatio=" + (((float)gcCounter/(float)newCounter) * 100) + "%");  // NOI18N
        } else {
            println("MSG No memory utilization data known, use -J-Dnetbeans.experimental.vcsTurboStatistics=performance.");
        }
        println("CS  queue.threads=" + threads + " queue.duplicates=" + duplicates + " queue.maxSize=" + maxQueueSize);  // NOI18N
        println("MSG --"); // NOI18N
        println("MSG turbo.log.Statistics on " + new Date().toString()); // NOI18N
    }

    private synchronized void println(String s) {
        if (out == null) {
            String filePath = logPath();
            try {
                out = new PrintWriter(new BufferedWriter(new FileWriter(filePath), 512));
            } catch (IOException e) {
                out = new PrintWriter(new OutputStreamWriter(System.out), true);
            }
            out.println("MSG EK followed by +/- denotes new memory cache entry/releasing it"); // NOI18N
            out.println("MSG CS describes summary statistics of memory and disk caches"); // NOI18N
            out.println("MSG the MSG prefix denotes free form messages"); // NOI18N
            out.println("MSG turbo.Statistics instance serving:\n");  // NOI18N
            StackTraceElement elements[] = origin.getStackTrace();
            for (int i = 0; i < elements.length; i++) {
                StackTraceElement element = elements[i];
                out.println("MSG " + element.toString() ); // NOI18N
            }
            out.println();
        }
        out.println(s);
    }

    /** Logs nothing. */
    private static final class NOP extends Statistics {
        public void keyAdded(Object key) {
        }

        public void computeRemoved(Set keys) {
        }

        public void attributeRequest() {
        }

        public void memoryHit() {
        }

        public void backgroundThread() {
        }

        public void duplicate() {
        }

        public void queueSize(int size) {
        }

        public void providerHit() {
        }

        public void shutdown() {
        }

    }
}
