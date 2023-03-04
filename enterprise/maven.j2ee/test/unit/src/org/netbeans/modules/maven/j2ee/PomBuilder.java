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
package org.netbeans.modules.maven.j2ee;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class used for building pom.xml (mainly for tests)
 * 
 * @author Martin Janicek
 */
public class PomBuilder {
    
    private StringBuilder sb;
    private String projectValues;
    private List<PomPlugin> plugins;
    private List<PomDependency> dependencies;

    
    public PomBuilder() {
        sb = new StringBuilder();
        plugins = new ArrayList<PomPlugin>();
        dependencies = new ArrayList<>();
    }
    
    public PomBuilder appendDefaultTestValues() {
        projectValues = "<modelVersion>4.0.0</modelVersion>" +
                        "<groupId>testGroupId</groupId>" +
                        "<artifactId>testArtifactId</artifactId>" +
                        "<packaging>war</packaging>" +
                        "<version>1.0</version>";
        return this;
    }
    
    public PomBuilder appendPomContent(String packaging) {
        projectValues = "<modelVersion>4.0.0</modelVersion>" +
                        "<groupId>testGroupId</groupId>" +
                        "<artifactId>testArtifactId</artifactId>" +
                        "<packaging>" + packaging + "</packaging>" +
                        "<version>1.0</version>";
        return this;
    }
    
    public PomBuilder appendPomContent(String modelVersion, String groupID, String artifactID, String packaging, String version) {
        projectValues = "<modelVersion>" + modelVersion + "</modelVersion>" +
                        "<groupId>" + groupID + "</groupId>" +
                        "<artifactId>" + artifactID + "</artifactId>" +
                        "<packaging>" + packaging + "</packaging>" +
                        "<version>" + version + "</version>";
        return this;
    }
    
    public PomBuilder appendPlugin(PomPlugin plugin) {
        plugins.add(plugin);
        return this;
    }
    
    public PomBuilder appendDependency(PomDependency dependency) {
        dependencies.add(dependency);
        return this;
    }
     
    /**
     * Creates and returns pom.xml file based on the previous actions.
     * 
     * @return builded pom.xml file
     */
    public String buildPom() {
        sb.append("<project>");
        sb.append(projectValues);      
        sb.append("<build>");
        sb.append("<plugins>");
        
        for (PomPlugin plugin : plugins) {
            addPlugin(plugin);
        }
        
        sb.append("</plugins>");
        sb.append("</build>");

        sb.append("<dependencies>");
        for (PomDependency dependency : dependencies) {
            addDependency(dependency);
        }
        sb.append("</dependencies>");
        
        sb.append("</project>");
        
        return sb.toString();
    }
    
    public void clear() {
        sb.delete(0, sb.length());
        plugins.clear();
        projectValues = "";
    }
    
    private void addPlugin(PomPlugin plugin) {
        sb.append("<plugin>")
            .append("<groupId>").append(plugin.groupId).append("</groupId>")
            .append("<artifactId>").append(plugin.artifactId).append("</artifactId>")
            .append("<version>").append(plugin.version).append("</version>")
          .append("</plugin>");
    }
    
    private void addDependency(PomDependency dependency) {
        sb.append("<dependency>")
            .append("<groupId>").append(dependency.groupId).append("</groupId>")
            .append("<artifactId>").append(dependency.artifactId).append("</artifactId>")
            .append("<version>").append(dependency.version).append("</version>")
          .append("</dependency>");
    }
    
    public static class PomPlugin {
        private String groupId;
        private String artifactId;
        private String version;

        public PomPlugin(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
    }
    
    public static class PomDependency {
        private String groupId;
        private String artifactId;
        private String version;

        public PomDependency(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
    }
}
