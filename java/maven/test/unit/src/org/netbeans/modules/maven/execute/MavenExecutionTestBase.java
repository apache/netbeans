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
package org.netbeans.modules.maven.execute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.extexecution.startup.StartupExtenderRegistrationProcessor;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.netbeans.modules.maven.runjar.MavenExecuteUtils;
import org.netbeans.modules.maven.runjar.RunJarPrereqChecker;
import org.netbeans.spi.extexecution.startup.StartupExtenderImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.openide.windows.InputOutput;


/**
 *
 * @author sdedic
 */
public class MavenExecutionTestBase extends NbTestCase {
    protected static final String DEFAULT_MAIN_CLASS_TOKEN = "main.class.TokenMarker";
    
    protected Map<String, String> runP = new HashMap<>();
    protected Map<String, String> debugP = new HashMap<>();
    protected Map<String, String> profileP = new HashMap<>();
    protected FileObject pom;
    protected FileObject nbActions;

    protected NetbeansActionMapping runMapping;
    protected NetbeansActionMapping debugMapping;
    protected NetbeansActionMapping profileMapping;
    
    protected ActionToGoalMapping defaultActionMapping;
    
    protected ActionToGoalMapping actionToGoalMap;
    
    protected String mavenVmArgs = ""; // NOI18N
    protected String mavenAppArgs = ""; // NOI18N
    protected Map<String, String> mavenExecutorDefines = new HashMap<>();
    protected Map<String, String> mavenExecutorRawDefines = new HashMap<>();
    protected Map<String, String> mavenExecutorEnvironment = new HashMap<>();
    
    protected final InstanceContent actionData = new InstanceContent();
    protected final Lookup actionLookup = new AbstractLookup(actionData);

    public MavenExecutionTestBase(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        clearWorkDir();
        MockLookup.setLayersAndInstances();
    }

    @Override
    protected void tearDown() throws Exception {
        TestExtender.vmArg = null;
        super.tearDown();
    }
    
    
    protected String substProperties(String input, String token, Map<String, String> properties) {
        int pos;
        while ((pos = input.indexOf(token)) != -1) {
            int last = input.lastIndexOf('\n', pos);
            String indent = String.join(" ", Collections.nCopies(pos - (last +1), ""));
            StringBuilder sb = new StringBuilder();
            
            for (String s : properties.keySet()) {
                sb.append(indent);
                sb.append("<").append(s).append(">");
                sb.append(properties.get(s));
                sb.append("</").append(s).append(">");
                sb.append("\n");
            }
            input = input.replace(token, sb.toString());
        }
        return input;
    }
    
    protected InputStream getActionResourceStream() {
        return getClass().getResourceAsStream("nbactions-template.xml");
    }
    
