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
package org.netbeans.modules.javawebstart;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.java.j2seproject.api.J2SEProjectPlatform;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.javawebstart.ui.customizer.JWSProjectProperties;
import org.netbeans.modules.javawebstart.ui.customizer.JWSProjectPropertiesUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 * @author Petr Somol
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-java-j2seproject")
public class JWSProjectOpenHook extends ProjectOpenedHook {

    private static final Logger LOG = Logger.getLogger(JWSProjectOpenHook.class.getName());

    private final Project prj;
    private final J2SEPropertyEvaluator eval;
    private final PlatformListener listener;

    public JWSProjectOpenHook(final Lookup lkp) {
        Parameters.notNull("lkp", lkp); //NOI18N
        this.prj = lkp.lookup(Project.class);
        Parameters.notNull("prj", prj); //NOI18N
        this.eval = lkp.lookup(J2SEPropertyEvaluator.class);
        Parameters.notNull("eval", eval);   //NOI18N
        this.listener = new PlatformListener(lkp.lookup(J2SEProjectPlatform.class));
    }

    @Override
    protected void projectOpened() {
        listener.attach();
        ProjectManager.mutex().writeAccess(
            new Runnable() {
                @Override
                public void run() {
                    updateBuildScript();
                    updateLibraries();
                }
        });
    }

    private void updateBuildScript() {
        final AntBuildExtender extender = prj.getLookup().lookup(AntBuildExtender.class);
        if (extender == null) {
            LOG.log(
                Level.WARNING,
                "The project {0} ({1}) does not support AntBuildExtender.",     //NOI18N
                new Object[] {
                    ProjectUtils.getInformation(prj).getDisplayName(),
                    FileUtil.getFileDisplayName(prj.getProjectDirectory())
                });
            return;
        }
        if (extender.getExtension(JWSProjectPropertiesUtils.getCurrentExtensionName()) == null) {
            if (LOG.isLoggable(Level.FINE)) {
                //Prevent expensive ProjectUtils.getInformation(prj) when not needed
                LOG.log(
                    Level.FINE,
                    "The project {0} ({1}) does not have a current version ({2}) of JWS extension.", //NOI18N
                    new Object[] {
                        ProjectUtils.getInformation(prj).getDisplayName(),
                        FileUtil.getFileDisplayName(prj.getProjectDirectory()),
                        JWSProjectPropertiesUtils.getCurrentExtensionName()
                    });
            }
            return;
        }
        if (JWSProjectPropertiesUtils.isJnlpImplUpToDate(prj)) {
            if (LOG.isLoggable(Level.FINE)) {
                //Prevent expensive ProjectUtils.getInformation(prj) when not needed
                LOG.log(
                    Level.FINE,
                    "The project {0} ({1}) have an up to date JWS extension.", //NOI18N
                    new Object[] {
                        ProjectUtils.getInformation(prj).getDisplayName(),
                        FileUtil.getFileDisplayName(prj.getProjectDirectory())
                    });
            }
            return;
        }
        try {
            JWSProjectPropertiesUtils.copyJnlpImplTemplate(prj);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void updateLibraries() {
        try {
            if (isWebStartEnabled()) {
                JWSProjectProperties.updateOnOpen(prj, eval.evaluator());
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    private boolean isWebStartEnabled() {
        return isTrue(eval.evaluator().getProperty(JWSProjectProperties.JNLP_ENABLED)); //JNLP_ENABLED - inlined by compiler
    }

    @Override
    protected void projectClosed() {
        listener.detach();
    }

    //Don't use JWSProjectProperties.isTrue - causes loading of big JWSProjectProperties
    private static boolean isTrue(final String value) {
        return value != null &&
        (value.equalsIgnoreCase("true") ||  //NOI18N
         value.equalsIgnoreCase("yes") ||   //NOI18N
         value.equalsIgnoreCase("on"));     //NOI18N
    }

    private final class PlatformListener implements PropertyChangeListener {

        private final J2SEProjectPlatform projectPlatform;
        private final AtomicBoolean attached = new AtomicBoolean();

        PlatformListener(@NonNull final J2SEProjectPlatform projectPlatform) {
            Parameters.notNull("projectPlatform", projectPlatform);   //NOI18N
            this.projectPlatform = projectPlatform;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (J2SEProjectPlatform.PROP_PROJECT_PLATFORM.equals(evt.getPropertyName()) &&
                isWebStartEnabled()) {
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if (ProjectManager.mutex().isWriteAccess()) {
                            updateLibraries();
                        } else {
                            ProjectManager.mutex().postWriteRequest(this);
                        }
                    }
                };
                ProjectManager.mutex().postReadRequest(r);
            }
        }

        void attach() {
            if (attached.compareAndSet(false, true)) {
                projectPlatform.addPropertyChangeListener(this);
            } else {
                throw new IllegalStateException(String.format(
                    "Listener %d is already attached to J2SEProjectPlatform %d for Project %s.",   //NOI18N
                    System.identityHashCode(this),
                    System.identityHashCode(projectPlatform),
                    ProjectUtils.getInformation(prj).getDisplayName()));
            }
        }

        void detach() {
            if (attached.compareAndSet(true, false)) {
                projectPlatform.removePropertyChangeListener(this);
            } else {
                throw new IllegalStateException(String.format(
                    "Listener %d is not attached to J2SEProjectPlatform %d for Project %s.",   //NOI18N
                    System.identityHashCode(this),
                    System.identityHashCode(projectPlatform),
                    ProjectUtils.getInformation(prj).getDisplayName()));
            }
        }
    }
}
