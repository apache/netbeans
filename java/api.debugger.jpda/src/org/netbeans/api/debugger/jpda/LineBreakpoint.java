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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.debugger.jpda.BreakpointStratifier;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * Notifies about line breakpoint events.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint (LineBreakpoint.create (
 *        "examples.texteditor.Ted",
 *        27
 *    ));</pre>
 * This breakpoint stops in Ted class on 27 line number.
 *
 * @author Jan Jancura
 */
public class LineBreakpoint extends JPDABreakpoint {

    /** Property name constant */
    public static final String          PROP_LINE_NUMBER = "lineNumber"; // NOI18N
    /** Property name constant */
    public static final String          PROP_URL = "url"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_CONDITION = "condition"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_SOURCE_NAME = "sourceName"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_SOURCE_PATH = "sourcePath"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_STRATUM = "stratum"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_PREFERRED_CLASS_NAME = "classNamePreferred"; // NOI18N
    /** Property name constant */
    public static final String          PROP_INSTANCE_FILTERS = "instanceFilters"; // NOI18N
    /** Property name constant */
    public static final String          PROP_THREAD_FILTERS = "threadFilters"; // NOI18N
    
    private static final Logger LOG = Logger.getLogger(LineBreakpoint.class.getName());

    private String                      url = ""; // NOI18N
    private int                         lineNumber;
    private String                      condition = ""; // NOI18N
    private String                      sourceName = null;
    private String                      sourcePath = null;
    private String                      stratum = "Java"; // NOI18N
    private String                      className = null;
    private Map<JPDADebugger,ObjectVariable[]> instanceFilters;
    private Map<JPDADebugger,JPDAThread[]> threadFilters;

    
    private LineBreakpoint (String url) {
        this.url = url;
    }
    
    /**
     * Creates a new breakpoint for given parameters.
     *
     * @param url a string representation of URL of the source file
     * @param lineNumber a line number
     * @return a new breakpoint for given parameters
     */
    public static LineBreakpoint create (
        String url,
        int lineNumber
    ) {
        LineBreakpoint b = new LineBreakpointImpl (url);
        b.setLineNumber (lineNumber);
        if (url != null && !url.isEmpty()) {
            Collection<? extends BreakpointStratifier> stratifiers = Lookup.getDefault().lookupAll(BreakpointStratifier.class);
            for (BreakpointStratifier stratifier : stratifiers) {
                stratifier.stratify(b);
            }
        }
        return b;
    }

    /**
     * Gets the string representation of URL of the source file,
     * which contains the class to stop on.
     *
     * @return name of class to stop on
     */
    public String getURL () {
        return url;
    }
    
    /**
     * Sets the string representation of URL of the source file,
     * which contains the class to stop on.
     *
     * @param url the URL of class to stop on
     */
    public void setURL (String url) {
        String old;
        synchronized (this) {
            if (url == null) {
                url = "";
            }
            if (url.equals(this.url)) {
                return;
            }
            old = this.url;
            this.url = url;
        }
        firePropertyChange (PROP_URL, old, url);
    }
    
    /**
     * Gets number of line to stop on.
     *
     * @return line number to stop on
     */
    public int getLineNumber () {
        return lineNumber;
    }
    
