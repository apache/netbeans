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

package org.netbeans.modules.editor.bookmarks.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.ref.WeakReference;
import java.util.ResourceBundle;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.KeyEventBlocker;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.modules.editor.bookmarks.BookmarkInfo;
import org.netbeans.modules.editor.bookmarks.BookmarkManager;
import org.netbeans.modules.editor.bookmarks.BookmarkUtils;
import org.openide.actions.GotoAction;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Goto line or bookmark (replacement of GotoDialogPanel).
 *
 * @author Miloslav Metelka
 */
public class GotoLineOrBookmarkPanel extends JPanel implements ActionListener, FocusListener, WindowListener {

    static final long serialVersionUID = 1L;
    
    private static final List<String> history = new GapList<String>();
    
    private static Rectangle lastBounds;
    
    private static final ResourceBundle bundle = NbBundle.getBundle(GotoLineOrBookmarkPanel.class);

    private static final int MAX_HISTORY_ITEMS = 20;

    private JButton[] buttons;

    private static Dialog dialog;
    
    private WeakReference<JTextComponent> targetComponent;

    /** The variable used during updating combo to prevent firing */
    private KeyEventBlocker blocker;
    
    /** Initializes the UI and fetches the history */
    public GotoLineOrBookmarkPanel() {
        initComponents ();
        getAccessibleContext().setAccessibleName(bundle.getString("ACSD_gotoDialogTitle")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_gotoDialogDescription")); // NOI18N
        gotoCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_gotoDialogGotoCombo")); // NOI18N
        updateCombo();
        JButton gotoButton = new JButton(bundle.getString("CTL_gotoDialogGotoButton")); // NOI18N
        JButton closeButton = new JButton(bundle.getString("CTL_gotoDialogCloseButton")); // NOI18N
        gotoButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_gotoDialogGotoButton")); // NOI18N
        closeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_gotoDialogCloseButton")); // NOI18N
        buttons = new JButton[] { gotoButton, closeButton };
    }
    
    private JTextComponent getTargetComponent() {
        return targetComponent != null ? targetComponent.get() :  EditorRegistry.lastFocusedComponent();
    }

    public void setTargetComponent(JTextComponent targetComponent) {
        this.targetComponent = new WeakReference<JTextComponent>(targetComponent);
    }
    
    

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        gotoLabel = new javax.swing.JLabel();
        gotoCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        gotoLabel.setLabelFor(gotoCombo);
        org.openide.awt.Mnemonics.setLocalizedText(gotoLabel, bundle.getString("CTL_gotoDialogGotoLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(gotoLabel, gridBagConstraints);

        gotoCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 6);
        add(gotoCombo, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JComboBox gotoCombo;
    protected javax.swing.JLabel gotoLabel;
    // End of variables declaration//GEN-END:variables

    public void showDialog(final JTextComponent target, final KeyEventBlocker blocker) {
        setTargetComponent(target);
        this.blocker = blocker;
        if (dialog == null) {
            initDialog();
        }
        dialog.setVisible(true);
        gotoCombo.getEditor().getEditorComponent().addFocusListener(this);
        gotoCombo.getEditor().selectAll();
        gotoCombo.getEditor().getEditorComponent().requestFocus();
        gotoCombo.getEditor().addActionListener(this);
        dialog.addWindowListener(this);
    }

    private Dialog initDialog() {
        dialog = org.netbeans.editor.DialogSupport.createDialog(
                bundle.getString( "CTL_gotoDialogTitle" ), // NOI18N
                this, false, // non-modal
                buttons, false, // sidebuttons,
                0, // defaultIndex = 0 => gotoButton
                1, // cancelIndex = 1 => cancelButton
                this // ActionListener
                );
        if (dialog instanceof JDialog) { // Add press-aga
            JRootPane rootPane = ((JDialog)dialog).getRootPane();
            Container contentPane = rootPane.getContentPane();
            KeyBindingSettings kbs = MimeLookup.getLookup("").lookup(KeyBindingSettings.class);
            KeyStroke ks;
            GotoAction gotoAction = SystemAction.get(GotoAction.class);
            if ((contentPane.getLayout() instanceof BorderLayout) && gotoAction != null &&
                    (ks = (KeyStroke) gotoAction.getValue(Action.ACCELERATOR_KEY)) != null)
            {
                BorderLayout layout = (BorderLayout) contentPane.getLayout();
                Component buttonBar = layout.getLayoutComponent(BorderLayout.SOUTH);
                contentPane.remove(buttonBar);

                JPanel southPanel = new JPanel();
                southPanel.setLayout(new GridBagLayout());
                JLabel keyChooserLabel = new JLabel();
                keyChooserLabel.setText(NbBundle.getMessage(GotoLineOrBookmarkPanel.class,
                        "CTL_gotoDialogBookmarkKeyChooserLabel", Actions.keyStrokeToString(ks)));
                InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                inputMap.put(ks, "openKeyChooser");
                rootPane.getActionMap().put("openKeyChooser", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        BookmarkKeyChooser.get().show(dialog, new Runnable() {
                            @Override
                            public void run() {
                                BookmarkInfo bookmark = BookmarkKeyChooser.get().getandClearResult();
                                if (bookmark != null) {
                                    disposeDialog();
                                    BookmarkUtils.postOpenEditor(bookmark);
                                }
                            }
                        });
                    }
                });

                GridBagConstraints constraints;
                constraints = new GridBagConstraints();
                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 1.0;
                constraints.gridx = 0;
                constraints.gridy = 0;
                constraints.anchor = GridBagConstraints.SOUTHWEST;
                constraints.insets = new Insets(0, 5, 0, 0);
                southPanel.add(keyChooserLabel, constraints);

                constraints = new GridBagConstraints();
                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 1.0;
                constraints.gridx = GridBagConstraints.RELATIVE;
                constraints.gridy = 0;
                constraints.anchor = GridBagConstraints.EAST;
                southPanel.add(buttonBar, constraints);

                contentPane.add(southPanel, BorderLayout.SOUTH);
                Font font = keyChooserLabel.getFont().deriveFont(Math.max(1,Math.round(0.8f *keyChooserLabel.getFont().getSize()))); 
                keyChooserLabel.setFont(font);
            }
        }

        dialog.pack();

        // Position the dialog according to the history
        if (lastBounds != null) {
            dialog.setBounds(lastBounds);
        } else {  // no history, center it on the screen
            Dimension dim = dialog.getPreferredSize();
            int x;
            int y;
            JTextComponent c = getTargetComponent();
            Window w = c != null ? SwingUtilities.getWindowAncestor(c) : null;
            if (w != null) {
                x = Math.max(0, w.getX() + (w.getWidth() - dim.width) / 2);
                y = Math.max(0, w.getY() + (w.getHeight() - dim.height) / 2);
            } else {
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                x = Math.max(0, (screen.width - dim.width) / 2);
                y = Math.max(0, (screen.height - dim.height) / 2);
            }
            dialog.setLocation(x, y);
        }

        return dialog;
    }

    public void disposeDialog() {
        if (dialog != null) {
            dialog.removeWindowListener(this);
            lastBounds = dialog.getBounds();
            dialog.dispose();
            dialog = null;
            buttons = null;
            blocker = null;
            Utilities.returnFocus();
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();
        if (src == gotoCombo.getEditor().getEditorComponent() || (buttons != null && src == buttons[0])) {
            if (performGoto()) {
                updateHistory();
                disposeDialog();
            }
        } else {
            disposeDialog();
        }
    }

    /**
     * Perform the goto operation.
     *
     * @return whether the dialog should be made invisible or not
     */
    private boolean performGoto() {
        JTextComponent c = getTargetComponent();
        String text = (String) gotoCombo.getEditor().getItem();
        if (c != null) {
            try {
                int lineNumber = Integer.parseInt(text);
                if (lineNumber == 0) { // Works in vim to jump to begining
                    lineNumber = 1;
                }
                int lineIndex = lineNumber - 1;
                Document doc = c.getDocument();
                Element rootElem = doc.getDefaultRootElement();
                int lineCount = rootElem.getElementCount();
                if (lineIndex >= 0) {
                    if (lineIndex >= lineCount) {
                        lineIndex = lineCount - 1;
                    }
                    int offset = rootElem.getElement(lineIndex).getStartOffset();
                    c.setCaretPosition(offset);
                    return true;
                } // else: lineIndex < 0 => beep and return false
            } catch (NumberFormatException e) {
                // Contains letters or other chars -> attempt bookmarks
                BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
                try {
                    BookmarkInfo bookmark = lockedBookmarkManager.findBookmarkByNameOrKey(text, false);
                    if (bookmark != null) {
                        BookmarkUtils.postOpenEditor(bookmark);
                        return true;
                    } // else: unknown bookmark => beep
                } finally {
                    lockedBookmarkManager.unlock();
                }
            }
        }
        c.getToolkit().beep();
        return false;
    }

    private void updateCombo() {
        gotoCombo.setModel(new DefaultComboBoxModel(history.toArray(new String[0])));
    }

    private String getValue() {
        return (String) gotoCombo.getEditor().getItem();
    }

    private void updateHistory() {
        String value = getValue();
        history.remove(value); // move item to top
        history.add(0, value);
        // assure it won't hold more than MAX_ITEMS
        if (history.size() > MAX_HISTORY_ITEMS) {
            history.remove(MAX_HISTORY_ITEMS);
        }
        updateCombo();
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (blocker != null)
            blocker.stopBlocking();
        ((JComponent)e.getSource()).removeFocusListener(this);
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        disposeDialog();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (blocker != null) {
                    blocker.stopBlocking(false);
                }
            }
        });
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    
    @EditorActionRegistration(
            name = ExtKit.gotoAction,
            weight = 100
    )
    public static final class BookmarksGotoAction extends ExtKit.GotoAction {
        
        public BookmarksGotoAction() {
        }
    
        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                new GotoLineOrBookmarkPanel().showDialog(target, new KeyEventBlocker(target, false));
            }
        }

    }

}
