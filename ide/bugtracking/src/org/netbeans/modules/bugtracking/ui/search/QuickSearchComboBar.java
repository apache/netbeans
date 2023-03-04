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

package org.netbeans.modules.bugtracking.ui.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.ui.search.PopupItem.IssueItem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Quick search toolbar component
 * @author Jan Becicka
 * @author Tomas Stupka
 */
public class QuickSearchComboBar extends javax.swing.JPanel {

    private QuickSearchPopup displayer;
    private Color origForeground;
    private JComponent caller;    
    private ChangeSupport changeSupport;

    public QuickSearchComboBar(JComponent caller) {
        this.caller = caller;
        initComponents();
        command.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if(value instanceof IssueImpl) {
                    IssueImpl item = (IssueImpl) value;
                    value = IssueItem.getIssueDescription(item);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        ComboEditor comboEditor = new ComboEditor(command.getEditor());
        command.setEditor(comboEditor);
        displayer = new QuickSearchPopup(this);
        
        JTextField editor = comboEditor.editor;
        command.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                displayer.maybeEvaluate(editor.getText());
                command.getUI().setPopupVisible(command, false);
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
            @Override public void popupMenuCanceled(PopupMenuEvent e) { }
        });        
    }

    public static Issue selectIssue(String message, Repository repository, JPanel caller, HelpCtx helpCtx) {
        QuickSearchComboBar bar = new QuickSearchComboBar(caller);
        bar.setRepository(repository);
        bar.setAlignmentX(0f);
        bar.setMaximumSize(new Dimension(Short.MAX_VALUE, bar.getPreferredSize().height));
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
        panel.setLayout(layout);
        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, message);
        panel.add(label);
        label.setLabelFor(bar.getIssueComponent());
        LayoutStyle layoutStyle = LayoutStyle.getInstance();
        int gap = layoutStyle.getPreferredGap(label, bar, LayoutStyle.ComponentPlacement.RELATED, SwingConstants.SOUTH, panel);
        panel.add(Box.createVerticalStrut(gap));
        panel.add(bar);
        panel.add(Box.createVerticalStrut(gap));
        ResourceBundle bundle = NbBundle.getBundle(QuickSearchComboBar.class);
        JLabel hintLabel = new JLabel(bundle.getString("MSG_SelectIssueHint")); // NOI18N
        hintLabel.setEnabled(false);
        panel.add(hintLabel);
        panel.add(Box.createVerticalStrut(80));
        panel.setBorder(BorderFactory.createEmptyBorder(
                layoutStyle.getContainerGap(panel, SwingConstants.NORTH, null),
                layoutStyle.getContainerGap(panel, SwingConstants.WEST, null),
                0,
                layoutStyle.getContainerGap(panel, SwingConstants.EAST, null)));
        panel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_IssueSelector"));
        Issue issue = null;
        JButton ok = new JButton(bundle.getString("LBL_Select")); // NOI18N
        ok.getAccessibleContext().setAccessibleDescription(ok.getText());
        JButton cancel = new JButton(bundle.getString("LBL_Cancel")); // NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(cancel.getText());
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                bundle.getString("LBL_Issues"), // NOI18N
                true,
                NotifyDescriptor.OK_CANCEL_OPTION,
                ok,
                null);
        descriptor.setOptions(new Object [] {ok, cancel});
        descriptor.setHelpCtx(helpCtx);
        DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);
        if (descriptor.getValue() == ok) {
            issue = bar.getIssue();
        }
        return issue;
    }

    public synchronized void removeChangeListener(ChangeListener listener) {
        getChangeSupport().removeChangeListener(listener);
    }

    public synchronized void addChangeListener(ChangeListener listener) {
        getChangeSupport().addChangeListener(listener);
    }

    public Issue getIssue() {
        IssueImpl impl = getIssueImpl();
        return impl != null ? impl.getIssue() : null;
    }
    
    public IssueImpl getIssueImpl() {
        return (IssueImpl) command.getEditor().getItem();
    }

    public void setRepository(Repository repo) {
        setRepository(APIAccessor.IMPL.getImpl(repo));
    }
    
    public void setRepository(RepositoryImpl repositoryImpl) {
        displayer.setRepository(repositoryImpl);
        command.setModel(new DefaultComboBoxModel());
        command.setSelectedItem(null);
    }
    
    public void setIssue(IssueImpl issue) {
        if(issue != null) {
            command.getEditor().setItem(issue);
            displayer.setVisible(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        command = new javax.swing.JComboBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setMaximumSize(new java.awt.Dimension(200, 2147483647));
        setName("Form"); // NOI18N
        setOpaque(false);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFocusLost(evt);
            }
        });

        command.setEditable(true);
        command.setName("command"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(command, 0, 353, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(command, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusLost
        displayer.setVisible(false);
    }//GEN-LAST:event_formFocusLost

    private void returnFocus () {
        displayer.setVisible(false);
        if (caller != null) {
            caller.requestFocus();
        }
    }

    public void enableFields(boolean enable) {
        command.setEnabled(enable);
    }

    /** Actually invokes action selected in the results list */
    public void invokeSelectedItem () {
        JList list = displayer.getList();
        if (list.getModel().getSize() > 0) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JList l = displayer.getList();
                    if (l.getSelectedIndex() == -1) { // Issue 161447
                        l.setSelectedIndex(l.getModel().getSize()-1);
                    }
                    displayer.invoke();
                }
            });
        }
    }

    private ChangeSupport getChangeSupport() {
        if(changeSupport == null) {
            changeSupport = new ChangeSupport(this);
        }
        return changeSupport;
    }

    public void setNoResults (boolean areNoResults) {
        // no op when called too soon
        if (command == null || origForeground == null) {
            return;
        }
        // don't alter color if showing hint already
        if (command.getForeground().equals(((JTextField) command.getEditor().getEditorComponent()).getDisabledTextColor())) {
            return;
        }
        command.setForeground(areNoResults ? Color.RED : origForeground);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox command;
    // End of variables declaration//GEN-END:variables


    @Override
    public void requestFocus() {
        super.requestFocus();        
        command.requestFocus();
    }

    public Component getIssueComponent() {
        return command;
    }

    String getText() {
        return ((JTextField)command.getEditor().getEditorComponent()).getText();
    }

    static Color getPopupBorderColor () {
        Color shadow = UIManager.getColor("controlShadow"); // NOI18N
        return shadow != null ? shadow : Color.GRAY;
    }

    static Color getTextBackground () {
        Color textB = UIManager.getColor("TextPane.background"); // NOI18N
        return textB != null ? textB : Color.WHITE;
    }

    static Color getResultBackground () {
        return getTextBackground();
    }

    boolean isTextFieldFocusOwner() {
        return command.getEditor().getEditorComponent().isFocusOwner();
    }

    private class ComboEditor implements ComboBoxEditor {
        private final JTextField editor;
        private IssueImpl issue;
        private boolean ignoreCommandChanges = false;
        private final ComboBoxEditor delegate;

        public ComboEditor(ComboBoxEditor delegate) {
            this.delegate = delegate;
            editor = (JTextField) delegate.getEditorComponent();
            editor.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent arg0) {
                    textChanged();
                }
                @Override
                public void removeUpdate(DocumentEvent arg0) {
                    textChanged();
                }
                @Override
                public void changedUpdate(DocumentEvent arg0) {
                    textChanged();
                }
                private void textChanged () {
                    if(ignoreCommandChanges) {
                        return;
                    }
                    if (isTextFieldFocusOwner()) {
                        if(!editor.getText().equals("")) {
                            command.hidePopup();
                        }
                        displayer.maybeEvaluate(editor.getText());
                    }
                    setItem(null, true);
                }
            });
            editor.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    commandKeyPressed(evt);
                }
            });
        }

        @Override
        public Component getEditorComponent() {
            return editor;
        }

        private void setItem(Object anObject, boolean keepText) {
            IssueImpl oldIssue = issue;
            IssueImpl newIssue = null;
            if(anObject == null) {
                issue = null;
                if(!keepText) {
                    editor.setText(""); // NOI18N
                }
            } else if(anObject instanceof IssueImpl) {
                newIssue = (IssueImpl) anObject;
            } else if (anObject instanceof Issue) {
                newIssue = APIAccessor.IMPL.getImpl((Issue) anObject);
            }
            if(newIssue != null) {
                issue = newIssue;
                ignoreCommandChanges = true;
                if(!keepText) {
                    editor.setText(IssueItem.getIssueDescription(issue));
                }
                ignoreCommandChanges = false;
            }
            if(oldIssue != null || issue != null) {
                getChangeSupport().fireChange();
            }
        }

        @Override
        public void setItem(Object anObject) {
            setItem(anObject, false);
        }

        @Override
        public Object getItem() {
            return issue;
        }

        @Override
        public void selectAll() {
            delegate.selectAll();
        }

        @Override
        public void addActionListener(ActionListener l) {
            delegate.addActionListener(l);
        }

        @Override
        public void removeActionListener(ActionListener l) {
            delegate.removeActionListener(l);
        }

        private void commandKeyPressed(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode()==KeyEvent.VK_DOWN) {
                if(displayer.isVisible()) {
                    displayer.selectNext();
                    evt.consume();
                }
            } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                if(displayer.isVisible()) {
                    displayer.selectPrev();
                    evt.consume();
                }
            } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                if(displayer.isVisible()) {
                    evt.consume();
                    invokeSelectedItem();
                }
            } else if ((evt.getKeyCode()) == KeyEvent.VK_ESCAPE) {
                    if(displayer.isVisible()) {
                        returnFocus();
                        displayer.clearModel();
                        requestFocus();
                        evt.consume();
                    }
            }
        }
    }

}
