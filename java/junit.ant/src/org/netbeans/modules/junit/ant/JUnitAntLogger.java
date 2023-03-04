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

package org.netbeans.modules.junit.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.TestSession.SessionType;
import org.netbeans.modules.java.testrunner.ant.utils.AntLoggerUtils;
import org.netbeans.modules.java.testrunner.ant.utils.AntProject;
import org.netbeans.modules.java.testrunner.ant.utils.TestCounter;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Ant logger interested in task &quot;junit&quot;,
 * dispatching events to instances of the {@link JUnitOutputReader} class.
 * There is one <code>JUnitOutputReader</code> instance created per each
 * Ant session.
 *
 * @see  JUnitOutputReader
 * @see  Report
 * @author  Marian Petras
 */
@org.openide.util.lookup.ServiceProvider(service=org.apache.tools.ant.module.spi.AntLogger.class)
public final class JUnitAntLogger extends AntLogger {
    
    /** levels of interest for logging (info, warning, error, ...) */
    private static final int[] LEVELS_OF_INTEREST = {
        AntEvent.LOG_INFO,
        AntEvent.LOG_WARN,     //test failures
        AntEvent.LOG_VERBOSE
    };
    
    private static final String[] INTERESTING_TASKS = {AntLoggerUtils.TASK_JAVA, AntLoggerUtils.TASK_JUNIT};
    private static final String ANT_TEST_RUNNER_CLASS_NAME =
            "org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner";//NOI18N
    private static final String XML_FORMATTER_CLASS_NAME =
            "org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter";//NOI18N
    private static final Logger LOGGER = Logger.getLogger(JUnitAntLogger.class.getName());
    
    /** default constructor for lookup */
    public JUnitAntLogger() { }
    
    @Override
    public boolean interestedInSession(AntSession session) {
        return true;
    }
    
