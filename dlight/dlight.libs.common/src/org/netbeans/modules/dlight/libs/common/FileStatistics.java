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
package org.netbeans.modules.dlight.libs.common;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.openide.filesystems.FileSystem;
import org.openide.modules.OnStop;

/**
 *
 */
public class FileStatistics {
    
    private static final ConcurrentHashMap<FileSystem, FileStatistics> instances = new ConcurrentHashMap<FileSystem, FileStatistics>();
    
    private static final boolean REPORT_FIRST = Boolean.getBoolean("dlight.file.read.statistics.report.first"); //NOI18N
    private static final String PREFIX = "### FRS "; //NOI18N
    
    private static final Set<String> extensionsToInclude;
    private static final Set<String> namesToExclude;
    static {
        extensionsToInclude = new TreeSet<String>();
        String extList = System.getProperty("dlight.file.read.statistics.include.extensions"); //NOI18N
        if (extList != null) {
            String[] splitExt = extList.split(","); // NOI18N
            extensionsToInclude.addAll(Arrays.asList(splitExt));
        }
        
        namesToExclude = new TreeSet<String>();
        String nameList = System.getProperty("dlight.file.read.statistics.exclude.names"); //NOI18N
        if (nameList != null) {
            String[] splitNam = nameList.split(","); // NOI18N
            namesToExclude.addAll(Arrays.asList(splitNam));
        }
    }
    
    private final PrintStream out = System.err;

    private final FileSystem fileSystem;
    
    private final Map<String, StatEntry> stacks = new HashMap<String, StatEntry>();
    private final Map<String, StatEntry> files = new HashMap<String, StatEntry>();
    private final Object lock = new Object();
    
    public static FileStatistics getInstance(FileSystem fs) {
        FileStatistics instance = instances.get(fs);
        if (instance == null) {
            instance = new FileStatistics(fs);
            FileStatistics oldInstance = instances.putIfAbsent(fs, instance);
            if (oldInstance != null) {
                instance = oldInstance;
            }
        }
        return instance;
    }

    private FileStatistics(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    private List<StackTraceElement> filter(StackTraceElement[] stack) {
        List<StackTraceElement> result = new ArrayList<StackTraceElement>();
        String thisClassName = getClass().getName();
        for (int i = 0; i < stack.length; i++) {
            StackTraceElement st = stack[i];
            if (!st.getClassName().startsWith(thisClassName) && 
                    !st.getClassName().startsWith("java.util.logging") && // NOI18N
                    !st.getClassName().startsWith("java.lang.Thread")) { // NOI18N
                result.add(st);
            }                
        }
        return result;
    }
    
    private static <T> List<T> removeCycles(List<T> stack) {
        List<T> result = new ArrayList<T>();
        outer:
        for (int curr = 0; curr < stack.size(); curr++) {            
            T currElement = stack.get(curr);
            for (int prev = 0; prev < curr; prev++) {
                if (currElement.equals(stack.get(prev))) {
                    // it's probably a cycle; let's check
                    int distance = curr - prev;
                    if (curr + distance <= stack.size()) {                        
                        boolean cycle = true;
                        for (int i = 1; i < distance; i++) {
                            if (!stack.get(curr+i).equals(stack.get(prev+i))) {
                                cycle = false;
                                break;
                            }
                        }
                        if (cycle) {
                            curr += distance - 1;
                            continue outer;
                        }
                    }
                }
            }
            result.add(currElement);
        }        
        return result;        
    }
        
    public void clear() {
        synchronized (lock) {
            files.clear();
            stacks.clear();
        }
    }

    public void logPath(String path) {
        
        String fileName = PathUtilities.getBaseName(path);
        if (namesToExclude.contains(fileName)) {
            return;
        }
        int dotIndex = fileName.lastIndexOf('.'); // NOI18N
        String ext = (dotIndex > 0) ? fileName.substring(dotIndex + 1) : "";
        if (!extensionsToInclude.contains(ext)) {
            return;
        }

        // stack key
        
        List<StackTraceElement> stack = filter(Thread.currentThread().getStackTrace());
        stack = removeCycles(stack);
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement st : stack) {
            sb.append(st.getClassName()).append('.').append(st.getMethodName());
            sb.append('(').append(st.getFileName()).append(':').append(st.getLineNumber()).append(')');
            sb.append('\n');
        }
        String stackKey = sb.toString();
        
        StatEntry pathEntry, stackEntry;        
        boolean report = false;
        synchronized (lock) {            
            pathEntry = files.get(path);
            if (pathEntry == null) {
                pathEntry = new StatEntry(path, stackKey);                
                files.put(path, pathEntry);
            }            
            stackEntry = stacks.get(stackKey);
            if (stackEntry == null) {
                stackEntry = new StatEntry(path, stackKey);
                stacks.put(stackKey, stackEntry);
                if (REPORT_FIRST) {
                    report = true;
                }
            }            
        }
        pathEntry.increment();        
        stackEntry.increment();
        if (report) {
            report(stackEntry);
            out.flush();
        }
    }
        
