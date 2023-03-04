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

package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.options.MavenSettings;
import static org.netbeans.modules.maven.queries.Bundle.*;
import org.netbeans.modules.maven.spi.nodes.SpecialIcon;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

@ProjectServiceProvider(service=ProjectInformation.class, projectType="org-netbeans-modules-maven")
public final class Info implements ProjectInformation, PropertyChangeListener {
    
    private static final RequestProcessor RP = new RequestProcessor(Info.class.getName(), 10);
    private static final Logger LOG = Logger.getLogger(Info.class.getName());


    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Project project;
    private final PreferenceChangeListener preferenceChangeListener = new PreferenceChangeListener() {
                    @Override
                    public void preferenceChange(PreferenceChangeEvent evt) {
                        if (MavenSettings.PROP_PROJECTNODE_NAME_PATTERN.equals(evt.getKey())) {
                            pcs.firePropertyChange(ProjectInformation.PROP_NAME, null, null);
                            pcs.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, null, null);
                        }
                    }
                };
    private final AtomicBoolean prefChangeListenerSet = new AtomicBoolean(false);
    
    private boolean displayNameRunning = false;
    private String displayName;

    public Info(final Project project) {
        this.project = project;
    }

    @Override public String getName() {
        //always return the same value, never skip in AWT, used in quite some places relying on 
        //consistency for some reason..
        //For performance reasons, we should rather try to use something like:
        //return project.getProjectDirectory().toURL().toExternalForm();
        //..but we need to check getName() usages at first because it's used all over the place
        final NbMavenProject nb = project.getLookup().lookup(NbMavenProject.class);
        return nb.getMavenProject().getId().replace(':', '_');
    }

    @Override public @NonNull String getDisplayName() {
        synchronized(displayNameTask) {
            if(displayName == null) {
                displayName = FileUtil.getFileDisplayName(project.getProjectDirectory());
            }
            if(!displayNameRunning) {
                displayNameRunning = true;
                if(Boolean.getBoolean("test.load.sync")) {
                    displayNameTask.run();
                } else {
                    RP.schedule(displayNameTask, 100, TimeUnit.MILLISECONDS); // lots of repeating calls
                }
            }
            return displayName;
        }
    }
    
    private final Runnable displayNameTask = new Runnable() {
        @Override
        public void run() {
            try {
                final NbMavenProject nb = project.getLookup().lookup(NbMavenProject.class);
                if (!nb.isMavenProjectLoaded()) {                    
                    nb.getMavenProject();
                }
                String s = getDisplayName(nb);            
                if(!s.equals(displayName)) {
                    displayName = s;
                    pcs.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, null, null);
                }
            } finally {
                displayNameRunning = false;
            }
        }
    };
    
     @Messages({
        "# {0} - dir basename", "LBL_misconfigured_project={0} [unloadable]",
        "# {0} - path to project", "TXT_Maven_project_at=Maven project at {0}"
    })
    private String getDisplayName(NbMavenProject nb) {
        MavenProject pr = nb.getMavenProject();
        if (NbMavenProject.isErrorPlaceholder(pr)) {
            return LBL_misconfigured_project(project.getProjectDirectory().getNameExt());
        }
        String custom = project.getLookup().lookup(AuxiliaryProperties.class).get(Constants.HINT_DISPLAY_NAME, true);
        if (custom == null) {
            custom = NbPreferences.forModule(Info.class).get(MavenSettings.PROP_PROJECTNODE_NAME_PATTERN, null);
        }
        if (custom != null) {
            //we evaluate because of global property and property in nb-configuration.xml file. The pom.xml originating value should be already resolved.
            ExpressionEvaluator evaluator = PluginPropertyUtils.createEvaluator(project);
            try {
                Object s = evaluator.evaluate(custom);
                if (s != null) {
                    //just make sure the name gets resolved
                    String ss = s.toString().replace("${project.name)", "" + pr.getGroupId() + ":" + pr.getArtifactId());
                    return ss;
                }
            } catch (ExpressionEvaluationException ex) {
                //now just continue to the default processing
                LOG.log(Level.INFO, "bad display name expression:" + custom, ex);
            }
        }

        String toReturn = pr.getName();
        if (toReturn == null) {
            String grId = pr.getGroupId();
            String artId = pr.getArtifactId();
            if (grId != null && artId != null) {
                toReturn = grId + ":" + artId; //NOI18N
            } else {
                toReturn = TXT_Maven_project_at(FileUtil.getFileDisplayName(project.getProjectDirectory()));
            }
        }
        return toReturn;
    }

    
    @Override public Icon getIcon() {
        final NbMavenProject nb = project.getLookup().lookup(NbMavenProject.class);
        if (SwingUtilities.isEventDispatchThread() && !nb.isMavenProjectLoaded()) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    //assuming this takes long and hangs in sync.
                    nb.getMavenProject();
                    pcs.firePropertyChange(ProjectInformation.PROP_ICON, null, null);
                }
            });
            return ImageUtilities.loadImageIcon("org/netbeans/modules/maven/resources/Maven2Icon.gif", true);
        }
        SpecialIcon special = project.getLookup().lookup(SpecialIcon.class);
        if (special != null) {
            Icon icon = special.getIcon();
            if (icon != null) {
                return icon;
            } else {
                LOG.log(Level.WARNING, "No icon provided by {0}", special);
            }
        }
        return ImageUtilities.loadImageIcon("org/netbeans/modules/maven/resources/Maven2Icon.gif", true);
    }
    
    @Override public Project getProject() {
        return project;
    }
    
    @Override 
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (prefChangeListenerSet.compareAndSet(false, true)) {
            NbPreferences.forModule(Info.class).addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, preferenceChangeListener, NbPreferences.forModule(Info.class)));
        }
        if (!pcs.hasListeners(null)) {
            project.getLookup().lookup(NbMavenProject.class).addPropertyChangeListener(this);
        }
        pcs.addPropertyChangeListener(listener);
    }
    
    @Override 
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        boolean had = pcs.hasListeners(null);
        pcs.removePropertyChangeListener(listener);
        if (had && !pcs.hasListeners(null)) {
            project.getLookup().lookup(NbMavenProject.class).removePropertyChangeListener(this);
        }
    }

    @Override public void propertyChange(PropertyChangeEvent evt) {
        if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
            pcs.firePropertyChange(ProjectInformation.PROP_NAME, null, null);
            pcs.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, null, null);
            pcs.firePropertyChange(ProjectInformation.PROP_ICON, null, null);
        }
    }

}
