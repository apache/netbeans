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
import java.util.Map;
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
        if ( (className == this.className) ||
             ( (className != null) && 
               (this.className != null) && 
               this.className.equals (className)
             )
        ) return;
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
        if ( (name == fieldName) ||
             ((name != null) && (fieldName != null) && fieldName.equals (name))
        ) return;
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
