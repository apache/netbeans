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

import org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup;
import org.netbeans.modules.j2ee.ddloaders.web.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.Utils;
import org.openide.util.NbBundle;
import org.netbeans.api.project.SourceGroup;

/**
 * @author  mkuchtiak
 */
public class JspPGPanel extends SectionInnerPanel implements java.awt.event.ItemListener {
    private JspPropertyGroup group;
    private DDDataObject dObj;
    /** Creates new form JspPGPanel */
    public JspPGPanel(SectionView sectionView, DDDataObject dObj,JspPropertyGroup group) {
        super(sectionView);
        this.dObj=dObj;
        this.group=group;
        initComponents();
        
        trimWhiteSpaces.setEnabled(false);
        deferredSyntaxAllowed.setEnabled(false);
        
        dispNameTF.setText(group.getDefaultDisplayName());
        addModifier(dispNameTF);
        
        Utils.makeTextAreaLikeTextField(descriptionTA,dispNameTF);
        descriptionTA.setText(group.getDefaultDescription());
        addModifier(descriptionTA);
        
        pageEncodingTF.setText(group.getPageEncoding());
        addModifier(pageEncodingTF);
        
        ignoreEL.setSelected(group.isElIgnored());
        ignoreEL.addItemListener(this);
        disableScripting.setSelected(group.isScriptingInvalid());
        disableScripting.addItemListener(this);
        xmlSyntax.setSelected(group.isIsXml());
        xmlSyntax.addItemListener(this);
        
        if (group instanceof org.netbeans.modules.j2ee.dd.impl.web.model_2_5.JspPropertyGroup){
            org.netbeans.modules.j2ee.dd.impl.web.model_2_5.JspPropertyGroup group25 = (org.netbeans.modules.j2ee.dd.impl.web.model_2_5.JspPropertyGroup) group;
            trimWhiteSpaces.setEnabled(true);
            deferredSyntaxAllowed.setEnabled(true);
            trimWhiteSpaces.setSelected(group25.isTrimDirectiveWhitespaces());
            trimWhiteSpaces.addItemListener(this);
            deferredSyntaxAllowed.setSelected(group25.isDeferredSyntaxAllowedAsLiteral());
            deferredSyntaxAllowed.addItemListener(this);
        }
        
        urlPatternsTF.setText(DDUtils.urlPatternList(group.getUrlPattern()));
        addValidatee(urlPatternsTF);
        
        preludeTF.setText(DDUtils.urlPatternList(group.getIncludePrelude()));
        addModifier(preludeTF);
        
        codaTF.setText(DDUtils.urlPatternList(group.getIncludeCoda()));
        addModifier(codaTF);
        
        LinkButton linkButton = new LinkButton(this, group, "url_patterns"); //NOI18N
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        org.openide.awt.Mnemonics.setLocalizedText(linkButton,
                NbBundle.getMessage(JspPGPanel.class, "LBL_goToSources"));
        add(linkButton, gridBagConstraints);
        
        linkButton = new LinkButton(this, group, "preludes"); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        org.openide.awt.Mnemonics.setLocalizedText(linkButton,
                NbBundle.getMessage(JspPGPanel.class, "LBL_goToSources_2"));
        add(linkButton, gridBagConstraints);
        
        linkButton = new LinkButton(this, group, "codas"); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 0);
        org.openide.awt.Mnemonics.setLocalizedText(linkButton,
                NbBundle.getMessage(JspPGPanel.class, "LBL_goToSources_3"));
        add(linkButton, gridBagConstraints);
    }
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("url_patterns".equals(errorId)) return urlPatternsTF;
        return null;
    }
    
    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==urlPatternsTF) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.TYPE_FATAL, Error.MISSING_VALUE_MESSAGE, "URL Pattern",urlPatternsTF));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source==urlPatternsTF) {
            String text = (String)value;
            // change servlet-mappings
            setUrlPatterns(text);
        } else if (source==dispNameTF) {
            String text = (String)value;
            group.setDisplayName(text.length()==0?null:text);
            SectionPanel enclosingPanel = getSectionView().findSectionPanel(group);
            enclosingPanel.setTitle(((PagesMultiViewElement.PagesView)getSectionView()).getJspGroupTitle(group));
            enclosingPanel.getNode().setDisplayName(((PagesMultiViewElement.PagesView)getSectionView()).getJspGroupNodeName(group));
        } else if (source==descriptionTA) {
            String text = (String)value;
            group.setDescription(text.length()==0?null:text);
        } else if (source==pageEncodingTF) {
            String text = (String)value;
            group.setPageEncoding(text.length()==0?null:text);
        } else if (source==preludeTF) {
            String text = (String)value;
            setPreludes(text);
        } else if (source==codaTF) {
            String text = (String)value;
            setCodas(text);
        }
    }
    
    private void setUrlPatterns(String text) {
        String[] urlPatterns = DDUtils.getStringArray(text);
        group.setUrlPattern(urlPatterns);
        SectionPanel enclosingPanel = getSectionView().findSectionPanel(group);
        enclosingPanel.setTitle(((PagesMultiViewElement.PagesView)getSectionView()).getJspGroupTitle(group));
    }
    private void setPreludes(String text) {
        String[] preludes = DDUtils.getStringArray(text);
        group.setIncludePrelude(preludes);
    }
    private void setCodas(String text) {
        String[] codas = DDUtils.getStringArray(text);
        group.setIncludeCoda(codas);
    }
    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (source==urlPatternsTF) {
            urlPatternsTF.setText(DDUtils.urlPatternList(group.getUrlPattern()));
        }
    }
    
    public void linkButtonPressed(Object obj, String id) {
        String text=null;
        if ("url_patterns".equals(id)) {
            text = urlPatternsTF.getText();
        } else if ("preludes".equals(id)) {
            text = preludeTF.getText();
        } else if ("codas".equals(id)) {
            text = codaTF.getText();
        }
        java.util.StringTokenizer tok = new java.util.StringTokenizer(text,",");
        DDUtils.openEditorForFiles(dObj,tok);
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        dispNameLabel = new javax.swing.JLabel();
        dispNameTF = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionTA = new javax.swing.JTextArea();
        urlPatternsLabel = new javax.swing.JLabel();
        urlPatternsTF = new javax.swing.JTextField();
        browseButton1 = new javax.swing.JButton();
        hintUrlPatterns = new javax.swing.JLabel();
        pageEncodingLabel = new javax.swing.JLabel();
        pageEncodingTF = new javax.swing.JTextField();
        ignoreEL = new javax.swing.JCheckBox();
        disableScripting = new javax.swing.JCheckBox();
        xmlSyntax = new javax.swing.JCheckBox();
        preludeLabel = new javax.swing.JLabel();
        preludeTF = new javax.swing.JTextField();
        browseButton2 = new javax.swing.JButton();
        codaLabel = new javax.swing.JLabel();
        codaTF = new javax.swing.JTextField();
        browseButton3 = new javax.swing.JButton();
        filler = new javax.swing.JPanel();
        trimWhiteSpaces = new javax.swing.JCheckBox();
        deferredSyntaxAllowed = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        dispNameLabel.setLabelFor(dispNameTF);
        org.openide.awt.Mnemonics.setLocalizedText(dispNameLabel, org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_displayName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(dispNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(dispNameTF, gridBagConstraints);

        descriptionLabel.setLabelFor(descriptionTA);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_description")); // NOI18N
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
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(descriptionTA, gridBagConstraints);

        urlPatternsLabel.setLabelFor(urlPatternsTF);
        org.openide.awt.Mnemonics.setLocalizedText(urlPatternsLabel, org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_urlPatterns")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(urlPatternsLabel, gridBagConstraints);

        urlPatternsTF.setColumns(40);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(urlPatternsTF, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton1, org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_browse")); // NOI18N
        browseButton1.setMargin(new java.awt.Insets(0, 14, 0, 14));
        browseButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        add(browseButton1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(hintUrlPatterns, org.openide.util.NbBundle.getMessage(JspPGPanel.class, "HINT_urlPatterns")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(hintUrlPatterns, gridBagConstraints);

        pageEncodingLabel.setLabelFor(pageEncodingTF);
        org.openide.awt.Mnemonics.setLocalizedText(pageEncodingLabel, org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_pageEncoding")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(pageEncodingLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(pageEncodingTF, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(ignoreEL, org.openide.util.NbBundle.getMessage(JspPGPanel.class, "CHB_ignoreEL")); // NOI18N
        ignoreEL.setActionCommand("Expression Language Ignored");
        ignoreEL.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(ignoreEL, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(disableScripting, org.openide.util.NbBundle.getMessage(JspPGPanel.class, "CHB_ignoreScripting")); // NOI18N
        disableScripting.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(disableScripting, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(xmlSyntax, org.openide.util.NbBundle.getMessage(JspPGPanel.class, "CHB_xmlSyntax")); // NOI18N
        xmlSyntax.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(xmlSyntax, gridBagConstraints);

        preludeLabel.setLabelFor(preludeTF);
        org.openide.awt.Mnemonics.setLocalizedText(preludeLabel, org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_includePrelude")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(preludeLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(preludeTF, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton2, org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_browse")); // NOI18N
        browseButton2.setMargin(new java.awt.Insets(0, 14, 0, 14));
        browseButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        add(browseButton2, gridBagConstraints);

        codaLabel.setLabelFor(codaTF);
        org.openide.awt.Mnemonics.setLocalizedText(codaLabel, org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_includeCoda")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        add(codaLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        add(codaTF, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton3, org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_browse")); // NOI18N
        browseButton3.setMargin(new java.awt.Insets(0, 14, 0, 14));
        browseButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButton3ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 5, 0);
        add(browseButton3, gridBagConstraints);

        filler.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(filler, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(trimWhiteSpaces, bundle.getString("CHB_TrimDirectiveWhitespace")); // NOI18N
        trimWhiteSpaces.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(trimWhiteSpaces, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(deferredSyntaxAllowed, bundle.getString("CHB_DeferredSyntaxAllowedAsLiteral")); // NOI18N
        deferredSyntaxAllowed.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(deferredSyntaxAllowed, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void browseButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButton3ActionPerformed
        try {
            SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
            org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
            if (fo!=null) {
                String fileName = "/"+DDUtils.getResourcePath(groups,fo,'/',true);
                String oldValue = codaTF.getText();
                if (fileName.length()>0) {
                    String newValue = DDUtils.addItem(oldValue,fileName,false);
                    if (!oldValue.equals(newValue)) {
                        dObj.modelUpdatedFromUI();
                        dObj.setChangedFromUI(true);
                        codaTF.setText(newValue);
                        setCodas(newValue);
                        dObj.setChangedFromUI(false);
                    }
                }
            }
        } catch (java.io.IOException ex) {}
    }//GEN-LAST:event_browseButton3ActionPerformed
    
    private void browseButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButton2ActionPerformed
        try {
            SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
            org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
            if (fo!=null) {
                String fileName = "/"+DDUtils.getResourcePath(groups,fo,'/',true);
                String oldValue = preludeTF.getText();
                if (fileName.length()>0) {
                    String newValue = DDUtils.addItem(oldValue,fileName,false);
                    if (!oldValue.equals(newValue)) {
                        dObj.modelUpdatedFromUI();
                        dObj.setChangedFromUI(true);
                        preludeTF.setText(newValue);
                        setPreludes(newValue);
                        dObj.setChangedFromUI(false);
                    }
                }
            }
        } catch (java.io.IOException ex) {}
    }//GEN-LAST:event_browseButton2ActionPerformed
    
    private void browseButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButton1ActionPerformed
        try {
            SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
            org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
            if (fo!=null) {
                String fileName = "/"+DDUtils.getResourcePath(groups,fo,'/',true);
                String oldValue = urlPatternsTF.getText();
                if (fileName.length()>0) {
                    String newValue = DDUtils.addItem(oldValue,fileName,false);
                    if (!oldValue.equals(newValue)) {
                        dObj.modelUpdatedFromUI();
                        dObj.setChangedFromUI(true);
                        urlPatternsTF.setText(newValue);
                        setUrlPatterns(newValue);
                        dObj.setChangedFromUI(false);
                        getSectionView().checkValidity();
                    }
                }
            }
        } catch (java.io.IOException ex) {}
    }//GEN-LAST:event_browseButton1ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton1;
    private javax.swing.JButton browseButton2;
    private javax.swing.JButton browseButton3;
    private javax.swing.JLabel codaLabel;
    private javax.swing.JTextField codaTF;
    private javax.swing.JCheckBox deferredSyntaxAllowed;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextArea descriptionTA;
    private javax.swing.JCheckBox disableScripting;
    private javax.swing.JLabel dispNameLabel;
    private javax.swing.JTextField dispNameTF;
    private javax.swing.JPanel filler;
    private javax.swing.JLabel hintUrlPatterns;
    private javax.swing.JCheckBox ignoreEL;
    private javax.swing.JLabel pageEncodingLabel;
    private javax.swing.JTextField pageEncodingTF;
    private javax.swing.JLabel preludeLabel;
    private javax.swing.JTextField preludeTF;
    private javax.swing.JCheckBox trimWhiteSpaces;
    private javax.swing.JLabel urlPatternsLabel;
    private javax.swing.JTextField urlPatternsTF;
    private javax.swing.JCheckBox xmlSyntax;
    // End of variables declaration//GEN-END:variables
    
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        System.out.println("State changed: " + evt);
        System.out.println("State changed: " + evt.getSource());
        dObj.modelUpdatedFromUI();
        dObj.setChangedFromUI(true);
        if (evt.getSource() == ignoreEL) {
            group.setElIgnored(ignoreEL.isSelected());
        } else if (evt.getSource() == disableScripting) {
            group.setScriptingInvalid(disableScripting.isSelected());
        } else if (evt.getSource() == xmlSyntax) {
            group.setIsXml(xmlSyntax.isSelected());
        }else if (evt.getSource() == trimWhiteSpaces){
            ((org.netbeans.modules.j2ee.dd.impl.web.model_2_5.JspPropertyGroup) group).setTrimDirectiveWhitespaces(trimWhiteSpaces.isSelected());
        } else if (evt.getSource() == deferredSyntaxAllowed){
            ((org.netbeans.modules.j2ee.dd.impl.web.model_2_5.JspPropertyGroup) group).setDeferredSyntaxAllowedAsLiteral(deferredSyntaxAllowed.isSelected());
        }
        dObj.setChangedFromUI(false);
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
