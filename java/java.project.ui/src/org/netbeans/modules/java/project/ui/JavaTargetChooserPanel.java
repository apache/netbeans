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

package org.netbeans.modules.java.project.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.project.ui.NewJavaFileWizardIterator.Type;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import static org.netbeans.modules.java.project.ui.Bundle.*;

/**
 * @author  Petr Hrebejk
 */
public final class JavaTargetChooserPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    private static final String FOLDER_TO_DELETE = "folderToDelete";    //NOI18N

    private final SpecificationVersion JDK_14 = new SpecificationVersion ("1.4");   //NOI18N
    private final SpecificationVersion JDK_18 = new SpecificationVersion ("1.8");   //NOI18N
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private JavaTargetChooserPanelGUI gui;
    private WizardDescriptor.Panel<WizardDescriptor> bottomPanel;
    private WizardDescriptor wizard;

    private Project project;
    private SourceGroup folders[];
    private final Type type;
    private boolean isValidPackageRequired;

    public JavaTargetChooserPanel(Project project, SourceGroup folders[], WizardDescriptor.Panel<WizardDescriptor> bottomPanel, Type type, boolean isValidPackageRequired) {
        this.project = project;
        this.folders = folders;
        this.bottomPanel = bottomPanel;
        this.type = type;
        if ( bottomPanel != null ) {
            bottomPanel.addChangeListener( this );
        }
        this.isValidPackageRequired = isValidPackageRequired;
    }

    @Override
    public Component getComponent() {
        if (gui == null) {
            gui = new JavaTargetChooserPanelGUI( project, folders, bottomPanel == null ? null : bottomPanel.getComponent(), type );
            gui.addChangeListener(this);
        }
        return gui;
    }

    @Override
    public HelpCtx getHelp() {
        return bottomPanel != null ?
                bottomPanel.getHelp() :
                null;
    }

    @Messages("ERR_JavaTargetChooser_WrongPlatform=Wrong source level of the project. You will not be able to compile this file since it contains JDK {0} features.")
    @Override
    public boolean isValid() {              
        if (gui == null) {
           setErrorMessage( null );
           return false;
        }        
        if (type == Type.PACKAGE) {
            if (gui.getTargetName() == null) {
                setErrorMessage("INFO_JavaTargetChooser_ProvidePackageName");
                return false;
            }
            if ( !isValidPackageName( gui.getTargetName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" );
                return false;
            }
            if (!isValidPackage(gui.getRootFolder(), gui.getTargetName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidFolder");
                return false;
            }
        } else if (type == Type.PKG_INFO) {
            //Change in firing order caused that isValid is called before readSettings completed => no targetName available
            if (gui.getTargetName() == null) {
                setErrorMessage("INFO_JavaTargetChooser_ProvideClassName");
                return false;
            }
            assert "package-info".equals( gui.getTargetName() );        //NOI18N
            if ( !isValidPackageName( gui.getPackageName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" );
                return false;
            }
            if (!isValidPackage(gui.getRootFolder(), gui.getPackageName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidFolder");
                return false;
            }
        } else if (type == Type.MODULE_INFO) {
            //Change in firing order caused that isValid is called before readSettings completed => no targetName available
            if (gui.getTargetName() == null) {
                setErrorMessage("INFO_JavaTargetChooser_ProvideClassName");
                return false;
            }
            assert "module-info".equals( gui.getTargetName() );        //NOI18N
            if ( !isValidPackageName( gui.getPackageName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" );
                return false;
            }
            if (!isValidPackage(gui.getRootFolder(), gui.getPackageName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidFolder");
                return false;
            }
        } else {
            if (gui.getTargetName() == null) {
                setErrorMessage("INFO_JavaTargetChooser_ProvideClassName");
                return false;
            } 
            else if ( !isValidTypeIdentifier( gui.getTargetName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidClass" );
                return false;
            }
            else if ( !isValidPackageName( gui.getPackageName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" );
                return false;
            }
            if (!isValidPackage(gui.getRootFolder(), gui.getPackageName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidFolder");
                return false;
            }
        }
        
        // check if the file name can be created
        FileObject template = Templates.getTemplate( wizard );

        boolean returnValue=true;
        FileObject rootFolder = gui.getRootFolder();
        SpecificationVersion specVersion = null;
        if (type != Type.PACKAGE) {
            String sl = SourceLevelQuery.getSourceLevel(rootFolder);
            specVersion = sl != null? new SpecificationVersion(sl): null;
        }
        String errorMessage = canUseFileName (rootFolder, gui.getPackageFileName(), gui.getTargetName(), template.getExt ());        
        if (gui != null) {
            wizard.getNotificationLineSupport ().setErrorMessage (errorMessage);
        }
        if (errorMessage!=null) returnValue=false;                
        
        if (type != Type.PACKAGE && type != Type.MODULE_INFO && returnValue && gui.getPackageName().length() == 0 && specVersion != null && JDK_14.compareTo(specVersion) <= 0) {
            if(isValidPackageRequired){
                setInfoMessage( "ERR_JavaTargetChooser_CantUseDefaultPackage" );
                return false;
            }
            //Only warning, display it only if everything else is OK.
            setErrorMessage( "ERR_JavaTargetChooser_DefaultPackage" );            
        }
        String categories = (String) template.getAttribute("templateCategory"); // NOI18N
        if (specVersion != null && categories != null) {
            List<String> catList = Arrays.asList(categories.split(","));
            if (catList.contains(NewJavaFileWizardIterator.JDK_5) && specVersion.compareTo(JDK_14) <= 0) {
                //Only warning, display it only if everything else id OK.
                wizard.getNotificationLineSupport().setErrorMessage(ERR_JavaTargetChooser_WrongPlatform(5));
            } else if (catList.contains(NewJavaFileWizardIterator.JDK_9) && specVersion.compareTo(JDK_18) <= 0) {
                //Only warning, display it only if everything else id OK.
                wizard.getNotificationLineSupport().setErrorMessage(ERR_JavaTargetChooser_WrongPlatform(9));
            }
        }
        
        // this enables to display error messages from the bottom panel
        // Nevertheless, the previous error messages have bigger priorities 
        if (returnValue && bottomPanel != null) {
           if (!bottomPanel.isValid())
               return false;
        }
        
        return returnValue;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator<ChangeListener> it = listeners.iterator();
        while (it.hasNext()) {
            it.next().stateChanged(e);
        }
    }

    @Override
    public void readSettings(WizardDescriptor wizard) {
        this.wizard = wizard;
        if ( gui != null ) {
            // Try to preselect a folder
            FileObject preselectedFolder = Templates.getTargetFolder( wizard );            
            // Init values
            gui.initValues( Templates.getTemplate( wizard ), preselectedFolder );
        }
        
        if ( bottomPanel != null ) {
            bottomPanel.readSettings(wizard);
        }        
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        if (gui != null) {
            Object substitute = gui.getClientProperty ("NewFileWizard_Title"); // NOI18N
            if (substitute != null) {
                wizard.putProperty ("NewFileWizard_Title", substitute); // NOI18N
            }
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wizard) { 
        Object value = wizard.getValue();
        if (WizardDescriptor.PREVIOUS_OPTION.equals(value) || WizardDescriptor.CANCEL_OPTION.equals(value) ||
                WizardDescriptor.CLOSED_OPTION.equals(value)) {
            return;
        }
        if( isValid() ) {
            if ( bottomPanel != null ) {
                bottomPanel.storeSettings( wizard );
            }
            Templates.setTargetFolder(wizard, getTargetFolderFromGUI(wizard));
            Templates.setTargetName(wizard, gui.getTargetName());
        }        
        if (WizardDescriptor.FINISH_OPTION.equals(value)) {
            wizard.putProperty("NewFileWizard_Title", null); // NOI18N
            wizard.putProperty(FOLDER_TO_DELETE, null);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }
    
    // Private methods ---------------------------------------------------------
    
    private void setErrorMessage( String key ) {
        if ( key == null ) {
            wizard.getNotificationLineSupport().clearMessages();
        } else {
            wizard.getNotificationLineSupport().setErrorMessage(NbBundle.getMessage(JavaTargetChooserPanelGUI.class, key));
        }
    }
    
    private void setInfoMessage(String key) {
        if (key == null) {
            wizard.getNotificationLineSupport().clearMessages();
        } else {
            wizard.getNotificationLineSupport().setInformationMessage (NbBundle.getMessage(JavaTargetChooserPanelGUI.class, key));
        }
    }
    
    private FileObject getTargetFolderFromGUI (WizardDescriptor wd) {
        assert gui != null;
        FileObject rootFolder = gui.getRootFolder();
        if (!rootFolder.isValid()) {
            return null;
        }
        FileObject folder = null;
        if (type != Type.PACKAGE) {
            String packageFileName = gui.getPackageFileName();
            folder = rootFolder.getFileObject( packageFileName );
            if ( folder == null ) {
                try {
                    folder = rootFolder;
                    StringTokenizer tk = new StringTokenizer (packageFileName,"/"); //NOI18N
                    String name = null;
                    while (tk.hasMoreTokens()) {
                        name = tk.nextToken();
                        FileObject fo = folder.getFileObject (name,"");   //NOI18N
                        if (fo == null) {
                            break;
                        }
                        folder = fo;
                    }
                    folder = folder.createFolder(name);
                    FileObject toDelete = (FileObject) wd.getProperty(FOLDER_TO_DELETE);
                    if (toDelete == null) {
                        wd.putProperty(FOLDER_TO_DELETE,folder);
                    }
                    else if (!toDelete.equals(folder)) {
                        toDelete.delete();
                        wd.putProperty(FOLDER_TO_DELETE,folder);
                    }
                    while (tk.hasMoreTokens()) {
                        name = tk.nextToken();
                        folder = folder.createFolder(name);
                    }

                }
                catch( IOException e ) {
                    Exceptions.printStackTrace(e);
                    folder = null;
                }
            }
        }
        else {
            folder = rootFolder;
        }
        return folder;
    }
    
    // Nice copy of useful methods (Taken from JavaModule)
    
    static boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(str, ".");
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token))
                return false;
            if("snippet-files".equals(token) && !tukac.hasMoreTokens())
                return true;
            if (!Utilities.isJavaIdentifier(token))
                return false;
        }
        return true;
    }
    
    private static boolean isValidPackage (FileObject root, final String path) {
        //May be null when nothing selected in the GUI.
        if (root == null) {
            return false;
        }
        if (path == null) {
            return false;
        }
        final StringTokenizer tk = new StringTokenizer(path,".");   //NOI18N
        while (tk.hasMoreTokens()) {
            root = root.getFileObject(tk.nextToken());
            if (root == null) {
                return true;
            }
            else if (root.isData()) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    static String[] getPackageAndSimpleName(@NonNull final String name) {
        final int lastDot = name.lastIndexOf('.');  //NOI18N
        if (lastDot > 0) {
            return new String[] {
                name.substring(0, lastDot),
                lastDot == name.length() - 1 ?
                    "" :    //NOI18N
                    name.substring(lastDot+1)
            };
        } else {
            return new String[] {
                "",             //NOI18N
                name
            };
        }        
    }

    private static boolean isValidTypeIdentifier(String ident) {
        return ident != null && !"".equals(ident) && Utilities.isJavaIdentifier( ident );  //NOI18N
    }
    
    // helper methods copied from project/ui/ProjectUtilities
    /** Checks if the given file name can be created in the target folder.
     *
     * @param targetFolder target folder (e.g. source group)
     * @param folderName name of the folder relative to target folder
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @return localized error message or null if all right
     */    
    public static String canUseFileName (FileObject targetFolder, String folderName, String newObjectName, String extension) {
        String newObjectNameToDisplay = newObjectName;
        if (newObjectName != null) {
            newObjectName = newObjectName.replace ('.', '/'); // NOI18N
        }
        if (extension != null && extension.length () > 0) {
            StringBuilder sb = new StringBuilder ();
            sb.append (newObjectName);
            sb.append ('.'); // NOI18N
            sb.append (extension);
            newObjectName = sb.toString ();
        }
        
        if (extension != null && extension.length () > 0) {
            StringBuilder sb = new StringBuilder ();
            sb.append (newObjectNameToDisplay);
            sb.append ('.'); // NOI18N
            sb.append (extension);
            newObjectNameToDisplay = sb.toString ();
        }
        
        String relFileName = folderName + "/" + newObjectName; // NOI18N

        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null || !targetFolder.isValid()) {
            return NbBundle.getMessage (JavaTargetChooserPanel.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }
        
        // target package should be writable
        File targetPackage = folderName != null ? new File (FileUtil.toFile (targetFolder), folderName) : FileUtil.toFile (targetFolder);
        if (targetPackage != null) {
            if (targetPackage.exists () && ! targetPackage.canWrite ()) {
                return NbBundle.getMessage (JavaTargetChooserPanel.class, "MSG_fs_is_readonly"); // NOI18N
            }
        } else if (! targetFolder.canWrite ()) {
            return NbBundle.getMessage (JavaTargetChooserPanel.class, "MSG_fs_is_readonly"); // NOI18N
        }
        
        if (existFileName(targetFolder, relFileName)) {
            return NbBundle.getMessage (JavaTargetChooserPanel.class, "MSG_file_already_exist", newObjectNameToDisplay); // NOI18N
        }
        
        // all ok
        return null;
    }

    private static boolean existFileName(FileObject targetFolder, String relFileName) {        
        File fileForTargetFolder = FileUtil.toFile(targetFolder);
        return fileForTargetFolder.exists() ?
                new File (fileForTargetFolder, relFileName).exists() :
                targetFolder.getFileObject (relFileName) != null;
    }
}
