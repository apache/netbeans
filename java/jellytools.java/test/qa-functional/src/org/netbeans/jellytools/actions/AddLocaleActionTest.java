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
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JDialogOperator;

/** JUnit test suite with Jemmy support
 *
 * @author Adam Sotona
 */
public class AddLocaleActionTest extends JellyTestCase {

    public static final String[] tests = new String[] {
        "testPerformPopup"
    };
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public AddLocaleActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(AddLocaleActionTest.class, tests);
    }

    @Override
    protected void setUp() throws IOException  {
        openDataProjects("SampleProject");
    }
    
    public void testPerformPopup() {
        Node n = new Node(new SourcePackagesNode("SampleProject"), "sample1|properties.properties");
        new AddLocaleAction().performPopup(n);
        // "New Locale"
        String newLocaleTitle = Bundle.getString("org.netbeans.modules.properties.Bundle", "CTL_NewLocaleTitle");
        new JDialogOperator(newLocaleTitle).requestClose();
    }
    
}
