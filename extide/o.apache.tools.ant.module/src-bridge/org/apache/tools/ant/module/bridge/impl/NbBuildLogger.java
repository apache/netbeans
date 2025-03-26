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

package org.apache.tools.ant.module.bridge.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.module.run.Hyperlink;
import org.apache.tools.ant.module.run.LoggerTrampoline;
import org.apache.tools.ant.module.run.StandardLogger;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.project.indexingbridge.IndexingBridge;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * NetBeans-sensitive build logger.
 * Just delegates all events to the registered SPI loggers
 * through an abstraction layer.
 * Synchronization: all callbacks are synchronized, both to protect access to logger
 * caches, and to prevent AntBridge.suspend/resumeDelegation from being called with
 * dynamic overlaps.
 * @author Jesse Glick
 */
final class NbBuildLogger implements BuildListener, LoggerTrampoline.AntSessionImpl {
    
    private static final Logger LOG = Logger.getLogger(NbBuildLogger.class.getName());
    
    final AntSession thisSession;
    
    private final File origScript;
    private String[] targets = null;
    final OutputWriter out;
    final OutputWriter err;
    final InputOutput io;
    private final int verbosity;
    private final Map<String,String> properties;
    private final Set<? extends String> concealedProperties;
    private final String displayName;
    private final Runnable interestingOutputCallback;
    private final ProgressHandle handle;
    private boolean insideRunTask = false; // #95201
    private IndexingBridge.Lock protectedMode; // #211005
    private final Object protectedModeLock = new Object();
    private final RequestProcessor.Task sleepTask = new RequestProcessor(NbBuildLogger.class.getName(), 1, false, false).create(new Runnable() {
        public @Override void run() {
            handle.suspend(insideRunTask ? NbBundle.getMessage(NbBuildLogger.class, "MSG_sleep_running") : "");
            exitProtectedMode();
        }
    });
    private static final int SLEEP_DELAY = 5000;
    
    private final Map<AntLogger,Object> customData = new HashMap<AntLogger,Object>();
    
    private List<AntLogger> interestedLoggers = null;
    private Map<File/*|null*/,Collection<AntLogger>> interestedLoggersByScript = new HashMap<File,Collection<AntLogger>>();
    private Map<String/*|null*/,Collection<AntLogger>> interestedLoggersByTarget = new HashMap<String,Collection<AntLogger>>();
    private Map<String/*|null*/,Collection<AntLogger>> interestedLoggersByTask = new HashMap<String,Collection<AntLogger>>();
    private Map<Integer,Collection<AntLogger>> interestedLoggersByLevel = new HashMap<Integer,Collection<AntLogger>>();
    
    private final Set<Project> projectsWithProperties = Collections.synchronizedSet(new WeakSet<Project>());
    
    private final Set<Throwable> consumedExceptions = new WeakSet<Throwable>();
    
    /** whether this process should be halted at the next safe point */
    private boolean stop = false;
    /** whether this process is thought to be still running */
    private boolean running = true;
    private static final ThreadLocal<Boolean> insideToString = new ThreadLocal<Boolean>() {
        protected @Override Boolean initialValue() {
            return false;
        }
    };
    
    /**
     * Map from master build scripts to maps from imported target names to imported locations.
     * Hack for lack of Target.getLocation() in Ant 1.6.2 and earlier.
     * Unused if targetGetLocation is not null.
     */
    private final Map<String,Map<String,String>> knownImportedTargets = Collections.synchronizedMap(new HashMap<String,Map<String,String>>());
    /**
     * Main script known to be being parsed at the moment.
     * Unused if targetGetLocation is not null.
     */
    private String currentlyParsedMainScript = null;
    /**
     * Imported script known to be being parsed at the moment.
     * Unused if targetGetLocation is not null.
     */
    private String currentlyParsedImportedScript = null;
    /**
     * Last task which was known to be running. Heuristic. Cf. #49464.
     */
    private Task lastTask = null;
    private synchronized Task getLastTask() {
        return lastTask;
    }
    private synchronized void setLastTask(Task lastTask) {
        this.lastTask = lastTask;
    }
    
    @SuppressWarnings("LeakingThisInConstructor")
    NbBuildLogger(File origScript, OutputWriter out, OutputWriter err, int verbosity, String displayName, Map<String,String> properties,
            Set<? extends String> concealedProperties, Runnable interestingOutputCallback, ProgressHandle handle, InputOutput io) {
        thisSession = LoggerTrampoline.ANT_SESSION_CREATOR.makeAntSession(this);
        this.origScript = origScript;
        this.out = out;
        this.err = err;
        this.io = io;
        this.verbosity = verbosity;
        this.properties = properties;
        this.concealedProperties = concealedProperties;
        this.displayName = displayName;
        this.interestingOutputCallback = interestingOutputCallback;
        this.handle = handle;
        LOG.log(Level.FINE, "---- Initializing build of {0} \"{1}\" at verbosity {2} ----", new Object[] {origScript, displayName, verbosity});
        enterProtectedMode(isCompileOnSave(properties));
    }

