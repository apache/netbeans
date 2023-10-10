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
package org.netbeans.modules.groovy.editor.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.control.Phases;

/**
 * Performance data collected from groovy parsing
 * @author sdedic
 */
public class PerfData {
    public static final Logger LOG = Logger.getLogger(PerfData.class.getName());
    
    public static PerfData global = new PerfData();
    
    private Map<Integer, Long> parserPhaseTime = new TreeMap<>();
    private Map<String, Long> perfCounters = new TreeMap<>();
    private Map<Integer, Map<String, Long>> visitorCounters = new TreeMap<>();
    private int fileCount;
    
    private PerfData collector;
    
    private static ThreadLocal<PerfData> threadInstance = new ThreadLocal<>();
    
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable exception) throws T {
            throw (T) exception;
    }        
    
    /**
     * Retrieves the contextual perf counters. If none are set, returns global counters.
     * @return context counters, or global ones.
     */
    public static PerfData context() {
        PerfData d = threadInstance.get();
        return d == null ? global : d;
    }

    /**
     * Helper wrapper that establishes a thread-local context instance of {@link PerfData} that can be retrieved by
     * {@link #context}.
     * @param <T> result type of the task
     * @param data perfdata instance
     * @param c the task to execute
     * @return data computed by the task
     */
    public static <T> T withPerfData(PerfData data, Callable<T> c) {
        PerfData old = threadInstance.get();
        try {
            threadInstance.set(data);
            return c.call();
        } catch (Exception ex) {
            sneakyThrow(ex);
        } finally {
            if (old != null) {
                threadInstance.set(old);
            } else {
                threadInstance.remove();
            }
        }
        return null;
    }

    public PerfData() {
        collector = global;
    }

    public void setCollector(PerfData collector) {
        this.collector = collector;
    }
    
    public void clear() {
        parserPhaseTime.clear();
        perfCounters.clear();
        visitorCounters.clear();
        fileCount = 0;
    }
    
    public void addParserPhase(int phaseId, long time) {
        parserPhaseTime.merge(phaseId, time, 
                (a, b) -> a + b
        );
    }
    
    public void addVisitorTime(int stage, String visitorKey, long time) {
        visitorCounters.computeIfAbsent(stage, (s) -> new HashMap<>()).
            merge(visitorKey, time, (a, b) -> a + b);
    }
    
    public void addPerfCounter(String id, long time) {
        perfCounters.merge(id, time, (a, b) -> a + b);
    }
    
    public void addPerfStats(PerfData other) {
        fileCount++;
        merge(other);
    }
    
    public void merge(PerfData other) {
        parserPhaseTime.forEach(this::addParserPhase);
        perfCounters.forEach(this::addPerfCounter);

        for (int i = Phases.INITIALIZATION; i <= Phases.ALL; i++) {
            Map<String, Long> vc = other.visitorCounters.get(i);
            if (vc == null) {
                continue;
            }

            for (Map.Entry<String, Long> it : vc.entrySet()) {
                addVisitorTime(i, it.getKey(), it.getValue());
            }
        }
    }

    static Map<Class, String> lambdaClassNames = new HashMap<>();
    
    public static String phaseToString(int phase) {
        String s;
        switch (phase) {
            case Phases.INITIALIZATION: s = "initialization"; break;
            case Phases.PARSING: s = "parsing"; break;
            case Phases.CONVERSION: s = "conversion"; break;
            case Phases.SEMANTIC_ANALYSIS: s = "semanticAnalysis"; break;
            case Phases.CANONICALIZATION: s = "canonicalization"; break;
            case Phases.INSTRUCTION_SELECTION: s = "instructionSelection"; break;
            case Phases.CLASS_GENERATION: s = "classGeneration"; break;
            case Phases.OUTPUT: s = "output"; break;
            case Phases.FINALIZATION: s = "finalization"; break;
            default: s = "" + phase; break;
        }
        return s;
    }
    
    private boolean useAvg;
    
    public void dumpStatsAndMerge() {
        if (fileCount == 0) {
            fileCount = 1;
        }
        if (fileCount > 1) {
            LOG.log(Level.FINER, "File count: {0}", fileCount);
        }
        useAvg = false;
        dumpStats();
        if (fileCount > 1) {
           LOG.log(Level.FINER, "\n--- Average counters");
           useAvg = true;
           dumpStats();
        }
        LOG.log(Level.FINER, "------------------------------------------------\n\n");
        if (this.collector != null) {
            collector.addPerfStats(this);
        }
    }
    
    void dumpStats() {
        if (parserPhaseTime.isEmpty()) {
            return;
        }
        LOG.finer("Phase times:");
        for (int i = 0; i <= Phases.ALL; i++) {
            Long d = parserPhaseTime.get(i);
            if (d == null) {
                continue;
            }
            LOG.log(Level.FINER, "\t" + phaseToString(i) + ":\t" + avg(d));
        }
        
        LOG.log(Level.FINER, "\nPhase - visitor statistics:");
        for (int i = Phases.INITIALIZATION; i <= Phases.ALL; i++) {
            Map<String, Long> vc = visitorCounters.get(i);
            if (vc == null || vc.isEmpty()) {
                continue;
            }
            LOG.log(Level.FINER, "Visitors from {0}", phaseToString(i));
            List<String> visitors = new ArrayList<>(vc.keySet());
            Collections.sort(visitors);
            for (String c : visitors) {
                LOG.log(Level.FINER, "\t{0}:\t{1}", new Object[] { c, avg(vc.get(c)) });
            }
        }
        LOG.log(Level.FINER, "Performance counters:");
        List<String> keys = new ArrayList<>(perfCounters.keySet());
        Collections.sort(keys);
        for (String s : keys) {
            LOG.log(Level.FINER, "\t{0}:\t{1}", new Object[] { s, avg(perfCounters.get(s)) });
        }
    }
    
    private String avg(long time) {
        if (!useAvg || fileCount < 2) {
            return String.valueOf(time);
        }
        if (time / fileCount > 20) {
            return String.valueOf(time / fileCount);
        }
        return String.format("%.2f", (float)time / fileCount);
    }
}
