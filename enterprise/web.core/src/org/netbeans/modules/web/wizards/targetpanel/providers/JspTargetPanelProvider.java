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

import java.awt.event.ItemEvent;

import javax.swing.JPanel;

import org.netbeans.modules.target.iterator.api.TargetChooserPanel;
import org.netbeans.modules.target.iterator.api.TargetChooserPanelGUI;
import org.netbeans.modules.target.iterator.spi.TargetPanelProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.wizards.FileType;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


/**
 * @author ads
 *
 */
@ServiceProvider(service=TargetPanelProvider.class)
public class JspTargetPanelProvider extends WebTargetPanelProvider<FileType> {
    
    static final String JSP = "jsp";        // NOI18N

    public JspTargetPanelProvider(){
        myUIManager = new JspUIManager();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.wizards.targetpanel.providers.WebTargetPanelProvider#init(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    @Override
    public void init( TargetChooserPanel<FileType> panel ) {
        super.init(panel);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.wizards.targetpanel.providers.WebTargetPanelProvider#getResultExtension(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public String getResultExtension( TargetChooserPanel<FileType> panel ) {
        String ext = super.getResultExtension(panel);
        if (getUIManager().isSegment()) {
            ext+="f"; //NOI18N
        }
        else if (getUIManager().isXml()) {
            ext+="x"; //NOI18N
        }
        return ext;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getWizardTitle()
     */
    public String getWizardTitle() {
        return NbBundle.getMessage(JspTargetPanelProvider.class, "TITLE_JspFile");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getNewFileName()
     */
    public String getNewFileName() {
        return super.getNewFileName()+JSP;        // NOI18N
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.wizards.targetpanel.providers.WebTargetPanelProvider#storeSettings(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    @Override
    public void storeSettings( TargetChooserPanel<FileType> panel ) {
        panel.getTemplateWizard().putProperty(FileType.IS_XML, getUIManager().isXml());
        panel.getTemplateWizard().putProperty(FileType.IS_SEGMENT, getUIManager().isSegment());
        panel.getTemplateWizard().putProperty(FileType.IS_FACELETS, false);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#isApplicable(java.lang.Object)
     */
    public boolean isApplicable( FileType id ) {
        return id == FileType.JSP;
    }

    /* (non-Javadoc)
     */
    public JspUIManager getUIManager() {
        return myUIManager;
    }
    
    protected WebModule getWebModule(){
        return myUIManager.getWebModule();
    }

    private JspUIManager myUIManager;

}

class JspUIManager extends XmlOptionPanelManager{
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.wizards.targetpanel.providers.AbstractOptionPanelManager#initComponents(javax.swing.JPanel, org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    @Override
    public void initComponents( JPanel mainPanel , TargetChooserPanel<FileType> panel  ,
            TargetChooserPanelGUI<FileType> uiPanel ) {
        super.initComponents(mainPanel, panel, uiPanel );
        
        uiPanel.setNameLabel(NbBundle.getMessage(
                JspTargetPanelProvider.class, "LBL_JspName"));
        getJspSyntaxButton().setText(NbBundle.getMessage(JspTargetPanelProvider.class, 
                "OPT_JspSyntax"));
        getJspSyntaxButton().getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(JspTargetPanelProvider.class, "DESC_JSP"));
        
        getXmlSyntaxButton().setText(NbBundle.getMessage(
                JspTargetPanelProvider.class, "OPT_XmlSyntax"));
        getXmlSyntaxButton().getAccessibleContext()
                .setAccessibleDescription(
                        NbBundle.getMessage(JspTargetPanelProvider.class,
                                "DESC_JSP_XML"));
        
        getSegmentBox().setText(NbBundle.getMessage(JspTargetPanelProvider.class, 
                "OPT_JspSegment"));
        getSegmentBox().getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(JspTargetPanelProvider.class, "A11Y_DESC_JSP_segment"));
        getDescription().setText(NbBundle.getMessage(JspTargetPanelProvider.class,
                "DESC_JSP"));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.wizards.targetpanel.providers.XmlOptionPanelManager#initSyntaxButton(int)
     */
    @Override
    protected int initSyntaxButton( int grid , TargetChooserPanel<FileType> panel, 
            TargetChooserPanelGUI<FileType> uiPanel ) {
        int result = super.initSyntaxButton(grid, panel , uiPanel);
        getJspSyntaxButton().setMnemonic(NbBundle.getMessage(
                JspTargetPanelProvider.class, "A11Y_JspStandard_mnem").charAt(0));
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#getAccessibleDescription()
     */
    public String getAccessibleDescription() {
        return JspTargetPanelProvider.JSP;      
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#getErrorMessage(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public String getErrorMessage(TargetChooserPanel<FileType> panel ) {
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

    @Override
    protected void checkBoxChanged( ItemEvent evt , TargetChooserPanel<FileType> panel,
            TargetChooserPanelGUI<FileType> uiPanel ) {
        if (isSegment()) {
            if (isXml()) {
                getDescription().setText(NbBundle.getMessage(JspTargetPanelProvider.class,
                        "DESC_segment_XML"));
            } else {
                getDescription().setText(NbBundle.getMessage(JspTargetPanelProvider.class,
                        "DESC_segment"));
            }
            String createdFile = uiPanel.getFile();
            if (createdFile.endsWith("jspx")) {//NOI18N
                uiPanel.setFile(
                        createdFile.substring(0,createdFile.length()-1)+"f"); //NOI18N
            }
            else if (createdFile.endsWith("jsp")) {//NOI18N
                uiPanel.setFile(createdFile+"f"); //NOI18N
            }
        } else {
            String createdFile = uiPanel.getFile();
            if (isXml()) {
                getDescription().setText(NbBundle.getMessage(JspTargetPanelProvider.class,
                        "DESC_JSP_XML"));
                if (createdFile.endsWith("jspf")) { //NOI18N
                    uiPanel.setFile(
                            createdFile.substring(0,createdFile.length()-1)+"x"); //NOI18N
                } else if (createdFile.endsWith("jsp")) { //NOI18N
                    uiPanel.setFile(createdFile+"x"); //NOI18N
                } else {
                    uiPanel.setFile(
                            createdFile.substring(0,createdFile.lastIndexOf(".")+1)+"jspx"); //NOI18N
                }
            } else {
                getSegmentBox().setEnabled(true);
                getDescription().setText(NbBundle.getMessage(JspTargetPanelProvider.class,
                        "DESC_JSP"));
                if (createdFile.endsWith("jspf") || createdFile.endsWith("jspx")) { //NOI18N
                    uiPanel.setFile(
                            createdFile.substring(0,createdFile.length()-1)); //NOI18N
                } else {
                    uiPanel.setFile(
                            createdFile.substring(0,createdFile.lastIndexOf(".")+1)+"jsp"); //NOI18N
                }
            }
        }     
        panel.fireChange();
    }
    
}