    /**
     * Sets number of line to stop on.
     *
     * @param ln a line number to stop on
     */
    public void setLineNumber (int ln) {
        int old;
        synchronized (this) {
            if (ln == lineNumber) {
                return;
            }
            old = lineNumber;
            lineNumber = ln;
        }
        firePropertyChange (
            PROP_LINE_NUMBER,
            Integer.valueOf(old),
            Integer.valueOf(ln)
        );
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
     * @param c a new condition
     */
    public void setCondition (String c) {
        String old;
        synchronized (this) {
            if (c == null) {
                c = "";
            }
            c = c.trim ();
            if (c.equals (condition)) {
                return;
            }
            old = condition;
            condition = c;
        }
        firePropertyChange (PROP_CONDITION, old, c);
    }
    
    /**
     * Returns stratum.
     *
     * @return a stratum
     */
    public String getStratum () {
        return stratum;
    }
    
    /**
     * Sets stratum.
     *
     * @param s a new stratum
     */
    public void setStratum (String s) {
        String old;
        synchronized (this) {
            if (s == null) {
                s = "";
            }
            s = s.trim ();
            if (s.equals (stratum)) {
                return;
            }
            old = stratum;
            stratum = s;
        }
        firePropertyChange (PROP_CONDITION, old, s);
    }
    
    /**
     * Returns the name of the source file.
     *
     * @return a source name or <code>null</code> when no source name is defined.
     */
    public String getSourceName () {
        return sourceName;
    }
    
    /**
     * Sets the name of the source file.
     *
     * @param sn a new source name or <code>null</code>.
     */
    public void setSourceName (String sn) {
        String old;
        synchronized (this) {
            if (sn != null) {
                sn = sn.trim ();
                if (sn.equals(sourceName)) {
                    return ;
                }
            } else {
                if (sourceName == null) {
                    return ;
                }
            }
            old = sourceName;
            sourceName = sn;
        }
        firePropertyChange (PROP_SOURCE_NAME, old, sn);
    }

    /**
     * Returns source path, relative to the source root.
     *
     * @return a source path or <code>null</code> when no source path is defined.
     *
     * @since 1.3
     */
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * Sets source path, relative to the source root.
     *
     * @param sp a new source path or <code>null</code>
     *
     * @since 1.3
     */
    public void setSourcePath (String sp) {
        String old;
        synchronized (this) {
            if (sp != null) {
                sp = sp.trim();
                if (sp.equals(sourcePath)) {
                   return ;
                }
            } else {
                if (sourcePath == null) {
                    return ;
                }
            }
            old = sourcePath;
            sourcePath = sp;
        }
        firePropertyChange (PROP_SOURCE_PATH, old, sp);
    }
    
    /**
     * Sets the binary class name that is used to submit the breakpoint.
     * @param className The binary class name, or <code>null</code> if the class
     * name should be retrieved automatically from the URL and line number.
     * @since 2.8
     */
    public void setPreferredClassName(String className) {
        String old;
        synchronized (this) {
            if (className == null ? this.className == null : className.equals(this.className)) {
                return ;
            }
            old = className;
            this.className = className;
        }
        firePropertyChange (PROP_PREFERRED_CLASS_NAME, old, className);
    }
    
    /**
     * Gets the binary class name that is used to submit the breakpoint.
     * @return The binary class name, if previously set by {@link #setPreferredClassName}
     * method, or <code>null</code> if the class name should be retrieved
     * automatically from the URL and line number.
     * @since 2.8
     */
    public String getPreferredClassName() {
        return className;
    }
    
    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    @Override
    public String toString () {
        String fileName = null;
        try {
            FileObject fo = URLMapper.findFileObject(new URL(url));
            if (fo != null) {
                fileName = fo.getNameExt();
            }
        } catch (MalformedURLException ex) {}
        if (fileName == null) {
            fileName = url;
        }
        return "LineBreakpoint " + fileName + " : " + lineNumber;
    }
    
    private static class LineBreakpointImpl extends LineBreakpoint 
                                            implements Comparable, FileChangeListener,
                                                       ChangeListener, PropertyChangeListener {
        
        public static final String          PROP_PREFERRED_CLASS_TYPE = "classTypePreferred"; // NOI18N
        
        // We need to hold our FileObject so that it's not GC'ed, because we'd loose our listener.
        private FileObject fo;
        private ChangeListener registryListener;
        private FileChangeListener fileListener;
        private JPDAClassType classType;
        private boolean wasRegisteredWhenFileDeleted;
       
        public LineBreakpointImpl(String url) {
            super(url);
            if (url.length() > 0) {
                try {
                    fo = URLMapper.findFileObject(new URL(url));
                    if (fo != null) {
                        fileListener = WeakListeners.create(FileChangeListener.class, this, fo);
                        fo.addFileChangeListener(fileListener);
                        /*
                        registryListener = WeakListeners.change(this, DataObject.getRegistry());
                        DataObject.getRegistry().addChangeListener(registryListener);
                                */
                    }
                } catch (MalformedURLException ex) {
                    LOG.log(Level.WARNING, "URL = '"+url+"'", ex);
                } catch (IllegalArgumentException ex) {
                    LOG.log(Level.WARNING, "URL = '"+url+"'", ex);
                }
            }
        }

        @Override
        public void setURL(String url) {
            if (fo != null) {
                fo.removeFileChangeListener(fileListener);
            }
            super.setURL(url);
            if (url.length() > 0) {
                try {
                    fo = URLMapper.findFileObject(new URL(url));
                    if (fo != null) {
                        fileListener = WeakListeners.create(FileChangeListener.class, this, fo);
                        fo.addFileChangeListener(fileListener);
                    }
                } catch (MalformedURLException ex) {
                    LOG.log(Level.WARNING, "URL = '"+url+"'", ex);
                } catch (IllegalArgumentException ex) {
                    LOG.log(Level.WARNING, "URL = '"+url+"'", ex);
                }
            }
            firePropertyChange(PROP_GROUP_PROPERTIES, null, null);
        }

        public void setPreferredClassType(JPDAClassType classType) {
            JPDAClassType old;
            synchronized (this) {
                if (classType == null ? this.classType == null : classType.equals(this.classType)) {
                    return ;
                }
                old = classType;
                this.classType = classType;
            }
            firePropertyChange (PROP_PREFERRED_CLASS_TYPE, old, classType);
        }

        public JPDAClassType getPreferredClassType() {
            return classType;
        }
    
        @Override
        public GroupProperties getGroupProperties() {
            return new LineGroupProperties();
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof LineBreakpointImpl) {
                LineBreakpoint lbthis = this;
                LineBreakpoint lb = (LineBreakpoint) o;
                int uc = lbthis.url.compareTo(lb.url);
                if (uc != 0) {
                    return uc;
                } else {
                    return lbthis.lineNumber - lb.lineNumber;
                }
            } else {
                return -1;
            }
        }

        @Override
        public boolean equals(Object obj) {
            // We want each breakpoint to be distinctive
            return obj == this;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
            for (Breakpoint b : breakpoints) {
                // Not nice, but the API is limiting
                if (b == this) {
                    wasRegisteredWhenFileDeleted = true;
                    DebuggerManager.getDebuggerManager().removeBreakpoint(this);
                    break;
                }
            }
            fo = null;
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
                this.setURL(((FileObject) fe.getSource()).toURL().toString());
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    
        @Override
        public void stateChanged(ChangeEvent chev) {
            Object source = chev.getSource();
            if (source instanceof Breakpoint.VALIDITY) {
                setValidity((Breakpoint.VALIDITY) source, chev.toString());
            /*} else if (source instanceof Collection) {
                for (Object obj : ((Collection) source)) {
                    DataObject dobj = (DataObject) obj;
                    if (registryListener != null) {
                        FileObject fileObject = this.fo;
                        if (fileObject == null) {
                            DataObject.getRegistry().removeChangeListener(registryListener);
                            registryListener = null;
                            return ;
                        }
                        FileObject primary = dobj.getPrimaryFile();
                        if (fileObject.equals(primary)) {
                            dobj.addPropertyChangeListener(WeakListeners.propertyChange(this, dobj));
                            DataObject.getRegistry().removeChangeListener(registryListener);
                            registryListener = null;
                        }
                    }
                }*/
            } else {
                throw new UnsupportedOperationException(chev.toString());
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            /*if (DataObject.PROP_PRIMARY_FILE.equals(evt.getPropertyName())) {
                if (fo != null) {
                    fo.removeFileChangeListener(fileListener);
                }
                FileObject newFO = ((DataObject) evt.getSource()).getPrimaryFile();
                fileListener = WeakListeners.create(FileChangeListener.class, this, newFO);
                newFO.addFileChangeListener(fileListener);
                    this.setURL(newFO.toURL().toString());
                fo = newFO;
                if (wasRegisteredWhenFileDeleted) {
                    // Add back
                    DebuggerManager.getDebuggerManager().addBreakpoint(this);
                    wasRegisteredWhenFileDeleted = false;
                }
                firePropertyChange(PROP_GROUP_PROPERTIES, null, null);
            } else*/ if (DebuggerEngine.class.getName().equals(evt.getPropertyName())) {
                enginePropertyChange(evt);
            }
        }

        private final class LineGroupProperties extends GroupProperties {

            @Override
            public String getType() {
                return NbBundle.getMessage(LineBreakpoint.class, "LineBrkp_Type");
            }

            @Override
            public String getLanguage() {
                return "Java";
            }

            @Override
            public FileObject[] getFiles() {
                return new FileObject[] { fo };
            }

            @Override
            public Project[] getProjects() {
                FileObject f = fo;
                while (f != null) {
                    f = f.getParent();
                    if (f != null && ProjectManager.getDefault().isProject(f)) {
                        break;
                    }
                }
                if (f != null) {
                    try {
                        return new Project[] { ProjectManager.getDefault().findProject(f) };
                    } catch (IOException ex) {
                    } catch (IllegalArgumentException ex) {
                    }
                }
                return null;
            }

            @Override
            public DebuggerEngine[] getEngines() {
                return LineBreakpointImpl.this.getEngines();
            }

            @Override
            public boolean isHidden() {
                return LineBreakpointImpl.this.isHidden();
            }

        }

    }
}
