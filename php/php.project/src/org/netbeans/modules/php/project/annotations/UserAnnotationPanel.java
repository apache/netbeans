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
package org.netbeans.modules.php.project.annotations;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel (form) for user annotation.
 */
public class UserAnnotationPanel extends JPanel implements HelpCtx.Provider {

    private static final long serialVersionUID = -135764354564L;

    private static final String HTML_CONTENT_TYPE = "text/html"; // NOI18N

    // @GuardedBy(EDT)
    private final EnumMap<UserAnnotationTag.Type, JCheckBox> typeCheckBoxes;
    // @GuardedBy(EDT)
    private DialogDescriptor descriptor = null;
    // @GuardedBy(EDT)
    private NotificationLineSupport notificationLineSupport = null;


    public UserAnnotationPanel(UserAnnotationTag annotation) {
        assert EventQueue.isDispatchThread();
        assert annotation != null;

        initComponents();
        typeCheckBoxes = createTypeCheckBoxes();
        initSourceEditor();
        init(annotation);
    }

    private void initSourceEditor() {
        sourceEditorPane.setEditorKit(CloneableEditorSupport.getEditorKit(HTML_CONTENT_TYPE));
        // ui
        Font font = new JLabel().getFont();
        sourceEditorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        sourceEditorPane.setFont(font);
        previewTextPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        previewTextPane.setFont(font);
    }

