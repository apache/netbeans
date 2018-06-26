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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.jaxwsmodel.project;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.websvc.api.jaxws.project.JaxWsBuildScriptExtensionProvider;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-j2ee-clientproject")
public class AppClientJaxWsOpenHook extends ProjectOpenedHook {
    Project prj;

    /** Creates a new instance of AppClientJaxWsOpenHook */
    public AppClientJaxWsOpenHook(Project prj) {
        this.prj = prj;
        try {
            Class.forName(WSUtils.class.getName());
        }
        catch (ClassNotFoundException e) {
            assert false;
        }
    }
    
    @Override
    protected void projectOpened() {
        AntBuildExtender ext = prj.getLookup().lookup(AntBuildExtender.class);
        final JaxWsBuildScriptExtensionProvider extProvider = prj.getLookup().lookup(JaxWsBuildScriptExtensionProvider.class);
        if (ext != null && extProvider != null) {
            FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
            try {
                AntBuildExtender.Extension extension = ext.getExtension(JaxWsBuildScriptExtensionProvider.JAXWS_EXTENSION);
                FileObject jaxWsFo = WSUtils.findJaxWsFileObject(prj);
                if (jaxws_build==null || extension == null) {
                    if (jaxWsFo != null && WSUtils.hasClients(jaxWsFo)) {
                        // generate nbproject/jaxws-build.xml
                        // add jaxws extension
                        extProvider.addJaxWsExtension(ext);
                    }
                } else if (jaxWsFo == null || !WSUtils.hasClients(jaxWsFo)) {
                    // remove nbproject/jaxws-build.xml
                    // remove the jaxws extension
                    extProvider.removeJaxWsExtension(ext);
                } else {
                    // remove compile dependencies, and re-generate build-script if needed
                    FileObject project_xml = prj.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH);
                    if (project_xml != null) {
                        removeCompileDependencies(prj, project_xml, ext);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void projectClosed() {
    }
    
    /** make old project backward compatible with new projects
     *
     */
    private void removeCompileDependencies (
                        Project prj,
                        FileObject project_xml,
                        final AntBuildExtender ext) throws IOException {

        BufferedReader br = new BufferedReader( new InputStreamReader( 
                new FileInputStream(FileUtil.toFile(project_xml)), 
                    Charset.forName("UTF-8")));                         // NOI18N
        String line = null;
        boolean isOldVersion = false;
        while ((line = br.readLine()) != null) {
            if (line.contains("wsimport-client-compile")) { //NOI18N
                isOldVersion = true;
                break;
            }
        }
        br.close();
        if (isOldVersion) {
            TransformerUtils.transformClients(prj.getProjectDirectory(), AppClientBuildScriptExtensionProvider.JAX_WS_STYLESHEET_RESOURCE);
            AntBuildExtender.Extension extension = ext.getExtension(JaxWsBuildScriptExtensionProvider.JAXWS_EXTENSION);
            if (extension!=null) {
                extension.removeDependency("-do-compile", "wsimport-client-compile"); //NOI18N
                extension.removeDependency("-do-compile-single", "wsimport-client-compile"); //NOI18N
                ProjectManager.getDefault().saveProject(prj);
            }
        }

    }

}
