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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.AbsentInformationException;
import java.awt.Color;
import java.awt.datatransfer.Transferable;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.DeadlockDetector;
import org.netbeans.api.debugger.jpda.DeadlockDetector.Deadlock;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.ThreadBreakpoint;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.ui.SourcePath;
import org.netbeans.modules.debugger.jpda.ui.debugging.DebuggingViewSupportImpl;
import org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThread;
import org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThreadGroup;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author martin
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types=ExtendedNodeModel.class,
                             position=400)
public class DebuggingNodeModel implements ExtendedNodeModel {

    public static final String CURRENT_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/CurrentThread"; // NOI18N
    public static final String RUNNING_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/RunningThread"; // NOI18N
    public static final String SUSPENDED_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/SuspendedThread"; // NOI18N
    public static final String CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame";
    public static final String CURRENT_CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/CurrentFrame";
    
    public static final String THREAD_AT_BRKT_LINE = 
            "org/netbeans/modules/debugger/resources/threadsView/thread_at_line_bpkt_16.png";
    public static final String THREAD_AT_BRKT_NONLINE = 
            "org/netbeans/modules/debugger/resources/threadsView/thread_at_non_line_bpkt_16.png";
    public static final String THREAD_AT_BRKT_CONDITIONAL = 
            "org/netbeans/modules/debugger/resources/threadsView/thread_at_conditional_bpkt_16.png";
    public static final String THREAD_SUSPENDED = 
            "org/netbeans/modules/debugger/resources/threadsView/thread_suspended_16.png";
    public static final String THREAD_RUNNING = 
            "org/netbeans/modules/debugger/resources/threadsView/thread_running_16.png";
    public static final String THREAD_ZOMBIE = 
            "org/netbeans/modules/debugger/resources/threadsView/thread_zombie_16.png";
    public static final String CALL_STACK2 =
            "org/netbeans/modules/debugger/resources/threadsView/call_stack_16.png";
    
    public static final String THREAD_GROUP_MIXED =
            "org/netbeans/modules/debugger/resources/debuggingView/thread_group_mixed_16.png";
    public static final String THREAD_GROUP_SUSPENDED =
            "org/netbeans/modules/debugger/resources/debuggingView/thread_group_suspended_16.png";
    public static final String THREAD_GROUP_RESUMED =
            "org/netbeans/modules/debugger/resources/debuggingView/thread_group_running_16.png";
    
    public static final String SHOW_PACKAGE_NAMES = "show.packageNames";

    private final JPDADebugger debugger;
    private final DebuggingViewSupportImpl dvSupport;
    
    private final List<ModelListener> listeners = new ArrayList<ModelListener>();
    
    private final Map<JPDAThread, ThreadStateUpdater> threadStateUpdaters = new WeakHashMap<JPDAThread, ThreadStateUpdater>();
    private final CurrentThreadListener currentThreadListener;
    private final DeadlockDetector deadlockDetector;
    private final Set nodesInDeadlock = new HashSet();
    private static final Map<JPDADebugger, Set> nodesInDeadlockByDebugger = new WeakHashMap<JPDADebugger, Set>();
    private final Preferences preferences = DebuggingViewSupportImpl.getFilterPreferences();
    private final PreferenceChangeListener prefListener;
    private final RequestProcessor rp;
    private final SourcePath sourcePath;
    private final Session session;
    private final PropertyChangeListener sessionLanguageListener;
    
