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

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.WeakListeners;

/**
 * This class handles the transition of breakpoints to moved (re-factored) files.
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(types={LazyDebuggerManagerListener.class})
public class FileMoveBreakpointsHandler implements LazyDebuggerManagerListener {
    
    private static final Logger LOG = Logger.getLogger(FileMoveBreakpointsHandler.class.getName());
    
    private final Map<Breakpoint, BreakpointHandler> handlerMap = new HashMap<Breakpoint, BreakpointHandler>();
    private final ThreadLocal<BreakpointHandler> preferedHandler = new ThreadLocal<BreakpointHandler>();

    @Override
    public String[] getProperties() {
        return new String [] {
            DebuggerManager.PROP_BREAKPOINTS,
        };
    }

    @Override
    public Breakpoint[] initBreakpoints() {
        return new Breakpoint[] {};
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (breakpoint instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) breakpoint;
            BreakpointHandler bh = preferedHandler.get();
            if (bh == null) {
                bh = new BreakpointHandler(lb);
            }
            synchronized (handlerMap) {
                handlerMap.put(breakpoint, bh);
            }
        }
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (breakpoint instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) breakpoint;
            BreakpointHandler bh;
            synchronized (handlerMap) {
                bh = handlerMap.remove(lb);
            }
            if (bh != null) {
                bh.removed();
            }
        }
    }

    @Override
    public void initWatches() {}

    @Override
    public void watchAdded(Watch watch) {}

    @Override
    public void watchRemoved(Watch watch) {}

    @Override
    public void sessionAdded(Session session) {}

    @Override
    public void sessionRemoved(Session session) {}

    @Override
    public void engineAdded(DebuggerEngine engine) {}

    @Override
    public void engineRemoved(DebuggerEngine engine) {}

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }
    
    private class BreakpointHandler implements PropertyChangeListener, ChangeListener {
        
        private final LineBreakpoint lb;
        private FileObject fo;
        private ChangeListener registryListener;
        private WeakReference<DataObject> dobjRef;
        private PropertyChangeListener dobjwl;  // Weak listener on the dataObject
        private boolean fileWasDeleted;         // Flag whether the breakpoint file was deleted
        
        public BreakpointHandler(LineBreakpoint lb) {
            this.lb = lb;
            lb.addPropertyChangeListener(LineBreakpoint.PROP_URL, this);
            handleURL(lb.getURL());
        }
        
        private void handleURL(String url) {
            FileObject newFO = null;
            if (url.length() > 0) {
                try {
                    newFO = URLMapper.findFileObject(new URL(url));
                } catch (MalformedURLException ex) {
                    LOG.log(Level.WARNING, "URL = '"+url+"'", ex);
                } catch (IllegalArgumentException ex) {
                    LOG.log(Level.WARNING, "URL = '"+url+"'", ex);
                }
            }
            synchronized (this) {
                fo = newFO;
                if (dobjRef != null) {
                    DataObject dobj = dobjRef.get();
                    if (dobj != null) {
                        dobj.removePropertyChangeListener(dobjwl);
                    }
                    dobjRef = null;
                    dobjwl = null;
                }
                if (newFO != null && registryListener == null) {
                    /*
                    fileListener = WeakListeners.create(FileChangeListener.class, this, fo);
                    fo.addFileChangeListener(fileListener);
                    */
                    registryListener = WeakListeners.change(this, DataObject.getRegistry());
                    DataObject.getRegistry().addChangeListener(registryListener);
                } else if (newFO == null && registryListener != null) {
                    DataObject.getRegistry().removeChangeListener(registryListener);
                    registryListener = null;
                }
            }
        }
        
        void removed() {
            lb.removePropertyChangeListener(LineBreakpoint.PROP_URL, this);
            FileObject theFO;
            boolean valid;
            synchronized (this) {
                theFO = fo;
                if (theFO == null) {
                    return ;
                }
                valid = theFO.isValid();
                if (valid) {  // Removed from a non-deleted file, abandon listening on DataObject
                    if (dobjRef != null) {
                        DataObject dobj = dobjRef.get();
                        if (dobj != null) {
                            dobj.removePropertyChangeListener(dobjwl);
                        }
                        dobjRef = null;
                        dobjwl = null;
                    }
                    if (registryListener != null) {
                        DataObject.getRegistry().removeChangeListener(registryListener);
                        registryListener = null;
                    }
                } else {    // Breakpoint was removed on file deletion, keep listening on DataObject, it can get a new primary file
                    fileWasDeleted = true;
                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == lb) {
                handleURL(lb.getURL());
            } else if (DataObject.PROP_PRIMARY_FILE.equals(evt.getPropertyName())) {
                FileObject newFO = ((DataObject) evt.getSource()).getPrimaryFile();
                boolean addBack = false;
                synchronized (this) {
                    fo = newFO;
                    if (fileWasDeleted) {
                        // Add back
                        addBack = true;
                        fileWasDeleted = false;
                    }
                }
                lb.setURL(newFO.toURL().toString());
                if (addBack) {
                    try {
                        preferedHandler.set(this);
                        DebuggerManager.getDebuggerManager().addBreakpoint(lb);
                        lb.addPropertyChangeListener(LineBreakpoint.PROP_URL, this);
                    } finally {
                        preferedHandler.remove();
                    }
                    
                }
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            Object source = e.getSource();
            if (source instanceof Collection) {
                FileObject bfo = this.fo;
                if (bfo == null) {
                    return ;
                }
                for (Object obj : ((Collection) source)) {
                    DataObject dobj = (DataObject) obj;
                    FileObject primary = dobj.getPrimaryFile();
                    if (bfo.equals(primary)) {
                        synchronized (this) {
                            dobjRef = new WeakReference<DataObject>(dobj);
                            dobjwl = WeakListeners.propertyChange(this, dobj);
                            dobj.addPropertyChangeListener(dobjwl);
                            if (registryListener != null) {
                                DataObject.getRegistry().removeChangeListener(registryListener);
                                registryListener = null;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }
    
}