    protected FileObject  createNbActions(
            Map<String, String> runProperties, 
            Map<String, String> debugProperties, 
            Map<String, String> profileProperties) throws IOException, XmlPullParserException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(
                getActionResourceStream(), StandardCharsets.UTF_8))) {
            String l;
            
            while ((l = rdr.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(l);
            }
        }
        String result = substProperties(
            substProperties(
                substProperties(sb.toString(), "&runProperties;", runProperties),
                "&debugProperties;", debugProperties),
            "&profileProperties;", profileProperties);
        FileObject actions = TestFileUtils.writeFile(FileUtil.toFileObject(getWorkDir()), "nbactions.xml", result);
        nbActions = actions;
        actionToGoalMap = new NetbeansBuildActionXpp3Reader().read(new StringReader(actions.asText()));
        return actions;
    }
    
    protected void loadActionMappings(Project project) throws Exception {
        runMapping = ModelHandle2.getMapping("run", project, 
                project.getLookup().lookup(M2ConfigProvider.class).getActiveConfiguration());
        debugMapping = ModelHandle2.getMapping("debug", project, 
                project.getLookup().lookup(M2ConfigProvider.class).getActiveConfiguration());
        profileMapping = ModelHandle2.getMapping("profile", project, 
                project.getLookup().lookup(M2ConfigProvider.class).getActiveConfiguration());
        
        M2ConfigProvider usr = project.getLookup().lookup(M2ConfigProvider.class);
        defaultActionMapping = new NetbeansBuildActionXpp3Reader().read(new StringReader((usr.getDefaultConfig().getRawMappingsAsString())));
    }
    
    protected FileObject createPom(String argsString, String propString) throws IOException {
         pom = TestFileUtils.writeFile(FileUtil.toFileObject(getWorkDir()), "pom.xml", "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "    <groupId>grp</groupId>\n"
                + "    <artifactId>art</artifactId>\n"
                + "    <version>1.0</version>\n"
                +      propString
                + "    <build>\n"
                + "        <plugins>\n"
                + "            <plugin>\n"
                + "                <groupId>org.codehaus.mojo</groupId>\n"
                + "                <artifactId>exec-maven-plugin</artifactId>\n"
                + "                <version>3.1.0</version>\n"
                + "                <configuration>\n"
                +                      argsString 
                + "                </configuration>\n"
                + "            </plugin>\n"      
                + "        </plugins>\n"
                + "    </build>\n"
                + "</project>\n");
        return pom;
    }
    
    protected void createPomWithArguments() throws Exception {
        pom = createPom("<arguments>"
                    + "<argument>-DsomeProperty=${AA}</argument>"
                    + "<argument>-classpath</argument>"
                    + "<classpath></classpath>"
                + "</arguments>", "<properties><AA>blah</AA></properties>");
    }

    protected NetbeansActionMapping getActionMapping(String aName) {
        return actionToGoalMap.getActions().stream().filter(m -> m.getActionName().equals(aName)).findAny().get();
    }
    
    private static String interpolate(String expr, ExpressionEvaluator mvnEval, Map<String, String> props) {
        if (expr.trim().isEmpty()) {
            return expr;
        }
        int varIndex = expr.indexOf("${");
        int endIndex = -1;
        if (varIndex >= 0) {
            endIndex = expr.indexOf("}", varIndex + 2);
        }
        if (varIndex == -1 || endIndex == -1) {
            try {
                return Objects.toString(mvnEval.evaluate(expr));
            } catch (ExpressionEvaluationException ex) {
                return expr;
            }
        }
        String n = expr.substring(varIndex + 2, endIndex);
        String v = props.get(n);
        if (v == null) {
            String n2 = expr.substring(varIndex, endIndex + 1);
            try {
                v = Objects.toString(mvnEval.evaluate(n2));
            } catch (ExpressionEvaluationException ex) {
                v = n2;
            }
        }
        String prefix = interpolate(expr.substring(0, varIndex), mvnEval, props);
        String suffix = interpolate(expr.substring(endIndex + 1), mvnEval, props);
        return prefix + v + suffix;
    }

    /**
     * Evaluates property references in arguments. Uses Maven evaluator for the project + properties defined in the 
     * ModelRunConfig (which are passed as -D to the maven executor). Collects all -D defines into {@link #mavenExecutorDefines}
     */
    protected List<String> substituteProperties(List<String> args, Map<String, String> environment, Project p, Map<? extends String, ? extends String> properties) {
        Map<String, String> props = new HashMap<>(properties);
        for (int i = 0; i < args.size(); i++) {
            String a = args.get(i);
            if (a.startsWith("-D")) {
                int eq = a.indexOf('=');
                String k = a.substring(2, eq);
                String v = a.substring(eq + 1);
                props.put(k, v);
            }
        }
        mavenExecutorRawDefines = new HashMap<>(props);
        ExpressionEvaluator e = PluginPropertyUtils.createEvaluator(p);
        for (int i = 0; i < args.size(); i++) {
            String a = args.get(i);
            args.set(i, interpolate(a, e, props));
        }
        for (Map.Entry<String, String> pE : props.entrySet()) {
            pE.setValue(interpolate(pE.getValue(), e, props));
        }
        mavenExecutorDefines = props;
        mavenExecutorEnvironment = new HashMap<>(environment);
        return args;
    }
    

    /**
     * Drives the infrastructure up to the process execution, but does not actually execute the process. Needs "mainClass"
     * to be defined, serves as a delimiter between passed VM args and args. Checks that vmArgs and args are present (if not {@code null}
     * at the appropriate place around mainClass. Collects -D properties into {@link #mavenExecutorDefines} for inspection.
     * 
     * @param mapping the action mapping for the execution
     * @param vmArgs mandatory VM args to check
     * @param mainClass main class to check
     * @param args program arguments.
     * @throws Exception 
     */
    protected void assertRunArguments(NetbeansActionMapping mapping, String vmArgs, String mainClass, String args) throws Exception {
        final Project project = ProjectManager.getDefault().findProject(pom.getParent());        
        
        assertMavenRunAction(project, mapping, "run", mainClass, (List<String> cmdLine) -> {
            String argString = mavenExecutorDefines.get(MavenExecuteUtils.RUN_PARAMS);
            int indexOfMainClass = argString.indexOf(mainClass);
            assertTrue(indexOfMainClass >= 0);

            if (vmArgs != null) {
                int indexOfVM = argString.indexOf(vmArgs);
                assertTrue(indexOfVM >= 0);
                assertTrue("VM args must precede main class", indexOfVM < indexOfMainClass);
            }
            mavenVmArgs = argString.substring(0, indexOfMainClass).trim();
            mavenAppArgs = argString.substring(indexOfMainClass + mainClass.length()).trim();
            if (args != null) {
                int indexOfAppParams = argString.indexOf(args);
                assertTrue(indexOfAppParams >= 0);
                assertTrue("App args must followmain class", indexOfMainClass <  indexOfAppParams);
            }
        });
        
        // checks that 'split' arguments are defined / consistent with exec.args cmdline:
        if (vmArgs != null) {
            // FIXME !!!
            assertTrue("VM args must contain " + vmArgs, mavenExecutorDefines.getOrDefault(MavenExecuteUtils.RUN_VM_PARAMS, "").contains(vmArgs));
        }
        if (args != null) {
            // FIXME !!!
            assertTrue("App args must contain " + args, mavenExecutorDefines.getOrDefault(MavenExecuteUtils.RUN_APP_PARAMS, "").contains(args));
        }
    }
    
    protected void assertMavenRunAction(Project project, NetbeansActionMapping mapping, String actionName, Consumer<List<String>> commandLineAcceptor) throws Exception {
            assertMavenRunAction(project, mapping, actionName, null, commandLineAcceptor);
    }
    
    protected void assertMavenRunAction(Project project, NetbeansActionMapping mapping, String actionName, String mainClassname, Consumer<List<String>> commandLineAcceptor) throws Exception {
        NbPreferences.root().node("org/netbeans/modules/maven").put(EmbedderFactory.PROP_COMMANDLINE_PATH, "mvn");
        ModelRunConfig cfg = new ModelRunConfig(project, mapping, actionName, null, actionLookup, true);
        // prevent displaying dialogs.
        RunJarPrereqChecker.setMainClass(mainClassname == null ? DEFAULT_MAIN_CLASS_TOKEN : mainClassname);
        for (PrerequisitesChecker elem : cfg.getProject().getLookup().lookupAll(PrerequisitesChecker.class)) {
            if (!elem.checkRunConfig(cfg)) {
                fail("");
            }
            if (cfg.getPreExecution() != null) {
                if (!elem.checkRunConfig(cfg.getPreExecution())) {
                    fail("");
                }
            }
        }
        MavenCommandLineExecutor exec = new MavenCommandLineExecutor(cfg, InputOutput.NULL, null) {
            @Override
            int executeProcess(CommandLineOutputHandler out, ProcessBuilder builder, Consumer<Process> processSetter) throws IOException, InterruptedException {
                List<String> args = substituteProperties(builder.command(), builder.environment(), project, cfg.getProperties());
                commandLineAcceptor.accept(args);
                return 0;
            }
        };
        exec.task = new ExecutorTask(exec) {
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
        exec.run();
    }
    
    protected static class TestExtender implements StartupExtenderImplementation {
        public static String vmArg;
        
        @Override
        public List<String> getArguments(Lookup context, StartupExtender.StartMode mode) {
            return vmArg == null ? Collections.emptyList() : Collections.singletonList(vmArg);
        }
    }
    
    protected void registerExtender(Class<?> c) throws IOException {
        if (c == null) {
            c = TestExtender.class;
        }
        FileObject p = FileUtil.getConfigFile(StartupExtenderRegistrationProcessor.PATH);
        if (p == null) {
            p = FileUtil.getConfigRoot().createFolder(StartupExtenderRegistrationProcessor.PATH);
        }
        DataFolder fld = DataFolder.findFolder(p);
        InstanceDataObject.create(fld, "test-extender", c);
    }
    
    //
    //====================== samples =====================
    public static void samplePassAdditionalVMargs() {
        Project prj = null;
        // @start region="samplePassAdditionalVMargs"
        // get action provider:
        ActionProvider projectActionProvider = prj.getLookup().lookup(ActionProvider.class);
        
        // create explicit additional parameters instruction:
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                launcherArg("-DvmArg2=2").
                arg("paramY").
                build();
        // pass explicit parameters to the Run action:
        projectActionProvider.invokeAction(ActionProvider.COMMAND_RUN, Lookups.fixed(params));
        // @end region="samplePassAdditionalVMargs"
    }
}
