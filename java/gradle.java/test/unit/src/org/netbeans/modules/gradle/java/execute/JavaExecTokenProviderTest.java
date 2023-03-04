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
package org.netbeans.modules.gradle.java.execute;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gradle.actions.ActionToTaskUtils;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleBaseProjectTrampoline;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sdedic
 */
public class JavaExecTokenProviderTest extends NbTestCase {

    public JavaExecTokenProviderTest(String name) {
        super(name);
    }
    
    public void testTokensNotAvailableInNonJava() throws Exception {
        FileObject dataRoot = FileUtil.toFileObject(getDataDir());
        FileObject nj = dataRoot.getFileObject("nonjava");
        
        Project p = ProjectManager.getDefault().findProject(nj);
        
        JavaExecTokenProvider jetp = new JavaExecTokenProvider(p);
        assertEquals(0, jetp.getSupportedTokens().size());
        
        Collections.list(System.getProperties().propertyNames()).stream().
                filter(n -> n.toString().startsWith("test.")).
                map(Object::toString).sorted().toArray();
        
    }
    
    private Project createSimpleJavaProject() throws IOException {
        FileObject dataRoot = FileUtil.toFileObject(getDataDir());
        FileObject nj = dataRoot.getFileObject("javasimple");
        
        Project p = ProjectManager.getDefault().findProject(nj);
        return p;
    }
    
    public void testTokensAvailableWithJavaPlugin() throws Exception {
        JavaExecTokenProvider jetp = new JavaExecTokenProvider(createSimpleJavaProject());
        assertEquals(4, jetp.getSupportedTokens().size());
    }
    
    private void assertParamList(String val, String... items) {
        assertNotNull("Value present", val);
        String[] pars  = BaseUtilities.parseParameters(val);
        assertEquals(items.length + " params passed", items.length, pars.length);
        assertEquals(Arrays.asList(items), Arrays.asList(pars));
    }
    
    private void assertListInValue(String val, String... items) {
        assertNotNull("Value present", val);
        String[] check  = BaseUtilities.parseParameters(val);
        assertEquals("Passed as single arg", 1, check.length);
        
        String[] pars = BaseUtilities.parseParameters(check[0].substring(check[0].indexOf('=') + 1));
        assertEquals(items.length + " params passed", items.length, pars.length);
        assertEquals(Arrays.asList(items), Arrays.asList(pars));
    }
    
    private void assertArgs(String val, String... items) {
        assertNotNull("Value present", val);
        String[] check  = BaseUtilities.parseParameters(val);
        assertEquals("--args + param", 2, check.length);
        assertEquals("Passed with --args", "--args", check[0]);
        assertParamList(check[1], items);
    }
    
    private String[] TWO_PROPS = {
        "-Dtest.foo=bar",
        "-Dtest.bar=foo"
    };
    
    private String[] THREE_PROPS = {
        "-Dtest.foo=bar",
        "-Dtest.bar=foo",
        "-Dtest.space=with space"
    };
    
    public void testExplicitJVMArgsSimple() throws Exception {
        JavaExecTokenProvider jetp = new JavaExecTokenProvider(createSimpleJavaProject());
        
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                launcherArgs(TWO_PROPS).
                build();
        
        Map<String, String> map = jetp.createReplacements(ActionProvider.COMMAND_RUN, Lookups.singleton(params));
        assertEquals(8, map.size());
        
        assertParamList(map.get("java.jvmArgs"), TWO_PROPS);
        assertListInValue(map.get("javaExec.jvmArgs"), TWO_PROPS);
    }
    
    public void testExplicitJVMArgsWithSpace() throws Exception {
        JavaExecTokenProvider jetp = new JavaExecTokenProvider(createSimpleJavaProject());
        
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                launcherArgs(THREE_PROPS).
                build();
        
        Map<String, String> map = jetp.createReplacements(ActionProvider.COMMAND_RUN, Lookups.singleton(params));
        assertEquals(8, map.size());

        assertParamList(map.get("java.jvmArgs"), THREE_PROPS);
        assertListInValue(map.get("javaExec.jvmArgs"), THREE_PROPS);
    }
    
    public void testAppParams() throws Exception {
        JavaExecTokenProvider jetp = new JavaExecTokenProvider(createSimpleJavaProject());
        
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                args(TWO_PROPS).
                build();

        Map<String, String> map = jetp.createReplacements(ActionProvider.COMMAND_RUN, Lookups.singleton(params));
        assertEquals(8, map.size());

        assertArgs(map.get("javaExec.args"), TWO_PROPS);
        assertParamList(map.get("java.args"), TWO_PROPS);
    }
    
