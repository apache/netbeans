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

package org.netbeans.modules.javascript2.debug.ui.annotation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfo;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfoManager;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.javascript2.debug.ui.JSUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;

/**
 *
 * @author Martin
 */
class BreakpointAnnotationManager implements PropertyChangeListener {
    
    private static Reference<BreakpointAnnotationManager> INSTANCE_REF = new WeakReference<>(null);
    
    static BreakpointAnnotationManager getInstance() {
        JSBreakpointsInfoManager jsBIM = JSBreakpointsInfoManager.getDefault();
        BreakpointAnnotationManager instance;
        synchronized (BreakpointAnnotationManager.class) {
            instance = INSTANCE_REF.get();
            if (instance == null) {
                instance = new BreakpointAnnotationManager(jsBIM);
                INSTANCE_REF = new WeakReference<>(instance);
            }
        }
        return instance;
    }
    
    private static final Logger logger = Logger.getLogger(BreakpointAnnotationManager.class.getName());
    
    private final Map<JSLineBreakpoint, Annotation> breakpointAnnotations = new HashMap<>();
    private final Set<FileObject> annotatedFiles = new WeakSet<>();
    private Set<PropertyChangeListener> dataObjectListeners;
    private final RequestProcessor annotationProcessor = new RequestProcessor("Annotation Refresh", 1);
    private volatile Boolean active = null;
    
    private BreakpointAnnotationManager(JSBreakpointsInfoManager jsBIM) {
        jsBIM.addPropertyChangeListener(WeakListeners.propertyChange(this, jsBIM));
    }
    
    private static boolean isAnnotateable(Breakpoint breakpoint) {
        return breakpoint instanceof JSLineBreakpoint;
    }
    
    private static boolean isAnnotateable(FileObject fo) {
        return JSBreakpointsInfoManager.getDefault().isAnnotatable(fo);
    }

