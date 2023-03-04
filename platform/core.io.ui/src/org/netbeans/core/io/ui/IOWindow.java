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

package org.netbeans.core.io.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Utilities;
import org.openide.awt.MouseUtils;
import org.openide.awt.TabbedPaneFactory;
import org.openide.awt.ToolbarWithOverflow;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.IOContainer;
import org.openide.windows.IOContainer.CallBacks;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 *
 * @author Tomas Holy
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.windows.IOContainer.Provider.class, position=100)
public final class IOWindow implements IOContainer.Provider {
    private static IOWindowImpl impl;
    
    IOWindowImpl impl() {
        if (impl == null) {
            impl = IOWindowImpl.findDefault();
        }
        return impl;
    }

    public void add(JComponent comp, CallBacks cb) {
        impl().addTab(comp, cb);
    }

    public JComponent getSelected() {
        return impl().getSelectedTab();
    }

    public boolean isActivated() {
        return impl().isActivated();
    }

    public void open() {
        impl().open();
    }

    public void remove(JComponent comp) {
        impl().removeTab(comp);
    }

    public void requestActive() {
        impl().requestActive();
    }

    public void requestVisible() {
        impl().requestVisible();
    }

    public void select(JComponent comp) {
        impl().selectTab(comp);
    }

    public void setIcon(JComponent comp, Icon icon) {
        impl().setIcon(comp, icon);
    }

    public void setTitle(JComponent comp, String name) {
        impl().setTitle(comp, name);
    }

    public void setToolTipText(JComponent comp, String text) {
        impl().setToolTipText(comp, text);
    }

