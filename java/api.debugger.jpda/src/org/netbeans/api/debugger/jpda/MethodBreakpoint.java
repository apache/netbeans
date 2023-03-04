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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Notifies about method entry events.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint (MethodBreakpoint.create (
 *        "examples.texteditor.Ted*",
 *        "<init>
 *    ));</pre>
 * This breakpoint stops when some initializer of class Ted or innercalsses is
 * called.
 *
 * @author Jan Jancura
 */
public class MethodBreakpoint extends JPDABreakpoint {

    /** Property name constant */
    public static final String          PROP_METHOD_NAME = "methodName"; // NOI18N
    /** Property name constant */
    public static final String          PROP_METHOD_SIGNATURE = "signature"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_BREAKPOINT_TYPE = "breakpointtType"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_CONDITION = "condition"; // NOI18N
    /** Property name constant */
    public static final String          PROP_CLASS_FILTERS = "classFilters"; // NOI18N
    /** Property name constant */
    public static final String          PROP_CLASS_EXCLUSION_FILTERS = "classExclusionFilters"; // NOI18N
    /** Property name constant */
    public static final String          PROP_INSTANCE_FILTERS = "instanceFilters"; // NOI18N
    /** Property name constant */
    public static final String          PROP_THREAD_FILTERS = "threadFilters"; // NOI18N

    /** Breakpoint type property value constant. */
    public static final int             TYPE_METHOD_ENTRY = 1;
    /** Breakpoint type property value constant. */
    public static final int             TYPE_METHOD_EXIT = 2;

    /** Property variable. */
    private String[]                    classFilters = new String [0];
    private String[]                    classExclusionFilters = new String [0];
    private String                      methodName = "";
    private String                      methodSignature;
    private int                         breakpointType = TYPE_METHOD_ENTRY;
    private String                      condition = "";
    private Map<JPDADebugger,ObjectVariable[]> instanceFilters;
    private Map<JPDADebugger,JPDAThread[]> threadFilters;
    
    
    private MethodBreakpoint () {
    }
    
    /**
     * Creates a new method breakpoint for given parameters.
     *
     * @param className a class name filter
     * @param methodName a name of method
     * @return a new breakpoint for given parameters
     */
    public static MethodBreakpoint create (
        String className,
        String methodName
    ) {
        MethodBreakpoint b = new MethodBreakpointImpl ();
        b.setClassFilters (new String[] {className});
        b.setMethodName (methodName);
        return b;
    }
    
    /**
     * Creates a new method breakpoint.
     *
     * @return a new method breakpoint
     */
    public static MethodBreakpoint create (
    ) {
        MethodBreakpoint b = new MethodBreakpointImpl ();
        return b;
    }

    /**
     * Get name of method to stop on.
     *
     * @return name of method to stop on
     */
    public String getMethodName () {
        return methodName;
    }

    /**
     * Set name of method to stop on.
     *
     * @param mn a name of method to stop on
     */
    public void setMethodName (String mn) {
        if (mn != null) {
            mn = mn.trim();
        }
        if (Objects.equals(mn, methodName)) return;
        String old = methodName;
        methodName = mn;
        firePropertyChange (PROP_METHOD_NAME, old, mn);
    }
    
    /**
     * Get the JNI-style signature of the method to stop on.
     *
     * @return JNI-style signature of the method to stop on
     * @see com.sun.jdi.TypeComponent#signature
     */
    public String getMethodSignature () {
        return methodSignature;
    }

