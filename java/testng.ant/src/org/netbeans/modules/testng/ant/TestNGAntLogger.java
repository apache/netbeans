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
package org.netbeans.modules.testng.ant;

import java.io.File;
import java.util.*;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.TestSession.SessionType;
import org.netbeans.modules.java.testrunner.ant.utils.AntLoggerUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 * Ant logger interested in task &quot;junit&quot;,
 * dispatching events to instances of the {@link TestNGOutputReader} class.
 * There is one <code>TestNGOutputReader</code> instance created per each
 * Ant session.
 *
 * @see  TestNGOutputReader
 * @see  Report
 * @author  Marian Petras
 * @author  Lukas Jungmann
 */
@ServiceProvider(service = AntLogger.class)
public final class TestNGAntLogger extends AntLogger {

    /** levels of interest for logging (info, warning, error, ...) */
    private static final int[] LEVELS_OF_INTEREST = {
        AntEvent.LOG_INFO,
        AntEvent.LOG_WARN, //test failures
        AntEvent.LOG_VERBOSE, //our test listener
        AntEvent.LOG_ERR
    };
    private static final String[] INTERESTING_TASKS = {AntLoggerUtils.TASK_JAVA, AntLoggerUtils.TASK_TESTNG};
    private static final String ANT_TEST_RUNNER_CLASS_NAME =
            "org.testng.TestNG";//NOI18N

    /**
     * Default constructor for lookup
     */
    public TestNGAntLogger() {
    }

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
        SessionType sessionType = AntLoggerUtils.detectSessionType(event, AntLoggerUtils.TASK_TESTNG);
        if (AntLoggerUtils.isTestSessionType(sessionType)) {
            AntSessionInfo sessionInfo = getSessionInfo(event.getSession());
            assert !AntLoggerUtils.isTestSessionType(sessionInfo.getCurrentSessionType());
            sessionInfo.setTimeOfTestTaskStart(System.currentTimeMillis());
            sessionInfo.setCurrentSessionType(sessionType);
            if (sessionInfo.getSessionType() == null) {
                sessionInfo.setSessionType(sessionType);
            }
            String suiteName = null;
            String logLevel = null;
            TaskStructure struct = event.getTaskStructure();
            if (AntLoggerUtils.TASK_TESTNG.equals(struct.getName())) {
                suiteName = struct.getAttribute("suitename");
                logLevel = struct.getAttribute("verbose");
                if (logLevel == null) {
                    logLevel = struct.getAttribute("log");
                }
            } else if (AntLoggerUtils.TASK_JAVA.equals(struct.getName())) {
                TaskStructure[] nestedElems = struct.getChildren();
                for (TaskStructure ts : nestedElems) {
                    if (ts.getName().equals("arg")) {                //NOI18N
                        String a = ts.getAttribute("line");
                        if (a != null) {
                            String[] args = event.evaluate(a).split(" ");
                            int size = args.length;
                            for (int i = 0; i < size; i++) {
                                String curr = args[i];
                                if ("-suitename".equals(curr)) {
                                    suiteName = i + 1 < size ? args[i + 1] : null;
                                    i++;
                                } else if ("-log".equals(curr) || "-verbose".equals(curr)) {
                                    logLevel = i + 1 < size ? args[i + 1] : null;
                                    i++;
                                }
                            }
                        }
                    }
                }

            } else {
                assert false : "Unexpeted task " + struct.getName();
            }
            /*
             * Count the test classes in the try-catch block so that
             * 'testTaskStarted(...)' is called even if counting fails (throws
             * an exception):
             */
            //would have to parse all incoming xmls, take includes/excludes
            //into accout, dependencies between tests, groups etc
//            int testClassCount;
//            try {
//                testClassCount = TestCounter.getTestClassCount(event);
//            } catch (Exception ex) {
//                testClassCount = 0;
//                Logger.getLogger(TestNGAntLogger.class.getName()).log(Level.SEVERE, null, ex);
//            }

            if (suiteName != null) {
                sessionInfo.setSessionName(event.evaluate(suiteName));
            }
            boolean offline = false;
            if (logLevel != null) {
                int lvl;
                try {
                    lvl = Integer.valueOf(event.evaluate(logLevel));
                } catch (NumberFormatException nfe) {
                    lvl = -1;
                }
                //logging is explicitly turned off by the user, so show only final
                //results computed off-line from testng-results.xml file
                offline = lvl == 0;
            }
            getOutputReader(event).testTaskStarted(offline, event);
//            getOutputReader(event).testTaskStarted(testClassCount, hasXmlOutput, event);
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
    private TestNGOutputReader getOutputReader(final AntEvent event) {
        assert AntLoggerUtils.isTestSessionType(getSessionInfo(event.getSession()).getSessionType());

        final AntSession session = event.getSession();
        final AntSessionInfo sessionInfo = getSessionInfo(session);
        TestNGOutputReader outputReader = sessionInfo.outputReader;
        if (outputReader == null) {
            String projectDir = null;
            Project project = null;
            try {
                projectDir = event.getProperty("work.dir"); //NOI18N
            } catch (Exception e) {
            }// Maven throws exception for this property
            try {
                if (projectDir == null) {
                    projectDir = event.getProperty("basedir"); // NOI18N
                }
                if ((projectDir != null) && (projectDir.length() != 0)) {
                    File f = FileUtil.normalizeFile(new File(projectDir));
                    project = FileOwnerQuery.getOwner(FileUtil.toFileObject(f)); //NOI18N
                }
            } catch (Exception e) {
            }
            Properties props = new Properties();
            String[] propsOfInterest = {"test.class", "test.methods", "javac.includes", "classname", "methodname", "work.dir", "classpath", "platform.java", "test.includes"};//NOI18N
            for(String prop:propsOfInterest) {
                String val = event.getProperty(prop);
                if (val!=null) {
                    props.setProperty(prop, val);
                }
            }
            outputReader = new TestNGOutputReader(
                    session,
                    sessionInfo,
                    project,
                    props);
            sessionInfo.outputReader = outputReader;
        }
        return outputReader;
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
}
