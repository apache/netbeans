/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.apache.tools.ant.module.spi;

import java.io.File;
import java.util.Set;
import org.apache.tools.ant.module.run.LoggerTrampoline;

/**
 * One event delivered to an {@link AntLogger}.
 * <p>
 * Note that one event is shared across all listeners.
 * </p>
 * <p>
 * The information available from the event represents a best effort to gather
 * information from the Ant run. Some versions of Ant may not support all of
 * these capabilities, in which case the event method will simply return null
 * or whatever the documented fallback value is. For example, Ant 1.5 does
 * not permit details of task structure to be introspected, but 1.6 does.
 * </p>
 * @author Jesse Glick
 * @since org.apache.tools.ant.module/3 3.12
 */
public final class AntEvent {

    static {
        LoggerTrampoline.ANT_EVENT_CREATOR = new LoggerTrampoline.Creator() {
            public AntSession makeAntSession(LoggerTrampoline.AntSessionImpl impl) {
                throw new AssertionError();
            }
            public AntEvent makeAntEvent(LoggerTrampoline.AntEventImpl impl) {
                return new AntEvent(impl);
            }
            public TaskStructure makeTaskStructure(LoggerTrampoline.TaskStructureImpl impl) {
                throw new AssertionError();
            }
        };
    }
    
    private final LoggerTrampoline.AntEventImpl impl;
    private AntEvent(LoggerTrampoline.AntEventImpl impl) {
        this.impl = impl;
    }
    
    /**
     * Error log level.
     */
    public static final int LOG_ERR = 0;
    
    /**
     * Warning log level.
     */
    public static final int LOG_WARN = 1;
    
    /**
     * Information log level.
     */
    public static final int LOG_INFO = 2;
    
    /**
     * Verbose log level.
     */
    public static final int LOG_VERBOSE = 3;
    
    /**
     * Debugging log level.
     */
    public static final int LOG_DEBUG = 4;
    
    /**
     * Get the associated session.
     * @return the session object
     */
    public AntSession getSession() {
        return impl.getSession();
    }
    
    /**
     * Mark an event as consumed to advise other loggers not to handle it.
     * @throws IllegalStateException if it was already consumed
     */
    public void consume() throws IllegalStateException {
        impl.consume();
    }
    
    /**
     * Test whether this event has already been consumed by some other logger.
     * @return true if it has already been consumed
     */
    public boolean isConsumed() {
        return impl.isConsumed();
    }
    
    /**
     * Get the location of the Ant script producing this event.
     * @return the script location, or null if unknown
     */
    public File getScriptLocation() {
        return impl.getScriptLocation();
    }
    
    /**
     * Get the line number in {@link #getScriptLocation} corresponding to this event.
     * Line numbers start at one.
     * @return the line number, or -1 if unknown
     */
    public int getLine() {
        return impl.getLine();
    }
    
    /**
     * Get the name of the target in {@link #getScriptLocation} producing this event.
     * Some events occur outside targets and so there will be no target name.
     * @return the target name (never empty), or null if unknown or inapplicable
     */
    public String getTargetName() {
        return impl.getTargetName();
    }
    
    /**
     * Get the name of the task producing this event.
     * XXX semantics w.r.t. namespaces, taskdefs, etc.?
     * Some events occur outside of tasks and so there will be no name.
     * @return the task name (never empty), or null if unknown or inapplicable
     */
    public String getTaskName() {
        return impl.getTaskName();
    }
    
    /**
     * Get the configured XML structure of the task producing this event.
     * Some events occur outside of tasks and so there will be no information.
     * @return the task structure, or null if unknown or inapplicable
     */
    public TaskStructure getTaskStructure() {
        return impl.getTaskStructure();
    }
    
    /**
     * Get the name of the message being logged.
     * Applies only to {@link AntLogger#messageLogged}.
     * @return the message, or null if inapplicable
     */
    public String getMessage() {
        return impl.getMessage();
    }
    
    /**
     * Get the log level of the message.
     * Applies only to {@link AntLogger#messageLogged}.
     * Note that lower numbers are higher priority.
     * @return the log level (e.g. LOG_INFO), or -1 if inapplicable
     */
    public int getLogLevel() {
        return impl.getLogLevel();
    }
    
    /**
     * Get a terminating exception.
     * Applies only to {@link AntLogger#buildFinished}
     * and {@link AntLogger#buildInitializationFailed}.
     * @return an exception ending the build, or null for normal completion or if inapplicable
     */
    public Throwable getException() {
        return impl.getException();
    }
    
    /**
     * Get a property set on the current Ant project.
     * Also can retrieve references using their string values since org.apache.tools.ant.module/3 3.29.
     * @param name the property name
     * @return its value, or null
     */
    public String getProperty(String name) {
        return impl.getProperty(name);
    }
    
    /**
     * Get a set of property names defined on the current Ant project.
     * Also includes reference names since org.apache.tools.ant.module/3 3.29.
     * @return a set of property names; may be empty but not null
     */
    public Set<String> getPropertyNames() {
        return impl.getPropertyNames();
    }
    
    /**
     * Evaluate a string with possible substitutions according to defined properties.
     * @param text the text to evaluate
     * @return its value (may be the same as the incoming text), never null
     * @see TaskStructure#getAttribute
     * @see TaskStructure#getText
     */
    public String evaluate(String text) {
        return impl.evaluate(text);
    }
    
    @Override
    public String toString() {
        return impl.toString();
    }
    
}
