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

package org.netbeans.modules.maven.api.customizer;

import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class ModelHandle2Test extends NbTestCase {

    public ModelHandle2Test(String name) {
        super(name);
    }

    private FileObject d;

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
    }

    public void testModifyActiveConfig() throws Exception { // #200772
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version><profiles><profile><id>jetty</id></profile></profiles></project>");
        TestFileUtils.writeFile(d, "nbactions.xml", "<actions><action><actionName>run</actionName><goals><goal>package</goal></goals></action></actions>");
        FileObject nbactionsJetty = TestFileUtils.writeFile(d, "nbactions-jetty.xml", "<actions><action><displayName>Jetty</displayName><actionName>run</actionName><goals><goal>jetty:run</goal></goals><properties><someprop>v</someprop></properties></action></actions>");
        Project project = ProjectManager.getDefault().findProject(d);
        M2ConfigProvider cp = project.getLookup().lookup(M2ConfigProvider.class);
        for (M2Configuration c : cp.getConfigurations()) {
            if (c.getId().equals("jetty")) {
                cp.setActiveConfiguration(c);
            }
        }
        assertEquals("jetty", cp.getActiveConfiguration().getId());
        NetbeansActionMapping mapp = ModelHandle2.getMapping("run", project, cp.getActiveConfiguration());
        assertNotNull(mapp);
        assertEquals("Jetty", mapp.getDisplayName());
        Map<String,String> props = mapp.getProperties();
        assertNotNull(props);
        assertEquals("{someprop=v}", props.toString());
        props.remove("someprop");
        ModelHandle2.putMapping(mapp, project, cp.getActiveConfiguration());
        assertEquals("jetty", cp.getActiveConfiguration().getId());
        assertFalse(nbactionsJetty.asText().contains("someprop"));
    }
    
    public void testModifyActiveConfigInOneFile() throws Exception { // #200772
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version><profiles><profile><id>jetty</id></profile></profiles></project>");
        TestFileUtils.writeFile(d, "nbactions.xml", "<actions><action><actionName>run</actionName><goals><goal>package</goal></goals></action>" +
            "<profiles><profile><id>jetty</id><actions><action><displayName>Jetty</displayName>"
                + "<actionName>run</actionName><goals><goal>jetty:run</goal></goals>"
                + "<properties><someprop>v</someprop></properties></action>"
                + "</actions></profile></profiles>"
                + "</actions>");
        Project project = ProjectManager.getDefault().findProject(d);
        M2ConfigProvider cp = project.getLookup().lookup(M2ConfigProvider.class);
        M2Configuration conf = null;
        for (M2Configuration c : cp.getConfigurations()) {
            if (c.getId().equals("jetty")) {
                cp.setActiveConfiguration(c);
                conf = c;
                break;
            }
        }
        assertNotNull("Configuration found", conf);
        assertEquals("jetty", cp.getActiveConfiguration().getId());
        NetbeansActionMapping mapp = ModelHandle2.getMapping("run", project, cp.getActiveConfiguration());
        assertNotNull(mapp);
        assertEquals("Jetty", mapp.getDisplayName());
        Map<String,String> props = mapp.getProperties();
        assertNotNull(props);
        assertEquals("{someprop=v}", props.toString());

        {
            RunConfig run = conf.createConfigForDefaultAction("run", project, project.getLookup());
            assertEquals("One goal", 1, run.getGoals().size());
            assertEquals("jetty:run", run.getGoals().get(0));
        }
        
        {
            RunConfig run = ActionToGoalUtils.createRunConfig("run", (NbMavenProjectImpl) project, project.getLookup());
            assertEquals("One goal", 1, run.getGoals().size());
            assertEquals("jetty:run", run.getGoals().get(0));
            assertEquals("Profile activated in profile action: " + run.getActivatedProfiles(), 1, run.getActivatedProfiles().size());
        }
        
    }
    
    public void testConfigInOneFileFallbacksToBaseProfile() throws Exception { // #229192
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version><profiles><profile><id>jetty</id></profile></profiles></project>");
        TestFileUtils.writeFile(d, "nbactions.xml", "<actions><action><actionName>debug</actionName><displayName>DbgJtt</displayName><goals><goal>package</goal></goals></action>" +
            "<profiles><profile><id>jetty</id><actions><action><displayName>Jetty</displayName>"
                + "<actionName>run</actionName><goals><goal>jetty:run</goal></goals>"
                + "<properties><someprop>v</someprop></properties></action>"
                + "</actions></profile></profiles>"
                + "</actions>");
        Project project = ProjectManager.getDefault().findProject(d);
        M2ConfigProvider cp = project.getLookup().lookup(M2ConfigProvider.class);
        M2Configuration conf = null;
        for (M2Configuration c : cp.getConfigurations()) {
            if (c.getId().equals("jetty")) {
                cp.setActiveConfiguration(c);
                conf = c;
                break;
            }
        }
        assertNotNull("Configuration found", conf);
        assertEquals("jetty", cp.getActiveConfiguration().getId());
        NetbeansActionMapping mapp = ModelHandle2.getMapping("debug", project, cp.getActiveConfiguration());
        assertNotNull(mapp);
        assertEquals("DbgJtt", mapp.getDisplayName());

        {
            RunConfig run = conf.createConfigForDefaultAction("debug", project, project.getLookup());
            assertEquals("One goal", 1, run.getGoals().size());
            assertEquals("package", run.getGoals().get(0));
            assertTrue("No profile activated, we are fallback: " + run.getActivatedProfiles(), run.getActivatedProfiles().isEmpty());
        }
        {
            RunConfig run = ActionToGoalUtils.createRunConfig("debug", (NbMavenProjectImpl) project, project.getLookup());
            assertEquals("One goal", 1, run.getGoals().size());
            assertEquals("package", run.getGoals().get(0));
            assertTrue("No profile activated in action, we are fallback: " + run.getActivatedProfiles(), run.getActivatedProfiles().isEmpty());
        }
    }

    public void testJettyDebugSingle() throws Exception { // #
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version><profiles><profile><id>jetty</id></profile></profiles></project>");
        TestFileUtils.writeFile(d, "nbactions.xml", 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<actions>\n" +
"        <action>\n" +
"            <actionName>run.single.main</actionName>\n" +
"            <packagings>\n" +
"                <packaging>*</packaging>\n" +
"            </packagings>\n" +
"            <goals>\n" +
"                <goal>process-classes</goal>\n" +
"                <goal>org.codehaus.mojo:exec-maven-plugin:3.5.1:exec</goal>\n" +
"            </goals>\n" +
"            <properties>\n" +
"                <exec.args>-classpath %classpath ${packageClassName}</exec.args>\n" +
"                <exec.executable>java</exec.executable>\n" +
"                <exec.classpathScope>${classPathScope}</exec.classpathScope>\n" +
"            </properties>\n" +
"        </action>\n" +
"        <action>\n" +
"            <actionName>debug.single.main</actionName>\n" +
"            <packagings>\n" +
"                <packaging>*</packaging>\n" +
"            </packagings>\n" +
"            <goals>\n" +
"                <goal>process-classes</goal>\n" +
"                <goal>org.codehaus.mojo:exec-maven-plugin:3.5.1:exec</goal>\n" +
"            </goals>\n" +
"            <properties>\n" +
"                <exec.args>-agentlib:jdwp=transport=dt_socket,server=n,address=${jpda.address} -classpath %classpath ${packageClassName}</exec.args>\n" +
"                <exec.executable>java</exec.executable>\n" +
"                <exec.classpathScope>${classPathScope}</exec.classpathScope>\n" +
"                <jpda.listen>true</jpda.listen>\n" +
"            </properties>\n" +
"        </action>\n" +
"</actions>\n" +
""
        );
        FileObject nbactionsJetty = TestFileUtils.writeFile(d, "nbactions-jetty.xml", 
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<actions>\n" +
"        <action>\n" +
"            <actionName>debug.single.main</actionName>\n" +
"            <packagings>\n" +
"                <packaging>*</packaging>\n" +
"            </packagings>\n" +
"            <goals>\n" +
"                <goal>process-classes</goal>\n" +
"                <goal>org.codehaus.mojo:exec-maven-plugin:3.5.1:exec</goal>\n" +
"            </goals>\n" +
"            <properties>\n" +
"                <exec.args>-agentlib:jdwp=transport=dt_socket,server=n,address=${jpda.address} -classpath %classpath ${packageClassName}</exec.args>\n" +
"                <exec.executable>java</exec.executable>\n" +
"                <exec.classpathScope>${classPathScope}</exec.classpathScope>\n" +
"                <jpda.listen>true</jpda.listen>\n" +
"            </properties>\n" +
"        </action>\n" +
"    </actions>\n" +
""
        );
        Project project = ProjectManager.getDefault().findProject(d);
        M2ConfigProvider cp = project.getLookup().lookup(M2ConfigProvider.class);
        for (M2Configuration c : cp.getConfigurations()) {
            if (c.getId().equals("jetty")) {
                cp.setActiveConfiguration(c);
            }
        }
        assertEquals("jetty", cp.getActiveConfiguration().getId());
        {
            RunConfig run = ActionToGoalUtils.createRunConfig("debug.single.main", (NbMavenProjectImpl) project, project.getLookup());
            assertEquals("Two goals: " + run.getGoals(), 2, run.getGoals().size());
            assertEquals("process-classes", run.getGoals().get(0));
            assertEquals("org.codehaus.mojo:exec-maven-plugin:3.5.1:exec", run.getGoals().get(1));
            assertEquals("One profile activated in action: " + run.getActivatedProfiles(), 1, run.getActivatedProfiles().size());
            assertEquals("jetty", run.getActivatedProfiles().get(0));
        }
    }
    
}
