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

package org.netbeans.modules.ant.debugger;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.AntEvent;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.SessionProvider;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
        
        
/*
 * AntTest.java
 *
 * Created on 19. leden 2004, 20:03
 */

/**
 *
 * @author  Honza
 */
@org.openide.util.lookup.ServiceProvider(service=org.apache.tools.ant.module.spi.AntLogger.class)
public class DebuggerAntLogger extends AntLogger {
    
    
    static DebuggerAntLogger getDefault () {
        Iterator<? extends AntLogger> it = Lookup.getDefault().lookupAll(AntLogger.class).iterator();
        while (it.hasNext ()) {
            AntLogger al = it.next();
            if (al instanceof DebuggerAntLogger) {
                return (DebuggerAntLogger) al;
            }
        }
        throw new InternalError ();
    }
    
    /**
     * Fired only if the build could not even be started.
     * {@link AntEvent#getException} will be non-null.
     * The default implementation does nothing.
     * @param event the associated event object
     */
    @Override
    public void buildInitializationFailed (AntEvent event) {
//        File script = event.getScriptLocation ();
//        int lineNumber = event.getLine ();
//
//        AntSession session = event.getSession ();
//        String message = event.getMessage ();
//        String target = event.getTargetName ();
//        String task = event.getTaskName ();
//        Set properties = event.getPropertyNames ();
//        Utils.markCurrent (event);
//        S ystem.out.println(event);
    }
    
    /**
     * Fired once when a build is started.
     * The default implementation does nothing.
     * @param event the associated event object
     */
    @Override
    public void buildStarted (AntEvent event) {
    }
    
    /**
     * Fired once when a build is finished.
     * The default implementation does nothing.
     * @param event the associated event object
     * @see AntEvent#getException
     */
    @Override
    public void buildFinished (AntEvent event) {
        AntDebugger d = getDebugger (event.getSession (), event);
        if (d == null) return;
        d.buildFinished (event);
        finishDebugging (d);
    }
    
    /**
     * Fired when a target is started.
     * It is <em>not</em> guaranteed that {@link AntEvent#getTargetName}
     * will be non-null (as can happen in some circumstances with
     * <code>&lt;import&gt;</code>, for example).
     * The default implementation does nothing.
     * @param event the associated event object
     */
    @Override
    public void targetStarted (AntEvent event) {
        AntDebugger d = getDebugger (event.getSession (), event);
        if (d == null) return;
        d.targetStarted (event);
    }
    
    /**
     * Fired when a target is finished.
     * It is <em>not</em> guaranteed that {@link AntEvent#getTargetName}
     * will be non-null.
     * The default implementation does nothing.
     * @param event the associated event object
     */
    @Override
    public void targetFinished (AntEvent event) {
        AntDebugger d = getDebugger (event.getSession (), event);
        if (d == null) return;
        d.targetFinished (event);
    }
    
    /**
     * Fired when a task is started.
     * It is <em>not</em> guaranteed that {@link AntEvent#getTaskName} or
     * {@link AntEvent#getTaskStructure} will be non-null, though they will
     * usually be defined.
     * {@link AntEvent#getTargetName} might also be null.
     * The default implementation does nothing.
     * @param event the associated event object
     */
    @Override
    public void taskStarted (AntEvent event) {
        AntDebugger d = getDebugger (event.getSession (), event);
        if (d == null) return;
        d.taskStarted (event);
    }
    
    /**
     * Fired when a task is finished.
     * It is <em>not</em> guaranteed that {@link AntEvent#getTaskName} or
     * {@link AntEvent#getTaskStructure} will be non-null.
     * {@link AntEvent#getTargetName} might also be null.
     * The default implementation does nothing.
     * @param event the associated event object
     */
    @Override
    public void taskFinished (AntEvent event) {
        AntDebugger d = getDebugger (event.getSession (), event);
        if (d == null) return;
        d.taskFinished (event);
    }

    /**
     * Fired when a message is logged.
     * The task and target fields may or may not be defined.
     * The default implementation does nothing.
     * @param event the associated event object
     */
    @Override
    public void messageLogged (AntEvent event) {
    }

    /**
     * Mark whether this logger is interested in a given Ant session.
     * @param session a session which is about to be start
     * @return true to receive events about it; by default, false
     */
    @Override
    public boolean interestedInSession (AntSession session) {
        return true;
    }
    
    /**
     * Mark whether this logger is interested in any Ant script.
     * If true, no events will be masked due to the script location.
     * Note that a few events have no defined script and so will only
     * be delivered to loggers interested in all scripts; typically this
     * applies to debugging messages when a project is just being configured.
     * @param session the relevant session
     * @return true to receive events for all scripts; by default, false
     */
    @Override
    public boolean interestedInAllScripts (AntSession session) {
        return true;
    }
    
    /**
     * Mark whether this logger is interested in a given Ant script.
     * Called only if {@link #interestedInAllScripts} is false.
     * Only events with a defined script according to {@link AntEvent#getScriptLocation}
     * which this logger is interested in will be delivered.
     * Note that a few events have no defined script and so will only
     * be delivered to loggers interested in all scripts; typically this
     * applies to debugging messages when a project is just being configured.
     * Note also that a single session can involve many different scripts.
     * @param script a particular build script
     * @param session the relevant session
     * @return true to receive events sent from this script; by default, false
     */
    @Override
    public boolean interestedInScript (File script, AntSession session) {
        return true;
    }

