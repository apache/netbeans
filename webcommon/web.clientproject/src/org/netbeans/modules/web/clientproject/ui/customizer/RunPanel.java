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
package org.netbeans.modules.web.clientproject.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.api.CustomizerPanel;
import org.netbeans.modules.web.clientproject.api.platform.PlatformProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class RunPanel extends JPanel implements HelpCtx.Provider, ChangeListener {

    private final ClientSideProject project;
    private final ProjectCustomizer.Category category;
    private final ClientSideProjectProperties uiProperties;
    private final BrowserRunPanel browserRunPanel;
    private final List<CustomizerPanel> platformPanels = new CopyOnWriteArrayList<>();

    private volatile CustomizerPanel selectedPanel = null;


    public RunPanel(ProjectCustomizer.Category category, ClientSideProjectProperties uiProperties) {
        assert category != null;
        assert uiProperties != null;

        this.category = category;
        this.uiProperties = uiProperties;
        project = uiProperties.getProject();
        browserRunPanel = new BrowserRunPanel(uiProperties);

        for (PlatformProvider platformProvider : project.getPlatformProviders()) {
            platformPanels.addAll(platformProvider.getRunCustomizerPanels(project));
        }

        initComponents();
        init();

        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedPanel != null) {
                    selectedPanel.save();
                }
            }
        });
    }

    private void init() {
        // init values
        String selectedRunAs = uiProperties.getRunAs().get();
        runBrowserCheckBox.setSelected(uiProperties.isRunBrowser());
        runAsComboBox.setRenderer(new RunAsRenderer());
        // platform
        if (platformPanels.isEmpty()) {
            runAsHolder.setVisible(false);
            platformProviderPanelHolder.setVisible(false);
            runBrowserHolder.setVisible(false);
        } else {
            // default item ("Web Application")
            if (!project.isJsLibrary()) {
                runAsComboBox.addItem(null);
            }
            for (CustomizerPanel platformPanel : platformPanels) {
                runAsComboBox.addItem(platformPanel);
                if (platformPanel.getIdentifier().equals(selectedRunAs)) {
                    runAsComboBox.setSelectedItem(platformPanel);
                }
            }
            switchPlatformPanel();
        }
        // browser
        if (project.isJsLibrary()) {
            runBrowserHolder.setVisible(false);
            browserPanelHolder.setVisible(false);
        } else {
            browserPanel.add(browserRunPanel, BorderLayout.CENTER);
            browserPanel.revalidate();
            browserPanel.repaint();
        }
        // ui
        if (runAsComboBox.getItemCount() <= 1) {
            runAsHolder.setVisible(false);
        }
        browserPanel.setVisible(isRunBrowser());
        // listeners
        initListeners();
    }

    private void initListeners() {
        runAsComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                switchPlatformPanel();
                validateAndStoreData();
            }
        });
        runBrowserCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                browserPanel.setVisible(e.getStateChange() == ItemEvent.SELECTED);
                validateAndStoreData();
            }
        });
        browserRunPanel.addChangeListener(this);
    }

    private boolean isRunBrowser() {
        if (project.isJsLibrary()) {
            return false;
        }
        if (getSelectedPanel() == null) {
            return true;
        }
        return runBrowserCheckBox.isSelected();
    }

    @CheckForNull
    private String getRunAs() {
        CustomizerPanel panel = getSelectedPanel();
        if (panel == null) {
            return null;
        }
        return panel.getIdentifier();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.web.clientproject.ui.customizer.RunPanel"); // NOI18N
    }

    void switchPlatformPanel() {
        assert EventQueue.isDispatchThread();
        assert platformProviderPanel.isVisible();
        if (selectedPanel != null) {
            selectedPanel.removeChangeListener(this);
        }
        platformProviderPanel.removeAll();
        selectedPanel = getSelectedPanel();
        if (selectedPanel != null) {
            // some platform
            platformProviderPanelHolder.setVisible(true);
            selectedPanel.addChangeListener(this);
            platformProviderPanel.add(selectedPanel.getComponent(), BorderLayout.CENTER);
            runBrowserHolder.setVisible(!project.isJsLibrary());
            browserRunPanel.onlyExternalUrl(true);
        } else {
            // just browser
            platformProviderPanelHolder.setVisible(false);
            runBrowserHolder.setVisible(false);
            browserRunPanel.onlyExternalUrl(false);
        }
        platformProviderPanelHolder.revalidate();
        platformProviderPanelHolder.repaint();
        browserPanel.setVisible(isRunBrowser());
        // force validation
        validateAndStoreData();
    }

    void validateAndStoreData() {
        validateData();
        storeData();
    }

    private void validateData() {
        assert EventQueue.isDispatchThread();
        String error = null;
        String warning = null;
        // platform
        if (selectedPanel != null) {
            error = selectedPanel.getErrorMessage();
            warning = selectedPanel.getWarningMessage();
        }
        // browser
        if (isRunBrowser()) {
            if (error == null) {
                error = browserRunPanel.getErrorMessage();
            }
            if (warning == null) {
                warning = browserRunPanel.getWarningMessage();
            }
        }
        // message
        if (error != null) {
            category.setErrorMessage(error);
            category.setValid(false);
            return;
        }
        if (warning != null) {
            category.setErrorMessage(warning);
            category.setValid(true);
            return;
        }
        category.setErrorMessage(null);
        category.setValid(true);
    }

    private void storeData() {
        uiProperties.setRunAs(getRunAs());
        uiProperties.setRunBrowser(isRunBrowser());
    }

    @CheckForNull
    private CustomizerPanel getSelectedPanel() {
        if (platformPanels.isEmpty()) {
            return null;
        }
        return (CustomizerPanel) runAsComboBox.getSelectedItem();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        runAsHolder = new JPanel();
        runAsLabel = new JLabel();
        runAsComboBox = new JComboBox<CustomizerPanel>();
        platformProviderPanelHolder = new JPanel();
        platformProviderPanel = new JPanel();
        runBrowserHolder = new JPanel();
        runBrowserCheckBox = new JCheckBox();
        browserPanelHolder = new JPanel();
        browserPanel = new JPanel();

        Mnemonics.setLocalizedText(runAsLabel, NbBundle.getMessage(RunPanel.class, "RunPanel.runAsLabel.text")); // NOI18N

        GroupLayout runAsHolderLayout = new GroupLayout(runAsHolder);
        runAsHolder.setLayout(runAsHolderLayout);
        runAsHolderLayout.setHorizontalGroup(runAsHolderLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(runAsHolderLayout.createSequentialGroup()
                .addComponent(runAsLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(runAsComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        runAsHolderLayout.setVerticalGroup(runAsHolderLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(runAsHolderLayout.createSequentialGroup()
                .addGroup(runAsHolderLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(runAsLabel)
                    .addComponent(runAsComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        platformProviderPanel.setLayout(new BorderLayout());

        GroupLayout platformProviderPanelHolderLayout = new GroupLayout(platformProviderPanelHolder);
        platformProviderPanelHolder.setLayout(platformProviderPanelHolderLayout);
        platformProviderPanelHolderLayout.setHorizontalGroup(platformProviderPanelHolderLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(platformProviderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        platformProviderPanelHolderLayout.setVerticalGroup(platformProviderPanelHolderLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(platformProviderPanelHolderLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(platformProviderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        Mnemonics.setLocalizedText(runBrowserCheckBox, NbBundle.getMessage(RunPanel.class, "RunPanel.runBrowserCheckBox.text")); // NOI18N

        GroupLayout runBrowserHolderLayout = new GroupLayout(runBrowserHolder);
        runBrowserHolder.setLayout(runBrowserHolderLayout);
        runBrowserHolderLayout.setHorizontalGroup(runBrowserHolderLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(runBrowserHolderLayout.createSequentialGroup()
                .addComponent(runBrowserCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        runBrowserHolderLayout.setVerticalGroup(runBrowserHolderLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(runBrowserHolderLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(runBrowserCheckBox)
                .addGap(0, 0, 0))
        );

        browserPanel.setLayout(new BorderLayout());

        GroupLayout browserPanelHolderLayout = new GroupLayout(browserPanelHolder);
        browserPanelHolder.setLayout(browserPanelHolderLayout);
        browserPanelHolderLayout.setHorizontalGroup(browserPanelHolderLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(browserPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        browserPanelHolderLayout.setVerticalGroup(browserPanelHolderLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(browserPanelHolderLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(browserPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(platformProviderPanelHolder, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(runAsHolder, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(runBrowserHolder, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(browserPanelHolder, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(runAsHolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(platformProviderPanelHolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(runBrowserHolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browserPanelHolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel browserPanel;
    private JPanel browserPanelHolder;
    private JPanel platformProviderPanel;
    private JPanel platformProviderPanelHolder;
    private JComboBox<CustomizerPanel> runAsComboBox;
    private JPanel runAsHolder;
    private JLabel runAsLabel;
    private JCheckBox runBrowserCheckBox;
    private JPanel runBrowserHolder;
    // End of variables declaration//GEN-END:variables

    @Override
    public void stateChanged(ChangeEvent e) {
        // change in panels
        validateAndStoreData();
    }

    //~ Inner classes

    private static final class RunAsRenderer implements ListCellRenderer<Object> {

        private final DefaultListCellRenderer renderer = new DefaultListCellRenderer();


        @NbBundle.Messages("RunAsRenderer.default=Web Application")
        @Override
        public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            String label;
            if (value == null) {
                label = Bundle.RunAsRenderer_default();
            } else if (value instanceof String) {
                // see BasicComboBoxUI
                label = (String) value;
            } else {
                assert value instanceof CustomizerPanel : value.getClass().getName();
                label = ((CustomizerPanel) value).getDisplayName();
            }
            return renderer.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
        }

    }

}
