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
package org.netbeans.modules.gradle.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import static junit.framework.TestCase.assertNotNull;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.execute.RunUtilsTest;
import org.netbeans.modules.gradle.execute.ConfigPersistenceUtilsTest;
import org.netbeans.modules.gradle.execute.GradleExecAccessor;
import org.netbeans.modules.gradle.execute.GradleExecutor;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.openide.windows.InputOutput;

/**
 *
 * @author sdedic
 */
public class ConfigurableActionsProviderImplTest {
    private FileObject projectFolder;
    
    private Project project;
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    /** Represents destination directory with NetBeans (always available). */
    protected File destDirF;

    private static File getTestNBDestDir() {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }

    @Test
    public void loadCustomizedDefaultActions() {
        
    }

    @Before
    public void setUp() {
        destDirF = getTestNBDestDir();
        DummyInstalledFileLocator.registerDestDir(destDirF);
    }
    
    @After
    public void tearDown() {
        // reset to default
        MockLookup.setLayersAndInstances();
        RunUtilsTest.setExecutonFactory(null);
    }
    
    private void createGradleProject() throws Exception {
        projectFolder = FileUtil.toFileObject(tempFolder.newFolder());
        FileUtil.createFolder(projectFolder, "src/main/java");
        TestFileUtils.writeFile(projectFolder, "build.gradle",
                "plugins {\n" +
                "    id(\"java-library-distribution\")\n" +
                "}"
                            
        );
        project = ProjectManager.getDefault().findProject(projectFolder);
    }
    
    private void openProject() throws Exception {
        OpenProjects.getDefault().open(new Project[] { project }, false);
        OpenProjects.getDefault().openProjects().get();
    }
    
    private void primeProject() throws Exception {
        // new project, so try to load it:
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        final CountDownLatch waitPrime = new CountDownLatch(1);
        ap.invokeAction(ActionProvider.COMMAND_PRIME, Lookups.fixed(new ActionProgress() {
            @Override
            protected void started() {
            }

            @Override
            public void finished(boolean success) {
                waitPrime.countDown();
            }
        }));
        waitPrime.await();
    }
    
    /**
     * Checks that a Fallback project does not know anything about the 
     */
    @Test
    public void testNoProvidedConfigWithFallback() throws Exception {
        createGradleProject();
        
        ProjectConfigurationProvider<GradleExecConfiguration> pcp = project.getLookup().lookup(ProjectConfigurationProvider.class);
        List<? extends GradleExecConfiguration> configs = new ArrayList<>(pcp.getConfigurations());
        
        assertEquals(1, configs.size());
        ConfigPersistenceUtilsTest.assertConfiguration(
                GradleExecAccessor.createDefault(), configs.get(0));
    }
    
    /**
     * Checks that configurations provided by XML-declared Provider
     * are enumerated.
     */
    @Test
    public void testFindProvidedConfigurations() throws Exception {
        createGradleProject();
        primeProject();

        ProjectConfigurationProvider<GradleExecConfiguration> pcp = project.getLookup().lookup(ProjectConfigurationProvider.class);
        List<? extends GradleExecConfiguration> configs = new ArrayList<>(pcp.getConfigurations());
        assertEquals(2, configs.size());
        
        ConfigPersistenceUtilsTest.assertConfiguration(
                GradleExecAccessor.createDefault(), configs.get(0));
        
        GradleExecConfiguration provided = configs.get(1);
        assertEquals("provided-config", provided.getId());
    }

    /**
     * Checks that provided-contributed Configs are populated by properties.
     */
    @Test
    public void testProvidedConfigLoadsWithProperties() throws Exception {
        createGradleProject();
        primeProject();

        ProjectConfigurationProvider<GradleExecConfiguration> pcp = project.getLookup().lookup(ProjectConfigurationProvider.class);
        List<? extends GradleExecConfiguration> configs = new ArrayList<>(pcp.getConfigurations());
        
        GradleExecConfiguration provided = configs.get(1);
        assertEquals("value1", provided.getProjectProperties().get("propA"));
        assertEquals("--info", provided.getCommandLineArgs());
    }
    
    /**
     * Checks that an action invoked with a specific configuration gets a specialized ActionMapping
     * @throws Exception 
     */
    @Test
    public void testInvokeWithSpecificConfiguration() throws Exception {
        createGradleProject();
        primeProject();

        ProjectConfigurationProvider<GradleExecConfiguration> pcp = project.getLookup().lookup(ProjectConfigurationProvider.class);
        List<? extends GradleExecConfiguration> configs = new ArrayList<>(pcp.getConfigurations());
        
        class MockDaemonExecutor implements GradleExecutor {
            ExecutorTask primaryTask;
            
            @Override
            public void setTask(ExecutorTask task) {
                primaryTask = task;
            }

            @Override
            public InputOutput getInputOutput() {
                return InputOutput.NULL;
            }

            @Override
            public ExecutorTask createTask(ExecutorTask process) {
                return new ExecutorTask(() -> {}) {
                    @Override
                    public void stop() {
                    }
                    
                    @Override
                    public int result() {
                        return 0;
                    }
                    
                    @Override
                    public InputOutput getInputOutput() {
                        return InputOutput.NULL;
                    }
                };
            }

            @Override
            public void run() {
                // no op
            }

            @Override
            public boolean cancel() {
                return false;
            }
        }
        
        MockDaemonExecutor inst = new MockDaemonExecutor();
        AtomicReference<RunConfig> configured = new AtomicReference<>();
        RunUtilsTest.setExecutonFactory((rc) -> {
            configured.set(rc);
            return inst;
        });
        
        
        // BEGIN:invokeActionWithConfiguraiton
        GradleExecConfiguration providedConf = configs.get(1);

        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        ap.invokeAction(ActionProvider.COMMAND_RUN_SINGLE, Lookups.fixed(providedConf));
        // END:invokeActionWithConfiguraiton
        
        assertNotEquals(providedConf, pcp.getActiveConfiguration());

        RunConfig runWith = configured.get();
        
        GradleExecConfiguration usedConfig = runWith.getExecConfig();
        assertSame(providedConf, usedConfig);
        
        // customized action for this config
        assertTrue(runWith.getCommandLine().hasFlag(GradleCommandLine.Flag.CONTINUOUS));
    }
}