    /**
     * Mark which kinds of targets this logger is interested in.
     * This applies to both target start and finish events, as well as any other
     * events for which {@link AntEvent#getTargetName} is not null, such as task
     * start and finish events, and message log events.
     * If {@link #NO_TARGETS}, no events with specific targets will be sent to it.
     * If a specific list, only events with defined target names included in the list
     * will be sent to it.
     * If {@link #ALL_TARGETS}, all events not otherwise excluded will be sent to it.
     * @param session the relevant session
     * @return a nonempty (and non-null) list of target names; by default, {@link #NO_TARGETS}
     */
    @Override
    public String[] interestedInTargets (AntSession session) {
        return ALL_TARGETS;
    }
    
    /**
     * Mark which kinds of tasks this logger is interested in.
     * This applies to both task start and finish events, as well as any other
     * events for which {@link AntEvent#getTaskName} is not null, such as
     * message log events.
     * If {@link #NO_TASKS}, no events with specific tasks will be sent to it.
     * If a specific list, only events with defined task names included in the list
     * will be sent to it.
     * If {@link #ALL_TASKS}, all events not otherwise excluded will be sent to it.
     * @param session the relevant session
     * @return a nonempty (and non-null) list of task names; by default, {@link #NO_TASKS}
     */
    @Override
    public String[] interestedInTasks (AntSession session) {
        return ALL_TASKS;
    }
    
    /**
     * Mark which kinds of message log events this logger is interested in.
     * This applies only to message log events and no others.
     * Only events with log levels included in the returned list will be delivered.
     * @param session the relevant session
     * @return a list of levels such as {@link AntEvent#LOG_INFO}; by default, an empty list
     * @see AntSession#getVerbosity
     */
    @Override
    public int[] interestedInLogLevels (AntSession session) {
        return new int[] {
            AntEvent.LOG_INFO,
            AntEvent.LOG_DEBUG,
            AntEvent.LOG_ERR,
            AntEvent.LOG_VERBOSE,
            AntEvent.LOG_WARN
        };
    }
    
    /** AntSession => AntDebugger */
    private Map<AntSession, AntDebugger> runningDebuggers  = new HashMap<>();
    /** AntDebugger => AntSession */
    private Map<AntDebugger, AntSession> runningDebuggers2 = new HashMap<>();
    private Set<File> filesToDebug = new HashSet<>();
    /** File => WeakReference -> ExecutorTask */
    private Map<File, WeakReference<ExecutorTask>> fileExecutors = new HashMap<>();
    
    void debugFile (File f) {
        filesToDebug.add (f);
    }
    
    void fileExecutor(File f, ExecutorTask execTask) {
        fileExecutors.put(f, new WeakReference<>(execTask));
    }
    
    private void finishDebugging (
        AntDebugger debugger
    ) {
        AntSession session = runningDebuggers2.remove (debugger);
        runningDebuggers.remove (session);
    }
    
    private AntDebugger getDebugger (AntSession s, AntEvent antEvent) {
        AntDebugger d = runningDebuggers.get(s);
        if (d != null) return d;
        
        if (!filesToDebug.contains (s.getOriginatingScript ())) 
            return null;
        filesToDebug.remove (s.getOriginatingScript ());
        Reference execRef = (Reference) fileExecutors.remove(s.getOriginatingScript());
        ExecutorTask execTask = null;
        if (execRef != null) {
            execTask = (ExecutorTask) execRef.get();
        }
        
        // start debugging othervise
        FileObject fo = FileUtil.toFileObject (s.getOriginatingScript ());
        DataObject dob;
        try {
            dob = DataObject.find (fo);
        } catch (DataObjectNotFoundException ex) {
            Logger.getLogger(DebuggerAntLogger.class.getName()).log(Level.CONFIG, "No DataObject from "+fo, ex);
            return null;
        }
        AntProjectCookie antCookie = dob.getLookup().lookup(AntProjectCookie.class);
        if (antCookie == null)
            throw new NullPointerException ("No AntProjectCookie provided by "+dob);
        d = startDebugging (antCookie, antEvent, execTask);
        runningDebuggers.put (s, d);
        runningDebuggers2.put (d, s);
        return d;
    }

    private static AntDebugger startDebugging (
        final AntProjectCookie antCookie,
        final AntEvent         antEvent,
        final ExecutorTask     execTask
    ) {
        DebuggerInfo di = DebuggerInfo.create (
            "AntDebuggerInfo",
            new Object[] {
                new SessionProvider () {
                    @Override
                    public String getSessionName () {
                        return antEvent.getSession ().getDisplayName ();
                    }
                    
                    @Override
                    public String getLocationName () {
                        return "localhost";
                    }
                    
                    @Override
                    public String getTypeID () {
                        return "AntSession";
                    }

                    @Override
                    public Object[] getServices () {
                        return new Object[] {};
                    }
                },
                antCookie
            }
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (di);
        AntDebugger debugger = es[0].lookupFirst(null, AntDebugger.class);
        debugger.setExecutor(execTask);
        return debugger;
    }
}