    public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
        impl().setToolbarActions(comp, toolbarActions);
    }

    public boolean isCloseable(JComponent comp) {
        return true;
    }

    public static final class IOWindowImpl extends TopComponent implements ChangeListener, PropertyChangeListener {

        public static IOWindowImpl DEFAULT;

        static synchronized IOWindowImpl findDefault() {
            if (DEFAULT == null) {
                TopComponent tc = WindowManager.getDefault().findTopComponent("output"); // NOI18N
                if (tc != null) {
                    if (tc instanceof IOWindowImpl) {
                        DEFAULT = (IOWindowImpl) tc;
                    } else {
                        //This should not happen. Possible only if some other module
                        //defines different settings file with the same name but different class.
                        //Incorrect settings file?
                        IllegalStateException exc = new IllegalStateException("Incorrect settings file. Unexpected class returned." // NOI18N
                                + " Expected: " + IOWindowImpl.class.getName() // NOI18N
                                + " Returned: " + tc.getClass().getName()); // NOI18N
                        Logger.getLogger(IOWindowImpl.class.getName()).log(Level.WARNING, null, exc);
                        //Fallback to accessor reserved for window system.
                        IOWindowImpl.getDefault();
                    }
                } else {
                    IOWindowImpl.getDefault();
                }
            }
            DEFAULT.getActionMap().remove("org.openide.actions.FindAction"); // NOI18N
            return DEFAULT;
        }

        /* Singleton accessor reserved for window system ONLY. Used by window system to create
         * IOWindowImpl instance from settings file when method is given. Use <code>findDefault</code>
         * to get correctly deserialized instance of IOWindowImpl. */
        public static synchronized IOWindowImpl getDefault() {
            if (DEFAULT == null) {
                DEFAULT = new IOWindowImpl();
            }
            return DEFAULT;
        }

        public Object readResolve() throws java.io.ObjectStreamException {
            return getDefault();
        }

        private static final String ICON_PROP = "tabIcon"; //NOI18N
        private static final String TOOLBAR_ACTIONS_PROP = "toolbarActions"; //NOI18N
        private static final String TOOLBAR_BUTTONS_PROP = "toolbarButtons"; //NOI18N
        private static final String ICON_RESOURCE = "org/netbeans/core/io/ui/output.png"; // NOI18N
        private static final boolean AQUA = "Aqua".equals(UIManager.getLookAndFeel().getID()); // NOI18N
        private JTabbedPane pane;
        private JComponent singleTab;
        private JToolBar toolbar;
        private JPopupMenu popupMenu;
        private Map<JComponent, CallBacks> tabToCb = new HashMap<JComponent, CallBacks>();

        public IOWindowImpl() {
            pane = TabbedPaneFactory.createCloseButtonTabbedPane();
            pane.addChangeListener(this);
            pane.addPropertyChangeListener(TabbedPaneFactory.PROP_CLOSE, this);
            setFocusable(true);

            toolbar = new ToolbarWithOverflow();
            toolbar.setOrientation(JToolBar.VERTICAL);
            toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
            toolbar.setFloatable(false);
            Insets ins = toolbar.getMargin();
            JButton sample = new JButton();
            sample.setBorderPainted(false);
            sample.setOpaque(false);
            sample.setText(null);
            sample.setIcon(new Icon() {

                public int getIconHeight() {
                    return 16;
                }

                public int getIconWidth() {
                    return 16;
                }

                public void paintIcon(Component c, Graphics g, int x, int y) {
                }
            });
            toolbar.add(sample);
            Dimension buttonPref = sample.getPreferredSize();
            Dimension minDim = new Dimension(buttonPref.width + ins.left + ins.right, buttonPref.height + ins.top + ins.bottom);
            toolbar.setMinimumSize(minDim);
            toolbar.setPreferredSize(minDim);
            toolbar.remove(sample);
            setLayout(new BorderLayout());
            add(toolbar, BorderLayout.WEST);
            toolbar.setBorder(new VariableRightBorder(pane));
            toolbar.setBorderPainted(true);

            popupMenu = new JPopupMenu();
            popupMenu.add(new Close());
            popupMenu.add(new CloseAll());
            popupMenu.add(new CloseOthers());
            pane.addMouseListener(new MouseUtils.PopupMouseAdapter() {

                @Override
                protected void showPopup(MouseEvent evt) {
                    popupMenu.show(IOWindowImpl.this, evt.getX(), evt.getY());
                }
            });
            pane.addMouseListener(new MouseAdapter() { // #221375
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        requestActive();
                    }
                }
            });

            String name = NbBundle.getMessage(IOWindow.class, "LBL_IO_WINDOW");
            setDisplayName(name); //NOI18N
            setToolTipText(name);
            // setting name to satisfy the accesible name requirement for window.
            setName(name); //NOI18N

            setIcon(ImageUtilities.loadImage(ICON_RESOURCE)); // NOI18N
            // special title for sliding mode
            // XXX - please rewrite to regular API when available - see issue #55955
            putClientProperty("SlidingName", getDisplayName()); //NOI18N
            if (AQUA) {
                setBackground(UIManager.getColor("NbExplorerView.background"));
                setOpaque(true);
                toolbar.setBackground(UIManager.getColor("NbExplorerView.background"));
                pane.setBackground(UIManager.getColor("NbExplorerView.background"));
                pane.setOpaque(true);
                setMinimumSize(new Dimension()); // #254566
            }
        }

        @Override
        public String getShortName() {
            return NbBundle.getMessage(IOWindow.class, "LBL_IO_WINDOW");
        }

        @Override
        public void open() {
            if (!isOpened())
		super.open();
        }

        @Override
        public void requestActive() {
            super.requestActive();
            JComponent tab = getSelectedTab();
            if (tab != null) {
                tab.requestFocus();
            }
        }

        @Override
        public void requestVisible() {
            if (!isShowing()) {
		super.requestVisible();
	    }
        }

        boolean activated;
        public boolean isActivated() {
            return activated;
        }


        public void addTab(JComponent comp, CallBacks cb) {
            if (cb != null) {
                tabToCb.put(comp, cb);
            }
            if (singleTab != null) {
                // only single tab, remove it from TopComp. and add it to tabbed pane
                assert pane.getParent() == null;
                assert pane.getTabCount() == 0;
                remove(singleTab);
                pane.add(singleTab);
                pane.setIconAt(0, (Icon) singleTab.getClientProperty(ICON_PROP));
                pane.setToolTipTextAt(0, singleTab.getToolTipText());
                singleTab = null;
                pane.add(comp);
                add(pane);
                updateWindowName(null);
            } else if (pane.getTabCount() > 0) {
                // already several tabs
                assert pane.getParent() != null;
                assert singleTab == null;
                pane.add(comp);
            } else {
                // nothing yet
                assert pane.getParent() == null;
                assert singleTab == null;
                setFocusable(false);
                singleTab = comp;
                add(comp);
                updateWindowName(singleTab.getName());
                checkTabSelChange();
            }
            revalidate();
        }

        public void removeTab(JComponent comp) {
            if (singleTab != null) {
                assert singleTab == comp;
                remove(singleTab);
                singleTab = null;
                updateWindowName(null);
                checkTabSelChange();
                setFocusable(true);
                revalidate();
                repaint();
            } else if (pane.getParent() == this) {
                assert pane.getTabCount() > 1;
                pane.remove(comp);
                if (pane.getTabCount() == 1) {
                    singleTab = (JComponent) pane.getComponentAt(0);
                    pane.remove(singleTab);
                    remove(pane);
                    add(singleTab);
                    updateWindowName(singleTab.getName());
                }
                revalidate();
            }
            CallBacks cb = tabToCb.remove(comp);
            if (cb != null) {
                cb.closed();
            }
        }

        public void selectTab(JComponent comp) {
//	    Calls to open/requestVisible() lifted into Controller, case CMD_SELECT.
//	    Tests pushed into this.open() and this.requestVisible().
//
//            if (!isOpened()) {
//                open();
//            }
//            if (!isShowing()) {
//                requestVisible();
//            }
            if (singleTab == null) {
                pane.setSelectedComponent(comp);
            }
            checkTabSelChange();
        }

        public JComponent getSelectedTab() {
            return singleTab != null ? singleTab : (JComponent) pane.getSelectedComponent();
        }

        public void setTitle(JComponent comp, String name) {
            comp.setName(name);
            if (singleTab != null) {
                assert singleTab == comp;
                updateWindowName(name);
            } else {
                assert pane.getParent() == this;
                int idx = pane.indexOfComponent(comp);
                assert idx >= 0;
                pane.setTitleAt(idx, name);
            }
        }

        public void setToolTipText(JComponent comp, String text) {
            comp.setToolTipText(text);
            if (singleTab != null) {
                assert singleTab == comp;
            } else {
                assert pane.getParent() == this;
                int idx = pane.indexOfComponent(comp);
                assert idx >= 0;
                pane.setToolTipTextAt(idx, text);
            }
        }

        public void setIcon(JComponent comp, Icon icon) {
            if (comp == singleTab) {
                comp.putClientProperty(ICON_PROP, icon);
                return;
            }
            int idx = pane.indexOfComponent(comp);
            if (idx < 0) {
                return;
            }
            comp.putClientProperty(ICON_PROP, icon);
            pane.setIconAt(idx, icon);
            pane.setDisabledIconAt(idx, icon);
        }

        void setToolbarActions(JComponent comp, Action[] toolbarActions) {
            if (toolbarActions != null && toolbarActions.length > 0) {
                comp.putClientProperty(TOOLBAR_ACTIONS_PROP, toolbarActions);
            }
            if (getSelectedTab() == comp) {
                updateToolbar(comp);
            }
        }

        @Override
        public int getPersistenceType() {
            return PERSISTENCE_ALWAYS;
        }

        @Override
        public String preferredID() {
            return "output"; //NOI18N
        }

        @Override
        public void processFocusEvent(FocusEvent fe) {
            super.processFocusEvent(fe);
            if (Boolean.TRUE.equals(getClientProperty("isSliding"))) { //NOI18N
                repaint(200);
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            if (AQUA) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            super.paintComponent(g);
            if (hasFocus()) {
                Insets ins = getInsets();
                Color col = UIManager.getColor("controlShadow"); //NOI18N
                //Draw *some* focus indication
                if (col == null) {
                    col = java.awt.Color.GRAY;
                }
                g.setColor(col);
                g.drawRect(
                        ins.left + 2,
                        ins.top + 2,
                        getWidth() - (ins.left + ins.right + 4),
                        getHeight() - (ins.top + ins.bottom + 4));
            }
        }

        void updateWindowName(String name) {
            String winName = NbBundle.getMessage(IOWindowImpl.class, "LBL_IO_WINDOW"); //NOI18N
            if (name != null) {
                String newName = NbBundle.getMessage(IOWindowImpl.class, "FMT_IO_WINDOW", new Object[]{winName, name}); //NOI18N
                if (newName.indexOf("<html>") != -1) {
                    newName = "<html>" + newName.replace("<html>", ""); //NOI18N
                    setHtmlDisplayName(newName); //NOI18N
                    setToolTipText(newName);
                } else {
                    setDisplayName(newName);
                    setHtmlDisplayName(null);
                    setToolTipText(newName);
                }
            } else {
                setDisplayName(winName);
                setToolTipText(winName);
                setHtmlDisplayName(null);
            }

        }

        private void updateToolbar(JComponent comp) {
            toolbar.removeAll();
            if (comp != null) {
                JButton[] buttons = getTabButtons(comp);
                for (int i = 0; i < buttons.length; i++) {
                    toolbar.add(buttons[i]);
                }
            }
            toolbar.validate();
            toolbar.repaint();
        }

        JButton[] getTabButtons(JComponent comp) {
            JButton[] buttons = (JButton[]) comp.getClientProperty(TOOLBAR_BUTTONS_PROP);
            if (buttons != null) {
                return buttons;
            }
            Action[] actions = (Action[]) comp.getClientProperty(TOOLBAR_ACTIONS_PROP);
            if (actions == null) {
                return new JButton[0];
            }

            buttons = new JButton[actions.length];
            for (int i=0; i < buttons.length; i++) {
                buttons[i] = new JButton(actions[i]);
                buttons[i].setBorderPainted(false);
                buttons[i].setOpaque(false);
                buttons[i].setText(null);
                buttons[i].putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
                Object icon = actions[i].getValue(Action.SMALL_ICON);
                if (!(icon instanceof Icon)) {
                    throw new IllegalStateException ("No icon provided for " + actions[i]); //NOI18N
                }
                buttons[i].setDisabledIcon(ImageUtilities.createDisabledIcon((Icon) icon));
                String name = (String) actions[i].getValue(Action.NAME);
                String shortDescription = (String) actions[i].getValue(Action.SHORT_DESCRIPTION);
                String longDescription = (String) actions[i].getValue(Action.LONG_DESCRIPTION);
                if (name == null) name = shortDescription;
                if (longDescription == null) longDescription = shortDescription;
                buttons[i].getAccessibleContext().setAccessibleName(name);
                buttons[i].getAccessibleContext().setAccessibleDescription(longDescription);
            }
            return buttons;
        }

        @Override
        protected void componentActivated() {
            super.componentActivated();
            activated = true;
            JComponent comp = getSelectedTab();
            CallBacks cb = tabToCb.get(comp);
            if (cb != null) {
                cb.activated();
            }
        }

        @Override
        protected void componentDeactivated() {
            super.componentDeactivated();
            activated = false;
            JComponent comp = getSelectedTab();
            CallBacks cb = tabToCb.get(comp);
            if (cb != null) {
                cb.deactivated();
            }
        }

        public void stateChanged(ChangeEvent e) {
            checkTabSelChange();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                JComponent comp = (JComponent) evt.getNewValue();
                removeTab(comp);
            }
        }

        JComponent lastSelTab;
        void checkTabSelChange() {
            JComponent sel = getSelectedTab();
            if (sel != lastSelTab) {
                lastSelTab = sel;
                updateToolbar(sel);
                getActionMap().setParent(sel != null ? sel.getActionMap() : null);
            }
        }

        private JComponent[] getTabs() {
            if (singleTab != null) {
                return new JComponent[] {singleTab};
            }

            JComponent[] tabs = new JComponent[pane.getTabCount()];
            for (int i = 0; i < pane.getTabCount(); i++) {
                tabs[i] = (JComponent) pane.getComponentAt(i);
            }
            return tabs;
        }

        @Override
        public SubComponent[] getSubComponents() {
            if( singleTab != null )
                return new SubComponent[0];
            JComponent[] tabs = getTabs();
            SubComponent[] res = new SubComponent[tabs.length];
            for( int i=0; i<res.length; i++ ) {
                final JComponent theTab = tabs[i];
                String title = pane.getTitleAt( i );
                res[i] = new SubComponent( title, new ActionListener() {

                    @Override
                    public void actionPerformed( ActionEvent e ) {
                        if( singleTab != null || pane.indexOfComponent( theTab ) < 0 )
                            return; //the tab is gone already
                        selectTab( theTab );
                    }
                }, theTab == getSelectedTab() );
            }
            return res;
        }


        private void closeOtherTabs() {
            assert pane.getParent() == this;
            JComponent sel = getSelectedTab();
            for (JComponent tab : getTabs()) {
                if (tab != sel) {
                    removeTab(tab);
                }
            }
        }

        private void closeAllTabs() {
            for (JComponent tab : getTabs()) {
                removeTab(tab);
            }
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("org.netbeans.core.io.ui.IOWindow");
        }

        private class Close extends AbstractAction {

            public Close() {
                super(NbBundle.getMessage(IOWindowImpl.class, "LBL_Close"));
            }

            public void actionPerformed(ActionEvent e) {
                removeTab(getSelectedTab());
            }
        }

        private class CloseAll extends AbstractAction {

            public CloseAll() {
                super(NbBundle.getMessage(IOWindowImpl.class, "LBL_CloseAll"));
            }

            public void actionPerformed(ActionEvent e) {
                closeAllTabs();
            }
        }

        private class CloseOthers extends AbstractAction {

            public CloseOthers() {
                super(NbBundle.getMessage(IOWindowImpl.class, "LBL_CloseOthers"));
            }

            public void actionPerformed(ActionEvent e) {
                closeOtherTabs();
            }
        }

        private class VariableRightBorder implements Border {

            private JTabbedPane pane;

            public VariableRightBorder(JTabbedPane pane) {
                this.pane = pane;
            }

            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                if (pane.getParent() != IOWindowImpl.this) {
                    Color old = g.getColor();
                    g.setColor(getColor());
                    g.drawLine(x + width - 1, y, x + width - 1, y + height);
                    g.setColor(old);
                }
            }

            public Color getColor() {
                if (Utilities.isMac()) {
                    Color c1 = UIManager.getColor("controlShadow");
                    Color c2 = UIManager.getColor("control");
                    return new Color((c1.getRed() + c2.getRed()) / 2,
                            (c1.getGreen() + c2.getGreen()) / 2,
                            (c1.getBlue() + c2.getBlue()) / 2);
                } else {
                    return UIManager.getColor("controlShadow");
                }
            }

            public Insets getBorderInsets(Component c) {
                if (pane.getParent() == IOWindowImpl.this) {
                    return new Insets(0, 0, 0, 0);
                }
                return new Insets(0, 0, 0, 2);
            }

            public boolean isBorderOpaque() {
                return true;
            }
        }
    }
}
