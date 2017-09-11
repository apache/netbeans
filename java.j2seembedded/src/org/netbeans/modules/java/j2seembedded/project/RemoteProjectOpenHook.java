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

package org.netbeans.modules.java.j2seembedded.project;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-java-j2seproject")
public class RemoteProjectOpenHook extends ProjectOpenedHook {

    private static final Logger LOG = Logger.getLogger(RemoteProjectOpenHook.class.getName());
    
    private final Project prj;

    public RemoteProjectOpenHook(@NonNull final Project prj) {
        Parameters.notNull("prj", prj);
        this.prj = prj;
    }

    @Override
    protected void projectOpened() {
        ProjectManager.mutex().writeAccess(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        updateBuildScript();
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            });
    }

    @Override
    protected void projectClosed() {
    }

    private void updateBuildScript() throws IOException {
        assert ProjectManager.mutex().isWriteAccess();
        final AntBuildExtender extender = prj.getLookup().lookup(AntBuildExtender.class);
        if (extender == null) {
            LOG.log(
                Level.WARNING,
                "No AntBuildExtender in project: {0}({1})", //NOI18N
                new Object[]{
                    ProjectUtils.getInformation(prj).getDisplayName(),
                    FileUtil.getFileDisplayName(prj.getProjectDirectory())
                });
            return;
        }
        if (!Utilities.hasRemoteExtension(prj)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(
                    Level.FINE,
                    "The project: {0}({1}) does not have current remote platform extension",  //NOI18N
                    new Object[]{
                        ProjectUtils.getInformation(prj).getDisplayName(),
                        FileUtil.getFileDisplayName(prj.getProjectDirectory())
                    });
            }
            return;
        }
        Utilities.copyBuildScript(prj);
    }
}
