/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.acp.ui;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;

/**
 * Gathers project metadata to enrich the ACP session context.
 */
public final class AcpProjectContextProvider {

    private AcpProjectContextProvider() {
    }

    public static String gatherProjectContext() {
        Project project = OpenProjects.getDefault().getMainProject();
        if (project == null) {
            Project[] open = OpenProjects.getDefault().getOpenProjects();
            if (open.length > 0) {
                project = open[0];
            }
        }
        if (project == null) {
            return null;
        }
        return buildContext(project);
    }

    private static String buildContext(Project project) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Project Context\n\n");

        ProjectInformation info = ProjectUtils.getInformation(project);
        sb.append("- **Project name**: ").append(info.getDisplayName()).append('\n');

        FileObject projectDir = project.getProjectDirectory();
        sb.append("- **Project directory**: ").append(projectDir.getPath()).append('\n');

        String buildSystem = detectBuildSystem(projectDir);
        if (buildSystem != null) {
            sb.append("- **Build system**: ").append(buildSystem).append('\n');
        }

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        if (sourceGroups.length > 0) {
            sb.append("- **Source roots**:\n");
            for (SourceGroup sg : sourceGroups) {
                sb.append("  - `").append(sg.getRootFolder().getPath())
                  .append("` (").append(sg.getDisplayName()).append(")\n");
            }
        }

        appendFileStructureHints(sb, projectDir);

        return sb.toString();
    }

    private static String detectBuildSystem(FileObject projectDir) {
        if (projectDir.getFileObject("pom.xml") != null) {
            return "Maven";
        } else if (projectDir.getFileObject("build.gradle") != null
                || projectDir.getFileObject("build.gradle.kts") != null) {
            return "Gradle";
        } else if (projectDir.getFileObject("build.xml") != null) {
            return "Ant";
        } else if (projectDir.getFileObject("Makefile") != null) {
            return "Make";
        }
        return null;
    }

    private static void appendFileStructureHints(StringBuilder sb, FileObject projectDir) {
        sb.append("- **Key files present**: ");
        boolean first = true;
        String[] interesting = {
            "pom.xml", "build.xml", "build.gradle", "build.gradle.kts",
            "README.md", "README.txt", ".gitignore", "Dockerfile",
            "docker-compose.yml", "Makefile", "package.json", "tsconfig.json"
        };
        for (String name : interesting) {
            if (projectDir.getFileObject(name) != null) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append('`').append(name).append('`');
                first = false;
            }
        }
        if (first) {
            sb.append("(none detected)");
        }
        sb.append('\n');
    }
}
