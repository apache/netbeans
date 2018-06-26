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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.ddloaders.web.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.api.project.SourceGroup;

/**
 * @author  mkuchtiak
 */
public class WelcomeFilesPanel extends SectionInnerPanel {
    DDDataObject dObj;
    /** Creates new form JspPGPanel */
    public WelcomeFilesPanel(SectionView sectionView, DDDataObject dObj) {
        super(sectionView);
        this.dObj=dObj;
        initComponents();
        addModifier(wfTF);
        // welcome files initialization
        getWelcomeFiles();
        LinkButton linkButton = new LinkButton(this, null,null);
        org.openide.awt.Mnemonics.setLocalizedText(linkButton,
                org.openide.util.NbBundle.getMessage(WelcomeFilesPanel.class, "LBL_goToSources"));
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(linkButton, gridBagConstraints);
    }
    public javax.swing.JComponent getErrorComponent(String errorId) {
        return wfTF;
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

    public void setValue(javax.swing.JComponent source, Object value) {
        WebApp webApp = dObj.getWebApp();
        String text = (String)value;
        setWelcomeFiles(webApp,text);
    }
    
    private void setWelcomeFiles(WebApp webApp, String text) {
        if (text.length()==0) {
            webApp.setWelcomeFileList(null);
        } else {
            java.util.List wfList = new java.util.ArrayList();
            java.util.StringTokenizer tok = new java.util.StringTokenizer(text,",");
            while (tok.hasMoreTokens()) {
                String wf = tok.nextToken().trim();
                if (wf.length()>0 && !wfList.contains(wf)) wfList.add(wf);
            }
            if (wfList.size()==0) {
                try {
                    WelcomeFileList welcomeFileList = (WelcomeFileList)webApp.createBean("WelcomeFileList"); //NOI18N
                    webApp.setWelcomeFileList(welcomeFileList);
                } catch (ClassNotFoundException ex) {}
            }
            else {
                String[] welcomeFiles = new String[wfList.size()];
                wfList.toArray(welcomeFiles);
                WelcomeFileList welcomeFileList = webApp.getSingleWelcomeFileList();
                if (welcomeFileList==null) {
                    try {
                        welcomeFileList = (WelcomeFileList)webApp.createBean("WelcomeFileList"); //NOI18N
                        welcomeFileList.setWelcomeFile(welcomeFiles);
                        webApp.setWelcomeFileList(welcomeFileList);
                    } catch (ClassNotFoundException ex) {}
                } else welcomeFileList.setWelcomeFile(welcomeFiles);
            }
        }
    }
    
    public void linkButtonPressed(Object obj, String id) {
        java.util.StringTokenizer tok = new java.util.StringTokenizer(wfTF.getText(),",");
        DDUtils.openEditorForFiles(dObj,tok);
    }
    
    private void getWelcomeFiles() {
        WebApp webApp = dObj.getWebApp();
        WelcomeFileList wfList = webApp.getSingleWelcomeFileList();
        if (wfList==null) {
            wfTF.setText("");
            return;
        } else {
            String[] welcomeFiles = wfList.getWelcomeFile();
            StringBuffer buf = new StringBuffer();
            for (int i=0;i<welcomeFiles.length;i++) {
                if (i>0) buf.append(", ");
                buf.append(welcomeFiles[i].trim());
            }
            wfTF.setText(buf.toString()); 
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

        wfLabel = new javax.swing.JLabel();
        wfTF = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        wfDescription = new javax.swing.JLabel();
        filler = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        wfLabel.setLabelFor(wfTF);
        org.openide.awt.Mnemonics.setLocalizedText(wfLabel, org.openide.util.NbBundle.getMessage(WelcomeFilesPanel.class, "LBL_welcomeFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 6);
        add(wfLabel, gridBagConstraints);

        wfTF.setColumns(50);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(wfTF, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(WelcomeFilesPanel.class, "LBL_browse")); // NOI18N
        browseButton.setMargin(new java.awt.Insets(0, 14, 0, 14));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 0, 0);
        add(browseButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(wfDescription, org.openide.util.NbBundle.getMessage(WelcomeFilesPanel.class, "DESC_welcomeFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(wfDescription, gridBagConstraints);

        filler.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(filler, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        try {
            SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
            org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
            if (fo!=null) {
                String fileName = DDUtils.getResourcePath(groups,fo,'/',true);
                String oldWF = wfTF.getText();
                if (fileName.length()>0) {
                    String newWF = DDUtils.addItem(oldWF,fileName,true);
                    if (!oldWF.equals(newWF)) {
                        wfTF.setText(newWF);
                        dObj.modelUpdatedFromUI();
                        dObj.setChangedFromUI(true);
                        setWelcomeFiles(dObj.getWebApp(), newWF);
                        dObj.setChangedFromUI(false);
                    }
                }
            }
        } catch (java.io.IOException ex) {}
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JPanel filler;
    private javax.swing.JLabel wfDescription;
    private javax.swing.JLabel wfLabel;
    private javax.swing.JTextField wfTF;
    // End of variables declaration//GEN-END:variables
    
}
