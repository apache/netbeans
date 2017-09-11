/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