    private void printf(String pattern, Object... args) {
        String formattedString = String.format(pattern, args);
        String[] lines = formattedString.split("\n"); // NOI18N
        for (int i = 0; i < lines.length; i++) {
            out.printf("%s%s\n", PREFIX,  lines[i]); // NOI18N
        }
        out.flush();
    }
    
    @OnStop
    public static class Reporter implements Runnable {
        @Override
        public void run() {
            if (!extensionsToInclude.isEmpty()) {
                for (FileStatistics fileStatistics : instances.values()) {
                    fileStatistics.report();
                }
            }
        }        
    }
    
    private void report(StatEntry entry) {
        String stack = entry.getStack();
        printf("%6d @%d  1-st path: %s\n", entry.getCount(), stack.hashCode(), entry.getPath()); // NOI18N
        printf("    %s\n", stack); // NOI18N        
    }
    
    public void report() {
        int readCount = 0;
        for (StatEntry cnt : stacks.values()) {
            readCount += cnt.getCount();
        }
        {
            out.printf("\n\n\n"); // NOI18N
            printf("\nFile Read Statistics by Path [%s]\n", fileSystem.getDisplayName()); // NOI18N
            printf("Files count: %d reads count: %d\n", files.size(), readCount); // NOI18N
            printf("\t   Cnt  File\n"); // NOI18N
            for (Map.Entry<String, StatEntry> mapEntry : sortByCount(files)) {
                StatEntry statEntry = mapEntry.getValue();
                printf("\t%6d  %s\n", statEntry.getCount(), mapEntry.getKey()); // NOI18N
            }
            out.flush();
        }

        {
            printf("\n\nFile Read Statistics by Stack [%s]\n", fileSystem.getDisplayName()); // NOI18N
            printf("Stacks count: %d reads count: %d\n", stacks.size(), readCount); // NOI18N
            for (Map.Entry<String, StatEntry> mapEntry : sortByCount(stacks)) {
                StatEntry statEntry = mapEntry.getValue();
                assert statEntry.getStack().equals(mapEntry.getKey());
                report(statEntry);
            }
            out.flush();
        }
        
    }
    
    private static List<Map.Entry<String, StatEntry>> sortByCount(Map<String, StatEntry> map) {
        List<Map.Entry<String, StatEntry>> sorted = new ArrayList<Map.Entry<String, StatEntry>>(map.entrySet());                
        Collections.sort(sorted, new Comparator<Map.Entry<String, StatEntry>>() {
            @Override
            public int compare(Map.Entry<String, StatEntry> o1, Map.Entry<String, StatEntry> o2) {
                int cnt1 = o1.getValue().getCount();
                int cnt2 = o2.getValue().getCount();
                return (cnt1 == cnt2) ? o1.getKey().compareTo(o2.getKey()) : (cnt2 - cnt1);
            }

        });        
        return sorted;
    }

    private static class StatEntry {
        
        private int count;
        
        /** 1-st (or the only) path */
        private final String path;
        
        /** 1-st (or the only) stack */
        private final String stack;

        public StatEntry(String path, String stack) {
            this.path = path;
            this.stack = stack;
        }
        
        public synchronized int getCount() {
            return count;
        }
        
        public synchronized void increment() {
            count++;
        }
        
        public String getPath() {
            return path;
        }

        public String getStack() {
            return stack;
        }                
    }
}
