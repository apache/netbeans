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

package org.netbeans.modules.web.freeform.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.web.freeform.WebProjectGenerator;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  Radko Najman
 */
public class WebLocationsPanel extends javax.swing.JPanel implements HelpCtx.Provider {
    
    private static String J2EE_SPEC_5 = "1.5";    //NOI18N
    private static String J2EE_SPEC_1_4 = "1.4";    //NOI18N
    private static String J2EE_SPEC_1_3 = "1.3";    //NOI18N
    
    /** Original project base folder */
    private File baseFolder;
    /** Freeform Project base folder */
    private File nbProjectFolder;

    private AntProjectHelper projectHelper;
    
    private File srcPackagesLocation;
    private String classpath;
    
    private WizardDescriptor wizardDescriptor;
    
    /** Creates new form WebLocations */
    public WebLocationsPanel(WizardDescriptor wizardDescriptor) {
        initComponents();
        this.wizardDescriptor = wizardDescriptor;
        jComboBoxJ2eeLevel.addItem(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_5"));    //NOI18N
        jComboBoxJ2eeLevel.addItem(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_0"));    //NOI18N
        jComboBoxJ2eeLevel.addItem(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_1"));    //NOI18N
        jComboBoxJ2eeLevel.setSelectedIndex(0);
    }
    
    public WebLocationsPanel(AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux) {
        this(null);
        this.projectHelper = projectHelper;
        setFolders(Util.getProjectLocation(projectHelper, projectEvaluator), FileUtil.toFile(projectHelper.getProjectDirectory()));
        
        List l = WebProjectGenerator.getWebmodules(projectHelper, aux);
        if (l != null) {
            WebProjectGenerator.WebModule wm = (WebProjectGenerator.WebModule)l.get(0);
            String docroot = getLocationDisplayName(projectEvaluator, nbProjectFolder, wm.docRoot);
            String webInf;
            if (wm.webInf != null)
                webInf = getLocationDisplayName(projectEvaluator, nbProjectFolder, wm.webInf);
            else
                //NetBeans 5.x and older projects (WEB-INF is placed under Web Pages)
                webInf = docroot + "/WEB-INF"; //NOI18N
            classpath = wm.classpath;
            jTextFieldWeb.setText(docroot);
            jTextFieldWebInf.setText(webInf);
            
            jTextFieldContextPath.setText(wm.contextPath);
            if (wm.j2eeSpecLevel == null || wm.j2eeSpecLevel.equals(J2EE_SPEC_5))
                jComboBoxJ2eeLevel.setSelectedItem(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_5"));
            else if (wm.j2eeSpecLevel.equals(J2EE_SPEC_1_4))
                jComboBoxJ2eeLevel.setSelectedItem(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_0"));
            else
                jComboBoxJ2eeLevel.setSelectedItem(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_1"));
        }
    }

    /**
     * Convert given string value (e.g. "${project.dir}/src" to a file
     * and try to relativize it.
     */
    // XXX: copied from java/freeform:SourceFoldersPanel.getLocationDisplayName
    public static String getLocationDisplayName(PropertyEvaluator evaluator, File base, String val) {
        File f = Util.resolveFile(evaluator, base, val);
        if (f == null) {
            return val;
        }
        String location = f.getAbsolutePath();
        if (CollocationQuery.areCollocated(base, f)) {
            location = PropertyUtils.relativizeFile(base, f).replace('/', File.separatorChar); // NOI18N
        }
        return location;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( WebLocationsPanel.class );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldWeb = new javax.swing.JTextField();
        jButtonWeb = new javax.swing.JButton();
        jLabelWebInf = new javax.swing.JLabel();
        jTextFieldWebInf = new javax.swing.JTextField();
        jButtonWebInf = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldContextPath = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jTextArea1 = new javax.swing.JTextArea();
        jComboBoxJ2eeLevel = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "LBL_WebPagesPanel_Description")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(jLabel1, gridBagConstraints);

        jLabel2.setLabelFor(jTextFieldWeb);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "LBL_WebPagesPanel_WebPagesLocation_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(jTextFieldWeb, gridBagConstraints);
        jTextFieldWeb.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "ACS_LBL_WebPagesPanel_WebPagesLocation_A11YDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonWeb, org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "BTN_BasicProjectInfoPanel_browseAntScript")); // NOI18N
        jButtonWeb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWebActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(jButtonWeb, gridBagConstraints);
        jButtonWeb.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "ACS_LBL_WebPagesPanel_WebPagesLocationBrowse_A11YDesc")); // NOI18N

        jLabelWebInf.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/freeform/ui/Bundle").getString("MNE_DeploymentDescriptorFolder").charAt(0));
        jLabelWebInf.setLabelFor(jTextFieldWebInf);
        jLabelWebInf.setText(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "LBL_DeploymentDescriptorFolder_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jLabelWebInf, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jTextFieldWebInf, gridBagConstraints);
        jTextFieldWebInf.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "ACSD_WEBINF_TEXTFIELD")); // NOI18N

        jButtonWebInf.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/freeform/ui/Bundle").getString("MNE_BrowseWebInfLocation").charAt(0));
        jButtonWebInf.setText(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "LBL_DeploymentDescriptorBrowse_Label")); // NOI18N
        jButtonWebInf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWebInfActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(jButtonWebInf, gridBagConstraints);
        jButtonWebInf.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "ACSD_WEBINF_BROWSE")); // NOI18N

