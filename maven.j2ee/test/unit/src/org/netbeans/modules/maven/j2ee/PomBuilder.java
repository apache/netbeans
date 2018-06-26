/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
