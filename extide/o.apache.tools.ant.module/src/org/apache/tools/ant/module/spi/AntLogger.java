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

package org.apache.tools.ant.module.spi;

import java.io.File;

/**
 * A pluggable logger that can listen to {@link AntEvent}s during
 * one or more {@link AntSession}s.
 * <p>
 * There can be several loggers active on a given session,
 * so {@link AntEvent#consume} may be used to cooperate.
 * Implementations may be registered to default {@link org.openide.util.Lookup}.
 * Loggers are notified of events in the order of their registration in lookup.
 * </p>
 * <p>
 * A logger will always first be asked if it is interested in a given session;
 * if it declines, it will receive no further events for that session.
 * Otherwise it will normally receive {@link #buildStarted}; then some combination of
 * target, task, and message logged events; then {@link #buildFinished}.
 * (Or it may receive just {@link #buildInitializationFailed}.)
 * A logger may <em>not</em> assume that target and task events are properly
 * nested in any way, due to Ant's <code>&lt;parallel&gt;</code> task and
 * other complexities such as <code>&lt;import&gt;</code> handling. Events may
 * also be delivered from the originating script or any subscripts, again with
 * no guaranteed nesting behavior. A logger may not assume that it will not
 * receive any events after {@link #buildFinished}.
 * </p>
 * <p>
 * Various mask methods permit loggers to declare themselves uninterested in
 * some kinds of events. Such events will not be delivered to them. Loggers should
 * declare themselves interested only in events they will actually use in some way,
 * to permit the Ant engine to minimize the number of events delivered. Note that
 * loggers which do not declare themselves interested in the given session will
 * not receive {@link #buildStarted}, {@link #buildFinished}, or
 * {@link #buildInitializationFailed} at all, and loggers not additionally interested
 * in all scripts will not receive {@link #buildInitializationFailed}.
 * </p>
 * <p>
 * A logger should not keep any state as a rule; this would be a memory leak, and
 * also a logger may be called from multiple threads with different sessions.
 * Use {@link AntSession#getCustomData} and {@link AntSession#putCustomData} instead.
 * Loggers may not make calls to the session, event, or task structure objects outside
 * the dynamic scope of an event callback.
 * </p>
 * <p>
 * This is an abstract class so that new event types or masks may be added in the future.
 * To prevent possible conflicts, implementors are forbidden to define other methods
 * which take a single parameter of type {@link AntEvent} or which have a name beginning
 * with the string <code>interested</code>.
 * </p>
 * <div class="nonnormative">
 * <p>
 * The Ant module registers one logger at position 100 in META-INF/services lookup
 * which may or may not handle any events which have not already been consumed
 * (marking them consumed itself) and will typically process message logged events
 * by printing them to the output somehow, using hyperlinks for common file error
 * patterns such as <code>/path/to/File.java:34: some message</code>. It may also
 * handle sequences of messages logged within a task in the format
 * </p>
 * <pre>&nbsp;/path/to/File.java:34: you cannot throw a bogus exception here
 * &nbsp;        throw new Exception("bogus!");
 * &nbsp;                            ^</pre>
 * <p>
 * by linking to the column number indicated by the caret (<code>^</code>).
 * </p>
 * <p>
 * Please Note: Using <code>System.out</code> or <code>System.err</code> in your subclass 
 * will add the messages to the build's output, not IDE log file. The behaviour is not part of the
 * API contract though and can be changed in the future.
 * </p>
 * </div>
 * @author Jesse Glick
 * @see <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=42525">Issue #42525</a>
 * @since org.apache.tools.ant.module/3 3.12
 */
public abstract class AntLogger {
    
    /** No-op constructor for implementors. */
    protected AntLogger() {}
    
    /**
     * Special constant indicating the logger is not interested in receiving
     * any target events.
     * @see #interestedInTargets
     */
    public static final String[] NO_TARGETS = {};
    
    /**
     * Special constant indicating the logger is interested in receiving
     * all target events.
     * @see #interestedInTargets
     */
    public static final String[] ALL_TARGETS = {};
    
    /**
     * Special constant indicating the logger is not interested in receiving
     * any task events.
     * @see #interestedInTasks
     */
    public static final String[] NO_TASKS = {};
    
    /**
     * Special constant indicating the logger is interested in receiving
     * all task events.
     * @see #interestedInTasks
     */
    public static final String[] ALL_TASKS = {};
    
    /**
     * Fired only if the build could not even be started.
     * {@link AntEvent#getException} will be non-null.
     * The default implementation does nothing.
     * @param event the associated event object
     */
    public void buildInitializationFailed(AntEvent event) {}
    
    /**
     * Fired once when a build is started.
     * The default implementation does nothing.
     * @param event the associated event object
     */
    public void buildStarted(AntEvent event) {}
    
    /**
     * Fired once when a build is finished.
     * The default implementation does nothing.
     * @param event the associated event object
     * @see AntEvent#getException
     */
    public void buildFinished(AntEvent event) {}
    
    /**
     * Fired when a target is started.
     * It is <em>not</em> guaranteed that {@link AntEvent#getTargetName}
     * will be non-null (as can happen in some circumstances with
     * <code>&lt;import&gt;</code>, for example).
     * The default implementation does nothing.
     * @param event the associated event object
     */
    public void targetStarted(AntEvent event) {}
    
    /**
     * Fired when a target is finished.
     * It is <em>not</em> guaranteed that {@link AntEvent#getTargetName}
     * will be non-null.
     * The default implementation does nothing.
     * @param event the associated event object
     */
    public void targetFinished(AntEvent event) {}
    
    /**
     * Fired when a task is started.
     * It is <em>not</em> guaranteed that {@link AntEvent#getTaskName} or
     * {@link AntEvent#getTaskStructure} will be non-null, though they will
     * usually be defined.
     * {@link AntEvent#getTargetName} might also be null.
     * The default implementation does nothing.
     * @param event the associated event object
     */
    public void taskStarted(AntEvent event) {}
    
    /**
     * Fired when a task is finished.
     * It is <em>not</em> guaranteed that {@link AntEvent#getTaskName} or
     * {@link AntEvent#getTaskStructure} will be non-null.
     * {@link AntEvent#getTargetName} might also be null.
     * The default implementation does nothing.
     * @param event the associated event object
     */
    public void taskFinished(AntEvent event) {}

    /**
     * Fired when a message is logged.
     * The task and target fields may or may not be defined.
     * The default implementation does nothing.
     * @param event the associated event object
     */
    public void messageLogged(AntEvent event) {}

    /**
     * Mark whether this logger is interested in a given Ant session.
     * @param session a session which is about to be start
     * @return true to receive events about it; by default, false
     */
    public boolean interestedInSession(AntSession session) {
        return false;
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
    public boolean interestedInAllScripts(AntSession session) {
        return false;
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
    public boolean interestedInScript(File script, AntSession session) {
        return false;
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
    public String[] interestedInTargets(AntSession session) {
        return NO_TARGETS;
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
    public String[] interestedInTasks(AntSession session) {
        return NO_TASKS;
    }
    
    /**
     * Mark which kinds of message log events this logger is interested in.
     * This applies only to message log events and no others.
     * Only events with log levels included in the returned list will be delivered.
     * @param session the relevant session
     * @return a list of levels such as {@link AntEvent#LOG_INFO}; by default, an empty list
     * @see AntSession#getVerbosity
     */
    public int[] interestedInLogLevels(AntSession session) {
        return new int[0];
    }
    
}
