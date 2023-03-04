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
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ViewportUI;

import org.netbeans.modules.openide.explorer.TabbedContainerBridge;


/**
 * UI handling portion of the property sheet - handles installing/uninstalling
 * the description area, the tabbed container, etc. automatically.  AddImpl
 * is overridden to always do the correct thing in terms of what is added -
 * if you add a description panel, it will automatically be wrapped in a
 * JSplitPane and the table moved inside that;  if a tab control is added,
 * the split pane, if present, or the scroll pane if not, will automatically
 * be set as the inner component of the SheetTabbedPane, etc.  Adds can
 * happen in any order.
 * <p>
 * This class contains no PropertySheet specific logic (and it should stay
 * that way), only the code that manages toggling between the states of
 * having a description panel, having tabs, etc.  This can be handled quite
 * transparently through setState().
 * <p>
 * This class contains a considerable amount of component management logic;
 * however, it is all done in the simplest and most straightforward way
 * possible (with the exception that org.netbeans.swing.TabControl is accessed
 * through a bridge via Lookup, because OpenIDE may not use module code
 * directly).
 * The goal is to a., make the component management as bulletproof, readable
 * and debuggable as possible.  So this class should provide accessor methods
 * to manipulate its child components, but under no circumstances should
 * external code ever reference any of its internal components directly -
 * only accessor methods on this class should be used for such purposes.
 *
 *
 * @author  Tim Boudreau
 */
class PSheet extends JPanel implements MouseListener {
    public static final int STATE_HAS_DESCRIPTION = 1;
    public static final int STATE_HAS_TABS = 2;
    private int addCount = 0;
    private String description = ""; //NOI18N
    private String title = ""; //NOI18N
    private SelectionAndScrollPositionManager manager = new SelectionAndScrollPositionManager();
    private boolean adjusting = false;
    private boolean helpEnabled = true;
    private boolean marginPainted = !PropUtils.neverMargin;
    private Color marginColor = UIManager.getColor("controlShadow");
    private String emptyString = "THIS IS A BUG"; //NOI18N
    private Boolean firstSplit = null;
    private Object[] tabbedContainerObjects = new String[] { "Hello", "World", "This", "Is", "Me" };
    private String[] tabbedContainerTitles = new String[] { "Tab 1", "Tab 2", "Tab 3", "Tab 4", "Tab 5" };
    private ChangeListener selectionListener = null;

