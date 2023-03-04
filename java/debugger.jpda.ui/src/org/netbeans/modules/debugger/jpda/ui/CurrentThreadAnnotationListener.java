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
package org.netbeans.modules.debugger.jpda.ui;

import com.sun.jdi.AbsentInformationException;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.beancontext.BeanContextChild;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;


/**
 * Listens on {@link org.netbeans.api.debugger.DebuggerManager} on
 * {@link JPDADebugger#PROP_CURRENT_THREAD}
 * property and annotates current line and call stack for
 * {@link org.netbeans.api.debugger.jpda.JPDAThread}s in NetBeans editor.
 *
 * @author Jan Jancura
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class CurrentThreadAnnotationListener extends DebuggerManagerAdapter {
    
    private static final int ANNOTATION_SCHEDULE_TIME = 100;
    private static final int ANNOTATION_STACK_SCHEDULE_TIME = 500;

    // annotation for current line
    private transient Object                currentPC;
    private final transient Object          currentPCLock = new Object();
    private transient boolean               currentPCSet = false;
    private JPDAThread                      currentThread;
    private JPDADebugger                    currentDebugger;
    private SourcePath                      currentSourcePath;
    private AllThreadsAnnotator             allThreadsAnnotator;



    @Override
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_CURRENT_ENGINE};
    }

    /**
     * Listens JPDADebuggerEngineImpl and DebuggerManager.
     */
    @Override
    public void propertyChange (PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        if (DebuggerManager.PROP_CURRENT_ENGINE.equals(propertyName)) {
            updateCurrentDebugger ((DebuggerEngine) e.getNewValue());
            updateCurrentThread ();
            annotate ();
        } else
        if (JPDADebugger.PROP_CURRENT_THREAD.equals(propertyName)) {
            updateCurrentThread ((JPDAThread) e.getNewValue());
            annotate ();
        } else
        if (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME.equals(propertyName)) {
            //updateCurrentThread ();
            //annotate ();
            showCurrentFrame((CallStackFrame) e.getNewValue());
        } else
        if (JPDADebugger.PROP_STATE.equals(propertyName)) {
            annotate ();
        } else
        if (JPDADebugger.PROP_THREAD_STARTED.equals(propertyName)) {
            synchronized (this) {
                if (allThreadsAnnotator != null) {
                    allThreadsAnnotator.add((JPDAThread) e.getNewValue());
                }
            }
        }
    }


    // helper methods ..........................................................

    private synchronized void updateCurrentDebugger (DebuggerEngine currentEngine) {
        JPDADebugger newDebugger;
        if (currentEngine == null) {
            newDebugger = null;
        } else {
            newDebugger = currentEngine.lookupFirst(null, JPDADebugger.class);
        }
        if (currentDebugger == newDebugger) return;
        //System.err.println("updateCurrentDebugger: "+currentDebugger+" -> "+newDebugger);
        if (currentDebugger != null)
            currentDebugger.removePropertyChangeListener (this);
        if (allThreadsAnnotator != null) {
            allThreadsAnnotator.cancel();
            allThreadsAnnotator = null;
        }
        currentSourcePath = null;
        if (newDebugger != null) {
            newDebugger.addPropertyChangeListener (this);
            allThreadsAnnotator = new AllThreadsAnnotator(newDebugger);
            currentSourcePath = getCurrentSourcePath(newDebugger);
        }
        currentDebugger = newDebugger;
        if (currentThread != null && allThreadsAnnotator != null) {
            allThreadsAnnotator.remove(currentThread);
            currentThread = null;
        }
        updateCurrentThread();
    }
    
    private synchronized void updateCurrentThread () {
        updateCurrentThread(currentDebugger != null ? currentDebugger.getCurrentThread() : null);
    }

    @SuppressWarnings(value={"LocalVariableHidesMemberVariable"})
    private synchronized void updateCurrentThread (JPDAThread newCurrentThread) {
        AllThreadsAnnotator allThreadsAnnotator;
        JPDAThread oldCurrent = null;
        JPDAThread newCurrent = null;
        synchronized (this) {
            oldCurrent = currentThread;
            // get current thread
            if (currentDebugger != null) {
                currentThread = newCurrentThread;
                newCurrent = currentThread;
            } else {
                currentThread = null;
            }
            allThreadsAnnotator = this.allThreadsAnnotator;
        if (allThreadsAnnotator != null) {
            if (oldCurrent != null) {
                allThreadsAnnotator.annotate(oldCurrent, false);
            }
            if (newCurrent != null) {
                allThreadsAnnotator.annotate(newCurrent, true);
            }
        }
        }
    }
    
    private SourcePath getCurrentSourcePath(JPDADebugger debugger) {
        Session currentSession = null;
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i].lookupFirst(null, JPDADebugger.class) == debugger) {
                currentSession = sessions[i];
                break;
            }
        }
        DebuggerEngine currentEngine = (currentSession == null) ?
            null : currentSession.getCurrentEngine();
        SourcePath sourcePath = (currentEngine == null) ? 
            null : currentEngine.lookupFirst(null, SourcePath.class);
        return sourcePath;
    }

    /**
     * Annotates current thread or removes annotations.
     */
    private void annotate () {
        // 1) no current thread => remove annotations
        final JPDADebugger debugger;
        final SourcePath sourcePath;
        final JPDAThread thread;
        synchronized (this) {
            debugger = currentDebugger;
            if ( (currentThread == null) ||
                 (debugger.getState () != JPDADebugger.STATE_STOPPED) ) {
                synchronized (currentPCLock) {
                    currentPCSet = false; // The annotation is goint to be removed
                }
                removeAnnotations ();
                return;
            }

            sourcePath = currentSourcePath;
            thread = currentThread;
        }
        Session s;
        try {
            s = (Session) debugger.getClass().getMethod("getSession").invoke(debugger);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            s = null;
        }
        RequestProcessor rProcessor = null;
        if (s != null) {
            rProcessor = s.lookupFirst(null, RequestProcessor.class);
        }
        if (rProcessor == null) {
            rProcessor = this.rp;
        }
        rProcessor.post(new Runnable() {
            @Override
            public void run() {
                annotate(debugger, thread, sourcePath);
            }
        });
    }

    private void annotate (JPDADebugger debugger, final JPDAThread currentThread, final SourcePath sourcePath) {
        // 1) no current thread => remove annotations
        CallStackFrame[] stack = null;
        final CallStackFrame csf;
        final String language;

        // 2) get call stack & Line
        csf = debugger.getCurrentCallStackFrame ();
        Session s;
        try {
            s = (Session) debugger.getClass().getMethod("getSession").invoke(debugger);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            s = null;
        }
        language = (s != null) ? s.getCurrentLanguage() : null;

        // 3) annotate current line & stack
        synchronized (currentPCLock) {
            currentPCSet = true; // The annotation is goint to be set
        }
        if (csf != null && sourcePath != null && currentThread != null) {
            CallStackFrame frameToShow = csf;
            int lineNumber = frameToShow.getLineNumber(language);
            if (lineNumber < 1) {
                try {
                    stack = currentThread.getCallStack ();
                } catch (AbsentInformationException ex) {
                    synchronized (currentPCLock) {
                        currentPCSet = false; // The annotation is goint to be removed
                    }
                    removeAnnotations ();
                    return;
                }
                for (int x = 0; x < stack.length; x++) {
                    if (csf.equals(stack[x])) {
                        for (int xx = x + 1; xx < stack.length; xx++) {
                            if (stack[xx].getLineNumber(language) >= 1) {
                                frameToShow = stack[xx];
                                break;
                            }
                        } // for
                        break;
                    } // if
                } // for
            } // if
            sourcePath.showSource (frameToShow, language);
        }
        final int lineNumber = currentThread.getLineNumber (language);
        final String url = getTheURL(sourcePath, currentThread, language);
        annotateCurrentPosition(currentThread, sourcePath, csf, language, url, lineNumber);
        annotateCallStack (currentThread, stack, sourcePath);
    }
    
    private static final String PROP_OPERATIONS_UPDATE = "operationsUpdate"; // NOI18N
    private static final String PROP_OPERATIONS_SET = "operationsSet"; // NOI18N
    
    private void annotateCurrentPosition(final JPDAThread currentThread,
                                         final SourcePath sourcePath,
                                         final CallStackFrame csf, final String language,
                                         final String url, final int lineNumber) {
        final Runnable updateCurrentAnnotation = new Runnable () {
            @Override
            public void run () {
                // show current line
                synchronized (currentPCLock) {
                    if (currentPC != null)
                        EditorContextBridge.getContext().removeAnnotation (currentPC);
                    if (csf != null && sourcePath != null && currentThread != null && url != null && lineNumber >= 0) {
                        // annotate current line
                        currentPC = sourcePath.annotate (currentThread, language, url, lineNumber);
                    } else {
                        currentPC = null;
                    }
                }
            }
        };
        PropertyChangeListener operationsUpdateListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (PROP_OPERATIONS_UPDATE.equals(name)) {
                    SwingUtilities.invokeLater (updateCurrentAnnotation);
                }
                if (PROP_OPERATIONS_SET.equals(name)) {
                    ((BeanContextChild) currentThread).removePropertyChangeListener(PROP_OPERATIONS_UPDATE, this);
                    ((BeanContextChild) currentThread).removePropertyChangeListener(PROP_OPERATIONS_SET, this);
                }
            }
        };
        ((BeanContextChild) currentThread).addPropertyChangeListener(PROP_OPERATIONS_UPDATE, operationsUpdateListener);
        ((BeanContextChild) currentThread).addPropertyChangeListener(PROP_OPERATIONS_SET, operationsUpdateListener);
        SwingUtilities.invokeLater (updateCurrentAnnotation);
    }

    private String getTheURL(SourcePath sourcePath, JPDAThread currentThread, String language) {
        return sourcePath.getURL(currentThread, language);
//        final String url;
//        String sPath;
//        try {
//            sPath = currentThread.getSourcePath (language);
//        } catch (AbsentInformationException e) {
//            sPath = "";
//        }
//        if (sPath.length() > 0) {
//            url = sourcePath.getURL (SourcePath.convertSlash (sPath), true);
//        } else {
//            String className = currentThread.getClassName ();
//            if (className.length() == 0) {
//                url = null;
//            } else {
//                url = sourcePath.getURL (SourcePath.convertClassNameToRelativePath (className), true);
//            }
//        }
//        return url;
    }

    private void showCurrentFrame(final CallStackFrame frame) {
        if (frame == null) return ;
        final SourcePath sp;
        JPDADebugger dbg;
        synchronized (this) {
            sp = currentSourcePath;
            dbg = currentDebugger;
        }
        if (sp != null && dbg != null) {
            final Session s;
            try {
                s = (Session) dbg.getClass().getMethod("getSession").invoke(dbg);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
                return ;
            }
            sp.showSource(frame, s.getCurrentLanguage());
        }
    }


    // do not need synchronization, called in a 1-way RP
    private HashMap               stackAnnotations = new HashMap ();
    
    private final RequestProcessor rp = new RequestProcessor("Debugger Thread Annotation Refresher");

    // currently waiting / running refresh task
    // there is at most one
    private RequestProcessor.Task taskRemove;
    private RequestProcessor.Task taskAnnotate;
    private JPDAThread threadToAnnotate;
    private CallStackFrame[] stackToAnnotate;
    private SourcePath sourcePathToAnnotate;

    private void removeAnnotations () {
        synchronized (rp) {
            if (taskRemove == null) {
                taskRemove = rp.create (new RemoveAnnotationsTask());
            }
            if (taskAnnotate != null) {
                taskAnnotate.cancel();
            }
        }
        taskRemove.schedule(ANNOTATION_SCHEDULE_TIME);
    }

    private void annotateCallStack (
        JPDAThread thread,
        CallStackFrame[] stack,
        SourcePath sourcePath
    ) {
        synchronized (rp) {
            if (taskRemove != null) {
                taskRemove.cancel();
            }
            this.threadToAnnotate = thread;
            this.stackToAnnotate = stack;
            this.sourcePathToAnnotate = sourcePath;
            if (taskAnnotate == null) {
                taskAnnotate = rp.create (new AnnotateCallStackTask());
            }
        }
        taskAnnotate.schedule(ANNOTATION_STACK_SCHEDULE_TIME);
    }
    
    private class RemoveAnnotationsTask implements Runnable {
        @Override
        public void run () {
            synchronized (currentPCLock) {
                if (currentPCSet) {
                    // Keep the set PC
                    return ;
                }
                if (currentPC != null)
                    EditorContextBridge.getContext().removeAnnotation (currentPC);
                currentPC = null;
            }
            Iterator i = stackAnnotations.values ().iterator ();
            while (i.hasNext ())
                EditorContextBridge.getContext().removeAnnotation (i.next ());
            stackAnnotations.clear ();
        }
    }
    
    private class AnnotateCallStackTask implements Runnable {
        @Override
        public void run () {
            CallStackFrame[] stack;
            SourcePath sourcePath;
            JPDAThread thread = null;
            synchronized (rp) {
                if (stackToAnnotate == null) {
                    if (threadToAnnotate != null) {
                        thread = threadToAnnotate;
                    } else {
                        return ; // Nothing to do
                    }
                }
                stack = stackToAnnotate;
                sourcePath = sourcePathToAnnotate;
                threadToAnnotate = null;
                stackToAnnotate = null;
                sourcePathToAnnotate = null;
            }
            if (thread != null) {
                try {
                    stack = thread.getCallStack();
                } catch (AbsentInformationException ex) {
                    // Nothing to annotate
                    return ;
                }

            }
            HashMap newAnnotations = new HashMap ();
            int i, k = stack.length;
            for (i = 1; i < k; i++) {

                // 1) check Line
                String language = stack[i].getDefaultStratum();                    
                String resourceName = EditorContextBridge.getRelativePath
                    (stack[i], language);
                int lineNumber = stack[i].getLineNumber (language);
                String line = resourceName + lineNumber;

                // 2) line already annotated?
                if (newAnnotations.containsKey (line))
                    continue;

                // 3) line has been annotated?
                Object da = stackAnnotations.remove (line);
                if (da == null) {
                    // line has not been annotated -> create annotation
                    da = sourcePath.annotate (stack[i], language);
                }

                // 4) add new line to hashMap
                if (da != null)
                    newAnnotations.put (line, da);
            } // for

            // delete old anotations
            Iterator iter = stackAnnotations.values ().iterator ();
            while (iter.hasNext ())
                EditorContextBridge.getContext().removeAnnotation (
                    iter.next ()
                );
            stackAnnotations = newAnnotations;
        }
    }
    
    private class AllThreadsAnnotator implements Runnable, PropertyChangeListener {
        
        private boolean active = true;
        private final JPDADebugger debugger;
        private final Map<JPDAThread, Object> threadAnnotations = new HashMap<JPDAThread, Object>();
        private final Set<JPDAThread> threadsToAnnotate = new HashSet<JPDAThread>();
        private final Map<JPDAThread, FutureAnnotation> futureAnnotations = new HashMap<JPDAThread, FutureAnnotation>();
        private final Set<Object> annotationsToRemove = new HashSet<Object>();
        private final RequestProcessor.Task task;
        
        @SuppressWarnings(value="LeakingThisInConstructor")
        public AllThreadsAnnotator(JPDADebugger debugger) {
            this.debugger = debugger;
            task = CurrentThreadAnnotationListener.this.rp.create(this);

            //System.err.println("AllThreadsAnnotator("+Integer.toHexString(debugger.hashCode())+").NEW");
            for (JPDAThread t : debugger.getThreadsCollector().getAllThreads()) {
                add(t);
            }
        }
        
        private void add(JPDAThread t) {
            ((Customizer) t).addPropertyChangeListener(this);
            //System.err.println("AllThreadsAnnotator("+Integer.toHexString(debugger.hashCode())+").add("+t+")");
            annotate(t);
        }
        
        private void remove(JPDAThread t) {
            ((Customizer) t).removePropertyChangeListener(this);
            //System.err.println("AllThreadsAnnotator("+Integer.toHexString(debugger.hashCode())+").remove("+t+")");
            synchronized (this) {
                Object annotation = threadAnnotations.remove(t);
                if (annotation != null) {
                    threadsToAnnotate.remove(t);
                    annotationsToRemove.add(annotation);
                    task.schedule(ANNOTATION_SCHEDULE_TIME);
                }
            }
        }
        
        private synchronized void cancel() {
            active = false;
            //System.err.println("AllThreadsAnnotator("+Integer.toHexString(debugger.hashCode())+").CANCEL");
            for (JPDAThread t : new HashSet<JPDAThread>(threadAnnotations.keySet())) {
                remove(t);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (this) {
                if (!active) {
                    ((Customizer) evt.getSource()).removePropertyChangeListener(this);
                    return ;
                }
            }
            JPDAThread t = (JPDAThread) evt.getSource();
            annotate(t);
        }
        
        private void annotate(JPDAThread t) {
            annotate(t, t == debugger.getCurrentThread());
        }

        private void annotate(JPDAThread t, boolean isCurrentThread) {
            //System.err.println("annotate("+t+", "+isCurrentThread+")");
            synchronized(this) {
                Object annotation = threadAnnotations.remove(t);
                //System.err.println("SCHEDULE removal of "+annotation+" for "+t);
                if (annotation != null) {
                    threadsToAnnotate.remove(t);
                    annotationsToRemove.add(annotation);
                    task.schedule(ANNOTATION_SCHEDULE_TIME);
                }
                if (!isCurrentThread && t.isSuspended()) {
                    threadsToAnnotate.add(t);
                    FutureAnnotation future = futureAnnotations.get(t);
                    if (future == null) {
                        future = new FutureAnnotation(t);
                    }
                    threadAnnotations.put(t, future);
                    futureAnnotations.put(t, future);
                    task.schedule(ANNOTATION_SCHEDULE_TIME);
                    //System.err.println("SCHEDULE annotation of "+t+", have future = "+future);
                }
            }
        }
        
        @Override
        @SuppressWarnings(value={"LocalVariableHidesMemberVariable"})
        public void run() {
            Set<Object> annotationsToRemove;
            Set<JPDAThread> threadsToAnnotate;
            Map<JPDAThread, FutureAnnotation> futureAnnotations;
            SourcePath theCurrentSourcePath;
            synchronized (this) {
                //System.err.println("TASK threadsToAnnotate: "+this.threadsToAnnotate);
                annotationsToRemove = new HashSet<Object>(this.annotationsToRemove);
                this.annotationsToRemove.clear();
                threadsToAnnotate = new HashSet<JPDAThread>(this.threadsToAnnotate);
                this.threadsToAnnotate.clear();
                futureAnnotations = new HashMap<JPDAThread, FutureAnnotation>(this.futureAnnotations);
                this.futureAnnotations.clear();
                theCurrentSourcePath = currentSourcePath;
                /*for (JPDAThread t : threadsToAnnotate) {
                    FutureAnnotation future = (FutureAnnotation) this.threadAnnotations.get(t);
                    //this.threadAnnotations.put(t, future);
                    futureAnnotations.put(t, future);
                }*/
                //System.err.println("TASK: annotationsToRemove = "+annotationsToRemove);
                //System.err.println("      threadsToAnnotate = "+threadsToAnnotate);
            }
            for (Object annotation : annotationsToRemove) {
                if (annotation instanceof FutureAnnotation) {
                    annotation = ((FutureAnnotation) annotation).getAnnotation();
                    if (annotation == null) {
                        continue;
                    }
                }
                EditorContextBridge.getContext().removeAnnotation(annotation);
            }
            Map<JPDAThread, FutureAnnotation> threadFutureAnnotations = new HashMap<JPDAThread, FutureAnnotation>();
            Set<JPDAThread> removeFutures = new HashSet<JPDAThread>();
            Session s;
            try {
                s = (Session) debugger.getClass().getMethod("getSession").invoke(debugger);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                s = null;
            }
            String language = (s != null) ? s.getCurrentLanguage() : null;
            for (JPDAThread t : threadsToAnnotate) {
                Object annotation;
                if (theCurrentSourcePath != null) {
                    final int lineNumber = t.getLineNumber (language);
                    final String url = getTheURL(theCurrentSourcePath, t, language);
                    annotation = theCurrentSourcePath.annotate(t, language, url, lineNumber, false);
                } else {
                    annotation = null;
                }
                if (annotation != null) {
                    FutureAnnotation fa = futureAnnotations.get(t);
                    fa.setAnnotation(annotation);
                    threadFutureAnnotations.put(t, fa);
                } else {
                    removeFutures.add(t);
                }
            }
            synchronized (this) {
                //System.err.print("TASK annot: "+this.threadAnnotations.keySet()+" -> ");
                this.threadAnnotations.keySet().removeAll(removeFutures);
                //this.futureAnnotations.keySet().removeAll(removeFutures);
                for (Map.Entry<JPDAThread, FutureAnnotation> entry : threadFutureAnnotations.entrySet()) {
                    JPDAThread t = entry.getKey();
                    FutureAnnotation fa = entry.getValue();
                    if (!this.annotationsToRemove.contains(fa)) {
                        this.threadAnnotations.put(t, fa.getAnnotation());
                    }
                }

                //System.err.println(this.threadAnnotations.keySet());
                /*for (JPDAThread t : futureAnnotations.keySet()) {
                    futureAnnotations.get(t).setAnnotation(threadAnnotations.get(t));
                }*/
                //System.err.println("TASK: have annotations: "+threadAnnotations);
            }
        }
        
        private final class FutureAnnotation {
            
            private JPDAThread thread;
            private Object annotation;
            
            public FutureAnnotation(JPDAThread thread) {
                this.thread = thread;
            }
            
            public JPDAThread getThread() {
                return thread;
            }
            
            public void setAnnotation(Object annotation) {
                this.annotation = annotation;
            }
            
            public Object getAnnotation() {
                return annotation;
            }

            @Override
            public String toString() {
                return "Future annotation ("+annotation+") for "+thread;
            }
            
        }

    }
}
