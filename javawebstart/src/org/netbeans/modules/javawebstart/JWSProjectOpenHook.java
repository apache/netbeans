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
