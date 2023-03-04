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

package org.apache.tools.ant.module.spi;

import java.io.File;
import java.net.URL;
import java.util.Map;
import org.apache.tools.ant.module.run.LoggerTrampoline;
import org.openide.windows.IOColors;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;

/**
 * Represents one Ant build session, possibly consisting of multiple targets,
 * subprojects, and so on.
 * A session may be shared by several {@link AntLogger}s.
 * @author Jesse Glick
 * @since org.apache.tools.ant.module/3 3.12
 */
public final class AntSession {

    static {
        LoggerTrampoline.ANT_SESSION_CREATOR = new LoggerTrampoline.Creator() {
            public AntSession makeAntSession(LoggerTrampoline.AntSessionImpl impl) {
                return new AntSession(impl);
            }
            public AntEvent makeAntEvent(LoggerTrampoline.AntEventImpl impl) {
                throw new AssertionError();
            }
            public TaskStructure makeTaskStructure(LoggerTrampoline.TaskStructureImpl impl) {
                throw new AssertionError();
            }
        };
    }
    
    private final LoggerTrampoline.AntSessionImpl impl;
    private AntSession(LoggerTrampoline.AntSessionImpl impl) {
        this.impl = impl;
    }
    
    /**
     * Get the Ant script originally invoked.
     * Note that due to subproject support some events may come from other scripts.
     * @return the Ant script which was run to start with
     */
    public File getOriginatingScript() {
        return impl.getOriginatingScript();
    }
    
    /**
     * Get the Ant targets originally run.
     * @return a list of one or more targets (but may be empty during {@link AntLogger#buildInitializationFailed})
     */
    public String[] getOriginatingTargets() {
        return impl.getOriginatingTargets();
    }
    
    /**
     * Get optional data stored by the logger in this session.
     * @param logger the logger which wishes to retrieve data
     * @return any optional data, or null initially
     */
    public Object getCustomData(AntLogger logger) {
        return impl.getCustomData(logger);
    }
    
    /**
     * Store custom data associated with this session.
     * May be used by the logger to keep some information that will persist
     * for the lifetime of the session.
     * @param logger the logger which wishes to store data
     * @param data some custom data to retain
     */
    public void putCustomData(AntLogger logger, Object data) {
        impl.putCustomData(logger, data);
    }
    
    /**
     * Print a line of text to the Ant output.
     * @param message a message to print (newline will be appended automatically)
     * @param err true to send to the error stream, false for regular output
     * @param listener an output listener suitable for hyperlinks, or null for a plain print
     * @see #createStandardHyperlink
     */
    public void println(String message, boolean err, OutputListener listener) {
        impl.println(message, err, listener);
    }
    
    /**
     * Deliver a message logged event to all matching loggers.
     * <p>
     * Loggers will receive {@link AntLogger#messageLogged} with an event
     * similar to the original event except for the message and log level;
     * also the exception will always be null and the event will initially
     * be unconsumed.
     * </p>
     * <p>
     * This call blocks until all loggers have processed the nested event.
     * Note that this logger may also receive the event so it must be reentrant.
     * </p>
     * <p class="nonnormative">
     * Loggers are discouraged from using this facility merely to create hyperlinks
     * for which the target is known. Use {@link #println} instead. This method
     * is primarily intended for use from the standard logger to deliver stack
     * trace lines to other loggers which may be able to hyperlink them.
     * </p>
     * @param originalEvent the original event received by the calling logger
     * @param message a message to log (see {@link AntEvent#getMessage})
     * @param level the level to log it at (see {@link AntEvent#getLogLevel})
     */
    public void deliverMessageLogged(AntEvent originalEvent, String message, int level) {
        impl.deliverMessageLogged(originalEvent, message, level);
    }
    
    /**
     * Marks an exception as having been processed by a logger.
     * <p>
     * A single build-halting exception can traverse any number of Ant events
     * as it progresses outwards, typically from the failing task to the failing
     * target (possibly several times due to subprojects) and finally to the build failure.
     * </p>
     * <p class="nonnormative">
     * Consuming the exception permits a logger to indicate to other loggers that it
     * has already handled the problem in an appropriate manner. Since the standard
     * logger may print an exception (possibly with stack trace) that is part of
     * a {@link AntLogger#buildFinished} event, loggers which deal with the exception
     * in some other way should consume it before returning from the callback.
     * </p>
     * @param t an exception to mark as consumed
     * @throws IllegalStateException if it was already consumed
     */
    public void consumeException(Throwable t) throws IllegalStateException {
        impl.consumeException(t);
    }
    
    /**
     * Tests whether a given exception has already been consumed by some logger.
     * <p>
     * Note that if an exception is consumed, any exception with that exception
     * as its {@link Throwable#getCause} (possibly recursively) is also considered
     * consumed. This is useful because Ant's <code>ProjectHelper.addLocationToBuildException</code>
     * will annotate <code>BuildException</code>s with location information by constructing
     * wrapper exceptions.
     * </p>
     * @param t an exception
     * @return true if it (or a nested exception) has already been consumed by {@link #consumeException}
     */
    public boolean isExceptionConsumed(Throwable t) {
        return impl.isExceptionConsumed(t);
    }

    /**
     * Get the (user-requested) verbosity level for this session.
     * Generally only messages logged at this or lesser level (higher priority) should be displayed.
     * @return the verbosity, e.g. {@link AntEvent#LOG_INFO}
     */
    public int getVerbosity() {
        return impl.getVerbosity();
    }

    /**
     * Gets a set of user properties defined for this session.
     * These might be a mixture of global default definitions and per-session properties.
     * @return an immutable property map
     * @since 3.52
     */
    public Map<String,String> getProperties() {
        return impl.getProperties();
    }

    /**
     * Checks if the property is concealed.
     * The value of the concealed property is not visible to the user.
     * @param propertyName the property name to check
     * @return true if property should not be visible to user
     * @since 3.71
     */
    public boolean isConcealed(String propertyName) {
        return impl.isConcealed(propertyName);
    }
    
    /**
     * Get a display name used for the session as a whole.
     * @return a user-presentable display name appropriate for session-scope messaging
     */
    public String getDisplayName() {
        return impl.getDisplayName();
    }
    
    /**
     * Convenience method to create a standard hyperlink implementation.
     * The GUI of the hyperlink will be oriented toward error messages and
     * may involve editor annotations.
     * Line and column numbers start at 1.
     * @param file a file to link to (may or may not exist, but hyperlink will not work if it does not)
     * @param message a message to use e.g. for the status bar when clicking on the hyperlink,
     *                or for annotation tool tips
     * @param line1 the starting line number, or -1 if there is no associated line number
     * @param column1 the starting column number, or -1 if there is no associated column number
     *                (must be -1 if line1 is -1)
     * @param line2 the ending line number, or -1 for a single-line link
     *              (must be -1 if line1 is -1)
     * @param column2 the ending column number, or -1 if not applicable
     *                (must be -1 if either line2 or column1 is -1)
     * @return a standard hyperlink suitable for {@link #println}
     */
    public OutputListener createStandardHyperlink(URL file, String message, int line1, int column1, int line2, int column2) {
        return impl.createStandardHyperlink(file, message, line1, column1, line2, column2);
    }

    /**
     * Obtains the I/O handle for advanced operations such as {@link IOColors}.
     * @return an I/O handle
     * @since org.apache.tools.ant.module/3 3.35
     */
    public InputOutput getIO() {
        return impl.getIO();
    }
    
    @Override
    public String toString() {
        return impl.toString();
    }
    
}
