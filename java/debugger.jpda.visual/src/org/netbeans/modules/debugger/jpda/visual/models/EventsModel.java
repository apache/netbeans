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
package org.netbeans.modules.debugger.jpda.visual.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.RemoteAWTScreenshot.AWTComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.RemoteServices;
import org.netbeans.modules.debugger.jpda.visual.RemoteServices.RemoteListener;
import org.netbeans.modules.debugger.jpda.visual.actions.GoToSourceAction;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.spi.ScreenshotUIManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeExpansionModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/EventsView", types={ TreeModel.class, NodeModel.class, TableModel.class, NodeActionsProvider.class, TreeExpansionModelFilter.class })
public class EventsModel implements TreeModel, NodeModel, TableModel, NodeActionsProvider, TreeExpansionModelFilter {
    
    private static final String customListeners = "customListeners"; // NOI18N
    private static final String swingListeners = "swingListeners"; // NOI18N
    private static final String eventsLog = "eventsLog"; // NOI18N
    
    private Set<ModelListener> listeners = new CopyOnWriteArraySet<ModelListener>();
    
    private JavaComponentInfo selectedCI = null;
    private final List<RemoteEvent> events = new ArrayList<RemoteEvent>();
    private volatile List<RemoteListener> customListenersList;
    private volatile List<RemoteListener> swingListenersList;
    private JPDADebugger debugger;
    private final Map<ObjectReference, Set<LoggingEventListener>> loggingListeners =
            new HashMap<ObjectReference, Set<LoggingEventListener>>();
    
