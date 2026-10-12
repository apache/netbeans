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
package org.netbeans.modules.acp.mcp.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.acp.mcp.McpToolProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = McpToolProvider.class)
public final class ProjectToolProvider implements McpToolProvider {

    @Override
    public List<McpToolDefinition> getTools() {
        List<McpToolDefinition> tools = new ArrayList<>();

        tools.add(new McpToolDefinition(
                "list_projects",
                "Returns a list of absolute paths of all open NetBeans projects. "
                + "Use this to discover the workspace structure and understand what projects "
                + "are available for navigation or context.",
                Map.of("type", "object", "properties", Map.of())
        ));

        tools.add(new McpToolDefinition(
                "get_ide_info",
                "Returns information about the running NetBeans IDE: version, Java runtime, "
                + "user directory, and platform details.",
                Map.of("type", "object", "properties", Map.of())
        ));

        return tools;
    }

    @Override
    public Map<String, Object> executeTool(String name, Map<String, Object> arguments) throws Exception {
        return switch (name) {
            case "list_projects" -> listProjects();
            case "get_ide_info" -> getIdeInfo();
            default -> throw new IllegalArgumentException("Unknown tool: " + name);
        };
    }

    private Map<String, Object> listProjects() {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        List<String> paths = new ArrayList<>();
        for (Project p : projects) {
            File dir = FileUtil.toFile(p.getProjectDirectory());
            if (dir != null) {
                paths.add(dir.getAbsolutePath());
            }
        }
        return Map.of("status", "ok", "projects", paths);
    }

    private Map<String, Object> getIdeInfo() {
        return Map.of(
                "status", "ok",
                "productVersion", System.getProperty("netbeans.productversion", "unknown"),
                "java", System.getProperty("java.version"),
                "javaVm", System.getProperty("java.vm.name"),
                "javaHome", System.getProperty("java.home"),
                "os", System.getProperty("os.name") + " " + System.getProperty("os.version"),
                "userDir", System.getProperty("netbeans.user", "unknown"),
                "encoding", System.getProperty("file.encoding")
        );
    }
}
