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

package org.netbeans.modules.debugger.jpda.projectsui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.util.Exceptions;

/**
 * Provides access to the main or currently selected project.
 *
 * @author   Jan Jancura, Martin Entlicher
 */
public class MainProjectManager implements ProjectActionPerformer, PropertyChangeListener {

    public static final String PROP_MAIN_PROJECT = "mainProject";   // NOI18N

    public static final String PROP_SELECTED_PROJECT = "selectedProject";   // NOI18N

    private static MainProjectManager mainProjectManager = new MainProjectManager ();

    public static MainProjectManager getDefault () {
        return mainProjectManager;
    }
    
    
    private Action a;
    private Reference<Project> currentProject = new WeakReference<Project>(null); // The current/main project
    private Reference<Project> lastSelectedProjectRef = new WeakReference<Project>(null);
    private boolean isMainProject;  // true iff currentProject is the main project.
    private PropertyChangeSupport pcs;

    @java.lang.SuppressWarnings("LeakingThisInConstructor")
    private MainProjectManager () {
        pcs = new PropertyChangeSupport (this);
        a = ProjectSensitiveActions.projectSensitiveAction (
            this, "x", null
        );
        OpenProjects.getDefault().addPropertyChangeListener(this);
        currentProject = new WeakReference<Project>(OpenProjects.getDefault().getMainProject());
        isMainProject = currentProject.get() != null;
        a.addPropertyChangeListener(this); // I'm listening on it so that I get enable() called.
        SwingUtilities.invokeLater(new Runnable() {
            public @Override void run() {
                a.isEnabled();
            }
        });
    }

    public Project getMainProject () {
        final Project lastSelectedProject;
        final Project current;
        final boolean isMain;
        synchronized (this) {
            lastSelectedProject = lastSelectedProjectRef.get();
            current = currentProject.get();
            isMain = isMainProject;
        }
        if (isMain && lastSelectedProject != null &&
            lastSelectedProject != current &&
            !isDependent(lastSelectedProject, current)) {
            // If there's a main project set, but the current project has no
            // dependency on it, return the current project.
            //System.err.println("getMainProject() = (LS) "+lastSelectedProject);
            return lastSelectedProject;
        } else {
            return current;
        }
        //System.err.println("getMainProject() = "+currentProject);
    }

    public @Override void perform(Project p) {
        assert false : "Fake action should never really be called";
    }

    public @Override boolean enable(Project p) {
        Project old = p;
        Project oldSelected = p;
        synchronized (this) {
            if (isMainProject) {
                oldSelected = lastSelectedProjectRef.get();
            }
            lastSelectedProjectRef = new WeakReference<Project>(p);
            if (!isMainProject) {
                if (currentProject.get() != p) {
                    old = currentProject.get();
                    currentProject = new WeakReference<Project>(p);
                }
            }
        }
        //System.err.println("MainProjectManager.enable("+p+") old = "+old+", oldSelected = "+oldSelected);
        if (old != p) {
            pcs.firePropertyChange (PROP_MAIN_PROJECT, old, p);
        }
        if (oldSelected != p) {
            pcs.firePropertyChange (PROP_SELECTED_PROJECT, oldSelected, p);
        }
        return true; // unused
    }

    /**
     * Test whether one project is dependent on the other.
     * @param p1 dependent project
     * @param p2 main project
     * @return <code>true</code> if project <code>p1</code> depends on project <code>p2</code>
     */
    @SuppressWarnings("DMI_COLLECTION_OF_URLS")
    private static boolean isDependent(Project p1, Project p2) {
        Set<URL> p1Roots = getProjectRoots(p1);
        Set<URL> p2Roots = getProjectRoots(p2);

        for (URL root : p2Roots) {
            Set<URL> dependentRoots = SourceUtils.getDependentRoots(root);
            for (URL sr : p1Roots) {
                if (dependentRoots.contains(sr)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("DMI_COLLECTION_OF_URLS")
    private static Set<URL> getProjectRoots(Project p) {
        Set<URL> projectRoots = new HashSet<URL>(); // roots
        Sources sources = ProjectUtils.getSources(p);
        SourceGroup[] sgs = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sg : sgs) {
            URL root;
            try {
                root = sg.getRootFolder().toURL();
                projectRoots.add(root);
            } catch (NullPointerException npe) {
                // http://www.netbeans.org/issues/show_bug.cgi?id=148076
                if (sg == null) {
                    npe = Exceptions.attachMessage(npe, "Null source group returned from "+sources+" of class "+sources.getClass());
                } else if (sg.getRootFolder() == null) {
                    npe = Exceptions.attachMessage(npe, "Null root folder returned from "+sg+" of class "+sg.getClass());
                }
                Exceptions.printStackTrace(npe);
            }
        }
        return projectRoots;
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

    public @Override void propertyChange(PropertyChangeEvent evt) {
        //System.err.println("MainProjectManager.propertyChange("+evt+") name = "+evt.getPropertyName());
        if (OpenProjects.PROPERTY_MAIN_PROJECT.equals(evt.getPropertyName())) {
            Project theMainProject = OpenProjects.getDefault().getMainProject();
            Project old;
            synchronized (this) {
                isMainProject = theMainProject != null;
                old = currentProject.get();
                if (isMainProject) {
                    currentProject = new WeakReference<Project>(theMainProject);
                } else {
                    currentProject = lastSelectedProjectRef;
                }
            }
            //System.err.println(" main project = "+theMainProject+", old = "+old);
            if (old != theMainProject) {
                pcs.firePropertyChange (PROP_MAIN_PROJECT, old, theMainProject);
            }
        } else if (evt.getSource() == a && "enabled".equals(evt.getPropertyName()) && !a.isEnabled() ||
                   // If the action is enabled, enable(Project) method is called.
                   // Here we unset the main project if action gets disabled and
                   // there do not remain any opened projects.
                   OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
            // Test if the current project is still opened:
            Project theMainProject = OpenProjects.getDefault().getMainProject();
            Project currentGone = null;
            Project currentNew = null;
            Project lastGone = null;
            synchronized (this) {
                boolean isCurrent = OpenProjects.getDefault().isProjectOpen(currentProject.get());
                Project last = lastSelectedProjectRef.get();
                boolean isLast = OpenProjects.getDefault().isProjectOpen(last);
                if (!isCurrent && currentProject.get() != null) {
                    currentGone = currentProject.get();
                    currentProject = new WeakReference<Project>(null);
                }
                if (!isLast && last != null) {
                    if (isMainProject) {
                        lastGone = last;
                    }
                    lastSelectedProjectRef = new WeakReference<Project>(null);
                }
                isMainProject = theMainProject != null;
                if (isMainProject && currentProject.get() == null) {
                    currentProject = new WeakReference<Project>(theMainProject);
                    currentNew = currentProject.get();
                }
            }
            if (currentGone != currentNew) {
                pcs.firePropertyChange (PROP_MAIN_PROJECT, currentGone, currentNew);
            }
            if (lastGone != null) {
                pcs.firePropertyChange (PROP_SELECTED_PROJECT, lastGone, null);
            }
        }
    }
}
