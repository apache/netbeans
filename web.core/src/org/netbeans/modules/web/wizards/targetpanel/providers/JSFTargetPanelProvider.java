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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
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

        gridBagConstraints = new java.awt.GridBagConstraints();
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
