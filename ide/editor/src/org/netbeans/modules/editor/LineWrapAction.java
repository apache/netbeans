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
package org.netbeans.modules.editor;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.lib2.view.DocumentViewOp;
import org.netbeans.modules.editor.lib2.view.LineWrapType;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Registers a {@code Line Wrap} submenu under the {@code View} menu to control
 * editor line wrapping.
 *
 * <p>
 * The {@code Line Wrap} submenu contains four actions: the first three activate
 * each of the three wrapping modes individually, and the fourth cycles through
 * the modes sequentially.
 *
 * <p>
 * Each of these four actions supports customizable keyboard shortcuts
 * configured under {@code Tools > Options > Keymap}. By default, only the
 * {@code Toggle Line Wrap} action is bound to {@code ALT-Z}.</p>
 *
 * @author Alejandro L. Belhafi
 * @author Matthias Bläsing
 * @see LineWrapType
 */
@Messages({
    "lineWrapAction.displayName=Line Wrap",
    "lineWrapAction.menu=Line &Wrap",
    "lineWrapAction.menuItem.off=&Off",
    "lineWrapAction.menuItem.anywhere=&Anywhere",
    "lineWrapAction.menuItem.afterWords=After &words",
    "lineWrapAction.menuItem.toggleLineWrap=&Toggle Line Wrap",
    "lineWrapAction.wrapOffAction.displayName=Wrap Off",
    "lineWrapAction.wrapAfterWordsAction.displayName=Wrap After Words",
    "lineWrapAction.wrapAnywhereAction.displayName=Wrap Anywhere",
    "lineWrapAction.toggleLineWrapAction.displayName=Toggle Line Wrap"
})
@ActionID(
        category = LineWrapAction.CATEGORY,
        id = LineWrapAction.ID)
@ActionReferences({
    @ActionReference(
            path = "Menu/View",
            position = 215)})
@ActionRegistration(
        lazy = false,
        iconInMenu = false,
        displayName = "#lineWrapAction.displayName")
