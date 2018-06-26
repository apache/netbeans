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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.core.dev.wizard;

import java.awt.Dialog;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.WSStackUtils;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  radko
 */
public class WebServiceTypePanel extends javax.swing.JPanel implements HelpCtx.Provider, ItemListener {
    
    private Project project;
    private Node[] sessionBeanNodes;
    
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    private boolean jsr109Supported;
    private boolean jsr109oldSupported;
    private boolean isWebModule;
    WSStackUtils stackUtils;
    
    /** Creates new form WebServiceTypePanel */
    public WebServiceTypePanel(Project project) {
        this.project = project;
        
        initComponents();
        
        stackUtils = new WSStackUtils(project);
        jsr109Supported = stackUtils.isJsr109Supported();
        jsr109oldSupported = stackUtils.isJsr109OldSupported();
        
        //convert Java class not implemented for 5.5 release, disable components
        jRadioButtonConvert.setEnabled(false);
        jLabelConvert.setEnabled(false);
        jTextFieldConvert.setEnabled(false);
        jButtonConvert.setEnabled(false);
        jRadioButtonConvert.setVisible(false);
        jLabelConvert.setVisible(false);
        jTextFieldConvert.setVisible(false);
        jButtonConvert.setVisible(false);

        if (JaxWsUtils.isEjbJavaEE5orHigher(project)) {
            sessionBeanCB.setSelected(true);
            sessionBeanCB.setEnabled(false);
        } else if (isEjbInWebSupported(project)) {
            sessionBeanCB.setEnabled(true);
        } else {
            sessionBeanCB.setEnabled(false);
        }
        
        //disable encapsulate session bean for j2se project
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            isWebModule = J2eeModule.Type.WAR.equals(j2eeModuleProvider.getJ2eeModule().getType());
        }
                if ( (j2eeModuleProvider == null) ||
                //disable encapsulate session beans for Tomcat
                (!jsr109Supported && !jsr109oldSupported) ) {
            disableDelegateToEJB();
        }
        
        addItemListener(this);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jRadioButtonScratch = new javax.swing.JRadioButton();
        jRadioButtonDelegate = new javax.swing.JRadioButton();
        jLabelDelegate = new javax.swing.JLabel();
        jTextFieldDelegate = new javax.swing.JTextField();
        jButtonDelegate = new javax.swing.JButton();
        jRadioButtonConvert = new javax.swing.JRadioButton();
        jLabelConvert = new javax.swing.JLabel();
        jTextFieldConvert = new javax.swing.JTextField();
        jButtonConvert = new javax.swing.JButton();
        sessionBeanCB = new javax.swing.JCheckBox();

