/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.web.project.ui.customizer;
import java.text.MessageFormat;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectServerWizardPanel;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.ExtenderController.Properties;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebFrameworks;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Task;
import org.openide.util.TaskListener;

public class CustomizerFrameworks extends javax.swing.JPanel implements HelpCtx.Provider, ListSelectionListener {
    
    private final ProjectCustomizer.Category category;
    private WebProject project;
    private WebProjectProperties uiProperties;
    private List<WebModuleExtender> newExtenders = new LinkedList<WebModuleExtender>();
    private List<WebFrameworkProvider> usedFrameworks = new LinkedList<WebFrameworkProvider>();
    private List<String> addedFrameworks = new LinkedList<String>();
    private Map<WebFrameworkProvider, WebModuleExtender> extenders = new IdentityHashMap<WebFrameworkProvider, WebModuleExtender>();
    private ExtenderController controller = ExtenderController.create();
    private boolean initialized = false;
    
    /** Creates new form CustomizerFrameworks */
    public CustomizerFrameworks(ProjectCustomizer.Category category, WebProjectProperties uiProperties) {
        this.category = category;
        this.uiProperties = uiProperties;
        initComponents();
        
        project = uiProperties.getProject();
        jListFrameworks.setModel(new DefaultListModel());
        ((DefaultListModel) jListFrameworks.getModel()).addElement(NbBundle.getMessage(CustomizerFrameworks.class, "LBL_CustomizerFrameworks_Loading"));
        // do not load frameworks again but use list from uiProperties; list is being loaded in background thread:
        uiProperties.getLoadingFrameworksTask().addTaskListener(new TaskListener() {
            public void taskFinished(Task task) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        initFrameworksList(project.getAPIWebModule());
                    }
                });
            }
        });
        if (uiProperties.getLoadingFrameworksTask().isFinished()) {
            initFrameworksList(project.getAPIWebModule());
        }
    }
    
    private void initFrameworksList(WebModule webModule) {
        if (initialized) {
            return;
        }
        initialized = true;
        Profile j2eeProfile = Profile.fromPropertiesString(uiProperties.getProject().evaluator().getProperty(WebProjectProperties.J2EE_PLATFORM));
        if (j2eeProfile == null) {
            j2eeProfile = Profile.JAVA_EE_6_WEB;
        }
        String serverInstanceID = uiProperties.getProject().evaluator().getProperty(WebProjectProperties.J2EE_SERVER_INSTANCE);
        Properties properties = controller.getProperties();

        // FIXME I left string here for compatibility reasons (frameworks)
        properties.setProperty(ProjectServerWizardPanel.J2EE_LEVEL, j2eeProfile.toPropertiesString()); // NOI18N
        properties.setProperty("serverInstanceID", serverInstanceID); // NOI18N
        
        jListFrameworks.setModel(new DefaultListModel());
        List<WebModuleExtender> usedExtenders = new LinkedList<WebModuleExtender>();
        if (uiProperties.getCurrentFrameworks() != null) {
            for (WebFrameworkProvider framework : uiProperties.getCurrentFrameworks()) {
                usedFrameworks.add(framework);
                ((DefaultListModel) jListFrameworks.getModel()).addElement(framework.getName());
                WebModuleExtender extender = framework.createWebModuleExtender(webModule, controller);
                extenders.put(framework, extender);
                usedExtenders.add(extender);
                extender.addChangeListener(new ExtenderListener(extender));
            }
        }
        jListFrameworks.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jListFrameworks.addListSelectionListener(this);
        if (usedFrameworks.size() > 0)
            jListFrameworks.setSelectedIndex(0);
        
        if (WebFrameworks.getFrameworks().size() == jListFrameworks.getModel().getSize())
            jButtonAdd.setEnabled(false);

        uiProperties.setExistingExtenders(usedExtenders);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelFrameworks = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListFrameworks = new javax.swing.JList();
        jButtonAdd = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanelConfig = new javax.swing.JPanel();
        jLabelConfig = new javax.swing.JLabel();

        jLabelFrameworks.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "LBL_CustomizerFrameworks_ListMnemonic").charAt(0));
        jLabelFrameworks.setLabelFor(jListFrameworks);
        jLabelFrameworks.setText(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "LBL_UsedFrameworks")); // NOI18N

        jScrollPane1.setViewportView(jListFrameworks);
        jListFrameworks.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "ACS_Frameworks_FrameworksList_A11YDesc")); // NOI18N

        jButtonAdd.setMnemonic(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "LBL_CustomizerFrameworks_AddButton_LabelMnemonic").charAt(0));
        jButtonAdd.setText(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "LBL_AddFramework")); // NOI18N
        jButtonAdd.setActionCommand("Add...");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jPanelConfig.setLayout(new java.awt.GridBagLayout());

        jLabelConfig.setLabelFor(jPanelConfig);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonAdd))
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
            .addComponent(jPanelConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelFrameworks)
                    .addComponent(jLabelConfig))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabelFrameworks)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonAdd)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelConfig)
                .addGap(18, 18, 18)
                .addComponent(jPanelConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE))
        );

        jButtonAdd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerFrameworks.class, "ACS_Frameworks_AddButton_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
        AddFrameworkPanel panel = new AddFrameworkPanel(usedFrameworks);
        javax.swing.JPanel inner = new javax.swing.JPanel();
        inner.setLayout(new java.awt.GridBagLayout());
        inner.getAccessibleContext().setAccessibleDescription(panel.getAccessibleContext().getAccessibleDescription());
        inner.getAccessibleContext().setAccessibleName(panel.getAccessibleContext().getAccessibleName());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        inner.add(panel, gridBagConstraints);
 
        DialogDescriptor desc = new DialogDescriptor(inner, NbBundle.getMessage(CustomizerFrameworks.class, "LBL_SelectWebExtension_DialogTitle")); //NOI18N
        Object res = DialogDisplayer.getDefault().notify(desc);
        if (res.equals(NotifyDescriptor.YES_OPTION)) {
            List newFrameworks = panel.getSelectedFrameworks();
            WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
            for(int i = 0; i < newFrameworks.size(); i++) {
                WebFrameworkProvider framework = (WebFrameworkProvider) newFrameworks.get(i);
                if (!((DefaultListModel) jListFrameworks.getModel()).contains(framework.getName()))
                    ((DefaultListModel) jListFrameworks.getModel()).addElement(framework.getName());

                boolean added = false;
                if (usedFrameworks.size() == 0) {
                    usedFrameworks.add(framework);
                    added = true;
                }
                else
                    for (int j = 0; j < usedFrameworks.size(); j++)
                        if (!((WebFrameworkProvider) usedFrameworks.get(j)).getName().equals(framework.getName())) {
                            usedFrameworks.add(framework);
                            added = true;
                            break;
                        }
                
                if (added) {
                    WebModuleExtender extender = framework.createWebModuleExtender(wm, controller);
                    if (extender != null) {
                        extenders.put(framework, extender);
                        newExtenders.add(extender);
                        extender.addChangeListener(new ExtenderListener(extender));
                        addedFrameworks.add(framework.getName());
                    }
                }

                jListFrameworks.setSelectedValue(framework.getName(), true);
            }
            
            uiProperties.setNewExtenders(newExtenders);
            uiProperties.setNewFrameworksNames(addedFrameworks);
        }
        
        if (WebFrameworks.getFrameworks().size() == jListFrameworks.getModel().getSize())
            jButtonAdd.setEnabled(false);
    }//GEN-LAST:event_jButtonAddActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JLabel jLabelConfig;
    private javax.swing.JLabel jLabelFrameworks;
    private javax.swing.JList jListFrameworks;
    private javax.swing.JPanel jPanelConfig;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerFrameworks.class);
    }

    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        String frameworkName = (String) jListFrameworks.getSelectedValue();
	int selectedIndex = jListFrameworks.getSelectedIndex();
	if (selectedIndex != -1 && selectedIndex < usedFrameworks.size()) {	
	    WebFrameworkProvider framework = (WebFrameworkProvider) usedFrameworks.get(selectedIndex);
	    if (framework.getName().equals(frameworkName)) {
                WebModuleExtender extender = extenders.get(framework);
		if (extender != null) {
		    String message = MessageFormat.format(NbBundle.getMessage(CustomizerFrameworks.class, "LBL_FrameworkConfiguration"), new Object[] {frameworkName}); //NOI18N
		    jLabelConfig.setText(message);
		    jPanelConfig.removeAll();

		    java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		    gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
		    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		    gridBagConstraints.weightx = 1.0;
		    gridBagConstraints.weighty = 1.0;

		    jPanelConfig.add(extender.getComponent(), gridBagConstraints);
                    jPanelConfig.repaint();
		    jPanelConfig.revalidate();
		} else {
		    hideConfigPanel();
		}
            }
	} else
	    hideConfigPanel();
    }
    
    // #109426
    private final class ExtenderListener implements ChangeListener {
    
        private final WebModuleExtender extender;
        
        public ExtenderListener(WebModuleExtender extender) {
            this.extender = extender;
            extender.update();
            stateChanged(new ChangeEvent(this));
        }

        public void stateChanged(ChangeEvent e) {
            controller.setErrorMessage(null);
            if (extender.isValid()) {
                if (!category.isValid()) {
                    category.setValid(true);
                    category.setErrorMessage(null);
                }
            } else {
                category.setValid(false);
                category.setErrorMessage(controller.getErrorMessage());
            }
        }
    }

    private void hideConfigPanel() {
	jLabelConfig.setText(""); //NOI18N
	jPanelConfig.removeAll();
	jPanelConfig.repaint();
	jPanelConfig.revalidate();
    }
}
