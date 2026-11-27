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

import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;

/**
 * Gathers project metadata to enrich the ACP session context.
 */
public final class AcpProjectContextProvider {

    private AcpProjectContextProvider() {
    }

    public static ProjectContext gatherProjectContext(Project project) {
        if (project == null) {
            return null;
        }
        FileObject directory = project.getProjectDirectory();
        String projectName = ProjectUtils.getInformation(project).getDisplayName();
        String buildSystem = detectBuildSystem(directory);
        List<SourceRoot> sourceRoots = Arrays.stream(ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC))
                .map(group -> new SourceRoot(group.getRootFolder().getPath(), group.getDisplayName()))
                .toList();
        return new ProjectContext(projectName, directory.getPath(), buildSystem, sourceRoots, keyFiles(directory));
    }

    public record SourceRoot(String path, String name) {

        public String description() {
            return path + " (" + name + ")";
        }
    }

    public record ProjectContext(String name, String directory, String buildSystem,
            List<SourceRoot> sourceRoots, List<String> keyFiles) {

        public ProjectContext {
            sourceRoots = List.copyOf(sourceRoots);
            keyFiles = List.copyOf(keyFiles);
        }

        public String promptText() {
            StringBuilder text = new StringBuilder("## Project Context\n\n");
            text.append("- **Project name**: ").append(name).append('\n');
            text.append("- **Project directory**: ").append(directory).append('\n');
            if (buildSystem != null) {
                text.append("- **Build system**: ").append(buildSystem).append('\n');
            }
            if (!sourceRoots.isEmpty()) {
                text.append("- **Source roots**:\n");
                for (SourceRoot root : sourceRoots) {
                    text.append("  - `").append(root.path()).append("` (").append(root.name()).append(")\n");
                }
            }
            String files = keyFiles.isEmpty() ? "(none detected)" : "`" + String.join("`, `", keyFiles) + "`";
            return text.append("- **Key files present**: ").append(files).append('\n').toString();
        }
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

    private static List<String> keyFiles(FileObject projectDir) {
        return List.of("pom.xml", "build.xml", "build.gradle", "build.gradle.kts",
                "README.md", "README.txt", ".gitignore", "Dockerfile", "docker-compose.yml",
                "Makefile", "package.json", "tsconfig.json").stream()
                .filter(name -> projectDir.getFileObject(name) != null)
                .toList();
    }
}