    private void init(UserAnnotationTag annotation) {
        assert EventQueue.isDispatchThread();
        // values
        nameTextField.setText(annotation.getName());
        selectTypes(annotation.getTypes());
        templateTextField.setText(annotation.getInsertTemplate());
        setDocumentation(annotation.getDocumentation());
        // listeners
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        ItemListener defaultItemListener = new DefaultItemListener();
        nameTextField.getDocument().addDocumentListener(defaultDocumentListener);
        for (JCheckBox checkBox : typeCheckBoxes.values()) {
            checkBox.addItemListener(defaultItemListener);
        }
        templateTextField.getDocument().addDocumentListener(defaultDocumentListener);
        sourceEditorPane.getDocument().addDocumentListener(defaultDocumentListener);
        docTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                refreshPreview();
            }
        });
    }

    @NbBundle.Messages("UserAnnotationPanel.title=Custom Annotation")
    public boolean open() {
        assert EventQueue.isDispatchThread();
        descriptor = new DialogDescriptor(
                this,
                Bundle.UserAnnotationPanel_title(),
                true,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.OK_OPTION,
                null);
        notificationLineSupport = descriptor.createNotificationLineSupport();
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        validateAnnotation();
        boolean okPressed;
        try {
            dialog.setVisible(true);
            okPressed = descriptor.getValue() == NotifyDescriptor.OK_OPTION;
        } finally {
            dialog.dispose();
        }
        return okPressed;
    }

    public UserAnnotationTag getAnnotation() {
        return new UserAnnotationTag(
                getSelectedTypes(),
                nameTextField.getText(),
                templateTextField.getText(),
                sourceEditorPane.getText());
    }

    @NbBundle.Messages({
        "UserAnnotationPanel.error.noName=Name must be set.",
        "UserAnnotationPanel.error.noFor=At least one target must be set.",
        "UserAnnotationPanel.error.noTemplate=Template must be set."
    })
    void validateAnnotation() {
        if (!StringUtils.hasText(nameTextField.getText())) {
            setError(Bundle.UserAnnotationPanel_error_noName());
            return;
        }
        if (!anyTypeSelected()) {
            setError(Bundle.UserAnnotationPanel_error_noFor());
            return;
        }
        if (!StringUtils.hasText(templateTextField.getText())) {
            setError(Bundle.UserAnnotationPanel_error_noTemplate());
            return;
        }
        clearError();
    }

    void setError(String error) {
        assert EventQueue.isDispatchThread();
        notificationLineSupport.setErrorMessage(error);
        descriptor.setValid(false);
    }

    void clearError() {
        assert EventQueue.isDispatchThread();
        notificationLineSupport.clearMessages();
        descriptor.setValid(true);
    }

    void refreshPreview() {
        if (docTabbedPane.getSelectedIndex() == 1) {
            previewTextPane.setText(sourceEditorPane.getText());
            previewTextPane.setCaretPosition(0);
        }
    }

    private EnumMap<UserAnnotationTag.Type, JCheckBox> createTypeCheckBoxes() {
        EnumMap<UserAnnotationTag.Type, JCheckBox> map = new EnumMap<>(UserAnnotationTag.Type.class);
        for (UserAnnotationTag.Type type : UserAnnotationTag.Type.values()) {
            JCheckBox checkBox;
            switch (type) {
                case FUNCTION:
                    checkBox = functionCheckBox;
                    break;
                case TYPE:
                    checkBox = typeCheckBox;
                    break;
                case METHOD:
                    checkBox = methodCheckBox;
                    break;
                case FIELD:
                    checkBox = fieldCheckBox;
                    break;
                default:
                    throw new IllegalStateException("Unknown type: " + type);
            }
            map.put(type, checkBox);
        }
        return map;
    }

    private void selectTypes(EnumSet<UserAnnotationTag.Type> types) {
        assert EventQueue.isDispatchThread();
        for (UserAnnotationTag.Type type : types) {
            typeCheckBoxes.get(type).setSelected(true);
        }
    }

    private void setDocumentation(String documentation) {
        try {
            sourceEditorPane.getDocument().insertString(0, documentation, null);
        } catch (BadLocationException ex) {
            assert false : ex;
        }
        sourceEditorPane.setCaretPosition(0);
    }

    private boolean anyTypeSelected() {
        assert EventQueue.isDispatchThread();
        for (JCheckBox checkBox : typeCheckBoxes.values()) {
            if (checkBox.isSelected()) {
                return true;
            }
        }
        return false;
    }

    private EnumSet<UserAnnotationTag.Type> getSelectedTypes() {
        assert EventQueue.isDispatchThread();
        EnumSet<UserAnnotationTag.Type> types = EnumSet.noneOf(UserAnnotationTag.Type.class);
        for (Map.Entry<UserAnnotationTag.Type, JCheckBox> entry : typeCheckBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                types.add(entry.getKey());
            }
        }
        return types;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.project.annotations.UserAnnotationPanel"); // NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new JLabel();
        nameTextField = new JTextField();
        forLabel = new JLabel();
        functionCheckBox = new JCheckBox();
        typeCheckBox = new JCheckBox();
        fieldCheckBox = new JCheckBox();
        methodCheckBox = new JCheckBox();
        templateLabel = new JLabel();
        templateTextField = new JTextField();
        docLabel = new JLabel();
        docTabbedPane = new JTabbedPane();
        sourceScrollPane = new JScrollPane();
        sourceEditorPane = new JEditorPane();
        previewScrollPane = new JScrollPane();
        previewTextPane = new JTextPane();

        setPreferredSize(new Dimension(400, 350));

        nameLabel.setLabelFor(nameTextField);
        Mnemonics.setLocalizedText(nameLabel, NbBundle.getMessage(UserAnnotationPanel.class, "UserAnnotationPanel.nameLabel.text")); // NOI18N

        forLabel.setLabelFor(functionCheckBox);

        Mnemonics.setLocalizedText(forLabel, NbBundle.getMessage(UserAnnotationPanel.class, "UserAnnotationPanel.forLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(functionCheckBox, NbBundle.getMessage(UserAnnotationPanel.class, "UserAnnotationPanel.functionCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(typeCheckBox, NbBundle.getMessage(UserAnnotationPanel.class, "UserAnnotationPanel.typeCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(fieldCheckBox, NbBundle.getMessage(UserAnnotationPanel.class, "UserAnnotationPanel.fieldCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(methodCheckBox, NbBundle.getMessage(UserAnnotationPanel.class, "UserAnnotationPanel.methodCheckBox.text")); // NOI18N

        templateLabel.setLabelFor(templateTextField);
        Mnemonics.setLocalizedText(templateLabel, NbBundle.getMessage(UserAnnotationPanel.class, "UserAnnotationPanel.templateLabel.text")); // NOI18N

        docLabel.setLabelFor(docTabbedPane);
        Mnemonics.setLocalizedText(docLabel, NbBundle.getMessage(UserAnnotationPanel.class, "UserAnnotationPanel.docLabel.text")); // NOI18N

        sourceScrollPane.setViewportView(sourceEditorPane);

        docTabbedPane.addTab(NbBundle.getMessage(UserAnnotationPanel.class, "UserAnnotationPanel.sourceScrollPane.TabConstraints.tabTitle"), sourceScrollPane);
        previewTextPane.setContentType("text/html"); // NOI18N
        previewTextPane.setEditable(false);
        previewScrollPane.setViewportView(previewTextPane);

        docTabbedPane.addTab(NbBundle.getMessage(UserAnnotationPanel.class, "UserAnnotationPanel.previewScrollPane.TabConstraints.tabTitle"), previewScrollPane);
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                .addContainerGap()

                .addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()

                        .addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(forLabel).addComponent(nameLabel)).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(nameTextField).addGroup(layout.createSequentialGroup()

                                .addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(functionCheckBox).addComponent(fieldCheckBox)).addGap(18, 18, 18).addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(methodCheckBox).addComponent(typeCheckBox)).addGap(0, 0, Short.MAX_VALUE)))).addComponent(templateTextField).addGroup(layout.createSequentialGroup()

                        .addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(templateLabel).addComponent(docLabel)).addGap(0, 0, Short.MAX_VALUE)).addComponent(docTabbedPane, GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)).addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                .addContainerGap()

                .addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(nameLabel).addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(forLabel).addComponent(functionCheckBox).addComponent(typeCheckBox)).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(fieldCheckBox).addComponent(methodCheckBox)).addPreferredGap(ComponentPlacement.RELATED).addComponent(templateLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(templateTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(docLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(docTabbedPane, GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel docLabel;
    private JTabbedPane docTabbedPane;
    private JCheckBox fieldCheckBox;
    private JLabel forLabel;
    private JCheckBox functionCheckBox;
    private JCheckBox methodCheckBox;
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JScrollPane previewScrollPane;
    private JTextPane previewTextPane;
    private JEditorPane sourceEditorPane;
    private JScrollPane sourceScrollPane;
    private JLabel templateLabel;
    private JTextField templateTextField;
    private JCheckBox typeCheckBox;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            validateAnnotation();
        }

    }

    private final class DefaultItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            validateAnnotation();
        }

    }

}