    public DebuggingNodeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        dvSupport = (DebuggingViewSupportImpl) lookupProvider.lookupFirst(null, DebuggingView.DVSupport.class);
        currentThreadListener = new CurrentThreadListener();
        debugger.addPropertyChangeListener(WeakListeners.propertyChange(currentThreadListener, debugger));
        deadlockDetector = debugger.getThreadsCollector().getDeadlockDetector();
        deadlockDetector.addPropertyChangeListener(new DeadlockListener());
        rp = lookupProvider.lookupFirst(null, RequestProcessor.class);
        sourcePath = lookupProvider.lookupFirst(null, SourcePath.class);
        session = lookupProvider.lookupFirst(null, Session.class);
        sessionLanguageListener = new SessionLanguageListener();
        session.addPropertyChangeListener(Session.PROP_CURRENT_LANGUAGE,
                WeakListeners.propertyChange(sessionLanguageListener,
                                             new ListenerDetaching(Session.PROP_CURRENT_LANGUAGE, session)));
        prefListener = new DebuggingPreferenceChangeListener();
        preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefListener, preferences));
    }
    
    public static Set getNodesInDeadlock(JPDADebugger debugger) {
        synchronized (nodesInDeadlockByDebugger) {
            return nodesInDeadlockByDebugger.get(debugger);
        }
    }

    public String getDisplayName(Object node) throws UnknownTypeException {
        if (TreeModel.ROOT.equals(node)) {
            return ""; // NOI18N
        }
        boolean showPackageNames = preferences.getBoolean(SHOW_PACKAGE_NAMES, false);
        Color c = null;
        synchronized (nodesInDeadlock) {
            if (nodesInDeadlock.contains(node)) {
                c = Color.RED;
            }
        }
        if (node instanceof JPDADVThread) {
            JPDAThread t = ((JPDADVThread) node).getKey();
            watch(t);
            JPDAThread currentThread = debugger.getCurrentThread();
            if (t == currentThread && (!DebuggingTreeExpansionModelFilter.isExpanded(debugger, node) ||
                    !t.isSuspended())) {
                return BoldVariablesTableModelFilter.toHTML(
                        getDisplayName(t, showPackageNames, this),
                        true, false, c);
            } else {
                if (c != null) {
                    return BoldVariablesTableModelFilter.toHTML(
                        getDisplayName(t, showPackageNames, this),
                        false, false, c);
                } else {
                    return getDisplayName(t, showPackageNames, this);
                }
            }
        }
        if (node instanceof JPDADVThreadGroup) {
            JPDAThreadGroup group = ((JPDADVThreadGroup) node).getKey();
            if (isCurrent(group) && !DebuggingTreeExpansionModelFilter.isExpanded(debugger, node)) {
                return BoldVariablesTableModelFilter.toHTML (
                    group.getName (),
                    true,
                    false,
                    null
                );
            } else {
                return group.getName ();
            }
        }
        if (node instanceof CallStackFrame) {
            CallStackFrame f = (CallStackFrame) node;
            boolean isCurrent;
            try {
                isCurrent = (Boolean) f.getClass().getMethod("isCurrent").invoke(f);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                isCurrent = false;
            }
            // Do not call JDI in AWT
            //CallStackFrame currentFrame = debugger.getCurrentCallStackFrame();
            //if (f.equals(currentFrame)) {
            String frameDescr;
            synchronized (frameDescriptionsByFrame) {
                frameDescr = frameDescriptionsByFrame.get(f);
                if (frameDescr == null) {
                    loadFrameDescription(f, showPackageNames);
                    return BoldVariablesTableModelFilter.toHTML(
                            NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Frame_Loading"),
                            false,
                            false,
                            Color.LIGHT_GRAY);
                }
            }
            if (isCurrent) {
                return BoldVariablesTableModelFilter.toHTML(
                        frameDescr,
                        true, false, c);
            } else {
                if (c != null) {
                    return BoldVariablesTableModelFilter.toHTML(
                            frameDescr,
                            false, false, c);
                } else {
                    return frameDescr;
                }
            }
        }
        throw new UnknownTypeException(node.toString());
    }

    /**
     * Map of threads and their frame descriptions.
     * These are loaded lazily, since we must not load call stack frames in AWT EQ.
     */
    private static final Map<JPDAThread, String> frameDescriptionsByThread = new WeakHashMap<JPDAThread, String>();

    private final Map<CallStackFrame, String> frameDescriptionsByFrame = new WeakHashMap<CallStackFrame, String>();

    private static final Map<CallStackFrame, FrameUIInfo> framePathClassURL = new WeakHashMap<CallStackFrame, FrameUIInfo>();
    
    public static String getDisplayName(JPDAThread t, boolean showPackageNames) throws UnknownTypeException {
        return getDisplayName(t, showPackageNames, null);
    }

    private static String getDisplayName(JPDAThread t, boolean showPackageNames, DebuggingNodeModel model) throws UnknownTypeException {
        String name = t.getName();
        JPDABreakpoint breakpoint = t.getCurrentBreakpoint();
        if (((JPDAThreadImpl) t).isMethodInvoking()) {
            return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_Invoking_Method", name);
        }
        if (breakpoint != null) {
            return getThreadAtBreakpointDisplayName(name, breakpoint);
        }
        if (t.isSuspended()) {
            String frame;
            synchronized (frameDescriptionsByThread) {
                frame = frameDescriptionsByThread.get(t);
                if (t.isSuspended()) {
                    // Load it in any case to assure refreshes
                    loadFrameDescription(frame, t, showPackageNames, model);
                }
            }
            if (frame != null) {
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Suspended_At", name, frame);
            } else {
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Suspended", name);
            }
        } else if (JPDAThread.STATE_ZOMBIE == t.getState()) {
            // Died, but is still around
            return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Zombie", name);
        } else {
            return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Running", name);
        }
        /*
        int i = t.getState ();
        switch (i) {
            case JPDAThread.STATE_UNKNOWN:
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Unknown", name);
            case JPDAThread.STATE_MONITOR:
                if (frame != null) {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Monitor_At", name, frame);
                } else {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Monitor", name);
                }
            case JPDAThread.STATE_NOT_STARTED:
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_NotStarted", name);
            case JPDAThread.STATE_RUNNING:
                if (frame != null) {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Running_At", name, frame);
                } else {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Running", name);
                }
            case JPDAThread.STATE_SLEEPING:
                if (frame != null) {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Sleeping_At", name, frame);
                } else {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Sleeping", name);
                }
            case JPDAThread.STATE_WAIT:
                if (frame != null) {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Waiting_At", name, frame);
                } else {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Waiting", name);
                }
            case JPDAThread.STATE_ZOMBIE:
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Zombie", name);
            default:
                Exceptions.printStackTrace(new IllegalStateException("Unexpected thread state: "+i+" of "+t));
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Unknown", name);
        }
         */
    }

    private static String getThreadAtBreakpointDisplayName(String threadName, JPDABreakpoint breakpoint) {
        if (breakpoint instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) breakpoint;
            String fileName = null;
            String urlStr = lb.getURL();
            if (urlStr.isEmpty()) {
                fileName = lb.getPreferredClassName();
                if (fileName != null) {
                    int i = fileName.lastIndexOf('.');
                    if (i > 0) {
                        fileName = fileName.substring(i+1);
                    }
                }
            } else {
                try {
                    FileObject fo = URLMapper.findFileObject(new URL(urlStr));
                    if (fo != null) {
                        fileName = fo.getNameExt();
                    }
                } catch (MalformedURLException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            if (fileName == null) fileName = lb.getURL();
            return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_At_LineBreakpoint",
                                       threadName, fileName, lb.getLineNumber());
        }
        if (breakpoint instanceof MethodBreakpoint) {
            MethodBreakpoint mb = (MethodBreakpoint) breakpoint;
            String classFilters = java.util.Arrays.asList(mb.getClassFilters()).toString();
            if (mb.getMethodSignature() == null) {
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_At_MethodBreakpoint",
                                           threadName, classFilters, mb.getMethodName());
            } else {
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_At_MethodBreakpointSig",
                                           new Object[] { threadName, classFilters, mb.getMethodName(), mb.getMethodSignature() });
            }
        }
        if (breakpoint instanceof ExceptionBreakpoint) {
            ExceptionBreakpoint eb = (ExceptionBreakpoint) breakpoint;
            return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_At_ExceptionBreakpoint",
                                       threadName, eb.getExceptionClassName());
        }
        if (breakpoint instanceof FieldBreakpoint) {
            FieldBreakpoint fb = (FieldBreakpoint) breakpoint;
            return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_At_FieldBreakpoint",
                                       threadName, fb.getClassName(), fb.getFieldName());
        }
        if (breakpoint instanceof ThreadBreakpoint) {
            //ThreadBreakpoint tb = (ThreadBreakpoint) breakpoint;
            return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_At_ThreadBreakpoint",
                                       threadName);
        }
        if (breakpoint instanceof ClassLoadUnloadBreakpoint) {
            ClassLoadUnloadBreakpoint cb = (ClassLoadUnloadBreakpoint) breakpoint;
            String classFilters = java.util.Arrays.asList(cb.getClassFilters()).toString();
            return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_At_ClassBreakpoint",
                                       threadName, classFilters);
        }
        return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_At_Breakpoint", threadName, breakpoint.toString());
    }

    private static void loadFrameDescription(final String oldFrame,
                                             final JPDAThread t,
                                             final boolean showPackageNames,
                                             final DebuggingNodeModel model) {
        final Session s;
        try {
            JPDADebugger debugger = (JPDADebugger) t.getClass().getMethod("getDebugger").invoke(t);
            s = (Session) debugger.getClass().getMethod("getSession").invoke(debugger);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return ;
        }
        RequestProcessor rp;
        if (model != null && model.rp != null) {
            rp = model.rp;
        } else {
            rp = s.lookupFirst(null, RequestProcessor.class);
        }
        if (rp == null) {
            // Debugger is finishing
            return ;
        }
        rp.post(new Runnable() {
            public void run() {
                String frame = null;
                t.getReadAccessLock().lock();
                try {
                    if (t.isSuspended () && (t.getStackDepth () > 0)) {
                        try {
                            CallStackFrame[] sf = t.getCallStack (0, 1);
                            if (sf.length > 0) {
                                frame = CallStackNodeModel.getCSFName (s, sf[0], showPackageNames);
                            }
                        } catch (AbsentInformationException e) {
                        }
                    }
                } finally {
                    t.getReadAccessLock().unlock();
                }
                if (oldFrame == null && frame != null || oldFrame != null && !oldFrame.equals(frame)) {
                    synchronized (frameDescriptionsByThread) {
                        frameDescriptionsByThread.put(t, frame);
                    }
                    if (model != null) {
                        model.fireDisplayNameChanged(t);
                    }
                }
            }
        });
    }

    private void loadFrameDescription(final CallStackFrame f, final boolean showPackageNames) {
        rp.post(new Runnable() {
            @Override
            public void run() {
                String frameDescr = CallStackNodeModel.getCSFName(session, f, showPackageNames);
                synchronized (frameDescriptionsByFrame) {
                    frameDescriptionsByFrame.put(f, frameDescr);
                }
                FrameUIInfo frameInfo = new FrameUIInfo();
                String language = session.getCurrentLanguage();
                try {
                    frameInfo.sourcePath = f.getSourcePath(language);
                } catch (AbsentInformationException ex) {
                }
                frameInfo.className = f.getClassName();
                frameInfo.url = sourcePath.getURL(f, language);
                frameInfo.language = language;
                if (frameInfo.url == null) {
                    // URL is not known for this language. Try other languages...
                    List<String> supportedLanguages = f.getAvailableStrata();
                    String otherLanguage = null;
                    String otherURL = null;
                    for (String ol : supportedLanguages) {
                        if (ol.equals(language)) {
                            continue;
                        }
                        otherURL = sourcePath.getURL(f, ol);
                        if (otherURL != null) {
                            otherLanguage = ol;
                            break;
                        }
                    }
                    if (otherURL != null) {
                        frameInfo.url = otherURL;
                        frameInfo.language = otherLanguage;
                    }
                }
                synchronized (framePathClassURL) {
                    framePathClassURL.put(f, frameInfo);
                }
                fireDisplayNameChanged(f);
            }
        });
    }

    static String getCachedFramePath(CallStackFrame f) {
        synchronized (framePathClassURL) {
            FrameUIInfo frameInfo = framePathClassURL.get(f);
            if (frameInfo != null) {
                return frameInfo.sourcePath;
            }
        }
        return null;
    }

    static String getCachedFrameClass(CallStackFrame f) {
        synchronized (framePathClassURL) {
            FrameUIInfo frameInfo = framePathClassURL.get(f);
            if (frameInfo != null) {
                return frameInfo.className;
            }
        }
        return null;
    }
    
    static String getCachedFrameURL(CallStackFrame f) {
        synchronized (framePathClassURL) {
            FrameUIInfo frameInfo = framePathClassURL.get(f);
            if (frameInfo != null) {
                return frameInfo.url;
            }
        }
        return null;
    }
    
    static String getCachedFrameURLLanguage(CallStackFrame f) {
        synchronized (framePathClassURL) {
            FrameUIInfo frameInfo = framePathClassURL.get(f);
            if (frameInfo != null) {
                return frameInfo.language;
            }
        }
        return null;
    }

    public static String getIconBase(JPDAThread thread) {
        Breakpoint b = thread.getCurrentBreakpoint();
        if (b != null) {
            if (b instanceof LineBreakpoint) {
                String condition = ((LineBreakpoint) b).getCondition();
                if (condition != null && condition.length() > 0) {
                    return THREAD_AT_BRKT_CONDITIONAL;
                } else {
                    return THREAD_AT_BRKT_LINE;
                }
            } else {
                return THREAD_AT_BRKT_NONLINE;
            }
        }
        if (thread.isSuspended()) {
            return THREAD_SUSPENDED;
        } else if (JPDAThread.STATE_ZOMBIE == thread.getState()) {
            return THREAD_ZOMBIE;
        } else {
            return THREAD_RUNNING;
        }
    }
    
    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node instanceof CallStackFrame) {
            CallStackFrame ccsf = debugger.getCurrentCallStackFrame ();
            if ((ccsf != null) &&  (ccsf.equals (node))) {
                return CURRENT_CALL_STACK;
            }
            return CALL_STACK;
        }
        if (node instanceof JPDADVThread) {
            JPDADVThread dvt = (JPDADVThread) node;
            if (dvt.getKey() == debugger.getCurrentThread ()) {
                return CURRENT_THREAD;
            }
            return dvt.isSuspended () ? SUSPENDED_THREAD : RUNNING_THREAD;
        }
        if (node == TreeModel.ROOT) {
            return CALL_STACK; // will not be displayed
        }
        throw new UnknownTypeException (node);
    }

    @Override
    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node instanceof JPDADVThread) {
            return getIconBase(((JPDADVThread)node).getKey());
        }
        if (node instanceof CallStackFrame) {
            return CALL_STACK2;
        }
        if (node instanceof JPDADVThreadGroup) {
            boolean[] flags = new boolean[] {false, false};
            computeGroupStatus(((JPDADVThreadGroup) node).getKey(), flags);
            if (flags[0]) {
                // at least one thread suspended
                if (flags[1]) {
                    // mixed thread group
                    return THREAD_GROUP_MIXED;
                } else {
                    // only suspended threads
                    return THREAD_GROUP_SUSPENDED;
                }
            } else {
                return THREAD_GROUP_RESUMED;
            }
        }
        return getIconBase(node) + ".gif"; // NOI18N
    }

    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof JPDADVThread) {
            JPDAThread t = ((JPDADVThread) node).getKey();
            int i = t.getState ();
            String s = "";
            switch (i) {
                case JPDAThread.STATE_UNKNOWN:
                    s = NbBundle.getBundle (DebuggingNodeModel.class).getString
                        ("CTL_ThreadsModel_State_Unknown");
                    break;
                case JPDAThread.STATE_MONITOR:
                    s = "";
                    ObjectVariable ov = t.getContendedMonitor ();
                    if (ov == null)
                        s = NbBundle.getBundle (ThreadsNodeModel.class).
                            getString ("CTL_ThreadsModel_State_Monitor");
                    else
                        try {
                            s = java.text.MessageFormat.
                                format (
                                    NbBundle.getBundle (ThreadsNodeModel.class).
                                        getString (
                                    "CTL_ThreadsModel_State_ConcreteMonitor"), 
                                    new Object [] { ov.getToStringValue () });
                        } catch (InvalidExpressionException ex) {
                            s = ex.getLocalizedMessage ();
                        }
                    break;
                case JPDAThread.STATE_NOT_STARTED:
                    s = NbBundle.getBundle (DebuggingNodeModel.class).getString
                        ("CTL_ThreadsModel_State_NotStarted");
                    break;
                case JPDAThread.STATE_RUNNING:
                    s = NbBundle.getBundle (DebuggingNodeModel.class).getString
                        ("CTL_ThreadsModel_State_Running");
                    break;
                case JPDAThread.STATE_SLEEPING:
                    s = NbBundle.getBundle (DebuggingNodeModel.class).getString
                        ("CTL_ThreadsModel_State_Sleeping");
                    break;
                case JPDAThread.STATE_WAIT:
                    ov = t.getContendedMonitor ();
                    if (ov == null)
                        s = NbBundle.getBundle (DebuggingNodeModel.class).
                            getString ("CTL_ThreadsModel_State_Waiting");
                    else
                        try {
                            s = java.text.MessageFormat.format
                                (NbBundle.getBundle (DebuggingNodeModel.class).
                                getString ("CTL_ThreadsModel_State_WaitingOn"), 
                                new Object [] { ov.getToStringValue () });
                        } catch (InvalidExpressionException ex) {
                            s = ex.getLocalizedMessage ();
                        }
                    break;
                case JPDAThread.STATE_ZOMBIE:
                    s = NbBundle.getBundle (DebuggingNodeModel.class).getString
                        ("CTL_ThreadsModel_State_Zombie");
                    break;
            }
            String msg;
            if (t.isSuspended()) {
                msg = NbBundle.getMessage(DebuggingNodeModel.class,
                        "CTL_ThreadsModel_Suspended_Thread_Desc");
            } else {
                msg = NbBundle.getMessage(DebuggingNodeModel.class,
                        "CTL_ThreadsModel_Resumed_Thread_Desc");
            }
            if (s != null && s.length() > 0) {
                msg = "<html>" + msg + "<br>" + NbBundle.getMessage(DebuggingNodeModel.class,
                        "CTL_ThreadsModel_Thread_State_Desc", s) + "</html>";
            }
            return msg;
        }
        if (node instanceof CallStackFrame) {
            CallStackFrame sf = (CallStackFrame) node;
            if (((JPDAThreadImpl) sf.getThread()).isMethodInvoking()) {
                return "";
            }
            return CallStackNodeModel.getCSFToolTipText(session, sf);
        }
        if (node instanceof JPDAThreadGroup) {
            return ((JPDAThreadGroup) node).getName ();
        }
        if (node == TreeModel.ROOT) {
            return ""; // NOI18N
        }
        throw new UnknownTypeException(node.toString());
    }

    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public boolean canCopy(Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCut(Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canRename(Object node) throws UnknownTypeException {
        return false;
    }

    public void setName(Object node, String name) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    private void fireNodeChanged (Object node) {
        if (node instanceof JPDAThread) {
            node = dvSupport.get((JPDAThread) node);
        } else if (node instanceof JPDAThreadGroup) {
            node = dvSupport.get((JPDAThreadGroup) node);
        }
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        ModelEvent event;
        if (node instanceof JPDAThread/* && DebuggingTreeModel.isMethodInvoking((JPDAThread) node)*/) {
            event = new ModelEvent.NodeChanged(this, node,
                    ModelEvent.NodeChanged.DISPLAY_NAME_MASK |
                    ModelEvent.NodeChanged.ICON_MASK |
                    ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK);
        } else {
            event = new ModelEvent.NodeChanged(this, node,
                    ModelEvent.NodeChanged.DISPLAY_NAME_MASK |
                    ModelEvent.NodeChanged.ICON_MASK |
                    ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK |
                    ModelEvent.NodeChanged.CHILDREN_MASK |
                    ModelEvent.NodeChanged.EXPANSION_MASK);
        }
        for (ModelListener ml : ls) {
            ml.modelChanged (event);
        }
    }
    
    private void fireDisplayNameChanged (Object node) {
        if (node instanceof JPDAThread) {
            node = dvSupport.get((JPDAThread) node);
        } else if (node instanceof JPDAThreadGroup) {
            node = dvSupport.get((JPDAThreadGroup) node);
        }
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        ModelEvent event = new ModelEvent.NodeChanged(this, node,
                ModelEvent.NodeChanged.DISPLAY_NAME_MASK);
        for (ModelListener ml : ls) {
            ml.modelChanged (event);
        }
    }

    private void fireTreeChanged() {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        ModelEvent event = new ModelEvent.TreeChanged(this);
        for (ModelListener ml : ls) {
            ml.modelChanged (event);
        }
    }
    
    private void watch(JPDAThread t) {
        synchronized (threadStateUpdaters) {
            if (!threadStateUpdaters.containsKey(t)) {
                threadStateUpdaters.put(t, new ThreadStateUpdater(t));
            }
        }
    }
    
    private boolean isCurrent(JPDAThreadGroup tg) {
        JPDAThread t = debugger.getCurrentThread ();
        if (t == null)
            return false;
        JPDAThreadGroup ctg = t.getParentThreadGroup ();
        while (ctg != null) {
            if (ctg == tg) return true;
            ctg = ctg.getParentThreadGroup ();
        }
        return false;
    }
    
    /**
     * @param tg thread group to inspect
     * @param flags flags[0] true if there is at least one suspended thread,
     *     flags[1] true if there is at least one resumed thread
     */
    private void computeGroupStatus(JPDAThreadGroup tg, boolean[] flags) {
        JPDAThread[] threads = tg.getThreads();
        for (int x = 0; x < threads.length; x++) {
            if (threads[x].isSuspended()) {
                flags[0] = true; // set 'suspended' flag
            } else {
                flags[1] = true; // set 'resumed' flag
            }
            if (flags[0] && flags[1]) {
                return; // mixed group detected
            }
        }
        JPDAThreadGroup[] groups = tg.getThreadGroups();
        for (int x = 0; x < groups.length; x++) {
            computeGroupStatus(groups[x], flags);
            if (flags[0] && flags[1]) {
                return; // mixed group detected
            }
        }
    }
    
    private class ThreadStateUpdater implements PropertyChangeListener {
        
        private Reference<JPDAThread> tr;
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        private boolean shouldExpand = false;
        
        public ThreadStateUpdater(JPDAThread t) {
            this.tr = new WeakReference(t);
            ((Customizer) t).addPropertyChangeListener(WeakListeners.propertyChange(this, t));
        }

        public void propertyChange(PropertyChangeEvent evt) {
            JPDAThread t = tr.get();
            if (t != null) {
                //if (DebuggingTreeModel.isMethodInvoking(t)) return ;
                if (JPDAThread.PROP_BREAKPOINT.equals(evt.getPropertyName()) &&
                    t.isSuspended() && t.getCurrentBreakpoint() != null) {
                    synchronized (this) {
                        shouldExpand = true;
                    }
                }
                synchronized (this) {
                    if (task == null) {
                        task = rp.create(new Refresher());
                    }
                    task.schedule(100);
                }
            }
        }
        
        private class Refresher extends Object implements Runnable {
            public void run() {
                JPDAThread thread = tr.get();
                if (thread != null) {
                    if (preferences.getBoolean(DebuggingTreeModel.SHOW_SUSPENDED_THREADS_ONLY, false)) {
                        fireNodeChanged(TreeModel.ROOT);
                    } else {
                        fireNodeChanged(thread);
                    }
                    boolean shouldExpand;
                    synchronized (this) {
                        shouldExpand = ThreadStateUpdater.this.shouldExpand;
                        ThreadStateUpdater.this.shouldExpand = false;
                    }
                    if (shouldExpand) {
                        DebuggingTreeExpansionModelFilter.expand(debugger, dvSupport.get(thread));
                    }
                    if (preferences.getBoolean(DebuggingTreeModel.SHOW_THREAD_GROUPS, false)) {
                        JPDAThreadGroup group = thread.getParentThreadGroup();
                        while (group != null) {
                            fireNodeChanged(group);
                            group = group.getParentThreadGroup();
                        } // while
                    } // if
                } // if
            } // run
        }
    }
    
    private class CurrentThreadListener implements PropertyChangeListener {
        
        private Reference<JPDAThread> lastCurrentThreadRef = new WeakReference<JPDAThread>(null);
        //private Reference<CallStackFrame> lastCurrentFrameRef = new WeakReference<CallStackFrame>(null);
        //private CallStackFrame lastCurrentFrame = null;
        private Reference<JPDAThread> lastCurrentFrameThreadRef = new WeakReference<JPDAThread>(null);
        private int lastCurrentFrameDepth;

        public void propertyChange(PropertyChangeEvent evt) {
            if (JPDADebugger.PROP_CURRENT_THREAD.equals(evt.getPropertyName())) {
                JPDAThread currentThread = debugger.getCurrentThread();
                JPDAThread lastCurrentThread;
                synchronized (this) {
                    lastCurrentThread = lastCurrentThreadRef.get();
                    lastCurrentThreadRef = new WeakReference(currentThread);
                }
                if (lastCurrentThread != null) {
                    fireNodeChanged(lastCurrentThread);
                }
                if (currentThread != null) {
                    fireNodeChanged(currentThread);
                }
                if (preferences.getBoolean(DebuggingTreeModel.SHOW_SUSPENDED_THREADS_ONLY, false)) {
                    fireNodeChanged(TreeModel.ROOT);
                }
            }
            if (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME.equals(evt.getPropertyName())) {
                CallStackFrame currentFrame = debugger.getCurrentCallStackFrame();
                CallStackFrame lastcurrentFrame = null;
                JPDAThread lastCurrentFrameThread;
                synchronized (this) {
                    lastCurrentFrameThread = lastCurrentFrameThreadRef.get();
                }
                if (lastCurrentFrameThread != null) {
                    try {
                        CallStackFrame[] frames = lastCurrentFrameThread.getCallStack(lastCurrentFrameDepth, lastCurrentFrameDepth + 1);
                        if (frames.length > 0) {
                            lastcurrentFrame = frames[0];
                        }
                    } catch (AbsentInformationException aiex) {}
                }
                synchronized (this) {
                    //lastcurrentFrame = lastCurrentFrame;//Ref.get();
                    //lastCurrentFrameRef = new WeakReference(currentFrame);
                    //lastCurrentFrame = currentFrame;
                    if (currentFrame != null) {
                        lastCurrentFrameThreadRef = new WeakReference(currentFrame.getThread());
                        lastCurrentFrameDepth = currentFrame.getFrameDepth();
                    } else {
                        lastCurrentFrameThreadRef = new WeakReference(null);
                        lastCurrentFrameDepth = 0;
                    }
                }
                if (lastcurrentFrame != null) {
                    fireNodeChanged(lastcurrentFrame);
                }
                if (currentFrame != null) {
                    fireNodeChanged(currentFrame);
                }
            }
        }
        
    }
    
    private class DeadlockListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            Set<Deadlock> deadlocks = deadlockDetector.getDeadlocks();
            Set deadlockedElements = new HashSet();
            for (Deadlock deadlock : deadlocks) {
                for (JPDAThread t : deadlock.getThreads()) {
                    deadlockedElements.add(dvSupport.get(t));
                    deadlockedElements.add(t.getContendedMonitor());
                }
            }
            if (deadlockedElements.isEmpty()) {
                return ;
            }
            synchronized (nodesInDeadlock) {
                nodesInDeadlock.addAll(deadlockedElements);
            }
            synchronized (nodesInDeadlockByDebugger) {
                nodesInDeadlockByDebugger.put(debugger, nodesInDeadlock);
            }
            for (Object node : deadlockedElements) {
                fireDisplayNameChanged(node);
                DebuggingTreeExpansionModelFilter.expand(debugger, node);
            }
            //fireNodeChanged(TreeModel.ROOT);
        }
        
    }
    
    private final class DebuggingPreferenceChangeListener implements PreferenceChangeListener {

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            if (DebuggingNodeModel.SHOW_PACKAGE_NAMES.equals(key)) {
                synchronized (frameDescriptionsByFrame) {
                    frameDescriptionsByFrame.clear();
                }
            }
        }
    }

    private final class SessionLanguageListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (frameDescriptionsByFrame) {
                frameDescriptionsByFrame.clear();
            }
            synchronized (framePathClassURL) {
                framePathClassURL.clear();
            }
            fireTreeChanged();
        }
        
    }

    private static final class ListenerDetaching {

        private String propertyName;
        private Session session;

        ListenerDetaching(String propertyName, Session session) {
            this.propertyName = propertyName;
            this.session = session;
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            session.removePropertyChangeListener(propertyName, l);
        }
    }
    
    private static final class FrameUIInfo {
        String sourcePath;
        String className;
        String url;
        String language;
    }
}
