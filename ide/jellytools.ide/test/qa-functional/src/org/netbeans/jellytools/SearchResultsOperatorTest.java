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

/**
 * Test of SearchResultsOperator.
 *
 * @author Jiri Skrivanek
 */
public class SearchResultsOperatorTest extends JellyTestCase {

    public static final String[] tests = new String[]{
        "testWaitEndOfSearch",
        //"testBtStopSearch", // don't know how to test
        "testBtModifySearch",
        "testOutlineResult",
        "testSelectResult",
        "testOpenResult",
        "testModifySearch",
        "testVerify",
        "testBtShowDetails",
        "testShowDetails", // has to be last because it steals focus
        "testClose"
    };

    /** Method used for explicit test suite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(SearchResultsOperatorTest.class, tests);
    }

    /** Creates new SearchResultsOperatorTest */
    public SearchResultsOperatorTest(String testName) {
        super(testName);
    }
    private static SearchResultsOperator searchResultsOper = null;

    /** Open find dialog on sample project and find sample substring. */
    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        if (searchResultsOper == null) {
            FindInFilesOperator fifo = FindInFilesOperator.invoke(new ProjectsTabOperator().getProjectRootNode("SampleProject"));
            fifo.txtText().setText("sample");
            searchResultsOper = fifo.find();
        }
    }

    /** Test btStopSearch method */
    public void testBtStopSearch() {
        searchResultsOper.btStopSearch();
    }

    /** Test btShowDetails method */
    public void testBtShowDetails() {
        searchResultsOper.btShowDetails();
    }

    /** Test btModifySearch method  */
    public void testBtModifySearch() {
        searchResultsOper.btModifySearch();
    }

    /** Test treeResult method  */
    public void testOutlineResult() {
        searchResultsOper.outlineResult();
    }

    /** Test selectResult method */
    public void testSelectResult() {
        searchResultsOper.selectResult("SampleClass1.java"); //NOI18N
    }

    /** Test openResult method */
    public void testOpenResult() {
        searchResultsOper.openResult("SampleClass1.java|sample");  //NOI18N
        new EditorOperator("SampleClass1").close(); //NOI18N
    }

    /** Test stopSearch method */
    public void testStopSearch() {
        // need to find a test case to test it
    }

    /** Test showDetails method  */
    public void testShowDetails() {
        searchResultsOper.makeComponentVisible();
        searchResultsOper.showDetails().close();
        searchResultsOper.makeComponentVisible();
    }

    /** Test modifySearch method*/
    public void testModifySearch() {
        searchResultsOper.modifySearch().close();
    }

    /** Test waitEndOfSearch method */
    public void testWaitEndOfSearch() {
        // searching done in setup should be finished
        searchResultsOper.waitEndOfSearch();
    }

    /** Test verify method */
    public void testVerify() {
        // maximize to satisfy all buttons are visible
        searchResultsOper.maximize();
        searchResultsOper.verify();
    }
    
    /** Test close method */
    public void testClose() {
        searchResultsOper.close();
    }
}
