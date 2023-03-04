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
package org.netbeans.jellytools;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTest;

/** Test of FindInFilesOperator.
 * @author  ai97726
 */
public class FindInFilesOperatorTest extends JellyTestCase {

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return (NbTest) NbModuleSuite.create(
                NbModuleSuite.createConfiguration(FindInFilesOperatorTest.class).
                addTest("testOverall").enableModules(".*").clusters(".*"));

    }
    
    /** Creates a new instance of SearchFilesystemOperatorTest */
    public FindInFilesOperatorTest(String testName) {
        super(testName);
    }
    
    /** Setup before every test case. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### "+getName()+" ###");
        openDataProjects("SampleProject");
    }
    
    public void testOverall() {
        FindInFilesOperator fifo = FindInFilesOperator.invoke(new ProjectsTabOperator().getProjectRootNode("SampleProject"));
        fifo.txtText().setText("SampleClass1");
        fifo.txtPatterns().setText("*.java");
        fifo.cbWholeWords().changeSelection(true);
        fifo.cbCase().changeSelection(true);
        SearchResultsOperator sro = fifo.find();
        sro.close();
    }
}
