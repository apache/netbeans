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
package org.netbeans.core.multitabs.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.core.multitabs.impl.ProjectSupport;
import org.netbeans.swing.tabcontrol.TabData;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author stan
 */
@ServiceProvider(service = ProjectSupport.class)
public class ProjectSupportImpl extends ProjectSupport {

    private static final Map<FileObject, Project> file2project = new WeakHashMap<>(50);
    private static final RequestProcessor RP = new RequestProcessor("TabProjectBridge"); //NOI18N
    private static final ChangeSupport changeSupport = new ChangeSupport(RP);
    private static PropertyChangeListener projectsListener;

    public ProjectSupportImpl() {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void addChangeListener( ChangeListener l ) {
        synchronized( changeSupport ) {
            changeSupport.addChangeListener(l);
            if( null == projectsListener ) {
                projectsListener = (PropertyChangeEvent evt) -> changeSupport.fireChange();
                OpenProjects.getDefault().addPropertyChangeListener( projectsListener );
            }
        }
    }

    @Override
    public void removeChangeListener( ChangeListener l ) {
        synchronized( changeSupport ) {
            changeSupport.removeChangeListener(l);
            if( !changeSupport.hasListeners() && null != projectsListener ) {
                OpenProjects.getDefault().removePropertyChangeListener( projectsListener );
                projectsListener = null;
            }
        }
    }

    @Override
    public ProjectProxy[] getOpenProjects() {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        ProjectProxy[] res = new ProjectProxy[projects.length];
        for( int i=0; i<projects.length; i++ ) {
            Project p = projects[i];
            ProjectProxy proxy = createProxy( p );
            res[i] = proxy;
        }
        return res;
    }

    @Override
    public ProjectProxy getProjectForTab( final TabData tab ) {
        Project p = null;
        if( null != tab && tab.getComponent() instanceof TopComponent ) {
            TopComponent tc = ( TopComponent ) tab.getComponent();
            DataObject dob = tc.getLookup().lookup( DataObject.class );
            if( null != dob ) {
                final FileObject fo = dob.getPrimaryFile();
                if( null != fo ) {
                    synchronized( file2project ) {
                        p = file2project.get(fo);
                        if( null == p ) {
                            p = FileOwnerQuery.getOwner( fo );
                            if (null != p) {
                                file2project.put(fo, p);
                            }
                        }
                    }
                }
            }
        }
        return null == p ? null : createProxy( p );
    }

    private static ProjectProxy createProxy( Project p ) {
        ProjectInformation info = ProjectUtils.getInformation( p );
        FileObject projectDir = p.getProjectDirectory();
        return new ProjectProxy( p, info.getDisplayName(), projectDir.getPath() );
    }
}
