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

package org.netbeans.modules.maven.api;

import java.io.File;
import java.util.Arrays;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class PluginPropertyUtilsTest extends NbTestCase {

    public PluginPropertyUtilsTest(String name) {
        super(name);
    }

    private FileObject d;
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
    }

    public void testGetPluginPropertyEvaluated() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><directory>${project.basedir}/build/maven/${project.artifactId}/target</directory>" +
                "<plugins>" +
                "<plugin><groupId>g</groupId><artifactId>p</artifactId><version>0</version><configuration><key>${project.reporting.outputDirectory}/stuff</key></configuration></plugin>" +
                "</plugins></build>" +
                "</project>");
        assertEquals(new File(getWorkDir(), "build/maven/a/target/site/stuff"), new File(PluginPropertyUtils.getPluginProperty(ProjectManager.getDefault().findProject(d), "g", "p", "key", null)));
    }

    public void testGetPluginPropertyNotString() throws Exception { // #207098
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><directory>${project.basedir}/build/maven/${project.artifactId}/target</directory>" +
                "<plugins>" +
                "<plugin><groupId>g</groupId><artifactId>p</artifactId><version>0</version><configuration><key/></configuration></plugin>" +
                "</plugins></build>" +
                "</project>");
        assertNull(null, PluginPropertyUtils.getPluginProperty(ProjectManager.getDefault().findProject(d), "g", "p", "key", null));
    }

    public void testGetReportPluginVersionM2() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<reporting><plugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>17</version></plugin>" +
                "</plugins></reporting>" +
                "</project>");
        assertEquals("17", PluginPropertyUtils.getReportPluginVersion(ProjectManager.getDefault().findProject(d).getLookup().lookup(NbMavenProject.class).getMavenProject(), "g", "r"));
    }

    public void testGetReportPluginVersionM3() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-site-plugin</artifactId><version>3.0</version><configuration><reportPlugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>17</version></plugin>" +
                "</reportPlugins></configuration></plugin></plugins></build>" +
                "</project>");
        assertEquals("17", PluginPropertyUtils.getReportPluginVersion(ProjectManager.getDefault().findProject(d).getLookup().lookup(NbMavenProject.class).getMavenProject(), "g", "r"));
    }

    public void testGetReportPluginPropertyM2() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<reporting><plugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>0</version><configuration><key>value</key></configuration></plugin>" +
                "</plugins></reporting>" +
                "</project>");
        assertEquals("value", PluginPropertyUtils.getReportPluginProperty(ProjectManager.getDefault().findProject(d), "g", "r", "key", null));
    }

    public void testGetReportPluginPropertyM3() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-site-plugin</artifactId><version>3.0</version><configuration><reportPlugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>0</version><configuration><key>value</key></configuration></plugin>" +
                "</reportPlugins></configuration></plugin></plugins></build>" +
                "</project>");
        assertEquals("value", PluginPropertyUtils.getReportPluginProperty(ProjectManager.getDefault().findProject(d), "g", "r", "key", null));
    }

    public void testGetReportPluginPropertyListM2() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<reporting><plugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>0</version><configuration><things><thing>one</thing><thing>two</thing></things></configuration></plugin>" +
                "</plugins></reporting>" +
                "</project>");
        assertEquals("[one, two]", Arrays.toString(PluginPropertyUtils.getReportPluginPropertyList(ProjectManager.getDefault().findProject(d), "g", "r", "things", "thing", null)));
    }

    public void testGetReportPluginPropertyListM3() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-site-plugin</artifactId><version>3.0</version><configuration><reportPlugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>0</version><configuration><things><thing>one</thing><thing>two</thing></things></configuration></plugin>" +
                "</reportPlugins></configuration></plugin></plugins></build>" +
                "</project>");
        assertEquals("[one, two]", Arrays.toString(PluginPropertyUtils.getReportPluginPropertyList(ProjectManager.getDefault().findProject(d), "g", "r", "things", "thing", null)));
    }
    public void testGetCompilerArgs() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project>"
                        + "<modelVersion>4.0.0</modelVersion>"
                        + "<groupId>g</groupId>"
                        + "<artifactId>a</artifactId>"
                        + "<version>0</version>"
                        + "<build>"
                        + "<plugins><plugin>"
                        + "<groupId>org.apache.maven.plugins</groupId>"
                        + "<artifactId>maven-compiler-plugin</artifactId>"
                        + "<version>3.8.0</version>"
                        + "<configuration><compilerArgs><arg>--enable-preview</arg></compilerArgs>"
                        + "</configuration>"
                        + "</plugin></plugins></build></project>");
        assertEquals("[--enable-preview]", Arrays.toString(PluginPropertyUtils.getPluginPropertyList(ProjectManager.getDefault().findProject(d), "org.apache.maven.plugins", "maven-compiler-plugin", "compilerArgs", "arg", null)));
    }

}