        buttonGroup1.add(jRadioButtonScratch);
        jRadioButtonScratch.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonScratch, org.openide.util.NbBundle.getMessage(WebServiceTypePanel.class, "LBL_EmptyWebService")); // NOI18N
        jRadioButtonScratch.setToolTipText(org.openide.util.NbBundle.getMessage(WebServiceTypePanel.class, "HINT_EmptyWebService")); // NOI18N
        jRadioButtonScratch.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        buttonGroup1.add(jRadioButtonDelegate);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonDelegate, org.openide.util.NbBundle.getMessage(WebServiceTypePanel.class, "LBL_EncapsulateSessionBean")); // NOI18N
        jRadioButtonDelegate.setToolTipText(org.openide.util.NbBundle.getMessage(WebServiceTypePanel.class, "HINT_EnterpriseBean")); // NOI18N
        jRadioButtonDelegate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        jLabelDelegate.setLabelFor(jTextFieldDelegate);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelDelegate, org.openide.util.NbBundle.getMessage(WebServiceTypePanel.class, "LBL_EnterpriseBean")); // NOI18N
        jLabelDelegate.setToolTipText(org.openide.util.NbBundle.getMessage(WebServiceTypePanel.class, "HINT_EnterpriseBean")); // NOI18N

        jTextFieldDelegate.setEditable(false);
        jTextFieldDelegate.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonDelegate, org.openide.util.NbBundle.getMessage(WebServiceTypePanel.class, "LBL_Browse")); // NOI18N
        jButtonDelegate.setToolTipText(org.openide.util.NbBundle.getMessage(WebServiceTypePanel.class, "HINT_BrowseBean")); // NOI18N
        jButtonDelegate.setEnabled(false);
        jButtonDelegate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDelegateActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonConvert);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonConvert, org.openide.util.NbBundle.getMessage(WebServiceTypePanel.class, "LBL_ConvertJavaClass")); // NOI18N
        jRadioButtonConvert.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        jLabelConvert.setLabelFor(jTextFieldConvert);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelConvert, org.openide.util.NbBundle.getMessage(WebServiceTypePanel.class, "LBL_JavaClass")); // NOI18N

        jTextFieldConvert.setEditable(false);
        jTextFieldConvert.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonConvert, org.openide.util.NbBundle.getMessage(WebServiceTypePanel.class, "LBL_Browse")); // NOI18N
        jButtonConvert.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(sessionBeanCB, org.openide.util.NbBundle.getMessage(WebServiceTypePanel.class, "LBL_WsAsSessionBean")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jRadioButtonScratch)
                .addContainerGap(330, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jRadioButtonDelegate)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelConvert)
                    .addComponent(jLabelDelegate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextFieldConvert)
                    .addComponent(jTextFieldDelegate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonConvert)
                    .addComponent(jButtonDelegate)))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jRadioButtonConvert, 0, 578, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(sessionBeanCB)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabelConvert, jLabelDelegate});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jRadioButtonScratch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonDelegate, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonDelegate)
                    .addComponent(jLabelDelegate)
                    .addComponent(jTextFieldDelegate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonConvert)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelConvert)
                    .addComponent(jButtonConvert)
                    .addComponent(jTextFieldConvert, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(sessionBeanCB)
                .addContainerGap(145, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButtonDelegateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDelegateActionPerformed
        Project[] allProjects = getCallableEjbProjects(project);
        List<Node> ejbProjectNodes = new LinkedList<Node>();
        
        for (int i = 0; i < allProjects.length; i++) {
            LogicalViewProvider lvp = allProjects[i].getLookup().lookup(LogicalViewProvider.class);
            Node projectView = lvp.createLogicalView();
            ejbProjectNodes.add(new FilterNode(projectView, new EJBListViewChildren(allProjects[i])) {
                @Override
                public Action[] getActions(boolean context) {
                    return new Action[0];
                }
            });
        }
        
        Children.Array children = new Children.Array();
        children.add(ejbProjectNodes.<Node>toArray(new Node[ejbProjectNodes.size()]));
        Node root = new AbstractNode(children);
        EjbChooser chooser = new EjbChooser(root, J2eeProjectCapabilities.forProject(project).isEjb31LiteSupported());
        final DialogDescriptor dd = new DialogDescriptor(chooser, org.openide.util.NbBundle.getMessage(WebServiceTypePanel.class, "LBL_BrowseBean_Title"));
        
        dd.setValid(false);
        chooser.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(EjbChooser.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        dd.setValid(((Boolean) newvalue).booleanValue());
                    }
                }
            }
        });
        
        //Object result = DialogDisplayer.getDefault().notify(dd);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        dlg.setVisible(true);
 
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            jTextFieldDelegate.setText(chooser.getSelectedEJBProjectName() + "#" + chooser.getSelectedNodes()[0].getDisplayName());
            sessionBeanNodes = chooser.getSelectedNodes();
            fireChange();
        }
    }//GEN-LAST:event_jButtonDelegateActionPerformed
    
    void validate(WizardDescriptor wizardDescriptor) {
    }
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        
        if (getServiceType() == WizardProperties.ENCAPSULATE_SESSION_BEAN &&
            jTextFieldDelegate.getText().length() == 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, NbBundle.getMessage(WebServiceTypePanel.class, "LBL_SelectOneEJB")); //NOI18N
            return false;        
        }

        WSStackUtils.ErrorMessage message = stackUtils.getErrorMessage(WSStackUtils.WizardType.WS);
        if (message != null) {
            wizardDescriptor.putProperty(message.getWizardMessageProperty(), message.getText());
            if (message.isSerious()) {
                return false;
            }
        }

        return true;
    }  
    
    void store(WizardDescriptor d) {
        d.putProperty(WizardProperties.WEB_SERVICE_TYPE, Integer.valueOf(getServiceType()));
        if (getServiceType() == WizardProperties.ENCAPSULATE_SESSION_BEAN)
            d.putProperty(WizardProperties.DELEGATE_TO_SESSION_BEAN, sessionBeanNodes);
        d.putProperty(WizardProperties.IS_STATELESS_BEAN, Boolean.valueOf(sessionBeanCB.isSelected()));
    }
    
    void read(WizardDescriptor wizardDescriptor) {
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(WebServiceTypePanel.class);
    }
    
    public void itemStateChanged(ItemEvent e) {
        Object src = e.getSource();
        if (src.equals(jRadioButtonScratch)) {
            jButtonDelegate.setEnabled(false);
            jTextFieldDelegate.setEnabled(false);
//convert Java class not implemented for 5.5 release
//            jButtonConvert.setEnabled(false);
//            jTextFieldConvert.setEnabled(false);
        } else if (src.equals(jRadioButtonDelegate)) {
            jButtonDelegate.setEnabled(true);
            jTextFieldDelegate.setEnabled(true);
//convert Java class not implemented for 5.5 release
//            jButtonConvert.setEnabled(false);
//            jTextFieldConvert.setEnabled(false);
        }
//convert Java class not implemented for 5.5 release
//        else if (src.equals(jRadioButtonConvert)) {
//            jButtonDelegate.setEnabled(false);
//            jTextFieldDelegate.setEnabled(false);
//            jButtonConvert.setEnabled(true);
//            jTextFieldConvert.setEnabled(true);
//        }
        
        fireChange();
    }
    
    public void addItemListener(ItemListener l) {
        jRadioButtonScratch.addItemListener(l);
        jRadioButtonDelegate.addItemListener(l);
//convert Java class not implemented for 5.5 release
//        jRadioButtonConvert.addItemListener(l);
    }
    
    public void removeItemListener(ItemListener l) {
        jRadioButtonScratch.removeItemListener(l);
        jRadioButtonDelegate.removeItemListener(l);
//convert Java class not implemented for 5.5 release
//        jRadioButtonConvert.removeItemListener(l);
    }
    
    public int getServiceType() {
        if (jRadioButtonScratch.isSelected())
            return WizardProperties.FROM_SCRATCH;
        else
//        else if (jRadioButtonDelegate.isSelected())
            return WizardProperties.ENCAPSULATE_SESSION_BEAN;
//convert Java class not implemented for 5.5 release
//        else
//            return NewWebServiceWizardIterator.CONVERT_JAVA_CLASS;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonConvert;
    private javax.swing.JButton jButtonDelegate;
    private javax.swing.JLabel jLabelConvert;
    private javax.swing.JLabel jLabelDelegate;
    private javax.swing.JRadioButton jRadioButtonConvert;
    private javax.swing.JRadioButton jRadioButtonDelegate;
    private javax.swing.JRadioButton jRadioButtonScratch;
    private javax.swing.JTextField jTextFieldConvert;
    private javax.swing.JTextField jTextFieldDelegate;
    private javax.swing.JCheckBox sessionBeanCB;
    // End of variables declaration//GEN-END:variables
    
    private void disableDelegateToEJB(){
        jRadioButtonDelegate.setEnabled(false);
        jLabelDelegate.setEnabled(false);
        jTextFieldDelegate.setEnabled(false);
        jButtonDelegate.setEnabled(false);
    }
    
    /** Returns list of all EJB projects that can be called from the caller project.
     *
     * @param enterpriseProject the caller enterprise project
     */
    private Project [] getCallableEjbProjects(Project enterpriseProject) {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        
        boolean isCallerEJBModule = false;
        J2eeModuleProvider callerJ2eeModuleProvider = (J2eeModuleProvider) enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);
        if (callerJ2eeModuleProvider != null && callerJ2eeModuleProvider.getJ2eeModule().getType().equals(J2eeModule.Type.EJB)) {
            // TODO: HACK - this should be set by calling AntArtifactQuery.findArtifactsByType(p, EjbProjectConstants.ARTIFACT_TYPE_EJBJAR)
            // but now freeform doesn't implement this correctly
            isCallerEJBModule = true;
        }
        // TODO: HACK - this must be solved by freeform's own implementation of EnterpriseReferenceContainer, see issue 57003
        // call ejb should not make this check, all should be handled in EnterpriseReferenceContainer
        boolean isCallerFreeform = enterpriseProject.getClass().getName().equals("org.netbeans.modules.ant.freeform.FreeformProject");
        
        List<Project> filteredResults = new ArrayList<Project>(allProjects.length);
        for (int i = 0; i < allProjects.length; i++) {
            boolean isEJBModule = false;
            J2eeModuleProvider j2eeModuleProvider = allProjects[i].getLookup().lookup(J2eeModuleProvider.class);
            EjbJarProvider ejbJarProvider = allProjects[i].getLookup().lookup(EjbJarProvider.class);
            if (j2eeModuleProvider != null && ejbJarProvider != null) {
                isEJBModule = true;
            }
            if ((isEJBModule && !isCallerFreeform) ||
                    (isCallerFreeform && enterpriseProject.equals(allProjects[i]))) {
                filteredResults.add(allProjects[i]);
            }
        }
        return filteredResults.<Project>toArray(new Project[filteredResults.size()]);
    }
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator<ChangeListener> it = listeners.iterator();
        while (it.hasNext()) {
            it.next().stateChanged(e);
        }
    }

    private static boolean isEjbInWebSupported(Project prj) {
        if (prj== null) {
            throw new IllegalArgumentException("Passed null to Util.isEjbInWebSupported(Project prj)");
        }
        J2eeModuleProvider j2eeModuleProvider = prj.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            J2eeModule j2eeModule = j2eeModuleProvider.getJ2eeModule();
            if (j2eeModule != null) {
                J2eeModule.Type type = j2eeModule.getType();
                String moduleVersion = j2eeModule.getModuleVersion();
                if (moduleVersion != null) {
                    double version = Double.parseDouble(moduleVersion);
                    if (J2eeModule.Type.WAR.equals(type) && (version >= 3.0)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
}
