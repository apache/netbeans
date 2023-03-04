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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.ddloaders.web.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Utils;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.dd.api.common.RunAs;
import org.openide.util.NbBundle;

/**
 * @author  mkuchtiak
 */
public class ServletPanel extends SectionInnerPanel implements java.awt.event.ActionListener {
    private DDDataObject dObj;
    private Servlet servlet;
    private javax.swing.JButton linkServletClass, linkJspFile;
    private InitParamsPanel initParamsPanel;
    private JComboBox runAsCB;
    
    /** Creates new form ServletPanel */
    public ServletPanel(SectionView sectionView, DDDataObject dObj,Servlet servlet) {
        super(sectionView);
        this.dObj=dObj;
        this.servlet=servlet;
        initComponents();
        // Servlet Name
        servletNameTF.setText(servlet.getServletName());
        addValidatee(servletNameTF);
        
        // description
        Utils.makeTextAreaLikeTextField(descriptionTA,servletNameTF);
        descriptionTA.setText(servlet.getDefaultDescription());
        addModifier(descriptionTA);
        
        // Order
        java.math.BigInteger los = servlet.getLoadOnStartup();
        orderTF.setText(los==null?"":los.toString());
        addValidatee(orderTF);
        
        // servlet-class/jsp-file
        String jspFile = servlet.getJspFile();
        if (jspFile!=null) {
            jspFileRB.setSelected(true);
            servletClassTF.setEnabled(false);
            browseButton.setEnabled(false);
            jspFileTF.setText(jspFile);
        } else {
            servletClassRB.setSelected(true);
            jspFileTF.setEnabled(false);
            browseButton1.setEnabled(false);
            servletClassTF.setText(servlet.getServletClass());
        }
        servletClassRB.addActionListener(this);
        jspFileRB.addActionListener(this);
        addValidatee(servletClassTF);
        addValidatee(jspFileTF);
        
        
        // URL Patterns
        String[] urlPatterns = DDUtils.getUrlPatterns(dObj.getWebApp(),servlet);
        servletMappingsTF.setText(DDUtils.urlPatternList(urlPatterns));
        addValidatee(servletMappingsTF);
        
        // Init Params
        InitParamTableModel model = new InitParamTableModel();
        initParamsPanel = new InitParamsPanel(dObj, model);
        initParamsPanel.setModel(servlet,servlet.getInitParam());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 0);
        //gridBagConstraints.weightx = 1.0;
        //gridBagConstraints.weighty = 5.0;
        add(initParamsPanel, gridBagConstraints);
        
        JLabel securityRoleRefsLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(securityRoleRefsLabel,
                org.openide.util.NbBundle.getMessage(ServletPanel.class, "LBL_SecurityRoleRefs"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 0, 0);
        add(securityRoleRefsLabel, gridBagConstraints);
        
        // Security Role Refs
        SecurityRoleRefTableModel roleRefModel = new SecurityRoleRefTableModel();
        SecurityRoleRefTablePanel roleRefPanel = new SecurityRoleRefTablePanel(dObj, roleRefModel);
        roleRefPanel.setModel(dObj.getWebApp(), servlet, servlet.getSecurityRoleRef());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 0);
        add(roleRefPanel, gridBagConstraints);
        
