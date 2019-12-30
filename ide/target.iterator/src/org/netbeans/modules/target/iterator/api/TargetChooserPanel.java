/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.target.iterator.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.event.ChangeListener;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.target.iterator.spi.TargetPanelProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  ads
 */
public final class TargetChooserPanel<T> implements WizardDescriptor.Panel {

    private static final Logger LOG = Logger.getLogger(TargetChooserPanel.class.getName());

    private static final Pattern INVALID_FILENAME_CHARACTERS = 
        Pattern.compile("[`~!@%^&*()=+\\|{};:'\",<>/?]"); // NOI18N
    private static final Pattern INVALID_FOLDERNAME_CHARACTERS = 
        Pattern.compile("\\.$|[`~!@%^&*()=+|{};'\",<>?]", Pattern.MULTILINE); // NOI18N

    public TargetChooserPanel(Project project, SourceGroup[] folders , 
            T id ) 
    {
        myChangeSupport = new ChangeSupport( this );
        myFolders = folders;
        myProject = project;
        myId = id;
        
        loadProvider( );
        getProvider().init( this );
    }
    
    public T getId(){
        return myId;
    }
    
    public TargetChooserPanelGUI<T> getComponent() {
        if (myGui == null) {
            myGui = new TargetChooserPanelGUI<T>(this);
        }
        return myGui;
    }
    
    public Project getProject(){
        return myProject;
    }
    
    public SourceGroup[] getSourceGroups(){
        return myFolders;
    }

    public HelpCtx getHelp() {
        return null;
        //return new HelpCtx( this.getClass().getName() +"."+fileType.toString()); //NOI18N
    }
    
    public boolean isValid() {
        return getProvider().isValid( this );
    }

    public boolean checkValid() {
        boolean ok = ( myGui != null && myGui.getTargetName() != null);
        
        if (!ok) {
            myTemplateWizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, null);
            return false;
        }
        
