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

package org.netbeans.modules.debugger.jpda.projectsui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.modules.debugger.jpda.projects.EditorContextSupport;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.AnnotationProvider;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;


/**
 * This class is called when some file in editor is opened. It changes if
 * some LineBreakpoints with annotations should be read.
 *
 * @author Jan Jancura, Martin Entlicher
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.text.AnnotationProvider.class)
public class BreakpointAnnotationProvider implements AnnotationProvider {

    private final Map<JPDABreakpoint, Set<Annotation>> breakpointToAnnotations =
            new IdentityHashMap<JPDABreakpoint, Set<Annotation>>();
    private final Set<FileObject> annotatedFiles = Collections.newSetFromMap(new WeakHashMap<>());
    private Set<PropertyChangeListener> dataObjectListeners;
    private volatile boolean breakpointsActive = true;
    private RequestProcessor annotationProcessor = new RequestProcessor("Annotation Refresh", 1);
    private RequestProcessor contextWaitingProcessor = new RequestProcessor("Annotation Refresh Context Waiting", 1);
    
    static BreakpointAnnotationProvider getInstance() {
        for (AnnotationProvider act : Lookup.getDefault().lookupAll(AnnotationProvider.class)) {
            if (act instanceof BreakpointAnnotationProvider) {
                return (BreakpointAnnotationProvider) act;
            }
        }
        throw new IllegalStateException("BreakpointAnnotationProvider is not registered in Lookup!");
    }

    @Override
    public void annotate (Line.Set set, Lookup lookup) {
        final FileObject fo = lookup.lookup(FileObject.class);
        if (fo != null) {
            DataObject dobj = lookup.lookup(DataObject.class);
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
                        dataObjectListeners = new HashSet<PropertyChangeListener>();
                    }
                    // Prevent from GC.
                    dataObjectListeners.add(pchl);
                }
            }
            annotate(fo);
        }
    }
    
    private void annotate (final FileObject fo) {
        synchronized (breakpointToAnnotations) {
            for (Breakpoint breakpoint : DebuggerManager.getDebuggerManager().getBreakpoints()) {
                if (isAnnotatable(breakpoint)) {
                    JPDABreakpoint b = (JPDABreakpoint) breakpoint;
                    int[] lines = getAnnotationLines(b, fo);
                    if (lines != null && lines.length > 0) {
                        removeAnnotations(b);   // Remove any staled breakpoint annotations
                        breakpointToAnnotations.put(b, Collections.newSetFromMap(new WeakHashMap<>()));
                        if (b instanceof LineBreakpoint) {
                            LineBreakpoint lb = (LineBreakpoint) b;
                            LineTranslations.getTranslations().unregisterFromLineUpdates(lb); // To be sure
                            LineTranslations.getTranslations().registerForLineUpdates(lb);
                        }
                        addAnnotationTo(b, fo, lines);
                    }
                }
            }
            annotatedFiles.add(fo);
        }
    }

    void setBreakpointsActive(boolean active) {
        if (breakpointsActive == active) {
            return ;
        }
        breakpointsActive = active;
        annotationProcessor.post(new AnnotationRefresh(null, true, true));
    }
    
    void postAnnotationRefresh(JPDABreakpoint b, boolean remove, boolean add) {
        annotationProcessor.post(new AnnotationRefresh(b, remove, add));
    }
    
    private final class AnnotationRefresh implements Runnable {
        
        private final JPDABreakpoint b;
        private final boolean remove, add;
        
        public AnnotationRefresh(JPDABreakpoint b, boolean remove, boolean add) {
            this.b = b;
            this.remove = remove;
            this.add = add;
        }

        @Override
        public void run() {
            synchronized (breakpointToAnnotations) {
                if (b != null) {
                    refreshAnnotation(b);
                } else {
                    List<JPDABreakpoint> bpts = new ArrayList<JPDABreakpoint>(breakpointToAnnotations.keySet());
                    for (JPDABreakpoint bp : bpts) {
                        refreshAnnotation(bp);
                    }
                }
            }
        }
        
        private void refreshAnnotation(JPDABreakpoint b) {
            removeAnnotations(b);
            if (remove) {
                if (!add) {
                    breakpointToAnnotations.remove(b);
                }
            }
            if (add) {
                breakpointToAnnotations.put(b, Collections.newSetFromMap(new WeakHashMap<>()));
                for (FileObject fo : annotatedFiles) {
                    addAnnotationTo(b, fo);
                }
            }
        }
        
    }
    
    static boolean isAnnotatable(Breakpoint b) {
        return (b instanceof LineBreakpoint ||
                b instanceof FieldBreakpoint ||
                b instanceof MethodBreakpoint ||
                b instanceof ClassLoadUnloadBreakpoint) &&
               !((JPDABreakpoint) b).isHidden();
    }
    
    private static String getAnnotationType(JPDABreakpoint b, boolean isConditional,
                                            boolean active) {
        boolean isInvalid = b.getValidity() == VALIDITY.INVALID;
        String annotationType;
        if (b instanceof LineBreakpoint) {
            annotationType = b.isEnabled () ?
            (isConditional ? EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.BREAKPOINT_ANNOTATION_TYPE) :
            (isConditional ? EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE);
        } else if (b instanceof FieldBreakpoint) {
            annotationType = b.isEnabled () ?
                EditorContext.FIELD_BREAKPOINT_ANNOTATION_TYPE :
                EditorContext.DISABLED_FIELD_BREAKPOINT_ANNOTATION_TYPE;
        } else if (b instanceof MethodBreakpoint) {
            annotationType = b.isEnabled () ?
                EditorContext.METHOD_BREAKPOINT_ANNOTATION_TYPE :
                EditorContext.DISABLED_METHOD_BREAKPOINT_ANNOTATION_TYPE;
        } else if (b instanceof ClassLoadUnloadBreakpoint) {
            annotationType = b.isEnabled() ?
                EditorContext.CLASS_BREAKPOINT_ANNOTATION_TYPE :
                EditorContext.DISABLED_CLASS_BREAKPOINT_ANNOTATION_TYPE;
        } else {
            throw new IllegalStateException(b.toString());
        }
        if (!active) {
            annotationType = annotationType + "_stroke";    // NOI18N
        } else if (isInvalid && b.isEnabled ()) {
            annotationType += "_broken";                    // NOI18N
        }
        return annotationType;
    }
    
    /** @return The annotation lines or <code>null</code>. */
    private int[] getAnnotationLines(JPDABreakpoint b, FileObject fo) {
        if (b instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) b;
            try {
                if (fo.toURL().equals(new URL(lb.getURL()))) {
                    return new int[] { lb.getLineNumber() };
                }
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        } else if (b instanceof FieldBreakpoint) {
            FieldBreakpoint fb = (FieldBreakpoint) b;
            String className = fb.getClassName();
            String fieldName = fb.getFieldName();
            Future<Integer> fi = EditorContextSupport.getFieldLineNumber(fo, className, fieldName);
            int line;
            if (fi != null) {
                if (!fi.isDone()) {
                    delayedAnnotation(b, fo, fi);
                    return null;
                }
                try {
                    line = fi.get();
                } catch (InterruptedException ex) {
                    return null;
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            } else {
                return null;
            }
            return new int[] { line };
        } else if (b instanceof MethodBreakpoint) {
            MethodBreakpoint mb = (MethodBreakpoint) b;
            String[] filters = mb.getClassFilters();
            int[] lns = new int[] {};
            for (int i = 0; i < filters.length; i++) {
                // TODO: annotate also other matched classes
                if (!filters[i].startsWith("*") && !filters[i].endsWith("*")) {
                    Future<int[]> futurelns = EditorContextSupport.getMethodLineNumbers(
                            fo, filters[i], mb.getClassExclusionFilters(),
                            mb.getMethodName(),
                            mb.getMethodSignature());
                    int[] newlns;
                    if (futurelns != null) {
                        if (!futurelns.isDone()) {
                            delayedAnnotation2(b, fo, futurelns);
                        } else {
                            try {
                                newlns = futurelns.get();
                                if (newlns == null) {
                                    continue;
                                }
                                if (lns.length == 0) {
                                    lns = newlns;
                                } else {
                                    int[] ln = new int[lns.length + newlns.length];
                                    System.arraycopy(lns, 0, ln, 0, lns.length);
                                    System.arraycopy(newlns, 0, ln, lns.length, newlns.length);
                                    lns = ln;
                                }
                            } catch (InterruptedException ex) {
                            } catch (ExecutionException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
            return lns;
        } else if (b instanceof ClassLoadUnloadBreakpoint) {
            ClassLoadUnloadBreakpoint cb = (ClassLoadUnloadBreakpoint) b;
            String[] filters = cb.getClassFilters();
            int[] lns = new int[] {};
            for (int i = 0; i < filters.length; i++) {
                // TODO: annotate also other matched classes
                if (!filters[i].startsWith("*") && !filters[i].endsWith("*")) {
                    Future<Integer> futurelns = EditorContextSupport.getClassLineNumber(
                            fo, filters[i], cb.getClassExclusionFilters());
                    Integer newline;
                    if (futurelns != null) {
                        if (!futurelns.isDone()) {
                            delayedAnnotation(b, fo, futurelns);
                        } else {
                            try {
                                newline = futurelns.get();
                                if (newline == null) {
                                    continue;
                                }
                                if (lns.length == 0) {
                                    lns = new int[] { newline };
                                } else {
                                    int[] ln = new int[lns.length + 1];
                                    System.arraycopy(lns, 0, ln, 0, lns.length);
                                    ln[lns.length] = newline;
                                    lns = ln;
                                }
                            } catch (InterruptedException ex) {
                            } catch (ExecutionException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
            return lns;
        } else {
            throw new IllegalStateException(b.toString());
        }
    }

    private void delayedAnnotation(final JPDABreakpoint b, final FileObject fo,
                                   final Future<Integer> fi) {
        contextWaitingProcessor.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Integer line = fi.get();
                    if (line != null) {
                        addAnnotationTo(b, fo, new int[] { line.intValue() });
                    }
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
    
    private void delayedAnnotation2(final JPDABreakpoint b, final FileObject fo,
                                    final Future<int[]> futurelns) {
        contextWaitingProcessor.post(new Runnable() {
            @Override
            public void run() {
                try {
                    int[] lines = futurelns.get();
                    if (lines != null && lines.length > 0) {
                        addAnnotationTo(b, fo, lines);
                    }
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    // Is called under synchronized (breakpointToAnnotations)
    private void addAnnotationTo(JPDABreakpoint b, FileObject fo) {
        int[] lines = getAnnotationLines(b, fo);
        addAnnotationTo(b, fo, lines);
    }

    // Is called under synchronized (breakpointToAnnotations)
    private void addAnnotationTo(JPDABreakpoint b, FileObject fo, int[] lines) {
        if (lines == null || lines.length == 0) {
            return ;
        }
        String condition = getCondition(b);
        boolean isConditional = condition.trim().length() > 0 || b.getHitCountFilteringStyle() != null;
        String annotationType = getAnnotationType(b, isConditional, breakpointsActive);
        DataObject dataObject;
        try {
            dataObject = DataObject.find(fo);
        } catch (DataObjectNotFoundException donfex) {
            Logger.getLogger(BreakpointAnnotationProvider.class.getName()).log(Level.INFO, "No DO for "+fo, donfex);
            return ;
        }
        LineCookie lc = dataObject.getLookup().lookup(LineCookie.class);
        if (lc == null) {
            return;
        }
        List<DebuggerBreakpointAnnotation> annotations = new ArrayList<DebuggerBreakpointAnnotation>();
        for (int l : lines) {
            if (l < 1){
                //avoid IndexOutOfBoundsException
                continue;
            }
            try {
                Line line = lc.getLineSet().getCurrent(l - 1);
                DebuggerBreakpointAnnotation annotation = new DebuggerBreakpointAnnotation (annotationType, line, b);
                annotations.add(annotation);
            } catch (IndexOutOfBoundsException e) {
            } catch (IllegalArgumentException e) {
            }
        }
        if (annotations.isEmpty()) {
            return ;
        }
        synchronized (breakpointToAnnotations) {
            Set<Annotation> bpAnnotations = breakpointToAnnotations.get(b);
            if (bpAnnotations == null) {
                Set<Annotation> set = Collections.newSetFromMap(new WeakHashMap<>());
                set.addAll(annotations);
                breakpointToAnnotations.put(b, set);
            } else {
                bpAnnotations.addAll(annotations);
                breakpointToAnnotations.put(b, bpAnnotations);
            }
        }
    }

    // Is called under synchronized (breakpointToAnnotations)
    private void removeAnnotations(JPDABreakpoint b) {
        assert Thread.holdsLock(breakpointToAnnotations);
        Set<Annotation> annotations = breakpointToAnnotations.remove(b);
        if (annotations == null) {
            return ;
        }
        for (Annotation a : annotations) {
            a.detach();
        }
    }

    /**
     * Gets the condition of a breakpoint.
     * @param b The breakpoint
     * @return The condition or empty {@link String} if no condition is supported.
     */
    static String getCondition(Breakpoint b) {
        if (!(b instanceof JPDABreakpoint)) {
            return ""; // e.g. JSP breakpoints
        }
        if (b instanceof LineBreakpoint) {
            return ((LineBreakpoint) b).getCondition();
        } else if (b instanceof FieldBreakpoint) {
            return ((FieldBreakpoint) b).getCondition();
        } else if (b instanceof MethodBreakpoint) {
            return ((MethodBreakpoint) b).getCondition();
        } else if (b instanceof ClassLoadUnloadBreakpoint) {
            return "";
        } else {
            throw new IllegalStateException(b.toString());
        }
    }
    
    /*
    // Not used
    @Override
    public Breakpoint[] initBreakpoints() { return new Breakpoint[] {}; }

    // Not used
    @Override
    public void initWatches() {}

    // Not used
    @Override
    public void watchAdded(Watch watch) {}

    // Not used
    @Override
    public void watchRemoved(Watch watch) {}

    // Not used
    @Override
    public void sessionAdded(Session session) {}

    // Not used
    @Override
    public void sessionRemoved(Session session) {}

    // Not used
    @Override
    public void engineAdded(DebuggerEngine engine) {}

    // Not used
    @Override
    public void engineRemoved(DebuggerEngine engine) {}
    */
}
