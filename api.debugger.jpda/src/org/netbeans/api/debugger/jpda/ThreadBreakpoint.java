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
