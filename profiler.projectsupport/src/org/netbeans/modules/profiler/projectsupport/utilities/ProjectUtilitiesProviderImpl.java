/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.projectsupport.utilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.profiler.spi.ProjectUtilitiesProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup.Provider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
@ServiceProvider(service = ProjectUtilitiesProvider.class)
public class ProjectUtilitiesProviderImpl extends ProjectUtilitiesProvider {
    
    private Set<ChangeListener> listeners;

    @Override
    public Icon getIcon(Provider project) {
        return ProjectUtilities.getProjectIcon((Project)project);
    }

    @Override
    public Provider getMainProject() {
        return ProjectUtilities.getMainProject();
    }

    @Override
    public String getDisplayName(Provider project) {
        return ProjectUtilities.getProjectName((Project)project);
    }

    @Override
    public FileObject getProjectDirectory(Provider project) {
        return ((Project)project).getProjectDirectory();
    }

    @Override
    public Provider[] getOpenedProjects() {
        return ProjectUtilities.getOpenedProjects();
    }
    
    @Override
    public boolean hasSubprojects(Provider project) {
        return ProjectUtilities.hasSubprojects((Project)project);
    }

    @Override
    public void fetchSubprojects(Provider project, Set<Provider> subprojects) {
        ProjectUtilities.fetchSubprojects((Project)project, (Set)subprojects);
    }
    
    @Override
    public Provider getProject(FileObject fobj) {
        return FileOwnerQuery.getOwner(fobj);
    }
    
    /**
     * Adds a listener to be notified when set of open projects changes.
     * @param listener listener to be added
     */
    @Override
    public synchronized void addOpenProjectsListener(ChangeListener listener) {
        listeners().add(listener);
    }
    
    /**
     * Removes a listener to be notified when set of open projects changes.
     * @param listener listener to be removed
     */
    @Override
    public synchronized void removeOpenProjectsListener(ChangeListener listener) {
        if (hasListeners()) listeners.remove(listener);
        if (!hasListeners()) listeners = null;
    }
    
    private synchronized Set<ChangeListener> listeners() {
        if (!hasListeners()) listeners = new HashSet<ChangeListener>();
        return listeners;
    }
    
    private synchronized boolean hasListeners() {
        return listeners != null;
    }
    
    
    public ProjectUtilitiesProviderImpl() {
        OpenProjects.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                synchronized(ProjectUtilitiesProviderImpl.this) {
                    if (!hasListeners()) return;

                    String prop = evt.getPropertyName();
                    if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(prop) ||
                        OpenProjects.PROPERTY_MAIN_PROJECT.equals(prop)) {
                        for (ChangeListener listener : listeners)
                            listener.stateChanged(new ChangeEvent(evt));
                    }
                }
            }
        });
    }
}
