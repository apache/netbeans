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

package org.netbeans.modules.apisupport.project.ui.customizer;

import org.netbeans.modules.apisupport.project.ModuleDependency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.ui.ApisupportAntUIUtils;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;

/**
 * Represents panel for editing dependency details. Shown e.g. after <em>Edit</em>
 * button on the <code>CustomizerLibraries</code> panel has been pushed.
 *
 * @author Martin Krauskopf
 */
public final class EditDependencyPanel extends JPanel {
    
    private final ModuleDependency origDep;
    private final URL javadoc;
    
    private final ManifestManager.PackageExport[] pp;
    private final DefaultListModel packagesModel = new DefaultListModel();
    
    /** Creates new form EditDependencyPanel */
    public EditDependencyPanel(final ModuleDependency dep, final NbPlatform platform) {
        this.origDep = dep;
        this.pp = origDep.getModuleEntry().getPublicPackages();
        initComponents();
        initDependency();
        javadoc = origDep.getModuleEntry().getJavadoc(platform);
        showJavadocButton.setEnabled(javadoc != null);
        getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.title.AccessibleContext.accessibleName"));
    }
    
    private void refresh() {
        specVerValue.setEnabled(specVer.isSelected());
        includeInCP.setEnabled(hasAvailablePackages());
        if (!includeInCP.isEnabled()) {
            includeInCP.setSelected(false);
        } // else leave the user's selection
    }
    
    private boolean hasAvailablePackages() {
        return implVer.isSelected() || pp.length > 0;
    }
    
    /** Called first time dialog is opened. */
    private void initDependency() {
        ModuleEntry me = origDep.getModuleEntry();
        ApisupportAntUIUtils.setText(codeNameBaseValue, me.getCodeNameBase());
        ApisupportAntUIUtils.setText(jarLocationValue, me.getJarLocation().getAbsolutePath());
        ApisupportAntUIUtils.setText(releaseVersionValue, origDep.getReleaseVersion());
        ApisupportAntUIUtils.setText(specVerValue, origDep.hasImplementationDependency() ?
            me.getSpecificationVersion() :
            origDep.getSpecificationVersion());
        implVer.setSelected(origDep.hasImplementationDependency());
        availablePkg.setEnabled(hasAvailablePackages());
        includeInCP.setSelected(origDep.hasCompileDependency());
        refreshAvailablePackages();
        refresh();
        ActionListener versionListener = new ActionListener() {
            public @Override void actionPerformed(ActionEvent arg0) {
                refreshAvailablePackages();
            }
        };
        implVer.addActionListener(versionListener);
        specVer.addActionListener(versionListener);
    }
    
