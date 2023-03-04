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
package org.netbeans.modules.web.wizards.targetpanel.providers;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.netbeans.modules.target.iterator.api.TargetChooserPanel;
import org.netbeans.modules.target.iterator.api.TargetChooserPanelGUI;
import org.netbeans.modules.target.iterator.spi.TargetPanelProvider;
import org.netbeans.modules.target.iterator.spi.TargetPanelUIManager;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.wizards.FileType;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


/**
 * @author ads
 *
 */
@ServiceProvider(service=TargetPanelProvider.class)
public class TagLibTargetPanelProvider extends WebTargetPanelProvider<FileType> {

    static final String TAG_LIBRARY = "tag_library";        // NOI18N
    
    public static final String URI      = "uri";            // NOI18N
    public static final String PREFIX   = "prefix";         // NOI18N
    
    public TagLibTargetPanelProvider(){
        myUIManager = new TagLibUIManager();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.wizards.targetpanel.providers.WebTargetPanelProvider#init(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    @Override
    public void init( TargetChooserPanel<FileType> panel ) {
        super.init(panel);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getNewFileName()
     */
    public String getNewFileName() {
        return super.getNewFileName()+TAG_LIBRARY;        // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getWizardTitle()
     */
    public String getWizardTitle() {
        return NbBundle.getMessage(TagLibTargetPanelProvider.class, "TITLE_TLD");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#isValid(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public boolean isValid( TargetChooserPanel<FileType> panel ) {
        if ( !panel.checkValid() ){
            return false;
        }
        //  check if the TLD info is correct
        // XX precisely we should check for 'tokens composed of characters, 
        // digits, ".", ":", "-", and the characters defined by Unicode, 
        // such as "combining" or "extender"' to be sure that TLD will validate
        String tldName = panel.getComponent().getTargetName();
        if (tldName.indexOf(' ') >= 0 || tldName.indexOf(',') >= 0) {
            panel.getTemplateWizard().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(TagLibTargetPanelProvider.class,
                            "TXT_wrongTagLibName", tldName)); // NOI18N
            return false;
        }
        return true;
    }
    
    public TagLibUIManager getUIManager() {
        return myUIManager;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#isApplicable(java.lang.Object)
     */
    public boolean isApplicable( FileType id ) {
        return id == FileType.TAGLIBRARY;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.wizards.targetpanel.providers.WebTargetPanelProvider#storeSettings(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    @Override
    public void storeSettings( TargetChooserPanel<FileType> panel ) {
        panel.getTemplateWizard().putProperty(URI, getUIManager().getUri());
        panel.getTemplateWizard().putProperty(PREFIX, getUIManager().getPrefix());
    }
    
    
    protected WebModule getWebModule(){
        return myUIManager.getWebModule();
    }
    
    private TagLibUIManager myUIManager;
}

class TagLibUIManager implements TargetPanelUIManager<FileType> {
    
    private static final String TLD_IN_JAVALIB_FOLDER="META-INF"; //NOI18N
    private static final String TLD_FOLDER="tlds"; //NOI18N
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#initValues(org.netbeans.modules.target.iterator.api.TargetChooserPanel, org.netbeans.modules.target.iterator.api.TargetChooserPanelGUI)
     */
    public void initValues( TargetChooserPanel<FileType> panel,
            TargetChooserPanelGUI<FileType> uiPanel )
    {
        if (panel.getSourceGroups()!=null && 
                panel.getSourceGroups().length>0) 
        {
            myWebModule = WebModule.getWebModule(panel.getSourceGroups()[0].
                    getRootFolder());
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#changeUpdate(javax.swing.event.DocumentEvent, org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public void changeUpdate( DocumentEvent e , TargetChooserPanel<FileType> panel) {
        if (!myUriWasTyped) {
            String norm=panel.getComponent().getNormalizedFolder( );
            //Default value for uri
            if (getWebModule()==null) {
                String pack = getPackageNameInMetaInf( panel );
                myUriTextField.setText((pack.length()>0?pack+".":"")+
                        panel.getComponent().getDocumentName());    // NOI18N
            }
            else {
                String selectedFolder = panel.getComponent().getSelectedFolder();
                if ("WEB-INF".equals(selectedFolder)) {
                    myUriTextField.setText("/"+selectedFolder+(norm.length()==0?"":
                        "/"+norm)+ "/"+panel.getComponent().getDocumentName()); //NOI18N
                } else {
                    myUriTextField.setText((norm.length()==0?"":
                        "/"+norm)+"/"+panel.getComponent().getDocumentName()); //NOI18N
                }
            }
        }
        //Default value for prefix
        if (!myPrefixWasTyped){
             myPrefixTextField.setText(panel.getComponent().getDocumentName().toLowerCase());
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#getAccessibleDescription()
     */
    public String getAccessibleDescription() {
        return TagLibTargetPanelProvider.TAG_LIBRARY;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#getErrorMessage(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public String getErrorMessage( TargetChooserPanel<FileType> panel ) {
        if (getWebModule()==null) {
            if (!panel.getComponent().getNormalizedFolder().
                    startsWith(TLD_IN_JAVALIB_FOLDER))
            {
                return NbBundle.getMessage(TagLibTargetPanelProvider.class,
                        "NOTE_TLDInJavalib");       // NOI18N
            }
        } else {
            boolean isWebInfLocation = panel.getComponent().getSelectedFolder().
                equals("WEB-INF");      // NOI18N
            if (!panel.getComponent().getNormalizedFolder().
                    startsWith("WEB-INF") && !isWebInfLocation) //NOI18N
            {
                return NbBundle.getMessage(TagLibTargetPanelProvider.class,
                        "NOTE_TLDInWeb");       // NOI18N
            }
        }
        if (getUri().length()==0){ 
            return NbBundle.getMessage(TagLibTargetPanelProvider.class,
                    "MSG_missingUri");  //NOI18N
        }
        if (getPrefix().length()==0) {
            return NbBundle.getMessage(TagLibTargetPanelProvider.class,
                    "MSG_missingPrefix"); //NOI18N
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#getOptionPanel()
     */
    public JPanel getOptionPanel() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#initComponents(javax.swing.JPanel, org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public void initComponents( JPanel mainPanel , 
            final TargetChooserPanel<FileType> panel,
            final TargetChooserPanelGUI<FileType> uiPanel ) 
    {
        GridBagConstraints gridBagConstraints;
        myUriTextField = new JTextField();
        myUriTextField.setColumns(20);
        myPrefixTextField = new JTextField();
        myPrefixTextField.setColumns(5);
        
        myUriLabel = new javax.swing.JLabel(NbBundle.getMessage(TagLibTargetPanelProvider.class, 
                "LBL_URI"));
        myUriLabel.setToolTipText(NbBundle.getMessage(TagLibTargetPanelProvider.class,
                "TTT_URI"));
        myUriLabel.setLabelFor(myUriTextField);
        myUriLabel.setDisplayedMnemonic(NbBundle.getMessage(
                TagLibTargetPanelProvider.class, "A11Y_URI_mnem").charAt(0));
        myUriTextField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(TagLibTargetPanelProvider.class, "A11Y_DESC_URI"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        mainPanel.add(myUriLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        mainPanel.add(myUriTextField, gridBagConstraints);

        myPrefixLabel = new javax.swing.JLabel(NbBundle.getMessage(
                TagLibTargetPanelProvider.class, "LBL_Prefix"));
        myPrefixLabel.setLabelFor(myPrefixTextField);
        myPrefixLabel.setToolTipText(NbBundle.getMessage(
                TagLibTargetPanelProvider.class, "TTT_prefix"));
        myPrefixLabel.setDisplayedMnemonic(NbBundle.getMessage(
                TagLibTargetPanelProvider.class, "A11Y_Prefix_mnem").charAt(0));
        myPrefixTextField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(TagLibTargetPanelProvider.class, "A11Y_DESC_Prefix"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        mainPanel.add(myPrefixLabel, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        mainPanel.add(myPrefixTextField, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.weightx = 2.0;
        mainPanel.add(new javax.swing.JPanel(), gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        mainPanel.add(new javax.swing.JPanel(), gridBagConstraints);

        myUriTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                myUriWasTyped=true;
                panel.fireChange();
            }
        });

        myPrefixTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                myPrefixWasTyped=true;
                panel.fireChange();
            }
        });        
        
        
        uiPanel.setNameLabel(NbBundle.getMessage(
                TagLibTargetPanelProvider.class, "LBL_TldName"));
        //listener to update fileTextField
        uiPanel.addLocationListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uiPanel.changedUpdate(null);
            }
        });
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#initFolderValue(org.netbeans.modules.target.iterator.api.TargetChooserPanel, java.lang.String, javax.swing.JTextField)
     */
    public void initFolderValue( TargetChooserPanel<FileType> panel , String target , 
            JTextField field ) 
    {
        if ( target!=null && !target.startsWith(TLD_IN_JAVALIB_FOLDER)) {
            target=null;
        }     
        
        if (getWebModule()==null) {
            field.setText(TLD_IN_JAVALIB_FOLDER+"/"); // NOI18N
        }
        else {
            field.setText(TLD_FOLDER+"/"); // NOI18N
        }
    }

    public boolean isPanelValid() {
        return (getUri().length()!=0 && getPrefix().length()!=0);
    }
    
    
    protected WebModule getWebModule(){
        return myWebModule;
    }
    
    String getUri() {
        if (myUriTextField==null) {
            return "";
        }
        else {
            return myUriTextField.getText();
        }
    }
    
    String getPrefix() {
        if (myPrefixTextField==null) {
            return "";
        }
        else {
            return myPrefixTextField.getText();
        }
    }
    
    private String getPackageNameInMetaInf(TargetChooserPanel<FileType> panel ) {
        String pack = panel.getComponent().getRelativeTargetFolder();
        if (pack.startsWith("META-INF")) {//NOI18N
            pack = pack.substring(8);
            if (pack.length()==0) {
                return "";
            }
            if (pack.startsWith("/")) {
                pack=pack.substring(1);//NOI18N
            }
        }
        if (pack.length()==0) {
            return "";//NOI18N
        }
        pack = pack.replace('/', '.');
        return pack;
    }
    
    private JTextField myUriTextField;
    private JTextField myPrefixTextField;
    private JLabel myUriLabel;
    private JLabel myPrefixLabel;
    private boolean myUriWasTyped;
    private boolean myPrefixWasTyped;
    
    private WebModule myWebModule;
    
}