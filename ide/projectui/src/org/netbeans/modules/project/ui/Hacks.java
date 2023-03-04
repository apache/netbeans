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

package org.netbeans.modules.project.ui;

import java.awt.EventQueue;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import static org.netbeans.modules.project.ui.Bundle.*;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Various hacks that should be solved better later.
 */
public class Hacks {

    private Hacks() {}
    
    private static final Logger LOG = Logger.getLogger(Hacks.class.getName());

    static final RequestProcessor RP = new RequestProcessor("Project UI"); // NOI18N
    
    /**
     * Show name of project corresponding to selection in Main Window title bar.
     * @author Jesse Glick
     */
    @Messages("LBL_MultipleProjects=Multiple Projects")
    static void keepCurrentProjectNameUpdated() {
        final TopComponent.Registry r = TopComponent.getRegistry();
        final AtomicReference<PropertyChangeListener> displayNameListener = new AtomicReference<PropertyChangeListener>();
        ResourceBundle bundle;
        try {
            bundle = NbBundle.getBundle("org.netbeans.core.windows.view.ui.Bundle");
        } catch (MissingResourceException x) {
            LOG.log(Level.FINE, "Running from a unit test?", x);
            return;
        }
        final String BUILD_NUMBER = System.getProperty("netbeans.buildnumber"); // NOI18N
        final String NO_PROJECT_TITLE = MessageFormat.format(bundle.getString("CTL_MainWindow_Title_No_Project"), BUILD_NUMBER);
        final String PROJECT_TITLE_FORMAT = bundle.getString("CTL_MainWindow_Title");
        final RequestProcessor.Task task = RP.create(new Runnable() {
            String lastKnownTitle; // #134802
            public @Override void run() {
                Node[] sel = r.getActivatedNodes();
                Set<Project> projects = new HashSet<Project>();
                boolean noProjectsOpen = OpenProjectList.getDefault().getOpenProjects().length == 0;
                for (int i = 0; i < sel.length; i++) {
                    Lookup l = sel[i].getLookup();
                    Project p = l.lookup(Project.class);
                    if (p != null) {
                        projects.add(p);
                        if (noProjectsOpen) {
                            LOG.log(/*XXX for now*/Level.FINE, "Activated node selection {0} contains nonopen project {1} though none are open; leak? activated TC: {2} current nodes: {3} (report in issue #102805)", new Object[] {Arrays.toString(sel), p, r.getActivated(), Arrays.toString(r.getCurrentNodes())});
                        }
                    } else {
                        DataObject d = l.lookup(DataObject.class);
                        if (d != null) {
                            FileObject f = d.getPrimaryFile();
                            p = ProjectConvertors.getNonConvertorOwner(f);
                            if (p != null) {
                                projects.add(p);
                            }
                        }
                    }
                }
                final String pname;
                LOG.log(Level.FINER, "found project selection: {0}", projects);
                if (projects.size() == 1) {
                    Project p = projects.iterator().next();
                    ProjectInformation info = ProjectUtils.getInformation(p);
                    info.removePropertyChangeListener(displayNameListener.get());
                    info.addPropertyChangeListener(displayNameListener.get());
                    pname = info.getDisplayName();
                    assert pname != null : p;
                } else if (projects.isEmpty()) {
                    pname = null;
                } else {
                    pname = LBL_MultipleProjects();
                }
                EventQueue.invokeLater(new Runnable() {
                    public @Override void run() {
                        String title = pname != null ? MessageFormat.format(PROJECT_TITLE_FORMAT, BUILD_NUMBER, pname) : NO_PROJECT_TITLE;
                        Frame mainWindow = WindowManager.getDefault().getMainWindow();
                        String currTitle = mainWindow.getTitle();
                        if (currTitle == null || currTitle.isEmpty() || currTitle.equals(lastKnownTitle) || /*#199245*/currTitle.equals(NO_PROJECT_TITLE)) {
                            lastKnownTitle = title;
                            mainWindow.setTitle(title);
                        } else {
                            LOG.log(Level.FINE, "not replacing {0} with {1} since it was last set to {2}", new Object[] {currTitle, title, lastKnownTitle});
                        }
                    }
                });
            }
        });
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            public @Override void run() {
                task.schedule(0);
                displayNameListener.set(new PropertyChangeListener() {
                    public @Override void propertyChange(PropertyChangeEvent ev) {
                        String prop = ev.getPropertyName();
                        if (prop == null || prop.equals(ProjectInformation.PROP_DISPLAY_NAME)) {
                            LOG.finer("got PROP_DISPLAY_NAME");
                            task.schedule(0);
                        }
                    }
                });
                r.addPropertyChangeListener(new PropertyChangeListener() {
                    public @Override void propertyChange(PropertyChangeEvent ev) {
                        if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(ev.getPropertyName())) {
                            LOG.finer("got PROP_ACTIVATED_NODES");
                            task.schedule(200);
                        }
                    }
                });
            }
        });
    }
    
    
    /** Forces reload of panels in TemplateWizard. Public method updateState doesn't re-read
     * the new panels from new iterator.
     * Note: it should be solved better either fixing TemplateWizard or implement
     * whole NewFileWizard (w/o TemplateWizard).
     *
     * @param tw instance of TemplateWizard (thus NewFileWizard)
     * @param dobj template
     */    
    static void reloadPanelsInWizard (final TemplateWizard tw, final DataObject dobj) {
        try {
            Class<?> twClazz = Class.forName(
                "org.openide.loaders.TemplateWizard", true, // NOI18N
                Thread.currentThread().getContextClassLoader());
            if (twClazz != null) {
                Method reloadPanels = twClazz.getDeclaredMethod("reload", DataObject.class); // NOI18N
                reloadPanels.setAccessible (true);
                reloadPanels.invoke(tw, dobj);
            }
        } catch (Exception e) {
            // XXX
            e.printStackTrace();
        }
    }
}
