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

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;

/** Test org.netbeans.jellytools.actions.ProjectViewAction
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class ProjectViewActionTest extends JellyTestCase {

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ProjectViewActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(ProjectViewActionTest.class);
    }

    /** Test performMenu */
    public void testPerformMenu() throws InterruptedException {

        //Make sure the menu has time to load (workaround for the case the menu
        //is not fully loaded at the beginning of the test.
        new Action(Bundle.getStringTrimmed(
                "org.netbeans.core.ui.resources.Bundle", "Menu/Tools"), null).performMenu();

        Thread.sleep(1000);


        new Action(Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle",
                "Menu/BuildProject"), null).performMenu();

        Thread.sleep(1000);

        ProjectsTabOperator.invoke().close();
        new ProjectViewAction().performMenu();
        new ProjectsTabOperator();
    }
}