        RunAsPanel runAsPanel = new RunAsPanel(dObj.getWebApp());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 0);
        add(runAsPanel, gridBagConstraints);
        runAsCB = runAsPanel.getRunAsCB();
        
        RunAs runAs = servlet.getRunAs();
        if (runAs == null) {
            runAsCB.setSelectedItem("");    //NOI18N
        } else {
            runAsCB.setSelectedItem(runAs.getRoleName());
        }
        addModifier(runAsCB);
        
        linkServletClass = new LinkButton(this, servlet, "ClassName"); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        org.openide.awt.Mnemonics.setLocalizedText(linkServletClass,
                NbBundle.getMessage(ServletPanel.class, "LBL_goToSource"));
        add(linkServletClass, gridBagConstraints);
        
        linkJspFile = new LinkButton(this, servlet, "JspFile"); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        org.openide.awt.Mnemonics.setLocalizedText(linkJspFile,
                NbBundle.getMessage(ServletPanel.class, "LBL_goToSource_2"));
        add(linkJspFile, gridBagConstraints);
        
        setAccessibility();
    }
    
    private void setAccessibility() {
        initParamsLabel.setLabelFor(initParamsPanel.getTable());
    }
    
    public void linkButtonPressed(Object ddBean, String property) {
        if ("ClassName".equals(property)) { //NOI18N
            DDUtils.openEditorFor(dObj,((Servlet)ddBean).getServletClass());
        } else if ("JspFile".equals(property)) { //NOI18N
            DDUtils.openEditorForSingleFile(dObj,((Servlet)ddBean).getJspFile());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        servletNameLabel = new javax.swing.JLabel();
        servletNameTF = new javax.swing.JTextField();
        orderLabel = new javax.swing.JLabel();
        orderTF = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionTA = new javax.swing.JTextArea();
        servletClassRB = new javax.swing.JRadioButton();
        servletClassTF = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jspFileRB = new javax.swing.JRadioButton();
        jspFileTF = new javax.swing.JTextField();
        browseButton1 = new javax.swing.JButton();
        servletMappingLabel = new javax.swing.JLabel();
        servletMappingsTF = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        initParamsLabel = new javax.swing.JLabel();
        filler = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        servletNameLabel.setLabelFor(servletNameTF);
        org.openide.awt.Mnemonics.setLocalizedText(servletNameLabel, org.openide.util.NbBundle.getMessage(ServletPanel.class, "LBL_servletName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(servletNameLabel, gridBagConstraints);

        servletNameTF.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(servletNameTF, gridBagConstraints);

        orderLabel.setLabelFor(orderTF);
        org.openide.awt.Mnemonics.setLocalizedText(orderLabel, org.openide.util.NbBundle.getMessage(ServletPanel.class, "LBL_order")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        add(orderLabel, gridBagConstraints);

        orderTF.setColumns(2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(orderTF, gridBagConstraints);

        descriptionLabel.setLabelFor(descriptionTA);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(ServletPanel.class, "LBL_description")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(descriptionLabel, gridBagConstraints);

        descriptionTA.setRows(3);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(descriptionTA, gridBagConstraints);

        buttonGroup1.add(servletClassRB);
        org.openide.awt.Mnemonics.setLocalizedText(servletClassRB, org.openide.util.NbBundle.getMessage(ServletPanel.class, "LBL_servletClass")); // NOI18N
        servletClassRB.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        servletClassRB.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 10);
        add(servletClassRB, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(servletClassTF, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ServletPanel.class, "LBL_browse")); // NOI18N
        browseButton.setMargin(new java.awt.Insets(0, 14, 0, 14));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 0, 0);
        add(browseButton, gridBagConstraints);

        buttonGroup1.add(jspFileRB);
        org.openide.awt.Mnemonics.setLocalizedText(jspFileRB, org.openide.util.NbBundle.getMessage(ServletPanel.class, "LBL_jspFile")); // NOI18N
        jspFileRB.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jspFileRB.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 10);
        add(jspFileRB, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jspFileTF, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton1, org.openide.util.NbBundle.getMessage(ServletPanel.class, "LBL_browse_2")); // NOI18N
        browseButton1.setMargin(new java.awt.Insets(0, 14, 0, 14));
        browseButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 0, 0);
        add(browseButton1, gridBagConstraints);

        servletMappingLabel.setLabelFor(servletMappingsTF);
        org.openide.awt.Mnemonics.setLocalizedText(servletMappingLabel, org.openide.util.NbBundle.getMessage(ServletPanel.class, "LBL_urlPatterns")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(servletMappingLabel, gridBagConstraints);

        servletMappingsTF.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(servletMappingsTF, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ServletPanel.class, "HINT_urlPatterns")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(initParamsLabel, org.openide.util.NbBundle.getMessage(ServletPanel.class, "LBL_initParams")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 0, 0);
        add(initParamsLabel, gridBagConstraints);

        filler.setBackground(new java.awt.Color(255, 255, 255));
        filler.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(filler, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void browseButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButton1ActionPerformed
        try {
            org.netbeans.api.project.SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
            org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
            if (fo!=null) {
                String res = "/"+DDUtils.getResourcePath(groups,fo,'/',true);
                if (!res.equals(jspFileTF.getText())) {
                    dObj.modelUpdatedFromUI();
                    jspFileTF.setText(res);
                    dObj.setChangedFromUI(true);
                    servlet.setJspFile(res);
                    dObj.setChangedFromUI(false);
                    getSectionView().checkValidity();
                }
            }
        } catch (java.io.IOException ex) {}
    }//GEN-LAST:event_browseButton1ActionPerformed
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        try {
            SourceGroup[] groups = DDUtils.getJavaSourceGroups(dObj);
            org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
            if (fo!=null) {
                String className = DDUtils.getResourcePath(groups,fo);
                if (className.length()>0 && !className.equals(servletClassTF.getText())) {
                    dObj.modelUpdatedFromUI();
                    servletClassTF.setText(className);
                    dObj.setChangedFromUI(true);
                    servlet.setServletClass(className);
                    dObj.setChangedFromUI(false);
                    getSectionView().checkValidity();
                }
            }
        } catch (java.io.IOException ex) {}
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JButton browseButton1;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextArea descriptionTA;
    private javax.swing.JPanel filler;
    private javax.swing.JLabel initParamsLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton jspFileRB;
    private javax.swing.JTextField jspFileTF;
    private javax.swing.JLabel orderLabel;
    private javax.swing.JTextField orderTF;
    private javax.swing.JRadioButton servletClassRB;
    private javax.swing.JTextField servletClassTF;
    private javax.swing.JLabel servletMappingLabel;
    private javax.swing.JTextField servletMappingsTF;
    private javax.swing.JLabel servletNameLabel;
    private javax.swing.JTextField servletNameTF;
    // End of variables declaration//GEN-END:variables
    
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (servletClassRB.isSelected()) {
            servletClassTF.setEnabled(true);
            jspFileTF.setEnabled(false);
            browseButton.setEnabled(true);
            browseButton1.setEnabled(false);
            String servletClass = servletClassTF.getText().trim();
            if (servletClass.length()>0 && !servletClass.equals(servlet.getServletClass())) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                servlet.setServletClass(servletClass);
                dObj.setChangedFromUI(false);
                getSectionView().checkValidity();
            }
        } else {
            servletClassTF.setEnabled(false);
            jspFileTF.setEnabled(true);
            browseButton.setEnabled(false);
            browseButton1.setEnabled(true);
            String jspFile = jspFileTF.getText().trim();
            if (jspFile.length()>0 && !jspFile.equals(servlet.getJspFile())) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                servlet.setJspFile(jspFile);
                dObj.setChangedFromUI(false);
                getSectionView().checkValidity();
            }
        }
    }
    public javax.swing.JComponent getErrorComponent(String name) {
        if ("ServletName".equals(name)) return servletNameTF; //NOI18N
        else if ("ServletClass".equals(name)) return servletClassTF; //NOI18N
        else if ("JspFile".equals(name)) return jspFileTF; //NOI18N
        else if ("ServletMapping".equals(name)) return servletMappingsTF; //NOI18N
        return null;
    }
    
    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==servletNameTF) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Servlet Name",servletNameTF));
                return;
            }
            Servlet[] servlets = dObj.getWebApp().getServlet();
            for (int i=0;i<servlets.length;i++) {
                if (servlet!=servlets[i] && val.equals(servlets[i].getServletName())) {
                    getSectionView().getErrorPanel().setError(new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, val, servletNameTF));
                    return;
                }
            }
            getSectionView().getErrorPanel().clearError();
        } else if (comp==servletClassTF) {
            String text = (String)value;
            if (text.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Servlet Class",servletClassTF));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        } else if (comp==jspFileTF) {
            String text = (String)value;
            if (text.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Jsp File",jspFileTF));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        } else if (comp==servletMappingsTF) {
            String text = (String)value;
            String[] patterns = DDUtils.getStringArray(text);
            for (int i=0;i<patterns.length;i++) {
                String errorMessage = DDUtils.checkServletMappig(patterns[i]);
                if (errorMessage!=null) {
                    getSectionView().getErrorPanel().setError(new Error(Error.ERROR_MESSAGE, errorMessage, servletMappingsTF));
                    return;
                }
                if (DDUtils.isServletMapping(dObj.getWebApp(),servlet,patterns[i])) {
                    getSectionView().getErrorPanel().setError(new Error(Error.DUPLICATE_VALUE_MESSAGE, patterns[i], servletMappingsTF));
                    return;
                }
            }
            getSectionView().getErrorPanel().clearError();
        } else if (comp==orderTF) {
            String text = (String)value;
            java.math.BigInteger los=null;
            try {
                los = new java.math.BigInteger(text);
            } catch (NumberFormatException ex) {}
            if (los==null) {
                getSectionView().getErrorPanel().setError(new Error(Error.TYPE_FATAL, Error.ERROR_MESSAGE, "Invalid Value : "+text, orderTF));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source==servletNameTF) {
            String text = (String)value;
            // change servlet-mappings
            ServletMapping[] maps = DDUtils.getServletMappings(dObj.getWebApp(), servlet);
            for (int i=0;i<maps.length;i++) {
                maps[i].setServletName(text);
            }
            // change refering filter mappings
            FilterMapping[] filterMappings = DDUtils.getFilterMappings(dObj.getWebApp(), servlet);
            for (int i = 0; i < filterMappings.length; i++) {
                filterMappings[i].setServletName(text);
            }
            // change servlet-name
            servlet.setServletName(text);
            //change panel title, node name
            SectionPanel enclosingPanel = getSectionView().findSectionPanel(servlet);
            enclosingPanel.setTitle(((ServletsMultiViewElement.ServletsView)getSectionView()).getServletTitle(servlet));
            enclosingPanel.getNode().setDisplayName(text);
        } else if (source==servletClassTF) {
            String text = (String)value;
            servlet.setServletClass(text.length()==0?null:text);
        } else if (source==jspFileTF) {
            String text = (String)value;
            servlet.setJspFile(text.length()==0?null:text);
        } else if (source==descriptionTA) {
            String text = (String)value;
            servlet.setDescription(text.length()==0?null:text);
        } else if (source==servletMappingsTF) {
            DDUtils.setServletMappings(dObj.getWebApp(),servlet,DDUtils.getStringArray((String)value));
            SectionPanel enclosingPanel = getSectionView().findSectionPanel(servlet);
            enclosingPanel.setTitle(((ServletsMultiViewElement.ServletsView)getSectionView()).getServletTitle(servlet));
        } else if (source==orderTF) {
            String text = (String)value;
            servlet.setLoadOnStartup(text.length()==0?null:new java.math.BigInteger(text));
        } else if (source == runAsCB) {
            String text = (String) runAsCB.getSelectedItem();
            
            if (text.length() == 0) {
                servlet.setRunAs(null);
            } else {
                try {
                    RunAs runAs = (RunAs) dObj.getWebApp().createBean("RunAs"); //NOI18N
                    runAs.setRoleName(text);
                    servlet.setRunAs(runAs);
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }     //NOI18N
                
            }
        }
    }
    
    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (source==servletNameTF) {
            servletNameTF.setText(servlet.getServletName());
        } else if (source==servletClassTF) {
            servletClassTF.setText(servlet.getServletClass());
        } else if (source==jspFileTF) {
            jspFileTF.setText(servlet.getJspFile());
        } else if (source==servletMappingsTF) {
            String[] urlPatterns = DDUtils.getUrlPatterns(dObj.getWebApp(),servlet);
            servletMappingsTF.setText(DDUtils.urlPatternList(urlPatterns));
        } else if (source==orderTF) {
            java.math.BigInteger los = servlet.getLoadOnStartup();
            orderTF.setText(los==null?"":los.toString());
        }
    }
    
    /** This will be called before model is changed from this panel
     */
    @Override
    protected void startUIChange() {
        dObj.setChangedFromUI(true);
    }
    
    /** This will be called after model is changed from this panel
     */
    @Override
    protected void endUIChange() {
        dObj.modelUpdatedFromUI();
        dObj.setChangedFromUI(false);
    }
    
    
}