    /**
     * Set JNI-style signature of the method to stop on.
     *
     * @param signature the JNI-style signature of the method to stop on
     * @see com.sun.jdi.TypeComponent#signature
     */
    public void setMethodSignature (String signature) {
        if (signature != null) {
            signature = signature.trim();
        }
        if (Objects.equals(signature, methodSignature)) {
            return;
        }
        String old = methodSignature;
        methodSignature = signature;
        firePropertyChange (PROP_METHOD_SIGNATURE, old, signature);
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
     * Returns type of this breakpoint.
     *
     * @return type of this breakpoint
     */
    public int getBreakpointType () {
        return breakpointType;
    }

    /**
     * Sets type of this breakpoint (TYPE_METHOD_ENTRY or TYPE_METHOD_EXIT).
     *
     * @param breakpointType a new value of breakpoint type property
     */
    public void setBreakpointType (int breakpointType) {
        if (breakpointType == this.breakpointType) return;
        if ((breakpointType & (TYPE_METHOD_ENTRY | TYPE_METHOD_EXIT)) == 0)
            throw new IllegalArgumentException  ();
        int old = this.breakpointType;
        this.breakpointType = breakpointType;
        firePropertyChange (PROP_BREAKPOINT_TYPE, Integer.valueOf(old), Integer.valueOf(breakpointType));
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
     * Get the instance filter for a specific debugger session.
     * @return The instances or <code>null</code> when there is no instance restriction.
     */
    public ObjectVariable[] getInstanceFilters(JPDADebugger session) {
        if (instanceFilters != null) {
            return instanceFilters.get(session);
        } else {
            return null;
        }
    }
    
    /**
     * Set the instance filter for a specific debugger session. This restricts
     * the breakpoint to specific instances in that session.
     * @param session the debugger session
     * @param instances the object instances or <code>null</code> to unset the filter.
     */
    public void setInstanceFilters(JPDADebugger session, ObjectVariable[] instances) {
        if (instanceFilters == null) {
            instanceFilters = new WeakHashMap<JPDADebugger, ObjectVariable[]>();
        }
        if (instances != null) {
            instanceFilters.put(session, instances);
        } else {
            instanceFilters.remove(session);
        }
        firePropertyChange(PROP_INSTANCE_FILTERS, null,
                instances != null ?
                    new Object[] { session, instances } : null);
    }

    /**
     * Get the thread filter for a specific debugger session.
     * @return The thread or <code>null</code> when there is no thread restriction.
     */
    public JPDAThread[] getThreadFilters(JPDADebugger session) {
        if (threadFilters != null) {
            return threadFilters.get(session);
        } else {
            return null;
        }
    }
    
    /**
     * Set the thread filter for a specific debugger session. This restricts
     * the breakpoint to specific threads in that session.
     * @param session the debugger session
     * @param threads the threads or <code>null</code> to unset the filter.
     */
    public void setThreadFilters(JPDADebugger session, JPDAThread[] threads) {
        if (threadFilters == null) {
            threadFilters = new WeakHashMap<JPDADebugger, JPDAThread[]>();
        }
        if (threads != null) {
            threadFilters.put(session, threads);
        } else {
            threadFilters.remove(session);
        }
        firePropertyChange(PROP_THREAD_FILTERS, null,
                threads != null ?
                    new Object[] { session, threads } : null);
    }

    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString () {
        return "MethodBreakpoint " + java.util.Arrays.asList(classFilters).toString() + "." + methodName +
                ((methodSignature != null) ? " '"+methodSignature+"'" : "");
    }
    
    
    private static final class MethodBreakpointImpl extends MethodBreakpoint implements ChangeListener,
                                                                                        PropertyChangeListener {
        
        @Override
        public GroupProperties getGroupProperties() {
            return new MethodGroupProperties();
        }

        public void stateChanged(ChangeEvent chev) {
            Object source = chev.getSource();
            if (source instanceof Breakpoint.VALIDITY) {
                setValidity((Breakpoint.VALIDITY) source, chev.toString());
            } else {
                throw new UnsupportedOperationException(chev.toString());
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            enginePropertyChange(evt);
        }

        
        private final class MethodGroupProperties extends GroupProperties {

            @Override
            public String getType() {
                return NbBundle.getMessage(MethodBreakpoint.class, "MethodBrkp_Type");
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
                return MethodBreakpointImpl.this.getEngines();
            }

            @Override
            public boolean isHidden() {
                return MethodBreakpointImpl.this.isHidden();
            }

        }

    }
}
