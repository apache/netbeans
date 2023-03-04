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
