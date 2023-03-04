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
package org.netbeans.modules.web.common.ui.cssprep;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorUI;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorsUI;
import org.netbeans.modules.web.common.ui.spi.CssPreprocessorUIImplementation;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * UI of CSS preprocessors for project customizer.
 */
public final class CssPreprocessorsCustomizerPanel extends JPanel implements ChangeListener, HelpCtx.Provider {

    private static final long serialVersionUID = -364546871657687310L;

    private static final Logger LOGGER = Logger.getLogger(CssPreprocessorsCustomizerPanel.class.getName());

    private final ProjectCustomizer.Category category;
    private final Project project;
    private final List<CssPreprocessorUIImplementation.Customizer> customizers = new CopyOnWriteArrayList<>();
    private final Map<Component, CssPreprocessorUIImplementation.Customizer> componentCustomizers = new ConcurrentHashMap<>();


    public CssPreprocessorsCustomizerPanel(ProjectCustomizer.Category category, Project project) {
        assert category != null;
        assert project != null;

        this.category = category;
        this.project = project;

        customizers.addAll(getCustomizers());

        initComponents();
        init();
        validateCustomizers();
    }

    private void init() {
        // tabs
        for (CssPreprocessorUIImplementation.Customizer customizer : customizers) {
            assert customizer != null;
            customizer.addChangeListener(this);
            JComponent component = customizer.getComponent();
            mainTabbedPane.addTab(customizer.getDisplayName(), component);
            componentCustomizers.put(component, customizer);
        }
        if (!customizers.isEmpty()) {
            mainTabbedPane.setSelectedIndex(0);
        }
        // store
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                store();
            }
        });
        // close
        category.setCloseListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeListeners();
            }
        });
    }

    private Collection<CssPreprocessorUIImplementation.Customizer> getCustomizers() {
        List<CssPreprocessorUI> preprocessors = CssPreprocessorsAccessor.getDefault().getPreprocessors();
        List<CssPreprocessorUIImplementation.Customizer> result = new ArrayList<>(preprocessors.size());
        for (CssPreprocessorUI cssPreprocessor : preprocessors) {
            CssPreprocessorUIImplementation.Customizer customizer = CssPreprocessorAccessor.getDefault().createCustomizer(cssPreprocessor, project);
            if (customizer != null) {
                result.add(customizer);
            }
        }
        return result;
    }

    @NbBundle.Messages({
        "# {0} - customizer name",
        "# {1} - message",
        "CssPreprocessorsCustomizerPanel.message={0}: {1}",
    })
    void validateCustomizers() {
        String message = null; // NOI18N
        for (CssPreprocessorUIImplementation.Customizer customizer : customizers) {
            if (!customizer.isValid()) {
                String errorMessage = customizer.getErrorMessage();
                Parameters.notNull("errorMessage", errorMessage); // NOI18N
                category.setErrorMessage(Bundle.CssPreprocessorsCustomizerPanel_message(customizer.getDisplayName(), errorMessage));
                category.setValid(false);
                return;
            }
            String warning = customizer.getWarningMessage();
            if (message == null
                    && warning != null) {
                message = Bundle.CssPreprocessorsCustomizerPanel_message(customizer.getDisplayName(), warning);
            }
        }
        category.setErrorMessage(message != null ? message : " "); // NOI18N
        category.setValid(true);
    }

    void store() {
        for (CssPreprocessorUIImplementation.Customizer customizer : customizers) {
            assert customizer.isValid() : "Saving invalid customizer: " + customizer.getDisplayName() + " (error: " + customizer.getErrorMessage() + ")";
            try {
                customizer.save();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Error while saving CSS preprocessor: " + customizer.getDisplayName(), ex);
            }
        }
    }

    void removeListeners() {
        for (CssPreprocessorUIImplementation.Customizer customizer : customizers) {
            customizer.removeChangeListener(this);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        validateCustomizers();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configureButton = new JButton();
        mainTabbedPane = new JTabbedPane();

        Mnemonics.setLocalizedText(configureButton, NbBundle.getMessage(CssPreprocessorsCustomizerPanel.class, "CssPreprocessorsCustomizerPanel.configureButton.text")); // NOI18N
        configureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configureButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(mainTabbedPane, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(configureButton))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(configureButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainTabbedPane, GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void configureButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        OptionsDisplayer.getDefault().open(CssPreprocessorsUI.OPTIONS_PATH);
    }//GEN-LAST:event_configureButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton configureButton;
    private JTabbedPane mainTabbedPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        HelpCtx generalHelp = new HelpCtx("org.netbeans.modules.web.common.api.ui.CssPreprocessorsCustomizerPanel"); // NOI18N
        Component selectedComponent = mainTabbedPane.getSelectedComponent();
        // #235917
        if (selectedComponent == null) {
            return generalHelp;
        }
        CssPreprocessorUIImplementation.Customizer customizer = componentCustomizers.get(selectedComponent);
        assert customizer != null : "Unknown tab: " + mainTabbedPane.getSelectedIndex();
        HelpCtx help = customizer.getHelp();
        if (help != null) {
            return help;
        }
        return generalHelp;
    }

}
