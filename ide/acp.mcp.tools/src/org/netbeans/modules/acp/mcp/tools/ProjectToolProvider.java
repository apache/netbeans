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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.acp.mcp.spi.McpToolProvider;
import org.netbeans.modules.acp.mcp.spi.ToolResult;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = McpToolProvider.class)
public final class ProjectToolProvider implements McpToolProvider {

    private static final Logger LOG = Logger.getLogger(ProjectToolProvider.class.getName());

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
        LOG.log(Level.FINE, "Executing project tool: {0}", name);
        Map<String, Object> result = switch (name) {
            case "list_projects" -> listProjects();
            case "get_ide_info" -> getIdeInfo();
            default -> throw new IllegalArgumentException("Unknown tool: " + name);
        };
        LOG.log(Level.FINE, "Project tool {0} succeeded", name);
        return result;
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
        return ToolResult.ok("projects", paths);
    }

    private Map<String, Object> getIdeInfo() {
        Map<String, Object> result = ToolResult.ok(
                "productVersion", System.getProperty("netbeans.productversion", "unknown"),
                "java", System.getProperty("java.version"),
                "javaVm", System.getProperty("java.vm.name"),
                "javaHome", System.getProperty("java.home"));
        result.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        result.put("userDir", System.getProperty("netbeans.user", "unknown"));
        result.put("encoding", System.getProperty("file.encoding"));
        return result;
    }
}
