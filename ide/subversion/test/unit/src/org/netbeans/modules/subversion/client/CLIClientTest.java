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

package org.netbeans.modules.subversion.client;

import org.netbeans.modules.subversion.client.commands.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

/**
 * Subversion 1.8
 * @author tomas
 */
public class CLIClientTest extends NbTestCase {

    // XXX test cancel
    
    public CLIClientTest(String arg0) {
        super(arg0);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        System.setProperty("svnClientAdapterFactory", "commandline");
        
        suite.addTestSuite(AddTestHidden.class);
        suite.addTestSuite(AvailabilityTest.class);
        suite.addTestSuite(BlameTestHidden.class);
//        suite.addTestSuite(CancelTest.class);
        suite.addTestSuite(CatTestHidden.class);
        suite.addTestSuite(CheckoutTestHidden.class);
        suite.addTestSuite(CommitTestHidden.class);
        suite.addTestSuite(CopyTestHidden.class);
        suite.addTestSuite(DifferentWorkingDirsTestHidden.class);
        suite.addTestSuite(ImportTestHidden.class);
        suite.addTestSuite(InfoTestHidden.class);
        suite.addTestSuite(ListTestHidden.class);
        suite.addTestSuite(LogTestHidden.class);
        suite.addTestSuite(MergeTestHidden.class);
        suite.addTestSuite(MkdirTestHidden.class);
        suite.addTestSuite(MoveTestHidden.class);
        suite.addTestSuite(ParsedStatusTestHidden.class);
        suite.addTestSuite(PropertyTestHidden.class);
        suite.addTestSuite(RelocateTestHidden.class);
        suite.addTestSuite(RemoveTestHidden.class);
        suite.addTestSuite(ResolvedTestHidden.class);
        suite.addTestSuite(RevertTestHidden.class);
        suite.addTestSuite(StatusTestHidden.class);
        suite.addTestSuite(TreeConflictsTestHidden.class);
        suite.addTestSuite(SwitchToTestHidden.class);
        suite.addTestSuite(UpdateTestHidden.class);
        
        return suite;
    }
}
