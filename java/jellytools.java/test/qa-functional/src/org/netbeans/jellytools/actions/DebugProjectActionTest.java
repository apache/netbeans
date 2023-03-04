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
package org.netbeans.jellytools.actions;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.*;
import org.netbeans.jemmy.JemmyException;

/** org.netbeans.jellytools.actions.DebugProjectAction
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class DebugProjectActionTest extends JellyTestCase {

    public static final String[] tests = new String[]{"testPerformPopup", "testPerformMenu", "testPerformShortcut"};

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public DebugProjectActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(DebugProjectActionTest.class, tests);
    }
    private static MainWindowOperator.StatusTextTracer statusTextTracer;

    /** Method called before all test cases. */
    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");  // NOI18N
        openDataProjects("SampleProject");
        if (statusTextTracer == null) {
            // increase timeout to 60 seconds
            MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
            statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        }
        statusTextTracer.start();
    }

    /** method called after each testcase
     */
    @Override
    protected void tearDown() {
        try {
            // "SampleWebProject (debug)"
            String outputTarget = Bundle.getString(
                    "org.apache.tools.ant.module.run.Bundle", "TITLE_output_target",
                    new Object[]{"SampleProject", null, "debug"});  // NOI18N
            // "Building SampleProject (debug)..."
            String buildingMessage = Bundle.getString(
                    "org.apache.tools.ant.module.run.Bundle", "FMT_running_ant",
                    new Object[]{outputTarget});
            // "Finished building SampleProject (debug)"
            String finishedMessage = Bundle.getString(
                    "org.apache.tools.ant.module.run.Bundle", "FMT_finished_target_status",
                    new Object[]{outputTarget});
            // wait status text "Building SampleProject (debug)..."
            statusTextTracer.waitText(buildingMessage);
            // wait status text "Finished building SampleProject (debug)."
            statusTextTracer.waitText(finishedMessage);
            // wait status text "User program finished"
            statusTextTracer.waitText("User program finished"); // NOI18N
        } catch (JemmyException e) {
            log("debugOutput.txt", new OutputTabOperator("SampleProject").getText()); // NOI18N
            throw e;
        } finally {
            statusTextTracer.stop();
        }
    }

    /** Test performPopup. */
    public void testPerformPopup() {
        new DebugProjectAction().performPopup(ProjectsTabOperator.invoke().getProjectRootNode("SampleProject")); // NOI18N
    }

    /** Test performMenu */
    public void testPerformMenu() {
        // Set as Main Project
        new Action("Run|Set Main Project|SampleProject", null).perform();
        new DebugProjectAction().performMenu();
    }

    /** Test performShortcut */
    public void testPerformShortcut() {
        new DebugProjectAction().performShortcut();
    }
}
