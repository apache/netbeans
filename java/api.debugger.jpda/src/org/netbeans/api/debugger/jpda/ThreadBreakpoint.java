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

package org.netbeans.api.debugger.jpda;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Notifies about thread started and dead events.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint (ThreadBreakpoint.create (
 *    ));</pre>
 * This breakpoint stops when some thread is created or killed.
 *
 * @author Jan Jancura
 */
public class ThreadBreakpoint extends JPDABreakpoint {

    /** Property name constant. */
    public static final String          PROP_BREAKPOINT_TYPE = "breakpointtType"; // NOI18N

    /** Catch type property value constant. */
    public static final int             TYPE_THREAD_STARTED = 1;
    /** Catch type property value constant. */
    public static final int             TYPE_THREAD_DEATH = 2;
    /** Catch type property value constant. */
    public static final int             TYPE_THREAD_STARTED_OR_DEATH = 3;
    
    /** Property variable. */
    private int                         breakpointType = TYPE_THREAD_STARTED;

    
    private ThreadBreakpoint () {
    }
    
    /**
     * Creates a new breakpoint for given parameters.
     *
     * @return a new breakpoint for given parameters
     */
    public static ThreadBreakpoint create () {
        return new ThreadBreakpointImpl ();
    }

    /**
     * Returns type of this breakpoint.
     *
     * @return type of this breakpoint
     */
    public int getBreakpointType () {
        return breakpointType;
    }

    /**
     * Sets type of this breakpoint (TYPE_THREAD_STARTED or TYPE_THREAD_DEATH).
     *
     * @param breakpointType a new value of breakpoint type property
     */
    public void setBreakpointType (int breakpointType) {
        if (breakpointType == this.breakpointType) return;
        if ((breakpointType & (TYPE_THREAD_STARTED | TYPE_THREAD_DEATH)) == 0)
            throw new IllegalArgumentException  ();
        int old = this.breakpointType;
        this.breakpointType = breakpointType;
        firePropertyChange (PROP_BREAKPOINT_TYPE, Integer.valueOf(old), Integer.valueOf(breakpointType));
    }

    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString () {
        return "ThreadBreakpoint " + breakpointType;
    }

    private static final class ThreadBreakpointImpl extends ThreadBreakpoint implements PropertyChangeListener {

        @Override
        public GroupProperties getGroupProperties() {
            return new ThreadGroupProperties();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            enginePropertyChange(evt);
        }


        private final class ThreadGroupProperties extends GroupProperties {

            @Override
            public String getType() {
                return NbBundle.getMessage(ThreadBreakpoint.class, "ThreadBrkp_Type");
            }

            @Override
            public String getLanguage() {
                return "Java";
            }

            @Override
            public FileObject[] getFiles() {
                return null;
            }

            @Override
            public Project[] getProjects() {
                return null;
            }

            @Override
            public DebuggerEngine[] getEngines() {
                return ThreadBreakpointImpl.this.getEngines();
            }

            @Override
            public boolean isHidden() {
                return ThreadBreakpointImpl.this.isHidden();
            }

        }
    }

}