public final class LineWrapAction extends AbstractAction
        implements PropertyChangeListener, Runnable, DynamicMenuContent {

    public static final String ID = "org.netbeans.modules.editor.LineWrapAction";
    public static final String CATEGORY = "View";
    public static final String SUBCATEGORY = CATEGORY + "/Line Wrap";

    private static final long serialVersionUID = 1L;

    private JMenu menu;
    private JRadioButtonMenuItem wrapOffMenuItem;
    private JRadioButtonMenuItem wrapAnywhereMenuItem;
    private JRadioButtonMenuItem wrapAfterWordsMenuItem;
    private JMenuItem toggleLinewrapMenuItem;

    public LineWrapAction() {
        super(Bundle.lineWrapAction_displayName());

        TopComponent.getRegistry().addPropertyChangeListener(
                WeakListeners.propertyChange(this, TopComponent.getRegistry()));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        // Do nothing
    }

    /**
     * Gets the line wrap type currently configured for the active editor.
     *
     * @return the current line wrap type of the active editor, or an empty
     * string if there is no active editor.
     */
    private static String getLineWrapTypeForActiveEditor() {
        final JTextComponent editor = Utilities.getLastActiveComponent();

        if (editor == null) {
            return "";
        }

        String lineWrapType = (String) editor.getClientProperty(SimpleValueNames.TEXT_LINE_WRAP);

        if (lineWrapType == null) {
            lineWrapType = LineWrapType.NONE.getSettingValue();
        }

        return lineWrapType;

    }

    /**
     * Sets the line wrap type for the active editor.
     *
     * @param lineWrapType the line wrap type to apply to the editor.
     * @see DocumentViewOp#updateLineWrapType()
     */
    private static void setLineWrapTypeForActiveEditor(LineWrapType lineWrapType) {
        final JTextComponent editor = Utilities.getLastActiveComponent();

        if (editor == null) {
            return;
        }

        editor.putClientProperty(SimpleValueNames.TEXT_LINE_WRAP, lineWrapType.getSettingValue());
    }

    @Override
    public JComponent[] getMenuPresenters() {
        createMenu();
        updateState();
        return new JComponent[]{menu};
    }

    private void createMenu() {

        Action action = Actions.forID(LineWrapAction.SUBCATEGORY, WrapOffAction.ID);
        wrapOffMenuItem = new JRadioButtonMenuItem(action);
        Mnemonics.setLocalizedText(wrapOffMenuItem, Bundle.lineWrapAction_menuItem_off());

        action = Actions.forID(LineWrapAction.SUBCATEGORY, WrapAnywhereAction.ID);
        wrapAnywhereMenuItem = new JRadioButtonMenuItem(action);
        Mnemonics.setLocalizedText(wrapAnywhereMenuItem, Bundle.lineWrapAction_menuItem_anywhere());

        action = Actions.forID(LineWrapAction.SUBCATEGORY, WrapAfterWordsAction.ID);
        wrapAfterWordsMenuItem = new JRadioButtonMenuItem(action);
        Mnemonics.setLocalizedText(wrapAfterWordsMenuItem, Bundle.lineWrapAction_menuItem_afterWords());

        action = Actions.forID(LineWrapAction.SUBCATEGORY, ToggleLineWrapAction.ID);
        toggleLinewrapMenuItem = new JMenuItem(action);
        Mnemonics.setLocalizedText(toggleLinewrapMenuItem, Bundle.lineWrapAction_menuItem_toggleLineWrap());

        ButtonGroup group = new ButtonGroup();
        group.add(wrapOffMenuItem);
        group.add(wrapAnywhereMenuItem);
        group.add(wrapAfterWordsMenuItem);

        menu = new JMenu(this);
        Mnemonics.setLocalizedText(menu, Bundle.lineWrapAction_menu());
        menu.add(wrapOffMenuItem);
        menu.add(wrapAnywhereMenuItem);
        menu.add(wrapAfterWordsMenuItem);
        menu.add(new JSeparator());
        menu.add(toggleLinewrapMenuItem);
    }

    private void updateState() {
        if (EventQueue.isDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] items) {
        updateState();
        return items;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            updateState();
        }
    }

    @Override
    public void run() {
        boolean isDocumentActive = false;
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if (tc != null) {
            isDocumentActive = WindowManager.getDefault().isEditorTopComponent(tc);
        }
        synchronized (this) {
            setEnabled(isDocumentActive);

            if (isDocumentActive) {
                updateSelectedMenu();
            }
        }
    }

    private void updateSelectedMenu() {
        String lineWrapType = getLineWrapTypeForActiveEditor();

        if (LineWrapType.NONE.getSettingValue().equals(lineWrapType)) {
            wrapOffMenuItem.setSelected(true);
        } else if (LineWrapType.CHARACTER_BOUND.getSettingValue().equals(lineWrapType)) {
            wrapAnywhereMenuItem.setSelected(true);
        } else if (LineWrapType.WORD_BOUND.getSettingValue().equals(lineWrapType)) {
            wrapAfterWordsMenuItem.setSelected(true);
        }
    }

    @ActionID(
            category = LineWrapAction.SUBCATEGORY,
            id = WrapOffAction.ID)
    @ActionRegistration(
            lazy = false,
            iconInMenu = false,
            displayName = "#lineWrapAction.wrapOffAction.displayName")
    public static final class WrapOffAction extends AbstractAction {

        public static final String ID = "org.netbeans.modules.editor.LineWrapAction.WrapOffAction";

        private static final long serialVersionUID = 1L;

        public WrapOffAction() {
            super(Bundle.lineWrapAction_wrapOffAction_displayName());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setLineWrapTypeForActiveEditor(LineWrapType.NONE);
        }

    }

    @ActionID(
            category = LineWrapAction.SUBCATEGORY,
            id = WrapAnywhereAction.ID)
    @ActionRegistration(
            lazy = false,
            iconInMenu = false,
            displayName = "#lineWrapAction.wrapAnywhereAction.displayName")
    public static final class WrapAnywhereAction extends AbstractAction {

        public static final String ID = "org.netbeans.modules.editor.LineWrapAction.WrapAnywhereAction";

        private static final long serialVersionUID = 1L;

        public WrapAnywhereAction() {
            super(Bundle.lineWrapAction_wrapAnywhereAction_displayName());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setLineWrapTypeForActiveEditor(LineWrapType.CHARACTER_BOUND);
        }

    }

    @ActionID(
            category = LineWrapAction.SUBCATEGORY,
            id = WrapAfterWordsAction.ID)
    @ActionRegistration(
            lazy = false,
            iconInMenu = false,
            displayName = "#lineWrapAction.wrapAfterWordsAction.displayName")
    public static final class WrapAfterWordsAction extends AbstractAction {

        public static final String ID = "org.netbeans.modules.editor.LineWrapAction.WrapAfterWordsAction";

        private static final long serialVersionUID = 1L;

        public WrapAfterWordsAction() {
            super(Bundle.lineWrapAction_wrapAfterWordsAction_displayName());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setLineWrapTypeForActiveEditor(LineWrapType.WORD_BOUND);
        }

    }

    @ActionID(
            category = LineWrapAction.SUBCATEGORY,
            id = ToggleLineWrapAction.ID)
    @ActionReferences({
        @ActionReference(
                path = "Shortcuts",
                name = "O-Z")})
    @ActionRegistration(
            lazy = false,
            iconInMenu = false,
            displayName = "#lineWrapAction.toggleLineWrapAction.displayName")
    public static final class ToggleLineWrapAction extends AbstractAction {

        public static final String ID = "org.netbeans.modules.editor.LineWrapAction.ToggleLineWrapAction";

        private static final long serialVersionUID = 1L;

        public ToggleLineWrapAction() {
            super(Bundle.lineWrapAction_toggleLineWrapAction_displayName());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String currentLineWrap = getLineWrapTypeForActiveEditor();

            if (LineWrapType.WORD_BOUND.getSettingValue().equals(currentLineWrap)) {
                setLineWrapTypeForActiveEditor(LineWrapType.NONE);
            } else if (LineWrapType.NONE.getSettingValue().equals(currentLineWrap)) {
                setLineWrapTypeForActiveEditor(LineWrapType.CHARACTER_BOUND);
            } else if (LineWrapType.CHARACTER_BOUND.getSettingValue().equals(currentLineWrap)) {
                setLineWrapTypeForActiveEditor(LineWrapType.WORD_BOUND);
            }
        }

    }
}
