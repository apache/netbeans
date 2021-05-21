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
package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import javax.swing.JComponent;
import org.netbeans.api.actions.Savable;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jellytools.actions.AttachWindowAction;
import org.netbeans.jellytools.actions.CloneViewAction;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.CloseViewAction;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.Operator;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;
import org.netbeans.swing.tabcontrol.TabbedContainer;

/**
 * Represents org.openide.windows.TopComponent. It is IDE wrapper for a lot of
 * panels in IDE. TopComponent is for example Projects panel, every editor
 * panel or output panel. TopComponent can be located by TopComponentOperator
 * anywhere inside IDE, if it is opened. It is by default activated which means
 * it is put to foreground if there exist more top components in a split area.
 * TopComponent can also be located explicitly inside some Container.
 *
 * <p> Usage:<br>
 * <pre>
 *      TopComponentOperator tco = new TopComponentOperator("Projects");
 *      tco.pushMenuOnTab("Maximize");
 *      tco.restore();
 *      tco.attachTo("Files", AttachWindowAction.AS_LAST_TAB);
 *      tco.attachTo("Output", AttachWindowAction.RIGHT);
 *      tco.close();
 * </pre>
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 *
 * @see org.netbeans.jellytools.actions.AttachWindowAction
 * @see org.netbeans.jellytools.actions.CloneViewAction
 * @see org.netbeans.jellytools.actions.CloseAllDocumentsAction
 * @see org.netbeans.jellytools.actions.CloseViewAction
 * @see org.netbeans.jellytools.actions.MaximizeWindowAction
 * @see org.netbeans.jellytools.actions.RestoreWindowAction
 */
public class TopComponentOperator extends JComponentOperator {