        jLabel4.setLabelFor(jTextFieldContextPath);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "LBL_WebPagesPanel_ContextPath_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jLabel4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jTextFieldContextPath, gridBagConstraints);
        jTextFieldContextPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "ACS_LBL_WebPagesPanel_ContextPath_A11YDesc")); // NOI18N

        jLabel5.setLabelFor(jComboBoxJ2eeLevel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "LBL_WebPagesPanel_J2EESpecLevel_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jLabel5, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Label.disabledForeground")));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/freeform/resources/alert_32.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jPanel1.add(jLabel6, gridBagConstraints);

        jTextArea1.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "Freeform_Warning_Message")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 4);
        jPanel1.add(jTextArea1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jComboBoxJ2eeLevel, gridBagConstraints);
        jComboBoxJ2eeLevel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "ACS_LBL_WebPagesPanel_J2EESpecLevel_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void jButtonWebInfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWebInfActionPerformed
        JFileChooser chooser = createChooser(getWebInfLocation(), wizardDescriptor);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            setWebInf(chooser.getSelectedFile());
        }
}//GEN-LAST:event_jButtonWebInfActionPerformed

    private void jButtonWebActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWebActionPerformed
        JFileChooser chooser = createChooser(getWebPagesLocation(), wizardDescriptor);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            setWebPages(chooser.getSelectedFile());
        }
    }//GEN-LAST:event_jButtonWebActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonWeb;
    private javax.swing.JButton jButtonWebInf;
    private javax.swing.JComboBox jComboBoxJ2eeLevel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelWebInf;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextFieldContextPath;
    private javax.swing.JTextField jTextFieldWeb;
    private javax.swing.JTextField jTextFieldWebInf;
    // End of variables declaration//GEN-END:variables
    
    private static JFileChooser createChooser(File webPagesLoc, WizardDescriptor wizardDescriptor) {
	String path = webPagesLoc.getAbsolutePath();
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, new File(path));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        if (path.length() > 0 && webPagesLoc.exists()) {
            chooser.setSelectedFile(webPagesLoc);
        } else {
	    if (wizardDescriptor != null) {
		// honor the contract in issue 58987
		File currentDirectory = null;
		FileObject existingSourcesFO = Templates.getExistingSourcesFolder(wizardDescriptor);
		if (existingSourcesFO != null) {
		    File existingSourcesFile = FileUtil.toFile(existingSourcesFO);
		    if (existingSourcesFile != null && existingSourcesFile.isDirectory()) {
			currentDirectory = existingSourcesFile;
		    }
		}
		if (currentDirectory != null) {
		    chooser.setCurrentDirectory(currentDirectory);
		} else {
		    chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
		}
	    } else {
		chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
	    }
        }
	
        return chooser;
    }

    protected List<WebProjectGenerator.WebModule> getWebModules() {
        ArrayList<WebProjectGenerator.WebModule> l = new ArrayList<WebProjectGenerator.WebModule>();

        WebProjectGenerator.WebModule wm = new WebProjectGenerator.WebModule ();
        wm.docRoot = getRelativeLocation(getWebPagesLocation());
        wm.webInf = getRelativeLocation(getWebInfLocation());
        wm.contextPath = jTextFieldContextPath.getText().trim();
        
        String j2eeLevel = (String) jComboBoxJ2eeLevel.getSelectedItem();
        if (j2eeLevel.equals(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_5")))
            wm.j2eeSpecLevel = J2EE_SPEC_5;
        else if (j2eeLevel.equals(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_0")))
            wm.j2eeSpecLevel = J2EE_SPEC_1_4;
        else
            wm.j2eeSpecLevel = J2EE_SPEC_1_3;
        
        wm.classpath = classpath;
        l.add (wm);
        return l;
    }

    protected List<String> getJavaSrcFolder() {
        ArrayList<String> l = new ArrayList<String>();
        File sourceLoc = getSrcPackagesLocation();
        l.add(getRelativeLocation(sourceLoc));
        l.add(sourceLoc.getName());
        return l;
    }

    /**
     * @return list of pairs [relative path, display name]
     */
    protected List<String> getWebSrcFolder() {
        ArrayList<String> l = new ArrayList<String>();
        final File webLocation = getWebPagesLocation();
        l.add(getRelativeLocation(webLocation));
        l.add(webLocation.getName());
        return l;
    }

    /**
     * @return list of pairs [relative path, display name]
     */
    protected List<String> getWebInfFolder() {
        ArrayList<String> l = new ArrayList<String>();
        final File webInfLocation = getWebInfLocation();
        l.add(getRelativeLocation(webInfLocation));
        l.add(webInfLocation.getName());
        return l;
    }

    private File getAsFile(String filename) {
        return PropertyUtils.resolveFile(nbProjectFolder, filename);
    }

    /** Called from WizardDescriptor.Panel and ProjectCustomizer.Panel
     * to set base folder. Panel will use this for default position of JFileChooser.
     * @param baseFolder original project base folder
     * @param nbProjectFolder Freeform Project base folder
     */
    public void setFolders(File baseFolder, File nbProjectFolder) {
        this.baseFolder = baseFolder;
        this.nbProjectFolder = nbProjectFolder;
    }
    
    protected void setWebPages(String path) {
        jTextFieldWeb.setText(path);
    }
    
    protected void setWebInf(String path) {
        jTextFieldWebInf.setText(path);
    }

    protected void setSrcPackages(String path) {
        setSrcPackages(getAsFile(path));
    }

    private void setWebPages(final File file) {
        setWebPages(relativizeFile(file));
    }
    
    private void setWebInf(final File file) {
        setWebInf(relativizeFile(file));
    }

    protected File getWebPagesLocation() {
        return getAsFile(jTextFieldWeb.getText()).getAbsoluteFile();
    }
    
    protected File getWebInfLocation() {
        return getAsFile(jTextFieldWebInf.getText()).getAbsoluteFile();
    }

    private void setSrcPackages(final File file) {
        srcPackagesLocation = file;
    }

    protected File getSrcPackagesLocation() {
        return srcPackagesLocation;
    }

    private String relativizeFile(final File file) {
        File normalizedFile = FileUtil.normalizeFile(file);
        if (CollocationQuery.areCollocated(nbProjectFolder, file)) {
            return PropertyUtils.relativizeFile(nbProjectFolder, normalizedFile);
        } else {
            return normalizedFile.getAbsolutePath();
        }
    }

    private String getRelativeLocation(final File location) {
        final File normalizedLocation = FileUtil.normalizeFile(location);
        return Util.relativizeLocation(baseFolder, nbProjectFolder, normalizedLocation);
    }

    ActionListener getCustomizerOkListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(projectHelper);
                WebProjectGenerator.putWebSourceFolder(projectHelper, getWebSrcFolder());
                WebProjectGenerator.putWebInfFolder(projectHelper, getWebInfFolder());
                WebProjectGenerator.putWebModules(projectHelper, aux, getWebModules());
            }
        };
    }
    
}
