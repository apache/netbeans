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
 * Notifies about variable change or access events.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint (FieldBreakpoint.create (
 *        "org.netbeans.modules.editor.EditorPanel",
 *        "state",
 *        FieldBreakpoint.TYPE_MODIFICATION
 *    ));</pre>
 * This breakpoint stops when state field of EditorPanel class is modified.
 *
 * @author Jan Jancura
 */
public class FieldBreakpoint extends JPDABreakpoint {

    /** Property name constant. */
    public static final String      PROP_FIELD_NAME = "fieldName"; // NOI18N
    /** Property name constant. */
    public static final String      PROP_CLASS_NAME = "className"; // NOI18N
    /** Property name constant. */
    public static final String      PROP_CONDITION = "condition"; // NOI18N
    /** Property name constant. */
    public static final String      PROP_BREAKPOINT_TYPE = "breakpointType"; // NOI18N
    /** Property name constant */
    public static final String      PROP_INSTANCE_FILTERS = "instanceFilters"; // NOI18N
    /** Property name constant */
    public static final String      PROP_THREAD_FILTERS = "threadFilters"; // NOI18N
    
    /** Property type value constant. */
    public static final int         TYPE_ACCESS = 1;
    /** Property type value constant. */
    public static final int         TYPE_MODIFICATION = 2;

    private String                  className = "";
    private String                  fieldName = "";
    private int                     type = TYPE_MODIFICATION;
    private String                  condition = ""; // NOI18N
    private Map<JPDADebugger,ObjectVariable[]> instanceFilters;
    private Map<JPDADebugger,JPDAThread[]> threadFilters;

    
    private FieldBreakpoint () {
    }
    
    /**
     * Creates a new breakpoint for given parameters.
     *
     * @param className class name
     * @param fieldName name of field
     * @param breakpointType one of constants: TYPE_ACCESS, 
     *   TYPE_MODIFICATION
     * @return a new breakpoint for given parameters
     */
    public static FieldBreakpoint create (
        String className,
        String fieldName,
        int breakpointType
    ) {
        FieldBreakpoint b = new FieldBreakpointImpl ();
        b.setClassName (className);
        b.setFieldName (fieldName);
        b.setBreakpointType (breakpointType);
        return b;
    }

    /**
     * Get name of class the field is defined in.
     *
     * @return the name of class the field is defined in
     */
    public String getClassName () {
        return className;
    }

    /**
     * Set name of class the field is defined in.
     *
     * @param className a new name of class the field is defined in
     */
    public void setClassName (String className) {
        if (Objects.equals(className, this.className)) return;
        Object old = this.className;
        this.className = className;
        firePropertyChange (PROP_CLASS_NAME, old, className);
    }

    /**
     * Returns name of field.
     *
     * @return a name of field
     */
    public String getFieldName () {
        return fieldName;
    }

    /**
     * Sets name of field.
     *
     * @param name a name of field
     */
    public void setFieldName (String name) {
        if (name != null) {
            name = name.trim();
        }
        if (Objects.equals(name, fieldName)) return;
        String old = fieldName;
        fieldName = name;
        firePropertyChange (PROP_FIELD_NAME, old, fieldName);
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
     * Returns type of breakpoint (one of TYPE_ACCESS and TYPE_MODIFICATION).
     *
     * @return type of breakpoint
     */
    public int getBreakpointType () {
        return type;
    }

    /**
     * Sets type of breakpoint.
     *
     * @param type a new type of breakpoint
     */
    public void setBreakpointType (int type) {
        if (this.type == type) return;
        if ( (type != TYPE_MODIFICATION) &&
                (type != TYPE_ACCESS) &&
                (type != (TYPE_MODIFICATION | TYPE_ACCESS))
           ) throw new IllegalArgumentException  ();
        int old = this.type;
        this.type = type;
        firePropertyChange (PROP_BREAKPOINT_TYPE, Integer.valueOf(old), Integer.valueOf(type));
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
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString () {
        return "FieldBreakpoint " + className + "." + fieldName;
    }
    
    private static final class FieldBreakpointImpl extends FieldBreakpoint implements ChangeListener,
                                                                                      PropertyChangeListener {
        
        @Override
        public GroupProperties getGroupProperties() {
            return new FieldGroupProperties();
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

        private final class FieldGroupProperties extends GroupProperties {

            @Override
            public String getType() {
                return NbBundle.getMessage(FieldBreakpoint.class, "FieldBrkp_Type");
            }

            @Override
            public String getLanguage() {
                return "Java";
            }

            @Override
            public FileObject[] getFiles() {
                List<FileObject> files = new ArrayList<FileObject>();
                String className = getClassName();
                // TODO: annotate also other matched classes
                if (!className.startsWith("*") && !className.endsWith("*")) {
                    fillFilesForClass(className, files);
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
                return FieldBreakpointImpl.this.getEngines();
            }

            @Override
            public boolean isHidden() {
                return FieldBreakpointImpl.this.isHidden();
            }

        }

    }
}
