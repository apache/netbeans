/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.core.multitabs.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    private static final Map<FileObject, Project> file2project = new WeakHashMap<FileObject, Project>(50);
    private static final RequestProcessor RP = new RequestProcessor("TabProjectBridge"); //NOI18N
    private static final Set<FileObject> currentQueries = new HashSet<FileObject>(20);
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
                projectsListener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        changeSupport.fireChange();
                    }
                };
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
        synchronized( file2project ) {
            if( null != tab && tab.getComponent() instanceof TopComponent ) {
                TopComponent tc = ( TopComponent ) tab.getComponent();
                DataObject dob = tc.getLookup().lookup( DataObject.class );
                if( null != dob ) {
                    final FileObject fo = dob.getPrimaryFile();
                    if( null != fo ) {
                        p = file2project.get(fo);
                        if( null == p ) {
                            if( currentQueries.contains(fo) ) {
                                //there already is a file owner query for this file
                                return null;
                            } else {
                                currentQueries.add(fo);
                                RP.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Project p = FileOwnerQuery.getOwner( fo );
                                        if( null != p ) {
                                            synchronized( file2project ) {
                                                file2project.put( fo, p );
                                                currentQueries.remove(fo);
                                                changeSupport.fireChange();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
            return null == p ? null : createProxy( p );
        }
    }

    private static ProjectProxy createProxy( Project p ) {
        ProjectInformation info = ProjectUtils.getInformation( p );
        FileObject projectDir = p.getProjectDirectory();
        return new ProjectProxy( p, info.getDisplayName(), projectDir.getPath() );
    }
}
