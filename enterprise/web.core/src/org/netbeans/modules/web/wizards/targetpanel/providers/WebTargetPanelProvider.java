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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.netbeans.modules.target.iterator.api.TargetChooserPanel;
import org.netbeans.modules.target.iterator.api.TargetChooserPanelGUI;
import org.netbeans.modules.target.iterator.spi.TargetPanelProvider;
import org.netbeans.modules.target.iterator.spi.TargetPanelUIManager;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
abstract class WebTargetPanelProvider<T> implements TargetPanelProvider<T> {
    
    private static final String NEW_FILE_PREFIX =
        NbBundle.getMessage( WebTargetPanelProvider.class, 
                "LBL_TargetChooserPanelGUI_NewFilePrefix" ); // NOI18N
    
    WebTargetPanelProvider(){
    }
    
    WebTargetPanelProvider(String titleA11Desc,String labelName){
        myUiManager = new DefaultTargetPanelUIManager<T>(titleA11Desc, labelName);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getRelativeSourcesFolder(org.netbeans.modules.target.iterator.api.TargetChooserPanel, org.openide.filesystems.FileObject)
     */
    public String getRelativeSourcesFolder( TargetChooserPanel<T> panel,
            FileObject sourceBase )
    {
        String sourceDir ="";
        WebModule wm = getWebModule();
        if (wm != null) {
            FileObject docBase = wm.getDocumentBase();
            if (docBase != null) {
                sourceDir = FileUtil.getRelativePath( docBase, sourceBase );
            }
            
            //just for source roots
            if (sourceDir == null) {
                sourceDir = "";
            }
        }
        return sourceDir;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getTargetFile(org.netbeans.modules.target.iterator.api.TargetChooserPanel, org.openide.filesystems.FileObject, java.lang.String)
     */
    public File getTargetFile( TargetChooserPanel<T> panel,
            FileObject locationRoot, String relativeTargetFolder )
    {
        WebModule wm = getWebModule();
        FileObject docBase = null;
        if (wm != null) {
            docBase = wm.getDocumentBase();
        }
        if ( relativeTargetFolder.length() == 0 ) {

            if (wm == null || docBase == null)
                return FileUtil.toFile(locationRoot);
            else
                return FileUtil.toFile( docBase);
        }
        else {
            // XXX have to account for FU.tF returning null
            if (wm==null || docBase == null) {
                return new File( FileUtil.toFile(locationRoot), relativeTargetFolder );
            } else {
                return new File( FileUtil.toFile( docBase ),
                        relativeTargetFolder );
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getNewFileName()
     */
    public String getNewFileName() {
        return NEW_FILE_PREFIX;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#storeSettings(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public void storeSettings( TargetChooserPanel<T> panel ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#readSettings(org.openide.loaders.TemplateWizard)
     */
    public void readSettings( TargetChooserPanel<T> panel ) {
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#isValid(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public boolean isValid( TargetChooserPanel<T> panel ) {
        return panel.checkValid();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getExpectedExtension(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public String getExpectedExtension( TargetChooserPanel<T> panel ) {
        return Templates.getTemplate( panel.getTemplateWizard()).getExt();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getResultExtension(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public String getResultExtension( TargetChooserPanel<T> panel ) {
        return Templates.getTemplate( panel.getTemplateWizard()).getExt();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#init(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public void init( TargetChooserPanel<T> panel ) {
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelProvider#getUIManager()
     */
    public TargetPanelUIManager<T> getUIManager() {
        return myUiManager;
    }
    
    protected WebModule getWebModule(){
        return myUiManager.getWebModule();
    }
    
    private DefaultTargetPanelUIManager<T> myUiManager;
}

class DefaultTargetPanelUIManager<T> implements TargetPanelUIManager<T> {

    DefaultTargetPanelUIManager( String titleA11Desc, String labelName ) {
        myA11TitleDesc = titleA11Desc;
        myLabelName = labelName;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#initValues(org.netbeans.modules.target.iterator.api.TargetChooserPanel, org.netbeans.modules.target.iterator.api.TargetChooserPanelGUI)
     */
    public void initValues( TargetChooserPanel<T> panel,
            TargetChooserPanelGUI<T> uiPanel )
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
    public void changeUpdate( DocumentEvent e , TargetChooserPanel<T> panel) {
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#getAccessibleDescription()
     */
    public String getAccessibleDescription() {
        return myA11TitleDesc;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#getErrorMessage(org.netbeans.modules.target.iterator.api.TargetChooserPanel)
     */
    public String getErrorMessage(TargetChooserPanel<T> panel) {
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
    public void initComponents( JPanel mainPanel , final TargetChooserPanel<T> panel,
            TargetChooserPanelGUI<T> uiPanel ) 
    {
        uiPanel.setNameLabel(NbBundle.getMessage(
                WebTargetPanelProvider.class, myLabelName ));
        //listener to update fileTextField
        uiPanel.addLocationListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                panel.getComponent().changedUpdate(null);
            }
        });        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#initFolderValue(org.netbeans.modules.target.iterator.api.TargetChooserPanel, java.lang.String, javax.swing.JTextField)
     */
    public void initFolderValue( TargetChooserPanel<T> panel , 
            String target , JTextField folderTextField) 
    {
        folderTextField.setText( target == null ? "" : target ); // NOI18N         
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.target.iterator.spi.TargetPanelUIManager#isPanelValid()
     */
    public boolean isPanelValid() {
        return true;
    }
    
    protected WebModule getWebModule(){
        return myWebModule;
    }
    
    private String myA11TitleDesc;
    private String myLabelName;
    
    private WebModule myWebModule;
}