    public void testAppParamsWithSpace() throws Exception {
        JavaExecTokenProvider jetp = new JavaExecTokenProvider(createSimpleJavaProject());
        
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                args(THREE_PROPS).
                build();

        Map<String, String> map = jetp.createReplacements(ActionProvider.COMMAND_RUN, Lookups.singleton(params));
        assertEquals(8, map.size());

        assertArgs(map.get("javaExec.args"), THREE_PROPS);
        assertParamList(map.get("java.args"), THREE_PROPS);
    }
    
    public void testAppCWD() throws Exception {
        JavaExecTokenProvider jetp = new JavaExecTokenProvider(createSimpleJavaProject());
        
        File wdf = new File("A test working/directory");
        String wd = wdf.getAbsolutePath();
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                workingDirectory(wdf).
                build();

        Map<String, String> map = jetp.createReplacements(ActionProvider.COMMAND_RUN, Lookups.singleton(params));
        assertEquals(8, map.size());

        assertParamList(map.get("java.workingDir"), wd);
        assertParamList(map.get("javaExec.workingDir"), "-PrunWorkingDir=\"" + wd + "\"");
    }
    
    public void testAppEnvironment() throws Exception {
        JavaExecTokenProvider jetp = new JavaExecTokenProvider(createSimpleJavaProject());
        
        Map<String, String> envVars = new LinkedHashMap<>();
        envVars.put("TestVar1", "Env Value 1");
        envVars.put("TestVar2", null);
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                environmentVariables(envVars).
                build();

        Map<String, String> map = jetp.createReplacements(ActionProvider.COMMAND_RUN, Lookups.singleton(params));
        assertEquals(8, map.size());

        assertParamList(map.get("java.environment"), new String[]{"TestVar1=Env Value 1",  "!TestVar2"});
        assertParamList(map.get("javaExec.environment"), "-PrunEnvironment=\"TestVar1=Env Value 1\" !TestVar2");
    }
    
    /**
     * Note: this code is not actually run, as it would require an entire
     * ProjectConnection to be mocked so that the Gradle process is captured
     * directly at execution. But the configuration code is actually tested in
     * {@link #testExamplePassJVmAndArguments} below. This method just gives
     * an example for javadoc, keep it synchronized with the real test !
     *
     */
    public void xtestExamplePassJVmAndArguments() throws Exception {
        Project project = createSimpleJavaProject();
        // @start region="testExamplePassJvmAndArguments"
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                args("Frodo").
                launcherArg("-verbose").
                build();
        ExplicitProcessParameters params2 = ExplicitProcessParameters.builder().
                args("Baggins").replaceArgs(false).
                launcherArg("-Dcompanion=Sam").
                build();
        
        ActionProvider actions = project.getLookup().lookup(ActionProvider.class);
        assert actions != null : "No actions available";
        // Invoke the project action, with two explicit params
        actions.invokeAction(ActionProvider.COMMAND_RUN, Lookups.fixed(
                params, params2
        ));
        // @end region="testExamplePassJvmAndArguments"
    }
    
    public void testExamplePassJVmAndArguments() throws Exception {
        Project project = createSimpleJavaProject();
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                args("Frodo").
                launcherArg("-verbose").
                build();
        ExplicitProcessParameters params2 = ExplicitProcessParameters.builder().
                args("Baggins").replaceArgs(false).
                launcherArg("-Dcompanion=Sam").
                build();
        Lookup ctx = Lookups.fixed(params, params2);
        
        GradleBaseProject gbp = GradleBaseProject.get(project);
        // hack into GBP, and add 'application' plugin in there, the project can't be really primed 
        // without running gradle.
        Set<String> plugins = new HashSet<>(gbp.getPlugins());
        plugins.add("application");
        GradleBaseProjectTrampoline.setPlugins(gbp, plugins);
        
        ActionMapping mapping = ActionToTaskUtils.getActiveMapping(ActionProvider.COMMAND_RUN, project, Lookup.EMPTY);
        String argLine = mapping.getArgs();
        final String[] args = RunUtils.evaluateActionArgs(project, ActionProvider.COMMAND_RUN, argLine, ctx);
        List<String> full = Arrays.asList(args);
        int argsIndex = full.indexOf("--args");
        assertTrue(argsIndex >= 0);
        assertTrue(argsIndex < full.size() - 1);
        String appParamsString = full.get(argsIndex + 1);
        
        String[] split = BaseUtilities.parseParameters(appParamsString);
        assertEquals(Arrays.asList("Frodo", "Baggins"), Arrays.asList(split));
        
        Optional<String> optArg = full.stream().filter(s -> s.startsWith("-PrunJvmArgs=")).findAny();
        assertTrue(optArg.isPresent());
        String vmParam = optArg.get().replace("-PrunJvmArgs=", "");
        split = BaseUtilities.parseParameters(vmParam);
        
        assertEquals(2, split.length);
        assertEquals(Arrays.asList("-verbose", "-Dcompanion=Sam"), Arrays.asList(split));
    }
}
