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

package org.netbeans.modules.tasklist.projectint;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 * Task scanning scope for all opened projects.
 * 
 * @author S. Aubrecht
 */
public class OpenedProjectsScanningScope extends TaskScanningScope 
        implements PropertyChangeListener {
    
    private Callback callback;
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup;
    private Project[] currentProjects;
    
    /** Creates a new instance of OpenedProjectsScanningScope 
     * @param displayName 
     * @param description
     * @param icon 
     */
    private OpenedProjectsScanningScope( String displayName, String description, Image icon ) {
        super( displayName, description, icon );
        Map<String,String> labels = new HashMap<String,String>(1);
        labels.put( Utils.KEY_STATUS_BAR_LABEL, 
                NbBundle.getMessage(OpenedProjectsScanningScope.class, "LBL_OpenedProjectsStatusBar") ); //NOI18N
        lookupContent.add( labels );
    }
        
    /**
     * @return New instance of OpenedProjectsScanningScope
     */
    public static OpenedProjectsScanningScope create() {
        return new OpenedProjectsScanningScope( 
                NbBundle.getBundle( MainProjectScanningScope.class ).getString( "LBL_OpenedProjectsScope" ), //NOI18N
                NbBundle.getBundle( MainProjectScanningScope.class ).getString( "HINT_OpenedProjectsScope" ), //NOI18N
                ImageUtilities.loadImage( "org/netbeans/modules/tasklist/projectint/opened_projects_scope.png" ) //NOI18N
                );
    }
    
    public Iterator<FileObject> iterator() {
        return new OpenedProjectsIterator();
    }
    
    @Override
    public boolean isInScope( FileObject resource ) {
        if( null == resource || null == currentProjects )
            return false;
        for( Project p : currentProjects ) {
            Sources sources = ProjectUtils.getSources( p );
            SourceGroup[] groups = sources.getSourceGroups( Sources.TYPE_GENERIC );
            for( SourceGroup group : groups ) {
                FileObject rootFolder = group.getRootFolder();
                if( FileUtil.isParentOf( rootFolder, resource ) || rootFolder.equals( resource ) )
                    return true;
            }
        }
        
        return false;
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
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        synchronized( this ) {
            if( null != newCallback && null == callback ) {
                OpenProjects.getDefault().addPropertyChangeListener( this );
                TopComponent.getRegistry().addPropertyChangeListener( this );
                setLookupContent( openProjects );
            } else if( null == newCallback && null != callback ) {
                OpenProjects.getDefault().removePropertyChangeListener( this );
                TopComponent.getRegistry().removePropertyChangeListener( this );
                setLookupContent( null );
            }
            this.callback = newCallback;
        }
    }
    
    public void propertyChange( PropertyChangeEvent e ) {
        if( OpenProjects.PROPERTY_OPEN_PROJECTS.equals( e.getPropertyName() ) ) {
            Project[] projects = OpenProjects.getDefault().getOpenProjects();
            synchronized( this ) {
                if( null != callback ) {
                    setLookupContent( projects );
                    callback.refresh();
                }
            }
        }
    }
    
    private void setLookupContent( Project[] newProjects ) {
        if( null != currentProjects ) {
            for( Project p : currentProjects ) {
                lookupContent.remove( p );
            }
        }
        if( null != newProjects ) {
            for( Project p : newProjects ) {
                lookupContent.add( p );
            }
        }
        currentProjects = newProjects;
    }
}
