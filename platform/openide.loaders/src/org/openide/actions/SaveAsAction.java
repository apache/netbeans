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
package org.openide.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.SaveAsCapable;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to save document under a different file name and/or extension.
 * The action is enabled for editor windows only.
 * 
 * @since 6.3
 * @author S. Aubrecht
 */
final class SaveAsAction extends AbstractAction implements ContextAwareAction {

    private Lookup context;
    private Lookup.Result<SaveAsCapable> lkpInfo;
    private boolean isGlobal = false;
    private boolean isDirty = true;
    private PropertyChangeListener registryListener;
    private LookupListener lookupListener;

    private SaveAsAction() {
        this( Utilities.actionsGlobalContext(), true );
    }
    
    private SaveAsAction( Lookup context, boolean isGlobal ) {
        super( NbBundle.getMessage(DataObject.class, "CTL_SaveAsAction") ); //NOI18N
        this.context = context;
        this.isGlobal = isGlobal;
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        setEnabled( false );
    }
    
    /**
     * Method is called from XML layers to create action instance for the main menu/toolbar.
     * @return Global instance for menu/toolbar
     */
    public static ContextAwareAction create() {
        return new SaveAsAction();
    }
    
    @Override
    public boolean isEnabled() {
        if (isDirty
            || null == changeSupport || !changeSupport.hasListeners("enabled") ) { //NOI18N
            refreshEnabled();
        }
        return super.isEnabled();
    }

    public void actionPerformed(ActionEvent e) {
        refreshListeners();
        Collection<? extends SaveAsCapable> inst = lkpInfo.allInstances();
        if( inst.size() > 0 ) {
            SaveAsCapable saveAs = inst.iterator().next();
            File newFile = getNewFileName();
            if( null != newFile ) {
                //create target folder if necessary    
                FileObject newFolder = null;
                try {
                    File targetFolder = newFile.getParentFile();
                    if( null == targetFolder )
                        throw new IOException(newFile.getAbsolutePath());
                    newFolder = FileUtil.createFolder( targetFolder );
                } catch( IOException ioE ) {
                    NotifyDescriptor error = new NotifyDescriptor( 
                            NbBundle.getMessage(DataObject.class, "MSG_CannotCreateTargetFolder"), //NOI18N
                            NbBundle.getMessage(DataObject.class, "LBL_SaveAsTitle"), //NOI18N
                            NotifyDescriptor.DEFAULT_OPTION,
                            NotifyDescriptor.ERROR_MESSAGE,
                            new Object[] {NotifyDescriptor.OK_OPTION},
                            NotifyDescriptor.OK_OPTION );
                    DialogDisplayer.getDefault().notify( error );
                    return;
                }
                
                try {
                    saveAs.saveAs( newFolder, newFile.getName() );
                } catch( IOException ioE ) {
                    Exceptions.attachLocalizedMessage( ioE,
                            NbBundle.getMessage( DataObject.class, "MSG_SaveAsFailed", // NOI18N
                            newFile.getName (),
                            ioE.getLocalizedMessage () ) );
                    Logger.getLogger( getClass().getName() ).log( Level.SEVERE, null, ioE );
                }
            }
        }
    }
    
