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
package org.netbeans.modules.debugger.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.DesignMode;
import java.beans.beancontext.BeanContextChildComponentProxy;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;

import org.netbeans.modules.debugger.ui.actions.DebuggerAction;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ui.EngineComponentsProvider;
import org.netbeans.spi.debugger.ui.EngineComponentsProvider.ComponentInfo;
import org.openide.ErrorManager;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;


/**
 * This listener notifies about changes in the
 * {@link DebuggerManager}.
 *
 * @author Jan Jancura, Martin Entlicher
 */
public class DebuggerManagerListener extends DebuggerManagerAdapter {

    private List<DebuggerEngine> openedGroups = new LinkedList<DebuggerEngine>();
    //private final Map<DebuggerEngine, List<? extends Component>> openedComponents = new HashMap<DebuggerEngine, List<? extends Component>>();
    private final Map<DebuggerEngine, Map<EngineComponentsProvider, List<? extends ComponentInfo>>> openedComponents =
            new HashMap<DebuggerEngine, Map<EngineComponentsProvider, List<? extends ComponentInfo>>>();
    private static final Set<ComponentInitiallyOpened> componentsInitiallyOpened = new HashSet<>();
    private final Map<DebuggerEngine, List<? extends Component>> closedToolbarButtons = new HashMap<DebuggerEngine, List<? extends Component>>();
    private final Map<DebuggerEngine, List<? extends Component>> usedToolbarButtons = new HashMap<DebuggerEngine, List<? extends Component>>();
    private final Map<Component, Dimension> toolbarButtonsPrefferedSize = new HashMap<Component, Dimension>();
    private final Map<Mode, Reference<TopComponent>> lastSelectedTopComponents = new WeakHashMap<Mode, Reference<TopComponent>>();
    private ToolbarContainerListener toolbarContainerListener;
    private static final RequestProcessor RP = new RequestProcessor("Debugger Engine Setup", 1);        // NOI18N

    private static final List<ComponentInfo> OPENED_COMPONENTS = new LinkedList<ComponentInfo>();

    @Override
    public void engineAdded (DebuggerEngine engine) {
        openEngineComponents(engine);
        setupToolbar(engine);
    }

