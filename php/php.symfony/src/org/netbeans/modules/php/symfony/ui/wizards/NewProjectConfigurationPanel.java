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

package org.netbeans.modules.php.symfony.ui.wizards;

import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.symfony.SymfonyScript;
import org.netbeans.modules.php.symfony.ui.options.SymfonyOptions;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Utilities;

/**
 * @author Tomas Mysik
 */
public final class NewProjectConfigurationPanel extends JPanel implements ChangeListener {
    private static final long serialVersionUID = -1785087654312318594L;
    private static final String APP_FRONTEND = "frontend"; // NOI18N
    private static final String APP_BACKEND = "backend"; // NOI18N
    private static final Pattern APP_NAME_PATTERN = Pattern.compile("\\S+"); // NOI18N
    private static final Pattern SECRET_PATTERN = Pattern.compile("\\b" + SymfonyOptions.DEFAULT_SECRET + "\\b"); // NOI18N

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public NewProjectConfigurationPanel() {
        initComponents();
        // work around - keep the label on the right side
        optionsLabel.setMaximumSize(optionsLabel.getPreferredSize());

        projectParamsTextField.setText(getOptions().getDefaultParamsForProject());
        String defaultParamsForApps = getOptions().getDefaultParamsForApps();
        frontendParamsTextField.setText(defaultParamsForApps);
        backendParamsTextField.setText(defaultParamsForApps);
        otherParamsTextField.setText(defaultParamsForApps);

        initApp(frontendCheckBox, frontendParamsLabel, frontendParamsTextField, null);
        initApp(backendCheckBox, backendParamsLabel, backendParamsTextField, null);
        initApp(otherCheckBox, otherParamsLabel, otherParamsTextField, otherNameTextField);

        ItemListener defaultItemListener = new DefaultItemListener();
        frontendCheckBox.addItemListener(defaultItemListener);
        backendCheckBox.addItemListener(defaultItemListener);
        otherCheckBox.addItemListener(defaultItemListener);

        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        frontendParamsTextField.getDocument().addDocumentListener(defaultDocumentListener);
        backendParamsTextField.getDocument().addDocumentListener(defaultDocumentListener);
        otherNameTextField.getDocument().addDocumentListener(defaultDocumentListener);
        otherParamsTextField.getDocument().addDocumentListener(defaultDocumentListener);

        generateProjectLabel.addPropertyChangeListener("enabled", new PropertyChangeListener() { // NOI18N
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                enableOptionsLabel();
            }
        });
        enableOptionsLabel();
    }

    @Override
    public void addNotify() {
        SymfonyOptions.getInstance().addChangeListener(this);
        super.addNotify();
    }

    @Override
    public void removeNotify() {
        SymfonyOptions.getInstance().removeChangeListener(this);
        super.removeNotify();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public String[] getProjectParams() {
        return Utilities.parseParameters(projectParamsTextField.getText().trim());
    }

    // < app-name , params[] >
    public List<Pair<String, String[]>> getApps() {
        List<Pair<String, String[]>> apps = new LinkedList<>();
        if (frontendCheckBox.isSelected()) {
            apps.add(Pair.of(APP_FRONTEND, Utilities.parseParameters(frontendParamsTextField.getText().trim()))); // NOI18N
        }
        if (backendCheckBox.isSelected()) {
            apps.add(Pair.of(APP_BACKEND, Utilities.parseParameters(backendParamsTextField.getText().trim()))); // NOI18N
        }
        if (otherCheckBox.isSelected()) {
            apps.add(Pair.of(getOtherAppName(), Utilities.parseParameters(otherParamsTextField.getText().trim())));
        }
        return apps;
    }

    public String getErrorMessage() {
        if (otherCheckBox.isSelected()) {
            String otherAppName = getOtherAppName();
            if (!StringUtils.hasText(otherAppName)) {
                return NbBundle.getMessage(NewProjectConfigurationPanel.class, "MSG_NoAppName");
            } else if (!APP_NAME_PATTERN.matcher(otherAppName).matches()) {
                return NbBundle.getMessage(NewProjectConfigurationPanel.class, "MSG_InvalidAppName", otherAppName);
            }
        }
        return null;
    }

    public String getWarningMessage() {
        String warn = null;
        if (frontendCheckBox.isSelected()) {
            warn = validateAppParams(APP_FRONTEND, frontendParamsTextField);
            if (warn != null) {
                return warn;
            }
        }
        if (backendCheckBox.isSelected()) {
            warn = validateAppParams(APP_BACKEND, backendParamsTextField);
            if (warn != null) {
                return warn;
            }
        }
        if (otherCheckBox.isSelected()) {
            String otherAppName = getOtherAppName();
            warn = validateAppParams(otherAppName, otherParamsTextField);
            if (warn != null) {
                return warn;
            }
        }
        return null;
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    void visibleApp(boolean visible, JLabel paramsLabel, JTextField paramsTextField, JTextField nameTextField) {
        paramsLabel.setVisible(visible);
        paramsTextField.setVisible(visible);
        if (nameTextField != null) {
            nameTextField.setVisible(visible);
        }
    }

    void enableOptionsLabel() {
        optionsLabel.setVisible(generateProjectLabel.isEnabled());
    }

    private String getOtherAppName() {
        return otherNameTextField.getText().trim();
    }

    private String validateAppParams(String appName, JTextField paramsTextField) {
        if (SECRET_PATTERN.matcher(paramsTextField.getText()).find()) {
            return NbBundle.getMessage(NewProjectConfigurationPanel.class, "MSG_DefaultParamUsed", SymfonyOptions.DEFAULT_SECRET, appName);
        }
        return null;
    }

    private void initApp(JCheckBox nameCheckBox, final JLabel paramsLabel, final JTextField paramsTextField, final JTextField nameTextField) {
        nameCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                visibleApp(e.getStateChange() == ItemEvent.SELECTED, paramsLabel, paramsTextField, nameTextField);
            }
        });
        visibleApp(nameCheckBox.isSelected(), paramsLabel, paramsTextField, nameTextField);
    }

    private SymfonyOptions getOptions() {
        return SymfonyOptions.getInstance();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        generateProjectLabel = new JLabel();
        optionsLabel = new JLabel();
        projectParamsLabel = new JLabel();
        projectParamsTextField = new JTextField();
        generateAppsLabel = new JLabel();
        frontendCheckBox = new JCheckBox();
        frontendParamsLabel = new JLabel();
        frontendParamsTextField = new JTextField();
        backendCheckBox = new JCheckBox();
        backendParamsLabel = new JLabel();
        backendParamsTextField = new JTextField();
        otherCheckBox = new JCheckBox();
        otherNameTextField = new JTextField();
        otherParamsLabel = new JLabel();
        otherParamsTextField = new JTextField();
        infoLabel = new JLabel();

        setFocusTraversalPolicy(null);

        generateProjectLabel.setLabelFor(frontendCheckBox);
        Mnemonics.setLocalizedText(generateProjectLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.generateProjectLabel.text")); // NOI18N

        optionsLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(optionsLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.optionsLabel.text"));
        optionsLabel.setToolTipText(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.optionsLabel.toolTipText")); // NOI18N
        optionsLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                optionsLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                optionsLabelMousePressed(evt);
            }
        });

        projectParamsLabel.setLabelFor(projectParamsTextField);
        Mnemonics.setLocalizedText(projectParamsLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.projectParamsLabel.text")); // NOI18N

        generateAppsLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(generateAppsLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.generateAppsLabel.text")); // NOI18N

        frontendCheckBox.setSelected(true);
        Mnemonics.setLocalizedText(frontendCheckBox, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.frontendCheckBox.text")); // NOI18N

        frontendParamsLabel.setLabelFor(frontendParamsTextField);

        Mnemonics.setLocalizedText(frontendParamsLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.frontendParamsLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(backendCheckBox, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.backendCheckBox.text"));

        backendParamsLabel.setLabelFor(backendParamsTextField);

        Mnemonics.setLocalizedText(backendParamsLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.backendParamsLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(otherCheckBox, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherCheckBox.text"));

        otherParamsLabel.setLabelFor(otherParamsTextField);
        Mnemonics.setLocalizedText(otherParamsLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherParamsLabel.text")); // NOI18N

        infoLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(infoLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.infoLabel.text"));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(backendParamsLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(backendParamsTextField, GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE))
                    .addComponent(backendCheckBox))
                .addGap(0, 0, 0))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(otherParamsLabel))
                    .addComponent(otherCheckBox))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(otherParamsTextField, GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                    .addComponent(otherNameTextField, GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE))
                .addGap(0, 0, 0))
            .addGroup(layout.createSequentialGroup()
                .addComponent(generateProjectLabel)
                .addPreferredGap(ComponentPlacement.RELATED, 291, Short.MAX_VALUE)
                .addComponent(optionsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(frontendParamsLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(frontendParamsTextField, GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(frontendCheckBox)
                .addContainerGap(393, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(projectParamsLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(projectParamsTextField, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(generateAppsLabel)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(generateProjectLabel)
                    .addComponent(optionsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(projectParamsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectParamsLabel))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(generateAppsLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(frontendCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(frontendParamsLabel)
                    .addComponent(frontendParamsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(backendCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(backendParamsLabel)
                    .addComponent(backendParamsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(otherCheckBox)
                    .addComponent(otherNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(otherParamsLabel)
                    .addComponent(otherParamsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(infoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        generateProjectLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.generateProjectLabel.AccessibleContext.accessibleName")); // NOI18N
        generateProjectLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.generateProjectLabel.AccessibleContext.accessibleDescription")); // NOI18N
        optionsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.optionsLabel.AccessibleContext.accessibleName")); // NOI18N
        optionsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.optionsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        projectParamsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.projectParamsLabel.AccessibleContext.accessibleName")); // NOI18N
        projectParamsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.projectParamsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        projectParamsTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.projectParamsTextField.AccessibleContext.accessibleName")); // NOI18N
        projectParamsTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.projectParamsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        generateAppsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.generateAppsLabel.AccessibleContext.accessibleName")); // NOI18N
        generateAppsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.generateAppsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        frontendCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.frontendCheckBox.AccessibleContext.accessibleName")); // NOI18N
        frontendCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.frontendCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        frontendParamsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.frontendParamsLabel.AccessibleContext.accessibleName")); // NOI18N
        frontendParamsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.frontendParamsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        frontendParamsTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.frontendParamsTextField.AccessibleContext.accessibleName")); // NOI18N
        frontendParamsTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.frontendParamsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        backendCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.backendCheckBox.AccessibleContext.accessibleName")); // NOI18N
        backendCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.backendCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        backendParamsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.backendParamsLabel.AccessibleContext.accessibleName")); // NOI18N
        backendParamsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.backendParamsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        backendParamsTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.backendParamsTextField.AccessibleContext.accessibleName")); // NOI18N
        backendParamsTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.backendParamsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        otherCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherCheckBox.AccessibleContext.accessibleName")); // NOI18N
        otherCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        otherNameTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherNameTextField.AccessibleContext.accessibleName")); // NOI18N
        otherNameTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherNameTextField.AccessibleContext.accessibleDescription")); // NOI18N
        otherParamsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherParamsLabel.AccessibleContext.accessibleName")); // NOI18N
        otherParamsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherParamsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        otherParamsTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherParamsTextField.AccessibleContext.accessibleName")); // NOI18N
        otherParamsTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherParamsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        infoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.infoLabel.AccessibleContext.accessibleName")); // NOI18N
        infoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.infoLabel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void optionsLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_optionsLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_optionsLabelMouseEntered

    private void optionsLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_optionsLabelMousePressed
        OptionsDisplayer.getDefault().open(SymfonyScript.getOptionsPath());
    }//GEN-LAST:event_optionsLabelMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox backendCheckBox;
    private JLabel backendParamsLabel;
    private JTextField backendParamsTextField;
    private JCheckBox frontendCheckBox;
    private JLabel frontendParamsLabel;
    private JTextField frontendParamsTextField;
    private JLabel generateAppsLabel;
    private JLabel generateProjectLabel;
    private JLabel infoLabel;
    private JLabel optionsLabel;
    private JCheckBox otherCheckBox;
    private JTextField otherNameTextField;
    private JLabel otherParamsLabel;
    private JTextField otherParamsTextField;
    private JLabel projectParamsLabel;
    private JTextField projectParamsTextField;
    // End of variables declaration//GEN-END:variables

    private final class DefaultItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            fireChange();
        }
    }

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
            fireChange();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }
}