    @Override
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }
    
    @Override
    public String[] interestedInTasks(AntSession session) {
        return INTERESTING_TASKS;
    }
    
    @Override
    public boolean interestedInScript(File script, AntSession session) {
        return true;
    }
    
    @Override
    public int[] interestedInLogLevels(AntSession session) {
        return LEVELS_OF_INTEREST;
    }
    
    /**
     */
    @Override
    public void messageLogged(final AntEvent event) {
        if (isTestTaskRunning(event)) {
            if (event.getLogLevel() != AntEvent.LOG_VERBOSE) {
                getOutputReader(event).messageLogged(event);
            } else {
                /* verbose messages are logged no matter which task produced them */
                getOutputReader(event).verboseMessageLogged(event);
            }
        }
    }
    
    /**
     */
    private boolean isTestTaskRunning(AntEvent event) {
        return AntLoggerUtils.isTestSessionType(getSessionInfo(event.getSession()).getCurrentSessionType());
    }
    
    /**
     */
    @Override
    public void taskStarted(final AntEvent event) {
        SessionType sessionType = AntLoggerUtils.detectSessionType(event, AntLoggerUtils.TASK_JUNIT);
        if (AntLoggerUtils.isTestSessionType(sessionType)) {
            AntSessionInfo sessionInfo = getSessionInfo(event.getSession());
            assert !AntLoggerUtils.isTestSessionType(sessionInfo.getCurrentSessionType());
            sessionInfo.setTimeOfTestTaskStart(System.currentTimeMillis());
            sessionInfo.setCurrentSessionType(sessionType);
            if (sessionInfo.getSessionType() == null) {
                sessionInfo.setSessionType(sessionType);
            }
            
            /*
             * Count the test classes in the try-catch block so that
             * 'testTaskStarted(...)' is called even if counting fails
             * (throws an exception):
             */
            int testClassCount;
            try {
                testClassCount = TestCounter.getTestClassCount(event);
            } catch (Exception ex) {
                testClassCount = 0;
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
            
            final boolean hasXmlOutput = hasXmlOutput(event);
            getOutputReader(event).testTaskStarted(testClassCount, hasXmlOutput);
        }
    }
    
    /**
     */
    @Override
    public void taskFinished(final AntEvent event) {
        AntSessionInfo sessionInfo = getSessionInfo(event.getSession());
        if (AntLoggerUtils.isTestSessionType(sessionInfo.getCurrentSessionType())) {
            getOutputReader(event).testTaskFinished();
            sessionInfo.setCurrentSessionType(null);
        }
        
    }
    
    /**
     */
    @Override
    public void buildFinished(final AntEvent event) {
        AntSession session = event.getSession();
        AntSessionInfo sessionInfo = getSessionInfo(session);

        if (AntLoggerUtils.isTestSessionType(sessionInfo.getSessionType())) {
            getOutputReader(event).buildFinished(event);
        }
        
        session.putCustomData(this, null);          //forget AntSessionInfo
    }
    
    /**
     * Retrieve existing or creates a new reader for the given session.
     *
     * @param  session  session to return a reader for
     * @return  output reader for the session
     */
    private JUnitOutputReader getOutputReader(final AntEvent event) {
        assert AntLoggerUtils.isTestSessionType(getSessionInfo(event.getSession()).getSessionType());
        
        final AntSession session = event.getSession();
        final AntSessionInfo sessionInfo = getSessionInfo(session);
        JUnitOutputReader outputReader = sessionInfo.outputReader;
        if (outputReader == null) {
            String projectDir = null;
            Project project = getProjectFromTaskStructure(event);
            if (project == null) { // that did not work for some reason, try the old hacky way
                try {
                    projectDir = event.getProperty("work.dir"); //NOI18N
                } catch (Exception e) {}// Maven throws exception for this property
                try {
                    if (projectDir == null) {
                        projectDir = event.getProperty("basedir"); // NOI18N
                    }
                    if ((projectDir != null) && (projectDir.length() != 0)) {
                        File pd = new File(projectDir);
                        File f = FileUtil.normalizeFile(pd); // #182715
                        project = FileOwnerQuery.getOwner(FileUtil.toFileObject(f));
                        if (project == null) {
                            LOGGER.log(Level.INFO, "Project was null for project dir: {0}", f.getPath()); //NOI18N
                        }
                    }
                } catch (Exception e) {}
            }
            Properties props = new Properties();
            //Passing only really used properties
            //as some others may highlight build script errors
            //(See #178798)
            String[] propsOfInterest = {"includes", "test.class", "test.methods", "test.includes", "javac.includes", "classname", "methodname", "work.dir", "classpath", "platform.java"};//NOI18N
            for(String prop:propsOfInterest) {
                String val = event.getProperty(prop);
                if (val!=null) {
                    props.setProperty(prop, val);
                }
            }
            if(project == null) { // still cannot locate the project
                File antScript = FileUtil.normalizeFile(session.getOriginatingScript());
                FileObject fileObj = FileUtil.toFileObject(antScript);
                project = FileOwnerQuery.getOwner(fileObj);
                if (project == null) {
                    LOGGER.log(Level.WARNING, "Project was null for ant script: {0}", antScript.getPath()); //NOI18N
                }
            }
            outputReader = new JUnitOutputReader(
                                        session,
                                        sessionInfo,
                                        project,
                                        props);
            sessionInfo.outputReader = outputReader;
        }
        return outputReader;
    }
    
    private Project getProjectFromTaskStructure(AntEvent event) {
        Project project = null;
        TaskStructure taskStructure = event.getTaskStructure();
        if (taskStructure != null) { // http://ant.apache.org/manual/Tasks/junit.html
            String attribute = taskStructure.getAttribute("dir"); //NOI18N
            if (attribute == null) {
                attribute = taskStructure.getAttribute("tempdir"); //NOI18N
            }
            if (taskStructure.getName().equals(AntLoggerUtils.TASK_JUNIT) && attribute != null) {
                String dir = event.evaluate(attribute);
                FileObject dirFO = FileUtil.toFileObject(FileUtil.normalizeFile(new File(dir)));
                if (dirFO != null) {
                    project = FileOwnerQuery.getOwner(dirFO);
                }
            }
            if (project == null) { // probably custom or modified Netbeans build script
                project = getProjectFromNestedElements(event, taskStructure);
            }
        }
        return project;
    }
    
    private Project getProjectFromNestedElements(AntEvent event, TaskStructure taskStructure) {
        for (TaskStructure nestedElement : taskStructure.getChildren()) {
            if (nestedElement.getName().equals("batchtest")) { //NOI18N
                return getProjectFromNestedElements(event, nestedElement);
            } else if (nestedElement.getName().equals("test") || nestedElement.getName().equals("fileset")) { //NOI18N
                return getProjectFromAttributes(event, nestedElement);
            }
        }
        return null;
    }
    
    private Project getProjectFromAttributes(AntEvent event, TaskStructure taskStructure) {
        if (taskStructure.getName().equals("test")) { //NOI18N
            String attribute = taskStructure.getAttribute("name"); //NOI18N
            if (attribute != null) {
                String name = event.evaluate(attribute);
                String fileName = name.replace(".", "/").concat(".java"); //NOI18N
                FileObject fo = GlobalPathRegistry.getDefault().findResource(fileName);
                if (fo != null) {
                    return FileOwnerQuery.getOwner(fo);
                }
            }
        }
        if (taskStructure.getName().equals("fileset")) { //NOI18N
            String attribute = taskStructure.getAttribute("dir"); //NOI18N
            if (attribute == null) {
                attribute = taskStructure.getAttribute("file"); //NOI18N
            }
            if (attribute != null) {
                String dir = event.evaluate(attribute);
                FileObject fo = FileUtil.toFileObject(new File(FileUtil.normalizePath(dir)));
                if (fo != null) {
                    return FileOwnerQuery.getOwner(fo);
                }
            }
        }
        return null;
    }
    
    /**
     */
    private AntSessionInfo getSessionInfo(final AntSession session) {
        Object o = session.getCustomData(this);
        assert (o == null) || (o instanceof AntSessionInfo);
        
        AntSessionInfo sessionInfo;
        if (o != null) {
            sessionInfo = (AntSessionInfo) o;
        } else {
            sessionInfo = new AntSessionInfo();
            session.putCustomData(this, sessionInfo);
        }
        return sessionInfo;
    }
    
    /**
     * Finds whether the test report will be generated in XML format.
     */
    private static boolean hasXmlOutput(AntEvent event) {
        final String taskName = event.getTaskName();
        if (taskName.equals(AntLoggerUtils.TASK_JUNIT)) {
            return hasXmlOutputJunit(event);
        } else if (taskName.equals(AntLoggerUtils.TASK_JAVA)) {
            return hasXmlOutputJava(event);
        } else {
            assert false;
            return false;
        }
    }
    
    /**
     * Finds whether the test report will be generated in XML format.
     */
    private static boolean hasXmlOutputJunit(AntEvent event) {
        TaskStructure taskStruct = event.getTaskStructure();
        for (TaskStructure child : taskStruct.getChildren()) {
            String childName = child.getName();
            if (childName.equals("formatter")) {                        //NOI18N
                String type = child.getAttribute("type");               //NOI18N
                type = (type != null) ? event.evaluate(type) : null;
                String usefile = child.getAttribute("usefile");         //NOI18N
                usefile = (usefile != null) ? event.evaluate(usefile) : null;
                if ((type != null) && type.equals("xml")                //NOI18N
                       && (usefile != null) && !AntProject.toBoolean(usefile)) {
                    String ifPropName = child.getAttribute("if");       //NOI18N
                    String unlessPropName =child.getAttribute("unless");//NOI18N

                    if ((ifPropName == null
                                || event.getProperty(ifPropName) != null)
                        && (unlessPropName == null
                                || event.getProperty(unlessPropName) == null)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Finds whether the test report will be generated in XML format.
     */
    private static boolean hasXmlOutputJava(AntEvent event) {
        TaskStructure taskStruct = event.getTaskStructure();
        
        String classname = taskStruct.getAttribute("classname");        //NOI18N
        if ((classname == null) ||
                !event.evaluate(classname).equals(ANT_TEST_RUNNER_CLASS_NAME)) {
            return false;
        }
        
        for (TaskStructure child : taskStruct.getChildren()) {
            String childName = child.getName();
            if (childName.equals("arg")) {                              //NOI18N
                String argValue = child.getAttribute("value");          //NOI18N
                if (argValue == null) {
                    argValue = child.getAttribute("line");              //NOI18N
                }
                if (argValue == null) {
                    continue;
                }
                argValue = event.evaluate(argValue);
                if (argValue.equals("formatter=" + XML_FORMATTER_CLASS_NAME)) { //NOI18N
                    return true;
                }
            }
        }
        return false;
    }
    
}
