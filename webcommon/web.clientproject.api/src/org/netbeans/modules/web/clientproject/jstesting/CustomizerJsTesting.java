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

package org.netbeans.modules.web.clientproject.jstesting;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.netbeans.modules.web.clientproject.util.WebCommonUtils;
import org.netbeans.modules.web.common.api.UsageLogger;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

class CustomizerJsTesting extends JPanel implements ChangeListener {

    private final ProjectCustomizer.Category category;
    private final Project project;
    private final JsTestingProvider originalProvider;
    // @GuardedBy("EDT")
    private final Map<JsTestingProvider, CustomizerPanelImplementation> providerPanels;
    private final UsageLogger usageLogger = new UsageLogger.Builder(WebCommonUtils.USAGE_LOGGER_NAME)
            .message(UsageLogger.class, "USG_TEST_CONFIG_JS") // NOI18N
            .firstMessageOnly(false)
            .create();

    volatile JsTestingProvider selectedProvider;
    volatile CustomizerPanelImplementation selectedPanel;


    CustomizerJsTesting(ProjectCustomizer.Category category, Project project) {
        assert EventQueue.isDispatchThread();
        assert category != null;
        assert project != null;

        this.category = category;
        this.project = project;
        originalProvider = JsTestingProviders.getDefault().getJsTestingProvider(project, false);
        providerPanels = createProviderPanels();
        selectedProvider = originalProvider;
        selectedPanel = getSelectedPanel();

        initComponents();
        init();
    }

    private Map<JsTestingProvider, CustomizerPanelImplementation> createProviderPanels() {
        Map<JsTestingProvider, CustomizerPanelImplementation> panels = new HashMap<>();
        for (JsTestingProvider provider : JsTestingProviders.getDefault().getJsTestingProviders()) {
            panels.put(provider, JsTestingProviderAccessor.getDefault().createCustomizerPanel(provider, project));
        }
        return panels;
    }

    private void init() {
        providerComboBox.addItem(null);
        for (JsTestingProvider provider : JsTestingProviders.getDefault().getJsTestingProviders()) {
            providerComboBox.addItem(provider);
        }
        providerComboBox.setSelectedItem(originalProvider);
        providerComboBox.setRenderer(new JsTestingProviderRenderer());
        // listeners
        providerComboBox.addActionListener(new ProviderActionListener());
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                storeData();
            }
        });
        category.setCloseListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanup();
            }
        });
        // initial setup
        providerChanged();
    }

    void providerChanged() {
        assert EventQueue.isDispatchThread();
        // remove existing listener
        if (selectedPanel != null) {
            selectedPanel.removeChangeListener(this);
        }
        // switch panel
        providerPanel.removeAll();
        selectedProvider = (JsTestingProvider) providerComboBox.getSelectedItem();
        selectedPanel = getSelectedPanel();
        if (selectedPanel != null) {
            selectedPanel.addChangeListener(this);
            providerPanel.add(selectedPanel.getComponent(), BorderLayout.CENTER);
        }
        providerPanel.revalidate();
        providerPanel.repaint();
        // validate
        validateData();
    }

    void validateData() {
        assert EventQueue.isDispatchThread();
        if (selectedProvider == null) {
            // no provider
            category.setErrorMessage(null);
            category.setValid(true);
            return;
        }
        if (selectedPanel == null) {
            // no provider panel
            category.setErrorMessage(null);
            category.setValid(true);
            return;
        }
        if (selectedPanel.isValid()) {
            // provider panel is valid
            category.setErrorMessage(selectedPanel.getWarningMessage());
            category.setValid(true);
            return;
        }
        // some error
        assert selectedPanel.getErrorMessage() != null : "Error must be return for invalid panel of " + selectedProvider;
        category.setErrorMessage(selectedPanel.getErrorMessage());
        category.setValid(false);
    }

    void storeData() {
        assert !EventQueue.isDispatchThread();
        if (selectedPanel != null) {
            selectedPanel.save();
        }
        if (Objects.equals(originalProvider, selectedProvider)) {
            // no change in provider => exit
            return;
        }
        usageLogger.log(project.getClass().getName(), selectedProvider == null ? "" : selectedProvider.getIdentifier());
        if (originalProvider != null) {
            JsTestingProviderAccessor.getDefault().notifyEnabled(originalProvider, project, false);
        }
        if (selectedProvider != null) {
            JsTestingProviderAccessor.getDefault().notifyEnabled(selectedProvider, project, true);
        }
    }

    void cleanup() {
        if (selectedPanel != null) {
            selectedPanel.removeChangeListener(this);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // change in provider panel itself
        validateData();
    }

    @CheckForNull
    private CustomizerPanelImplementation getSelectedPanel() {
        assert EventQueue.isDispatchThread();
        assert providerPanels != null;
        return providerPanels.get(selectedProvider);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        providerLabel = new JLabel();
        providerComboBox = new JComboBox<JsTestingProvider>();
        separator = new JSeparator();
        providerPanel = new JPanel();

        providerLabel.setLabelFor(providerComboBox);
        Mnemonics.setLocalizedText(providerLabel, NbBundle.getMessage(CustomizerJsTesting.class, "CustomizerJsTesting.providerLabel.text")); // NOI18N

        providerPanel.setLayout(new BorderLayout());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(providerPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(providerLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(providerComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addComponent(separator)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(providerLabel)
                    .addComponent(providerComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(providerPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<JsTestingProvider> providerComboBox;
    private JLabel providerLabel;
    private JPanel providerPanel;
    private JSeparator separator;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class ProviderActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            providerChanged();
        }

    }

}
