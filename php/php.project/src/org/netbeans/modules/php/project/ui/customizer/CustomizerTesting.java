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
package org.netbeans.modules.php.project.ui.customizer;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.php.api.testing.PhpTesting;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.ui.PathUiSupport;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


public class CustomizerTesting extends JPanel {

    private static final long serialVersionUID = -654768735165768L;

    private final ProjectCustomizer.Category category;
    private final PhpProjectProperties uiProps;
    private final DefaultListModel<BasePathSupport.Item> testDirectoriesPathListModel;
    final Map<String, TestingProviderPanel> testingPanels;
    // @GuardedBy("EDT")
    final Set<String> selectedTestingProviders = new TreeSet<>();


    CustomizerTesting(ProjectCustomizer.Category category, PhpProjectProperties uiProps, Map<String, TestingProviderPanel> testingPanels) {
        assert category != null;
        assert uiProps != null;
        assert testingPanels != null;

        this.category = category;
        this.uiProps = uiProps;
        this.testingPanels = testingPanels;
        testDirectoriesPathListModel = uiProps.getTestDirectoriesListModel();

        initComponents();
        init();
    }

    private void init() {
        initTestDirectories();
        initProvidersPanel();
    }

    private void initTestDirectories() {
        PathUiSupport.EditMediator.FileChooserDirectoryHandler directoryHandler = new PathUiSupport.EditMediator.FileChooserDirectoryHandler() {
            @Override
            public String getDirKey() {
                return CustomizerTesting.class.getName();
            }
            @Override
            public File getCurrentDirectory() {
                return FileUtil.toFile(uiProps.getProject().getProjectDirectory());
            }
        };
        testDirsList.setModel(testDirectoriesPathListModel);
        testDirsList.setCellRenderer(uiProps.getTestDirectoriesListRenderer());
        testDirectoriesPathListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                validateAndStore();
            }
            @Override
            public void intervalRemoved(ListDataEvent e) {
                validateAndStore();
            }
            @Override
            public void contentsChanged(ListDataEvent e) {
                validateAndStore();
            }
        });
        PathUiSupport.EditMediator.register(uiProps.getProject(),
                                               testDirsList,
                                               addFolderButton.getModel(),
                                               removeButton.getModel(),
                                               moveUpButton.getModel(),
                                               moveDownButton.getModel(),
                                               directoryHandler);
    }

    @NbBundle.Messages("CustomizerTesting.testingProviders.noneInstalled=No PHP testing provider found, install one via Plugins (e.g. PHPUnit).")
    private void initProvidersPanel() {
        List<PhpTestingProvider> allTestingProviders = PhpTesting.getTestingProviders();
        if (allTestingProviders.isEmpty()) {
            category.setErrorMessage(Bundle.CustomizerTesting_testingProviders_noneInstalled());
            category.setValid(true);
            return;
        }
        List<String> currentTestingProviders = uiProps.getTestingProviders();
        GroupLayout providersPanelLayout = new GroupLayout(providersPanel);
        GroupLayout.ParallelGroup horizontalGroup = providersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup verticalGroup = providersPanelLayout.createSequentialGroup();
        final Collator collator = Collator.getInstance();
        allTestingProviders.sort(new Comparator<PhpTestingProvider>() {
            @Override
            public int compare(PhpTestingProvider provider1, PhpTestingProvider provider2) {
                return collator.compare(provider1.getDisplayName(), provider2.getDisplayName());
            }
        });
        for (PhpTestingProvider testingProvider : allTestingProviders) {
            String identifier = testingProvider.getIdentifier();
            JCheckBox checkBox = new JCheckBox(testingProvider.getDisplayName());
            checkBox.addItemListener(new TestingProviderListener(identifier));
            if (currentTestingProviders.contains(identifier)) {
                checkBox.setSelected(true);
            }
            horizontalGroup.addComponent(checkBox);
            verticalGroup.addComponent(checkBox);
            verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        }
        providersPanel.setLayout(providersPanelLayout);
        providersPanelLayout.setHorizontalGroup(
            providersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(providersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(horizontalGroup)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        providersPanelLayout.setVerticalGroup(
            providersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(verticalGroup)
        );
        // set initial message (if any)
        validateAndStore();
    }

    void validateAndStore() {
        validateData();
        storeData();
    }

    @NbBundle.Messages("CustomizerTesting.error.none=For running tests, at least one testing provider must be selected.")
    private void validateData() {
        assert EventQueue.isDispatchThread();
        // test dirs
        ValidationResult result = new TestDirectoriesPathSupport.Validator()
                .validatePaths(uiProps.getProject(), convertToList(testDirectoriesPathListModel))
                .getResult();
        if (result.hasErrors()) {
            category.setErrorMessage(result.getErrors().get(0).getMessage());
            category.setValid(false);
            return;
        }
        if (result.hasWarnings()) {
            category.setErrorMessage(result.getWarnings().get(0).getMessage());
            category.setValid(true);
            return;
        }
        // providers
        if (selectedTestingProviders.isEmpty()) {
            category.setErrorMessage(Bundle.CustomizerTesting_error_none());
            category.setValid(true);
            return;
        }
        // everything ok
        category.setErrorMessage(" "); // NOI18N
        category.setValid(true);
    }

    private void storeData() {
        assert EventQueue.isDispatchThread();
        uiProps.setTestingProviders(new ArrayList<>(selectedTestingProviders));
    }

    private List<BasePathSupport.Item> convertToList(DefaultListModel<BasePathSupport.Item> listModel) {
        List<BasePathSupport.Item> items = new ArrayList<>(listModel.getSize());
        for (int i = 0; i < listModel.getSize(); i++) {
            items.add(listModel.get(i));
        }
        return items;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        testDirsLabel = new JLabel();
        testDirsScrollPane = new JScrollPane();
        testDirsList = new JList<BasePathSupport.Item>();
        addFolderButton = new JButton();
        removeButton = new JButton();
        moveUpButton = new JButton();
        moveDownButton = new JButton();
        providersLabel = new JLabel();
        providersPanel = new JPanel();

        Mnemonics.setLocalizedText(testDirsLabel, NbBundle.getMessage(CustomizerTesting.class, "CustomizerTesting.testDirsLabel.text")); // NOI18N

        testDirsScrollPane.setViewportView(testDirsList);

        Mnemonics.setLocalizedText(addFolderButton, NbBundle.getMessage(CustomizerTesting.class, "CustomizerTesting.addFolderButton.text")); // NOI18N

        Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(CustomizerTesting.class, "CustomizerTesting.removeButton.text")); // NOI18N

        Mnemonics.setLocalizedText(moveUpButton, NbBundle.getMessage(CustomizerTesting.class, "CustomizerTesting.moveUpButton.text")); // NOI18N

        Mnemonics.setLocalizedText(moveDownButton, NbBundle.getMessage(CustomizerTesting.class, "CustomizerTesting.moveDownButton.text")); // NOI18N

        Mnemonics.setLocalizedText(providersLabel, NbBundle.getMessage(CustomizerTesting.class, "CustomizerTesting.providersLabel.text")); // NOI18N

        GroupLayout providersPanelLayout = new GroupLayout(providersPanel);
        providersPanel.setLayout(providersPanelLayout);
        providersPanelLayout.setHorizontalGroup(
            providersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 407, Short.MAX_VALUE)
        );
        providersPanelLayout.setVerticalGroup(
            providersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 46, Short.MAX_VALUE)
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(providersPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(testDirsScrollPane)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(addFolderButton, GroupLayout.Alignment.TRAILING)
                    .addComponent(removeButton, GroupLayout.Alignment.TRAILING)
                    .addComponent(moveUpButton, GroupLayout.Alignment.TRAILING)
                    .addComponent(moveDownButton, GroupLayout.Alignment.TRAILING)))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(testDirsLabel)
                    .addComponent(providersLabel))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {addFolderButton, moveDownButton, moveUpButton, removeButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(testDirsLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(testDirsScrollPane, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addFolderButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(moveUpButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveDownButton)))
                .addGap(18, 18, 18)
                .addComponent(providersLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(providersPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton addFolderButton;
    private JButton moveDownButton;
    private JButton moveUpButton;
    private JLabel providersLabel;
    private JPanel providersPanel;
    private JButton removeButton;
    private JLabel testDirsLabel;
    private JList<BasePathSupport.Item> testDirsList;
    private JScrollPane testDirsScrollPane;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class TestingProviderListener implements ItemListener {

        private final String testingProvider;


        public TestingProviderListener(String testingProvider) {
            this.testingProvider = testingProvider;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            assert EventQueue.isDispatchThread();
            if (e.getStateChange() == ItemEvent.SELECTED) {
                boolean added = selectedTestingProviders.add(testingProvider);
                assert added : "Provider " + testingProvider + " already present in " + selectedTestingProviders;
                TestingProviderPanel panel = testingPanels.get(testingProvider);
                if (panel != null) {
                    panel.showProviderPanel();
                }
            } else {
                boolean removed = selectedTestingProviders.remove(testingProvider);
                assert removed : "Provider " + testingProvider + " not present in " + selectedTestingProviders;
                TestingProviderPanel panel = testingPanels.get(testingProvider);
                if (panel != null) {
                    panel.hideProviderPanel();
                }
            }
            validateAndStore();
        }

    }

}
