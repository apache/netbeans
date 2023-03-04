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

package org.netbeans.modules.tasklist.impl;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Default implementtion of TaskScanningScope. The scope is the currently edited file.
 * 
 * @author S. Aubrecht
 */
public class CurrentEditorScanningScope extends TaskScanningScope 
        implements PropertyChangeListener, Runnable {
    
    private FileObject currentFile = null;
    private Callback callback;
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup;
    
    /** Creates a new instance of CurrentEditorScope */
    public CurrentEditorScanningScope( String displayName, String description, Image icon ) {
        super( displayName, description, icon );
        Map<String,String> labels = new HashMap<String,String>(1);
        labels.put( "StatusBarLabel", //NOI18N
                NbBundle.getMessage(CurrentEditorScanningScope.class, "LBL_CurrentFileStatusMessage") ); //NOI18N
        lookupContent.add( labels );
    }
    
    public static CurrentEditorScanningScope create() {
        return new CurrentEditorScanningScope(
                NbBundle.getBundle( CurrentEditorScanningScope.class ).getString( "LBL_CurrentEditorScope" ), //NOI18N)
                NbBundle.getBundle( CurrentEditorScanningScope.class ).getString( "HINT_CurrentEditorScope" ), //NOI18N
                ImageUtilities.loadImage( "org/netbeans/modules/tasklist/ui/resources/cur_editor_scope.png" ) //NOI18N
                );
    }
    
    public Iterator<FileObject> iterator() {
        ArrayList<FileObject> list = new ArrayList<FileObject>( 1 );
        if( null != currentFile )
            list.add( currentFile );
        return list.iterator();
    }
    
    @Override
    public boolean isInScope( FileObject resource ) {
        if( null == resource )
            return false;
        return null != currentFile && currentFile.equals( resource );
    }
    
    public Lookup getLookup() {
        synchronized( this ) {
            if( null == lookup ) {
                lookup = new AbstractLookup( lookupContent );
            }
        }
        return lookup;
    }
    
    public void attach( Callback newCallback ) {
        if( null != newCallback && null == callback ) {
            WindowManager.getDefault().getRegistry().addPropertyChangeListener( this );
        } else if( null == newCallback && null != callback ) {
            WindowManager.getDefault().getRegistry().removePropertyChangeListener( this );
            if (null != currentFile) {
                lookupContent.remove(currentFile);
            }
            currentFile = null;
        }
        if( null != newCallback && newCallback != this.callback ) {
            this.callback = newCallback;
            if( SwingUtilities.isEventDispatchThread() ) {
                run();
            } else {
                SwingUtilities.invokeLater( this );
            }
        }
        this.callback = newCallback;
    }
    
    public void propertyChange( PropertyChangeEvent e ) {
        if( TopComponent.Registry.PROP_ACTIVATED_NODES.equals( e.getPropertyName() )
            || TopComponent.Registry.PROP_OPENED.equals( e.getPropertyName() )
            || TopComponent.Registry.PROP_ACTIVATED.equals( e.getPropertyName() ) ) {
            
            run();
        }
    }
    
    public void run() {
        switchCurrentFile(true);
    }
    
    private void switchCurrentFile( boolean callbackRefresh ) {
        FileObject newActiveFile = getCurrentFile();
        if( (null == currentFile && null != newActiveFile)
            || (null != currentFile && null == newActiveFile )
            || (null != currentFile && null != newActiveFile 
                && !currentFile.equals(newActiveFile)) ) {

            if( null != currentFile )
                lookupContent.remove( currentFile );
            if( null != newActiveFile )
                lookupContent.add( newActiveFile );
            currentFile = newActiveFile;
            //notify the TaskManager that user activated other file
            if( null != callback && callbackRefresh )
                callback.refresh();
        } else {
            currentFile = newActiveFile;
        }
    }
    
    private FileObject getCurrentFile() {
        TopComponent.Registry registry = TopComponent.getRegistry();
        
        TopComponent activeTc = registry.getActivated();
        FileObject newFile = getFileFromTopComponent( activeTc );
        
        ArrayList<FileObject> availableFiles = new ArrayList<FileObject>(3);
        if( null == newFile ) {
            Collection<TopComponent> openedTcs = new ArrayList<TopComponent>( registry.getOpened());
            for( Iterator i=openedTcs.iterator(); i.hasNext(); ) {
                TopComponent tc = (TopComponent)i.next();
                
                FileObject file = getFileFromTopComponent( tc );
                if( null != file ) {
                    availableFiles.add( file );
                }
            }
            if( null != currentFile && (availableFiles.contains( currentFile ) ) )
                newFile = currentFile;
            else if( availableFiles.size() > 0 )
                newFile = availableFiles.get( 0 );
        }
        return newFile;
    }
    
    private FileObject getFileFromTopComponent( final TopComponent tc ) {
        if( null == tc || !tc.isShowing() )
            return null;
        if( WindowManager.getDefault().isOpenedEditorTopComponent( tc ) ) {
            DataObject dob = tc.getLookup().lookup( DataObject.class );
            if( null != dob ) {
                return dob.getPrimaryFile();
            }
        }
        return null;
    }
}
