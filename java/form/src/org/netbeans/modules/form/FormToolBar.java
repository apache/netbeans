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

package org.netbeans.modules.form;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.BeanInfo;
import java.util.*;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.form.palette.*;
import org.netbeans.modules.form.actions.TestAction;
import org.openide.util.ImageUtilities;

/**
 * ToolBar in the FormDesigner - by default it holds buttons for selection and
 * connection mode and for testing the form. May contain other buttons for
 * some form editor actions.
 *
 * @author Tomas Pavek
 */

final class FormToolBar {

    private FormDesigner formDesigner;

    private JToolBar toolbar;
    private JToggleButton selectionButton;
    private JToggleButton connectionButton;
    private JToggleButton paletteButton;
    private JLabel addLabel;

    private PaletteMenuView paletteMenuView;

    private Listener listener;

    public FormToolBar(FormDesigner designer, JToolBar toolbar) {
        this.formDesigner = designer;
        if (toolbar == null) {
            toolbar = new ToolBar();
        } else {
            Object tb = toolbar.getClientProperty(FormToolBar.class);
            if (tb instanceof FormToolBar) { // clean everything added by the previous FormToolBar
                FormToolBar prevFormToolBar = (FormToolBar) tb;
                toolbar.removeMouseListener(prevFormToolBar.listener);
                // remove all relevant components - the first one is a horizontal strut before the selection button
                int i = toolbar.getComponentIndex(prevFormToolBar.selectionButton) - 1;
                if (i >= 0) {
                    while (i < toolbar.getComponentCount()) {
                        toolbar.remove(i);
                    }
                }
            }
        }
        this.toolbar = toolbar;
        toolbar.putClientProperty(FormToolBar.class, this);
        toolbar.putClientProperty("isPrimary", Boolean.TRUE); // for JDev // NOI18N

        listener = new Listener();

        // selection button
        selectionButton = new JToggleButton(ImageUtilities.loadImageIcon("org/netbeans/modules/form/resources/selection_mode.png", true), // NOI18N
                                            false);
        selectionButton.addActionListener(listener);
        selectionButton.addMouseListener(listener);
        selectionButton.setToolTipText(
            FormUtils.getBundleString("CTL_SelectionButtonHint")); // NOI18N
        HelpCtx.setHelpIDString(selectionButton, "gui.about"); // NOI18N
        selectionButton.setSelected(true);
        initButton(selectionButton);

        // connection button
        connectionButton = new JToggleButton(ImageUtilities.loadImageIcon("org/netbeans/modules/form/resources/connection_mode.png", true), // NOI18N
                                             false);
        connectionButton.addActionListener(listener);
        connectionButton.addMouseListener(listener);
        connectionButton.setToolTipText(
            FormUtils.getBundleString("CTL_ConnectionButtonHint")); // NOI18N
        HelpCtx.setHelpIDString(connectionButton, "gui.connecting.intro"); // NOI18N
        initButton(connectionButton);

        // palette button
        paletteButton = new JToggleButton(
            new ImageIcon(getClass().getResource(
                          "/org/netbeans/modules/form/resources/beansButton.gif")), // NOI18N
            false);
        paletteButton.addActionListener(listener);
        paletteButton.addMouseListener(listener);
        paletteButton.setToolTipText(
            FormUtils.getBundleString("CTL_BeansButtonHint")); // NOI18N
        HelpCtx.setHelpIDString(paletteButton, "gui.components.adding"); // NOI18N
        initButton(paletteButton);

        // status label
        addLabel = new JLabel();
        addLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));

        // popup menu
        toolbar.addMouseListener(listener);

        // a11y
        connectionButton.getAccessibleContext().setAccessibleName(connectionButton.getToolTipText());
        selectionButton.getAccessibleContext().setAccessibleName(selectionButton.getToolTipText());
        paletteButton.getAccessibleContext().setAccessibleName(paletteButton.getToolTipText());
        connectionButton.getAccessibleContext().setAccessibleDescription(FormUtils.getBundleString("ACSD_ConnectionMode")); // NOI18N
        selectionButton.getAccessibleContext().setAccessibleDescription(FormUtils.getBundleString("ACSD_SelectionMode")); // NOI18N
        paletteButton.getAccessibleContext().setAccessibleDescription(FormUtils.getBundleString("ACSD_AddMode")); // NOI18N

        // adding the components to the toolbar
        JToolBar.Separator separator2 = new JToolBar.Separator();
        separator2.setOrientation(JSeparator.VERTICAL);
        JToolBar.Separator separator3 = new JToolBar.Separator();
        separator3.setOrientation(JSeparator.VERTICAL);

        TestAction testAction = SystemAction.get(TestAction.class);
        JButton testButton = (JButton) testAction.getToolbarPresenter();
        testButton.addMouseListener(listener);
        initButton(testButton);

        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(selectionButton);
        toolbar.add(connectionButton);
        toolbar.add(paletteButton);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(testButton);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(separator2);
        toolbar.add(Box.createHorizontalStrut(4));
        installDesignerActions();
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(separator3);
        toolbar.add(Box.createHorizontalStrut(4));
        installResizabilityActions();

        // Add "addLabel" at the end of the toolbar
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(addLabel);

        if (!FormLoaderSettings.getInstance().isPaletteInToolBar()) {
            showPaletteButton(false);
        }
    }

    JToolBar getToolBar() {
        return toolbar;
    }

    void installDesignerActions() {
        Collection actions = formDesigner.getDesignerActions(true);
        Iterator iter = actions.iterator();
        while (iter.hasNext()) {
            Action action = (Action)iter.next();
            JButton button = toolbar.add(action);
            initButton(button);
        }
    }

    void installResizabilityActions() {
        Action[] actions = formDesigner.getResizabilityActions();
        JToggleButton[] resButtons = new JToggleButton[2];
        for (int i=0; i < actions.length; i++) {
            Action action = actions[i];
            JToggleButton button = new JToggleButton();
            button.setAction(action);
            initButton(button);
            resButtons[i] = button;
            toolbar.add(button);
            toolbar.add(Box.createHorizontalStrut(2));        
        }
    }
    
    // --------
    
    private void initButton(AbstractButton button) {
        if (!("Windows".equals(UIManager.getLookAndFeel().getID()) // NOI18N
            && (button instanceof JToggleButton))) {
            button.setBorderPainted(false);
        }
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
    }
    
    void updateDesignerMode(int mode) {
        selectionButton.setSelected(mode == FormDesigner.MODE_SELECT);
        connectionButton.setSelected(mode == FormDesigner.MODE_CONNECT);
        paletteButton.setSelected(mode == FormDesigner.MODE_ADD);

        if (addLabel.isVisible()) {
            PaletteItem item = PaletteUtils.getSelectedItem();
            if (item != null && mode == FormDesigner.MODE_ADD) {
                addLabel.setIcon(
                    ImageUtilities.image2Icon(item.getNode().getIcon(BeanInfo.ICON_COLOR_16x16)));
                addLabel.setText(item.getNode().getDisplayName());
            }
            else {
                addLabel.setText(""); // NOI18N
                addLabel.setIcon(null);
            }
        }
    }

    void showPaletteButton(boolean visible) {
        addLabel.setVisible(visible);
        paletteButton.setVisible(visible);
        // Hack that solves issue 147578
        if ("Nimbus".equals(UIManager.getLookAndFeel().getID())) { // NOI18N
            if (visible) {
                addLabel.setPreferredSize(null);
                paletteButton.setPreferredSize(null);
            } else {
                addLabel.setPreferredSize(new Dimension());
                paletteButton.setPreferredSize(new Dimension());
            }
        }
    }

    private void showPaletteViewMenu() {
        if (paletteMenuView == null) {
            paletteMenuView = new PaletteMenuView(listener);
            paletteMenuView.getPopupMenu().addPopupMenuListener(listener);
        }

        Point p = paletteButton.getLocation();
        p.y += paletteButton.getHeight() + 2;

        paletteMenuView.getPopupMenu().show(toolbar, p.x, p.y);
    }

    private void showVisibilityPopupMenu(Point p) {
        JPopupMenu menu = new JPopupMenu();
        final JMenuItem item = new JCheckBoxMenuItem(
                FormUtils.getBundleString("CTL_PaletteButton_MenuItem")); // NOI18N
        item.setSelected(FormLoaderSettings.getInstance().isPaletteInToolBar());
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FormLoaderSettings.getInstance().setPaletteInToolBar(
                                                         item.isSelected());
            }
        });
        menu.add(item);
        menu.show(toolbar, p.x, p.y);
    }

    // -------

    private class Listener extends MouseAdapter
                           implements ActionListener, NodeAcceptor,
                                      PopupMenuListener
    {
        // Determines whether palette popup menu should be shown (see issue 46673)
        private boolean showMenu;
        
        /** Action to switch to selection, connection or add mode. */
        @Override
        public void actionPerformed(ActionEvent ev) {
            if (ev.getSource() == selectionButton)
                formDesigner.toggleSelectionMode();
            else if (ev.getSource() == connectionButton)
                formDesigner.toggleConnectionMode();
            else if (ev.getSource() == paletteButton) {
                if (showMenu) {
                    formDesigner.toggleAddMode();
                    showPaletteViewMenu();
                } else {
                    MenuSelectionManager.defaultManager().clearSelectedPath();
                    formDesigner.toggleSelectionMode();
                }
            }
        }

        /** Acceptor for nodes in PaletteMenuView */
        @Override
        public boolean acceptNodes(Node[] nodes) {
            if (nodes.length == 0)
                return false;

            PaletteItem item = nodes[0].getCookie(PaletteItem.class);
            PaletteUtils.selectItem( item );
            return true;
        }

        /** Handles closing of PaletteMenuView popup */
        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            if( PaletteUtils.getSelectedItem() == null )
                formDesigner.toggleSelectionMode();
        }
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }
        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getSource() == paletteButton) {
                showMenu = !paletteButton.isSelected();
            }
        }

        /** Reacts on right mouse button up - showing toolbar's popup menu. */
        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)
                  && formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT)
                showVisibilityPopupMenu(e.getPoint());
        }
    }

    private static class ToolBar extends JToolBar {
        ToolBar() {
            // Proper initialization of aqua toolbar ui, see commit dbd66075827a
            super("editorToolbar"); // NOI18N
            // the toolbar should have roll-over buttons and no handle for dragging
            setFloatable(false);
            setRollover(true);
            setBorder(new EmptyBorder(0, 0, 0, 0));

            add(Box.createHorizontalStrut(4));
            addSeparator();
        }

        @Override
        public String getUIClassID() {
            // For GTK and Aqua look and feels, we provide a custom toolbar UI
            if (UIManager.get("Nb.Toolbar.ui") != null) { // NOI18N
                return "Nb.Toolbar.ui"; // NOI18N
            } else {
                return super.getUIClassID();
            }
        }
    }
}
