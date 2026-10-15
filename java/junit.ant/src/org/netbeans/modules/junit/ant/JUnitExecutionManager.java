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

package org.netbeans.modules.junit.ant;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.RerunType;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.junit.api.JUnitTestcase;
import org.netbeans.modules.java.testrunner.OutputUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author answer
 */
public class JUnitExecutionManager implements RerunHandler{
    public static final String JUNIT_CUSTOM_FILENAME = "junit-custom";      //NOI18N
    public static final String JUNIT_CUSTOM_TARGET = "test-custom";      //NOI18N

    private File scriptFile = null;
    private String[] targets = null;
    private Properties properties;
    private TestSession testSession;
    private Lookup lookup = Lookup.EMPTY;

    public JUnitExecutionManager(AntSession session, TestSession testSession, Properties props) {
        this.testSession = testSession;
        this.properties = props;
        try{
            scriptFile = session.getOriginatingScript();
            targets = session.getOriginatingTargets();
            //transform known ant targets to the action names
            for(int i=0;i < targets.length; i++){
                if (targets[i].equals("test-single")){                      //NOI18N
                    targets[i] = ActionProvider.COMMAND_TEST_SINGLE;      
                } else if (targets[i].equals("debug-test")){                //NOI18N
                    targets[i] = ActionProvider.COMMAND_DEBUG_TEST_SINGLE;
                } else if (targets[i].equals("test-unit")){                //NOI18N
                    targets[i] = ActionProvider.COMMAND_TEST;
                } else if (targets[i].equals("test-method")){                //NOI18N
                    targets[i] = SingleMethod.COMMAND_RUN_SINGLE_METHOD;
                } else if (targets[i].equals("debug-test-single-nb")){                //NOI18N
                    String testMethods = properties.getProperty("test.methods");//NOI18N
                    if (testMethods != null) {
                        targets[i] = SingleMethod.COMMAND_DEBUG_SINGLE_METHOD;
                    } else {
                        targets[i] = ActionProvider.COMMAND_DEBUG_TEST_SINGLE;
                    }
                }
            }
            
            String javacIncludes = properties.getProperty("javac.includes");//NOI18N
            if (javacIncludes != null){
                FileObject testFO = testSession.getFileLocator().find(javacIncludes);
                lookup = Lookups.fixed(testFO);
            }
            //"Test" action (test-unit) in a nb module project
            String testIncludes = properties.getProperty("test.includes");//NOI18N
            if (testIncludes != null){
                FileObject testFO = testSession.getFileLocator().find(testIncludes);
                lookup = Lookups.fixed(testFO);
            }
            //"Run/Debug Focused Test Method" actions (test-method/debug-test-single-nb) in a nb module project
            String testClass = properties.getProperty("test.class");//NOI18N
            String testMethods = properties.getProperty("test.methods");//NOI18N
            if(testClass != null) {
                FileObject testFO = testSession.getFileLocator().find(testClass.replace('.', '/') + ".java"); //NOI18N
                if (testMethods != null) {
                    SingleMethod methodSpec = new SingleMethod(testFO, testMethods);
                    lookup = Lookups.singleton(methodSpec);
                } else {
                    lookup = Lookups.fixed(testFO);
                }
            }

            if (targets.length == 0 ){
                String className = properties.getProperty("classname");     //NOI18N
                String methodName = properties.getProperty("methodname");     //NOI18N
                if (className != null){
                    FileObject testFO = testSession.getFileLocator().find(className.replace('.', '/') + ".java"); //NOI18N
                    if (methodName != null){
                        SingleMethod methodSpec = new SingleMethod(testFO, methodName);
                        lookup = Lookups.singleton(methodSpec);
                    }else{
                        lookup = Lookups.fixed(testFO);
                    }
                }
                if (scriptFile.getName().equals("junit.xml")){              //NOI18N
                    if (methodName != null){
                        targets = new String[]{SingleMethod.COMMAND_RUN_SINGLE_METHOD};
                    }else{
                        targets = new String[]{ActionProvider.COMMAND_TEST_SINGLE};
                    }
                }else if (scriptFile.getName().equals("junit-debug.xml")){  //NOI18N
                    if (methodName != null){
                        targets = new String[]{SingleMethod.COMMAND_DEBUG_SINGLE_METHOD};
                    }else{
                        targets = new String[]{ActionProvider.COMMAND_DEBUG_TEST_SINGLE};
                    }
                }
            }
        }catch(Exception e){}
    }

