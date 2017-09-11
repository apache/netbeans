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
package org.netbeans.modules.javawebstart;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.javawebstart.ui.customizer.JWSProjectPropertiesUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ant.GeneratedFilesInterceptor;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
@ProjectServiceProvider(
        service = GeneratedFilesInterceptor.class,
        projectType = "org-netbeans-modules-java-j2seproject")
public class JWSGeneratedFilesInterceptor implements GeneratedFilesInterceptor {

    private static final Logger LOG = Logger.getLogger(JWSGeneratedFilesInterceptor.class.getName());   

    private final ThreadLocal<Boolean> reenter = new ThreadLocal<Boolean>();
    
    @Override
    public void fileGenerated(
            final Project project,
            final String path) {
        if (reenter.get() == Boolean.TRUE) {
            return;
        }
        if (GeneratedFilesHelper.BUILD_IMPL_XML_PATH.equals(path)) {
            final AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
            if (extender == null) {
                LOG.log(
                    Level.WARNING,
                    "The project {0} ({1}) does not support AntBuildExtender.",     //NOI18N
                    new Object[] {
                        ProjectUtils.getInformation(project).getDisplayName(),
                        FileUtil.getFileDisplayName(project.getProjectDirectory())
                    });
                return;
            }
            runDeferred(new Runnable() {
                @Override
                public void run() {
                    updateIfNeeded(project, extender);
                }
            });
        }
    }
    
    private void runDeferred(final Runnable r) {
        ProjectManager.mutex().postReadRequest(new Runnable() {
            @Override
            public void run() {                
                ProjectManager.mutex().postWriteRequest(r);
            }
        });
    }

    private void updateIfNeeded(
            final Project project,
            final AntBuildExtender extender) {
        if (extender.getExtension(JWSProjectPropertiesUtils.getCurrentExtensionName()) != null) {
            //Already has a current version of extension
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(
                        Level.FINE,
                        "The project {0} ({1}) already has a current version ({2}) of JWS extension.", //NOI18N
                        new Object[]{
                    ProjectUtils.getInformation(project).getDisplayName(),
                    FileUtil.getFileDisplayName(project.getProjectDirectory()),
                    JWSProjectPropertiesUtils.getCurrentExtensionName()
                });
            }
            return;
        }
        reenter.set(Boolean.TRUE);
        try {
            boolean needsUpdate = false;
            for (String oldExt : JWSProjectPropertiesUtils.getOldExtensionNames()) {
                final AntBuildExtender.Extension extension = extender.getExtension(oldExt);
                if (extension != null) {
                    extender.removeExtension(oldExt);
                    needsUpdate = true;
                }
            }
            if (needsUpdate) {
                try {
                    //There was an old extension which needs to be updated
                    JWSProjectPropertiesUtils.updateJnlpExtension(project);
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } finally {
            reenter.remove();
        }
    }

}
