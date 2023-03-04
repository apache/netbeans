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
import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;

/** Test AttachWindowAction.
 *
 * @author Jiri Skrivanek
 */
public class AttachWindowActionTest extends JellyTestCase {

    public static final String[] tests = new String[]{
        "testPerformAPI"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public AttachWindowActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(AttachWindowActionTest.class, tests);
    }

    @Override
    public void setUp() {
        System.out.println("### " + getName() + " ###");
    }

    /** Test performAPI() method. */
    public void testPerformAPI() {
        ProjectsTabOperator projectsOper = ProjectsTabOperator.invoke();
        FavoritesOperator favoritesOper = FavoritesOperator.invoke();
        new AttachWindowAction(OutputOperator.invoke(), AttachWindowAction.AS_LAST_TAB).perform(favoritesOper);
        new AttachWindowAction(projectsOper, AttachWindowAction.TOP).perform(favoritesOper);
        new AttachWindowAction(OutputOperator.invoke(), AttachWindowAction.RIGHT).perform(favoritesOper);
        new AttachWindowAction(projectsOper, AttachWindowAction.AS_LAST_TAB).perform(favoritesOper);
        favoritesOper.close();
    }
}