    public PSheet() {
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK), "popup"
        ); //NOI18N
        
        getActionMap().put("popup", new PopupAction()); //NOI18N
        getActionMap().put("PreviousViewAction", new SwitchTabAction(-1)); //NOI18N
        getActionMap().put("NextViewAction", new SwitchTabAction(1)); //NOI18N
        if( PropUtils.isAqua ) {
            setBackground( UIManager.getColor("NbExplorerView.background") ); //NOI18N
            setOpaque(true);
        }
    }

    SelectionAndScrollPositionManager manager() {
        return manager;
    }

    public void adjustForName(String nodeName) {
        adjusting = true;

        try {
            JComponent comp = findTabbedContainer();
            String tabname = null;

            if (comp != null) {
                boolean success = false;
                
                //first try to keep the last used property group
                tabname = manager().getLastSelectedGroupName();
                if( tabname != null && tabname.length() > 0 ) {
                    success = TabbedContainerBridge.getDefault().setSelectionByName(comp, tabname);
                }
                
                //now try if there's a stored property group for the new node 
                if( !success ) {
                tabname = manager().getGroupNameForNodeName(nodeName);
                    if( tabname != null && tabname.length() > 0 ) {
                        success = TabbedContainerBridge.getDefault().setSelectionByName(comp, tabname);
                    }
                }

                //just use default property group
                if( !success ) {
                    tabname = PropUtils.basicPropsTabName(); // use basic tab
                    success = TabbedContainerBridge.getDefault().setSelectionByName(comp, tabname);
                }

                if (success) {
                    if (selectionListener != null) {
                        ChangeEvent ce = new ChangeEvent(this);
                        selectionListener.stateChanged(ce);
                    }
                }
            }

            JScrollPane jsc = findScrollPane();

            if (jsc != null) {
                String s = (tabname == null) ? manager().getCurrentNodeName() : tabname;

                if (s != null) { //Will be null the very first time

                    int pos = manager().getScrollPositionForNodeName(s);

                    if ((pos >= 0) && (pos < jsc.getVerticalScrollBar().getModel().getMaximum())) {
                        jsc.getVerticalScrollBar().getModel().setValue(pos);
                    }
                }
            }
        } finally {
            adjusting = false;
        }
    }

    public boolean isAdjusting() {
        return adjusting;
    }

    public void storeScrollAndTabInfo() {
        JComponent comp = findTabbedContainer();
        String tab = null;
        String node = manager().getCurrentNodeName();
        String lastTab = manager().getLastSelectedGroupName();

        if (node != null) {
            if (comp != null) {
                tab = TabbedContainerBridge.getDefault().getCurrentSelectedTabName(comp);

                if (tab != null) {
                    manager().storeLastSelectedGroup(tab);
                }
            }

            JScrollPane jsc = findScrollPane();

            if (jsc != null) {
                int pos = jsc.getVerticalScrollBar().getModel().getValue();
                String nm = (lastTab != null) ? lastTab : ((tab != null) ? tab : node);
                manager().storeScrollPosition(pos, nm);
            }
        }
    }

    /**
     * Set the description to be displayed in the description panel.  If the
     * description panel is not visible, the value will be cached and applied
     * the next time it is made visible.
     */
    public void setDescription(String title, String txt) {
        this.description = txt;
        this.title = title;

        DescriptionComponent desc = findDescriptionComponent();

        if (desc != null) {
            desc.setDescription(title, txt);
        }
    }

    /**
     * Set whether or not the help button in the description component should
     * be enabled.
     */
    public void setHelpEnabled(boolean val) {
        if (helpEnabled != val) {
            helpEnabled = val;

            DescriptionComponent desc = findDescriptionComponent();

            if (desc != null) {
                desc.setHelpEnabled(val);
            }
        }
    }

    /** Set whether or not the margin should be painted in the viewport */
    public void setMarginPainted(boolean val) {
        if (marginPainted != val) {
            marginPainted = val;

            MarginViewportUI ui = findMVUI();

            if (ui != null) {
                ui.setMarginPainted(val);
            }
        }
    }

    /** Set the color of the margin */
    public void setMarginColor(Color c) {
        if (!c.equals(marginColor)) {
            marginColor = c;

            MarginViewportUI ui = findMVUI();

            if (ui != null) {
                ui.setMarginColor(c);
            }
        }
    }

    /** Set the string that should be painted when the table has 0 height */
    public void setEmptyString(String s) {
        if (!s.equals(emptyString)) {
            emptyString = s;

            MarginViewportUI ui = findMVUI();

            if (ui != null) {
                ui.setEmptyString(s);
            }
        }
    }

    /**
     * Find the MarginViewportUI owned by the scrollpane, to set up
     * colors, margin, empty string */
    private MarginViewportUI findMVUI() {
        MarginViewportUI result = null;
        JScrollPane pane = findScrollPane();

        if (pane != null) {
            ViewportUI ui = pane.getViewport().getUI();

            if (ui instanceof MarginViewportUI) {
                result = (MarginViewportUI) ui;
            } else {
                //L&F changed or something such
                result = (MarginViewportUI) MarginViewportUI.createUI(pane.getViewport());
                pane.getViewport().setUI(result);
            }
        }

        return result;
    }

    /**
     * Overridden to handle our layout requirements
     */
    @Override
    public void doLayout() {
        Component[] c = getComponents();

        if (c.length > 0 && getWidth() >= 0 && getHeight() >= 0) {
            Insets ins = getInsets();
            c[0].setBounds(ins.left, ins.top, getWidth() - (ins.right + ins.left), getHeight() - ins.top + ins.bottom);

            if (c[0] instanceof JSplitPane && Boolean.TRUE.equals(firstSplit)) {
                JSplitPane pane = (JSplitPane) c[0];
                pane.setDividerLocation(0.80f);
                pane.resetToPreferredSizes();

                JComponent dc = findDescriptionComponent();

                if (dc != null) {
                    if (dc.getHeight() > 0) {
                        firstSplit = Boolean.FALSE;
                    }
                } else {
                    firstSplit = Boolean.FALSE;
                }
            }

            if (c.length > 1) {
                throw new IllegalStateException("Hmm, something is wrong: " + Arrays.asList(c));
            }
        }
    }

    /** Transfers focus to the table */
    @Override
    public void requestFocus() {
        JScrollPane jsc = findScrollPane();

        if ((jsc != null) && (jsc.getViewport().getView() != null)) {
            jsc.getViewport().getView().requestFocus();
        }
    }

    /** Transfers focus to the table */
    @Override
    public boolean requestFocusInWindow() {
        JScrollPane jsc = findScrollPane();

        if ((jsc != null) && (jsc.getViewport().getView() != null)) {
            return jsc.getViewport().getView().requestFocusInWindow();
        } else {
            return false;
        }
    }

    /**
     * Set the state of the component.  The state is a bitmask of
     * STATE_HAS_DESCRIPTION and STATE_HAS_TABS, and may be 0 to indicate
     * no tabs or description.  Calling this method will change the
     * component hierarchy to reflect the requested state.
     */
    public void setState(int state) {
        if (state != getState()) {
            synchronized (getTreeLock()) {
                switch (state) {
                case 0:

                    JComponent tc = findTabbedContainer();

                    if (tc != null) {
                        remove(tc);
                    }

                    JSplitPane jsp = findSplitPane();

                    if (jsp != null) {
                        remove(jsp);
                    }

                    break;

                case PSheet.STATE_HAS_DESCRIPTION:

                    JSplitPane split = findSplitPane();
                    remove(findTabbedContainer());

                    if (split != null) {
                        addImpl(split, null, 0);
                    } else {
                        addImpl(createDescriptionComponent(), null, 0);
                    }

                    break;

                case PSheet.STATE_HAS_TABS:

                    JScrollPane jsc = findScrollPane();
                    JComponent tct = findTabbedContainer();

                    if (tct == null) {
                        addImpl(createTabbedContainer(), null, 0);
                    }

                    JSplitPane spl = findSplitPane();

                    if (spl != null) {
                        remove(spl);
                    }

                    if (jsc != null) {
                        setTabbedContainerInnerComponent(findTabbedContainer(), jsc);
                    }

                    adjustForName(manager.getCurrentNodeName());

                    break;

                case (PSheet.STATE_HAS_DESCRIPTION | PSheet.STATE_HAS_TABS):

                    JComponent tcc = findTabbedContainer();
                    JSplitPane splt = findSplitPane();
                    JScrollPane scrl = findScrollPane();

                    if (tcc == null) {
                        tcc = createTabbedContainer();
                        addImpl(tcc, null, 0);
                    }

                    if (splt == null) {
                        addImpl(createDescriptionComponent(), null, 0);
                        splt = findSplitPane();
                    }

                    setTabbedContainerInnerComponent(tcc, splt);

                    if (scrl != null) {
                        splt.setLeftComponent(scrl);
                    }

                    adjustForName(manager.getCurrentNodeName());

                    break;

                default:
                    throw new IllegalArgumentException(Integer.toString(state));
                }
            }
        }

        revalidate();
        repaint();
    }

    /**
     * Get the current state of the component, as defined as a bitmask of
     * STATE_HAS_TABS and STATE_HAS_DESCRIPTION.
     */
    public int getState() {
        int result = 0;

        if (findTabbedContainer() != null) {
            result |= STATE_HAS_TABS;
        }

        if (findSplitPane() != null) {
            result |= STATE_HAS_DESCRIPTION;
        }

        return result;
    }

    /**
     * Overridden to handle component adds/removes.  This class has very
     * specific ideas about what to do with things that are added to it, such
     * that getting it into the right state is simply a matter of calling add
     * or remove with the right component.  To wit:
     * <ul>
     * <li>Adding a JTable adds a JScrollPane with the JTable in it.  If a
     *  split pane is present, it is added to the split pane;  if a tabbed
     *  pane is present and a split pane isn't, it is added to the tabbed
     *  pane, otherwise it's added to the instance of this class itself.
     * </li>
     * <li>Adding a DescriptionComponent will cause a JSplitPane to be
     * installed, and any scroll pane present moved into it;  split pane
     * will either be added to the container itself, or to a tabbed
     * pane if present.
     * </li>
     * </ul>
     * Basically, all of the logic that keeps the component state correct
     * lives here and in remove(), and all outside code needs to do is
     * add/remove things, or even more simply, call setState(), which handles
     * that.
     */
    @Override
    protected void addImpl(Component comp, Object constraints, int idx) {
        if (
            !(comp instanceof JSplitPane || comp instanceof JScrollPane || comp instanceof DescriptionComponent ||
                comp instanceof JTable ||
                (comp instanceof JComponent && Boolean.TRUE.equals(((JComponent) comp).getClientProperty("tc"))))
        ) {
            throw new IllegalArgumentException("Unexpected component " + comp);
        }

        synchronized (getTreeLock()) {
            addCount++;

            try {
                if (!Arrays.asList(comp.getMouseListeners()).contains(this)) {
                    comp.addMouseListener(this);
                }

                if (comp instanceof JTable) {
                    JScrollPane jsc = findScrollPane();

                    if (jsc == null) {
                        jsc = createScrollPane(comp);
                    } else {
                        jsc.setViewportView(comp);
                    }

                    JSplitPane split = findSplitPane();

                    if (split != null) {
                        split.setLeftComponent(jsc);
                        split.revalidate();
                    } else {
                        JComponent tc = findTabbedContainer();

                        if (tc != null) {
                            setTabbedContainerInnerComponent(tc, split);
                        } else {
                            addImpl(jsc, constraints, idx);
                        }
                    }
                } else if (comp instanceof DescriptionComponent) {
                    JSplitPane pane = findSplitPane();
                    boolean hadPane = pane != null;

                    if (pane == null) {
                        pane = createSplitPane(comp);
                    }

                    JScrollPane scroll = findScrollPane();

                    if (scroll != null) {
                        pane.setLeftComponent(scroll);
                    }

                    if (!hadPane) {
                        addImpl(pane, constraints, idx);
                    }

                    ((DescriptionComponent) comp).setDescription(title, description);
                    ((DescriptionComponent) comp).setHelpEnabled(helpEnabled);
                } else if (isTabbedContainer(comp)) {
                    JSplitPane split = findSplitPane();

                    if (split != null) {
                        super.remove(split);
                        setTabbedContainerInnerComponent((JComponent) comp, split);
                    } else {
                        JScrollPane pane = findScrollPane();

                        if (pane != null) {
                            setTabbedContainerInnerComponent((JComponent) comp, pane);
                            remove(pane);
                        }
                    }

                    super.addImpl(comp, constraints, idx);
                } else if (comp instanceof JScrollPane) {
                    JSplitPane split = findSplitPane();

                    if (split != null) {
                        split.setLeftComponent(comp);
                        split.revalidate();
                    } else {
                        JComponent tc = findTabbedContainer();

                        if (tc != null) {
                            setTabbedContainerInnerComponent(tc, (JComponent) comp);
                        } else {
                            super.addImpl(comp, constraints, idx);
                        }
                    }
                } else if (comp instanceof JSplitPane) {
                    JScrollPane jsc = findScrollPane();

                    if (jsc != null) {
                        ((JSplitPane) comp).setLeftComponent(jsc);
                    }

                    JComponent tc = findTabbedContainer();

                    if (tc != null) {
                        setTabbedContainerInnerComponent(tc, (JComponent) comp);
                    } else {
                        super.addImpl(comp, constraints, idx);
                    }
                } else {
                    super.addImpl(comp, constraints, idx);
                }
            } finally {
                addCount--;
                revalidate();
            }
        }
    }

    /**
     * Remove a component.  Overridden to handle management of state, nested
     * components, etc.  It is legitimate to call this method with null
     * (for convenience), or with a component that is a child of a child of
     * this container.
     */
    @Override
    public void remove(Component c) {
        if (c == null) {
            return;
        }

        c.removeMouseListener(this);

        synchronized (getTreeLock()) {
            if (c.getParent() == this) {
                super.remove(c);

                if (adding()) {
                    return;
                }
            }

            if (isTabbedContainer(c)) {
                Component inner = getTabbedContainerInnerComponent((JComponent) c);

                if (inner != null) {
                    addImpl(inner, null, 0);
                }
            } else if (c instanceof JSplitPane) {
                if (c.getParent() != null) {
                    c.getParent().remove(c);
                }

                Component inner = ((JSplitPane) c).getLeftComponent();

                if (inner != null) {
                    addImpl(inner, null, 0);
                }
            } else if (c instanceof DescriptionComponent) {
                JSplitPane jsp = findSplitPane();

                if (jsp != null) {
                    jsp.remove(c);
                    remove(jsp);
                }
            }
        }

        revalidate();
    }

    /**
     * Determine if a call to addImpl() is implicit or explicit.  remove()
     * uses this method to determine if it is being called from within
     * addImpl() or not, as its behavior will be different in that case.
     */
    private boolean adding() {
        return addCount > 0;
    }

    /**
     * Create a description component */
    private DescriptionComponent createDescriptionComponent() {
        return new DescriptionComponent();
    }

    private JSplitPane createSplitPane(Component lower) {
        JSplitPane pane = new JSplitPane();

        if (firstSplit == null) {
            firstSplit = Boolean.TRUE;
        } else {
            firstSplit = Boolean.FALSE;
        }

        pane.setRightComponent(lower);
        pane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        pane.setContinuousLayout(true);
        pane.setResizeWeight(1);
        pane.setDividerLocation(0.80f);
        pane.setBorder(BorderFactory.createEmptyBorder());
        //Do not install our custom split pane UI on Nimbus L&F
        if (!"Nimbus".equals(UIManager.getLookAndFeel().getID())) {
            pane.setUI(PropUtils.createSplitPaneUI());
        }

        // #52188: default F6 behaviour doesn't make to much sense in NB 
        // property sheet and blocks NetBeans default F6
        pane.getActionMap().getParent().remove("toggleFocus");
        if( PropUtils.isAqua ) {
            pane.setBackground( UIManager.getColor("NbExplorerView.background") ); //NOI18N
        }

        return pane;
    }

    private JScrollPane createScrollPane(Component inner) {
        JScrollPane result = new JScrollPane(inner);
        JViewport vp = result.getViewport();
        vp.addMouseListener(this);

        MarginViewportUI ui = (MarginViewportUI) MarginViewportUI.createUI(vp);
        vp.setUI(ui);
        ui.setMarginPainted(marginPainted);
        ui.setMarginColor(marginColor);
        ui.setEmptyString(emptyString);
        result.setBorder(BorderFactory.createEmptyBorder());
        result.setViewportBorder(result.getBorder());

        return result;
    }

    private JComponent createTabbedContainer() {
        JComponent result = TabbedContainerBridge.getDefault().createTabbedContainer();
        result.putClientProperty("tc", Boolean.TRUE);
        configureTabbedContainer(tabbedContainerObjects, tabbedContainerTitles, result);

        if (selectionListener != null) {
            TabbedContainerBridge.getDefault().attachSelectionListener(result, selectionListener);
        }

        return result;
    }

    public void setTabbedContainerItems(Object[] o, String[] s) {
        adjusting = true;

        try {
            tabbedContainerObjects = o;
            tabbedContainerTitles = s;

            if (o.length == 0) {
                int newState = ((getState() & STATE_HAS_DESCRIPTION) != 0) ? STATE_HAS_DESCRIPTION : 0;
                setState(newState);
            } else {
                configureTabbedContainer(o, s, null);
            }
        } finally {
            adjusting = false;
        }
    }

    public void setTabbedContainerSelection(Object item) {
        JComponent tabbed = findTabbedContainer();

        if (tabbed != null) {
            adjusting = true;

            try {
                TabbedContainerBridge.getDefault().setSelectedItem(tabbed, item);
            } finally {
                adjusting = false;
            }
        }
    }

    public Object getTabbedContainerSelection() {
        JComponent tabbed = findTabbedContainer();

        if (tabbed != null) {
            Object o = TabbedContainerBridge.getDefault().getSelectedItem(tabbed);

            if (o instanceof Node.PropertySet[]) { //won't be first time

                return o;
            }
        }

        return null;
    }

    private void configureTabbedContainer(Object[] o, String[] s, JComponent cont) {
        if (cont == null) {
            cont = findTabbedContainer();
        }

        if (cont != null) {
            TabbedContainerBridge.getDefault().setItems(cont, o, s);
        }
    }

    private void setTabbedContainerInnerComponent(JComponent tabbed, JComponent comp) {
        if (tabbed == null) {
            tabbed = findTabbedContainer();
        }

        TabbedContainerBridge.getDefault().setInnerComponent(tabbed, comp);
    }

    private static JComponent getTabbedContainerInnerComponent(JComponent c) {
        JComponent result = TabbedContainerBridge.getDefault().getInnerComponent(c);

        return result;
    }

    public void addSelectionChangeListener(ChangeListener l) {
        if (selectionListener != l) {
            JComponent comp = findTabbedContainer();
            selectionListener = l;

            if (comp != null) {
                TabbedContainerBridge.getDefault().attachSelectionListener(comp, l);
            }
        }
    }

    private static boolean isTabbedContainer(Component comp) {
        return comp instanceof JComponent && Boolean.TRUE.equals(((JComponent) comp).getClientProperty("tc")); //NOI18N
    }

    /**
     * Find the currently in use description component.
     */
    private DescriptionComponent findDescriptionComponent() {
        return (DescriptionComponent) findChildOfClass(findSplitPane(), DescriptionComponent.class);
    }

    /**
     * Find the currently in use scroll pane, if any (there should always be
     * one)
     */
    private JScrollPane findScrollPane() {
        JScrollPane result = (JScrollPane) findChildOfClass(this, JScrollPane.class);

        if (result == null) {
            result = (JScrollPane) findChildOfClass(findTabbedContainer(), JScrollPane.class);

            if (result == null) {
                result = (JScrollPane) findChildOfClass(findSplitPane(), JScrollPane.class);
            }
        }

        return result;
    }

    /**
     * Find the currently in use split pane, if any
     */
    private JSplitPane findSplitPane() {
        JSplitPane result = (JSplitPane) findChildOfClass(this, JSplitPane.class);

        if (result == null) {
            result = (JSplitPane) findChildOfClass(findTabbedContainer(), JSplitPane.class);
        }

        return result;
    }

    /**
     * Find the currently in use tabbed container, if any
     */
    private JComponent findTabbedContainer() {
        Component[] c = getComponents();

        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof JComponent && Boolean.TRUE.equals(((JComponent) c[i]).getClientProperty("tc"))) {
                return (JComponent) c[i];
            }
        }

        return null;
    }

    /**
     * Search one container for component of the requested class
     */
    private static Component findChildOfClass(Container container, Class clazz) {
        if (container == null) {
            return null;
        }

        if (isTabbedContainer((JComponent) container)) {
            Component c = getTabbedContainerInnerComponent((JComponent) container);

            if ((c != null) && (c.getClass() == clazz)) {
                return c;
            }
        } else {
            Component[] c = container.getComponents();

            for (int i = 0; i < c.length; i++) {
                if (clazz == c[i].getClass()) {
                    return c[i];
                }
            }
        }

        return null;
    }

    /**
     * Notification method that the user has requested a popup menu.
     */
    protected void popupRequested(Point p) {
        PropertySheet ps = (PropertySheet) SwingUtilities.getAncestorOfClass(PropertySheet.class, this);

        if (ps != null) {
            ps.showPopup(p);
        }
    }

    /**
     * Notification that the user has pressed the help button
     */
    protected void helpRequested() {
        PropertySheet ps = (PropertySheet) SwingUtilities.getAncestorOfClass(PropertySheet.class, this);

        if (ps != null) {
            ps.helpAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "invokeHelp")); //NOI18N
        }
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), this);
            updateSheetTableSelection(e);
            popupRequested(p);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), this);
            updateSheetTableSelection(e);
            popupRequested(p);
        }
    }

    private void updateSheetTableSelection(MouseEvent e) {
        Component comp = (Component) e.getSource();

        if (comp instanceof SheetTable) {
            SheetTable table = (SheetTable) comp;
            table.changeSelection(table.rowAtPoint(e.getPoint()), 0, false, false);
        }
    }

    public void mouseClicked(MouseEvent e) {
        //do nothing
    }

    public void mouseEntered(MouseEvent e) {
        //do nothing
    }

    public void mouseExited(MouseEvent e) {
        //do nothing
    }

    private class PopupAction extends AbstractAction {
        public void actionPerformed(ActionEvent actionEvent) {
            popupRequested(new Point(0, 0));
        }
    }
    
    private class SwitchTabAction extends AbstractAction {
        private int increment;
        public SwitchTabAction( int increment ) {
            this.increment = increment;
        }
        
        public void actionPerformed(ActionEvent actionEvent) {
            JComponent tabbed = findTabbedContainer();

            if( null == tabbed )
                return;
            
            Object o = TabbedContainerBridge.getDefault().getSelectedItem( tabbed );
            if( null == o )
                return;
            
            Object[] items = TabbedContainerBridge.getDefault().getItems( tabbed );
            int currentIndex = -1;
            for( int i=0; null != items && i<items.length; i++ ) {
                if( items[i].equals( o ) ) {
                    currentIndex = i;
                    break;
                }
            }

            if( currentIndex < 0 )
                return;
            
            int newIndex = currentIndex + increment;
            if( newIndex < 0 )
                newIndex = items.length-1;
            if( newIndex >= items.length )
                newIndex = 0;
            TabbedContainerBridge.getDefault().setSelectedItem( tabbed, items[newIndex] );
        }
    }
}
