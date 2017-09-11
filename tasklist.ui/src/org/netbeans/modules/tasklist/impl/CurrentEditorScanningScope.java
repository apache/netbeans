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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
