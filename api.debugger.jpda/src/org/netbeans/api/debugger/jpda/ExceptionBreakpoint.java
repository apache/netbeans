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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * Notifies about exceptions throw in debugged JVM.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint (ExceptionBreakpoint.create (
 *        "java.lang.NullPointerException",
 *        ExceptionBreakpoint.TYPE_EXCEPTION_UNCATCHED
 *    ));</pre>
 * This breakpoint stops when NullPointerException is throw and uncatched.
 *
 * @author Jan Jancura
 */
public class ExceptionBreakpoint extends JPDABreakpoint {

    /** Property name constant */
    public static final String          PROP_EXCEPTION_CLASS_NAME = "exceptionClassName"; // NOI18N
    /** Property name constant */
    public static final String          PROP_CLASS_FILTERS = "classFilters"; // NOI18N
    /** Property name constant */
    public static final String          PROP_CLASS_EXCLUSION_FILTERS = "classExclusionFilters"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_CATCH_TYPE = "catchType"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_CONDITION = "condition"; // NOI18N

    /** Catch type constant. <i>[sic]</i> "caught" */
    public static final int             TYPE_EXCEPTION_CATCHED = 1;
    /** Catch type constant. <i>[sic]</i> "uncaught" */
    public static final int             TYPE_EXCEPTION_UNCATCHED = 2;
    /** Catch type constant. <i>[sic]</i> "caught/uncaught" */
    public static final int             TYPE_EXCEPTION_CATCHED_UNCATCHED = 3;

    private String                      exceptionClassName = "";
    private String[]                    classFilters = new String [0];
    private String[]                    classExclusionFilters = new String [0];
    private int                         catchType = TYPE_EXCEPTION_UNCATCHED;
    private String                      condition = ""; // NOI18N

    
    private ExceptionBreakpoint () {
    }
    
    /**
     * Creates a new breakpoint for given parameters.
     *
     * @param exceptionClassName class name filter
     * @param catchType one of constants: TYPE_EXCEPTION_CATCHED, 
     *   TYPE_EXCEPTION_UNCATCHED, TYPE_EXCEPTION_CATCHED_UNCATCHED
     * @return a new breakpoint for given parameters
     */
    public static ExceptionBreakpoint create (
        String exceptionClassName,
        int catchType
    ) {
        ExceptionBreakpoint b = new ExceptionBreakpointImpl ();
        b.setExceptionClassName (exceptionClassName);
        b.setCatchType (catchType);
        return b;
    }
    
    /**
     * Get name of exception class to stop on.
     *
     * @return name of exception class to stop on
     */
    public String getExceptionClassName () {
        return exceptionClassName;
    }

    /**
     * Set name of exception class to stop on.
     *
     * @param cn a new name of exception class to stop on.
     */
    public void setExceptionClassName (String cn) {
        if (cn != null) {
            cn = cn.trim();
        }
        if ( (cn == exceptionClassName) ||
             ((cn != null) && (exceptionClassName != null) && exceptionClassName.equals (cn))
        ) return;
        Object old = exceptionClassName;
        exceptionClassName = cn;
        firePropertyChange (PROP_EXCEPTION_CLASS_NAME, old, exceptionClassName);
    }
    
    /**
     * Get list of class filters to stop on.
     *
     * @return list of class filters to stop on
     */
    public String[] getClassFilters () {
        return classFilters;
    }

    /**
     * Set list of class filters to stop on.
     *
     * @param classFilters a new value of class filters property
     */
    public void setClassFilters (String[] classFilters) {
        if (classFilters == this.classFilters) return;
        Object old = this.classFilters;
        this.classFilters = classFilters;
        firePropertyChange (PROP_CLASS_FILTERS, old, classFilters);
    }

    /**
     * Get list of class exclusion filters to stop on.
     *
     * @return list of class exclusion filters to stop on
     */
    public String[] getClassExclusionFilters () {
        return classExclusionFilters;
    }

    /**
     * Set list of class exclusion filters to stop on.
     *
     * @param classExclusionFilters a new value of class exclusion filters property
     */
    public void setClassExclusionFilters (String[] classExclusionFilters) {
        if (classExclusionFilters == this.classExclusionFilters) return;
        Object old = this.classExclusionFilters;
        this.classExclusionFilters = classExclusionFilters;
        firePropertyChange (PROP_CLASS_EXCLUSION_FILTERS, old, classExclusionFilters);
    }
    
    /**
     * Returns condition.
     *
     * @return cond a condition
     */
    public String getCondition () {
        return condition;
    }

    /**
     * Sets condition.
     *
     * @param cond a c new condition
     */
    public void setCondition (String cond) {
        if (cond != null) {
            cond = cond.trim();
        }
        String old = condition;
        condition = cond;
        firePropertyChange (PROP_CONDITION, old, cond);
    }

    /**
     * Returns breakpoint type property value.
     *
     * @return breakpoint type property value.
     */
    public int getCatchType () {
        return catchType;
    }

    /**
     * Sets breakpoint type property value.
     *
     * @param catchType a new value of breakpoint type property value
     */
    public void setCatchType (int catchType) {
        if (catchType == this.catchType) return;
        if ( (catchType & (TYPE_EXCEPTION_CATCHED | TYPE_EXCEPTION_UNCATCHED)) == 0
           ) throw new IllegalArgumentException  ();
        int old = this.catchType;
        this.catchType = catchType;
        firePropertyChange (
            PROP_CATCH_TYPE, 
            Integer.valueOf(old),
            Integer.valueOf(catchType)
        );
    }

    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString () {
        return "ExceptionBreakpoint" + exceptionClassName;
    }

    private static final class ExceptionBreakpointImpl extends ExceptionBreakpoint implements PropertyChangeListener {

        @Override
        public GroupProperties getGroupProperties() {
            return new ExceptionGroupProperties();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            enginePropertyChange(evt);
        }

        private final class ExceptionGroupProperties extends GroupProperties {

            @Override
            public String getType() {
                return NbBundle.getMessage(ExceptionBreakpoint.class, "ExceptionBrkp_Type");
            }

            @Override
            public String getLanguage() {
                return "Java";
            }

            @Override
            public FileObject[] getFiles() {
                String[] filters = getClassFilters();
                String[] exfilters = getClassExclusionFilters();
                List<FileObject> files = new ArrayList<FileObject>();
                for (int i = 0; i < filters.length; i++) {
                    // TODO: annotate also other matched classes
                    if (!filters[i].startsWith("*") && !filters[i].endsWith("*")) {
                        fillFilesForClass(filters[i], files);
                    }
                }
                return files.toArray(new FileObject[] {});
            }

            @Override
            public Project[] getProjects() {
                FileObject[] files = getFiles();
                List<Project> projects = new ArrayList<Project>();
                for (FileObject f : files) {
                    while (f != null) {
                        f = f.getParent();
                        if (f != null && ProjectManager.getDefault().isProject(f)) {
                            break;
                        }
                    }
                    if (f != null) {
                        try {
                            projects.add(ProjectManager.getDefault().findProject(f));
                        } catch (IOException ex) {
                        } catch (IllegalArgumentException ex) {
                        }
                    }
                }
                return projects.toArray(new Project[] {});
            }

            @Override
            public DebuggerEngine[] getEngines() {
                return ExceptionBreakpointImpl.this.getEngines();
            }

            @Override
            public boolean isHidden() {
                return ExceptionBreakpointImpl.this.isHidden();
            }

        }
    }
    
}
