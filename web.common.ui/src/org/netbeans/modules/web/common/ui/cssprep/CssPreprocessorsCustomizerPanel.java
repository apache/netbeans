/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
