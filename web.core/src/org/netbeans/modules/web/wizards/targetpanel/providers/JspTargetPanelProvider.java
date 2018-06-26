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
