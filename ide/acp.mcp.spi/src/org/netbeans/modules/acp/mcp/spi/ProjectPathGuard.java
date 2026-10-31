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
package org.netbeans.modules.acp.mcp.spi;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileUtil;

public final class ProjectPathGuard {

    private ProjectPathGuard() {
    }

    public static boolean isInOpenProject(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        try {
            Path canonical = new File(path).getCanonicalFile().toPath();
            for (Project p : OpenProjects.getDefault().getOpenProjects()) {
                File projectDir = FileUtil.toFile(p.getProjectDirectory());
                if (projectDir != null && canonical.startsWith(projectDir.getCanonicalFile().toPath())) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Validates that all given paths are inside an open project.
     * @return an error result for the first path that fails, or {@code null} if all pass
     */
    public static Map<String, Object> requireInOpenProject(String... paths) {
        for (String path : paths) {
            if (path != null && !path.isBlank() && !isInOpenProject(path)) {
                return outsideProjectError(path);
            }
        }
        return null;
    }

    public static Map<String, Object> outsideProjectError(String path) {
        return ToolResult.error("Path is outside any open project: " + path);
    }
}
