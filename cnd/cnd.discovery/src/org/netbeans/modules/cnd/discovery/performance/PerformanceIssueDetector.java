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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.discovery.performance.AnalyzeStat.AgregatedStat;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.modelutil.Tracer;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PerformanceLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class PerformanceIssueDetector implements PerformanceLogger.PerformanceListener, CsmProgressListener {
    private final Set<Project> projects = new HashSet<>();
    private final Map<String,ReadEntry> readPerformance = new HashMap<>();
    private final Map<String,CreateEntry> createPerformance = new HashMap<>();
    private final Map<FileObject,PerformanceLogger.PerformanceEvent> createFOTimeOut = new HashMap<>();
    private final Map<File,PerformanceLogger.PerformanceEvent> createFileTimeOut = new HashMap<>();
    private final Map<String,PerformanceLogger.PerformanceEvent> createItemTimeOut = new HashMap<>();
    private final Map<String,ParseEntry> parsePerformance = new HashMap<>();
    private final Map<FileObject,PerformanceLogger.PerformanceEvent> parseTimeOut = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ScheduledFuture<?> periodicTask;
    private static final int SCHEDULE = 15; // period in seconds
    static final long NANO_TO_SEC = 1000*1000*1000;
    static final long NANO_TO_MILLI = 1000*1000;
    static final int CREATION_SPEED_LIMIT = 100;
    static final int CREATION_SPEED_LIMIT_NORMAL = CREATION_SPEED_LIMIT*5;
    static final int READING_SPEED_LIMIT = 100;
    static final int READING_SPEED_LIMIT_NORMAL = READING_SPEED_LIMIT*10;
    static final int PARSING_SPEED_LIMIT = 1000;
    static final int PARSING_SPEED_LIMIT_NORMAL = PARSING_SPEED_LIMIT*10;
    static final int PARSING_RATIO_LIMIT = 5;
    static final int PARSING_RATIO_LIMIT_NORMAL = 2;
    private boolean slowItemCreation = false;
    private boolean slowFileRead = false;
    private boolean slowParsed = false;
    // 0 - not performed
    // 1 - scheduled
    // 2 - performed
    private final AtomicInteger  fullAnalyze = new AtomicInteger(0);
    static final Logger LOG = Logger.getLogger(PerformanceIssueDetector.class.getName());
    static final Level level = Level.FINE;
    private static final Level timeOutLevel = Level.INFO;
    private static PerformanceIssueDetector INSTANCE;

    public PerformanceIssueDetector() {
        if (PerformanceLogger.isProfilingEnabled()) {
            periodicTask = new RequestProcessor("PerformanceIssueDetector").scheduleAtFixedRate(new Runnable() { //NOI18N
                @Override
                public void run() {
                    analyze();
                }
            }, SCHEDULE, SCHEDULE, TimeUnit.SECONDS);
        } else {
            periodicTask = null;
        }
        INSTANCE = this;
    }
    
    public static PerformanceIssueDetector getActiveInstance() {
        return INSTANCE;
    }
    
    public void start(Project project) {
        synchronized(projects) {
            projects.add(project);
            if (projects.size() == 1) {
                CsmListeners.getDefault().addProgressListener(this);
            }
        }
    }

    public void stop(Project project) {
        synchronized(projects) {
            projects.remove(project);
            if (projects.isEmpty()) {
                CsmListeners.getDefault().removeProgressListener(this);
                lock.writeLock().lock();
                try {
                    createPerformance.clear();
                    createFOTimeOut.clear();
                    createFileTimeOut.clear();
                    createItemTimeOut.clear();
                    readPerformance.clear();
                    parsePerformance.clear();
                    parseTimeOut.clear();
                    slowItemCreation = false;
                    slowFileRead = false;
                    slowParsed = false;
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }
    }
    
    @Override
    public void processEvent(PerformanceLogger.PerformanceEvent event) {
        if (Folder.LS_FOLDER_PERFORMANCE_EVENT.equals(event.getId())) {
            processCreateFolder(event);
        } else if (CndFileUtils.LS_FOLDER_UTILS_PERFORMANCE_EVENT.equals(event.getId())) {
            processCreateFolderIO(event);
        } else if (Folder.CREATE_ITEM_PERFORMANCE_EVENT.equals(event.getId())) {
            processCreateItem(event);
        } else if (Folder.GET_ITEM_FILE_OBJECT_PERFORMANCE_EVENT.equals(event.getId())) {
            processGetItemFileObject(event);
        } else if (CndFileUtils.READ_FILE_PERFORMANCE_EVENT.equals(event.getId())) {
            processRead(event);
        } else if (Tracer.PARSE_FILE_PERFORMANCE_EVENT.equals(event.getId())) {
            processParse(event);
        }
    }
    
    private boolean isNotNormalized(String path) {
       if (path.endsWith("/.") || path.endsWith("\\.")) { // NOI18N
           return true;
       }
       if (path.contains("/./")) { // NOI18N
           return true;
       }
       if (path.contains("\\.\\")) { // NOI18N
           return true;
       }
       if (path.contains("..")) { // NOI18N
           return true;
       }
       return false;
    }
    
    private void processCreateFolder(PerformanceLogger.PerformanceEvent event) {
        FileObject fo = (FileObject) event.getSource();
        String dirName = fo.getPath();
        long time = event.getTime();
        if (event.getAttrs().length == 0) {
            LOG.log(timeOutLevel, "Timeout {0}s of directory list {1}", new Object[]{time/NANO_TO_SEC, dirName}); //NOI18N
            lock.writeLock().lock();
            try {
                createFOTimeOut.put(fo, event);
            } finally {
                lock.writeLock().unlock();
            }
            return;
        }
        if (isNotNormalized(dirName)) {
            // Ignore not normalized paths
            return;
        }
        long cpu = event.getCpuTime();
        long user = event.getUserTime();
        lock.writeLock().lock();
        try {
            createFOTimeOut.remove(fo);
            CreateEntry entry = createPerformance.get(dirName);
            if (entry == null) {
                entry = new CreateEntry();
                createPerformance.put(dirName, entry);
            }
            entry.number++;
            entry.time += time;
            entry.cpu += cpu;
            entry.user += user;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void processCreateFolderIO(PerformanceLogger.PerformanceEvent event) {
        File fo = (File) event.getSource();
        String dirName = fo.getPath();
        long time = event.getTime();
        if (event.getAttrs().length == 0) {
            LOG.log(timeOutLevel, "Timeout {0}s of directory list {1}", new Object[]{time/NANO_TO_SEC, dirName}); //NOI18N
            lock.writeLock().lock();
            try {
                createFileTimeOut.put(fo, event);
            } finally {
                lock.writeLock().unlock();
            }
            return;
        }
        if (isNotNormalized(dirName)) {
            // Ignore not normalized paths
            return;
        }
        long cpu = event.getCpuTime();
        long user = event.getUserTime();
        lock.writeLock().lock();
        try {
            createFileTimeOut.remove(fo);
            CreateEntry entry = createPerformance.get(dirName);
            if (entry == null) {
                entry = new CreateEntry();
                createPerformance.put(dirName, entry);
            }
            entry.number++;
            entry.time += time;
            entry.cpu += cpu;
            entry.user += user;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void processCreateItem(PerformanceLogger.PerformanceEvent event) {
        String path;
        if (event.getSource() instanceof FileObject) {
            FileObject fo = (FileObject) event.getSource();
            path = fo.getPath();
        } else {
            path = (String)event.getSource();
        }
        long time = event.getTime();
        if (event.getAttrs().length == 0) {
            LOG.log(timeOutLevel, "Timeout {0}s of create project item {1}", new Object[]{time/NANO_TO_SEC, path}); //NOI18N
            lock.writeLock().lock();
            try {
                createItemTimeOut.put(path, event);
            } finally {
                lock.writeLock().unlock();
            }
            return;
        }
        long cpu = event.getCpuTime();
        long user = event.getUserTime();
        lock.writeLock().lock();
        try {
            createItemTimeOut.remove(path);
            String dirName;
            if (event.getSource() instanceof FileObject) {
                dirName = CndPathUtilities.getDirName(path);
            } else {
                Item item = (Item) event.getAttrs()[0];
                dirName = CndPathUtilities.getDirName(item.getAbsolutePath());
            }
            if (isNotNormalized(dirName)) {
                // Ignore not normalized paths
                return;
            }
            CreateEntry entry = createPerformance.get(dirName);
            if (entry == null) {
                entry = new CreateEntry();
                createPerformance.put(dirName, entry);
            }
            entry.number++;
            entry.time += time;
            entry.cpu += cpu;
            entry.user += user;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void processGetItemFileObject(PerformanceLogger.PerformanceEvent event) {
        Item item = (Item) event.getSource();
        long time = event.getTime();
        String path = item.getAbsPath();
        if (event.getAttrs().length == 0) {
            //TODO: process timeout
            
            LOG.log(timeOutLevel, "Timeout {0}s of find file object {1}", new Object[]{time/NANO_TO_SEC, path}); //NOI18N
            return;
        }
        String dirName = CndPathUtilities.getDirName(path);
        if (isNotNormalized(dirName)) {
            // Ignore not normalized paths
            return;
        }
        long cpu = event.getCpuTime();
        long user = event.getUserTime();
        lock.writeLock().lock();
        try {
            CreateEntry entry = createPerformance.get(dirName);
            if (entry == null) {
                entry = new CreateEntry();
                createPerformance.put(dirName, entry);
            }
            entry.number++;
            entry.time += time;
            entry.cpu += cpu;
            entry.user += user;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void processRead(PerformanceLogger.PerformanceEvent event) {
        FileObject fo = (FileObject) event.getSource();
        long time = event.getTime();
        if (event.getAttrs().length == 0) {
            //TODO: process timeout
            return;
        }
        String dirName = CndPathUtilities.getDirName(fo.getPath());
        if (isNotNormalized(dirName)) {
            // Ignore not normalized paths
            return;
        }
        int readChars = ((Integer) event.getAttrs()[0]).intValue();
        int readLines = ((Integer) event.getAttrs()[1]).intValue();
        long cpu = event.getCpuTime();
        long user = event.getUserTime();
        lock.writeLock().lock();
        try {
            ReadEntry entry = readPerformance.get(dirName);
            if (entry == null) {
                entry = new ReadEntry();
                readPerformance.put(dirName, entry);
            }
            entry.number++;
            entry.read += readChars;
            entry.lines += readLines;
            entry.time += time;
            entry.cpu += cpu;
            entry.user += user;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private void processParse(PerformanceLogger.PerformanceEvent event) {
        FileObject fo = (FileObject) event.getSource();
        long time = event.getTime();
        long cpu = event.getCpuTime();
        if (event.getAttrs().length == 0) {
            //TODO: process timeout
            if (time > cpu && cpu > 0) {
                if (time/cpu < 5) {
                    LOG.log(timeOutLevel, "Timeout {0}s of parsing file {1}. The parsing already consumes {2}s CPU time", new Object[]{time/NANO_TO_SEC, fo.getPath(), cpu/NANO_TO_SEC}); //NOI18N
                } else {
                    LOG.log(timeOutLevel, "Timeout {0}s of parsing file {1}. The parsing spends {2}s on waiting resources.", new Object[]{time/NANO_TO_SEC, fo.getPath(), (time-cpu)/NANO_TO_SEC}); //NOI18N
                }
            } else {
                LOG.log(timeOutLevel, "Timeout {0}s of parsing file {1}", new Object[]{time/NANO_TO_SEC, fo.getPath()}); //NOI18N
            }
            lock.writeLock().lock();
            try {
                parseTimeOut.put(fo, event);
            } finally {
                lock.writeLock().unlock();
            }
            return;
        }
        String dirName = CndPathUtilities.getDirName(fo.getPath());
        if (isNotNormalized(dirName)) {
            // Ignore not normalized paths
            return;
        }
        int readLines = ((Integer) event.getAttrs()[0]).intValue();
        long user = event.getUserTime();
        lock.writeLock().lock();
        try {
            parseTimeOut.remove(fo);
            ParseEntry entry = parsePerformance.get(dirName);
            if (entry == null) {
                entry = new ParseEntry();
                parsePerformance.put(dirName, entry);
            }
            entry.number++;
            entry.lines += readLines;
            entry.time += time;
            entry.cpu += cpu;
            entry.user += user;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void processParseFinished(Lookup.Provider makeProject) {
        lock.writeLock().lock();
        try {
            if (fullAnalyze.get() == 0) {
                fullAnalyze.set(1);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private boolean canNotify() {
        List<Project> list = new ArrayList<>();
        synchronized(projects) {
            list.addAll(projects);
        }
        if (list.size() > 1) {
            return false;
        }
        for (Project project : list) {
            RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
            if (remoteProject != null) {
                ExecutionEnvironment developmentHost = remoteProject.getDevelopmentHost();
                if (developmentHost != null) {
                    if (developmentHost.isRemote()) {
                        return false;
                    }
                }
                ExecutionEnvironment sourceFileSystemHost = remoteProject.getSourceFileSystemHost();
                if (sourceFileSystemHost.isRemote()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean alreadyNotified() {
        return slowFileRead || slowItemCreation || slowParsed;
    }
    
    private void notifyProblem(final int problem, final String details) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NotifyProjectProblem.showNotification(PerformanceIssueDetector.this, problem, details);
            }
        });
    }
    
    private void analyze() {
        boolean doFullAnalyze = false;
        TreeMap<String, AgregatedStat> gatherStat = null;
        lock.writeLock().lock();
        try {
            if (fullAnalyze.get() == 1) {
                doFullAnalyze = true;
                fullAnalyze.set(2);
            }
            
        } finally {
            lock.writeLock().unlock();
        }
        
        lock.readLock().lock();
        try {
            analyzeCreateItems();
            analyzeInfiniteCreateFile();
            analyzeReadFile();
            analyzeParseFile();
            analyzeInfiniteParseFile();
            if (doFullAnalyze) {
                gatherStat = gatherStat();
            }
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
        } finally {
            lock.readLock().unlock();
        }
        if (gatherStat != null) {
            AnalyzeStat.upEmptyFolder(gatherStat);
            for (Map.Entry<String, AgregatedStat> entry : AnalyzeStat.getBigUnused(gatherStat)) {
                PerformanceIssueDetector.LOG.log(Level.INFO, "Unused folder {0} contains {1} items and consumes {2}s.", // NOI18N
                        new Object[]{entry.getKey(),
                                     PerformanceIssueDetector.format(entry.getValue().itemNumber),
                                     PerformanceIssueDetector.format(entry.getValue().itemTime/PerformanceIssueDetector.NANO_TO_SEC)});
            }
            AnalyzeStat.groupByReadingSpeed(gatherStat);
            AnalyzeStat.dumpAll(gatherStat);
            int i = 0;
            for (Map.Entry<String, AgregatedStat> entry : AnalyzeStat.getSlowReading(gatherStat)) {
                 PerformanceIssueDetector.LOG.log(Level.INFO, "Slow reading files in the folder {0}. Reading {1} lines consumes {2}s.", // NOI18N
                         new Object[]{entry.getKey(),
                                      PerformanceIssueDetector.format(entry.getValue().readLines),
                                      PerformanceIssueDetector.format(entry.getValue().readTime/PerformanceIssueDetector.NANO_TO_SEC)});
                 i++;
                 if (i > 5) {
                     break;
                 }
            }
        }
    }

    TreeMap<String, AgregatedStat> getStatistic() {
        lock.readLock().lock();
        try {
            return gatherStat();
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    Map<FileObject,PerformanceLogger.PerformanceEvent> getParseTimeout() {
        lock.readLock().lock();
        try {
             return new HashMap<>(parseTimeOut);
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private TreeMap<String, AgregatedStat> gatherStat() {
        TreeMap<String, AgregatedStat> map = new TreeMap<>();
        for(Map.Entry<String,CreateEntry> entry : createPerformance.entrySet()) {
            AgregatedStat state = map.get(entry.getKey());
            if (state == null) {
                state = new AgregatedStat();
                map.put(entry.getKey(), state);
            }
            state.itemNumber = entry.getValue().number;
            state.itemTime = entry.getValue().time;
            state.itemCPU = entry.getValue().cpu;
            state.itemUser = entry.getValue().user;
        }
        for(Map.Entry<String,ReadEntry> entry : readPerformance.entrySet()) {
            AgregatedStat state = map.get(entry.getKey());
            if (state == null) {
                state = new AgregatedStat();
                map.put(entry.getKey(), state);
            }
            state.readNumber = entry.getValue().number;
            state.readBytes = entry.getValue().read;
            state.readLines = entry.getValue().lines;
            state.readTime = entry.getValue().time;
            state.readCPU = entry.getValue().cpu;
            state.readUser = entry.getValue().user;
        }
        for(Map.Entry<String,ParseEntry> entry : parsePerformance.entrySet()) {
            AgregatedStat state = map.get(entry.getKey());
            if (state == null) {
                state = new AgregatedStat();
                map.put(entry.getKey(), state);
            }
            state.parseNumber = entry.getValue().number;
            state.parseLines = entry.getValue().lines;
            state.parseTime = entry.getValue().time;
            state.parseCPU = entry.getValue().cpu;
            state.parseUser = entry.getValue().user;
        }
        return map;
    }
    
    @Messages({
         "# {0} - time"
        ,"# {1} - items"
        ,"# {2} - speed"
        ,"# {3} - expected"
        ,"Details.slow.item.creation=Details:<br>\n"
                                   +"The IDE spent {0} seconds to create {1} project items.<br>\n"
                                   +"The average creation speed is {2} items per second.<br>\n"
                                   +"IDE expects the average creation speed is more than {3} items per second.<br>\n"
    })
    private void analyzeCreateItems() {
        long itemCount = 0;
        long time = 0;
        long cpu = 0;
        long user = 0;
        for(Map.Entry<String,CreateEntry> entry : createPerformance.entrySet()) {
            itemCount += entry.getValue().number;
            time +=  entry.getValue().time;
            cpu +=  entry.getValue().cpu;
            user +=  entry.getValue().user;
        }
        if (time <= 0) {
            return;
        }
        long wallTime = time/NANO_TO_SEC;
        long creationSpeed = (itemCount*NANO_TO_SEC)/time;
        if (wallTime > 15 && itemCount > 100 && creationSpeed < CREATION_SPEED_LIMIT) {
            if (!alreadyNotified()) {
                slowItemCreation = true;
                String details = Bundle.Details_slow_item_creation(format(wallTime), format(itemCount), format(creationSpeed), format(CREATION_SPEED_LIMIT));
                if (!CndUtils.isUnitTestMode() && !CndUtils.isStandalone() && canNotify()) {
                    notifyProblem(NotifyProjectProblem.CREATE_PROBLEM, details);
                }
                details = details.replace("<br>", "").replace("\n", " "); //NOI18N
                LOG.log(Level.INFO, "Slow File System Detected. {0}", details); //NOI18N
            }
        }
        LOG.log(level, "Average item creatoin speed is {0} item/s Created {1} items Time {2} ms CPU {3} ms User {4} ms", //NOI18N
                new Object[]{format(creationSpeed), format(itemCount), format(time/NANO_TO_MILLI), format(cpu/NANO_TO_MILLI), format(user/NANO_TO_MILLI)});
    }
    
    @Messages({
         "# {0} - table"
        ,"Details.infinite.items.creation=Details. The access is nether finished or consumes too much time of files:<br>\n"
                                     +"<table><tbody>\n"
                                     +"<tr><th>File</th><th>Time, s</th></tr>\n"
                                     +"{0}\n"
                                     +"</tbody></table>\n"
        ,"# {0} - file"
        ,"# {1} - time"
        ,"Details.infinite.item.creation=<tr><td>{0}</td><td>{1}</td></tr>\n"
    })
    private void analyzeInfiniteCreateFile() {
        int INFINITE_CREATE_ITEM_TIMOUT = 30;
        StringBuilder buf = new StringBuilder();
        Iterator<Map.Entry<FileObject, PerformanceLogger.PerformanceEvent>> iterator = createFOTimeOut.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<FileObject, PerformanceLogger.PerformanceEvent> entry = iterator.next();
            FileObject fo = entry.getKey();
            PerformanceLogger.PerformanceEvent event = entry.getValue();
            long delta = (System.nanoTime() - event.getStartTime())/NANO_TO_SEC;
            if (delta > INFINITE_CREATE_ITEM_TIMOUT) {
                iterator.remove();
                buf.append(Bundle.Details_infinite_item_creation(fo.getPath(), format(delta)));
                LOG.log(Level.INFO, "Too Long File Access Detected. Access to file {0} consumes more than {1}s.", new Object[]{fo.getPath(), format(delta)}); //NOI18N
            }
        }
        Iterator<Map.Entry<File, PerformanceLogger.PerformanceEvent>> iterator2 = createFileTimeOut.entrySet().iterator();
        while(iterator2.hasNext()) {
            Map.Entry<File, PerformanceLogger.PerformanceEvent> entry = iterator2.next();
            File fo = entry.getKey();
            PerformanceLogger.PerformanceEvent event = entry.getValue();
            long delta = (System.nanoTime() - event.getStartTime())/NANO_TO_SEC;
            if (delta > INFINITE_CREATE_ITEM_TIMOUT) {
                iterator2.remove();
                buf.append(Bundle.Details_infinite_item_creation(fo.getPath(), format(delta)));
                LOG.log(Level.INFO, "Too Long File Access Detected. Access to file {0} consumes more than {1}s.", new Object[]{fo.getPath(), format(delta)}); //NOI18N
            }
        }
        Iterator<Map.Entry<String, PerformanceLogger.PerformanceEvent>> iterator3 = createItemTimeOut.entrySet().iterator();
        while(iterator3.hasNext()) {
            Map.Entry<String, PerformanceLogger.PerformanceEvent> entry = iterator3.next();
            String fo = entry.getKey();
            PerformanceLogger.PerformanceEvent event = entry.getValue();
            long delta = (System.nanoTime() - event.getStartTime())/NANO_TO_SEC;
            if (delta > INFINITE_CREATE_ITEM_TIMOUT) {
                iterator3.remove();
                buf.append(Bundle.Details_infinite_item_creation(fo, format(delta)));
                LOG.log(Level.INFO, "Too Long File Access Detected. Access to file {0} consumes more than {1}s.", new Object[]{fo, format(delta)}); //NOI18N
            }
        }
        if (buf.length() > 0) {
            if (!alreadyNotified()) {
                slowItemCreation = true;
                if (!CndUtils.isUnitTestMode() && !CndUtils.isStandalone() && canNotify()) {
                    notifyProblem(NotifyProjectProblem.INFINITE_CREATE_PROBLEM, Bundle.Details_infinite_items_creation(buf.toString()));
                }
            }
        }
    }
    
    @Messages({
         "# {0} - time"
        ,"# {1} - read"
        ,"# {2} - speed"
        ,"# {3} - expected"
        ,"Details.slow.file.read=Details:<br>\n"
                               +"The IDE spent {0} seconds to read {1} Kb of project files.<br>\n"
                               +"The average reading speed is {2} Kb per second.<br>\n"
                               +"IDE expects the average reading speed is more than {3} Kb per second.<br>\n"
    })
    private void analyzeReadFile() {
        long fileCount = 0;
        long read = 0;
        long lines = 0;
        long time = 0;
        long cpu = 0;
        long user = 0;
        for(Map.Entry<String,ReadEntry> entry : readPerformance.entrySet()) {
            fileCount += entry.getValue().number;
            read += entry.getValue().read;
            lines += entry.getValue().lines;
            time +=  entry.getValue().time;
            cpu +=  entry.getValue().cpu;
            user +=  entry.getValue().user;
        }
        if (time <= 0) {
            return;
        }
        long wallTime = time/NANO_TO_SEC;
        long readSpeed = (read*1000*1000)/time;
        if (wallTime > 100 && fileCount > 100 && readSpeed < READING_SPEED_LIMIT) {
            if (!alreadyNotified()) {
                slowFileRead = true;
                String details = Bundle.Details_slow_file_read(format(wallTime), format(read/1000), format(readSpeed), format(READING_SPEED_LIMIT));
                if (!CndUtils.isUnitTestMode() && !CndUtils.isStandalone() && canNotify()) {
                    notifyProblem(NotifyProjectProblem.READ_PROBLEM, details);
                }
                details = details.replace("<br>", "").replace("\n", " "); //NOI18N
                LOG.log(Level.INFO, "Slow File System Detected. {0}", details); //NOI18N
            }
        }
        LOG.log(level, "Average file reading speed is {0} Kb/s Read {1} Kb Time {2} ms CPU {3} ms User {4} ms", //NOI18N
                new Object[]{format(readSpeed), format(read/1000), format(time/NANO_TO_MILLI), format(cpu/NANO_TO_MILLI), format(user/NANO_TO_MILLI)});
    }
    
    @Messages({
         "# {0} - time"
        ,"# {1} - lines"
        ,"# {2} - speed"
        ,"# {3} - cpu"
        ,"# {4} - ratio"
        ,"# {5} - expected"
        ,"Details.slow.file.parse=Details:<br>\n"
                                +"The IDE spent {0} seconds to parse {1} lines of project files.<br>\n"
                                +"The average parsing speed is {2} lines per second.<br>\n"
                                +"In other hand IDE consumed {3} seconds of CPU time to parse these files.<br>\n"
                                +"The ratio of wall time to CPU time is 1/{4}.<br>\n"
                                +"It shows that IDE spent too much time waiting for resources.<br>\n"
                                +"IDE expects the ratio is more than 1/{5}.<br>\n"
    })
    private void analyzeParseFile() {
        long fileCount = 0;
        long lines = 0;
        long time = 0;
        long cpu = 0;
        long user = 0;
        for(Map.Entry<String,ParseEntry> entry : parsePerformance.entrySet()) {
            fileCount += entry.getValue().number;
            lines += entry.getValue().lines;
            time +=  entry.getValue().time;
            cpu +=  entry.getValue().cpu;
            user +=  entry.getValue().user;
        }
        if (time <= 0) {
            return;
        }
        long wallTime = time/NANO_TO_SEC;
        long cpuTime = cpu/NANO_TO_SEC;
        if (wallTime <= 0 ) {
            return;
        }
        long parseSpeed = lines/wallTime;
        if (cpuTime > 1) {
            long k = time/cpu;
            if (wallTime > 100 && fileCount > 100 && parseSpeed < PARSING_SPEED_LIMIT && k > PARSING_RATIO_LIMIT) {
                if (!alreadyNotified()) {
                    slowParsed = true;
                    String details = Bundle.Details_slow_file_parse(format(wallTime), format(lines), format(parseSpeed), format(cpuTime), format(k), format(PARSING_RATIO_LIMIT));
                    if (!CndUtils.isUnitTestMode() && !CndUtils.isStandalone() && canNotify()) {
                        notifyProblem(NotifyProjectProblem.PARSE_PROBLEM, details);
                    }
                    details = details.replace("<br>", "").replace("\n", " "); //NOI18N
                LOG.log(Level.INFO, "Slow File System Detected. {0}", details); //NOI18N
                }
            }
        }
        LOG.log(level, "Average parsing speed is {0} Lines/s Lines {1} Time {2} ms CPU {3} ms User {4} ms", //NOI18N
                new Object[]{format(parseSpeed), format(lines), format(time/NANO_TO_MILLI), format(cpu/NANO_TO_MILLI), format(user/NANO_TO_MILLI)});
    }

    @Messages({
         "# {0} - table"
        ,"Details.infinite.files.parse=Details. The parsing is nether finished or consumes too much time of files:<br>\n"
                                     +"<table><tbody>\n"
                                     +"<tr><th>File</th><th>Time, s</th></tr>\n"
                                     +"{0}\n"
                                     +"</tbody></table>\n"
        ,"# {0} - file"
        ,"# {1} - time"
        ,"Details.infinite.file.parse=<tr><td>{0}</td><td>{1}</td></tr>\n"
    })
    private void analyzeInfiniteParseFile() {
        StringBuilder buf = new StringBuilder();
        Iterator<Map.Entry<FileObject, PerformanceLogger.PerformanceEvent>> iterator = parseTimeOut.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<FileObject, PerformanceLogger.PerformanceEvent> entry = iterator.next();
            FileObject fo = entry.getKey();
            PerformanceLogger.PerformanceEvent event = entry.getValue();
            long delta = (System.nanoTime() - event.getStartTime())/NANO_TO_SEC;
            if (delta > 100) {
                iterator.remove();
                long time = event.getTime();
                long cpu = event.getCpuTime();
                if (event.getAttrs().length == 0) {
                    //TODO: process timeout
                    if (time > cpu && cpu > 0) {
                        if (time/cpu < 5) {
                            buf.append(Bundle.Details_infinite_file_parse(fo.getPath(), format(delta)));
                            LOG.log(Level.INFO, "Too long file {0} parsing time {1}s. Probably parser has infinite loop or file is too big", new Object[]{fo.getPath(), format(delta)}); //NOI18N
                        }
                    }
                }
            }
        }
        if (buf.length() > 0) {
            if (!CndUtils.isUnitTestMode() && !CndUtils.isStandalone() && canNotify()) {
                String details = Bundle.Details_infinite_files_parse(buf.toString());
                notifyProblem(NotifyProjectProblem.INFINITE_PARSE_PROBLEM, details);
            }
        }
    }

    static String format(long val) {
        String res = Long.toString(val);
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < res.length(); i++) {
            char c = res.charAt(res.length()-i-1);
            if (i%3==0 && i > 0) {
                buf.insert(0, ','); //NOI18N
            }
            buf.insert(0, c);
        }
        return buf.toString();
    }

    @Override
    public void projectParsingStarted(CsmProject project) {
    }

    @Override
    public void projectFilesCounted(CsmProject project, int filesCount) {
    }

    @Override
    public void projectParsingFinished(CsmProject project) {
        Object platformProject = project.getPlatformProject();
        if (platformProject instanceof NativeProject) {
            Lookup.Provider makeProject = ((NativeProject)platformProject).getProject();
            processParseFinished(makeProject);
        }
    }

    @Override
    public void projectParsingCancelled(CsmProject project) {
    }

    @Override
    public void projectLoaded(CsmProject project) {
    }

    @Override
    public void fileInvalidated(CsmFile file) {
    }

    @Override
    public void fileAddedToParse(CsmFile file) {
    }

    @Override
    public void fileParsingStarted(CsmFile file) {
    }

    @Override
    public void fileParsingFinished(CsmFile file) {
    }

    @Override
    public void parserIdle() {
    }

    @Override
    public void fileRemoved(CsmFile file) {
    }

    private static final class CreateEntry {
        private int number;
        private long time;
        private long cpu;
        private long user;
    }

    private static final class ReadEntry {
        private int number;
        private long read;
        private long lines;
        private long time;
        private long cpu;
        private long user;
    }

    private static final class ParseEntry {
        private int number;
        private long lines;
        private long time;
        private long cpu;
        private long user;
    }
}