    //where
    private static boolean isCompileOnSave(final Map<String,String> properties) {
        return "true".equals(properties.get("nb.wait.for.caches")); //NOI18N
    }

    private void enterProtectedMode(final boolean waitForScan) {
        synchronized (protectedModeLock) {
            if (protectedMode == null) {
                protectedMode = IndexingBridge.getDefault().protectedMode(waitForScan);
            }
        }
    }
    private void exitProtectedMode() {
        synchronized (protectedModeLock) {
            if (protectedMode != null) {
                protectedMode.release();
                protectedMode = null;
            }
        }
    }
    
    /** Try to stop running at the next safe point. */
    public void stop() {
        stop = true;
    }
    
    /** Stop the build now if requested. Also restarts sleep timer. */
    private void checkForStop() {
        if (stop) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbBuildLogger.class, "MSG_stopped", displayName));
            throw new ThreadDeath();
        }
        if (running) {
            handle.switchToIndeterminate();
            enterProtectedMode(false);
            sleepTask.schedule(SLEEP_DELAY);
        }
    }

    /**
     * Notify this process that it has been shut down.
     * Refuse any further queries on AntEvent etc.
     * @see "#71266"
     */
    public void shutdown() {
        running = false;
        out.close();
        err.close();
        handle.finish();
        sleepTask.cancel();
        exitProtectedMode();
    }

    private void verifyRunning() {
        if (!running && !insideToString.get()) {
            throw new ThreadDeath(); // AntSession/AntEvent/TaskStructure method called after completion of Ant process
        }
    }
    
    /**
     * Compute a list of loggers to use for this session.
     * Do not do it in the constructor since the actual targets will not have been
     * set and some loggers may care about the targets. However if buildInitializationFailed
     * is called before then, initialize them anyway.
     */
    private synchronized void initInterestedLoggers() {
        if (interestedLoggers == null) {
            interestedLoggers = new ArrayList<AntLogger>();
            for (AntLogger l : Lookup.getDefault().lookupAll(AntLogger.class)) {
                if (l.interestedInSession(thisSession)) {
                    interestedLoggers.add(l);
                }
            }
            LOG.log(Level.FINEST, "getInterestedLoggers: loggers={0}", interestedLoggers);
        }
    }

    private synchronized Collection<AntLogger> getInterestedLoggers() {
        initInterestedLoggers();
        return new ArrayList<AntLogger>(interestedLoggers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // could use List<Collection<AntLogger>> but too slow?
    private final Collection<AntLogger>[] interestedLoggersByVariousCriteria = new Collection[4];
    private static final Comparator<Collection<AntLogger>> INTERESTED_LOGGERS_SORTER = new Comparator<Collection<AntLogger>>() {
        public @Override int compare(Collection<AntLogger> c1, Collection<AntLogger> c2) {
            int x = c1.size() - c2.size(); // reverse sort by size
            if (x != 0) {
                return x;
            } else {
                return System.identityHashCode(c1) - System.identityHashCode(c2);
            }
        }
    };
    /**
     * Get those loggers interested in a given event.
     */
    private Collection<AntLogger> getInterestedLoggersByEvent(AntEvent e) {
        File scriptLocation = e.getScriptLocation();
        String targetName = e.getTargetName();
        String taskName = e.getTaskName();
        int logLevel = e.getLogLevel();
        synchronized (this) { // #132945: <parallel> can deadlock if you block on event info here
            initInterestedLoggers();
            // Start with the smallest one and go down.
            interestedLoggersByVariousCriteria[0] = getInterestedLoggersByScript(scriptLocation);
            interestedLoggersByVariousCriteria[1] = getInterestedLoggersByTarget(targetName);
            interestedLoggersByVariousCriteria[2] = getInterestedLoggersByTask(taskName);
            interestedLoggersByVariousCriteria[3] = getInterestedLoggersByLevel(logLevel);
            Arrays.sort(interestedLoggersByVariousCriteria, INTERESTED_LOGGERS_SORTER);
            LOG.log(Level.FINEST, "getInterestedLoggersByVariousCriteria: event={0} loggers={1}", new Object[] {e, Arrays.asList(interestedLoggersByVariousCriteria)});
            // XXX could probably be even a bit more efficient by iterating on the fly...
            // and by skipping the sorting which is probably overkill for a small number of a loggers (or hardcode the sort)
            List<AntLogger> loggers = new LinkedList<AntLogger>(interestedLoggersByVariousCriteria[0]);
            for (int i = 1; i < 4; i++) {
                loggers.retainAll(interestedLoggersByVariousCriteria[i]);
            }
            LOG.log(Level.FINEST, "getInterestedLoggersByEvent: event={0} loggers={1}", new Object[] {e, loggers});
            return loggers;
        }
    }
    
    private synchronized Collection<AntLogger> getInterestedLoggersByScript(File script) {
        initInterestedLoggers();
        Collection<AntLogger> c = interestedLoggersByScript.get(script);
        if (c == null) {
            c = new LinkedHashSet<AntLogger>(interestedLoggers.size());
            interestedLoggersByScript.put(script, c);
            for (AntLogger l : interestedLoggers) {
                if (l.interestedInAllScripts(thisSession) || (script != null && l.interestedInScript(script, thisSession))) {
                    c.add(l);
                }
            }
            LOG.log(Level.FINEST, "getInterestedLoggersByScript: script={0} loggers={1}", new Object[] {script, c});
        }
        return c;
    }
    
    private synchronized Collection<AntLogger> getInterestedLoggersByTarget(String target) {
        Collection<AntLogger> c = interestedLoggersByTarget.get(target);
        if (c == null) {
            c = new LinkedHashSet<AntLogger>(interestedLoggers.size());
            interestedLoggersByTarget.put(target, c);
            for (AntLogger l : interestedLoggers) {
                String[] interestingTargets = l.interestedInTargets(thisSession);
                if (interestingTargets == AntLogger.ALL_TARGETS ||
                        (target != null && Arrays.asList(interestingTargets).contains(target)) ||
                        (target == null && interestingTargets == AntLogger.NO_TARGETS)) {
                    c.add(l);
                }
            }
            LOG.log(Level.FINEST, "getInterestedLoggersByTarget: target={0} loggers={1}", new Object[] {target, c});
        }
        return c;
    }
    
    private synchronized Collection<AntLogger> getInterestedLoggersByTask(String task) {
        Collection<AntLogger> c = interestedLoggersByTask.get(task);
        if (c == null) {
            c = new LinkedHashSet<AntLogger>(interestedLoggers.size());
            interestedLoggersByTask.put(task, c);
            for (AntLogger l : interestedLoggers) {
                String[] tasks = l.interestedInTasks(thisSession);
                if (tasks == AntLogger.ALL_TASKS ||
                        (task != null && Arrays.asList(tasks).contains(task)) ||
                        (task == null && tasks == AntLogger.NO_TASKS)) {
                    c.add(l);
                }
            }
            LOG.log(Level.FINEST, "getInterestedLoggersByTask: task={0} loggers={1}", new Object[] {task, c});
        }
        return c;
    }
    
    private synchronized Collection<AntLogger> getInterestedLoggersByLevel(int level) {
        Collection<AntLogger> c = interestedLoggersByLevel.get(level);
        if (c == null) {
            c = new LinkedHashSet<AntLogger>(interestedLoggers.size());
            interestedLoggersByLevel.put(level, c);
            for (AntLogger l : interestedLoggers) {
                if (level == -1) {
                    c.add(l);
                } else {
                    int[] levels = l.interestedInLogLevels(thisSession);
                    for (int _level : levels)  {
                        if (_level == level) {
                            c.add(l);
                            break;
                        }
                    }
                }
            }
            LOG.log(Level.FINEST, "getInterestedLoggersByLevel: level={0} loggers={1}", new Object[] {level, c});
        }
        return c;
    }
    
    synchronized void setActualTargets(String[] targets) {
        this.targets = targets;
    }
    
    void buildInitializationFailed(BuildException be) {
        AntEvent ev = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(be));
        LOG.log(Level.FINE, "buildInitializationFailed: {0}", ev);
        for (AntLogger l : getInterestedLoggersByScript(null)) {
            l.buildInitializationFailed(ev);
        }
        interestingOutputCallback.run();
    }
    
    public @Override void buildStarted(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            LOG.log(Level.FINE, "buildStarted: {0}", e);
            for (AntLogger l : getInterestedLoggers()) {
                try {
                    l.buildStarted(e);
                } catch (RuntimeException x) {
                    LOG.log(Level.WARNING, null, x);
                }
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public @Override void buildFinished(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            // #82160: do not call checkForStop() here
            stop = false; // do not throw ThreadDeath on messageLogged from BridgeImpl cleanup code
            setLastTask(null);
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            LOG.log(Level.FINE, "buildFinished: {0}", e);
            if (e.getException() != null) {
                LOG.log(Level.FINE, null, e.getException());
            }
            for (AntLogger l : getInterestedLoggers()) {
                try {
                    l.buildFinished(e);
                } catch (RuntimeException x) {
                    LOG.log(Level.WARNING, null, x);
                } catch (Error x) {
                    LOG.log(Level.WARNING, null, x);
                }
            }
            if (e.getException() != null) {
                interestingOutputCallback.run();
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public @Override void targetStarted(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            setLastTask(null);
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            LOG.log(Level.FINE, "targetStarted: {0}", e);
            for (AntLogger l : getInterestedLoggersByEvent(e)) {
                try {
                    l.targetStarted(e);
                } catch (RuntimeException x) {
                    LOG.log(Level.WARNING, null, x);
                }
            }
            // Update progress handle label so user can see what is being run.
            Project p = ev.getProject();
            String projectName = null;
            if (p != null) {
                projectName = p.getName();
            }
            String targetName = e.getTargetName();
            if (targetName != null) {
                String message;
                if (projectName != null) {
                    message = NbBundle.getMessage(NbBuildLogger.class, "MSG_progress_target", projectName, targetName);
                } else {
                    message = targetName;
                }
                /*
                if (message.equals(displayName)) {
                    // Redundant in this case.
                    message = "";
                }
                 */
                handle.progress(message);
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public @Override void targetFinished(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            setLastTask(null);
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            LOG.log(Level.FINE, "targetFinished: {0}", e);
            for (AntLogger l : getInterestedLoggersByEvent(e)) {
                try {
                    l.targetFinished(e);
                } catch (RuntimeException x) {
                    LOG.log(Level.WARNING, null, x);
                }
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public @Override void taskStarted(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            setLastTask(ev.getTask());
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            LOG.log(Level.FINE, "taskStarted: {0}", e);
            for (AntLogger l : getInterestedLoggersByEvent(e)) {
                try {
                    l.taskStarted(e);
                } catch (RuntimeException x) {
                    LOG.log(Level.WARNING, null, x);
                }
            }
            if ("input".equals(e.getTaskName())) { // #81139; NOI18N
                TaskStructure s = e.getTaskStructure();
                if (s != null) {
                    String def = s.getAttribute("defaultvalue"); // NOI18N
                    if (def != null) {
                        NbInputHandler.setDefaultValue(e.evaluate(def));
                    }
                }
            }
            if (isRunTask(e)) {
                insideRunTask = true;
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }

    private boolean isRunTask(AntEvent event) { // #95201
        String taskName = event.getTaskName();
        return "java".equals(taskName) || "exec".equals(taskName); // NOI18N
    }

    public @Override void taskFinished(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            setLastTask(null);
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            LOG.log(Level.FINE, "taskFinished: {0}", e);
            for (AntLogger l : getInterestedLoggersByEvent(e)) {
                try {
                    l.taskFinished(e);
                } catch (RuntimeException x) {
                    LOG.log(Level.WARNING, null, x);
                }
            }
            NbInputHandler.setDefaultValue(null);
            if (isRunTask(e)) {
                insideRunTask = false;
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    /**
     * Pattern matching an Ant message logged when it is parsing a build script.
     * Hack for lack of Target.getLocation() in Ant 1.6.2 and earlier.
     * Captured groups:
     * <ol>
     * <li>absolute path of build script
     * </ol>
     */
    private static final Pattern PARSING_BUILDFILE_MESSAGE =
        Pattern.compile("parsing buildfile (.+) with URI = (?:.+)"); // NOI18N
    
    /**
     * Pattern matching an Ant message logged when it is importing a build script.
     * Hack for lack of Target.getLocation() in Ant 1.6.2 and earlier.
     * Captured groups:
     * <ol>
     * <li>absolute path of build script which is doing the importing
     * </ol>
     */
    private static final Pattern IMPORTING_FILE_MESSAGE =
        Pattern.compile("Importing file (?:.+) from (.+)"); // NOI18N
    
    /**
     * Pattern matching an Ant message logged when it has encountered a target in some build script.
     * Hack for lack of Target.getLocation() in Ant 1.6.2 and earlier.
     * Captured groups:
     * <ol>
     * <li>target name
     * </ol>
     */
    private static final Pattern PARSED_TARGET_MESSAGE =
        Pattern.compile(" \\+Target: (.+)"); // NOI18N
    
    public @Override void messageLogged(BuildEvent ev) {
        if (!running) { // #145722
            return;
        }
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, true));
            LOG.log(Level.FINER, "messageLogged: {0}", e);
            for (AntLogger l : getInterestedLoggersByEvent(e)) {
                try {
                    l.messageLogged(e);
                } catch (RuntimeException x) {
                    LOG.log(Level.WARNING, null, x);
                }
            }
            // Let the hacks begin!
            String msg = ev.getMessage();
            if (msg.contains("ant.PropertyHelper") || /* #71816 */ msg.contains("ant.projectHelper")) { // NOI18N
                // Only after this has been defined can we get any properties.
                // Even trying earlier will give a recursion error since this pseudoprop
                // is set lazily, which produces a new message logged event.
                projectsWithProperties.add(ev.getProject());
            }
            if (targetGetLocation == null) {
                // Try to figure out which imported targets belong to which actual scripts.
                // XXX consider keeping a singleton Matcher for each pattern and reusing it
                // or just doing string comparisons
                Matcher matcher;
                if ((matcher = PARSING_BUILDFILE_MESSAGE.matcher(msg)).matches()) {
                    if (currentlyParsedMainScript != null) {
                        currentlyParsedImportedScript = matcher.group(1);
                    }
                    LOG.log(Level.FINE, "Got PARSING_BUILDFILE_MESSAGE: {0}", currentlyParsedImportedScript);
                    setLastTask(null);
                } else if ((matcher = IMPORTING_FILE_MESSAGE.matcher(msg)).matches()) {
                    currentlyParsedMainScript = matcher.group(1);
                    currentlyParsedImportedScript = null;
                    LOG.log(Level.FINE, "Got IMPORTING_FILE_MESSAGE: {0}", currentlyParsedMainScript);
                    setLastTask(null);
                } else if ((matcher = PARSED_TARGET_MESSAGE.matcher(msg)).matches()) {
                    if (currentlyParsedMainScript != null && currentlyParsedImportedScript != null) {
                        Map<String,String> targetLocations = knownImportedTargets.get(currentlyParsedMainScript);
                        if (targetLocations == null) {
                            targetLocations = new HashMap<String,String>();
                            knownImportedTargets.put(currentlyParsedMainScript, targetLocations);
                        }
                        targetLocations.put(matcher.group(1), currentlyParsedImportedScript);
                    }
                    LOG.log(Level.FINE, "Got PARSED_TARGET_MESSAGE: {0}", matcher.group(1));
                    setLastTask(null);
                }
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public @Override File getOriginatingScript() {
        verifyRunning();
        return origScript;
    }
    
    public @Override String[] getOriginatingTargets() {
        verifyRunning();
        return targets != null ? targets : new String[0];
    }
    
    public @Override synchronized Object getCustomData(AntLogger logger) {
        verifyRunning();
        return customData.get(logger);
    }
    
    public @Override synchronized void putCustomData(AntLogger logger, Object data) {
        verifyRunning();
        customData.put(logger, data);
    }
    
    public @Override void println(String message, boolean error, OutputListener listener) {
        verifyRunning();
        LOG.log(Level.FINEST, "println: error={0} listener={1} message={2}", new Object[] {error, listener, message});
        OutputWriter ow = error ? err : out;
        try {
            if (listener != null) {
                // Loggers wishing for more control can use getIO and do it themselves.
                boolean important = StandardLogger.isImportant(message);
                ow.println(message, listener, important);
                interestingOutputCallback.run();
            } else {
                ow.println(message);
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, null, e);
        }
    }
    
    public @Override void deliverMessageLogged(AntEvent originalEvent, String message, int level) {
        verifyRunning();
        if (originalEvent == null) {
            throw new IllegalArgumentException("Must pass an original event to deliverMessageLogged"); // NOI18N
        }
        if (message == null) {
            throw new IllegalArgumentException("Must pass a message to deliverMessageLogged"); // NOI18N
        }
        if (level < AntEvent.LOG_ERR || level > AntEvent.LOG_DEBUG) {
            throw new IllegalArgumentException("Unknown log level for reposted log event: " + level); // NOI18N
        }
        LOG.log(Level.FINEST, "deliverMessageLogged: level={0} message={1}", new Object[] {level, message});
        AntEvent newEvent = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new RepostedEvent(originalEvent, message, level));
        for (AntLogger l : getInterestedLoggersByEvent(newEvent)) {
            try {
                l.messageLogged(newEvent);
            } catch (RuntimeException x) {
                LOG.log(Level.WARNING, null, x);
            }
        }
    }
    
    public @Override synchronized void consumeException(Throwable t) throws IllegalStateException {
        verifyRunning();
        if (isExceptionConsumed(t)) {
            throw new IllegalStateException("Already consumed " + t); // NOI18N
        }
        consumedExceptions.add(t);
    }
    
    public @Override synchronized boolean isExceptionConsumed(Throwable t) {
        verifyRunning();
        if (consumedExceptions.contains(t)) {
            return true;
        }
        // Check for nested exceptions too.
        Throwable nested = t.getCause();
        if (nested != null && isExceptionConsumed(nested)) {
            // cache that
            consumedExceptions.add(t);
            return true;
        }
        return false;
    }
    
    public @Override int getVerbosity() {
        verifyRunning();
        return verbosity;
    }

    @Override public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public boolean isConcealed(@NonNull final String propertyName) {
        Parameters.notNull("propertyName", propertyName);   //NOI18N
        return concealedProperties.contains(propertyName);
    }
    
    String getDisplayNameNoLock() {
        return displayName;
    }
    
    public @Override String getDisplayName() {
        verifyRunning();
        return displayName;
    }

    public @Override OutputListener createStandardHyperlink(URL file, String message, int line1, int column1, int line2, int column2) {
        verifyRunning();
        return new Hyperlink(file, message, line1, column1, line2, column2);
    }

    public @Override InputOutput getIO() {
        return io;
    }
    
    // Accessors for stuff which is specific to particular versions of Ant.
    private static final Method targetGetLocation; // 1.6.2+
    private static final Method locationGetFileName; // 1.6+
    private static final Method locationGetLineNumber; // 1.6+
    private static final Method runtimeConfigurableGetAttributeMap; // 1.6+
    private static final Method runtimeConfigurableGetChildren; // 1.6+
    private static final Method runtimeConfigurableGetText; // 1.6+
    static {
        Method _targetGetLocation = null;
        try {
            _targetGetLocation = Target.class.getMethod("getLocation"); // NOI18N
            if (AntBridge.getInterface().getAntVersion().indexOf("1.6.2") != -1) { // NOI18N
                // Unfortunately in 1.6.2 the method exists but it doesn't work (Ant #28599):
                _targetGetLocation = null;
            }
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
        }
        targetGetLocation = _targetGetLocation;
        Method _locationGetFileName = null;
        try {
            _locationGetFileName = Location.class.getMethod("getFileName"); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
        }
        locationGetFileName = _locationGetFileName;
        Method _locationGetLineNumber = null;
        try {
            _locationGetLineNumber = Location.class.getMethod("getLineNumber"); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
        }
        locationGetLineNumber = _locationGetLineNumber;
        Method _runtimeConfigurableGetAttributeMap = null;
        try {
            _runtimeConfigurableGetAttributeMap = RuntimeConfigurable.class.getMethod("getAttributeMap"); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
        }
        runtimeConfigurableGetAttributeMap = _runtimeConfigurableGetAttributeMap;
        Method _runtimeConfigurableGetChildren = null;
        try {
            _runtimeConfigurableGetChildren = RuntimeConfigurable.class.getMethod("getChildren"); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
        }
        runtimeConfigurableGetChildren = _runtimeConfigurableGetChildren;
        Method _runtimeConfigurableGetText = null;
        try {
            _runtimeConfigurableGetText = RuntimeConfigurable.class.getMethod("getText"); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
        }
        runtimeConfigurableGetText = _runtimeConfigurableGetText;
    }

    /**
     * Try to find the location of an Ant target.
     * @param project if not null, the main project from which this target might have been imported
     */
    private Location getLocationOfTarget(Target target, Project project) {
        if (targetGetLocation != null) {
            try {
                return (Location) targetGetLocation.invoke(target);
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
        // For Ant 1.6.2 and earlier, hope we got the right info from the hacks above.
        LOG.log(Level.FINEST, "knownImportedTargets: {0}", knownImportedTargets);
        if (project != null) {
            String file = project.getProperty("ant.file"); // NOI18N
            if (file != null) {
                Map<String,String> targetLocations = knownImportedTargets.get(file);
                if (targetLocations != null) {
                    String importedFile = targetLocations.get(target.getName());
                    if (importedFile != null) {
                        // Have no line number, note.
                        return new Location(importedFile);
                    }
                }
            }
        }
        // Dunno.
        return null;
    }
    
    private static String getFileNameOfLocation(Location loc) {
        if (locationGetFileName != null) {
            try {
                return (String) locationGetFileName.invoke(loc);
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
        // OK, using Ant 1.5.x.
        String locs = loc.toString();
        // Format: "$file:$line: " or "$file: " or ""
        int x = locs.indexOf(':');
        if (x != -1) {
            return locs.substring(0, x);
        } else {
            return null;
        }
    }
    
    private static int getLineNumberOfLocation(Location loc) {
        if (locationGetLineNumber != null) {
            try {
                return (Integer) locationGetLineNumber.invoke(loc);
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
        // OK, using Ant 1.5.x.
        String locs = loc.toString();
        // Format: "$file:$line: " or "$file: " or ""
        int x = locs.indexOf(':');
        if (x != -1) {
            int x2 = locs.indexOf(':', x + 1);
            if (x2 != -1) {
                String line = locs.substring(x + 1, x2);
                try {
                    return Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    // ignore?
                }
            }
        }
        return 0;
    }
    
    private static Map<String,String> getAttributeMapOfRuntimeConfigurable(RuntimeConfigurable rc) {
        Map<String, String> m = new HashMap<String, String>();
        if (runtimeConfigurableGetAttributeMap != null) {
            try {
                for (Map.Entry<?,?> entry : ((Map<?,?>) runtimeConfigurableGetAttributeMap.invoke(rc)).entrySet()) {
                    m.put(((String) entry.getKey()).toLowerCase(Locale.ENGLISH), (String) entry.getValue());
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    private static Enumeration<RuntimeConfigurable> getChildrenOfRuntimeConfigurable(RuntimeConfigurable rc) {
        if (runtimeConfigurableGetChildren != null) {
            try {
                return (Enumeration<RuntimeConfigurable>) runtimeConfigurableGetChildren.invoke(rc);
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
        return Collections.enumeration(Collections.<RuntimeConfigurable>emptySet());
    }
    
    private static String getTextOfRuntimeConfigurable(RuntimeConfigurable rc) {
        if (runtimeConfigurableGetText != null) {
            try {
                return ((StringBuffer) runtimeConfigurableGetText.invoke(rc)).toString();
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
        return "";
    }
    
    /**
     * Standard event implementation, delegating to the Ant BuildEvent and Project.
     */
    private final class Event implements LoggerTrampoline.AntEventImpl {
        
        private boolean consumed = false;
        private final BuildEvent e;
        private final Throwable exception;
        private final int level;
        private File scriptLocation;
        
        /**
         * Create a new regular event.
         * @param e the Ant build event
         * @param msgLogged true for logged events
         */
        public Event(BuildEvent e, boolean msgLogged) {
            this.e = e;
            exception = e.getException();
            if (msgLogged) {
                level = e.getPriority();
            } else {
                level = -1;
            }
        }
        
        /**
         * Create a new event for buildInitializationFailed.
         * @param exception the problem
         */
        public Event(Throwable exception) {
            e = null;
            this.exception = exception;
            level = -1;
        }
        
        public @Override AntSession getSession() {
            verifyRunning();
            return thisSession;
        }

        public @Override void consume() throws IllegalStateException {
            verifyRunning();
            if (consumed) {
                throw new IllegalStateException("Event already consumed"); // NOI18N
            }
            consumed = true;
        }

        public @Override boolean isConsumed() {
            verifyRunning();
            return consumed;
        }

        public @Override File getScriptLocation() {
            verifyRunning();
            if (scriptLocation != null) {
                return scriptLocation;
            }
            if (e == null) {
                return null;
            }
            Task task = e.getTask();
            if (task != null) {
                Location l = task.getLocation();
                if (l != null) {
                    String file = getFileNameOfLocation(l);
                    if (file != null) {
                        return scriptLocation = new File(file);
                    }
                }
            }
            Target target = e.getTarget();
            Project project = getProjectIfPropertiesDefined();
            if (target != null) {
                Location l = getLocationOfTarget(target, project);
                if (l != null) {
                    String file = getFileNameOfLocation(l);
                    if (file != null) {
                        return scriptLocation = new File(file);
                    }
                }
            }
            // #49464: guess at task.
            Task lastTask = getLastTask();
            if (lastTask != null) {
                Location l = lastTask.getLocation();
                if (l != null) {
                    String file = getFileNameOfLocation(l);
                    if (file != null) {
                        return scriptLocation = new File(file);
                    }
                }
            }
            // #104103: lastTask is more likely to be accurate.
            // Consider a call to Project.log from within a task run in an imported script.
            if (project != null) {
                String file = project.getProperty("ant.file"); // NOI18N
                if (file != null) {
                    return scriptLocation = new File(file);
                }
            }
            // #57153 suggests using SubBuildListener, but is it really necessary?
            return null;
        }
        
        private Project getProjectIfPropertiesDefined() {
            Project project = e.getProject();
            if (project != null && projectsWithProperties.contains(project)) {
                return project;
            } else {
                return null;
            }
        }

        public @Override int getLine() {
            verifyRunning();
            if (e == null) {
                return -1;
            }
            Task task = e.getTask();
            if (task != null) {
                Location l = task.getLocation();
                if (l != null) {
                    int line = getLineNumberOfLocation(l);
                    if (line > 0) {
                        return line;
                    }
                }
            }
            Target target = e.getTarget();
            if (target != null) {
                Location l = getLocationOfTarget(target, getProjectIfPropertiesDefined());
                if (l != null) {
                    int line = getLineNumberOfLocation(l);
                    if (line > 0) {
                        return line;
                    }
                }
            }
            // #49464: guess at task.
            Task lastTask = getLastTask();
            if (lastTask != null) {
                Location l = lastTask.getLocation();
                if (l != null) {
                    int line = getLineNumberOfLocation(l);
                    if (line > 0) {
                        return line;
                    }
                }
            }
            return -1;
        }

        public @Override String getTargetName() {
            verifyRunning();
            if (e == null) {
                return null;
            }
            Target target = e.getTarget();
            if (target != null) {
                String name = target.getName();
                if (name != null && name.length() > 0) {
                    return name;
                }
            }
            // #49464: guess at task.
            Task lastTask = getLastTask();
            if (lastTask != null) {
                target = lastTask.getOwningTarget();
                if (target != null) {
                    String name = target.getName();
                    if (name != null && name.length() > 0) {
                        return name;
                    }
                }
            }
            return null;
        }

        public @Override String getTaskName() {
            verifyRunning();
            if (e == null) {
                return null;
            }
            Task task = e.getTask();
            if (task != null) {
                return task.getRuntimeConfigurableWrapper().getElementTag();
            }
            // #49464: guess at task.
            Task lastTask = getLastTask();
            if (lastTask != null) {
                return lastTask.getRuntimeConfigurableWrapper().getElementTag();
            }
            return null;
        }

        public @Override TaskStructure getTaskStructure() {
            verifyRunning();
            Task task = e.getTask();
            if (task != null) {
                return LoggerTrampoline.TASK_STRUCTURE_CREATOR.makeTaskStructure(new TaskStructureImpl(task.getRuntimeConfigurableWrapper()));
            }
            // #49464: guess at task.
            Task lastTask = getLastTask();
            if (lastTask != null) {
                return LoggerTrampoline.TASK_STRUCTURE_CREATOR.makeTaskStructure(new TaskStructureImpl(lastTask.getRuntimeConfigurableWrapper()));
            }
            return null;
        }

        public @Override String getMessage() {
            verifyRunning();
            if (e == null) {
                return null;
            }
            return e.getMessage();
        }

        public @Override int getLogLevel() {
            verifyRunning();
            return level;
        }

        public @Override Throwable getException() {
            verifyRunning();
            return exception;
        }

        public @Override String getProperty(String name) {
            verifyRunning();
            Project project = getProjectIfPropertiesDefined();
            if (project != null) {
                String v = project.getProperty(name);
                if (v != null) {
                    return v;
                } else {
                    Object o = project.getReference(name);
                    if (o != null) {
                        return o.toString();
                    } else {
                        return null;
                    }
                }
            } else {
                return null;
            }
        }

        public @Override Set<String> getPropertyNames() {
            verifyRunning();
            Project project = getProjectIfPropertiesDefined();
            if (project != null) {
                Set<String> s = new HashSet<String>();
                s.addAll(NbCollections.checkedSetByFilter(project.getProperties().keySet(), String.class, true));
                s.addAll(NbCollections.checkedSetByFilter(project.getReferences().keySet(), String.class, true));
                return s;
            } else {
                return Collections.emptySet();
            }
        }

        public @Override String evaluate(String text) {
            verifyRunning();
            Project project = getProjectIfPropertiesDefined();
            if (project != null) {
                return project.replaceProperties(text);
            } else {
                return text;
            }
        }
        
        @Override
        public String toString() {
            assert !insideToString.get();
            insideToString.set(true);
            try {
            StringBuilder b = new StringBuilder("Event"); // NOI18N
            String s = getTargetName();
            if (s != null) {
                b.append(";targ=").append(s); // NOI18N
            }
            s = getTaskName();
            if (s != null) {
                b.append(";task=").append(s); // NOI18N
            }
            if (exception != null) {
                b.append(";exc=").append(exception); // NOI18N
            }
            if (level != -1) {
                b.append(";lvl=").append(LEVEL_NAMES[level]); // NOI18N
            }
            if (consumed) {
                b.append(";consumed"); // NOI18N
            }
            s = getMessage();
            if (s != null) {
                b.append(";msg=").append(s); // NOI18N
            }
            File f = getScriptLocation();
            if (f != null) {
                b.append(";scrLoc=").append(f); // NOI18N
            }
            return b.toString();
            } finally {
                insideToString.set(false);
            }
        }
        
    }
    private static final String[] LEVEL_NAMES = {"ERR", "WARN", "INFO", "VERBOSE", "DEBUG"}; // NOI18N
    
    /**
     * Reposted event delegating to an original one except for message and level.
     * @see #deliverMessageLogged
     */
    private final class RepostedEvent implements LoggerTrampoline.AntEventImpl {
        
        private final AntEvent originalEvent;
        private final String message;
        private final int level;
        private boolean consumed = false;
        
        public RepostedEvent(AntEvent originalEvent, String message, int level) {
            this.originalEvent = originalEvent;
            this.message = message;
            this.level = level;
        }
        
        public @Override void consume() throws IllegalStateException {
            verifyRunning();
            if (consumed) {
                throw new IllegalStateException("Event already consumed"); // NOI18N
            }
            consumed = true;
        }

        public @Override boolean isConsumed() {
            verifyRunning();
            return consumed;
        }
        
        public @Override AntSession getSession() {
            return originalEvent.getSession();
        }
        
        public @Override File getScriptLocation() {
            return originalEvent.getScriptLocation();
        }
        
        public @Override int getLine() {
            return originalEvent.getLine();
        }
        
        public @Override String getTargetName() {
            return originalEvent.getTargetName();
        }
        
        public @Override String getTaskName() {
            return originalEvent.getTaskName();
        }
        
        public @Override TaskStructure getTaskStructure() {
            return originalEvent.getTaskStructure();
        }
        
        public @Override String getMessage() {
            verifyRunning();
            return message;
        }
        
        public @Override int getLogLevel() {
            verifyRunning();
            return level;
        }
        
        public @Override Throwable getException() {
            verifyRunning();
            return null;
        }
        
        public @Override String getProperty(String name) {
            return originalEvent.getProperty(name);
        }
        
        public @Override Set<String> getPropertyNames() {
            return originalEvent.getPropertyNames();
        }
        
        public @Override String evaluate(String text) {
            return originalEvent.evaluate(text);
        }
        
        @Override
        public String toString() {
            return "RepostedEvent[consumed=" + consumed + ",level=" + level + ",message=" + message + /*",orig=" + originalEvent +*/ "]"; // NOI18N
        }
        
    }
    
    /**
     * Implementation of TaskStructure based on an Ant Task.
     * @see Event#getTaskStructure
     */
    private final class TaskStructureImpl implements LoggerTrampoline.TaskStructureImpl {
        
        private final RuntimeConfigurable rc;
        
        public TaskStructureImpl(RuntimeConfigurable rc) {
            this.rc = rc;
        }
        
        public @Override String getName() {
            verifyRunning();
            String name = rc.getElementTag();
            if (name != null) {
                return name;
            } else {
                // What does this mean?
                return "";
            }
        }
        
        public @Override String getAttribute(String name) {
            verifyRunning();
            return getAttributeMapOfRuntimeConfigurable(rc).get(name.toLowerCase(Locale.ENGLISH));
        }
        
        public @Override Set<String> getAttributeNames() {
            verifyRunning();
            return getAttributeMapOfRuntimeConfigurable(rc).keySet();
        }
        
        public @Override String getText() {
            verifyRunning();
            String s = getTextOfRuntimeConfigurable(rc);
            if (s.length() > 0) {
                // XXX is it appropriate to trim() this? probably not
                return s;
            } else {
                return null;
            }
        }
        
        public @Override TaskStructure[] getChildren() {
            verifyRunning();
            List<TaskStructure> structures = new ArrayList<TaskStructure>();
            for (RuntimeConfigurable subrc : NbCollections.iterable(getChildrenOfRuntimeConfigurable(rc))) {
                structures.add(LoggerTrampoline.TASK_STRUCTURE_CREATOR.makeTaskStructure(new TaskStructureImpl(subrc)));
            }
            return structures.toArray(new TaskStructure[0]);
        }
        
    }

}