    public EventsModel(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
        ScreenshotUIManager uiManager = ScreenshotUIManager.getActive();
        if (uiManager != null) {
            ComponentInfo ci = uiManager.getSelectedComponent();
            if (ci instanceof JavaComponentInfo) {
                selectedCI = (JavaComponentInfo) ci;
            }
        }
        /*Node[] nodes = ComponentHierarchy.getInstance().getExplorerManager().getSelectedNodes();
        if (nodes.length > 0) {
            selectedCI = nodes[0].getLookup().lookup(AWTComponentInfo.class);
        }*/
        final Result<Node> nodeLookupResult = Utilities.actionsGlobalContext().lookupResult(Node.class);
        LookupListener ll = new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                Collection<? extends Node> nodeInstances = nodeLookupResult.allInstances();
                for (Node n : nodeInstances) {
                    JavaComponentInfo ci = n.getLookup().lookup(JavaComponentInfo.class);
                    if (ci != null) {
                        if (!ci.equals(selectedCI)) {
                            selectedCI = ci;
                            if (ev != null) {
                                fireModelChanged();
                            }
                        }
                        break;
                    }
                }
            }
        };
        nodeLookupResult.addLookupListener(ll);
        ll.resultChanged(null); // To initialize
    }
    
    @Override
    public Object getRoot() {
        return ROOT;
    }

    @Override
    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        if (parent == ROOT) {
            JavaComponentInfo ci = selectedCI;
            if (ci != null) {
                String componentName = ci.getDisplayName();
                List<RemoteListener> componentListeners;
                try {
                    componentListeners = RemoteServices.getAttachedListeners(ci, true);
                    //System.err.println("ALL Component Listeners = "+componentListeners);
                } catch (PropertyVetoException pvex) {
                    Exceptions.printStackTrace(pvex);
                    return new Object[] {};
                }
                Set<LoggingEventListener> lls = null;
                synchronized (loggingListeners) {
                    lls = loggingListeners.get(ci.getComponent());
                    if (lls != null) {
                        lls = new HashSet<LoggingEventListener>(lls);
                    }
                }
                //Map<String, ListenerCategory> listenerCategories;
                //customListenersMap = new TreeMap<String, ListenerCategory>();
                //swingListenersMap = new TreeMap<String, ListenerCategory>();
                List<RemoteListener> cll = new ArrayList<RemoteListener>(componentListeners.size());
                List<RemoteListener> sll = new ArrayList<RemoteListener>(componentListeners.size());
                for (RemoteListener rl : componentListeners) {
                    ObjectReference listener = rl.getListener();
                    if (lls != null) {
                        boolean isLogging = false;
                        for (LoggingEventListener ll : lls) {
                            if (listener.equals(ll.getListenerObject())) {
                                isLogging = true;
                                break;
                            }
                        }
                        if (isLogging) {
                            continue; // Ignore the logging listener.
                        }
                    }
                    try {
                        String type = ReferenceTypeWrapper.name(ObjectReferenceWrapper.referenceType(listener));
                        if (JavaComponentInfo.isCustomType(type)) {
                            //listenerCategories = customListenersMap;
                            cll.add(rl);
                        } else {
                            sll.add(rl);
                            //listenerCategories = swingListenersMap;
                        }
                    } catch (InternalExceptionWrapper iex) {
                    } catch (ObjectCollectedExceptionWrapper ocex) {
                    } catch (VMDisconnectedExceptionWrapper vmdex) {
                    }
                    /*
                    ListenerCategory lc = listenerCategories.get(type);
                    if (lc == null) {
                        lc = new ListenerCategory(type);
                        listenerCategories.put(type, lc);
                    }
                    lc.addListener(rl); */
                }
                customListenersList = cll;
                swingListenersList = sll;
                
                return new Object[] { componentName, customListeners, swingListeners, eventsLog };
            } else {
                customListenersList = null;
                swingListenersList = null;
            }
            return new Object[] {};
        }
        if (parent == customListeners) {
            return customListenersList.toArray();
        }
        if (parent == swingListeners) {
            return swingListenersList.toArray();
        }
        /*
        if (parent == customListeners || parent == swingListeners) {
            AWTComponentInfo ci = selectedCI;
            if (ci != null) {
                //ObjectReference component = ci.getComponent();
                List<RemoteListener> componentListeners;
                try {
                    componentListeners = RemoteServices.getAttachedListeners(ci);
                } catch (PropertyVetoException pvex) {
                    Exceptions.printStackTrace(pvex);
                    return new Object[] {};
                }
                Map<String, ListenerCategory> listenerCategories = new TreeMap<String, ListenerCategory>();
                for (RemoteListener rl : componentListeners) {
                    String type = rl.getType();
                    if ((parent == customListeners) == JavaComponentInfo.isCustomType(type)) {
                        ListenerCategory lc = listenerCategories.get(type);
                        if (lc == null) {
                            lc = new ListenerCategory(type);
                            listenerCategories.put(type, lc);
                        }
                        lc.addListener(rl);
                    }
                }
                return listenerCategories.values().toArray();
            }
        }
         */
        /*if (parent instanceof RemoteListener) {
            return ((RemoteListener) parent).getListener();
        }*/
        if (parent instanceof ListenerCategory) {
            return ((ListenerCategory) parent).getListeners().toArray();
        }
        if (parent == eventsLog) {
            synchronized (events) {
                return events.toArray();
            }
        }
        if (parent instanceof RemoteEvent) {
            return ((RemoteEvent) parent).getPropertiesWithStackNode();
        }
        if (parent instanceof Stack) {
            return ((Stack) parent).getStackElements();
        }
        return new Object[] {};
    }

    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT || node == eventsLog) {
            return false;
        }
        if (node == customListeners) {
            //List<RemoteListener> l = customListenersList;
            //return (l == null || l.isEmpty());
            return false; // To have the expand sign visible always so that it's
                          // clear that the listeners would be listed under this node.
        }
        if (node == swingListeners) {
            //List<RemoteListener> l = swingListenersList;
            //return (l == null || l.isEmpty());
            return false; // To have the expand sign visible always so that it's
                          // clear that the listeners would be listed under this node.
        }
        if (node instanceof RemoteListener) {
            return true;
        }
        if (node instanceof Stack.Element) {
            return true;
        }
        if (node instanceof String) {
            return true;
        }
        return false;
    }

    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {
        return Integer.MAX_VALUE;
    }

    @Override
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    
    private void fireModelChanged() {
        ModelEvent me = new ModelEvent.TreeChanged(this);
        for (ModelListener l : listeners) {
            l.modelChanged(me);
        }
    }
    
    private void fireNodeChanged(Object node) {
        ModelEvent me = new ModelEvent.NodeChanged(this, node);
        for (ModelListener l : listeners) {
            l.modelChanged(me);
        }
    }
    
    private void addEvent(RemoteEvent re) {
        synchronized (events) {
            events.add(re);
        }
        fireNodeChanged(eventsLog);
    }

    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return "Events";
        }
        if (node == customListeners) {
            return NbBundle.getMessage(EventsModel.class, "CTL_CustomListeners");
        }
        if (node == swingListeners) {
            return NbBundle.getMessage(EventsModel.class, "CTL_InternalListeners");
        }
        if (node == eventsLog) {
            return NbBundle.getMessage(EventsModel.class, "CTL_EventLog");
        }
        if (node instanceof ListenerCategory) {
            return ((ListenerCategory) node).getType();
        }
        if (node instanceof RemoteListener) {
            return ((RemoteListener) node).getListener().referenceType().name();
        }
        if (node instanceof RemoteEvent) {
            RemoteEvent re = (RemoteEvent) node;
            String toString = re.getEventToString();
            int end = toString.indexOf('[');
            if (end < 0) end = toString.length();
            return re.getListenerMethod()+" ("+toString.substring(0, end)+')';
        }
        if (node instanceof Stack) {
            return NbBundle.getMessage(EventsModel.class, "CTL_CalledFrom");
        }
        if (node instanceof Stack.Element) {
            Stack.Element e = (Stack.Element) node;
            return "<html>"+e.getClassName()+".<b>"+e.getMethodName()+"</b>(<font color=\"#0000FF\">"+e.getFileName()+":"+e.getLineNumber()+"</font>)</html>";
        }
        return String.valueOf(node);
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        return null;
    }

    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof RemoteListener) {
            try {
                return ReferenceTypeWrapper.sourceName(ObjectReferenceWrapper.referenceType(((RemoteListener) node).getListener()));
            } catch (InternalExceptionWrapper ex) {
                return ex.getLocalizedMessage();
            } catch (VMDisconnectedExceptionWrapper ex) {
                return "";
            } catch (ObjectCollectedExceptionWrapper ex) {
                return ex.getLocalizedMessage();
            } catch (AbsentInformationException ex) {
                return "";
            }
        }
        if (node instanceof RemoteEvent) {
            RemoteEvent re = (RemoteEvent) node;
            return re.getEventToString();
        }
        return "";
    }

    @Override
    public void performDefaultAction(Object node) throws UnknownTypeException {
        if (node == eventsLog) {
            new SetLoggingEvents().actionPerformed(null);
        }
        if (node instanceof Stack.Element) {
            final Stack.Element e = (Stack.Element) node;
            String type = e.getClassName();
            type = EditorContextBridge.getRelativePath (type);
            final String url = ((JPDADebuggerImpl) debugger).getEngineContext().getURL(type, true);
            if (url != null) {
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        EditorContextBridge.getContext().showSource(url, e.getLineNumber(), null);
                    }
                });
            }
        }
    }

    @Override
    public Action[] getActions(Object node) throws UnknownTypeException {
        if (selectedCI != null) {
            if (node == customListeners) {
                return new Action[] { new SetLoggingEvents() };//new AddLoggingListenerAction(null) };
            }
            if (node == eventsLog) {
                return new Action[] {
                    new SetLoggingEvents(),
                    null,
                    new ClearEventsAction() };
            }
            if (node instanceof ListenerCategory) {
                return new Action[] { new AddLoggingListenerAction((ListenerCategory) node) };
            }
            if (node instanceof RemoteListener) {
                return new Action[] { GoToSourceAction.get(GoToSourceAction.class) };
            }
        }
        return new Action[] {};
    }
    
    private boolean customListenersExpanded = true;
    private boolean eventsExpanded = true;

    @Override
    public boolean isExpanded(TreeExpansionModel original, Object node) throws UnknownTypeException {
        if (node == customListeners) {
            return customListenersExpanded;
        } else if (node == eventsLog) {
            return eventsExpanded;
        } else {
            return original.isExpanded(node);
        }
    }

    @Override
    public void nodeExpanded(Object node) {
        if (node == customListeners) {
            customListenersExpanded = true;
        } else if (node == eventsLog) {
            eventsExpanded = true;
        }
    }

    @Override
    public void nodeCollapsed(Object node) {
        if (node == customListeners) {
            customListenersExpanded = false;
        } else if (node == eventsLog) {
            eventsExpanded = false;
        }
    }

    @Override
    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        if (EventTypesColumnModel.ID.equals(columnID)) {
            if (node instanceof RemoteListener) {
                String[] types = ((RemoteListener) node).getTypes();
                if (types.length == 1) {
                    return types[0];
                } else {
                    StringBuilder sb = new StringBuilder(types[0]);
                    for (int i = 1; i < types.length; i++) {
                        sb.append(", ");
                        sb.append(types[i]);
                    }
                    return sb.toString();
                }
            } else {
                return "";
            }
        } else {
            throw new UnknownTypeException("Unknown column: "+columnID);
        }
    }

    @Override
    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        return true;
    }

    @Override
    public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
        throw new IllegalStateException("Is read only.");
    }
    
    private static class ListenerCategory {
        
        private String type;
        private List<RemoteListener> listeners = new ArrayList<RemoteListener>();
        
        public ListenerCategory(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
        
        public void addListener(RemoteListener l) {
            listeners.add(l);
        }
        
        public List<RemoteListener> getListeners() {
            return listeners;
        }
    }
    
    private class SetLoggingEvents extends AbstractAction {
        
        public SetLoggingEvents() {}

        @Override
        public Object getValue(String key) {
            if (Action.NAME.equals(key)) {
                return NbBundle.getMessage(EventsModel.class, "CTL_SetLoggingEvents");
            }
            return super.getValue(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final JavaComponentInfo ci = selectedCI;
            if (ci == null) return ;
            final ReferenceType[] listenerClasses;
            final List<LoggingEventListener> listenersToRemove = new ArrayList<LoggingEventListener>();
            listenerClasses = selectListenerClass(ci, listenersToRemove);
            if (listenerClasses == null) {
                return;
            }
            ci.getThread().getDebugger().getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    boolean fire = false;
                    for (ReferenceType rt : listenerClasses) {
                        ObjectReference l;
                        try {
                            LoggingEventListener listener = new LoggingEventListener(EventsModel.this);
                            ClassObjectReference cor = ReferenceTypeWrapper.classObject(rt);
                            l = RemoteServices.attachLoggingListener(ci, cor, listener);
                            listener.setListenerObject(l, cor);
                            synchronized (loggingListeners) {
                                Set<LoggingEventListener> listeners = loggingListeners.get(ci.getComponent());
                                if (listeners == null) {
                                    listeners = new HashSet<LoggingEventListener>();
                                    loggingListeners.put(ci.getComponent(), listeners);
                                }
                                listeners.add(listener);
                            }
                        } catch (PropertyVetoException pvex) {
                            Exceptions.printStackTrace(pvex);
                            return ;
                        } catch (InternalExceptionWrapper iex) {
                            return ;
                        } catch (ObjectCollectedExceptionWrapper ocex) {
                            Exceptions.printStackTrace(ocex);
                            return ;
                        } catch (UnsupportedOperationExceptionWrapper uex) {
                            Exceptions.printStackTrace(uex);
                            return ;
                        } catch (VMDisconnectedExceptionWrapper vmdex) {
                            return ;
                        }
                        if (l != null) {
                            fire = true;
                        }
                    }
                    for (LoggingEventListener ll : listenersToRemove) {
                        try {
                            boolean detached = RemoteServices.detachLoggingListener(ci, ll.getListenerClass(), ll.getListenerObject());
                            synchronized (loggingListeners) {
                                Set<LoggingEventListener> listeners = loggingListeners.get(ci.getComponent());
                                if (listeners != null) {
                                    listeners.remove(ll);
                                    if (listeners.isEmpty()) {
                                        loggingListeners.remove(ci.getComponent());
                                    }
                                }
                            }
                        } catch (PropertyVetoException ex) {
                            Exceptions.printStackTrace(ex);
                            return;
                        }
                    }
                    if (fire) {
                        fireNodeChanged(customListeners);
                    }
                }
            });
        }
        
        private ReferenceType[] selectListenerClass(JavaComponentInfo ci, Collection<LoggingEventListener> listenersToRemove) {
            List<ReferenceType> attachableListeners = RemoteServices.getAttachableListeners(ci);
            //System.err.println("Attachable Listeners = "+attachableListeners);
            Set<LoggingEventListener> currentLoggingListeners = null;
            synchronized (loggingListeners) {
                Set<LoggingEventListener> listeners = loggingListeners.get(ci.getComponent());
                if (listeners != null) {
                    currentLoggingListeners = new HashSet<LoggingEventListener>(listeners);
                }
            }
            String[] listData = new String[attachableListeners.size()];
            boolean[] logging = new boolean[listData.length];
            LoggingEventListener[] loggingListeners = null;
            if (currentLoggingListeners != null) {
                loggingListeners = new LoggingEventListener[listData.length];
            }
            for (int i = 0; i < listData.length; i++) {
                ReferenceType rt = attachableListeners.get(i);
                listData[i] = rt.name();
                if (currentLoggingListeners != null) {
                    for (LoggingEventListener ll : currentLoggingListeners) {
                        if (rt.equals(ll.getListenerClass().reflectedType())) {
                            logging[i] = true;
                            loggingListeners[i] = ll;
                        }
                    }
                }
            }
            SelectEventsPanel sep = new SelectEventsPanel();
            sep.setData(listData, logging);
            NotifyDescriptor nd = new DialogDescriptor(sep,
                    NbBundle.getMessage(EventsModel.class, "TTL_SelectListener"),
                    true, null);
            Object res = DialogDisplayer.getDefault().notify(nd);
            if (DialogDescriptor.OK_OPTION.equals(res)) {
                boolean[] loggingData = sep.getLoggingData();
                int na = 0;
                for (int i = 0; i < loggingData.length; i++) {
                    if (loggingData[i] && !logging[i]) na++;
                    //if (!loggingData[i] && logging[i]) nr++;
                }
                ReferenceType[] listenersToAdd = new ReferenceType[na];
                int lai = 0;
                for (int i = 0; i < listData.length; i++) {
                    if (loggingData[i] && !logging[i]) {
                        listenersToAdd[lai++] = attachableListeners.get(i);
                    }
                    if (!loggingData[i] && logging[i]) {
                        listenersToRemove.add(loggingListeners[i]);
                    }
                }
                return listenersToAdd;
            } else {
                return null;
            }
        }

    }
    
    private class AddLoggingListenerAction extends AbstractAction {
        
        private ListenerCategory lc;
        
        public AddLoggingListenerAction(ListenerCategory lc) {
            this.lc = lc;
        }

        @Override
        public Object getValue(String key) {
            if (Action.NAME.equals(key)) {
                return "Add Logging Listener" + ((lc == null) ? "..." : "" );
            }
            return super.getValue(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final JavaComponentInfo ci = selectedCI;
            if (ci == null) return ;
            String listenerClass;
            if (lc != null) {
                listenerClass = lc.getType();
            } else {
                listenerClass = selectListenerClass(ci);
                if (listenerClass == null) {
                    return;
                }
            }
            final ReferenceType rt = getReferenceType(ci.getComponent().virtualMachine(), listenerClass);
            if (rt == null) {
                System.err.println("No class "+listenerClass);
                return ;
            }
            ci.getThread().getDebugger().getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    ObjectReference l;
                    try {
                        ClassObjectReference cor = ReferenceTypeWrapper.classObject(rt);
                        l = RemoteServices.attachLoggingListener(ci, cor, new LoggingEventListener(EventsModel.this));
                    } catch (PropertyVetoException pvex) {
                        Exceptions.printStackTrace(pvex);
                        return ;
                    } catch (InternalExceptionWrapper iex) {
                        return ;
                    } catch (ObjectCollectedExceptionWrapper ocex) {
                        Exceptions.printStackTrace(ocex);
                        return ;
                    } catch (UnsupportedOperationExceptionWrapper uex) {
                        Exceptions.printStackTrace(uex);
                        return ;
                    } catch (VMDisconnectedExceptionWrapper vmdex) {
                        return ;
                    }
                    if (l != null) {
                        if (lc != null) {
                            String name;
                            try {
                                name = ReferenceTypeWrapper.name(ObjectReferenceWrapper.referenceType(l));
                            } catch (InternalExceptionWrapper ex) {
                                return ;
                            } catch (VMDisconnectedExceptionWrapper ex) {
                                return ;
                            } catch (ObjectCollectedExceptionWrapper ex) {
                                Exceptions.printStackTrace(ex);
                                return ;
                            }
                            lc.addListener(new RemoteListener(name, l));
                            fireNodeChanged(lc);
                        } else {
                            fireNodeChanged(customListeners);
                        }
                    }
                }
            });
            
        }
        
        private ReferenceType getReferenceType(VirtualMachine vm, String name) {
            ReferenceType clazz = null;
            try {
                List<ReferenceType> classList = VirtualMachineWrapper.classesByName(vm, name);
                for (ReferenceType c : classList) {
                    clazz = c;
                    if (ReferenceTypeWrapper.classLoader(c) == null) {
                        break;
                    }
                }
            } catch (ObjectCollectedExceptionWrapper ocex) {
            } catch (InternalExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
            }
            return clazz;
        }
        
        private String selectListenerClass(JavaComponentInfo ci) {
            List<ReferenceType> attachableListeners = RemoteServices.getAttachableListeners(ci);
            //System.err.println("Attachable Listeners = "+attachableListeners);
            String[] listData = new String[attachableListeners.size()];
            for (int i = 0; i < listData.length; i++) {
                listData[i] = attachableListeners.get(i).name();
            }
            JList jl = new JList(listData);
            JScrollPane jsp = new JScrollPane(jl);
            NotifyDescriptor nd = new DialogDescriptor(jsp,
                    NbBundle.getMessage(EventsModel.class, "TTL_SelectListener"),
                    true, null);
            Object res = DialogDisplayer.getDefault().notify(nd);
            if (DialogDescriptor.OK_OPTION.equals(res)) {
                String clazz = (String) jl.getSelectedValue();
                return clazz;
            } else {
                return null;
            }
        }

    }

    private class ClearEventsAction extends AbstractAction {

        @Override
        public Object getValue(String key) {
            if (Action.NAME.equals(key)) {
                return NbBundle.getMessage(EventsModel.class, "CTL_ClearEvents");
            }
            return super.getValue(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            synchronized (events) {
                events.clear();
            }
            fireNodeChanged(eventsLog);
        }
        
    }
    
    private static class LoggingEventListener implements RemoteServices.LoggingListenerCallBack {
        
        private ObjectReference listener;
        private ClassObjectReference listenerClass;
        private final Reference<EventsModel> modelRef;
        
        public LoggingEventListener(EventsModel model) {
            modelRef = new WeakReference<EventsModel>(model);
        }

        @Override
        public void eventsData(/*JavaComponentInfo ci,*/ String[] data, String[] stack) {
            RemoteEvent re = new RemoteEvent(data, stack);
            /*
            System.err.println("Have data about "+ci.getType()+":");//\n  "+Arrays.toString(data));
            System.err.println("  Method: "+data[0]+", event toString() = "+data[1]);
            for (int j = 2; j < data.length; j += 2) {
                System.err.println("    "+data[j]+" = "+data[j+1]);
            }
             */
            EventsModel model = modelRef.get();
            if (model != null) {
                model.addEvent(re);
            }
        }
        
        private void setListenerObject(ObjectReference listener, ClassObjectReference listenerClass) {
            this.listener = listener;
            this.listenerClass = listenerClass;
        }
        
        @Override
        public ObjectReference getListenerObject() {
            return listener;
        }
        
        public ClassObjectReference getListenerClass() {
            return listenerClass;
        }

    }
    
    private static class RemoteEvent {
        
        private String[] data;
        private Stack stack;
        
        public RemoteEvent(String[] data, String[] stack) {
            this.data = data;
            this.stack = new Stack(stack);
        }
        
        public String getListenerMethod() {
            return data[0];
        }
        
        public String getEventToString() {
            return data[1];
        }
        
        public Object[] getPropertiesWithStackNode() {
            int propertiesLength = data.length/2 - 1;
            Object[] properties = new Object[propertiesLength + 1];
            for (int i = 0; i < propertiesLength; i++) {
                properties[i + 1] = data[2 + 2*i] + " = "+data[3 + 2*i];
            }
            properties[0] = stack;
            return properties;
        }
        
        public Stack getStack() {
            return stack;
        }
        
    }
    
    private static class Stack {
        
        private String[] stack;
        private String listener = null;
        private Element[] elements = null;
        
        public Stack(String[] stack) {
            this.stack = stack;
        }
        
        public synchronized Element[] getStackElements() {
            if (elements == null) {
                elements = new Element[stack.length - 1];
                for (int i = 1; i < stack.length; i++) {
                    elements[i - 1] = new Element(stack[i]);
                }
            }
            return elements;
        }
        
        static class Element {
            
            private String line;
            private boolean parsed = false;
            private String className;
            private String methodName;
            private String fileName;
            private int lineNumber;
            
            // <class name>.<method>(<file name>:<line number>)
            public Element(String line) {
                this.line = line;
            }
            
            private synchronized void parse() {
                if (parsed) return;
                int i = line.indexOf('(');
                int mi = line.substring(0, i).lastIndexOf('.');
                int ci = line.lastIndexOf(':');
                className = line.substring(0, mi);
                methodName = line.substring(mi + 1, i);
                fileName = line.substring(i + 1, ci);
                String lineStr = line.substring(ci + 1, line.length() - 1);
                lineNumber = Integer.parseInt(lineStr);
            }
            
            public String getClassName() {
                parse();
                return className;
            }
            
            public String getMethodName() {
                parse();
                return methodName;
            }
            
            public String getFileName() {
                parse();
                return fileName;
            }
            
            public int getLineNumber() {
                parse();
                return lineNumber;
            }
        }
    }
    
}
