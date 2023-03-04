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

package org.netbeans.modules.db.dataview.output;

import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author jawed
 */
public class OutputSuite extends NbTestCase {
    
    public OutputSuite(String testName) {
        super(testName);
    }            

    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite("OutputSuite");
        //suite.addTest(org.netbeans.modules.db.dataview.output.DataViewTablePanelTest.suite());
        //suite.addTest(org.netbeans.modules.db.dataview.output.DataViewActionHandlerTest.suite());
        suite.addTest(org.netbeans.modules.db.dataview.output.DataViewDBTableTest.suite());
        suite.addTest(org.netbeans.modules.db.dataview.output.DataViewTest.suite());
        //suite.addTest(org.netbeans.modules.db.dataview.output.UpdatedRowContextTest.suite());
        suite.addTest(org.netbeans.modules.db.dataview.output.SQLExecutionHelperTest.suite());
        //suite.addTest(org.netbeans.modules.db.dataview.output.SQLStatementGeneratorTest.suite());
        //suite.addTest(org.netbeans.modules.db.dataview.output.DataViewTableSorterTest.suite());
        //suite.addTest(org.netbeans.modules.db.dataview.output.DataViewTableUITest.suite());
        //suite.addTest(org.netbeans.modules.db.dataview.output.ShowSQLDialogTest.suite());
        //suite.addTest(org.netbeans.modules.db.dataview.output.DataViewPageContextTest.suite());
        suite.addTest(org.netbeans.modules.db.dataview.output.SQLStatementExecutorTest.suite());
        //suite.addTest(org.netbeans.modules.db.dataview.output.DataViewUITest.suite());
        return suite;
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
