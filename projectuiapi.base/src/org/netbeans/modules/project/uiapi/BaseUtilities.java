/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.project.uiapi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.ProjectGroup;
import org.netbeans.api.project.ui.ProjectGroupChangeListener;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public final class BaseUtilities {
    
    private static final Logger LOG = Logger.getLogger(BaseUtilities.class.getName());
    
    /** 
     * Gets an object the OpenProjects can delegate to
     */
    public static OpenProjectsTrampoline getOpenProjectsTrampoline() {
        OpenProjectsTrampoline instance = Lookup.getDefault().lookup(OpenProjectsTrampoline.class);
        return instance != null ? instance : DefaultOpenProjectsTrampoline.getInstance();
    }
    
    // XXX anybody using this
    @org.netbeans.api.annotations.common.SuppressWarnings("MS_SHOULD_BE_FINAL")
    public static ProjectGroupAccessor ACCESSOR = null;

    static {
        // invokes static initializer of ModelHandle.class
        // that will assign value to the ACCESSOR field above
        Class<?> c = ProjectGroup.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "very wrong, very wrong, yes indeed", ex);
        }
    }

    public static abstract class ProjectGroupAccessor {

        public abstract ProjectGroup createGroup(String name, Preferences prefs);

    }

    private static final class DefaultOpenProjectsTrampoline implements OpenProjectsTrampoline {

        private static final AtomicReference<DefaultOpenProjectsTrampoline> INSTANCE = new AtomicReference<>();
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final Collection<Project> open = new ArrayList<>();
        private Project main;

        private DefaultOpenProjectsTrampoline() {
        }

        @Override public Project[] getOpenProjectsAPI() {
            return open.toArray(new Project[open.size()]);
        }
        @Override public void openAPI(Project[] projects, boolean openRequiredProjects, boolean showProgress) {
            open.addAll(Arrays.asList(projects));
            pcs.firePropertyChange(OpenProjects.PROPERTY_OPEN_PROJECTS, null, null);
        }
        @Override public void closeAPI(Project[] projects) {
            open.removeAll(Arrays.asList(projects));
            pcs.firePropertyChange(OpenProjects.PROPERTY_OPEN_PROJECTS, null, null);
        }
        @Override public Future<Project[]> openProjectsAPI() {
            return RequestProcessor.getDefault().submit(new Callable<Project[]>() {
                @Override public Project[] call() {
                    return getOpenProjectsAPI();
                }
            });
        }
        @Override public Project getMainProject() {
            return main;
        }
        @Override public void setMainProject(Project project) {
            main = project;
            pcs.firePropertyChange(OpenProjects.PROPERTY_MAIN_PROJECT, null, null);
        }
        @Override public void addPropertyChangeListenerAPI(PropertyChangeListener listener, Object source) {
            pcs.addPropertyChangeListener(listener);
        }
        @Override public void removePropertyChangeListenerAPI(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public void addProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener) {
        }

        @Override
        public void removeProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener) {
        }

        @Override
        public ProjectGroup getActiveProjectGroupAPI() {
            return null;
        }

        @NonNull
        static DefaultOpenProjectsTrampoline getInstance() {
            DefaultOpenProjectsTrampoline res = INSTANCE.get();
            if (res == null) {
                res = new DefaultOpenProjectsTrampoline();
                if (!INSTANCE.compareAndSet(null, res)) {
                    res = INSTANCE.get();
                }
            }
            assert res != null;
            return res;
        }
    }
}
