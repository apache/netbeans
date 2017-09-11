/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
