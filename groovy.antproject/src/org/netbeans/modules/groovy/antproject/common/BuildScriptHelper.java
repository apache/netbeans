/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.antproject.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.support.api.GroovyExtender;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Janicek
 */
public class BuildScriptHelper {

    private static final String GROOVY_BUILD_XML = "nbproject/groovy-build.xml"; // NOI18N
    private static final String GROOVY_BUILD_65_XML = "org/netbeans/modules/groovy/antproject/resources/groovy-build-65.xml"; // NOI18N
    private static final String GROOVY_BUILD_SAMPLE_65_XML = "org/netbeans/modules/groovy/antproject/resources/groovy-build-sample-65.xml"; // NOI18N

    
    public static void refreshBuildScript(Project project, URL stylesheet, boolean checkProjectXml) {
        if (GroovyExtender.isActive(project)) {
            GeneratedFilesHelper helper = new GeneratedFilesHelper(project.getProjectDirectory());
            try {
                int flags = helper.getBuildScriptState(GROOVY_BUILD_XML, stylesheet);
                // old 65 script looks like modified
                if ((GeneratedFilesHelper.FLAG_MODIFIED & flags) != 0
                        && (GeneratedFilesHelper.FLAG_OLD_PROJECT_XML & flags) != 0
                        && (GeneratedFilesHelper.FLAG_OLD_STYLESHEET & flags) != 0
                        && (hasBuildScriptFrom65(project, GROOVY_BUILD_65_XML) || hasBuildScriptFrom65(project, GROOVY_BUILD_SAMPLE_65_XML))) {
                    FileObject buildScript = project.getProjectDirectory().getFileObject(GROOVY_BUILD_XML);
                    if (buildScript != null) {
                        buildScript.delete();

                        helper.generateBuildScriptFromStylesheet(GROOVY_BUILD_XML, stylesheet);
                        return;
                    }
                }

                helper.refreshBuildScript(GROOVY_BUILD_XML, stylesheet, checkProjectXml);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalStateException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static boolean hasBuildScriptFrom65(Project project, String resource) throws IOException {
        FileObject fo = project.getProjectDirectory().getFileObject(GROOVY_BUILD_XML);
        if (fo == null) {
            return false;
        }

        // FIXME is ther any better way ?
        URL xml65 = BuildScriptHelper.class.getClassLoader().getResource(resource);
        URLConnection connection = xml65.openConnection();
        connection.setUseCaches(false);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8")); // NOI18N
        try {
            List<String> lines65 = fo.asLines("UTF-8"); // NOI18N
            for (String line65 : lines65) {
                String line = reader.readLine();
                if (line == null || !line.equals(line65)) {
                    return false;
                }
            }

            return reader.readLine() == null;
        } finally {
            reader.close();
        }
    }
}
