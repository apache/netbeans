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
package org.netbeans.modules.cnd.discovery.performance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public class AnalyzeStat {

    private AnalyzeStat() {
    }

    static void process(TreeMap<String, AgregatedStat> gatherStat) {
        upEmptyFolder(gatherStat);
        getBigUnused(gatherStat);
        groupByReadingSpeed(gatherStat);
        dumpAll(gatherStat);
        getSlowReading(gatherStat);
    }

    static List<Map.Entry<String, AgregatedStat>> getBigUnused(TreeMap<String, AgregatedStat> gatherStat) {
        List<Map.Entry<String, AgregatedStat>> orphan = new ArrayList<>();
        List<Map.Entry<String, AgregatedStat>> list = new ArrayList<>(gatherStat.entrySet());
        int i = 0;
        while (i < list.size()) {
            Map.Entry<String, AgregatedStat> entry = list.get(i);
            String path = entry.getKey();
            AgregatedStat stat = entry.getValue();
            if (stat.readNumber == 0 && stat.parseNumber == 0 && stat.itemTime / PerformanceIssueDetector.NANO_TO_SEC > 10) {
                if (i + 1 < list.size()) {
                    Map.Entry<String, AgregatedStat> next = list.get(i + 1);
                    if (!(next.getKey().startsWith(path + "/") || next.getKey().startsWith(path + "\\"))) { // NOI18N
                        AgregatedStat copy = new AgregatedStat();
                        mergeEntryes(copy, entry.getValue());
                        orphan.add(new MyEntry(entry.getKey(), copy));
                    }
                }
            }
            i++;
        }
        list.clear();
        Collections.sort(orphan, new Comparator<Map.Entry<String, AgregatedStat>>() {
            @Override
            public int compare(Map.Entry<String, AgregatedStat> o1, Map.Entry<String, AgregatedStat> o2) {
                //return o2.getValue().itemNumber - o1.getValue().itemNumber;
                return (int)(o2.getValue().itemTime/1000/1000 - o1.getValue().itemTime/1000/1000);
            }
        });

        return orphan;
    }

    static List<Map.Entry<String, AgregatedStat>> getSlowReading(TreeMap<String, AgregatedStat> gatherStat) {
        List<Map.Entry<String, AgregatedStat>> slow = new ArrayList<>();
        List<Map.Entry<String, AgregatedStat>> list = new ArrayList<>(gatherStat.entrySet());
        int i = 0;
        while (i < list.size()) {
            Map.Entry<String, AgregatedStat> entry = list.get(i);
            String path = entry.getKey();
            AgregatedStat stat = entry.getValue();
            if (stat.readTime / PerformanceIssueDetector.NANO_TO_SEC > 10 && stat.readCPU > 0 && stat.readTime / stat.readCPU > 10) {
                if (i + 1 < list.size()) {
                    Map.Entry<String, AgregatedStat> next = list.get(i + 1);
                    if (!(next.getKey().startsWith(path + "/") || next.getKey().startsWith(path + "\\"))) { // NOI18N
                        AgregatedStat copy = new AgregatedStat();
                        mergeEntryes(copy, entry.getValue());
                        slow.add(new MyEntry(entry.getKey(), copy));
                    }
                }
            }
            i++;
        }
        list.clear();
        Collections.sort(slow, new Comparator<Map.Entry<String, AgregatedStat>>() {
            @Override
            public int compare(Map.Entry<String, AgregatedStat> o1, Map.Entry<String, AgregatedStat> o2) {
                int k1 = (int)(o1.getValue().readTime/o1.getValue().readCPU);
                int k2 = (int)(o2.getValue().readTime/o2.getValue().readCPU);
                return k2 - k1;
            }
        });
        return slow;
    }

    static void upEmptyFolder(TreeMap<String, AgregatedStat> gatherStat) {
        List<Map.Entry<String, AgregatedStat>> list = new ArrayList<>(gatherStat.entrySet());
        int i = 0;
        while(i < list.size()) {
            Map.Entry<String, AgregatedStat> entry = list.get(i);
            String path = entry.getKey();
            AgregatedStat stat = entry.getValue();
            if (stat.readNumber == 0 && stat.parseNumber == 0) {
                boolean doAgregate = true;
                int nextItem = i;
                int addItem = 0;
                long addTime = 0;
                long addCPU = 0;
                long addUser = 0;
                for(int j = i + 1; j < list.size(); j++) {
                    Map.Entry<String, AgregatedStat> next = list.get(j);
                    if (next.getKey().startsWith(path+"/") || next.getKey().startsWith(path+"\\")) { //NOI18N
                        if (next.getValue().parseNumber == 0 && next.getValue().readNumber == 0) {
                            addItem += next.getValue().itemNumber;
                            addTime += next.getValue().itemTime;
                            addCPU += next.getValue().itemCPU;
                            addUser += next.getValue().itemUser;
                            nextItem = j;
                        } else {
                            doAgregate = false;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (doAgregate) {
                    stat.itemNumber += addItem;
                    stat.itemTime += addTime;
                    stat.itemCPU += addCPU;
                    stat.itemUser += addUser;
                    for(int j = i + 1; j <= nextItem; j++) {
                        Map.Entry<String, AgregatedStat> next = list.get(j);
                        gatherStat.remove(next.getKey());
                    }
                    i = nextItem;
                }
            }
            i++;
        }
    }

    static void groupByReadingSpeed(TreeMap<String, AgregatedStat> gatherStat) {
        while(true) {
            int start = gatherStat.size();
            getGroupByReadingSpeedImpl(gatherStat);
            if (gatherStat.size() == start) {
                break;
            }
        }
    }

    private static void getGroupByReadingSpeedImpl(TreeMap<String, AgregatedStat> gatherStat) {
        List<Map.Entry<String, AgregatedStat>> list = new ArrayList<>(gatherStat.entrySet());
        int i = 0;
        while (i < list.size()) {
            Map.Entry<String, AgregatedStat> entry = list.get(i);
            String path = entry.getKey();
            AgregatedStat stat = entry.getValue();
            int segment = 0;
            boolean hasRoot =true;
            for (int j = i + 1; j < list.size(); j++) {
                Map.Entry<String, AgregatedStat> next = list.get(j);
                if (next.getKey().startsWith(path + "/") || next.getKey().startsWith(path + "\\")) { //NOI18N
                    String subFolder = next.getKey().substring(path.length() + 1);
                    if (subFolder.indexOf('\\') > 0 || subFolder.indexOf('/') > 0) { //NOI18N
                        segment = 0;
                        break;
                    }
                    segment = j;
                } else {
                    break;
                }
            }
            if (segment == 0) {
                int k = path.lastIndexOf('/'); //NOI18N
                if (k < 0) {
                    k = path.lastIndexOf('\\'); //NOI18N
                }
                if (k > 0) {
                    String dir = path.substring(0, k);
                    if (i > 0) {
                        if (dir.equals(list.get(i-1).getKey()) || list.get(i-1).getKey().startsWith(dir+"/") || list.get(i-1).getKey().startsWith(dir+"\\")) { //NOI18N
                            i++;
                            continue;
                        }
                    }
                    for (int j = i; j < list.size(); j++) {
                        Map.Entry<String, AgregatedStat> next = list.get(j);
                        if (next.getKey().startsWith(dir + "/") || next.getKey().startsWith(dir + "\\")) { //NOI18N
                            String subFolder = next.getKey().substring(dir.length() + 1);
                            if (subFolder.indexOf('\\') > 0 || subFolder.indexOf('/') > 0) { //NOI18N
                                segment = 0;
                                break;
                            }
                            segment = j;
                        } else {
                            break;
                        }
                    }
                    if (segment == i) {
                        i++;
                        continue;
                    }
                    if (segment > 0) {
                        stat = new AgregatedStat();
                        gatherStat.put(dir, stat);
                        hasRoot = false;
                    }
                }
            }
            if (segment > 0) {
                long time = 0;
                long cpu = 0;
                //long line = 0;
                for (int j = i; j <= segment; j++) {
                    Map.Entry<String, AgregatedStat> next = list.get(j);
                    AgregatedStat value = next.getValue();
                    time += value.readTime;
                    cpu += value.readCPU;
                    //line += value.parseLines;
                }
                long averigeRatio = 0;
                if (time > 0 && cpu > 0) {
                    averigeRatio = time / cpu;
                }
                boolean canMerge = true;
                if (averigeRatio > 0) {
                    for (int j = i; j <= segment; j++) {
                        Map.Entry<String, AgregatedStat> next = list.get(j);
                        AgregatedStat value = next.getValue();
                        if (value.readTime / PerformanceIssueDetector.NANO_TO_SEC > 10 && value.readCPU > 0) {
                            long currentRatio = value.readTime / value.readCPU;
                            if (averigeRatio / 3 < currentRatio && currentRatio < averigeRatio * 3) {
                            } else {
                                canMerge = false;
                                break;
                            }
                        }
                    }
                }
                if (canMerge) {
                    for (int j = hasRoot ? i + 1 : i; j <= segment; j++) {
                        Map.Entry<String, AgregatedStat> next = list.get(j);
                        mergeEntryes(stat, next.getValue());
                        gatherStat.remove(next.getKey());
                    }
                }
                i = segment;
            }
            i++;
        }
    }

    private static void mergeEntryes(AgregatedStat stat, AgregatedStat from) {
        stat.itemNumber += from.itemNumber;
        stat.itemTime += from.itemTime;
        stat.itemCPU += from.itemCPU;
        stat.itemUser += from.itemUser;

        stat.readNumber += from.readNumber;
        stat.readBytes += from.readBytes;
        stat.readLines += from.readLines;
        stat.readTime += from.readTime;
        stat.readCPU += from.readCPU;
        stat.readUser += from.readUser;

        stat.parseNumber += from.parseNumber;
        stat.parseLines += from.parseLines;
        stat.parseTime += from.parseTime;
        stat.parseCPU += from.parseCPU;
        stat.parseUser += from.parseUser;
    }

    static void dumpAll(TreeMap<String, AgregatedStat> gatherStat) {
        if (!PerformanceIssueDetector.LOG.isLoggable(PerformanceIssueDetector.level)) {
            return;
        }
        StringBuilder buf = new StringBuilder();
        buf.append("Projects statistic\nitem\ttime\tCPU\tratio\tread\tlines\ttime\tCPU\tratio\tparse\tlines\ttime\tCPU\tratio\tfolder");  //NOI18N
        for (Map.Entry<String, AgregatedStat> entry : gatherStat.entrySet()) {
            AgregatedStat value = entry.getValue();
            buf.append('\n'); //NOI18N
            buf.append(PerformanceIssueDetector.format(value.itemNumber)).append('\t'); //NOI18N
            buf.append(PerformanceIssueDetector.format(value.itemTime/PerformanceIssueDetector.NANO_TO_SEC)).append('\t'); //NOI18N
            buf.append(PerformanceIssueDetector.format(value.itemCPU/PerformanceIssueDetector.NANO_TO_SEC)).append('\t'); //NOI18N
            if (value.itemTime / PerformanceIssueDetector.NANO_TO_SEC > 10 && value.itemCPU > 0) {
                buf.append(PerformanceIssueDetector.format(value.itemTime/value.itemCPU)).append('\t'); //NOI18N
            } else {
                buf.append('-').append('\t'); //NOI18N
            }

            buf.append(PerformanceIssueDetector.format(value.readNumber)).append('\t'); //NOI18N
            buf.append(PerformanceIssueDetector.format(value.readLines)).append('\t'); //NOI18N
            buf.append(PerformanceIssueDetector.format(value.readTime/PerformanceIssueDetector.NANO_TO_SEC)).append('\t'); //NOI18N
            buf.append(PerformanceIssueDetector.format(value.readCPU/PerformanceIssueDetector.NANO_TO_SEC)).append('\t'); //NOI18N
            if (value.readTime / PerformanceIssueDetector.NANO_TO_SEC > 10 && value.readCPU > 0) {
                buf.append(PerformanceIssueDetector.format(value.readTime/value.readCPU)).append('\t'); //NOI18N
            } else {
                buf.append('-').append('\t'); //NOI18N
            }

            buf.append(PerformanceIssueDetector.format(value.parseNumber)).append('\t'); //NOI18N
            buf.append(PerformanceIssueDetector.format(value.parseLines)).append('\t'); //NOI18N
            buf.append(PerformanceIssueDetector.format(value.parseTime/PerformanceIssueDetector.NANO_TO_SEC)).append('\t'); //NOI18N
            buf.append(PerformanceIssueDetector.format(value.parseCPU/PerformanceIssueDetector.NANO_TO_SEC)).append('\t'); //NOI18N

            if (value.parseTime / PerformanceIssueDetector.NANO_TO_SEC > 10 && value.parseCPU > 0) {
                buf.append(PerformanceIssueDetector.format(value.parseTime/value.parseCPU)).append('\t'); //NOI18N
            } else {
                buf.append('-').append('\t'); //NOI18N
            }

            buf.append(entry.getKey());
        }
        buf.append('\n'); //NOI18N
        PerformanceIssueDetector.LOG.log(PerformanceIssueDetector.level, buf.toString());
    }

    static final class AgregatedStat {
        int itemNumber;
        long itemTime;
        long itemCPU;
        long itemUser;
        int readNumber;
        long readBytes;
        long readLines;
        long readTime;
        long readCPU;
        long readUser;
        int parseNumber;
        long parseLines;
        long parseTime;
        long parseCPU;
        long parseUser;
    }
    private static final class MyEntry implements Map.Entry<String, AgregatedStat> {
        private final String key;
        private AgregatedStat value;

        MyEntry(String key, AgregatedStat value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public AgregatedStat getValue() {
            return value;
        }

        @Override
        public AgregatedStat setValue(AgregatedStat value) {
            AgregatedStat prev = this.value;
            this.value = value;
            return prev;
        }
    }
}
