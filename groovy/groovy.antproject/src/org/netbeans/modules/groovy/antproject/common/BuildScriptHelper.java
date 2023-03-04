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

package org.netbeans.modules.groovy.antproject.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
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
