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

package org.netbeans.modules.debugger.jpda.ui;

import java.io.IOException;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.JemmyProperties;

/**
 *
 * @author Vojtech Sigler, Jiri Kovalsky
 */
public class DebuggerTestCase extends JellyTestCase {

    private static boolean initialized = false;

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public DebuggerTestCase(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws IOException
    {
        //if run for the first time, open test project and clean+build
        if (!initialized)
        {
            openDataProjects(Utilities.testProjectName);
            Utilities.cleanBuildTestProject();
            initialized = true;
        }

    }

    @Override
    public void tearDown()
    {
        JemmyProperties.getCurrentOutput().printTrace("\nteardown\n");
        Utilities.endAllSessions();
        Utilities.deleteAllBreakpoints();
    }

}