    public void refreshAvailablePackages() {
        packagesModel.clear();
        if (hasAvailablePackages()) {
            // XXX should show all subpackages in the case of recursion is set
            // to true instead of e.g. org/**
            SortedSet<String> packages = new TreeSet<String>();
            for (int i = 0; i < pp.length; i++) { // add public packages
                packages.add(pp[i].getPackage() + (pp[i].isRecursive() ? ".**" : "")); // NOI18N
            }
            if (implVer.isSelected()) { // add all packages
                packages.addAll(origDep.getModuleEntry().getAllPackageNames());
            }
            for (String pkg : packages) {
                packagesModel.addElement(pkg);
            }
        } else {
            packagesModel.addElement(NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel_empty"));
        }
        availablePkg.setModel(packagesModel);
    }
    
    public ModuleDependency getEditedDependency() {
        try {
            return new ModuleDependency(origDep.getModuleEntry(),
                    releaseVersionValue.getText().trim(),
                    specVerValue.getText().trim(),
                    includeInCP.isSelected(),
                    implVer.isSelected());
        } catch (NumberFormatException x) {
            // XXX would be better to notify the user somehow
            return origDep;
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

        versionGroup = new javax.swing.ButtonGroup();
        codeNameBase = new javax.swing.JLabel();
        jarLocation = new javax.swing.JLabel();
        releaseVersion = new javax.swing.JLabel();
        releaseVersionValue = new javax.swing.JTextField();
        specVer = new javax.swing.JRadioButton();
        specVerValue = new javax.swing.JTextField();
        implVer = new javax.swing.JRadioButton();
        includeInCP = new javax.swing.JCheckBox();
        availablePkgSP = new javax.swing.JScrollPane();
        availablePkg = new javax.swing.JList();
        codeNameBaseValue = new javax.swing.JTextField();
        jarLocationValue = new javax.swing.JTextField();
        showJavadocButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setPreferredSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.GridBagLayout());

        codeNameBase.setLabelFor(codeNameBaseValue);
        org.openide.awt.Mnemonics.setLocalizedText(codeNameBase, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_CNB")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(codeNameBase, gridBagConstraints);
        codeNameBase.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.codeNameBase.AccessibleContext.accessibleDescription")); // NOI18N

        jarLocation.setLabelFor(jarLocationValue);
        org.openide.awt.Mnemonics.setLocalizedText(jarLocation, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_JAR")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 12);
        add(jarLocation, gridBagConstraints);
        jarLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.jarLocation.AccessibleContext.accessibleDescription")); // NOI18N

        releaseVersion.setLabelFor(releaseVersionValue);
        org.openide.awt.Mnemonics.setLocalizedText(releaseVersion, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_MajorReleaseVersion")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 12);
        add(releaseVersion, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(releaseVersionValue, gridBagConstraints);
        releaseVersionValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.releaseVersionValue.AccessibleContext.accessibleName")); // NOI18N
        releaseVersionValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.releaseVersionValue.AccessibleContext.accessibleDescription")); // NOI18N

        versionGroup.add(specVer);
        specVer.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(specVer, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_SpecificationVersion")); // NOI18N
        specVer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                versionChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(specVer, gridBagConstraints);
        specVer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.specVer.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(specVerValue, gridBagConstraints);
        specVerValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.specVerValue.AccessibleContext.accessibleName")); // NOI18N
        specVerValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.specVerValue.AccessibleContext.accessibleDescription")); // NOI18N

        versionGroup.add(implVer);
        org.openide.awt.Mnemonics.setLocalizedText(implVer, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_ImplementationVersion")); // NOI18N
        implVer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                versionChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(implVer, gridBagConstraints);
        implVer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.implVer.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(includeInCP, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_IncludeAPIPackages")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(includeInCP, gridBagConstraints);
        includeInCP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.includeInCP.AccessibleContext.accessibleDescription")); // NOI18N

        availablePkgSP.setViewportView(availablePkg);
        availablePkg.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.availablePkg.AccessibleContext.accessibleName")); // NOI18N
        availablePkg.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.availablePkg.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        add(availablePkgSP, gridBagConstraints);

        codeNameBaseValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(codeNameBaseValue, gridBagConstraints);
        codeNameBaseValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.codeNameBaseValue.AccessibleContext.accessibleName")); // NOI18N
        codeNameBaseValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.codeNameBaseValue.AccessibleContext.accessibleDescription")); // NOI18N

        jarLocationValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(jarLocationValue, gridBagConstraints);
        jarLocationValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.jarLocationValue.AccessibleContext.accessibleName")); // NOI18N
        jarLocationValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.jarLocationValue.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(showJavadocButton, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "CTL_ShowJavadoc")); // NOI18N
        showJavadocButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showJavadoc(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(showJavadocButton, gridBagConstraints);
        showJavadocButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel.showJavadocButton.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void showJavadoc(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showJavadoc
        HtmlBrowser.URLDisplayer.getDefault().showURL(javadoc);
    }//GEN-LAST:event_showJavadoc
    
    private void versionChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionChanged
        refresh();
        if (implVer.isSelected()) { // automatic compile-time dependency
            includeInCP.setSelected(true);
        }
    }//GEN-LAST:event_versionChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList availablePkg;
    private javax.swing.JScrollPane availablePkgSP;
    private javax.swing.JLabel codeNameBase;
    private javax.swing.JTextField codeNameBaseValue;
    private javax.swing.JRadioButton implVer;
    private javax.swing.JCheckBox includeInCP;
    private javax.swing.JLabel jarLocation;
    private javax.swing.JTextField jarLocationValue;
    private javax.swing.JLabel releaseVersion;
    private javax.swing.JTextField releaseVersionValue;
    private javax.swing.JButton showJavadocButton;
    private javax.swing.JRadioButton specVer;
    private javax.swing.JTextField specVerValue;
    private javax.swing.ButtonGroup versionGroup;
    // End of variables declaration//GEN-END:variables
    
}