    public void rerun() {
        if (scriptFile.getName().equals(JUNIT_CUSTOM_FILENAME + ".xml")){   //NOI18N
            try {
                runAnt(FileUtil.toFileObject(scriptFile), targets, properties);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            String includes = properties.getProperty("includes", "**"); //NOI18N
            if (!includes.equals("**")){ //NOI18N
                //"Test File(s)/Package(s)" action was invoked in the first place
                try {
                    properties.setProperty("ignore.failing.tests", "true"); //NOI18N
                    properties.setProperty("nb.wait.for.caches", "true"); //NOI18N
                    properties.setProperty("nb.internal.action.name", ActionProvider.COMMAND_TEST); //NOI18N
                    runAnt(FileUtil.toFileObject(scriptFile), targets, properties);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return;
            }
            Project project = testSession.getProject();
            if(ProjectManager.getDefault().isValid(project)) {
                ActionProvider actionProvider = project.getLookup().lookup(ActionProvider.class);
                if (actionProvider != null) {
                    if (Arrays.asList(actionProvider.getSupportedActions()).contains(targets[0])
                            && actionProvider.isActionEnabled(targets[0], lookup)) {
                        actionProvider.invokeAction(targets[0], lookup);
                    }
                }
            }
        }
    }

    public void rerun(Set<Testcase> tests) {
        SortedMap<String, String> toTest = new TreeMap<String, String>();
        FileObject someTestFO = null;
        for(Testcase test: tests){
            String className = test.getClassName();
	    if(className == null) {
		className = test.getName();
	    }
	    if(className == null) {
		continue;
	    }
            String prev = toTest.get(className);
            toTest.put(className, prev == null ? test.getName() : prev + "," + test.getName()); //NOI18N
            if (someTestFO == null && test instanceof JUnitTestcase){
                someTestFO = ((JUnitTestcase)test).getClassFileObject();
            }
        }

        DateFormat dateFormat = new SimpleDateFormat("HHmmssSSS");              //NOI18N
        String id = dateFormat.format(new Date());

        try {
            FileObject templateFO = FileUtil.getConfigFile("Templates/UnitTests/junit-custom.xml"); //NOI18N
//            DataObject templateDO = DataObject.find(templateFO);
            FileObject tmpDir = FileUtil.toFileObject(new File(System.getProperty("java.io.tmpdir")).getCanonicalFile());
            FileObject targetFO = tmpDir.createFolder("junit-custom-" + id);                //NOI18N
//            DataFolder targetDF = DataFolder.findFolder(targetFO);
            Map<String,Object> params = new HashMap<>();
            String testStr = "";
            for(String testClass: toTest.keySet()){
                testStr += "<test name=\"" + testClass + "\" methods=\"" + toTest.get(testClass) + "\" todir=\"${test.result.dir.custom}\"/>\n"; //NOI18N
            }
            params.put("tests", testStr); //NOI18N                     

            // TODO - generate new file from template using the new parsing API???
//            DataObject junitCustomDO = templateDO.createFromTemplate(targetDF, JUNIT_CUSTOM_FILENAME, params);
            Properties props = new Properties();
            props.put("work.dir", testSession.getProject().getProjectDirectory().getPath());    //NOI18N
            ClassPath cp = ClassPath.getClassPath(someTestFO, ClassPath.EXECUTE);
            props.put("classpath", cp != null ? cp.toString(ClassPath.PathConversionMode.FAIL) : "");//NOI18N
            Project p = testSession.getProject();
            String platformId = null;
            try {
                Method evalMethod = p.getClass().getDeclaredMethod("evaluator"); //NOI18N
                PropertyEvaluator evaluator = (PropertyEvaluator) evalMethod.invoke(p);
                if (evaluator != null) {
                    platformId = evaluator.getProperty("platform.active"); //NOI18N
                }
            } catch (Exception ex) {
            }

            JavaPlatform platform = OutputUtils.getActivePlatform(platformId); //NOI18N
            if (platform != null) {
                props.put("platform.java", platform.findTool("java").getPath());//NOI18N
            } else {
                //try to run with the "default platform", meaning the JDK on which NetBeans itself is running.
                props.put("platform.java", JavaPlatform.getDefault().findTool("java").getPath());//NOI18N
            }

            runAnt(templateFO, new String[]{JUNIT_CUSTOM_TARGET}, props);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static void runAnt(FileObject antScript, String[] antTargets, Properties antProps) throws IOException{
            AntTargetExecutor.Env execenv = new AntTargetExecutor.Env();
            Properties props = execenv.getProperties();
            props.putAll(antProps);
            execenv.setProperties(props);
            AntTargetExecutor.createTargetExecutor(execenv).execute(AntScriptUtils.antProjectCookieFor(antScript), antTargets);
    }

    public boolean enabled(RerunType type) {
        if ((scriptFile == null) || (targets == null) || (targets.length == 0)){
            return false;
        }
        if (scriptFile.getName().equals(JUNIT_CUSTOM_FILENAME + ".xml")){   //NOI18N
            return true;
        }
        Project project = testSession.getProject();
        if(project == null) { // could not locate the project for which the testSession was invoked for
            return false;
        }
        ActionProvider actionProvider = project.getLookup().lookup(ActionProvider.class);
        if (actionProvider != null){
            boolean runSupported = false;
            for (String action : actionProvider.getSupportedActions()) {
                if (action.equals(targets[0])) {
                    runSupported = true;
                    break;
                }
            }
            if (runSupported && actionProvider.isActionEnabled(targets[0], lookup)) {
                return true;
            }
        }

        return false;
    }
    
    public void addChangeListener(ChangeListener listener) {
    }

    public void removeChangeListener(ChangeListener listener) {
    }

}