    /**
     * "Close Window" popup menu item.
     */
    private static final String closeWindowItem = Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
            "CTL_CloseWindowAction");
    /**
     * "Close All Documents" popup menu item.
     */
    private static final String closeAllDocumentsItem = Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
            "LBL_CloseAllDocumentsAction");

    static {
        // Checks if you run on correct jemmy version. Writes message to jemmy log if not.
        JellyVersion.checkJemmyVersion();
        // need to set timeout for the case it was not set previously
        Timeouts.initDefault("EventDispatcher.RobotAutoDelay", 0);
        DriverManager.setDriver(DriverManager.MOUSE_DRIVER_ID,
                new MouseRobotDriver(JemmyProperties.getCurrentTimeouts().create("EventDispatcher.RobotAutoDelay"),
                new String[]{TopComponentOperator.class.getName()}));
    }

    /** Waits for index-th TopComponent with given name in specified container.
     * It is activated by default.
     * @param contOper container where to search
     * @param topComponentName name of TopComponent (it used to be label of tab)
     * @param index index of TopComponent to be find
     */
    public TopComponentOperator(ContainerOperator contOper, String topComponentName, int index) {
        super(waitTopComponent(contOper, topComponentName, index, null));
        copyEnvironment(contOper);
        makeComponentVisible();
    }

    /** Waits for TopComponent with given name in specified container.
     * It is activated by default.
     * @param contOper container where to search
     * @param topComponentName name of TopComponent (it used to be label of tab)
     */
    public TopComponentOperator(ContainerOperator contOper, String topComponentName) {
        this(contOper, topComponentName, 0);
    }

    /** Waits for index-th TopComponent in specified container.
     * It is activated by default.
     * @param contOper container where to search
     * @param index index of TopComponent to be find
     */
    public TopComponentOperator(ContainerOperator contOper, int index) {
        this(contOper, null, index);
    }

    /** Waits for first TopComponent in specified container.
     * It is activated by default.
     * @param contOper container where to search
     */
    public TopComponentOperator(ContainerOperator contOper) {
        this(contOper, null, 0);
    }

    /** Waits for index-th TopComponent with given name in whole IDE.
     * It is activated by default.
     * @param topComponentName name of TopComponent (it used to be label of tab)
     * @param index index of TopComponent to be find
     */
    public TopComponentOperator(String topComponentName, int index) {
        this(waitTopComponent(topComponentName, index));

    }

    /** Waits for first TopComponent with given name in whole IDE.
     * It is activated by default.
     * @param topComponentName name of TopComponent (it used to be label of tab)
     */
    public TopComponentOperator(String topComponentName) {
        this(topComponentName, 0);
    }

    /** Creates new instance from given TopComponent.
     * It is activated by default.
     * This constructor is used in properties.PropertySheetOperator.
     * @param jComponent instance of JComponent
     */
    public TopComponentOperator(JComponent jComponent) {
        super(jComponent);
        makeComponentVisible();
    }

    /** Makes active window in which this top component resides (main window
     * in joined mode) and then activates this top component to be in the
     * foreground.
     */
    @Override
    public void makeComponentVisible() {
        // Make active window in which this TopComponent resides.
        // It is necessary e.g. for keyboard focus
        super.makeComponentVisible();
        //  Check if it is really TopComponent. It doesn't have to be
        // for example for PropertySheetOperator in Options window.
        // In that case do nothing.
        if (getSource() instanceof TopComponent) {
            // activate TopComponent, i.e. switch tab control to be active.
            // run in dispatch thread
            runMapping(new MapVoidAction("requestActive") {

                @Override
                public void map() {
                    ((TopComponent) getSource()).requestActive();
                }
            });
        }
    }

    /** Attaches this top component to a new position defined by target top
     * component and side.
     * @param targetTopComponentName name of top component defining a position
     * where to attach top component
     * @param side side where to attach top component ({@link AttachWindowAction#LEFT},
     * {@link AttachWindowAction#RIGHT}, {@link AttachWindowAction#TOP},
     * {@link AttachWindowAction#BOTTOM}, {@link AttachWindowAction#AS_LAST_TAB})
     */
    public void attachTo(String targetTopComponentName, String side) {
        new AttachWindowAction(targetTopComponentName, side).perform(this);
    }

    /** Attaches this top component to a new position defined by target top
     * component and side.
     * @param targetTopComponentOperator operator of top component defining a position
     * where to attach top component
     * @param side side where to attach top component ({@link AttachWindowAction#LEFT},
     * {@link AttachWindowAction#RIGHT}, {@link AttachWindowAction#TOP},
     * {@link AttachWindowAction#BOTTOM}, {@link AttachWindowAction#AS_LAST_TAB})
     */
    public void attachTo(TopComponentOperator targetTopComponentOperator, String side) {
        new AttachWindowAction(targetTopComponentOperator, side).perform(this);
    }

    /** Maximizes this top component. */
    public void maximize() {
        new MaximizeWindowAction().perform(this);
    }

    /** Restores maximized window. */
    public void restore() {
        new RestoreWindowAction().perform(this);
    }

    /** Clones this TopComponent. TopComponent is activated before
     * action is performed. */
    public void cloneDocument() {
        new CloneViewAction().perform(this);
    }

    /** Closes this TopComponent and wait until it is closed.
     * TopComponent is activated before action is performed. */
    public void closeWindow() {
        if (isModified()) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    pushMenuOnTab(closeWindowItem);
                }
             ;
            }, "thread to close TopComponent").start();
        } else {
            new CloseViewAction().perform(this);
            waitComponentShowing(false);
        }
    }

    /** Closes this TopComponent instance by IDE API call and wait until
     * it is not closed. If this TopComponent is modified (e.g. editor top
     * component), it discards possible changes.
     * @see #close
     */
    public void closeDiscard() {
        setUnmodified();
        close();
    }

    /** Finds DataObject for the content of this TopComponent and set it
     * unmodified. Used in closeDiscard method.
     */
    public void setUnmodified() {
        DataObject dob = ((TopComponent) getSource()).getLookup().lookup(DataObject.class);
        if (dob == null) {
            // try to find possible enclosing MultiviewTopComponent
            TopComponentOperator parentTco = findParentTopComponent();
            if (parentTco != null) {
                parentTco.setUnmodified();
            }
        } else {
            dob.setModified(false);
        }
    }

    /** Returns true if this top component is modified (e.g. source in editor)
     * @return boolean true if this object is modified; false otherwise
     */
    public boolean isModified() {
        Savable savable = ((TopComponent) getSource()).getLookup().lookup(Savable.class);
        if (savable == null) {
            // try to find possible enclosing MultiviewTopComponent
            TopComponentOperator parentTco = findParentTopComponent();
            if (parentTco != null) {
                return parentTco.isModified();
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /** Saves content of this TopComponent. If it is not applicable or content
     * of TopComponent is not modified, it does nothing.
     */
    public void save() {
        Savable savable = ((TopComponent) getSource()).getLookup().lookup(Savable.class);
        if (savable == null) {
            // try to find possible enclosing MultiviewTopComponent
            TopComponentOperator parentTco = findParentTopComponent();
            if (parentTco != null) {
                parentTco.save();
            }
        } else {
            try {
                savable.save();
            } catch (IOException e) {
                throw new JemmyException("Saving of TopComponent " + ((TopComponent) getSource()).getDisplayName() + " failed (Savable=" + savable + ").", e);  //NOI18N
            }
        }
    }

    /** Closes this TopComponent instance by IDE API call and wait until
     * it is not closed. If this TopComponent is modified (e.g. editor top
     * component), question dialog is shown and you have to close it. To close
     * this TopComponent and discard possible changes use {@link #closeDiscard}
     * method.
     */
    public void close() {
        if (isModified()) {
            // need to call it by popup because it is impossible to call
            // TopComponent.close in AWT thread and handle question dialog in a different thread
            closeWindow();
        } else {
            if (isOpened()) {
                // run in dispatch thread
                runMapping(new MapVoidAction("close") {

                    @Override
                    public void map() {
                        ((TopComponent) getSource()).close();
                    }
                });
            } else {
                // try to find enclosing MultiviewTopComponent
                TopComponentOperator parent = findParentTopComponent();
                if (parent != null) {
                    parent.close();
                }
            }
            waitComponentShowing(false);
        }
    }

    /** Closes all opened documents and waits until this top component is closed. */
    public void closeAllDocuments() {
        DataObject[] modifs = DataObject.getRegistry().getModified();
        if (modifs.length != 0) {
            // some object modified => need to call in new thread because modal question dialog appears
            new Thread(new Runnable() {

                @Override
                public void run() {
                    pushMenuOnTab(closeAllDocumentsItem);
                }
             ;
            }, "thread to closeAllDocuments").start();
        } else {
            // no object modified
            new CloseAllDocumentsAction().perform(this);
            waitComponentShowing(false);
        }
    }

    /** Finds index-th TopComponent with given name in whole IDE.
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @return TopComponent instance or null if noone matching criteria was found
     */
    public static JComponent findTopComponent(String name, int index) {
        return findTopComponent(null, name, index, null);
    }

    /** Finds index-th TopComponent with given name in IDE registry.
     * @param cont container where to search
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @param subchooser ComponentChooser to determine exact TopComponent
     * @return TopComponent instance or null if noone matching criteria was found
     */
    protected static JComponent findTopComponent(final ContainerOperator cont, final String name, final int index, final ComponentChooser subchooser) {
        // run in dispatch thread
        return (JComponent) new QueueTool().invokeSmoothly(new QueueTool.QueueAction("findTopComponent") {    // NOI18N

            @Override
            public Object launch() {
                int counter = index;
                Object tc[] = TopComponent.getRegistry().getOpened().toArray();
                StringComparator comparator = cont == null ? Operator.getDefaultStringComparator() : cont.getComparator();
                TopComponent c;
                // loop through showing TopComponents
                for (int i = 0; i < tc.length; i++) {
                    c = (TopComponent) tc[i];
                    if (c.isShowing()
                            && (comparator.equals(c.getName(), name) || comparator.equals(c.getDisplayName(), name))
                            && isUnder(cont, c)) {

                        JComponent result = checkSubchooser(c, subchooser);
                        if (result != null) {
                            if (--counter < 0) {
                                return result;
                            }
                        }
                    }
                }
                // loop through NOT showing TopComponents but parent has to be showing
                for (int i = 0; i < tc.length; i++) {
                    c = (TopComponent) tc[i];
                    if ((!c.isShowing()) && isParentShowing(c)
                            && (comparator.equals(c.getName(), name) || comparator.equals(c.getDisplayName(), name))
                            && isUnder(cont, c)) {

                        JComponent result = checkSubchooser(c, subchooser);
                        if (result != null) {
                            if (--counter < 0) {
                                return result;
                            }
                        }
                    }
                }
                return null;
            }
        });
    }

    /** If subchooser is null, return TopComponent.
     * Else if c is instance of MultiViewCloneableTopComponent try to find
     * and return sub component in MVCTC corresponding to sub chooser. Else
     * check TC in sub chooser and return it if matches. MVCTC can host
     * several views, e.g. source and design view in form editor or xml, servlets,
     * overview views in web.xml editor. Then EditorOperator is able to find
     * appropriate CloneableEditor in MVCTC.
     * @param c TopComponent to check
     * @param subchooser ComponentChooser to check if matches
     * @return given TopComponent or appropriate sub component
     */
    private static JComponent checkSubchooser(TopComponent c, ComponentChooser subchooser) {
        if (subchooser == null) {
            return c;
        } else {
            boolean isMultiView = false;
            try {
                //isMultiView = c instanceof MultiViewCloneableTopComponent;
                isMultiView = isMultyView(c);
            } catch (Throwable t) {
                // ignore possible NoClassDefFoundError because org.netbeans.core.multiview module is not enabled in IDE
            }
            if (isMultiView) {
                TopComponentOperator tco = new TopComponentOperator((JComponent) c);
                // suppress output when finding sub component
                tco.setOutput(TestOut.getNullOutput());
                return (JComponent) tco.findSubComponent(subchooser);
            } else {
                if (subchooser.checkComponent(c)) {
                    return c;
                }
            }
        }
        return null;
    }

    private static boolean isMultyView(TopComponent c) {
        Class clz = c.getClass();
        do {
            if (clz.getName().equals("org.netbeans.core.multiview.MultiViewCloneableTopComponent")) {
                return true;
            }
        } while ((clz = clz.getSuperclass()) != null);
        return false;
    }

    private static boolean isParentShowing(Component c) {
        while (c != null) {
            if (c.isShowing()) {
                return true;
            }
            c = c.getParent();
        }
        return false;
    }

    private static boolean isUnder(ContainerOperator cont, Component c) {
        if (cont == null) {
            return true;
        }
        Component comp = cont.getSource();
        while (comp != c && c != null) {
            c = c.getParent();
        }
        return (comp == c);
    }

    /** Waits for index-th TopComponent with given name in IDE registry.
     * It throws JemmyException when TopComponent is not find until timeout
     * expires.
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @return TopComponent instance or throws JemmyException if not found
     * @see #findTopComponent
     */
    protected static JComponent waitTopComponent(final String name, final int index) {
        return waitTopComponent(null, name, index, null);
    }

    /** Waits for index-th TopComponent with given name in IDE registry.
     * It throws JemmyException when TopComponent is not find until timeout
     * expires.
     * @param cont container where to search
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @param subchooser ComponentChooser to determine exact TopComponent
     * @return TopComponent instance or throws JemmyException if not found
     * @see #findTopComponent
     */
    protected static JComponent waitTopComponent(final ContainerOperator cont, final String name, final int index, final ComponentChooser subchooser) {
        try {
            Waiter waiter = new Waiter(new Waitable() {

                @Override
                public Object actionProduced(Object obj) {
                    return findTopComponent(cont, name, index, subchooser);
                }

                @Override
                public String getDescription() {
                    return ("Wait TopComponent with name=" + name
                            + " index=" + String.valueOf(index)
                            + (subchooser == null ? "" : " subchooser=" + subchooser.getDescription())
                            + " loaded");
                }
            });
            Timeouts times = JemmyProperties.getCurrentTimeouts().cloneThis();
            times.setTimeout("Waiter.WaitingTime", times.getTimeout("ComponentOperator.WaitComponentTimeout"));
            waiter.setTimeouts(times);
            waiter.setOutput(JemmyProperties.getCurrentOutput());
            return ((JComponent) waiter.waitAction(null));
        } catch (InterruptedException e) {
            return (null);
        }
    }

    /** Makes top component active and pushes given menu on its tab.
     * @param popupPath menu path separated by '|' (e.g. "CVS|Refresh")
     */
    public void pushMenuOnTab(String popupPath) {
        if (isOpened()) {
            this.makeComponentVisible();
            TabbedContainer ta = findTabbedAdapter();

            int index = ta.indexOf((TopComponent) getSource());

            Rectangle r = new Rectangle();
            ta.getTabRect(index, r);
            Point p = new Point(r.x + (r.width / 2), r.y + (r.height / 2));
            Component tabsComp = ta.getComponentAt(p);
            new JPopupMenuOperator(JPopupMenuOperator.callPopup(tabsComp, p.x, p.y)).pushMenu(popupPath);
        } else {
            // try to find enclosing MultiviewTopComponent
            TopComponentOperator parent = findParentTopComponent();
            if (parent != null) {
                parent.pushMenuOnTab(popupPath);
            }
        }
    }

    /** Returns TabbedAdapter component from parents hierarchy.
     * Used also in EditorWindowOperator.
     */
    public TabbedContainer findTabbedAdapter() {
        Container parent = getSource().getParent();
        while (parent != null) {
            if (parent instanceof TabbedContainer) { // NOI18N
                return (TabbedContainer) parent;
            } else {
                parent = parent.getParent();
            }
        }
        return null;
    }

    public Container findTabDisplayer() {
        return ContainerOperator.findContainer(findTabbedAdapter(), new ComponentChooser() {

            @Override
            public boolean checkComponent(Component comp) {
                return comp.getClass().getName().endsWith("TabDisplayer");
            }

            @Override
            public String getDescription() {
                return "org.netbeans.swing.tabcontrol.TabDisplayer";
            }
        });
    }

    /**
     * Waits the topcomponent to be closed.
     */
    public void waitClosed() {
        getOutput().printLine("Wait topcomponent to be closed \n    : "
                + getSource().toString());
        getOutput().printGolden("Wait topcomponent to be closed");
        waitState(new ComponentChooser() {

            @Override
            public boolean checkComponent(Component comp) {
                return (!comp.isVisible());
            }

            @Override
            public String getDescription() {
                return ("Closed topcomponent");
            }
        });
    }

    /** Returns true if this TopComponent is opened. If it is not opened, it
     * usually means it is contained within MultiviewTopComponent.
     * @return true if open, false otherwise
     */
    protected boolean isOpened() {
        // run in dispatch thread
        return runMapping(new MapBooleanAction("isOpened") { // NOI18N

            @Override
            public boolean map() {
                return ((TopComponent) getSource()).isOpened();
            }
        });
    }

    /** Returns TopComponentOperator from parents hierarchy. It should be
     * MultiviewTopComponent.
     */
    protected TopComponentOperator findParentTopComponent() {
        Component parent = getSource().getParent();
        while (parent != null) {
            if (parent instanceof TopComponent) {
                return new TopComponentOperator((JComponent) parent);
            } else {
                parent = parent.getParent();
            }
        }
        return null;
    }
}