    public void annotate(Line.Set set, Lookup context) {
        final FileObject fo = context.lookup(FileObject.class);
        if (fo == null || !isAnnotateable(fo)) {
            return ;
        }
        DataObject dobj = context.lookup(DataObject.class);
        logger.log(Level.FINE, "annotate({0}, {1}), fo = {2}, foID = {3}, dobj = {4}",
                   new Object[] { set, context, fo, System.identityHashCode(fo), dobj });
        if (dobj != null) {
            PropertyChangeListener pchl = new PropertyChangeListener() {
                /** annotate renamed files. */
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (DataObject.PROP_PRIMARY_FILE.equals(evt.getPropertyName())) {
                        DataObject dobj = (DataObject) evt.getSource();
                        final FileObject newFO = dobj.getPrimaryFile();
                        annotationProcessor.post(new Runnable() {
                            @Override
                            public void run() {
                                annotate(newFO);
                            }
                        });
                    }
                }
            };
            dobj.addPropertyChangeListener(WeakListeners.propertyChange(pchl, dobj));
            synchronized (this) {
                if (dataObjectListeners == null) {
                    dataObjectListeners = new HashSet<>();
                }
                // Prevent from GC.
                dataObjectListeners.add(pchl);
            }
        }
        annotate(fo);
    }
    
    private void annotate (final FileObject fo) {
        synchronized (breakpointAnnotations) {
//            if (annotatedFiles.contains(fo)) {
//                // Already annotated
//                return ;
//            }
            //Set<JSBreakpoint> annotatedBreakpoints = breakpointAnnotations.keySet();
            for (Breakpoint breakpoint : DebuggerManager.getDebuggerManager().getBreakpoints()) {
                if (isAnnotateable(breakpoint) && !breakpointAnnotations.containsKey(breakpoint)) {
                    JSLineBreakpoint b = (JSLineBreakpoint) breakpoint;
                    if (fo.equals(b.getFileObject())) {
                        logger.log(Level.FINE, "annotate({0} (ID={1})): b = {2}",
                                   new Object[] { fo, System.identityHashCode(fo), b });
                        b.addPropertyChangeListener(this);
                        annotationProcessor.post(new AnnotationRefresh(b, false, true));
                    }
                }
            }
            annotatedFiles.add(fo);
            logger.log(Level.FINE, "Annotated files = {0}", annotatedFiles);
        }
    }
    
    void breakpointAdded(Breakpoint breakpoint) {
        if (!isAnnotateable(breakpoint)) {
            return;
        }
        JSLineBreakpoint lb = (JSLineBreakpoint) breakpoint;
        FileObject fo = lb.getFileObject();
        synchronized (breakpointAnnotations) {
            boolean isFileAnnotated = annotatedFiles.contains(fo);
            logger.log(Level.FINE, "breakpointAdded({0}), fo = {1}, foID = {2}, annotated = {3}",
                       new Object[] { breakpoint, fo, System.identityHashCode(fo), isFileAnnotated });
            //if (isFileAnnotated) {
                lb.addPropertyChangeListener(this);
                annotationProcessor.post(new AnnotationRefresh(lb, false, true));
            //}
        }
    }

    void breakpointRemoved(Breakpoint breakpoint) {
        if (!isAnnotateable(breakpoint)) {
            return;
        }
        JSLineBreakpoint lb = (JSLineBreakpoint) breakpoint;
        logger.log(Level.FINE, "breakpointRemoved({0})", breakpoint);
        lb.removePropertyChangeListener(this);
        annotationProcessor.post(new AnnotationRefresh(lb, true, false));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (Breakpoint.PROP_ENABLED.equals(propertyName) ||
            JSLineBreakpoint.PROP_LINE_NUMBER.equals(propertyName) ||
            Breakpoint.PROP_VALIDITY.equals(propertyName) ||
            JSLineBreakpoint.PROP_CONDITION.equals(propertyName)) {
            
            JSLineBreakpoint lb = (JSLineBreakpoint) evt.getSource();
            annotationProcessor.post(new AnnotationRefresh(lb, true, true));
        } else if (JSBreakpointsInfo.PROP_BREAKPOINTS_ACTIVE.equals(propertyName)) {
            boolean a = JSBreakpointsInfoManager.getDefault().areBreakpointsActivated();
            if (active != null && a != active.booleanValue()) {
                active = a;
                annotationProcessor.post(new AnnotationRefresh(null, true, true));
            }
        }
    }

    private void addAnnotation(JSLineBreakpoint breakpoint) {
        Line line = JSUtils.getLine(breakpoint);
        if (active == null) {
            active = JSBreakpointsInfoManager.getDefault().areBreakpointsActivated();
        }
        Annotation annotation = new LineBreakpointAnnotation(line, (JSLineBreakpoint) breakpoint, active);
        logger.log(Level.FINE, "Added annotation of {0} : {1}",
                   new Object[] { breakpoint, annotation });
        synchronized (breakpointAnnotations) {
            breakpointAnnotations.put(breakpoint, annotation);
        }
    }

    private boolean removeAnnotation(Breakpoint breakpoint) {
        Annotation annotation;
        synchronized (breakpointAnnotations) {
            annotation = breakpointAnnotations.remove(breakpoint);
        }
        if (annotation != null) {
            logger.log(Level.FINE, "Removed annotation of {0} : {1}",
                       new Object[] { breakpoint, annotation });
            annotation.detach();
            return true;
        } else {
            return false;
        }
    }
    
    private final class AnnotationRefresh implements Runnable {
        
        private final JSLineBreakpoint b;
        private final boolean remove, add;
        
        public AnnotationRefresh(JSLineBreakpoint b, boolean remove, boolean add) {
            this.b = b;
            this.remove = remove;
            this.add = add;
        }

        @Override
        public void run() {
            if (b != null) {
                refreshAnnotation(b);
            } else {
                List<JSLineBreakpoint> bpts;
                synchronized (breakpointAnnotations) {
                    bpts = new ArrayList<>(breakpointAnnotations.keySet());
                }
                for (JSLineBreakpoint bp : bpts) {
                    refreshAnnotation(bp);
                }
            }
        }
        
        private void refreshAnnotation(JSLineBreakpoint b) {
            boolean removed = removeAnnotation(b);
            if (add && (!remove || remove && removed)) {
                // if both add && remove are true (refresh of an existing annotation),
                // add only when some annotation was actually removed
                addAnnotation(b);
            }
        }
        
    }

}