        String filename = myGui.getTargetName();
        if (INVALID_FILENAME_CHARACTERS.matcher(filename).find()) {
                myTemplateWizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, 
                        NbBundle.getMessage(TargetChooserPanel.class, 
                                "MSG_invalid_filename")); // NOI18N
            return false;
        }

        String folderName = myGui.getNormalizedFolder();
        if (INVALID_FOLDERNAME_CHARACTERS.matcher(folderName).find()) {
                myTemplateWizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, 
                        NbBundle.getMessage(TargetChooserPanel.class, 
                                "MSG_invalid_foldername")); // NOI18N
            return false;
        }

        // check if the file name can be created
        String targetName=myGui.getTargetName();
        java.io.File file = myGui.getTargetFile();
        String ext = getProvider().getResultExtension( this );
        
        String errorMessage = canUseFileName (file, myGui.getRelativeTargetFolder(), 
                targetName, ext);
        if (errorMessage!=null) {
            myTemplateWizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, 
                    errorMessage);
        }
        else {
            myTemplateWizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                    myGui.getErrorMessage());
        }
        
        boolean valid = myGui.isPanelValid() && errorMessage == null;

        if (valid && targetName.indexOf('.')>=0) {
            // warning when file name contains dots
            myTemplateWizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                NbBundle.getMessage(TargetChooserPanel.class, 
                        "MSG_dotsInName",targetName+"."+ext));// NOI18N
        }
        return valid;
    }

    public void addChangeListener(ChangeListener listener) {
        myChangeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        myChangeSupport.removeChangeListener(listener);
    }
    
    public TemplateWizard getTemplateWizard() {
        return myTemplateWizard;
    }

    public void readSettings( Object settings ) {
        
        myTemplateWizard = (TemplateWizard)settings;
        
        if ( myGui != null ) {
            myGui.initValues( );
            String wizardTitle = getProvider().getWizardTitle();
            if ( wizardTitle!= null ) {
                if( null == getTemplateWizard().getProperty( "NewFileWizard_Title" ) ) {// NOI18N 
                    getTemplateWizard().putProperty ("NewFileWizard_Title",     // NOI18N 
                        wizardTitle);
                }
            }
            getProvider().readSettings( this );
        }
    }

    @Override
    public void storeSettings(Object settings) {
        if ( WizardDescriptor.PREVIOUS_OPTION.equals(((WizardDescriptor)settings).getValue())
                || WizardDescriptor.CANCEL_OPTION.equals(((WizardDescriptor)settings).getValue())
                || WizardDescriptor.CLOSED_OPTION.equals(((WizardDescriptor)settings).getValue())) {
            return;
        }

        if( isValid() ) {
            File f = new File(myGui.getCreatedFilePath());
            File ff = new File(f.getParentFile().getPath());
            if ( !ff.exists() ) {
                try {
                    FileUtil.createFolder(ff);
                } catch (IOException exc) {
                    LOG.log(Level.INFO, null, exc);
                }
            }
            FileObject folder = FileUtil.toFileObject(ff);                

            Templates.setTargetFolder( (WizardDescriptor)settings, folder );
            Templates.setTargetName( (WizardDescriptor)settings, 
                    myGui.getTargetName() );
            getProvider().storeSettings( this );
        }
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
    }
    
    public void fireChange() {
        myChangeSupport.fireChange();
    }
    
    TargetPanelProvider<T> getProvider(){
        return myProvider;
    }

    private void loadProvider( ) {
        Collection<? extends TargetPanelProvider<T>> providers
                = (Collection<? extends TargetPanelProvider<T>>) Lookup.getDefault().lookupAll(TargetPanelProvider.class);
        for (TargetPanelProvider<T> targetPanelProvider : providers) {
            if (targetPanelProvider.isApplicable(myId)) {
                myProvider = targetPanelProvider;
                break;
            }
        }
        if ( myProvider == null ){
            throw new IllegalStateException("No provider for id '"+myId+
                    "'  is found ");                     //NOI18N 
        }
    }
    
    private String canUseFileName (File dir, String relativePath, 
            String objectName, String extension) 
    {
        String newObjectName=objectName;
        if (extension != null && extension.length () > 0) {
            StringBuilder sb = new StringBuilder ();
            sb.append (objectName);
            sb.append ('.');
            sb.append (extension);
            newObjectName = sb.toString ();
        }
        
        // check file name
        
        if (!checkFileName(objectName)) {
            return NbBundle.getMessage (TargetChooserPanel.class, 
                    "MSG_invalid_filename", newObjectName); // NOI18N
        }
        // test if the directory is correctly specified
        FileObject folder = null;
        if (dir!=null) {
            try {
                 folder = FileUtil.toFileObject(dir);
            } catch(java.lang.IllegalArgumentException ex) {
                 return NbBundle.getMessage (TargetChooserPanel.class, 
                         "MSG_invalid_path", relativePath); // NOI18N
            }
        }
            
        // test whether the selected folder on selected filesystem is read-only or exists
        if (folder!=  null) {
            // target filesystem should be writable
            if (!folder.canWrite ()) {
                return NbBundle.getMessage (TargetChooserPanel.class, 
                        "MSG_fs_is_readonly"); // NOI18N
            }

            if (folder.getFileObject (newObjectName) != null) {
                return NbBundle.getMessage (TargetChooserPanel.class, 
                        "MSG_file_already_exist", newObjectName); // NOI18N
            }

            if (Utilities.isWindows ()) {
                if (checkCaseInsensitiveName (folder, newObjectName)) {
                    return NbBundle.getMessage (TargetChooserPanel.class, 
                            "MSG_file_already_exist", newObjectName); // NOI18N
                }
            }
        }

        // all ok
        return null;
    }
    
    private boolean checkCaseInsensitiveName (FileObject folder, String name) {
        Enumeration<? extends FileObject> children = folder.getChildren (false);
        FileObject fo;
        while (children.hasMoreElements ()) {
            fo = children.nextElement ();
            if (name.equalsIgnoreCase (fo.getName ())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean checkFileName(String str) {
        char c[] = str.toCharArray();
        for (int i=0;i<c.length;i++) {
            if (c[i]=='\\') return false;
            if (c[i]=='/') return false;
        }
        return true;
    }
    
    private ChangeSupport myChangeSupport;
    private TargetChooserPanelGUI<T> myGui;
    
    private T myId;

    private Project myProject;
    private SourceGroup[] myFolders;
    private TemplateWizard myTemplateWizard;
    private TargetPanelProvider<T> myProvider;
}