    /**
     * Show file 'save as' dialog window to ask user for a new file name.
     * @return File selected by the user or null if no file was selected.
     */
    private File getNewFileName() {
        File newFile = null;
        File currentFile = null;
        FileObject currentFileObject = getCurrentFileObject();
        if( null != currentFileObject ) {
            newFile = FileUtil.toFile( currentFileObject );
            currentFile = newFile;
            if( null == newFile ) {
                newFile = new File( currentFileObject.getNameExt() );
            }
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle( NbBundle.getMessage(DataObject.class, "LBL_SaveAsTitle" ) ); //NOI18N
        chooser.setMultiSelectionEnabled( false );
        if( null != newFile ) {
            chooser.setSelectedFile( newFile );
            FileUtil.preventFileChooserSymlinkTraversal( chooser, newFile.getParentFile() );
        }
        File initialFolder = getInitialFolderFrom( newFile );
        if( null != initialFolder )
            chooser.setCurrentDirectory( initialFolder );
        File origFile = newFile;
        while( true ) {
            if( JFileChooser.APPROVE_OPTION != chooser.showSaveDialog( WindowManager.getDefault().getMainWindow() ) ) {
                return null;
            }
            newFile = chooser.getSelectedFile();
            if( null == newFile )
                break;
            if( newFile.equals( origFile ) ) {
                NotifyDescriptor nd = new NotifyDescriptor(
                        NbBundle.getMessage( DataObject.class, "MSG_SaveAs_SameFileSelected"), //NOI18N
                        NbBundle.getMessage( DataObject.class, "MSG_SaveAs_SameFileSelected_Title"), //NOI18N
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.INFORMATION_MESSAGE,
                        new Object[] { NotifyDescriptor.OK_OPTION }, NotifyDescriptor.OK_OPTION );
                DialogDisplayer.getDefault().notify( nd );
            } else if (newFile.exists()) {
                NotifyDescriptor nd = new NotifyDescriptor(
                        NbBundle.getMessage( DataObject.class, "MSG_SaveAs_OverwriteQuestion", newFile.getName()), //NOI18N
                        NbBundle.getMessage( DataObject.class, "MSG_SaveAs_OverwriteQuestion_Title"), //NOI18N
                        NotifyDescriptor.YES_NO_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE,
                        new Object[] { NotifyDescriptor.NO_OPTION, NotifyDescriptor.YES_OPTION }, NotifyDescriptor.NO_OPTION );
                if (NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify( nd ))
                    break;
            } else {
                break;
            }
        }
        if( isFromUserDir(currentFile) ) {
            File lastUsedDir = chooser.getCurrentDirectory();
            NbPreferences.forModule(SaveAction.class).put("lastUsedDir", lastUsedDir.getAbsolutePath()); //NOI18N
        }

        return newFile;
    }
    
    private FileObject getCurrentFileObject() {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if( null != tc ) {
            DataObject dob = tc.getLookup().lookup( DataObject.class );
            if( null != dob )
                return dob.getPrimaryFile();
        }
        return null;
    }

    /**
     * @param newFile File being 'saved as'
     * @return Initial folder selected in file chooser. If the file is in netbeans
     * user dir then user's os-dependent home dir or last used folder will be used
     * instead of file's parent folder.
     */
    private File getInitialFolderFrom(File newFile) {
        File res = new File(System.getProperty("user.home")); //NOI18N
        if( null != newFile ) {
            File parent = newFile.getParentFile();
            if( isFromUserDir(parent) ) {
                String strLastUsedDir = NbPreferences.forModule(SaveAction.class).get("lastUsedDir", res.getAbsolutePath()); //NOI18N
                res = new File(strLastUsedDir);
                if( !res.exists() || !res.isDirectory() ) {
                    res = new File(System.getProperty("user.home")); //NOI18N
                }
            } else {
                res = parent;
            }
        }
        return res;
    }

    /**
     * @param file
     * @return True if given file is netbeans user dir.
     */
    private boolean isFromUserDir( File file ) {
        if( null == file )
            return false;
        File nbUserDir = new File(System.getProperty("netbeans.user")); //NOI18N
        return file.getAbsolutePath().startsWith(nbUserDir.getAbsolutePath());
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new SaveAsAction( actionContext, false );
    }
    
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        refreshListeners();
    }
    
    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        super.removePropertyChangeListener(listener);
        Mutex.EVENT.readAccess(new Runnable() { // might be called off EQ by WeakListeners
            public @Override void run() {
                refreshListeners();
            }
        });
    }
    
    private PropertyChangeListener createRegistryListener() {
        return WeakListeners.propertyChange( new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    isDirty = true;
                }
            }, TopComponent.getRegistry() );
    }
    
    private LookupListener createLookupListener() {
        return WeakListeners.create(LookupListener.class, new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    isDirty = true;
                }
            }, lkpInfo);
    }

    private void refreshEnabled() {
        if (lkpInfo == null) {
            //The thing we want to listen for the presence or absence of
            //on the global selection
            Lookup.Template<SaveAsCapable> tpl = new Lookup.Template<SaveAsCapable>(SaveAsCapable.class);
            lkpInfo = context.lookup (tpl);
        }
        
        TopComponent tc = TopComponent.getRegistry().getActivated();
        boolean isEditorWindowActivated = null != tc && WindowManager.getDefault().isEditorTopComponent(tc);
        setEnabled(null != lkpInfo && lkpInfo.allItems().size() != 0 && isEditorWindowActivated);
        isDirty = false;
    }
    
    private void refreshListeners() {
        assert SwingUtilities.isEventDispatchThread() 
               : "this shall be called just from AWT thread";

        if (lkpInfo == null) {
            //The thing we want to listen for the presence or absence of
            //on the global selection
            Lookup.Template<SaveAsCapable> tpl = new Lookup.Template<SaveAsCapable>(SaveAsCapable.class);
            lkpInfo = context.lookup (tpl);
        }
        
        if( null == changeSupport || !changeSupport.hasListeners("enabled") ) { //NOI18N
            if( isGlobal && null != registryListener ) {
                TopComponent.getRegistry().removePropertyChangeListener( registryListener );
                registryListener = null;
            }
            if( null != lookupListener ) {
                lkpInfo.removeLookupListener(lookupListener);
                lookupListener = null;
            }
        } else {
            if( null == registryListener ) {
                registryListener = createRegistryListener();
                TopComponent.getRegistry().addPropertyChangeListener( registryListener );
            }

            if( null == lookupListener ) {
                lookupListener = createLookupListener();
                lkpInfo.addLookupListener( lookupListener );
            }
            refreshEnabled();
        }
    }
    
    //for unit testing
    boolean _isEnabled() {
        return super.isEnabled();
    }
}

