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
package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Tomas Zezula
 */
 public class LogContext {
     
    private static final Logger TEST_LOGGER = Logger.getLogger(RepositoryUpdater.class.getName() + ".tests"); //NOI18N
    
    private static final RequestProcessor RP = new RequestProcessor("Thread dump shooter", 1); // NOI18N
    
    private static final int SECOND_DUMP_DELAY = 5 * 1000; 
    
    private static int serial;
    
    /**
     * Implemented as one-time flag. Not exactly correct, but will suffice since
     * it only suppresses some error detection.
     */
    private static volatile boolean closing;
    
    public static void notifyClosing() {
        closing = true;
    }
    
    public enum EventType {
        PATH(1, 10),
        FILE(2, 20),
        // INDEXER has a special handling
        INDEXER(2, 5),
        MANAGER(1, 10),
        UI(1, 4);
        
        EventType(int minutes, int treshold) {
            String prefix = EventType.class.getName() + "." + name();
            Integer m = Integer.getInteger(prefix + ".minutes", minutes);
            Integer t = Integer.getInteger(prefix + ".treshold", treshold);
            
            this.minutes = m;
            this.treshold = t;
        }
        
        /**
         * Number of events per minute allowed
         */
        private int treshold;
        /**
         * Time in minutes
         */
        private int minutes;

        public int getTreshold() {
            return treshold;
        }

        public int getMinutes() {
            return minutes;
        }
    }

    public static LogContext create(
        @NonNull EventType eventType,
        @NullAllowed final String message) {
        return create(eventType, message, null);
    }

    public static LogContext create(
        @NonNull EventType eventType,
        @NullAllowed final String message,
        @NullAllowed final LogContext parent) {
        return new LogContext(
            eventType,
            Thread.currentThread().getStackTrace(),
            message,
            parent);
    }

    /**
     * Creates a new {@link LogContext} with type and stack trace taken from the prototype,
     * the prototype is absorbed by the newly created {@link LogContext}.
     * @param prototype to absorb
     * @return newly created {@link LogContext}
     */
    @NonNull
    public static LogContext createAndAbsorb(@NonNull final LogContext prototype) {
        final LogContext ctx = new LogContext(
                prototype.eventType,
                prototype.stackTrace,
                String.format(
                    "Replacement of LogContext: [type: %s, message: %s]",   //NOI18N
                    prototype.eventType,
                    prototype.message),
                null);
        ctx.absorb(prototype);
        return ctx;
    }

    @Override
    public String toString() {
        final StringBuilder msg = new StringBuilder();
        createLogMessage(msg, new HashSet<LogContext>(), 0);
        return msg.toString();
    }
    
    private String createThreadDump() {
        StringBuilder sb = new StringBuilder();
        Map<Thread, StackTraceElement[]> allTraces = Thread.getAllStackTraces();
        for (Thread t : allTraces.keySet()) {
            sb.append(String.format("Thread id %d, \"%s\" (%s):\n", t.getId(), t.getName(), t.getState()));
            StackTraceElement[] elems = allTraces.get(t);
            for (StackTraceElement l : elems) {
                sb.append("\t").append(l).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    private synchronized void checkConsistency() {
        long total = 0;
        for (RootInfo ri : scannedSourceRoots.values()) {
            total += ri.spent;
        }
        if (total != totalScanningTime) {
            System.err.println("total scanning time mismatch");
        }
    }
    
    private synchronized void freeze() {
        // finish all roots
        this.timeCutOff = System.currentTimeMillis();
        this.frozenCurrentRoots = new HashMap<Thread, RootInfo>(allCurrentRoots);
        for (RootInfo ri : frozenCurrentRoots.values()) {
            ri.finishCurrentIndexer(timeCutOff);
            // merge root statistics to the overall stats
            finishScannedRoot(ri.url);
        }
        this.frozen = true;
        checkConsistency();
    }
    
    void log() {
        // prevent logging of events within 3 minutes from the start of scan. Do not freeze...
        if (canLogScanCancel()) {
            log(true, true);
        } else {
            if (System.getProperty(RepositoryUpdater.PROP_SAMPLING) == null) {
                // enable profiling just up to the 1st exception report
                System.setProperty(RepositoryUpdater.PROP_SAMPLING, "oneshot"); // NOI18N
            }
            final LogRecord r = new LogRecord(Level.INFO,  LOG_MESSAGE_EARLY);
            r.setResourceBundle(NbBundle.getBundle(LogContext.class));
            r.setResourceBundleName(LogContext.class.getPackage().getName() + ".Bundle"); //NOI18N
            r.setLoggerName(LOG.getName());
            LOG.log(r);

            // schedule the log for later, after enough time elapses.
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    synchronized (LogContext.this) {
                        if (finished == 0) {
                            log(true, true);
                        }
                    }
                }
            }, (int)EXEC_TRESHOLD);
        }
    }
    
    /**
     * The culprit indexer, or null if no single indexer seems responsible
     */
    private String culpritIndexer;
    
    /**
     * Uses global per-indexer statistics to find the indexer, which consumes the most time. If the indexer consumes more
     * than 30% time than the next indexer in a row, it is marked as 'culprit'
     */
    private void findCulpritIndexer() {
        long secondToMax = 0;
        long max = 0;
        String candidate = null;
        
        for (Map.Entry<String, Long> check : totalIndexerTime.entrySet()) {
            if (check.getValue() > max) {
                secondToMax = max;
                max = check.getValue();
                candidate = check.getKey();
            }
        }
        
        if (candidate == null) {
            return;
        }
        // the indexer consumes at least 30% of the total scanning time,
        // and it takes 30% more than the second-to-worst indexer, report it.
        if (max >= totalScanningTime / 3 &&
            max > secondToMax * 1.3) {
            culpritIndexer = candidate;
        }
    }
    
   /**
     * if non-null, the repo thread is potentially blocked by some other thread
     */
    private String blockingClass;
    
    private Map<String, String[]> threadTraces;
    
    private Map<String, String> threadStatuses;
    
    private void buildThreadTraces(String dumpString) {
        String[] threads = dumpString.split("Thread id ");
        threadTraces = new HashMap<String, String[]>();
        threadStatuses = new HashMap<String, String>();
        
        for (String t : threads) {
            String[] lines = t.split("\n");
            if (lines.length == 0 || !lines[0].contains("\"")) {
                continue;
            }
            int quote = lines[0].indexOf('"');
            if (quote == -1) {
                continue;
            }
            int nextQuote = lines[0].indexOf('"', quote + 1);
            if (nextQuote == -1) {
                continue;
            }
            String tName = lines[0].substring(quote + 1, nextQuote);
            
            threadTraces.put(tName, lines);
            int paren = lines[0].lastIndexOf('(');
            if (paren != -1) {
                int rparen = lines[0].lastIndexOf(')');
                String state;
                
                if (rparen == -1) {
                    state = lines[0].substring(paren + 1);
                } else {
                    state = lines[0].substring(paren + 1, rparen);
                }
                threadStatuses.put(tName, state);
            }
        }
    }
    
    /**
     * Checks whether a stacktrace contains "BLOCKED" status for the RepositoryUpdater thread.
     * Result "1" means that the thread has been waiting for some time. "2" means that the thread was blocked
     * by another thread.
     */
    private int analyzeStacktraces() {
        if (threadDump == null) {
            return 0;
        }
        buildThreadTraces(threadDump);
        
        Map<String, String[]> blockedThreads = new HashMap<String, String[]>();
        for (Map.Entry<String, String> stEntry : threadStatuses.entrySet()) {
            String st = stEntry.getValue();
            if (st.contains("BLOCKED") || st.contains("PARKED") || st.contains("WAITING")) {
                blockedThreads.put(stEntry.getKey(), threadTraces.get(stEntry.getKey()));
            }
        }
        if (!blockedThreads.containsKey("RepositoryUpdater.worker")) { // NOI18N
            return 0;
        }
        if (updaterThreadAlive) {
            return 1;
        }
        String[] repositoryStack = blockedThreads.get("RepositoryUpdater.worker"); // NOI18N
        int line;
        for (line = 1; line < repositoryStack.length; line++) {
            if (!CONCURRENT_PATTERN.matcher(repositoryStack[line]).find()) {
                break;
            }
        }
        if (line >= repositoryStack.length) {
            return 2;
        }
        String qual = repositoryStack[line];
        int paren = qual.indexOf('(');
        if (paren == -1) {
            // very unlikely, probably line information is missing ?
            paren = qual.length();
        }
        qual = qual.substring(0, paren);
        int dot = qual.lastIndexOf('.'); // NOI18N
        if (dot == -1) {
            return 2;
        }
        qual = qual.substring(0, dot);
        // ignore inner classes
        dot = qual.lastIndexOf('$');
        if (dot > -1) {
            qual = qual.substring(0, dot);
        }
        
        for (Map.Entry<String, String[]> t : blockedThreads.entrySet()) {
            if (t.getValue() == repositoryStack) {
                continue;
            }
            String[] lines = t.getValue();
            for (int i = 1 ; i < lines.length; i++) {
                if (lines[i].startsWith(qual)) {
                    blockingClass = qual.trim();
                    return 2;
                }
            }

        }
        blockingClass = qual.trim();
        return 2;
    }
    
    private static final Pattern CONCURRENT_PATTERN;
    
    private static final Pattern PARSING_PATTERN;
    
    private static final String STACK_CONCURRENT_PREFIXES = 
        "\\s+sun.*|" +
        "\\s+java.util.concurrent.*|" +
        "\\s+java.lang.*|" +
        "\\s+org.openide.*Mutex.*";
    
    private static final String STACK_PARSING_PREFIXES = 
        "\\s+org.netbeans.modules.parsing.impl";
    
    static {
        CONCURRENT_PATTERN = Pattern.compile(STACK_CONCURRENT_PREFIXES);
        PARSING_PATTERN = Pattern.compile(STACK_PARSING_PREFIXES);
    }
    

    private volatile boolean updaterThreadAlive = false;
    
    private ScanCancelledException createException() {
        
        if (analyzeStacktraces() == 2) {
            String msg = "RepositoryUpdater is blocked by " + ((blockingClass != null) ? blockingClass : " <unknown>"); // NOI18N
            return new ScanCancelledException(msg, blockingClass, stackTrace);
        } 
        findCulpritIndexer();
        if (culpritIndexer != null) {
            return new ScanCancelledException("Slow scanning in " + culpritIndexer, 
                    culpritIndexer, stackTrace); // NOI18N
        }

        // try to measure crawling speed
        if (crawlerTime >= (totalScanningTime * 0.6)) {
            return new ScanCancelledException("Slow crawling", "crawler", stackTrace); // NOI18N
        }
        
        long delay = (getExecutedTime() - getScheduledTime()) / 1000;
        // delayed by 5 minutes
        if (delay > 5 * 60  && predecessor != null) {
            Exception nested = predecessor.createException();
            if (nested != null) {
                return new ScanCancelledException("Execution delayed by: " + nested.getMessage(), null, stackTrace); // NOI18N
            }
        }
       return null;
    }
    
    /**
     * org.netbeans.modules.parsing.impl.indexing.LogContext.cancelTreshold specifies
     * the mandatory delay for scanning reports in seconds.
     */
    private static final long EXEC_TRESHOLD = Integer.getInteger(LogContext.class.getName() + ".cancelTreshold", 
            3 /* mins */ * 60) * 1000 /* millis */;
    
    /**
     * Checks if enough time has elapsed so the scan cancel can be logged.
     */
    boolean canLogScanCancel() {
        return !((executed > 0) && (System.currentTimeMillis() - executed) < EXEC_TRESHOLD);
    }
    
    void log(boolean cancel, boolean logAbsorbed) {
        freeze();
        final LogRecord r = new LogRecord(Level.INFO, 
                cancel ? LOG_MESSAGE : LOG_EXCEEDS_RATE); //NOI18N
        if (!logAbsorbed) {
            this.absorbed = null;
        }
        r.setParameters(new Object[]{this, null});
        r.setResourceBundle(NbBundle.getBundle(LogContext.class));
        r.setResourceBundleName(LogContext.class.getPackage().getName() + ".Bundle"); //NOI18N
        r.setLoggerName(LOG.getName());
        if (cancel) {
            final Runnable msg = Notify.showStatus("Please wait while the scan cancel report is being produced");
            threadDump = createThreadDump();
            updaterThreadAlive = false;
            RP.post(new Runnable() {

                @Override
                public void run() {
                    secondDump = createThreadDump();
                    ScanCancelledException e = createException();
                    if (e == null) {
                        e = new ScanCancelledException("Scanning cancelled", null, stackTrace); // NOI18N 
                    }
                    byte[] profileData = null;
                    if (profileDataSource != null) {
                        try {
                            profileData = profileDataSource.call();
                        } catch (Exception ex) {
                            // ignore
                        }
                    }
                    // Keep the parameters in sync with UIHandler !
                    r.setParameters(new Object[]{
                        LogContext.this,                // [0]
                        totalScanningTime,              // [1]
                        profileData,                    // [2]
                        e.getLocation()                 // [3]
                    });
                    r.setThrown(e);
                    if ("oneshot".equals(System.getProperty(RepositoryUpdater.PROP_SAMPLING))) { // NOI18N
                        System.getProperties().remove(RepositoryUpdater.PROP_SAMPLING); // NOI18N
                    }
                    msg.run();
                    LOG.log(r);
                }

            }, SECOND_DUMP_DELAY, Thread.MAX_PRIORITY);
        } else {
            LOG.log(r);
        }
    }

    synchronized void absorb(@NonNull final LogContext other) {
        Parameters.notNull("other", other); //NOI18N
        if (other.executed == 0) {
            // #241488: do not absorb works which did not execute at all. Try to save the predecessor to keep track of blocking works.
            if (other.predecessor != null) {
                absorb(other.predecessor);
            }
            return;
        }
        if (absorbed == null) {
            absorbed = new ArrayDeque<LogContext>();
        }
        absorbed.add(other);
    }
    
    /**
     * Records this LogContext as 'executed'. Absorbed LogContexts are
     * not counted, as they are absorbed to an existing indexing work.
     */
    synchronized void recordExecuted() {
        executed = System.currentTimeMillis();
        // Hack for unit tests, which watch the test logger and wait for RepoUpdater. Do not measure stats, so the test output is not obscured.
        if (!TEST_LOGGER.isLoggable(Level.FINEST)) {
            STATS.record(this);
        }
    }
    
    void recordFinished() {
        finished = System.currentTimeMillis();
        freeze();
    }
    
    void setPredecessor(LogContext pred) {
        this.predecessor = pred;
    }
    
    long getScheduledTime() {
        return timestamp;
    }
    
    long getExecutedTime() {
        return executed;
    }
    
    long getFinishedTime() {
        return finished;
    }

    @NonNull
    StackTraceElement[] getCaller() {
        return Arrays.copyOf(stackTrace, stackTrace.length);
    }
    
    private static ThreadLocal<RootInfo>    currentlyIndexedRoot = new ThreadLocal<RootInfo>();
    private static ThreadLocal<LogContext>    currentLogContext = new ThreadLocal<LogContext>();

    private int mySerial;
    private long storeTime;
    private final long timestamp;
    private long executed;
    private final EventType eventType;
    private final String message;
    private final StackTraceElement[] stackTrace;
    private LogContext predecessor;
    private final LogContext parent;
    //@GuardedBy("this")
    private Queue<LogContext> absorbed;
    private String threadDump;
    private String secondDump;
    
    private Map<URL, Set<String>> reindexInitiators = Collections.emptyMap();
    private List<String> indexersAdded = Collections.emptyList();
    // various path/root informaation, which was the reason for indexing.
    private Set<String>  filePathsChanged = Collections.emptySet();
    private Set<String>  classPathsChanged = Collections.emptySet();
    private Set<URL>        rootsChanged = Collections.emptySet();
    private Set<URL> filesChanged = Collections.emptySet();
    private Set<URI> fileObjsChanged = Collections.emptySet();
    
    /**
     * Source roots, which have been scanned so far in this LogContext
     */
    private Map<URL, RootInfo>   scannedSourceRoots = new LinkedHashMap<URL, RootInfo>();
    
    /**
     * Time crawling between files
     */
    private long        crawlerTime;
    
    /**
     * Time spent in scanning source roots listed in {@link #scannedSourceRoots}
     */
    private long        totalScanningTime;
    
    private long        timeCutOff;
    
    private long        finished;        
    
    /**
     * The current source root being scanned
     */
    private Map<Thread, RootInfo>    allCurrentRoots = new HashMap<Thread, RootInfo>();
    
    private Map<Thread, RootInfo>    frozenCurrentRoots = Collections.emptyMap();
    
    /**
     * The scanned root, possibly null.
     */
    private URL root;
    
    /**
     * If frozen becomes true, LogContext stops updating data.
     */
    private boolean frozen;
    
    private Map<String, Long>   totalIndexerTime = new HashMap<String, Long>();
    
    private long crawlerStart;
    
    private Callable<byte[]> profileDataSource;
    
    private class RootInfo {
        private URL     url;
        private long    startTime;
        private long    spent;
        private Map<String, Long>   rootIndexerTime = new HashMap<String, Long>();
        // indexer name and start time, to capture in statistics
        private long    indexerStartTime;
        private String  indexerName;
        private int count;
        private long    crawlerTime;
        private int     resCount = -1;
        private int     allResCount = -1;
        private LinkedList<Object> pastIndexers = null;
        private long    crawlerStart = -1;

        public RootInfo(URL url, long startTime) {
            this.url = url;
            this.startTime = startTime;
        }
        
        public String toString() {
            long time = spent == 0 ? timeCutOff - startTime : spent;
            String s = "< root = " + url.toString() + "; spent = " + time + "; crawler = " + crawlerTime + "; res = "
                    + resCount + "; allRes = " + allResCount;
            if (indexerName != null) {
                s = s + "; indexer: " + indexerName;
            }
            return s + ">";
        }
        
        public void merge(RootInfo ri) {
            if (this == ri) {
                return;
            }
            if (!url.equals(ri.url)) {
                throw new IllegalArgumentException();
            }
            this.spent += ri.spent;
            this.crawlerTime += ri.crawlerTime;
            if (ri.resCount > -1) {
                this.resCount = ri.resCount;
            }
            if (ri.allResCount > -1) {
                this.allResCount = ri.allResCount;
            }
            for (Map.Entry<String, Long> entry : ri.rootIndexerTime.entrySet()) {
                String id = entry.getKey();
                Long spent = entry.getValue();
                Long my = rootIndexerTime.get(id);
                if (my == null) {
                    my = spent;
                } else {
                    my += spent;
                }
                rootIndexerTime.put(id, my);
            }
        }
        
        void startIndexer(String indexerName) {
            if (indexerStartTime != 0) {
                if (pastIndexers == null) {
                    pastIndexers = new LinkedList<Object>();
                }
                pastIndexers.add(Long.valueOf(indexerStartTime));
                pastIndexers.add(this.indexerName);
            }
            this.indexerStartTime = System.currentTimeMillis();
            this.indexerName = indexerName;
        }
        
        long finishCurrentIndexer(long now) {
            if (indexerStartTime == 0) {
                return 0;
            }
            long time = now - indexerStartTime;
            Long t = rootIndexerTime.get(indexerName);
            if (t == null) {
                t = Long.valueOf(0);
            }
            t += time;
            rootIndexerTime.put(indexerName, t);
            if (pastIndexers != null && !pastIndexers.isEmpty()) {
                indexerName = (String)pastIndexers.removeLast();
                indexerStartTime = (Long)pastIndexers.removeLast();
            } else {
                indexerStartTime = 0;
            }
            return time;
        }
        
        long finishIndexer(String indexerName) {
            if (frozen) {
                return 0;
            }
            if (indexerStartTime == 0 || indexerName == null) {
                return 0;
            }
            if (!indexerName.equals(this.indexerName)) {
                boolean ok = false;
                if (pastIndexers != null) {
                    for (int i = 1; i < pastIndexers.size(); i += 2) {
                        if (indexerName.equals(pastIndexers.get(i))) {
                            long t = System.currentTimeMillis();
                            // rollback past indexers to the currently finishing one
                            while (pastIndexers.size() > i) {
                                finishCurrentIndexer(t);
                            }
                            ok = true;
                        }
                    }
                }
                if (!ok) {
                    LOG.log(Level.WARNING, "Mismatch in indexer: " + indexerName +
                            ", current: " + indexerName + ", past: " + pastIndexers, new Throwable());
                    // clean up
                    if (pastIndexers != null) {
                        pastIndexers.clear();
                    }
                    indexerStartTime = 0;
                    this.indexerName = null;
                    return 0;
                }
            }
            long l = finishCurrentIndexer(System.currentTimeMillis());
            if (indexerStartTime == 0) {
                this.indexerName = null;
            }
            return l;
        }
    }
    
    public synchronized void noteRootScanning(URL currentRoot, boolean crawling) {
        updaterThreadAlive = true;
        if (frozen) {
            return;
        }
        RootInfo ri = allCurrentRoots.get(Thread.currentThread());
        assert ri == null;
        allCurrentRoots.put(Thread.currentThread(), ri = new RootInfo(
                    currentRoot,
                    System.currentTimeMillis()
        ));
        if (crawling) {
            ri.crawlerStart = ri.startTime;
        }
        currentlyIndexedRoot.set(ri);
        currentLogContext.set(this);
    }
    
    public synchronized void startCrawler() {
        crawlerStart = System.currentTimeMillis();
    }
    
    public synchronized void reportCrawlerProgress(int resCount, int allResCount) {
        long t = System.currentTimeMillis();
        
        updaterThreadAlive = true;
        RootInfo ri = allCurrentRoots.get(Thread.currentThread());
        if (ri == null) {
            LOG.log(Level.WARNING, "No root specified for crawler run", new Throwable());
            return;
        }
        ri.crawlerTime = t - crawlerStart;
        if (resCount != -1) {
            ri.resCount = resCount;
        }
        if (allResCount != -1) {
            ri.allResCount = allResCount;
        }
    }
    
    public synchronized void addCrawlerTime(long time, int resCount, int allResCount) {
        updaterThreadAlive = true;
        if (frozen) {
            return;
        }
        this.crawlerTime += time;
        RootInfo ri = allCurrentRoots.get(Thread.currentThread());
        if (ri == null) {
            LOG.log(Level.WARNING, "No root specified for crawler run", new Throwable());
            return;
        }
        ri.crawlerTime += time;
        ri.crawlerStart = -1;
        if (resCount != -1) {
            ri.resCount = resCount;
        }
        if (allResCount != -1) {
            ri.allResCount = allResCount;
        }
        checkConsistency();
    }
    
    public synchronized void addStoreTime(long time) {
        updaterThreadAlive = true;
        if (frozen) {
            return;
        }
        this.storeTime += time;
    }
    
    public synchronized void finishScannedRoot(URL scannedRoot) {
        updaterThreadAlive = true;
        if (frozen) {
            return;
        }
        RootInfo ri = allCurrentRoots.get(Thread.currentThread());
        if (ri == null || !scannedRoot.equals(ri.url)) {
            return;
        }
        long time = System.currentTimeMillis();
        long diff = time - ri.startTime;
        totalScanningTime += diff;
        // support multiple entries
        ri.spent += diff;
        if (ri.crawlerStart >= 0) {
            ri.crawlerTime = time - ri.crawlerStart;
            ri.crawlerStart = -1;
        }
        crawlerTime += ri.crawlerTime;
        allCurrentRoots.remove(Thread.currentThread());
        currentlyIndexedRoot.remove();
        currentLogContext.remove();

        RootInfo ri2 = scannedSourceRoots.get(ri.url);
        if (ri2 == null) {
            ri2 = new RootInfo(ri.url, ri.startTime);
            scannedSourceRoots.put(ri.url, ri2);
        }
        ri2.merge(ri);
        checkConsistency();
    }
    
    public void setProfileSource(Callable<byte[]> source) {
        this.profileDataSource = source;
    }
    
    public synchronized void startIndexer(String fName) {
        updaterThreadAlive = true;
        if (frozen) {
            return;
        }
        RootInfo ri = allCurrentRoots.get(Thread.currentThread());
        if (ri == null) {
            LOG.log(Level.WARNING, "Unreported root for running indexer: " + fName, new Throwable());
        } else {
            ri.startIndexer(fName);
        }
    }
    
    public synchronized void finishIndexer(String fName) {
        updaterThreadAlive = true;
        if (frozen) {
            return;
        }
        RootInfo ri = allCurrentRoots.get(Thread.currentThread());
        if (ri == null) {
            LOG.log(Level.WARNING, "Unreported root for running indexer: " + fName, new Throwable());
        } else {
            long addTime = ri.finishIndexer(fName);
            Long t = totalIndexerTime.get(fName);
            if (t == null) {
                t = Long.valueOf(0);
            }
            totalIndexerTime.put(fName, t + addTime);
        }
    }
    
    public synchronized void addIndexerTime(String fName, long addTime) {
        updaterThreadAlive = true;
        if (frozen) {
            return;
        }
        Long t = totalIndexerTime.get(fName);
        if (t == null) {
            t = Long.valueOf(0);
        }
        RootInfo ri = allCurrentRoots.get(Thread.currentThread());
        if (ri == null) {
            LOG.log(Level.WARNING, "Unreported root for running indexer: " + fName, new Throwable());
        } else {
            if (ri.indexerName != null) {
                addTime = ri.finishIndexer(fName);
            }
            totalIndexerTime.put(fName, t + addTime);
        }
    }
    
    public synchronized LogContext withRoot(URL root) {
        this.root = root;
        return this;
    }
    
    public LogContext addPaths(Collection<? extends ClassPath> paths) {
        if (paths == null || paths.isEmpty()) {
            return this;
        }
        final Set<String> toAdd = new HashSet<String>();
        for (ClassPath cp : paths) {
            toAdd.add(cp.toString());
        }
        synchronized (this) {
            if (classPathsChanged.isEmpty()) {
                classPathsChanged = new HashSet<String>(paths.size());
            }
            classPathsChanged.addAll(toAdd);
        }
        return this;
    }
    
    public synchronized LogContext addFilePaths(Collection<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return this;
        }
        if (filePathsChanged.isEmpty()) {
            filePathsChanged = new HashSet<String>(paths.size());
        }
        filePathsChanged.addAll(paths);
        return this;
    }
    
    public synchronized LogContext addRoots(Iterable<? extends URL> roots) {
        if (roots == null) {
            return this;
        }
        Iterator<? extends URL> it = roots.iterator();
        if (!it.hasNext()) {
            return this;
        }
        if (rootsChanged.isEmpty()) {
            rootsChanged = new HashSet<URL>(11);
        }
        while (it.hasNext()) {
            rootsChanged.add(it.next());
        }
        return this;
    }

    public synchronized LogContext addFileObjects(Collection<FileObject> files) {
        if (files == null || files.isEmpty()) {
            return this;
        }
        if (fileObjsChanged.isEmpty()) {
            fileObjsChanged = new HashSet<URI>(files.size());
        }
        for (FileObject file : files) {
            fileObjsChanged.add(file.toURI());
        }
        return this;
    }

    public synchronized LogContext addFiles(Collection<? extends URL> files) {
        if (files == null || files.isEmpty()) {
            return this;
        }
        if (filesChanged.isEmpty()) {
            filesChanged = new HashSet<URL>(files.size());
        }
        filesChanged.addAll(files);
        return this;
    }

    private LogContext(
        @NonNull final EventType eventType,
        @NonNull final StackTraceElement[] stackTrace,
        @NullAllowed final String message,
        @NullAllowed final LogContext parent) {
        Parameters.notNull("eventType", eventType);     //NOI18N
        Parameters.notNull("stackTrace", stackTrace);   //NOI18N
        this.eventType = eventType;
        this.stackTrace = stackTrace;
        this.message = message;
        this.parent = parent;
        this.timestamp = System.currentTimeMillis();
        synchronized (LogContext.class) {
            this.mySerial = serial++;
        }
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(
                Level.FINER,
                "Event type: {0} from: {1}",    //NOI18N
                new Object[]{
                    eventType,
                    Arrays.toString(stackTrace)
                });
        }
    }
    
    private synchronized void createLogMessage(@NonNull final StringBuilder sb, Set<LogContext> reported, int depth) {
        sb.append("ID: ").append(mySerial).append(", Type:").append(eventType);   //NOI18N
        if (reported.contains(this)) {
            sb.append(" -- see above\n");
            return;
        }
        if (depth > 5) {
            sb.append("-- too deep nesting");
            return;
        }
        reported.add(this);
        if (message != null) {
            sb.append(" Description:").append(message); //NOI18N
        }
        sb.append("\nTime scheduled: ").append(new Date(timestamp));
        if (executed > 0) {
            sb.append("\nTime executed: ").append(new Date(executed));
            if (finished > 0) {
                sb.append("\nTime finished: ").append(new Date(finished));
            }
        } else {
            sb.append("\nNOT executed");
        }
        sb.append("\nScanned roots: ").append(scannedSourceRoots.values().toString().replace(",", "\n\t")).
                append("\n, total time: ").append(totalScanningTime);
        
        sb.append("\nCurrent root(s): ").append(frozenCurrentRoots.values().toString().replace(",", "\n\t"));
        sb.append("\nCurrent indexer(s): ");
        for (RootInfo ri : frozenCurrentRoots.values()) {
            sb.append("\n\t").append(ri.url);
            List<String> indexerNames = new ArrayList<String>(ri.rootIndexerTime.keySet());
            Collections.sort(indexerNames);
            for (String s : indexerNames) {
                long l = ri.rootIndexerTime.get(s);
                sb.append("\n\t\t").append(s).
                        append(": ").append(l);
            }
        }
        sb.append("\nTime spent in indexers:");
        List<String> iNames = new ArrayList<String>(totalIndexerTime.keySet());
        Collections.sort(iNames);
        for (String s : iNames) {
            long l = totalIndexerTime.get(s);
            sb.append("\n\t").append(s).
                    append(": ").append(l);
        }
        sb.append("\nTime spent in indexers, in individual roots:");
        for (Map.Entry<URL, RootInfo> rootEn : scannedSourceRoots.entrySet()) {
            sb.append("\n\t").append(rootEn.getKey());
            RootInfo ri = rootEn.getValue();
            List<String> indexerNames = new ArrayList<String>(ri.rootIndexerTime.keySet());
            Collections.sort(indexerNames);
            for (String s : indexerNames) {
                long l = ri.rootIndexerTime.get(s);
                sb.append("\n\t\t").append(s).
                        append(": ").append(l);
            }
        }
        
        sb.append("\nTime in index store: " + storeTime);
        sb.append("\nTime crawling: " + crawlerTime);
        
        if (!reindexInitiators.isEmpty()) {
            sb.append("\nReindexing demanded by indexers:\n");
            for (URL u : reindexInitiators.keySet()) {
                List<String> indexers = new ArrayList<String>(reindexInitiators.get(u));
                Collections.sort(indexers);
                sb.append("\t").append(u).append(": ").append(indexers).append("\n");
            }
        }
        if (!indexersAdded.isEmpty()) {
            sb.append("\nIndexers added: " + indexersAdded);
        }
        
        sb.append("\nStacktrace:\n");    //NOI18N
        for (StackTraceElement se : stackTrace) {
            sb.append('\t').append(se).append('\n'); //NOI18N
        }
        if (root != null) {
            sb.append("On root: ").append(root).append("\n");
        }
        if (!this.rootsChanged.isEmpty()) {
            sb.append("Changed CP roots: ").append(rootsChanged).append("\n");
        }
        if (!this.classPathsChanged.isEmpty()) {
            sb.append("Changed ClassPaths:").append(classPathsChanged).append("\n");
        }
        if (!this.filesChanged.isEmpty()) {
            sb.append("Changed files(URL): ").append(filesChanged.toString().replace(",", "\n\t")).append("\n");
        }
        
        if (!this.fileObjsChanged.isEmpty()) {            
            sb.append("Changed files(FO): ");
            for (URI uri : this.fileObjsChanged) {
                String name;
                try {
                    final File f = BaseUtilities.toFile(uri);
                    name = f.getAbsolutePath();
                } catch (IllegalArgumentException iae) {
                    name = uri.toString();
                }
                sb.append(name).append("\n\t");
            }
            sb.append("\n");
        }
        if (!this.filePathsChanged.isEmpty()) {
            sb.append("Changed files(Str): ").append(filePathsChanged.toString().replace(",", "\n\t")).append("\n");
        }
        if (parent != null) {
            sb.append("Parent {");  //NOI18N
            parent.createLogMessage(sb, reported, depth + 1);
            sb.append("}\n"); //NOI18N
        }
        
        if (threadDump != null) {
            sb.append("Thread dump:\n").append(threadDump).append("\n");
        }
        if (secondDump != null) {
            sb.append("Thread dump #2 (after ").
                    append(SECOND_DUMP_DELAY / 1000).
                    append(" seconds):\n").
                    append(secondDump).append("\n");
        }
        
        if (predecessor != null) {
            sb.append("Predecessor: {");
            predecessor.createLogMessage(sb, reported, depth + 1);
            sb.append("}\n");
        }

        if (absorbed != null) {
            sb.append("Absorbed {");    //NOI18N
            for (LogContext a : absorbed) {
                a.createLogMessage(sb, reported, depth + 1);
            }
            sb.append("}\n");             //NOI18N
        }
        
    }

    private static final Logger LOG = Logger.getLogger(LogContext.class.getName());
    private static final String LOG_MESSAGE = "SCAN_CANCELLED"; //NOI18
    private static final String LOG_MESSAGE_EARLY = "SCAN_CANCELLED_EARLY"; //NOI18N
    private static final String LOG_EXCEEDS_RATE = "SCAN_EXCEEDS_RATE {0}"; //NOI18N
    
    /**
     * Ring buffer that saves times and LogContexts for some past minutes.
     */
    private static class RingTimeBuffer {
        private static final int INITIAL_RINGBUFFER_SIZE = 20;
        /**
         * time limit to keep history, in minutes
         */
        private int historyLimit;
        
        /**
         * Ring buffer of timestamps
         */
        private long[]          times = new long[INITIAL_RINGBUFFER_SIZE];
        
        /**
         * LogContexts, at the same indexes as their timestamps
         */
        private LogContext[]    contexts = new LogContext[INITIAL_RINGBUFFER_SIZE];
        
        /**
         * Start = start of the data. Limit = just beyond of the data.
         * limit == start => empty buffer. Data is stored from start to the limit
         * modulo buffer sie.
         */
        private int start, limit;
        
        /**
         * index just beyond the last reported LogContext, -1 if nothing was
         * reported - will be printed from start/found position
         */
        private int reportedEnd = -1;
        
        /**
         * Timestamp of the last mark in the ringbuffer; for LRU expiration.
         */
        private long lastTime;

        public RingTimeBuffer(int historyLimit) {
            this.historyLimit = historyLimit;
        }
        
        /**
         * Advances start, dicards entries older that historyLimit.
         * @param now 
         */
        private void updateStart(long now) {
            long from = now - fromMinutes(historyLimit);
            
            while (!isEmpty() && times[start] < from) {
                // free for GC
                contexts[start] = null;
                
                if (reportedEnd == start) {
                    reportedEnd = -1;
                }
                start = inc(start);
            };
        }
        
        /**
         * ensures some minimum space is available; if gap reaches zero,
         * doubles the buffer size.
         */
        private void ensureSpaceAvailable() {
            if (!isEmpty() && gapSize() == 0) {
                long[] times2 = new long[times.length * 2];
                LogContext[] contexts2 = new LogContext[times.length * 2];
                
                int l;
                if (limit >= start) {
                    System.arraycopy(times, start, times2, 0, limit - start);
                    System.arraycopy(contexts, start, contexts2, 0, limit - start);
                    l = limit - start;
                } else {
                    // limit < start, end-of-array in the middle:
                    System.arraycopy(times, start, times2, 0, times.length - start);
                    System.arraycopy(times, 0, times2, times.length - start, limit);

                    System.arraycopy(contexts, start, contexts2, 0, times.length - start);
                    System.arraycopy(contexts, 0, contexts2, times.length - start, limit);

                    l = limit + (times.length - start);
                }
                limit = l;
                start = 0;

                this.times = times2;
                this.contexts = contexts2;
            }
        }
        
        /**
         * Adds LogContext to the ring buffer. Reports excess mark rate.
         * @param ctx 
         */
        public void mark(LogContext ctx) {
            long l = System.currentTimeMillis();
            updateStart(l);
            ensureSpaceAvailable();
            times[limit] = l;
            contexts[limit] = ctx;
            limit = inc(limit);
            
            EventType type = ctx.eventType;
            checkAndReport(l, type.getMinutes(), type.getTreshold());
            
            lastTime = l;
        }
        
        private int inc(int i) {
            return (i + 1) % ringSize();
        }
        
        private int ringSize() {
            return times.length;
        }
        
        private boolean isEmpty() {
            return start == limit;
        }
        
        private int gapSize() {
            if (start > limit) {
                return start - limit;
            } else {
                return start + ringSize() - limit;
            }
        }
        
        private int dataSize(int start, int end) {
            if (start < end) {
                return end - start;
            } else {
                return end + ringSize() - start;
            }
        }
        
        private Pair<Integer, Integer> findHigherRate(long minTime, int minutes, int treshold) {
            int s = reportedEnd == -1 ? start : reportedEnd;
            int l = -1;
            
            // skip events earlier than history limit; should be already cleared.
            while (s != limit && times[s] < minTime) {
                s = inc(s);
                if (s == l) {
                    l = -1;
                }
            }
            
            long minDiff = fromMinutes(minutes);
            do {
                if (s == limit) {
                    // end of data reached
                    return null;
                }
                // end of previous range reached, or even passed, reset range.
                if (l == -1) {
                    l = s;
                }

                long t = times[s];
                while (l != limit && (times[l] - t) < minDiff) {
                    l = inc(l);
                }
                if (dataSize(s, l) > treshold) {
                    return Pair.<Integer, Integer>of(s, l);
                }
                s = inc(s);
            } while (l != limit);
            return null;
        }
        
        void checkAndReport(long now, int minutes, int treshold) {
            long minTime = now - fromMinutes(historyLimit);
            Pair<Integer, Integer> found = findHigherRate(minTime, minutes, treshold);
            if (found == null) {
                return;
            }
            if (closing || RepositoryUpdater.getDefault().getState() == RepositoryUpdater.State.STOPPED) {
                return;
            }
            
            LOG.log(Level.WARNING, "Excessive indexing rate detected: " + dataSize(found.first(), found.second()) + " in " + minutes + "mins, treshold is " + treshold +
                    ". Dumping suspicious contexts");
            int index;
            
             for (index = found.first(); index != found.second(); index = (index + 1) % times.length) {
                contexts[index].log(false, false);
            }
            LOG.log(Level.WARNING, "=== End excessive indexing");
            this.reportedEnd = index;
        }
    }
    
    static class Stats {
        /**
         * For each possible event, one ring-buffer of LogContexts.
         */
        private Map<EventType, RingTimeBuffer>  history = new EnumMap<>(EventType.class);
        
        /**
         * For each root, one ring-buffer per event type. Items are removed using least recently accessed strategy. Once an
         * item is touched, it is removed and re-added so it is at the tail of the entry iterator.
         */
        private LinkedHashMap<URL, Map<EventType, RingTimeBuffer>> rootHistory = new LinkedHashMap<URL, Map<EventType, RingTimeBuffer>>(9, 0.7f, true);
        
        public synchronized void record(LogContext ctx) {
            EventType type = ctx.eventType;
            
            if (ctx.root != null) {
                if (type == EventType.INDEXER || type == EventType.MANAGER) {
                    recordIndexer(type, ctx.root, ctx);
                    return;
                }
            }
            recordRegular(type, ctx);
        }
        
        private void expireRoots() {
            long l = System.currentTimeMillis();
            
            for (Iterator<Map<EventType, RingTimeBuffer>> mapIt = rootHistory.values().iterator(); mapIt.hasNext(); ) {
                Map<EventType, RingTimeBuffer> map = mapIt.next();
                for (Iterator<Map.Entry<EventType, RingTimeBuffer>> it = map.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<EventType, RingTimeBuffer> entry = it.next();
                    EventType et = entry.getKey();
                    RingTimeBuffer rb = entry.getValue();
                    long limit = l - fromMinutes(et.getMinutes());
                    if (rb.lastTime < limit) {
                        it.remove();
                    }
                }
                if (map.isEmpty()) {
                    mapIt.remove();
                } else {
                    break;
                }
            }
        }
        
        private void recordIndexer(EventType et, URL root, LogContext ctx) {
            expireRoots();
            // re-adding maintains LRU order of the LinkedHM
            Map<EventType, RingTimeBuffer> map = rootHistory.remove(root);
            if (map == null) {
                map = new EnumMap<EventType, RingTimeBuffer>(EventType.class);
            }
            rootHistory.put(root, map);
            RingTimeBuffer existing = map.get(et);
            if (existing == null) {
                existing = new RingTimeBuffer(et.getMinutes() * 2);
                map.put(et, existing);
            }
            existing.mark(ctx);
        }
        
        private void recordRegular(EventType type, LogContext ctx) {
            
            RingTimeBuffer buf = history.get(type);
            if (buf == null) {
                buf = new RingTimeBuffer(type.getMinutes() * 2);
                history.put(type, buf);
            }
            
            buf.mark(ctx);
        }
    }

    private static long fromMinutes(int mins) {
        return mins * 60 * 1000;
    }
        
    private static final Stats STATS = new Stats();

    synchronized void reindexForced(URL root, String indexerName) {
        if (reindexInitiators.isEmpty()) {
            reindexInitiators = new HashMap<URL, Set<String>>();
        }
        Set<String> inits = reindexInitiators.get(root);
        if (inits == null) {
            inits = new HashSet<String>();
            reindexInitiators.put(root, inits);
        }
        inits.add(indexerName);
    }
    synchronized void newIndexerSeen(String s) {
        if (indexersAdded.isEmpty()) {
            indexersAdded = new ArrayList<String>();
        }
        indexersAdded.add(s);
    }
    
    private static final Logger BACKDOOR_LOG = Logger.getLogger(LogContext.class.getName() + ".backdoor");
    
    static {
        BACKDOOR_LOG.addHandler(new LH());
        BACKDOOR_LOG.setUseParentHandlers(false);
    }
    
    private static class LH extends java.util.logging.Handler {
        @Override
        public void publish(LogRecord record) {
            String msg = record.getMessage();
            if (msg.equals("INDEXER_START")) {
                String indexerName = (String)record.getParameters()[0];
//                RootInfo ri = currentlyIndexedRoot.get();
//                if (ri != null) {
//                    ri.startIndexer(indexerName);
//                }
                LogContext lcx = currentLogContext.get();
                if (lcx != null) {
                    lcx.startIndexer(indexerName);
                }
            } else if (msg.equals("INDEXER_END")) {
                String indexerName = (String)record.getParameters()[0];
                LogContext lcx = currentLogContext.get();
                if (lcx != null) {
                    lcx.finishIndexer(indexerName);
                }
//                RootInfo ri = currentlyIndexedRoot.get();
//                if (ri != null) {
//                    ri.finishIndexer(indexerName);
//                }
            }
            record.setLevel(Level.OFF);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
