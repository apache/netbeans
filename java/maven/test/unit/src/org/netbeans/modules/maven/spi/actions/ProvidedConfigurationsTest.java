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
package org.netbeans.modules.maven.spi.actions;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.MavenConfiguration;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.modules.maven.execute.MockMavenExec;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider.ResourceConfigAwareProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author sdedic
 */
public class ProvidedConfigurationsTest extends NbTestCase {

    public ProvidedConfigurationsTest(String name) {
        super(name);
    }
    
    private FileObject pomFile;
    
    private void setupOKProject() throws Exception {
        File f = TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), 
             "<project xmlns='http://maven.apache.org/POM/4.0.0'>"
            + "<properties>"
            + " <exec.mainClass>org.netbeans.modules.maven.test.Clazz</exec.mainClass>"
            + "</properties>"
            + "  <modelVersion>4.0.0</modelVersion>" 
            + "  <artifactId>m</artifactId>" 
            + "  <groupId>g</groupId>"
            + "    <version>0</version>"
            + "</project>");
        
        pomFile = FileUtil.toFileObject(f);
        
    }
    
    /**
     * Checks that the contributed action is enabled on the default configuration.
     */
    public void testExtraActionEnabledInDefaultConfig() throws Exception {
        setupOKProject();
        Project p = FileOwnerQuery.getOwner(pomFile);
        assertNotNull(p);
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        
        assertTrue(Arrays.asList(ap.getSupportedActions()).contains("run-extra"));
        assertTrue(ap.isActionEnabled("run-extra", Lookup.EMPTY));
    }
    
    /**
     * Checks that the contributed action is inherited into a specific
     * configuration with no override.
     */
    public void testExtraActionEnabledInProvidedConfig() throws Exception {
        setupOKProject();
        Project p = FileOwnerQuery.getOwner(pomFile);
        assertNotNull(p);
        ProjectConfigurationProvider<MavenConfiguration> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        MavenConfiguration c = pcp.getConfigurations().stream().filter(x -> "Micronaut: dev mode".equals(x.getDisplayName())).findAny().get();
        pcp.setActiveConfiguration(c);
        
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        assertTrue(ap.isActionEnabled("run-extra", Lookup.EMPTY));
    }

    /**
     * Checks that a profile-specific action is reported from {@link ActionProvider#getSupportedActions()}
     * but is disabled.
     */
    public void testProfileSpecificActionPresentAndDisabled() throws Exception {
        setupOKProject();
        Project p = FileOwnerQuery.getOwner(pomFile);
        assertNotNull(p);
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        
        assertTrue(Arrays.asList(ap.getSupportedActions()).contains("run-specific"));
        assertFalse(ap.isActionEnabled("run-specific", Lookup.EMPTY));
    }
    
    /**
     * Checks that a profile-specific action is missing from the default configuration
     */
    public void testSpecificConfigurationHasExtraActions() throws Exception {
        setupOKProject();
        Project p = FileOwnerQuery.getOwner(pomFile);
        assertNotNull(p);
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        
        ProjectConfigurationProvider<MavenConfiguration> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        MavenConfiguration c = pcp.getConfigurations().stream().filter(x -> "Micronaut: dev mode".equals(x.getDisplayName())).findAny().get();
        pcp.setActiveConfiguration(c);

        assertTrue(Arrays.asList(ap.getSupportedActions()).contains("run-specific"));
        assertTrue(ap.isActionEnabled("run-specific", Lookup.EMPTY));
    }
    
    /**
     * Checks that configuration-specific overrides apply, compared to the default
     * configuration.
     */
    public void testSpecificConfigurationGoalOverride() throws Exception {
        setupOKProject();
        Project p = FileOwnerQuery.getOwner(pomFile);
        assertNotNull(p);
        
        NbMavenProjectImpl pimpl = p.getLookup().lookup(NbMavenProjectImpl.class);
        RunConfig cfg = ActionToGoalUtils.createRunConfig("run", pimpl, Lookup.EMPTY);
        assertEquals(Arrays.asList(
                "process-classes",
                "org.codehaus.mojo:exec-maven-plugin:3.1.0:exec"), cfg.getGoals());
        
        ProjectConfigurationProvider<MavenConfiguration> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        MavenConfiguration c = pcp.getConfigurations().stream().filter(x -> "Micronaut: dev mode".equals(x.getDisplayName())).findAny().get();
        pcp.setActiveConfiguration(c);

        RunConfig cfg2 = ActionToGoalUtils.createRunConfig("run", pimpl, Lookup.EMPTY);
        assertEquals(Arrays.asList(
                "mn:run"), cfg2.getGoals());
    }
    
    /**
     * Checks that a nbactions.xml will override contributed action's defaults.
     */
    public void testContributedActionCanCustomize() throws Exception {
        clearWorkDir();
        setupOKProject();
        try (OutputStream o = pomFile.getParent().createAndOpen("nbactions.xml");
            OutputStreamWriter w = new OutputStreamWriter(o)) {
            w.write(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "    <actions>\n" +
                "        <action>\n" +
                "            <actionName>run-extra</actionName>\n" +
                "            <packagings>\n" +
                "                <packaging>jar</packaging>\n" +
                "            </packagings>\n" +
                "            <goals>\n" +
                "                <goal>boo:boo</goal>\n" +
                "            </goals>\n" +
                "            <properties>\n" +
                "                <exec.vmArgs></exec.vmArgs>\n" +
                "                <exec.args>${exec.vmArgs} -classpath %classpath ${exec.mainClass} ${exec.appArgs}</exec.args>\n" +
                "                <exec.appArgs></exec.appArgs>\n" +
                "                <exec.mainClass>${packageClassName}</exec.mainClass>\n" +
                "            </properties>\n" +
                "        </action>\n" +
                "    </actions>\n" +
                ""
            );
        }
        Project p = FileOwnerQuery.getOwner(pomFile);
        NbMavenProjectImpl pimpl = p.getLookup().lookup(NbMavenProjectImpl.class);
        RunConfig cfg = ActionToGoalUtils.createRunConfig("run-extra", pimpl, Lookup.EMPTY);
        assertEquals(Arrays.asList("boo:boo"), cfg.getGoals());
    }
    
    /**
     * Checks that a nbactions.xml will override contributed action's defaults.
     */
    public void testContributedProfileActionCanCustomize() throws Exception {
        clearWorkDir();
        setupOKProject();
        try (OutputStream o = pomFile.getParent().createAndOpen("nbactions-micronaut-auto.xml");
            OutputStreamWriter w = new OutputStreamWriter(o)) {
            w.write(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "    <actions>\n" +
                "        <action>\n" +
                "            <actionName>run-extra</actionName>\n" +
                "            <packagings>\n" +
                "                <packaging>jar</packaging>\n" +
                "            </packagings>\n" +
                "            <goals>\n" +
                "                <goal>moo:moo</goal>\n" +
                "            </goals>\n" +
                "            <properties>\n" +
                "                <exec.vmArgs></exec.vmArgs>\n" +
                "                <exec.args>${exec.vmArgs} -classpath %classpath ${exec.mainClass} ${exec.appArgs}</exec.args>\n" +
                "                <exec.appArgs></exec.appArgs>\n" +
                "                <exec.mainClass>${packageClassName}</exec.mainClass>\n" +
                "            </properties>\n" +
                "        </action>\n" +
                "    </actions>\n" +
                ""
            );
        }
        Project p = FileOwnerQuery.getOwner(pomFile);
        NbMavenProjectImpl pimpl = p.getLookup().lookup(NbMavenProjectImpl.class);
        RunConfig cfg = ActionToGoalUtils.createRunConfig("run-extra", pimpl, Lookup.EMPTY);
        assertEquals(Arrays.asList("mn:run"), cfg.getGoals());

        ProjectConfigurationProvider<MavenConfiguration> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        MavenConfiguration c = pcp.getConfigurations().stream().filter(x -> "Micronaut: dev mode".equals(x.getDisplayName())).findAny().get();
        pcp.setActiveConfiguration(c);

        RunConfig cfg2 = ActionToGoalUtils.createRunConfig("run-extra", pimpl, Lookup.EMPTY);
        assertEquals(Arrays.asList(
                "moo:moo"), cfg2.getGoals());
    }
    
    /**
     * Test that attempts to launch an action in a specific configuration, used as an example in 
     * the Javadoc.
     */
    public void testExampleProviderConfigurationUsage() throws Exception {
        MockMavenExec mme = new MockMavenExec();
        MockMavenExec.Reporter r = new MockMavenExec.Reporter();
        MockLookup.setLayersAndInstances(mme);
        
        setupOKProject();
        
        FileObject theFile = pomFile;

        Project p = FileOwnerQuery.getOwner(pomFile);
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        ProjectConfigurationProvider<MavenConfiguration> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        ProjectConfiguration configToUse = pcp.getConfigurations().stream().
                filter(x -> "Micronaut: dev mode".equals(x.getDisplayName())).findAny().get();
        Lookup ctx = Lookups.fixed(theFile, configToUse, r);
        if (!ap.isActionEnabled(ActionProvider.COMMAND_RUN, ctx)) {
            // action not enabled
            return;
        }
        ap.invokeAction(ActionProvider.COMMAND_RUN, ctx);
        
        r.executedLatch.await();
        assertEquals(Arrays.asList("mn:run"), r.executedConfig.getGoals());
    }
    
    @ProjectServiceProvider(service = MavenActionsProvider.class, projectType = NbMavenProject.TYPE)
    public static class P extends ResourceConfigAwareProvider {
        public P(Project prj) {
            super(prj, P.class.getResource("providedActions.xml"));
        }
    }
}
