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
package org.netbeans.modules.maven.runjar;

import java.io.File;
import java.io.InputStream;
import org.netbeans.modules.maven.execute.MavenExecutionTestBase;
import java.io.StringReader;
import java.util.Map;
import junit.framework.Test;
import org.junit.Assume;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.execute.ModelRunConfig;
import org.netbeans.modules.maven.runjar.MavenExecuteUtils.ExecutionEnvHelper;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public abstract class ExecutionEnvHelperTest extends MavenExecutionTestBase {
    public ExecutionEnvHelperTest(String name) {
        super(name);
    }
    
    protected abstract void initCustomizedProperties();
    protected abstract void initDefaultProperties();
    protected abstract String defaultCommandLineArgs();
    

    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite("ExecutionHelperTest");
        suite.addTest(new NbTestSuite(SpecialRunConfiguration.class));
        suite.addTest(new NbTestSuite(NetBeansSplitConfig.class));
        suite.addTest(new NbTestSuite(NetBeans123Config.class));
        return suite;
    }
    
    protected void assertActionCustomVMProperties(String vmArg, String mainClass, String appArg) throws Exception {}

    protected void assertActionWorkingDir(String workingDir) throws Exception {}

    protected void assertActionEnvVariable(String varName, String varValue) throws Exception {}
    
    public static class SpecialRunConfiguration extends NetBeansSplitConfig {

        public SpecialRunConfiguration(String name) {
            super(name);
        }

        @Override
        protected void setUp() throws Exception {
            super.setUp();
            clearWorkDir();
            FileObject root = FileUtil.getConfigFile("Projects/org-netbeans-modules-maven/RunGoals");
            if (root.getFileObject("foobar") == null) {
                FileObject foobar = root.createData("foobar");
                foobar.setAttribute("alias", "org.netbeans.test.mojo:test-plugin");
                FileObject goal = root.createData("org.netbeans.test.mojo:test-plugin");
                goal.setAttribute("goals", "test-run-goal");
            }
        }

        @Override
        protected void assertRunGoalName() {
            assertTrue(runMapping.getGoals().toString().contains(":test-run-goal"));
        }

        @Override
        protected InputStream getActionResourceStream() {
            return getClass().getResourceAsStream("custom-test-goals.xml");
        }
        
        /**
         * Sets up project's split properties - individual parts split to separate
         * properties (vm args, app args, main class).
         */
        @Override
        protected void initDefaultProperties() {
            super.initDefaultProperties();
            runP.put("custom.vmArgs", "${exec.vmArgs}");
            runP.put("custom.appArgs", "${exec.appArgs}");

            profileP.putAll(runP);
            debugP.putAll(runP);
        }

        @Override
        protected void assertActionCustomVMProperties(String vmArg, String mainClass, String appArg) throws Exception {
            super.assertActionOverridesArguments(vmArg, mainClass, appArg);
            
            // check that the split properties are populated
            assertEquals(vmArg, mavenExecutorDefines.get("custom.vmArgs"));
            assertEquals(appArg, mavenExecutorDefines.get("custom.appArgs"));
            if (mainClass != null) {
                assertEquals(mainClass, mavenExecutorDefines.get(MavenExecuteUtils.RUN_MAIN_CLASS));
            }
        }

        @Override
        protected String defaultCommandLineArgs() {
            return MavenExecuteUtils.DEFAULT_EXEC_ARGS_CLASSPATH2;
        }

        @Override
        public void test123DefaultActionWithVMAddition() throws Exception {
            Assume.assumeTrue("Reading from POM is not supported for custom goals yet", false);
        }
    }

    public static class NetBeans123Config extends ExecutionEnvHelperTest {

        public NetBeans123Config(String name) {
            super(name);
        }

        /**
         * Sets up properties for pre-12.3 maven projects, with some customizations
         * <ul>
         * <li>a system property
         * <li>classpath
         * <li>an application parameter
         * </ul>
         */
        protected void initCustomizedProperties() {
            initDefaultProperties();
            runP.put("exec.args", "-Dprop=val -classpath %classpath test.mavenapp.App param1");
            profileP.putAll(runP);
            debugP.put("exec.args", "-agentlib:jdwp=transport=dt_socket,server=n,address=${jpda.address} -Dprop=val -classpath %classpath test.mavenapp.App param1");
        }

        /**
         * Sets up DEFAULT pre-12.3 properties, no new properties will be defined at all. Use to test
         * compatibility with pre-existing projects.
         */
        protected void initDefaultProperties() {
            runP.remove("exec.vmArgs");
            runP.remove("exec.appArgs");
            runP.remove("exec.mainClass");
            runP.put("exec.executable", "java");
            runP.put("exec.args", MavenExecuteUtils.DEFAULT_EXEC_ARGS_CLASSPATH);
            profileP.putAll(runP);

            debugP.put("exec.args", MavenExecuteUtils.DEFAULT_DEBUG_PARAMS + " " + MavenExecuteUtils.DEFAULT_EXEC_ARGS_CLASSPATH);
            debugP.put("exec.executable", "java");
        }

        @Override
        protected String defaultCommandLineArgs() {
            return MavenExecuteUtils.DEFAULT_EXEC_ARGS_CLASSPATH;
        }
    }
    
    public static class NetBeansSplitConfig extends ExecutionEnvHelperTest {

        public NetBeansSplitConfig(String name) {
            super(name);
        }
        
        
        /**
         * Sets up project's split properties - individual parts split to separate
         * properties (vm args, app args, main class).
         */
        protected void initDefaultProperties() {
            runP.put("exec.executable", "java");
            runP.put("exec.mainClass", "${packageClassName}");
            runP.put("exec.vmArgs", "");
            runP.put("exec.appArgs", "");
            runP.put("exec.args", MavenExecuteUtils.DEFAULT_EXEC_ARGS_CLASSPATH2);

            profileP.putAll(runP);
            debugP.putAll(runP);

            debugP.put("exec.args", MavenExecuteUtils.DEFAULT_DEBUG_PARAMS + " " + MavenExecuteUtils.DEFAULT_EXEC_ARGS_CLASSPATH2);
            debugP.put("exec.executable", "java");
        }

        /**
         * Sets up customized split properties. A VM arg, app arg and a defined main class are present.
         */
        protected void initCustomizedProperties() {
            initDefaultProperties();
            runP.put("exec.vmArgs", "-Dprop=val");
            runP.put("exec.mainClass", "test.mavenapp.App");
            runP.put("exec.appArgs", "param1");
            profileP.putAll(runP);

            debugP.put("exec.vmArgs", "-Dprop=val");
            debugP.put("exec.mainClass", "test.mavenapp.App");
            debugP.put("exec.appArgs", "param1");
        }

        @Override
        protected void assertActionCustomVMProperties(String vmArg, String mainClass, String appArg) throws Exception {
            super.assertActionOverridesArguments(vmArg, mainClass, appArg);
            
            // check that the split properties are populated
            assertEquals(vmArg, mavenExecutorDefines.get(MavenExecuteUtils.RUN_VM_PARAMS));
            assertEquals(appArg, mavenExecutorDefines.get(MavenExecuteUtils.RUN_APP_PARAMS));
            if (mainClass != null) {
                assertEquals(mainClass, mavenExecutorDefines.get(MavenExecuteUtils.RUN_MAIN_CLASS));
            }
        }

        @Override
        protected void assertActionWorkingDir(String workingDir) throws Exception {
            assertEquals(workingDir, mavenExecutorDefines.get(MavenExecuteUtils.RUN_WORKDIR));
        }

        @Override
        protected void assertActionEnvVariable(String varName, String varValue) throws Exception {
            assertEquals(varValue, mavenExecutorEnvironment.get(varName));
        }

        @Override
        protected String defaultCommandLineArgs() {
            return MavenExecuteUtils.DEFAULT_EXEC_ARGS_CLASSPATH2;
        }
    }

    /**
     * Checks that a pristine project has no value set, despite property references.
     */
    public void testLoadDefaultActions() throws Exception {
        FileObject pom = createPom("", "");
        
        Project project = ProjectManager.getDefault().findProject(pom.getParent());        
        loadActionMappings(project);

        M2ConfigProvider usr = project.getLookup().lookup(M2ConfigProvider.class);
        NbMavenProjectImpl mavenProject = project.getLookup().lookup(NbMavenProjectImpl.class);
        
        ExecutionEnvHelper helper = MavenExecuteUtils.createExecutionEnvHelper(mavenProject, runMapping, debugMapping, profileMapping, defaultActionMapping);
        helper.loadFromProject();
        
        assertEquals("", helper.getVmParams());
        assertEquals("", helper.getAppParams());
        assertEquals("", helper.getMainClass());
    }
    
    private ExecutionEnvHelper createAndLoadHelper() throws Exception {
        createNbActions(runP, debugP, profileP);
        createPom("", "");
        
        Project project = ProjectManager.getDefault().findProject(pom.getParent());        
        loadActionMappings(project);
        
        NbMavenProjectImpl mavenProject = project.getLookup().lookup(NbMavenProjectImpl.class);
        
        ExecutionEnvHelper helper = MavenExecuteUtils.createExecutionEnvHelper(mavenProject, runMapping, debugMapping, profileMapping, actionToGoalMap);
        helper.loadFromProject();
        return helper;
    }
    
    /**
     * Checks that exec.args are correctly split into vm, app and main class.
     */
    public void testLoadFromExecArgs() throws Exception {
        initCustomizedProperties();
        
        ExecutionEnvHelper helper = createAndLoadHelper();
        assertEquals("-Dprop=val", helper.getVmParams());
        assertEquals("param1", helper.getAppParams());
        assertEquals("test.mavenapp.App", helper.getMainClass());
    }
    
    /**
     * Checks that a change to a helper loaded from an old config will result in proper properties being set.
     * 
     * @throws Exception 
     */
    public void testChangeFromOldConfig() throws Exception {
        initCustomizedProperties();
        ExecutionEnvHelper helper = createAndLoadHelper();        
        
        helper.setAppParams("param2");
        helper.setMainClass("bar.FooBar");
        helper.setVmParams("-Dwhatever=true");
        
        helper.applyToMappings();
        
        checkActionMapping(getActionMapping("run"), "bar.FooBar", "param2", "-Dwhatever=true");
        checkActionMapping(getActionMapping("debug"), "bar.FooBar", "param2", "-Dwhatever=true " + MavenExecuteUtils.DEFAULT_DEBUG_PARAMS);
        checkActionMapping(getActionMapping("profile"), "bar.FooBar", "param2", "-Dwhatever=true");
    }
    
    /**
     * If the original config was 'mixed', that is did not contain '%classpath' in some of the actions,
     * that action's properties will not change when altered in the Helper. This is consistent with
     * previous function of the RunJarPanel.
     */
    public void testMixedConfigNotChanged() throws Exception {
        initCustomizedProperties();
        
        // remove the classpath
        runP.put("exec.args", "-Dprop=val test.mavenapp.App param1");

        ExecutionEnvHelper helper = createAndLoadHelper();        
        
        helper.setAppParams("param2");
        helper.setMainClass("bar.FooBar");
        helper.setVmParams("-Dwhatever=true");
        
        helper.applyToMappings();
        
        // changes debug and profile are OK
        checkActionMapping(getActionMapping("debug"), "bar.FooBar", "param2", "-Dwhatever=true " + MavenExecuteUtils.DEFAULT_DEBUG_PARAMS);
        checkActionMapping(getActionMapping("profile"), "bar.FooBar", "param2", "-Dwhatever=true");
        
        // but 'run' profile didn't contain %classpath, so it should not be changed at all:
        NetbeansActionMapping rm = getActionMapping("run");
        assertEquals(runP, rm.getProperties());
    }

    private void checkActionMapping(NetbeansActionMapping map, String mainClass, String appArgs, String vmArgs) {
        String execArgs = map.getProperties().get("exec.args");
        assertTrue(execArgs.contains("${exec.mainClass}"));
        assertTrue(execArgs.contains("${exec.vmArgs}"));
        assertTrue(execArgs.contains("${exec.appArgs}"));
        assertTrue(execArgs.contains("-classpath %classpath"));
        
        assertEquals(appArgs, map.getProperties().get("exec.appArgs"));
        assertEquals(vmArgs, map.getProperties().get("exec.vmArgs"));
        assertEquals(mainClass, map.getProperties().get("exec.mainClass"));
    }
    
    private void assertPOMArguments(NetbeansActionMapping mapp, String actionsDefaultArgs) throws Exception {
        Project project = ProjectManager.getDefault().findProject(pom.getParent());        
        ModelRunConfig cfg = new ModelRunConfig(project, mapp, "run", null, Lookup.EMPTY, true);
        Assume.assumeTrue(runMapping.getGoals().toString().contains(":exec"));
        assertTrue("Must contain POM vm arg", cfg.getProperties().get(MavenExecuteUtils.RUN_PARAMS).contains("-DsomeProperty=blah"));
        assertTrue("Must contain arg " + actionsDefaultArgs, cfg.getProperties().get(MavenExecuteUtils.RUN_PARAMS).contains(actionsDefaultArgs));
    }
    
    /**
     * Checks that default actions set up for 12.3 and previous projects will merge in maven
     * settings.
     * @throws Exception 
     */
    public void testMergedMappingUsesPOMArguments() throws Exception {
        initDefaultProperties();
        createNbActions(runP, debugP, profileP);
        createPomWithArguments();
        Project project = ProjectManager.getDefault().findProject(pom.getParent());        
        loadActionMappings(project);

        assertPOMArguments(runMapping, defaultCommandLineArgs());
    }
    
    /**
     * Checks that a project with no nb-actions still merges in default actions
     * + the POM settings
     * @throws Exception 
     */
    public void testDefaultNoActionsMappingUsesPOMArguments() throws Exception {
        createPomWithArguments();

        Project project = ProjectManager.getDefault().findProject(pom.getParent());        
        loadActionMappings(project);

        // there's NO action mapping generated -> the default mapping that uses split arguments should be used.
        assertPOMArguments(runMapping, MavenExecuteUtils.DEFAULT_EXEC_ARGS_CLASSPATH2);
    }
    
    void assertActionOverridesArgumentsPlusProperties(String vmArg, String mainClass, String appArg) throws Exception {
        assertActionOverridesArguments(vmArg, mainClass, appArg);
        assertActionCustomVMProperties(vmArg, mainClass, appArg);
    }
    
    void assertActionOverridesArguments(String vmArg, String mainClass, String appArg) throws Exception {
        createNbActions(runP, debugP, profileP);
        createPomWithArguments();
        if (runMapping == null) {
            final Project project = ProjectManager.getDefault().findProject(pom.getParent());
            loadActionMappings(project);
        }
        assertRunArguments(runMapping, vmArg, mainClass == null ? DEFAULT_MAIN_CLASS_TOKEN : mainClass,  appArg);
    }
    
    /**
     * Checks that POM-provided arguments are used during execution.
     */
    private void assertPOMArgumentsUsed() throws Exception {
        createPomWithArguments();
        if (runMapping == null) {
            final Project project = ProjectManager.getDefault().findProject(pom.getParent());
            loadActionMappings(project);
        }
        Assume.assumeTrue(runMapping.getGoals().toString().contains(":exec"));
        assertRunArguments(runMapping, "-DsomeProperty=blah", DEFAULT_MAIN_CLASS_TOKEN,  null);
    }
    
    /**
     * Checks that without mapping maven arguments are properly passed to exec.args, so
     * they are not overriden.
     */
    public void test123DefaultProjectPassesPOMArguments() throws Exception {
        initDefaultProperties();
        assertPOMArgumentsUsed();
    }
    
    /**
     * Checks that a default, not customized mapping (just exec.args) properly passes POM arguments if
     * exec.args does not define any (just the default)
     */
    public void test123WithActionsAndNoArgsPassesPOMArguments() throws Exception {
        initDefaultProperties();
        createNbActions(runP, debugP, profileP);
        assertPOMArgumentsUsed();
    }
    
    /**
     * Checks that argument in mapping's exec.args overrides POM arguments
     */
    public void test123WithActionsArgumentsOverridePOM() throws Exception {
        initDefaultProperties();
        runP.put(MavenExecuteUtils.RUN_PARAMS, 
                "-DdifferentProperty=blurb " + defaultCommandLineArgs() + " param2 prevParam");
        assertActionOverridesArguments("-DdifferentProperty=blurb", DEFAULT_MAIN_CLASS_TOKEN, "param2 prevParam");
    }
    
    
    /**
     * Checks that if a mapping defines arguments, they are used in preference to the 
     * POM ones.
     */
    public void testCustomMappingPassesArguments() throws Exception {
        initCustomizedProperties();
        assertActionOverridesArgumentsPlusProperties("-Dprop=val", "test.mavenapp.App", "param1");
    }
    
    /**
     * Checks that pre-12.3 default actions will inject VM arguments and arguments from Lookup.
     */
    public void test123DefaultActionWithVMAddition() throws Exception {
        initDefaultProperties();
        createNbActions(runP, debugP, profileP);
        ExplicitProcessParameters explicit = ExplicitProcessParameters.builder().
                launcherArg("-DvmArg2=2").
                arg("paramY").build();
        actionData.add(explicit);
        createPomWithArguments();
        assertActionOverridesArgumentsPlusProperties("-DsomeProperty=blah -DvmArg2=2", null, "paramY");
        // check that default pom arguments are ALSO present
//        assertTrue(mavenVmArgs.contains("-DsomeProperty="));
    }
    
    /**
     * Checks that pre-12.3 default actions will inject arguments from Lookup. VM args
     * should be added <b>in addition to the existing ones</b> while application args
     * should be replaced.
     */
    public void test123DefaultActionWithVMReplacement() throws Exception {
        initDefaultProperties();
        createNbActions(runP, debugP, profileP);
        ExplicitProcessParameters explicit = ExplicitProcessParameters.builder().
                launcherArg("-DvmArg2=2").
                replaceLauncherArgs(true).
                arg("paramY").build();
        actionData.add(explicit);
        createPomWithArguments();
        assertActionOverridesArguments("-DvmArg2=2", null, "paramY");
        // check that default pom arguments are not present
        assertFalse(mavenVmArgs.contains("-DsomeProperty="));
    }
    
    /**
     * New actions: Checks that explicit params by default _append_ VM args and
     * replaces args.
     */
    public void testNewActionWithVMAdditionAndArgReplacement() throws Exception {
        initCustomizedProperties();
        ExplicitProcessParameters explicit = ExplicitProcessParameters.builder().
                launcherArg("-DvmArg2=2").
                arg("paramY").build();
        actionData.add(explicit);
        assertActionOverridesArguments("-DvmArg2=2", "test.mavenapp.App", "paramY");
        // check that default pom arguments are not present
        assertTrue(mavenVmArgs.contains("-Dprop=val"));
        // by default arguments are replaced:
        assertFalse(mavenAppArgs.contains("param1"));
    }
    
    /**
     * New actions: checks that appended VM args are also merged with a
     * startup extender
     */
    public void testNewActionVMAppendMergesWithExtenders() throws Exception {
        initCustomizedProperties();
        ExplicitProcessParameters explicit = ExplicitProcessParameters.builder().
                launcherArg("-DvmArg2=2").
                arg("paramY").build();
        registerExtender(null);
        TestExtender.vmArg = "-Dbar=foo";
        actionData.add(explicit);
        createPomWithArguments();
        assertActionOverridesArguments("-DvmArg2=2", "test.mavenapp.App", "paramY");
        // check that default pom arguments are not present
        assertTrue(mavenVmArgs.contains("-Dprop=val"));
        assertTrue(mavenVmArgs.contains("-Dbar=foo"));
    }
    
    /**
     * New actions: checks that appended VM args are merged with a
     * startup extender EVEN If config args are replaced.
     */
    public void testNewActionVMReplaceStillMergesWithExtenders() throws Exception {
        initCustomizedProperties();
        ExplicitProcessParameters explicit = ExplicitProcessParameters.builder().
                replaceLauncherArgs(true).
                launcherArg("-DvmArg2=2").
                arg("paramY").build();
        registerExtender(null);
        TestExtender.vmArg = "-Dbar=foo";
        actionData.add(explicit);
        assertActionOverridesArguments("-DvmArg2=2", "test.mavenapp.App", "paramY");
        assertFalse(mavenVmArgs.contains("-Dprop=val"));
        assertTrue(mavenVmArgs.contains("-Dbar=foo"));
    }

    /**
     * Checks that pre-12.3 default actions will inject working directory from Lookup.
     */
    public void test123DefaultActionWithCWD() throws Exception {
        initCustomizedProperties();
        createNbActions(runP, debugP, profileP);
        File wd = new File("WorkingDirectory");
        ExplicitProcessParameters explicit = ExplicitProcessParameters.builder().
                workingDirectory(wd).build();
        actionData.add(explicit);
        createPomWithArguments();
        final Project project = ProjectManager.getDefault().findProject(pom.getParent());
        loadActionMappings(project);
        assertMavenRunAction(project, runMapping, "run", c -> {});
        assertActionWorkingDir(wd.getAbsolutePath());
    }

    /**
     * Checks that pre-12.3 default actions will inject environment variables from Lookup.
     */
    public void test123DefaultActionWithEnvVars() throws Exception {
        initCustomizedProperties();
        createNbActions(runP, debugP, profileP);
        ExplicitProcessParameters explicit = ExplicitProcessParameters.builder().
                environmentVariable("TEST_VAR1", "test value 1").
                environmentVariable("TEST_VAR2", "test value 2").
                environmentVariable("PATH", null).build();
        actionData.add(explicit);
        createPomWithArguments();
        final Project project = ProjectManager.getDefault().findProject(pom.getParent());
        loadActionMappings(project);
        assertMavenRunAction(project, runMapping, "run", c -> {});
        assertActionEnvVariable("TEST_VAR1", "test value 1");
        assertActionEnvVariable("TEST_VAR2", "test value 2");
        assertActionEnvVariable("PATH", null);
    }

    private ExecutionEnvHelper load123ProjectExecutionHelper(boolean defaultProps) throws Exception {
        if (defaultProps) {
            initDefaultProperties();
        } else {
            initCustomizedProperties();
        }
        return loadExecutionHelper();
    }
    
    private ExecutionEnvHelper loadExecutionHelper() throws Exception {
        createPomWithArguments();
        FileObject actions = createNbActions(runP, debugP, profileP);
        
        Project project = ProjectManager.getDefault().findProject(pom.getParent());
        NbMavenProjectImpl mavenProject = project.getLookup().lookup(NbMavenProjectImpl.class);
        actionToGoalMap = new NetbeansBuildActionXpp3Reader().read(new StringReader(actions.asText()));
        
        loadActionMappings(project);
        return MavenExecuteUtils.createExecutionEnvHelper(mavenProject, runMapping, debugMapping, profileMapping, actionToGoalMap);
    }
    
    private void assertActionProperties(NetbeansActionMapping m, String vmParams, String params, String mainClass, String workDir) {
        Map<? extends String, ? extends String> p = m.getProperties();
        if (mainClass == null || mainClass.isEmpty()) {
            mainClass = MavenExecuteUtils.PACKAGE_CLASS_NAME_TOKEN;
        }
        assertEquals(MavenExecuteUtils.DEFAULT_EXEC_ARGS_CLASSPATH2, p.get(MavenExecuteUtils.RUN_PARAMS));
        assertEquals(vmParams, p.get(MavenExecuteUtils.RUN_VM_PARAMS));
        assertEquals(params, p.get(MavenExecuteUtils.RUN_APP_PARAMS));
        assertEquals(mainClass, p.get(MavenExecuteUtils.RUN_MAIN_CLASS));
        assertEquals(workDir, p.get(MavenExecuteUtils.RUN_WORKDIR));
    }
    
    /**
     * Checks that a config loaded from 12.3 maven project + change in VM params will
     * rephrase the mapping.
     */
    public void testLoad123ConfigChangeVMParams() throws Exception {
        ExecutionEnvHelper helper = load123ProjectExecutionHelper(false);
        helper.loadFromProject();
        // have \n in params
        helper.setVmParams("-Done=two\n-Dthree=four");
        helper.applyToMappings();
        
        // no \n in vm args !
        assertActionProperties(runMapping, "-Done=two -Dthree=four", "param1", helper.getMainClass(), null);
    }

    /**
     * Checks that a config loaded from 12.3 maven project + change in VM params will
     * rephrase the mapping.
     */
    public void testLoad123ConfigChangeAppParams() throws Exception {
        ExecutionEnvHelper helper = load123ProjectExecutionHelper(false);
        helper.loadFromProject();
        helper.setAppParams("Blah blah");
        helper.applyToMappings();
        
        // no \n in vm args !
        assertActionProperties(runMapping, "-Dprop=val", "Blah blah", helper.getMainClass(), null);
    }

    /**
     * Checks that a config loaded from 12.3 maven project + change in VM params will
     * rephrase the mapping.
     */
    public void testLoad123ConfigChangeMainClass() throws Exception {
        ExecutionEnvHelper helper = load123ProjectExecutionHelper(false);
        helper.loadFromProject();
        helper.setMainClass("org.Maven.Test");
        helper.applyToMappings();
        
        // no \n in vm args !
        assertActionProperties(runMapping, "-Dprop=val", "param1", "org.Maven.Test", null);
        assertRunArguments(runMapping, "-Dprop=val", "org.Maven.Test", "param1");
    }
    
    /**
     * Checks that a config loaded from 12.3 maven project + change in VM params will
     * rephrase the mapping.
     */
    public void testLoad123ConfigSetMainClassFromDefault() throws Exception {
        ExecutionEnvHelper helper = load123ProjectExecutionHelper(true);
        helper.loadFromProject();
        helper.setMainClass("org.Maven.Test");
        helper.applyToMappings();
        
        // no \n in vm args !
        assertActionProperties(runMapping, "", "", "org.Maven.Test", null);
        // VM or args NOT taken from the POM - the same as with 12.3 implementation. Maybe
        // could be treated as a bug.
        assertRunArguments(runMapping, "", "org.Maven.Test", null);
        assertEquals("", mavenAppArgs);
    }
    
    /**
     * Checks that the configured goal is the expected one, so that tests test correct behaviour.
     * Override in subclasses that modify goals.
     */
    protected void assertRunGoalName() {
        assertTrue(runMapping.getGoals().toString().contains(":exec"));
    }
    
    public void testEnvHelperConfigurable() throws Exception {
        initCustomizedProperties();

        FileObject pom = createPom("", "");
        createNbActions(runP, debugP, profileP);

        Project project = ProjectManager.getDefault().findProject(pom.getParent());        
        loadActionMappings(project);

        NbMavenProjectImpl mavenProject = project.getLookup().lookup(NbMavenProjectImpl.class);
        ExecutionEnvHelper helper = MavenExecuteUtils.createExecutionEnvHelper(mavenProject, runMapping, debugMapping, profileMapping, defaultActionMapping);
        helper.loadFromProject();
        
        assertRunGoalName();
        assertTrue(helper.isValid());
    }
}
