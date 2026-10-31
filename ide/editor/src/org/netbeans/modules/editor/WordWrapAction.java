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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.editor.lib2.view.DocumentViewOp;
import org.netbeans.modules.editor.lib2.view.LineWrapType;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action that toggles word wrapping for the currently active editor instance.
 * <p>
 * This action appears as a checkbox menu item under the <b>View</b> menu. It
 * allows users to enable or disable line wrapping dynamically for the current
 * file without changing the global or MIME-type settings configured in
 * {@code Tools/Options/Editor/Formatting}.
 * </p>
 * <p>
 * Word wrapping behavior relies on the {@link LineWrapType} enumeration, which
 * defines three values: {@code NONE}, {@code WORD_BOUND}, and
 * {@code CHARACTER_BOUND}. This action specifically toggles between
 * {@code NONE} and {@code WORD_BOUND}. If the editor is currently configured to
 * use {@code CHARACTER_BOUND}, it will be treated as {@code NONE} by this
 * action.
 * </p>
 * <p>
 * The action is automatically disabled when it cannot be applied to the current
 * context. It is bound to the {@code ALT-Z} keyboard shortcut by default, which
 * can be reconfigured via {@code Tools/Options/Keymap}.
 * </p>
 *
 * @author Alejandro L. Belhafi
 */
@Messages({"wordWrapAction.displayName=&Word Wrap"})
@ActionID(category = "View", id = "org.netbeans.modules.editor.WordWrapAction")
@ActionReferences({
    @ActionReference(path = "Menu/View", position = 215),
    @ActionReference(path = "Shortcuts", name = "O-Z")})
@ActionRegistration(lazy = false, iconInMenu = false, displayName = "#wordWrapAction.displayName")
public class WordWrapAction extends AbstractAction
        implements PropertyChangeListener, Runnable, DynamicMenuContent {

    /**
     * Constant representing no line wrapping, mapping to
     * {@link LineWrapType#NONE}. This is also used as the fallback treatment for
     * {@link LineWrapType#CHARACTER_BOUND}.
     */
    private static final String NONE = "none"; //NOI18N

    /**
     * Constant representing standard word wrapping, mapping to
     * {@link LineWrapType#WORD_BOUND}.
     */
    private static final String WORDS = "words"; //NOI18N

    private JCheckBoxMenuItem menu;

    public WordWrapAction() {
        super(Bundle.wordWrapAction_displayName());
        putValue(SELECTED_KEY, false);

        TopComponent.getRegistry().addPropertyChangeListener(
                WeakListeners.propertyChange(this, TopComponent.getRegistry()));
    }

    /**
     * Toggles the word wrap state for the currently active editor and updates
     * the checkbox selection status of the action accordingly.
     *
     * @param event the action event triggered by the user (e.g., clicking the
     *              View menu item or pressing the keyboard shortcut)
     * @see DocumentViewOp#updateLineWrapType()
     */
    @Override
    public void actionPerformed(ActionEvent event) {

        final JTextComponent editor = Utilities.getLastActiveComponent();

        if (editor == null) {
            return;
        }

        boolean newSelection = !(WORDS.equals(getLineWrap(editor)));

        putValue(SELECTED_KEY, newSelection);

        editor.putClientProperty(SimpleValueNames.TEXT_LINE_WRAP, newSelection ? WORDS : NONE);
    }

    private String getLineWrap(JTextComponent editor) {
        String lineWrap = (String) editor.getClientProperty(SimpleValueNames.TEXT_LINE_WRAP);

        if (lineWrap == null) {
            lineWrap = CodeStylePreferences.get(editor.getDocument())
                    .getPreferences()
                    .get(SimpleValueNames.TEXT_LINE_WRAP, NONE);
        }

        return lineWrap;
    }

    @Override
    public JComponent[] getMenuPresenters() {
        createMenu();
        updateState();
        return new JCheckBoxMenuItem[]{menu};
    }

    private void createMenu() {
        menu = new JCheckBoxMenuItem();
        Actions.connect(menu, this, true);
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
            updateSelection();
        }
    }

    private void updateSelection() {
        if (!isEnabled()) {
            putValue(SELECTED_KEY, false);
            return;
        }

        final JTextComponent editor = Utilities.getLastActiveComponent();
        if (editor == null) {
            putValue(SELECTED_KEY, false);
            return;
        }

        String lineWrap = getLineWrap(editor);

        putValue(SELECTED_KEY, WORDS.equals(lineWrap));
    }

}
