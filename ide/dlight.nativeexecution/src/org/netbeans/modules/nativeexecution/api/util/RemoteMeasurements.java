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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/*package*/ final class RemoteMeasurements {

    private static final ConcurrentHashMap<Integer, String> stacks = new ConcurrentHashMap<>();
    // [stackid-category-args]-to-statistics
    private final ConcurrentHashMap<Integer, Stat> stats = new ConcurrentHashMap<>();
    private final AtomicLong upTraffic = new AtomicLong();
    private final AtomicLong downTraffic = new AtomicLong();
    private final String name;
    private final long startWallTime;
    
    RemoteMeasurements(String name) {
        this.name = "'" + name + "'"; // NOI18N
        startWallTime = System.currentTimeMillis();
    }

    public RemoteStatistics.ActivityID startChannelActivity(CharSequence category, CharSequence... args) {
        Stat stat = new Stat(category, args);
        int hashCode = stat.hashCode();
        Stat prevStat = stats.putIfAbsent(hashCode, stat);
        if (prevStat != null) {
            stat = prevStat;
        }

        stat.count.incrementAndGet();
        return new StatKey(hashCode, System.currentTimeMillis());
    }

    public void stopChannelActivity(Object activityID) {
        stopChannelActivity(activityID, 0);
    }

    public void stopChannelActivity(Object activityID, long supposedTraffic) {
        StatKey key = (StatKey) activityID;
        Stat stat = stats.get(key.statID);
        assert stat != null;
        stat.deltaTime.addAndGet(System.currentTimeMillis() - key.startTime);
        stat.supposedTraffic.addAndGet(supposedTraffic);
    }

    void dump(PrintStream out) {
        try {
            dumpTrafficStatistics(out);
            dumpCategoriesStatistics(out);
            dumpCategoriesArgsStatistics(out);
            dumpCategoriesStacksStatistics(out);
        } finally {
            out.printf("Total wall time 20%s [ms]: %8d\n", name,  System.currentTimeMillis() - startWallTime); // NOI18N
        }
    }

    private void dumpTrafficStatistics(PrintStream out) {
        out.printf("Upload traffic   %20s [bytes]: %10d\n", name, upTraffic.get()); // NOI18N
        out.printf("Download traffic %20s [bytes]: %10d\n", name, downTraffic.get()); // NOI18N
    }

    private void dumpCategoriesArgsStatistics(PrintStream out) {
        HashMap<String, Counters> data = new HashMap<>();

        for (Stat stat : stats.values()) {
            String id = stat.category + "%" + Arrays.toString(stat.args); // NOI18N
            Counters counters = data.get(id);
            if (counters == null) {
                counters = new Counters();
                data.put(id, counters);
            }

            counters.add(stat);
        }

        out.printf("== Arguments statistics start ==\n"); // NOI18N
        out.printf("%20s|%8s|%8s|%10s|%s\n", "Category", "Count", "Time", "~Traffic", "Args"); // NOI18N

        LinkedList<Map.Entry<String, Counters>> dataList = new LinkedList<>(data.entrySet());
        dataList.sort(new CategoriesStatComparator());

        for (Map.Entry<String, Counters> entry : dataList) {
            String cat = entry.getKey();
            int idx = cat.indexOf('%');
            Counters cnts = entry.getValue();
            out.printf("%20s|%8d|%8d|%10d|%s\n",  cat.substring(0, idx), cnts.count.get(), cnts.time.get(), cnts.supposedTraffic.get(), cat.substring(idx + 1)); // NOI18N
        }

        out.printf("== Arguments statistics end ==\n"); // NOI18N
    }

    private void dumpCategoriesStacksStatistics(PrintStream out) {
        HashMap<String, Counters> data = new HashMap<>();
        
        for (Stat stat : stats.values()) {
            String id = stat.stackID + "%" + stat.category; // NOI18N
            Counters counters = data.get(id);
            if (counters == null) {
                counters = new Counters();
                data.put(id, counters);
            }

            counters.add(stat);
        }

        out.printf("== Categories stacks statistics start ==\n"); // NOI18N
        out.printf("%20s|%8s|%8s|%10s|%s\n", "Category", "Count", "Time", "~Traffic", "Args"); // NOI18N

        LinkedList<Map.Entry<String, Counters>> dataList = new LinkedList<>(data.entrySet());
        dataList.sort(new CategoriesStatComparator());

        for (Map.Entry<String, Counters> entry : dataList) {
            String cat = entry.getKey();
            int idx = cat.indexOf('%');
            Counters cnts = entry.getValue();
            Integer stackID = Integer.valueOf(cat.substring(0, idx));
            String category = cat.substring(idx + 1);
            out.printf("%20s|%8d|%8d|%10d|%s\n", category, cnts.count.get(), cnts.time.get(), cnts.supposedTraffic.get(), stacks.get(stackID)); // NOI18N
        }

        out.printf("== Categories stacks statistics end ==\n"); // NOI18N
    }

    private void dumpCategoriesStatistics(PrintStream out) {
        long totalTime = 0;
        long totalTraffic = 0;
        HashMap<String, Counters> map = new HashMap<>();
        for (Stat stat : stats.values()) {
            Counters counters = map.get(stat.category);
            if (counters == null) {
                counters = new Counters();
                map.put(stat.category, counters);
            }
            counters.add(stat);
        }

        out.printf("== Categories stat begin ==\n"); // NOI18N
        out.printf("%20s|%8s|%8s|%10s|%s\n", "Category", "Count", "Time", "~Traffic", "Args"); // NOI18N
        for (Map.Entry<String, Counters> entry : map.entrySet()) {
            long dt = entry.getValue().time.get();
            long supposedTraffic = entry.getValue().supposedTraffic.get();
            out.printf("%20s|%8d|%8d|%s\n", entry.getKey(), entry.getValue().count.get(), dt, supposedTraffic, entry.getValue().args.size()); // NOI18N
            totalTime += dt;
        }
        out.printf("== Categories stat end ==\n"); // NOI18N

        out.printf("Total time by all categories [ms]:                %20d\n", totalTime); // NOI18N
        out.printf("Total supposed traffic by all categories [bytes]: %20d\n", totalTraffic); // NOI18N
    }

    private static int getStackID() {
        if (!RemoteStatistics.COLLECT_STACKS) {
            return -1;
        }

        final StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace != null) {
            String curFile;
            String prevFile = null;
            for (int i = stackTrace.length - 1; i >= 0; i--) {
                StackTraceElement st = stackTrace[i];
                String filename = st.getFileName();
                curFile = filename.substring(0, filename.lastIndexOf('.'));
                sb.append(curFile.equals(prevFile) ? "" : curFile).append(':').append(st.getLineNumber()).append(';'); // NOI18N
                prevFile = curFile;
                String className = st.getClassName();
                if (className.startsWith("com.jcraft.jsch")) { // NOI18N
                    sb.append("..."); // NOI18N
                    break;
                }
            }
        }
        String stack = sb.toString();
        Integer stackID = stack.hashCode();
        stacks.putIfAbsent(stackID, stack);

        return stackID;
    }

    void bytesUploaded(int bytes) {
        upTraffic.addAndGet(bytes);
    }

    void bytesDownloaded(int bytes) {
        downTraffic.addAndGet(bytes);
    }

    private static final class Stat {

        private final String category;
        private final String[] args;
        private final int stackID;
        private final AtomicLong deltaTime = new AtomicLong(0);
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong supposedTraffic = new AtomicLong(0);

        public Stat(CharSequence category, CharSequence... args) {
            stackID = getStackID();
            this.category = category.toString();
            if (args == null) {
                this.args = null;
            } else {
                this.args = new String[args.length];
                int i = 0;
                for (CharSequence arg : args) {
                    this.args[i++] = arg.toString();
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Stat other = (Stat) obj;
            if (this.stackID != other.stackID) {
                return false;
            }
            if ((this.category == null) ? (other.category != null) : !this.category.equals(other.category)) {
                return false;
            }
            if (!Arrays.deepEquals(this.args, other.args)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + this.stackID;
            hash = 79 * hash + (this.category != null ? this.category.hashCode() : 0);
            hash = 79 * hash + Arrays.deepHashCode(this.args);
            return hash;
        }
    }

    private static final class Counters {

        private final AtomicLong count = new AtomicLong();
        private final AtomicLong time = new AtomicLong();
        private final AtomicLong supposedTraffic = new AtomicLong();
        private final HashSet<Integer> args = new HashSet<>();

        private void add(Stat stat) {
            count.addAndGet(stat.count.get());
            time.addAndGet(stat.deltaTime.get());
            args.add(Arrays.deepHashCode(stat.args));
            supposedTraffic.addAndGet(stat.supposedTraffic.get());
        }       
    }

    private static class CategoriesStatComparator implements Comparator<Map.Entry<String, Counters>> {

        @Override
        public int compare(Map.Entry<String, Counters> o1, Map.Entry<String, Counters> o2) {
            String s1 = o1.getKey();
            String s2 = o2.getKey();
            int idx1 = s1.indexOf('%');
            int idx2 = s2.indexOf('%');
            int cmp = s1.substring(0, idx1).compareTo(s2.substring(0, idx2));
            if (cmp != 0) {
                return cmp;
            }
            long o1Val = o1.getValue().time.get();
            long o2Val = o2.getValue().time.get();
            return (o1Val < o2Val ? 1 : (o1Val == o2Val ? 0 : -1));
        }
    }

    private static final class StatKey extends RemoteStatistics.ActivityID {

        private final int statID;
        private final long startTime;

        private StatKey(int statID, long startTime) {
            this.statID = statID;
            this.startTime = startTime;
        }
    }
}
