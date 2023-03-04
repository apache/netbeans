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
package org.netbeans.modules.java.testrunner.ant.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.modules.gsf.testrunner.api.TestSession;

/**
 *
 * @author Theofanis Oikonomou
 */
public final class AntLoggerUtils {

    public static final String TASK_JAVA = "java"; //NOI18N
    public static final String TASK_JUNIT = "junit"; //NOI18N
    public static final String TASK_TESTNG = "testng"; //NOI18N

    /**
     * Detects type of the Ant task currently running.
     *
     * @param event event produced by the currently running Ant session
     * @param caller name of the ant logger calling this method, e.g.
     * {@link #TASK_JUNIT} or {@link #TASK_TESTNG}
     * @return {@code TaskType.TEST_TASK} if the task is a JUnit test task,
     * {@code TaskType.DEBUGGING_TEST_TASK} if the task is a JUnit test task
     * running in debugging mode, {@code TaskType.OTHER_TASK} if the task is not
     * a JUnit test task; or {@code null} if no Ant task is currently running
     */
    public static TestSession.SessionType detectSessionType(AntEvent event, String caller) {
        final String taskName = event.getTaskName();

        if (taskName == null) {
            return null;
        }

        if (taskName.equals(AntLoggerUtils.TASK_JUNIT) || taskName.equals(AntLoggerUtils.TASK_TESTNG)) {
            return TestSession.SessionType.TEST;
        }

        if (taskName.equals(AntLoggerUtils.TASK_JAVA)) {
            TaskStructure taskStructure = event.getTaskStructure();

            String className = taskStructure.getAttribute("classname"); //NOI18N
            if (className == null) {
                return null;
            }

            className = event.evaluate(className);
            if ((caller.equals(TASK_JUNIT) && (className.equals("junit.textui.TestRunner") || className.startsWith("org.junit.runner.") //NOI18N
                    || className.equals("org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner")))  //NOI18N
                    || (caller.equals(TASK_TESTNG) && className.equals("org.testng.TestNG"))) {  //NOI18N
                TaskStructure[] nestedElems = taskStructure.getChildren();
                for (TaskStructure ts : nestedElems) {
                    if (ts.getName().equals("jvmarg")) {                //NOI18N
                        String a;
                        if ((a = ts.getAttribute("value")) != null) {   //NOI18N
                            if (isDebugArg(event.evaluate(a))) {
                                return TestSession.SessionType.DEBUG;
                            }
                        } else if ((a = ts.getAttribute("line")) != null) {//NOI18N
                            for (String part : parseCmdLine(event.evaluate(a))) {
                                if (isDebugArg(part)) {
                                    return TestSession.SessionType.DEBUG;
                                }
                            }
                        }
                    }
                }
                return TestSession.SessionType.TEST;
            }
            return null;
        }
        assert false : "Unhandled task name";                           //NOI18N
        return null;
    }

    private static boolean isDebugArg(String arg) {
        return arg.equals("-Xdebug") || arg.equals("-agentlib:jdwp") || //NOI18N
               arg.startsWith("-agentlib:jdwp=");                       //NOI18N
    }

    /**
     * Parses the given command-line string into individual arguments.
     *
     * @param cmdLine command-line to be parsed
     * @return list of invidividual parts of the given command-line, or an empty
     * list if the command-line was empty
     */
    public static List<String> parseCmdLine(String cmdLine) {
        cmdLine = cmdLine.trim();

        /* maybe the command-line is empty: */
        if (cmdLine.length() == 0) {
            return Collections.<String>emptyList();
        }

        final char[] chars = cmdLine.toCharArray();

        /* maybe the command-line contains just one part: */
        boolean simple = true;
        for (char c : chars) {
            if ((c == ' ') || (c == '"') || (c == '\'')) {
                simple = false;
                break;
            }
        }
        if (simple) {
            return Collections.<String>singletonList(cmdLine);
        }

        /* OK, so it is not trivial: */
        List<String> result = new ArrayList<String>(4);
        StringBuilder buf = new StringBuilder(20);
        final int stateBeforeWord = 0;
        final int stateAfterWord = 1;
        final int stateInSingleQuote = 2;
        final int stateInDoubleQuote = 3;
        int state = stateBeforeWord;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (state) {
                case stateBeforeWord:
                    if (c == '"') {
                        state = stateInDoubleQuote;
                    } else if (c == '\'') {
                        state = stateInSingleQuote;
                    } else if (c == ' ') {
                        //do nothing - remain in state "before word"
                    } else {
                        buf.append(c);
                        state = stateAfterWord;
                    }
                    break;
                case stateInDoubleQuote:
                    if (c == '"') {
                        state = stateAfterWord;
                    } else {
                        buf.append(c);
                    }
                    break;
                case stateInSingleQuote:
                    if (c == '\'') {
                        state = stateAfterWord;
                    } else {
                        buf.append(c);
                    }
                    break;
                case stateAfterWord:
                    if (c == '"') {
                        state = stateInDoubleQuote;
                    } else if (c == '\'') {
                        state = stateInSingleQuote;
                    } else if (c == ' ') {
                        result.add(buf.toString());
                        buf = new StringBuilder(20);
                        state = stateBeforeWord;
                    }
                    break;
                default:
                    assert false;
            }
        }
        assert state != stateBeforeWord;        //thanks to cmdLine.trim()
        result.add(buf.toString());

        return result;
    }

    /**
     * Tells whether the given task type is a test task type or not.
     *
     * @param sessionType taskType to be checked; may be {@code null}
     * @return {@code true} if the given task type marks a test task;
     * {@code false} otherwise
     */
    public static boolean isTestSessionType(TestSession.SessionType sessionType) {
        return sessionType != null;
    }

}
