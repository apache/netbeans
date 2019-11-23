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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.VMDisconnectedException;
import java.awt.Color;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.DeadlockDetector;
import org.netbeans.api.debugger.jpda.DeadlockDetector.Deadlock;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MonitorInfo;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.ui.debugging.DebuggingViewSupportImpl;
import org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThread;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.openide.util.datatransfer.PasteType;


/**
 * @author   Jan Jancura
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types=Model.class,
                             position=600)
public class DebuggingMonitorModel implements ExtendedNodeModelFilter, 
NodeActionsProviderFilter, TableModel, Constants {
    
    public static final String SHOW_MONITORS = "show.monitors"; // NOI18N

    public static final String CONTENDED_MONITOR =
        "org/netbeans/modules/debugger/resources/allInOneView/waiting_on_monitor_16.png"; // NOI18N
    public static final String OWNED_MONITORS =
        "org/netbeans/modules/debugger/resources/allInOneView/monitor_acquired_16.png"; // NOI18N
    public static final String MONITOR =
        "org/netbeans/modules/debugger/resources/allInOneView/monitor_acquired_16.png"; // NOI18N

    private RequestProcessor evaluationRP;
    private final Collection modelListeners = new HashSet();
    private JPDADebugger debugger;
    
    public DebuggingMonitorModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        evaluationRP = lookupProvider.lookupFirst(null, RequestProcessor.class);
    }
    
    // TreeView impl............................................................
    // DebuggingTreeModel uses cached children, therefore we provide this class
    // as a children filter

    static class Children {

        private final JPDADebugger debugger;
        private final DebuggingViewSupportImpl dvSupport;
        private final Set<JPDAThread> threadsAskedForMonitors = new WeakSet<JPDAThread>();
        private final Set<CallStackFrame> framesAskedForMonitors = new WeakSet<CallStackFrame>();
        private final DeadlockDetector deadlockDetector;
        private Preferences preferences = DebuggingViewSupportImpl.getFilterPreferences();
        private PreferenceChangeListener prefListener;
        private RevertShowMonitorsListener revertShowMonitorsListener;

        private ModelListener modelListener;
        private DebuggingTreeModel modelEventSource;
    
        Children(JPDADebugger debugger, DebuggingViewSupportImpl dvSupport,
                 ModelListener modelListener, DebuggingTreeModel modelEventSource) {
            this.debugger = debugger;
            this.dvSupport = dvSupport;
            this.modelListener = modelListener;
            this.modelEventSource = modelEventSource;
            prefListener = new MonitorPreferenceChangeListener();
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefListener, preferences));
            deadlockDetector = debugger.getThreadsCollector().getDeadlockDetector();
            deadlockDetector.addPropertyChangeListener(new DeadlockListener());
        }

        public Object[] getChildren (
            TreeModel   model,
            Object      o,
            int         from,
            int         to
        ) throws UnknownTypeException {
            if (o instanceof JPDADVThread) {
                JPDAThread t = ((JPDADVThread) o).getKey();
                synchronized (threadsAskedForMonitors) {
                    threadsAskedForMonitors.add(t);
                }
                if (preferences.getBoolean(SHOW_MONITORS, false)) {
                    try {
                        ObjectVariable contended = t.getContendedMonitor ();
                        ObjectVariable[] owned;
                        List<MonitorInfo> mf = t.getOwnedMonitorsAndFrames();
                        List<Monitor> ownedMonitors;
                        if (mf.size() > 0) {
                            ownedMonitors = new ArrayList<Monitor>();
                            for (MonitorInfo m: mf) {
                                if (m.getFrame() == null) {
                                    ownedMonitors.add(new Monitor(m.getMonitor(), m.getFrame(), debugger));
                                }
                            }
                            owned = null;
                        } else {
                            owned = t.getOwnedMonitors ();
                            ownedMonitors = null;
                        }
                        ContendedMonitor cm = null;
                        OwnedMonitors om = null;
                        if (contended != null &&
                            (from  == 0) && (to > 0)) {
                            CallStackFrame f = null;
                            try {
                                CallStackFrame[] frames = t.getCallStack(0, 1);
                                if (frames != null && frames.length == 1) {
                                    f = frames[0];
                                }
                            } catch (AbsentInformationException aiex) {}
                            if (f != null) {
                                cm = new ContendedMonitor (contended, f, debugger);
                            }
                        }
                        if (ownedMonitors != null && ownedMonitors.size() > 0 &&
                             ( ((contended != null) && (from < 2) && (to > 1)) ||
                               ((contended == null) && (from == 0) && (to > 0)))) {
                            om = new OwnedMonitors(ownedMonitors.toArray(new Monitor[] {}));
                        }
                        if (owned != null && (owned.length > 0) &&
                             ( ((contended != null) && (from < 2) && (to > 1)) ||
                               ((contended == null) && (from == 0) && (to > 0))
                             )) {
                            om = new OwnedMonitors (owned);
                        }
                        int i = 0;
                        if (cm != null) i++;
                        if (om != null) i++;
                        Object[] os = new Object [i];
                        i = 0;
                        if (cm != null) os[i++] = cm;
                        if (om != null) os[i++] = om;
                        Object[] ch = model.getChildren(o, from, to);
                        if (i > 0) {
                            Object[] newCh = new Object[i + ch.length];
                            System.arraycopy(os, 0, newCh, 0, os.length);
                            System.arraycopy(ch, 0, newCh, i, ch.length);
                            ch = newCh;
                        }
                        return ch;
                    } catch (ObjectCollectedException e) {
                    } catch (VMDisconnectedException e) {
                    }
                }
                return model.getChildren(o, from, to);
            }
            /*if (o instanceof JPDAThreadGroup) {
                JPDAThreadGroup tg = (JPDAThreadGroup) o;
                Object[] ch = model.getChildren (o, from, to);
                int i, k = ch.length;
                for (i = 0; i < k; i++) {
                    if (!(ch [i] instanceof JPDAThread)) continue;
                    try {
                        JPDAThread t = (JPDAThread) ch [i];
                        if (t.getContendedMonitor () == null &&
                            t.getOwnedMonitors ().length == 0
                        ) continue;
                        ThreadWithBordel twb = new ThreadWithBordel ();
                        twb.originalThread = t;
                        ch [i] = twb;
                    } catch (ObjectCollectedException e) {
                    } catch (VMDisconnectedException e) {
                    }
                }
                return ch;
            }*/
            if (o instanceof OwnedMonitors) {
                OwnedMonitors om = (OwnedMonitors) o;
                Object[] array;
                if (om.monitors != null) {
                    array = om.monitors;
                } else {
                    array = om.variables;
                }
                from = Math.min(from, array.length);
                to = Math.min(to, array.length);
                Object[] fo = new Object[to - from];
                System.arraycopy(array, from, fo, 0, to - from);
                return fo;
            }
            if (o instanceof Monitor) {
                return model.getChildren (((Monitor) o).variable, from, to);
            }
            if (o instanceof CallStackFrame) {
                CallStackFrame frame = (CallStackFrame) o;
                if (preferences.getBoolean(SHOW_MONITORS, false)) {
                    List<MonitorInfo> monitors = frame.getOwnedMonitors();
                    int n = monitors.size();
                    if (n > 0) {
                        synchronized (framesAskedForMonitors) {
                            framesAskedForMonitors.add(frame);
                        }
                        Monitor[] ms = new Monitor[n];
                        for (int i = 0; i < n; i++) {
                            ms[i] = new Monitor(monitors.get(i).getMonitor(), frame, debugger);
                        }
                        return ms;
                    }
                } else {
                    synchronized (framesAskedForMonitors) {
                        framesAskedForMonitors.add(frame);
                    }
                }
            }
            return model.getChildren (o, from, to);
        }

        public int getChildrenCount (
            TreeModel   model,
            Object      o
        ) throws UnknownTypeException {
            /*if (o instanceof ThreadWithBordel) {
                // Performance, see issue #59058.
                return Integer.MAX_VALUE;
                /*
                try {
                    JPDAThread t = ((ThreadWithBordel) o).originalThread;
                    ObjectVariable contended = t.getContendedMonitor ();
                    ObjectVariable[] owned = t.getOwnedMonitors ();
                    int i = 0;
                    if (contended != null) i++;
                    if (owned.length > 0) i++;
                    return i;
                } catch (ObjectCollectedException e) {
                } catch (VMDisconnectedException e) {
                }
                return 0;
                 *//*
            }
            if (o instanceof ThreadWithBordel) {
                return model.getChildrenCount (
                    ((ThreadWithBordel) o).originalThread
                );
            }*/
            if (o instanceof OwnedMonitors) {
                OwnedMonitors om = (OwnedMonitors)o;
                if (om.monitors != null) {
                    return om.monitors.length;
                } else {
                    return om.variables.length;
                }
            }
            if (o instanceof Monitor) {
                return model.getChildrenCount(((Monitor) o).variable);
            }
            if (o instanceof CallStackFrame) {
                return Integer.MAX_VALUE;
            }
            return model.getChildrenCount (o);
        }

        public boolean isLeaf (TreeModel model, Object o)
        throws UnknownTypeException {
            /*if (o instanceof ThreadWithBordel) {
                return false;
            }*/
            if (o instanceof OwnedMonitors)
                return false;
            if (o instanceof ContendedMonitor)
                return true;
            if (o instanceof Monitor) {
                return true;
            }
            if (o instanceof ObjectVariable)
                return true;
            if (o instanceof CallStackFrame) {
                if (preferences.getBoolean(SHOW_MONITORS, false)) {
                    return false;
                } else {
                    synchronized (framesAskedForMonitors) {
                        framesAskedForMonitors.add((CallStackFrame) o);
                    }
                }
            }
            return model.isLeaf (o);
        }

        /*void setModelListener (ModelListener l, DebuggingTreeModel modelEventSource) {
            modelListener = l;
            this.modelEventSource = modelEventSource;
        }*/

        private void fireModelChange(ModelEvent me) {
            modelListener.modelChanged(me);
        }

        private class MonitorPreferenceChangeListener implements PreferenceChangeListener {

            public void preferenceChange(PreferenceChangeEvent evt) {
                String key = evt.getKey();
                if (SHOW_MONITORS.equals(key)) {
                    List<JPDAThread> threads;
                    synchronized (threadsAskedForMonitors) {
                        threads = new ArrayList<>(threadsAskedForMonitors);
                    }
                    for (JPDAThread t : threads) {
                        modelEventSource.doRefreshCache(dvSupport.get(t));
                    }
                    List<CallStackFrame> frames;
                    synchronized (framesAskedForMonitors) {
                        frames = new ArrayList(framesAskedForMonitors);
                    }
                    for (CallStackFrame frame : frames) {
                        modelEventSource.doRefreshCache(frame);
                    }
                    for (CallStackFrame frame : frames) {
                        fireModelChange(new ModelEvent.NodeChanged(modelEventSource,
                                        frame, ModelEvent.NodeChanged.CHILDREN_MASK));
                    }
                    for (JPDAThread t : threads) {
                        fireModelChange(new ModelEvent.NodeChanged(modelEventSource,
                                        dvSupport.get(t), ModelEvent.NodeChanged.CHILDREN_MASK));
                    }
                    if (revertShowMonitorsListener != null) {
                        // Soneone has changed the SHOW_MONITORS property, cancel the revert.
                        debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, revertShowMonitorsListener);
                        revertShowMonitorsListener = null;
                    }
                }
            }

        }

        private class DeadlockListener implements PropertyChangeListener {

            public void propertyChange(PropertyChangeEvent evt) {
                Set<Deadlock> deadlocks = deadlockDetector.getDeadlocks();
                boolean isDeadlock = deadlocks.size() > 0;
                if (isDeadlock) {
                    boolean areMonitors = preferences.getBoolean(SHOW_MONITORS, false);
                    if (!areMonitors) {
                        // Show the monitors temporarily
                        preferences.putBoolean(SHOW_MONITORS, true);
                        final RevertShowMonitorsListener revertShowMonitorsListener = new RevertShowMonitorsListener();
                        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, revertShowMonitorsListener);
                        // Set revertShowMonitorsListener later, so that lazy preferenceChange does not remove it right away.
                        RequestProcessor.getDefault().post(new Runnable() {
                            @Override
                            public void run() {
                                Children.this.revertShowMonitorsListener = revertShowMonitorsListener;
                            }
                        }, 1000);
                    }
                }
            }

        }
        
        private class RevertShowMonitorsListener implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
                    // Turn the monitors off again.
                    preferences.putBoolean(SHOW_MONITORS, false);
                    debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, this);
                }
            }
            
        }

    }
    
    
    // NodeModel impl...........................................................
    
    public String getDisplayName (NodeModel model, Object o) throws 
    UnknownTypeException {
        if (o instanceof ContendedMonitor) {
            ObjectVariable v = ((ContendedMonitor) o).variable;
            Field field = ((ContendedMonitor) o).field;
            String varName;
            if (field == null) {
                varName = v.getType();
            } else {
                varName = field.getName();
            }
            String monitorText = java.text.MessageFormat.format(NbBundle.getBundle(DebuggingMonitorModel.class).getString(
                    "CTL_MonitorModel_Column_ContendedMonitor"), new Object [] { varName, v.getValue() });
            Set nodesInDeadlock = DebuggingNodeModel.getNodesInDeadlock(debugger);
            if (nodesInDeadlock != null) {
                synchronized (nodesInDeadlock) {
                    if (nodesInDeadlock.contains(v)) {
                        monitorText = BoldVariablesTableModelFilter.toHTML(
                                monitorText,
                                false, false, Color.RED);
                    }
                }
            }
            return monitorText;
        } else
        if (o instanceof OwnedMonitors) {
            return NbBundle.getBundle(DebuggingMonitorModel.class).getString("CTL_MonitorModel_Column_OwnedMonitors");
        } else
        if (o instanceof Monitor || o instanceof ObjectVariable) {
            ObjectVariable v;
            String varName;
            if (o instanceof Monitor) {
                v = ((Monitor) o).variable;
                Field field = ((Monitor) o).field;
                if (field == null) {
                    varName = v.getType();
                } else {
                    varName = field.getName();
                }
            } else {
                v = (ObjectVariable) o;
                varName = v.getType();
            }
            String monitorText = java.text.MessageFormat.format(NbBundle.getBundle(DebuggingMonitorModel.class).getString(
                    "CTL_MonitorModel_Column_Monitor"), new Object [] { varName, v.getValue() });
            Set nodesInDeadlock = DebuggingNodeModel.getNodesInDeadlock(debugger);
            if (nodesInDeadlock != null) {
                synchronized (nodesInDeadlock) {
                    if (nodesInDeadlock.contains(v)) {
                        monitorText = BoldVariablesTableModelFilter.toHTML(
                                monitorText,
                                false, false, Color.RED);
                    }
                }
            }
            return monitorText;
        } else
        return model.getDisplayName (o);
    }
    
    private final Map shortDescriptionMap = new HashMap();
    
    public String getShortDescription (final NodeModel model, final Object o) throws 
    UnknownTypeException {

        synchronized (shortDescriptionMap) {
            Object shortDescription = shortDescriptionMap.remove(o);
            if (shortDescription instanceof String) {
                return (String) shortDescription;
            } else if (shortDescription instanceof UnknownTypeException) {
                throw (UnknownTypeException) shortDescription;
            }
        }

        // Called from AWT - we need to postpone the work...
        evaluationRP.post(new Runnable() {
            public void run() {
                Object shortDescription;
                // TODO: Enable tooltips again after problem with call stack refreshing is fixed.
                if (o instanceof ContendedMonitor) {
                    /*ObjectVariable v = ((ContendedMonitor) o).variable;
                    try {
                        shortDescription = "(" + v.getType () + ") " + v.getToStringValue ();
                    } catch (InvalidExpressionException ex) {
                        shortDescription = ex.getLocalizedMessage ();
                    }*/
                    shortDescription = "";
                } else
                if (o instanceof OwnedMonitors) {
                    shortDescription = "";
                } else
                if (o instanceof Monitor || o instanceof ObjectVariable) {
                    /*
                    ObjectVariable v = (ObjectVariable) o;
                    try {
                        shortDescription = "(" + v.getType () + ") " + v.getToStringValue ();
                    } catch (InvalidExpressionException ex) {
                        shortDescription = ex.getLocalizedMessage ();
                    }
                     */
                    shortDescription = "";
                } else {
                    try {
                        shortDescription = model.getShortDescription (o);
                    } catch (UnknownTypeException utex) {
                        shortDescription = utex;
                    }
                }
                
                if (shortDescription != null && !"".equals(shortDescription)) {
                    synchronized (shortDescriptionMap) {
                        shortDescriptionMap.put(o, shortDescription);
                    }
                    fireModelChange(new ModelEvent.NodeChanged(DebuggingMonitorModel.this,
                        o, ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK));
                }
            }
        });
        
        return ""; // NOI18N
    }
    
    public String getIconBase (NodeModel model, Object o) throws 
    UnknownTypeException {
        return model.getIconBase (o);
    }

    public void addModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }

    public void removeModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.remove(l);
        }
    }
    
    private void fireModelChange(ModelEvent me) {
        Object[] listeners;
        synchronized (modelListeners) {
            listeners = modelListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            ((ModelListener) listeners[i]).modelChanged(me);
        }
    }
    
    
    // NodeActionsProvider impl.................................................
    
    public Action[] getActions (NodeActionsProvider model, Object o) throws 
    UnknownTypeException {
        if (o instanceof ContendedMonitor) {
            return new Action [0];
        } else
        if (o instanceof OwnedMonitors) {
            return new Action [0];
        } else
        if (o instanceof Monitor) {
            return new Action [0];
        } else
        if (o instanceof ObjectVariable) {
            return new Action [0];
        } else
        return model.getActions (o);
    }
    
    public void performDefaultAction (NodeActionsProvider model, Object o) 
    throws UnknownTypeException {
        if (o instanceof ContendedMonitor) {
            return;
        } else
        if (o instanceof OwnedMonitors) {
            return;
        } else
        if (o instanceof Monitor) {
            return;
        }
        if (o instanceof ObjectVariable) {
            return;
        } else
        model.performDefaultAction (o);
    }
    
    
    // TableModel ..............................................................
    
    public Object getValueAt (Object node, String columnID) throws 
    UnknownTypeException {
        if (node instanceof OwnedMonitors ||
            node instanceof ContendedMonitor ||
            node instanceof Monitor ||
            node instanceof ObjectVariable) {
            
            if (columnID == THREAD_STATE_COLUMN_ID)
                return "";
            if (columnID == THREAD_SUSPENDED_COLUMN_ID)
                return null;
        }
        throw new UnknownTypeException (node);
    }
    
    public boolean isReadOnly (Object node, String columnID) throws 
    UnknownTypeException {
        if (node instanceof OwnedMonitors ||
            node instanceof ContendedMonitor ||
            node instanceof Monitor ||
            node instanceof ObjectVariable) {
            
            if (columnID == THREAD_STATE_COLUMN_ID || 
                columnID == THREAD_SUSPENDED_COLUMN_ID) {
                
                return true;
            }
        }
        throw new UnknownTypeException (node);
    }
    
    public void setValueAt (Object node, String columnID, Object value) 
    throws UnknownTypeException {
    }
    
    
    // innerclasses ............................................................

    private static class Monitor {
        ObjectVariable variable;
        Field field;
        
        Monitor (ObjectVariable variable, CallStackFrame f, JPDADebugger debugger) {
            this.variable = variable;
            if (f != null) {
                setVarInfo(variable, f, debugger);
            }
        }

        private void setVarInfo(ObjectVariable v, CallStackFrame f, JPDADebugger debugger) {
            ObjectVariable t = f.getThisVariable();
            long uid = v.getUniqueID();
            JPDAClassType clazz;
            try {
                // Test static fields
                //JPDAClassType clazz = f.getClassType();
                clazz = (JPDAClassType) f.getClass().getMethod("getClassType").invoke(f); // NOI18N
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                clazz = null;
            }
            if (clazz != null) {
                List<Field> fields = clazz.staticFields();
                for (Field field : fields) {
                    if (field instanceof ObjectVariable &&
                        uid == ((ObjectVariable) field).getUniqueID()) {
                        this.field = field;
                        return ;
                    }
                }
            }
            while (t != null) {
                Field[] fields = t.getFields(0, t.getFieldsCount());
                for (Field field : fields) {
                    if (field instanceof ObjectVariable &&
                        uid == ((ObjectVariable) field).getUniqueID()) {
                        this.field = field;
                        return ;
                    }
                }
                // Not found, repeat for outer object:
                t = (ObjectVariable) t.getField("this$0");
            }
        }
    }

    static class OwnedMonitors {
        Monitor[] monitors;         // If set, these are monitors with frame information
        ObjectVariable[] variables; // If set, these are monitors which are not part of a concrete frame.
        
        OwnedMonitors (ObjectVariable[] variables) {
            this.variables = variables;
            this.monitors = null;
        }

        OwnedMonitors (Monitor[] monitors) {
            this.monitors = monitors;
            this.variables = null;
        }
    }
    
    private static class ContendedMonitor extends Monitor {
        
        ContendedMonitor (ObjectVariable v, CallStackFrame f, JPDADebugger d) {
            super(v, f, d);
        }
    }

    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return null;
    }

    public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return null;
    }

    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
    }

    public String getIconBaseWithExtension(ExtendedNodeModel model, Object node) throws UnknownTypeException {
        if (node instanceof ContendedMonitor) {
            return CONTENDED_MONITOR;
        } else
        if (node instanceof OwnedMonitors) {
            return OWNED_MONITORS;
        } else
        if (node instanceof ObjectVariable || node instanceof Monitor) {
            return MONITOR;
        } else
        return model.getIconBaseWithExtension(node);
    }
    
    
}
