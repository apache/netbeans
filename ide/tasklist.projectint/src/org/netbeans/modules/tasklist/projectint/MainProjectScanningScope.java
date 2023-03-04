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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.AbstractAction;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 * Task scanning scope for the selected project.
 * 
 * @author S. Aubrecht
 */
public class MainProjectScanningScope extends TaskScanningScope 
        implements PropertyChangeListener {

    private Callback callback;
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup;
    private Project currentProject;
    private Map<String,String> scopeLabels = Collections.synchronizedMap( new HashMap<String, String>(3) );
    private Timer refreshTimer;
    
    private MainProjectScanningScope( String displayName, String description, Image icon ) {
        super( displayName, description, icon, true );
        extractLabelsFromProject(null, scopeLabels);
        lookupContent.add(scopeLabels);
    }
    
    /**
     * @return New instance of MainProjectScanningScope
     */
    public static MainProjectScanningScope create() {
        return new MainProjectScanningScope(
                NbBundle.getBundle( MainProjectScanningScope.class ).getString( "LBL_MainProjectScope" ), //NOI18N
                NbBundle.getBundle( MainProjectScanningScope.class ).getString( "HINT_MainProjectScope" ), //NOI18N
                ImageUtilities.loadImage( "org/netbeans/modules/tasklist/projectint/main_project_scope.png" ) //NOI18N
                );
    }
    
    public Iterator<FileObject> iterator() {
        return new MainProjectIterator();
    }
    
    @Override
    public boolean isInScope( FileObject resource ) {
        Project p = null;
        synchronized( this ) {
            p = currentProject;
        }

        if( null == resource || null == p )
            return false;
        
        Project owner = FileOwnerQuery.getOwner( resource );
        if( null == owner )
            return false;
        
        if( owner.equals( p ) )
            return true;
        
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
        synchronized( this ) {
            if( null != newCallback && null == callback ) {
                OpenProjects.getDefault().addPropertyChangeListener( this );
                TopComponent.getRegistry().addPropertyChangeListener( this );

                Project p = findCurrentProject();
                setCurrentProject(p, false);
            } else if( null == newCallback && null != callback ) {
                OpenProjects.getDefault().removePropertyChangeListener( this );
                TopComponent.getRegistry().removePropertyChangeListener( this );
                setCurrentProject(null, false);
            }
            this.callback = newCallback;
        }
    }
    
    public void propertyChange( PropertyChangeEvent e ) {
        if( TopComponent.Registry.PROP_ACTIVATED_NODES.equals( e.getPropertyName() ) ) {
            //start timer to switch current project
            if( null != refreshTimer )
                refreshTimer.cancel();
            refreshTimer = new Timer();
            refreshTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    Project p = findCurrentProject();
                        setCurrentProject( p, true );
                }
            }, 500);
        }
    }
    
    private void setCurrentProject( Project newProject, boolean callbackRefresh ) {
        synchronized( this ) {
            if( null == newProject && null == currentProject 
             || (null != currentProject && currentProject.equals(newProject)) ) {
                return;
            }
            if( null != currentProject ) {
                lookupContent.remove( currentProject );
            }
            if( null != newProject ) {
                lookupContent.add( newProject );
            }
            extractLabelsFromProject( newProject, scopeLabels );
            currentProject = newProject;
        }
        
        if( callbackRefresh && null != callback )
            callback.refresh();
    }
    
    private void extractLabelsFromProject(Project p, Map<String, String> labels) {
        labels.clear();
        if( null == p ) {
            labels.put( Utils.KEY_STATUS_BAR_LABEL, 
                    NbBundle.getMessage(MainProjectScanningScope.class, "LBL_NoProjectStatusBar") ); //NOI18N
        } else {
            ProjectInformation pi = ProjectUtils.getInformation(p);
                labels.put(AbstractAction.SHORT_DESCRIPTION, NbBundle.getMessage(MainProjectScanningScope.class, 
                        "HINT_CurrentProjectScope", pi.getDisplayName()) ); //NOI18N
                labels.put(AbstractAction.NAME, pi.getDisplayName());
                labels.put( Utils.KEY_STATUS_BAR_LABEL, 
                        NbBundle.getMessage(MainProjectScanningScope.class, "LBL_CurrentProjectStatusBar", pi.getDisplayName()) ); //NOI18N
        }
    }
    
    static Project findCurrentProject() {
        Set<Project> result = new HashSet<Project>();
        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        for( Node n : nodes ) {
            for( Project p : n.getLookup().lookupAll(Project.class) ) {
                result.add(p);
                if( result.size() > 1 )
                    return null;
            }
            for( DataObject dob : n.getLookup().lookupAll(DataObject.class) ) {
                FileObject fob = dob.getPrimaryFile();
                Project p = FileOwnerQuery.getOwner(fob);
                if ( p != null ) {
                    result.add( p );
                    if( result.size() > 1 )
                        return null;
                }
            }
        }
        return result.isEmpty() ? null : new ArrayList<Project>(result).get(0);
    }
    
}
