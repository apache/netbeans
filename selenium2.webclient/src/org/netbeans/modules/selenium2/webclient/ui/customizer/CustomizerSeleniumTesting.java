/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.selenium2.webclient.ui.customizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProvider;
import org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProviders;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;

/**
 *
 * @author Theofanis Oikonomou
 */
public class CustomizerSeleniumTesting extends javax.swing.JPanel implements ChangeListener {

    private final ProjectCustomizer.Category category;
    private final Project project;
    private final SeleniumTestingProvider originalProvider;
    // @GuardedBy("EDT")
    private final Map<SeleniumTestingProvider, CustomizerPanelImplementation> providerPanels;
//    private final UsageLogger usageLogger = new UsageLogger.Builder(WebCommonUtils.USAGE_LOGGER_NAME)
//            .message(UsageLogger.class, "USG_TEST_CONFIG_JS") // NOI18N
//            .create();

    volatile SeleniumTestingProvider selectedProvider;
    volatile CustomizerPanelImplementation selectedPanel;

    /**
     * Creates new form Selenium2CustomizerPanel
     */
    public CustomizerSeleniumTesting(ProjectCustomizer.Category category, Project project) {
        assert EventQueue.isDispatchThread();
        assert category != null;
        assert project != null;

        this.category = category;
        this.project = project;
        originalProvider = SeleniumTestingProviders.getDefault().getSeleniumTestingProvider(project, false);
        providerPanels = createProviderPanels();
        selectedProvider = originalProvider;
        selectedPanel = getSelectedPanel();

        initComponents();
        init();
    }

    private Map<SeleniumTestingProvider, CustomizerPanelImplementation> createProviderPanels() {
        Map<SeleniumTestingProvider, CustomizerPanelImplementation> panels = new HashMap<>();
        for (SeleniumTestingProvider provider : SeleniumTestingProviders.getDefault().getSeleniumTestingProviders()) {
            panels.put(provider, SeleniumTestingProviderAccessor.getDefault().createCustomizerPanel(provider, project));
        }
        return panels;
    }

    private void init() {
        providerComboBox.addItem(null);
        for (SeleniumTestingProvider provider : SeleniumTestingProviders.getDefault().getSeleniumTestingProviders()) {
            providerComboBox.addItem(provider);
        }
        providerComboBox.setSelectedItem(originalProvider);
        providerComboBox.setRenderer(new SeleniumTestingProviderRenderer());
        // listeners
        providerComboBox.addActionListener(new CustomizerSeleniumTesting.ProviderActionListener());
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
        selectedProvider = (SeleniumTestingProvider) providerComboBox.getSelectedItem();
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
//        usageLogger.log(project.getClass().getName(), selectedProvider == null ? "" : selectedProvider.getIdentifier());
        if (originalProvider != null) {
            SeleniumTestingProviderAccessor.getDefault().notifyEnabled(originalProvider, project, false);
        }
        if (selectedProvider != null) {
            SeleniumTestingProviderAccessor.getDefault().notifyEnabled(selectedProvider, project, true);
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
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        providerLabel = new javax.swing.JLabel();
        providerComboBox = new javax.swing.JComboBox();
        separator = new javax.swing.JSeparator();
        providerPanel = new javax.swing.JPanel();

        providerLabel.setLabelFor(providerComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(providerLabel, org.openide.util.NbBundle.getMessage(CustomizerSeleniumTesting.class, "CustomizerSeleniumTesting.providerLabel.text")); // NOI18N

        providerPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(providerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(providerLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(providerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(separator))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(providerLabel)
                    .addComponent(providerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(providerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox providerComboBox;
    private javax.swing.JLabel providerLabel;
    private javax.swing.JPanel providerPanel;
    private javax.swing.JSeparator separator;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class ProviderActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            providerChanged();
        }

    }
    
}
