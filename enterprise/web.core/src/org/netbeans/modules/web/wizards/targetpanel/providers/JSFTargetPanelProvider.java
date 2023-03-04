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
import java.awt.event.ItemEvent;
import java.util.prefs.Preferences;

import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.common.ServerUtil;
import org.netbeans.modules.target.iterator.api.TargetChooserPanel;
import org.netbeans.modules.target.iterator.api.TargetChooserPanelGUI;
import org.netbeans.modules.target.iterator.spi.TargetPanelProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.wizards.FileType;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


/**
 * @author ads
 *
 */
@ServiceProvider(service=TargetPanelProvider.class)
public class JSFTargetPanelProvider extends WebTargetPanelProvider<FileType>{
    
    static final String JSF         = "jsf";        // NOI18N
    
    static final String FACELETS_EXT="xhtml";       //NOI18N
    
    public JSFTargetPanelProvider(){
        myUIManager = new JSFUIManager();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.wizards.targetpanel.providers.WebTargetPanelProvider#init(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    @Override
    public void init( TargetChooserPanel<FileType> panel ) {
        super.init(panel);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getExpectedExtension(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public String getExpectedExtension( TargetChooserPanel<FileType> panel ) {
        if (isFacelets()) {
            return FACELETS_EXT;    // NOI18N
        }
        else {
            return Templates.getTemplate( panel.getTemplateWizard()).getExt();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getResultExtension()
     */
    public String getResultExtension( TargetChooserPanel<FileType> panel ) {
        String ext = Templates.getTemplate( panel.getTemplateWizard()).getExt();
        if ( getUIManager().isSegment()) {
            ext+="f"; //NOI18N
        }
        else if (isFacelets()) {
            ext=FACELETS_EXT; 
        }
        return ext;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getNewFileName()
     */
    public String getNewFileName() {
        return super.getNewFileName()+JSF;            // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getUIManager()
     */
    public JSFUIManager getUIManager() {
        return myUIManager;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getWizardTitle()
     */
    public String getWizardTitle() {
        return NbBundle.getMessage(JSFTargetPanelProvider.class, 
                "TITLE_JsfFile"); // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#storeSettings(org.openide.loaders.TemplateWizard)
     */
    public void storeSettings( TargetChooserPanel<FileType> panel ) {
        if (isFacelets()) {
            Preferences preferences = ProjectUtils.getPreferences(
                    panel.getProject(), ProjectUtils.class, true);
            String key = "jsf.language";            //NOI18N
            String value = "Facelets";              //NOI18N
            if (!preferences.get(key, "").equals(value)){
                preferences.put(key, value);
            }
        }
        
        panel.getTemplateWizard().putProperty(FileType.IS_XML, false);
        panel.getTemplateWizard().putProperty(FileType.IS_SEGMENT, getUIManager().isSegment());
        panel.getTemplateWizard().putProperty(FileType.IS_FACELETS, getUIManager().isFacelets());
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#isApplicable(java.lang.Object)
     */
    public boolean isApplicable( FileType id ) {
        return id == FileType.JSF;
    }
    
    protected WebModule getWebModule(){
        return myUIManager.getWebModule();
    }

    @Override
    public boolean isValid(TargetChooserPanel<FileType> panel) {
        if (super.isValid(panel)) {
            // check that this project has a valid target server
            if (!ServerUtil.isValidServerInstance(panel.getProject())) {
                panel.getTemplateWizard().putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                    NbBundle.getMessage(JSFTargetPanelProvider.class, "WARN_MissingTargetServer"));
            }
            return true;
        }
        return false;
    }

    private boolean isFacelets() {
        return getUIManager().isFacelets();
    }
    
    private JSFUIManager myUIManager;

}

class JSFUIManager extends AbstractOptionPanelManager {
    
    @Override
    public void initComponents( JPanel mainPanel , 
            final TargetChooserPanel<FileType> panel,
            final TargetChooserPanelGUI<FileType> uiPanel ) 
    {
        super.initComponents(mainPanel, panel, uiPanel);
        
        uiPanel.setNameLabel(
                NbBundle.getMessage(JSFTargetPanelProvider.class, "LBL_JspName"));
        getJspSyntaxButton().setText(NbBundle.getMessage(
                JSFTargetPanelProvider.class, "OPT_JspSyntax"));
        getJspSyntaxButton().getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(JSFTargetPanelProvider.class, "DESC_JSP"));
        
        myFaceletsSyntaxButton.setText(NbBundle.getMessage(
                JSFTargetPanelProvider.class, "OPT_Facelets"));
        myFaceletsSyntaxButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(JSFTargetPanelProvider.class, "DESC_FACELETS"));
        
        getSegmentBox().setText(NbBundle.getMessage(JSFTargetPanelProvider.class, "OPT_JspSegment"));
        getSegmentBox().getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(JSFTargetPanelProvider.class, "A11Y_DESC_JSP_segment"));
        getDescription().setText(NbBundle.getMessage(
                JSFTargetPanelProvider.class,"DESC_FACELETS"));
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#getAccessibleDescription()
     */
    public String getAccessibleDescription() {
        return JSFTargetPanelProvider.JSF;            // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#getErrorMessage(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public String getErrorMessage( TargetChooserPanel<FileType> panel ) {
        if (isSegment() && !panel.getComponent().getNormalizedFolder().
                startsWith("WEB-INF/jspf")) //NOI18N
        {
            return NbBundle.getMessage(JSFTargetPanelProvider.class,
                    "NOTE_segment");        //NOI18N
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#isPanelValid()
     */
    public boolean isPanelValid() {
        return true;
    }
    
    protected boolean isFacelets() {
        if (myFaceletsSyntaxButton == null) {
            return false;
        }
        return myFaceletsSyntaxButton.isSelected();
    }
    
    protected boolean isSegment() {
        if (getSegmentBox() == null ) {
            return false;
        }
        return getSegmentBox().isSelected() && getSegmentBox().isEnabled();
    }

    @Override
    protected void checkBoxChanged( ItemEvent evt , TargetChooserPanel<FileType> panel ,
            final TargetChooserPanelGUI<FileType> uiPanel) {
        if (isFacelets()) {
            getSegmentBox().setEnabled(false);
            getDescription().setText(NbBundle.getMessage(JSFTargetPanelProvider.class, "DESC_FACELETS")); //NOI18N
            setNewFileExtension(uiPanel, JSFTargetPanelProvider.FACELETS_EXT);
        } else {
            getSegmentBox().setEnabled(true);
            if (isSegment()) {
                getDescription().setText(NbBundle.getMessage(JSFTargetPanelProvider.class, "DESC_segment")); //NOI18N
                setNewFileExtension(uiPanel, "jspf"); //NOI18N
            } else {
                getDescription().setText(NbBundle.getMessage(JSFTargetPanelProvider.class, "DESC_JSP")); //NOI18N
                setNewFileExtension(uiPanel, "jsp"); //NOI18N
            }
        }
        panel.fireChange();
    }

    private void setNewFileExtension(TargetChooserPanelGUI<FileType> uiPanel, String extension) {
        String createdFile = uiPanel.getFile();
        int dotOffset = createdFile.lastIndexOf("."); //NOI18N
        if (dotOffset > 0) {
            uiPanel.setFile(createdFile.substring(0, dotOffset + 1) + extension);
        } else {
            uiPanel.setFile(createdFile + "." + extension); //NOI18N
        }
    }

    @Override
    protected int initAdditionalSyntaxButton( int grid ,TargetChooserPanel<FileType> panel,
            final TargetChooserPanelGUI<FileType> uiPanel) 
    {
        return grid;
    }

    @Override
    protected int initSyntaxButton(int gridy ,final TargetChooserPanel<FileType> panel ,
            final TargetChooserPanelGUI<FileType> uiPanel) 
    {
        myFaceletsSyntaxButton = new JRadioButton();
        
        myFaceletsSyntaxButton.setSelected(true);
        getSegmentBox().setEnabled(false);
        myFaceletsSyntaxButton.setMnemonic(NbBundle.getMessage(
                JSFTargetPanelProvider.class, "A11Y_Facelets_mnem").charAt(0));
        getButtonGroup().add(myFaceletsSyntaxButton);
        myFaceletsSyntaxButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                checkBoxChanged(evt, panel , uiPanel);
            }
        });

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getOptionPanel().add(myFaceletsSyntaxButton,gridBagConstraints);
        
        getJspSyntaxButton().setMnemonic(NbBundle.getMessage(
                JSFTargetPanelProvider.class, "A11Y_JspStandard_mnem").charAt(0));
        return gridy;
    }
    
    private JRadioButton myFaceletsSyntaxButton;

}