    private void openEngineComponents (final DebuggerEngine engine) {
        synchronized (openedComponents) {
            if (openedComponents.containsKey(engine) || openedGroups.contains(engine)) {
                return;
            }
            final List<? extends BeanContextChildComponentProxy> componentProxies =
                    engine.lookup(null, BeanContextChildComponentProxy.class);
            final List<? extends EngineComponentsProvider> componentsProvidersL = engine.lookup(null, EngineComponentsProvider.class);
            //final List<? extends TopComponent> windowsToOpen = engine.lookup(null, TopComponent.class);
            if (!componentProxies.isEmpty() || !componentsProvidersL.isEmpty()) {
                final Map<EngineComponentsProvider, List<? extends ComponentInfo>> componentsToOpen =
                        new LinkedHashMap<EngineComponentsProvider, List<? extends ComponentInfo>>();
                componentsToOpen.put(null, null); // Going to initialize...
                final boolean filedOpenedComponents;
                if (openedComponents.isEmpty() && openedGroups.isEmpty()) {
                    fillOpenedDebuggerComponents(componentsInitiallyOpened);
                    filedOpenedComponents = true;
                } else {
                    filedOpenedComponents = false;
                }
                RequestProcessor rp = engine.lookupFirst(null, RequestProcessor.class);
                if (rp == null) {
                    rp = RP;
                }
                rp.post (new Runnable () {
                    @Override
                    public void run () {
                        if (filedOpenedComponents) {
                            try {
                                SwingUtilities.invokeAndWait(new Runnable() {
                                    @Override
                                    public void run() {
                                        synchronized (openedComponents) {
                                            for (ComponentInitiallyOpened cio : componentsInitiallyOpened) {
                                                cio.initState();
                                            }
                                        }
                                    }
                                });
                            } catch (InterruptedException | InvocationTargetException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        final Map<EngineComponentsProvider, List<? extends ComponentInfo>> ecs =
                                new LinkedHashMap<EngineComponentsProvider, List<? extends ComponentInfo>>();
                        final List<ComponentInfo> cs = new ArrayList<ComponentInfo>();
                        try {
                            final List<? extends EngineComponentsProvider> componentsProviders;
                            if (!componentProxies.isEmpty()) {
                                BeanContextComponentProvider bccp = new BeanContextComponentProvider(componentProxies);
                                if (componentsProvidersL.isEmpty()) {
                                    componentsProviders = Collections.singletonList(bccp);
                                } else {
                                    List<EngineComponentsProvider> cps = new ArrayList<EngineComponentsProvider>(componentsProvidersL.size() + 1);
                                    cps.addAll(componentsProvidersL);
                                    cps.add(bccp);
                                    componentsProviders = Collections.unmodifiableList(cps);
                                }
                            } else {
                                componentsProviders = componentsProvidersL;
                            }
                            final Map<TopComponent, ComponentInfo> topComponentsToOpen = new LinkedHashMap<TopComponent, ComponentInfo>();
                            for (EngineComponentsProvider ecp : componentsProviders) {
                                List<ComponentInfo> cis = ecp.getComponents();
                                for (final ComponentInfo ci : cis) {
                                    if (ci.isOpened()) {
                                        try {
                                            SwingUtilities.invokeAndWait(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Component c = ci.getComponent();
                                                    if (c == null) {
                                                        ErrorManager.getDefault().notify(new IllegalStateException("Null component from "+ci));
                                                        return ;
                                                    }
                                                    if (c instanceof TopComponent) {
                                                        topComponentsToOpen.put((TopComponent) c, ci);
                                                    } else {
                                                        c.setVisible(true);
                                                    }
                                                }
                                            });
                                        } catch (Exception ex) {
                                            Exceptions.printStackTrace(ex);
                                            continue;
                                        }
                                    }
                                    cs.add(ci);
                                }
                                ecs.put(ecp, cis);
                            }
                            if (topComponentsToOpen.size() > 0) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        openTopComponents(topComponentsToOpen);
                                    }
                                });
                            }
                        } finally {
                            synchronized (openedComponents) {
                                componentsToOpen.clear();
                                componentsToOpen.putAll(ecs);
                                openedComponents.notifyAll();
                            }
                            synchronized (OPENED_COMPONENTS) {
                                OPENED_COMPONENTS.addAll(cs);
                                /* consider componentsInitiallyOpened when closing components from OPENED_COMPONENTS
                                 * instead of this:
                                if (componentsInitiallyOpened.isEmpty()) {
                                    OPENED_COMPONENTS.addAll(cs);
                                } else {
                                    List<ComponentInfo> ocs = new ArrayList<ComponentInfo>(cs);
                                    for (Reference<Component> cref : componentsInitiallyOpened) {
                                        ocs.remove(cref.get());
                                    }
                                    OPENED_COMPONENTS.addAll(ocs);
                                }
                                */
                            }
                        }
                    }
                });
                openedComponents.put(engine, componentsToOpen);
            } else {
                if (openedGroups.isEmpty()) {
                    // Open debugger TopComponentGroup.
                    SwingUtilities.invokeLater (new Runnable () {
                        @Override
                        public void run () {
                            TopComponentGroup group = WindowManager.getDefault ().
                                findTopComponentGroup ("debugger"); // NOI18N
                            if (group != null) {
                                group.open ();
                            }
                        }
                    });
                }
                openedGroups.add(engine);
            }
        }
    }

    private void fillOpenedDebuggerComponents(Set<ComponentInitiallyOpened> componentsInitiallyOpened) {
        // For simplicity, add all opened components. These will not be closed when finishing the debugging session.
        TopComponent.Registry registry = TopComponent.getRegistry();
        synchronized (registry) {
            for (TopComponent tc : registry.getOpened()) {
                componentsInitiallyOpened.add(new ComponentInitiallyOpened(tc));
            }
        }
    }

    private void openTopComponents(Map<TopComponent, ComponentInfo> components) {
        assert SwingUtilities.isEventDispatchThread();
        Set<Mode> modesWithVisibleTC = new HashSet<Mode>();
        for (Map.Entry<TopComponent, ComponentInfo> tci : components.entrySet()) {
            TopComponent tc = tci.getKey();
            ComponentInfo ci = tci.getValue();
            boolean wasOpened = tc.isOpened();
            tc.open();
            if (!(wasOpened && ci.isMinimized())) { // Do not minimize opened windows
                WindowManager.getDefault().setTopComponentMinimized(tc, ci.isMinimized());
            }
            Mode mode = WindowManager.getDefault().findMode(tc);
            if (modesWithVisibleTC.add(mode)) {
                TopComponent tcSel = mode.getSelectedTopComponent();
                if (tcSel != null && tcSel != tc) {
                    WeakReference<TopComponent> lastSelectedTCRef = new WeakReference<TopComponent>(tcSel);
                    lastSelectedTopComponents.put(mode, lastSelectedTCRef);
                }
                String side = null;
                try {
                    side = (String) mode.getClass().getMethod("getSide").invoke(mode);
                } catch (Exception ex) {}
                if (side == null) {
                    tc.requestVisible();
                }
            }
        }
    }

    private void closeTopComponentsList(List<TopComponent> components) {
        assert SwingUtilities.isEventDispatchThread();
        List<TopComponent> componentToActivateAfterClose = new ArrayList<TopComponent>();
        for (TopComponent tc : components) {
            Mode mode = WindowManager.getDefault().findMode(tc);
            if (mode.getSelectedTopComponent() == tc) {
                Reference<TopComponent> tcActRef = lastSelectedTopComponents.remove(mode);
                if (tcActRef != null) {
                    TopComponent tcAct = tcActRef.get();
                    if (tcAct != null && tcAct.isOpened()) {
                        componentToActivateAfterClose.add(tcAct);
                    }
                }
            }
            tc.close();
        }
        for (TopComponent tc : componentToActivateAfterClose) {
            tc.requestVisible();
        }
    }

    private DebuggerAction getDebuggerAction(Component c) {
        if (c instanceof AbstractButton) {
            Action a = ((AbstractButton) c).getAction();
            if (a == null) {
                ActionListener[] actionListeners = ((AbstractButton) c).getActionListeners();
                for (ActionListener l : actionListeners) {
                    if (l instanceof Action) {
                        a = (Action) l;
                        break;
                    }
                }
            }
            if (a != null && a instanceof DebuggerAction) {
                return (DebuggerAction) a;
            }
        }
        return null;
    }

    private void setupToolbar(final DebuggerEngine engine) {
        final List<Component> buttonsToClose = new ArrayList<Component>();
        buttonsToClose.add(new java.awt.Label("EMPTY"));
        final boolean isFirst;
        synchronized (closedToolbarButtons) {
            isFirst = closedToolbarButtons.isEmpty();
            closedToolbarButtons.put(engine, buttonsToClose);
        }
        RequestProcessor rp = engine.lookupFirst(null, RequestProcessor.class);
        if (rp == null) {
            rp = RP;
        }
        rp.post(new Runnable() {
            @Override
            public void run() {
                List<? extends ActionsProvider> actionsProviderList = engine.lookup(null, ActionsProvider.class);
                final Set engineActions = new HashSet();
                for (ActionsProvider ap : actionsProviderList) {
                    engineActions.addAll(ap.getActions());
                }
                ToolbarPool.getDefault().waitFinished();
                SwingUtilities.invokeLater (new Runnable () {
                    @Override
                    public void run () {
                        List<Component> buttonsClosed = new ArrayList<Component>();
                        List<Component> buttonsUsed = new ArrayList<Component>();
                        try {
                            if (ToolbarPool.getDefault ().getConfiguration ().equals(ToolbarPool.DEFAULT_CONFIGURATION)) {
                                ToolbarPool.getDefault ().setConfiguration("Debugging"); // NOI18N
                            }
                            Toolbar debugToolbar = ToolbarPool.getDefault ().findToolbar("Debug");
                            if (debugToolbar == null) return ;
                            registerToolbarListener(debugToolbar);
                            for (Component c : debugToolbar.getComponents()) {
                                DebuggerAction a = getDebuggerAction(c);
                                if (a != null) {
                                    Object action = a.getAction();
                                    //System.err.println("Engine "+engine+" contains action "+a+"("+action+") = "+engineActions.contains(action));
                                    boolean containsAction = engineActions.contains(action);
                                    if (isFirst && !containsAction) {
                                        // For the first engine disable toolbar buttons for actions that are not provided
                                        c.setVisible(false);
                                        buttonsClosed.add(c);
                                        toolbarButtonsPrefferedSize.put(c, c.getPreferredSize());
                                        c.setPreferredSize(new Dimension(0, 0));
                                    }
                                    if (!isFirst && containsAction) {
                                        // For next engine enable toolbar buttons that could be previously disabled
                                        // and are used for actions that are provided.
                                        Dimension d = toolbarButtonsPrefferedSize.remove(c);
                                        if (d != null) {
                                            c.setPreferredSize(d);
                                        }
                                        c.setVisible(true);
                                    }
                                    if (containsAction) {
                                        // Keep track of buttons used by individual engines.
                                        buttonsUsed.add(c);
                                    }
                                }
                            }
                            debugToolbar.revalidate();
                            debugToolbar.repaint();
                        } finally {
                            synchronized (closedToolbarButtons) {
                                usedToolbarButtons.put(engine, buttonsUsed);
                                buttonsToClose.clear();
                                buttonsToClose.addAll(buttonsClosed);
                                closedToolbarButtons.notifyAll();
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void engineRemoved (final DebuggerEngine engine) {
        DebuggerModule dm = DebuggerModule.findObject(DebuggerModule.class);
        if (dm != null && dm.isClosing()) {
            // Do not interfere with closeDebuggerUI()
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (SwingUtilities.isEventDispatchThread()) {
                        RP.post(this);
                    } else {
                        doEngineRemoved(engine);
                    }
                }
            });
        } else {
            doEngineRemoved(engine);
        }
    }
    
    private void doEngineRemoved(DebuggerEngine engine) {
        //boolean doCloseToolbar = false;
        synchronized (openedComponents) {
            final Map<EngineComponentsProvider, List<? extends ComponentInfo>> openedWindowsByProvider = openedComponents.remove(engine);
            if (openedWindowsByProvider != null) {
                // If it's not filled yet by AWT, wait...
                while (openedWindowsByProvider.size() == 1 && openedWindowsByProvider.containsKey(null)) {
                    try {
                       openedComponents.wait();
                    } catch (InterruptedException iex) {}
                }
                List<ComponentInfo> openedWindows = new ArrayList<ComponentInfo>();
                for (List<? extends ComponentInfo> lci : openedWindowsByProvider.values()) {
                    if (lci == null) {
                        // The components are not set up yet for this engine.
                        continue;
                    }
                    openedWindows.addAll(lci);
                }
                // Check whether the component is opened by some other engine...
                final List<ComponentInfo> retainOpened = new ArrayList<ComponentInfo>();
                for (Map<EngineComponentsProvider, List<? extends ComponentInfo>> meci : openedComponents.values()) {
                    for (List<? extends ComponentInfo> lci : meci.values()){
                        if (lci == null) {
                            // The components are not set up yet for this engine.
                            continue;
                        }
                        retainOpened.addAll(lci);
                    }
                }
                final List<Component> initiallyOpened = new ArrayList<Component>();
                final Set<Component> initiallyOpenedMinimized = new HashSet<>();
                for (ComponentInitiallyOpened cio : componentsInitiallyOpened) {
                    Component c = cio.getComponent();
                    if (c != null) {
                        initiallyOpened.add(c);
                        if (cio.isMinimized()) {
                            initiallyOpenedMinimized.add(c);
                        }
                    }
                }
                final List<ComponentInfo> windowsToClose = new ArrayList<ComponentInfo>(openedWindows);
                //windowsToClose.removeAll(retainOpened);
                try {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            List<Component> retainOpenedComponents = new ArrayList<Component>(retainOpened.size());
                            for (ComponentInfo ci : retainOpened) {
                                Component c = ci.getComponent();
                                if (c == null) {
                                    ErrorManager.getDefault().notify(new IllegalStateException("Null component from "+ci));
                                    continue;
                                }
                                retainOpenedComponents.add(c);
                            }
                            for (Component c : initiallyOpened) {
                                if (c != null) {
                                    boolean initialOnly = retainOpenedComponents.add(c);
                                    if (initialOnly && c instanceof TopComponent) {
                                        WindowManager.getDefault().setTopComponentMinimized(
                                                (TopComponent) c,
                                                initiallyOpenedMinimized.contains(c));
                                    }
                                }
                            }
                            List<ComponentInfo> windowsToCloseCopy = (ArrayList<ComponentInfo>) ((ArrayList) windowsToClose).clone();
                            for (ComponentInfo ci : windowsToCloseCopy) {
                                Component c = ci.getComponent();
                                if (retainOpenedComponents.contains(c)) {
                                    windowsToClose.remove(ci);
                                }
                            }
                            for (EngineComponentsProvider ecp : openedWindowsByProvider.keySet()) {
                                List<? extends ComponentInfo> cis = openedWindowsByProvider.get(ecp);
                                List<ComponentInfo> closing = new ArrayList<ComponentInfo>(cis);
                                closing.retainAll(windowsToClose);
                                ecp.willCloseNotify(closing);
                            }
                            final List<TopComponent> topComponentsToClose = new ArrayList<TopComponent>(windowsToClose.size());
                            for (ComponentInfo ci : windowsToClose) {
                                Component c = ci.getComponent();
                                if (c == null) {
                                    ErrorManager.getDefault().notify(new IllegalStateException("Null component from "+ci));
                                    continue;
                                }
                                if (c instanceof TopComponent) {
                                    TopComponent tc = (TopComponent) c;
                                    boolean isOpened = tc.isOpened();
                                    //Properties.getDefault().getProperties(DebuggerManagerListener.class.getName()).
                                    //        getProperties(PROPERTY_CLOSED_TC).setBoolean(tc.getName(), !isOpened);
                                    if (isOpened) {
                                        topComponentsToClose.add(tc);
                                    }
                                } else {
                                    c.setVisible(false);
                                }
                            }
                            closeTopComponentsList(topComponentsToClose);
                            synchronized (OPENED_COMPONENTS) {
                                OPENED_COMPONENTS.removeAll(windowsToClose);
                            }
                        }
                    });
                } catch (Exception exc) {
                    Exceptions.printStackTrace(exc);
                }
            } else {
                openedGroups.remove(engine);
                if (openedGroups.isEmpty()) {
                    SwingUtilities.invokeLater (new Runnable () {
                        @Override
                        public void run () {
                            TopComponentGroup group = WindowManager.getDefault ().
                                findTopComponentGroup ("debugger"); // NOI18N
                            if (group != null) {
                                group.close();
                            }
                        }
                    });
                }
            }
            if (openedComponents.isEmpty() && openedGroups.isEmpty()) {
                componentsInitiallyOpened.clear();
                synchronized (OPENED_COMPONENTS) {
                    OPENED_COMPONENTS.clear();
                }
                /*doCloseToolbar = true;
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        closeToolbar();
                    }
                });*/
            }
        }
        //closeToolbar(engine, doCloseToolbar);
        closeToolbar(engine);
    }

    private void closeToolbar(DebuggerEngine engine) {
        final boolean doCloseToolbar;
        synchronized (closedToolbarButtons) {
            List<? extends Component> closedButtons = closedToolbarButtons.remove(engine);
            doCloseToolbar = closedToolbarButtons.isEmpty();
            if (closedButtons != null) {
                // If it's not filled yet by AWT, wait...
                while (closedButtons.size() == 1 && closedButtons.get(0) instanceof java.awt.Label) {
                    try {
                       closedToolbarButtons.wait();
                    } catch (InterruptedException iex) {}
                }
                List<? extends Component> usedButtons = usedToolbarButtons.remove(engine);
                ToolbarPool.getDefault().waitFinished();
                if (!ToolbarPool.getDefault ().getConfiguration ().equals("Debugging")) {
                    return ;
                }
                final Toolbar debugToolbar = ToolbarPool.getDefault ().findToolbar("Debug");
                if (debugToolbar == null) return ;
                if (!doCloseToolbar) {
                    // An engine is removed, but there remain others =>
                    // actions that remained enabled because of this are disabled unless needed by other engines.
                    // Check whether the toolbar buttons are used by some other engine...
                    final List<Component> usedByAllButtons = new ArrayList<Component>();
                    for (List<? extends Component> ltc : usedToolbarButtons.values()) {
                        usedByAllButtons.addAll(ltc);
                    }
                    final List<Component> buttonsToClose = new ArrayList<Component>(usedButtons);
                    buttonsToClose.removeAll(usedByAllButtons);
                    if (!buttonsToClose.isEmpty()) {
                        SwingUtilities.invokeLater (new Runnable () {
                            @Override
                            public void run () {
                                for (Component c : buttonsToClose) {
                                    c.setVisible(false);
                                    toolbarButtonsPrefferedSize.put(c, c.getPreferredSize());
                                    c.setPreferredSize(new Dimension(0, 0));
                                }
                                debugToolbar.revalidate();
                                debugToolbar.repaint();
                            }
                        });
                    }
                } else {
                    SwingUtilities.invokeLater (new Runnable () {
                        @Override
                        public void run () {
                            for (Component c : debugToolbar.getComponents()) {
                                if (c instanceof AbstractButton) {
                                    Dimension d = toolbarButtonsPrefferedSize.remove(c);
                                    if (d != null) {
                                        c.setPreferredSize(d);
                                    }
                                    c.setVisible(true);
                                }
                            }
                            debugToolbar.revalidate();
                            debugToolbar.repaint();
                        }
                    });
                }
            }
        }
        if (doCloseToolbar) {
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    Toolbar debugToolbar = ToolbarPool.getDefault ().findToolbar("Debug");
                    if (debugToolbar != null) {
                        unregisterToolbarListener(debugToolbar);
                    }
                    if (ToolbarPool.getDefault().getConfiguration().equals("Debugging")) { // NOI18N
                        ToolbarPool.getDefault().setConfiguration(ToolbarPool.DEFAULT_CONFIGURATION);
                    }
                }
            });
        }
    }

    private void registerToolbarListener(Toolbar debugToolbar) {
        if (toolbarContainerListener == null) {
            toolbarContainerListener = new ToolbarContainerListener();
            debugToolbar.addContainerListener(toolbarContainerListener);
        }
    }

    private void unregisterToolbarListener(Toolbar debugToolbar) {
        if (toolbarContainerListener != null) {
            debugToolbar.removeContainerListener(toolbarContainerListener);
            toolbarContainerListener = null;
        }
    }

    static void closeDebuggerUI() {
        /*
        java.util.logging.Logger.getLogger("org.netbeans.modules.debugger.ui").fine("CLOSING TopComponentGroup...");
        StringWriter sw = new StringWriter();
        new Exception("Stack Trace").fillInStackTrace().printStackTrace(new java.io.PrintWriter(sw));
        java.util.logging.Logger.getLogger("org.netbeans.modules.debugger.ui").fine(sw.toString());
         */
        // Close debugger TopComponentGroup.
        if (SwingUtilities.isEventDispatchThread()) {
            doCloseDebuggerUI();
        } else {
            SwingUtilities.invokeLater(new Runnable () {
                @Override
                public void run () {
                    doCloseDebuggerUI();
                }
            });
        }
        //java.util.logging.Logger.getLogger("org.netbeans.modules.debugger.ui").fine("TopComponentGroup closed.");
    }

    private static void doCloseDebuggerUI() {
        TopComponentGroup group = WindowManager.getDefault ().
                findTopComponentGroup ("debugger"); // NOI18N
        if (group != null) {
            group.close ();
        }
        synchronized (OPENED_COMPONENTS) {
            final List<Component> initiallyOpened;
            final Set<Component> initiallyOpenedMinimized;
            if (componentsInitiallyOpened.isEmpty()) {
                initiallyOpened = Collections.emptyList();
                initiallyOpenedMinimized = Collections.emptySet();
            } else {
                initiallyOpened = new ArrayList<Component>();
                initiallyOpenedMinimized = new HashSet<>();
                for (ComponentInitiallyOpened cio : componentsInitiallyOpened) {
                    Component c = cio.getComponent();
                    if (c != null) {
                        initiallyOpened.add(c);
                        if (cio.isMinimized()) {
                            initiallyOpenedMinimized.add(c);
                        }
                    }
                }
            }
            for (ComponentInfo ci : OPENED_COMPONENTS) {
                Component c = ci.getComponent();
                if (initiallyOpened.contains(c)) {
                    if (c instanceof TopComponent) {
                        WindowManager.getDefault().setTopComponentMinimized(
                                (TopComponent) c,
                                initiallyOpenedMinimized.contains(c));
                    }
                    continue;
                }
                if (c instanceof TopComponent) {
                    /* To check which components we're closing:
                    try {
                        Method pid = TopComponent.class.getDeclaredMethod("preferredID");
                        pid.setAccessible(true);
                    System.err.println("doCloseDebuggerUI("+pid.invoke(c)+")");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    */
                    ((TopComponent) c).close();
                } else if (c != null) {
                    c.setVisible(false);
                }
            }
            OPENED_COMPONENTS.clear();
            componentsInitiallyOpened.clear();
        }
        ToolbarPool.getDefault().waitFinished();
        if (ToolbarPool.getDefault().getConfiguration().equals("Debugging")) { // NOI18N
            ToolbarPool.getDefault().setConfiguration(ToolbarPool.DEFAULT_CONFIGURATION);
        }
    }

    private class ToolbarContainerListener implements ContainerListener {

        private final Map<DebuggerAction, Set<DebuggerEngine>> buttonActionsUsed = new HashMap<DebuggerAction, Set<DebuggerEngine>>();
        private final Map<DebuggerAction, Set<DebuggerEngine>> buttonActionsClosed = new HashMap<DebuggerAction, Set<DebuggerEngine>>();
        private boolean clearScheduled = false;
        
        @Override
        public void componentAdded(ContainerEvent e) {
            Component c = e.getChild();
            DebuggerAction action = getDebuggerAction(c);
            if (action != null) {
                Set<DebuggerEngine> usedEngines = buttonActionsUsed.get(action);
                if (usedEngines != null) {
                    for (DebuggerEngine engine : usedEngines) {
                        List<Component> buttonsUsed = (List<Component>) usedToolbarButtons.get(engine);
                        buttonsUsed.add(c);
                    }
                }
                Set<DebuggerEngine> closedEngines = buttonActionsClosed.get(action);
                if (closedEngines != null) {
                    for (DebuggerEngine engine : closedEngines) {
                        List<Component> buttonsClosed = (List<Component>) closedToolbarButtons.get(engine);
                        buttonsClosed.add(c);
                    }
                }
                if (usedEngines == null && closedEngines != null) {
                    // Disable toolbar buttons for actions that are not provided
                    c.setVisible(false);
                    toolbarButtonsPrefferedSize.put(c, c.getPreferredSize());
                    c.setPreferredSize(new Dimension(0, 0));
                }
                if (usedEngines == null && closedEngines == null) { // Unknown
                    for (DebuggerEngine engine : usedToolbarButtons.keySet()) {
                        List<Component> buttonsUsed = (List<Component>) usedToolbarButtons.get(engine);
                        // The button was explicitly added to the toolbar.
                        // We do not want to remove it right away if it's not supported by the engine.
                        // Thus add it as used by the engine...
                        buttonsUsed.add(c);
                    }
                }
            }
        }
        
        @Override
        public void componentRemoved(ContainerEvent e) {
            Component c = e.getChild();
            DebuggerAction action = getDebuggerAction(c);
            if (action != null) {
                Set<DebuggerEngine> usedEngines = null;
                Set<DebuggerEngine> closedEngines = null;
                for (DebuggerEngine engine : usedToolbarButtons.keySet()) {
                    if (usedToolbarButtons.get(engine).remove(c)) {
                        if (usedEngines == null) {
                            usedEngines = new HashSet<DebuggerEngine>();
                        }
                        usedEngines.add(engine);
                    }
                }
                for (DebuggerEngine engine : closedToolbarButtons.keySet()) {
                    if (closedToolbarButtons.get(engine).remove(c)) {
                        if (closedEngines == null) {
                            closedEngines = new HashSet<DebuggerEngine>();
                        }
                        closedEngines.add(engine);
                    }
                }
                if (usedEngines != null) {
                    buttonActionsUsed.put(action, usedEngines);
                }
                if (closedEngines != null) {
                    buttonActionsClosed.put(action, closedEngines);
                }
            }
            toolbarButtonsPrefferedSize.remove(c);

            if (!clearScheduled) {
                // Component with that action will be added back soon, if not, clear the map
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        buttonActionsUsed.clear();
                        buttonActionsClosed.clear();
                        clearScheduled = false;
                    }
                });
            }
        }
        
    }
    
    private static class BeanContextComponentProvider implements EngineComponentsProvider {
        
        private final List<? extends BeanContextChildComponentProxy> componentProxies;
        
        public BeanContextComponentProvider(List<? extends BeanContextChildComponentProxy> componentProxies) {
            this.componentProxies = componentProxies;
        }

        @Override
        public List<ComponentInfo> getComponents() {
            return ComponentInfoFromBeanContext.transform(componentProxies);
        }

        @Override
        public void willCloseNotify(List<ComponentInfo> components) {
            ComponentInfoFromBeanContext.closing(components);
        }
        
    }
    
    private static class ComponentInitiallyOpened {
        
        private final Reference<Component> componentRef;
        private boolean isMinimized;

        public ComponentInitiallyOpened(Component c) {
            this.componentRef = new WeakReference<Component>(c);
        }
        
        public Component getComponent() {
            return componentRef.get();
        }
        
        public boolean isMinimized() {
            return isMinimized;
        }

        private void initState() {
            Component c = getComponent();
            if (c instanceof TopComponent) {
                this.isMinimized = WindowManager.getDefault().isTopComponentMinimized((TopComponent) c);
            } else {
                this.isMinimized = false;
            }
        }
        
    }

}
