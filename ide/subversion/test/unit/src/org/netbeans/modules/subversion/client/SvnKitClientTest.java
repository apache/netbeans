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

import java.lang.reflect.Method;
import org.netbeans.modules.subversion.client.commands.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.subversion.FileStatusCache;

/**
 * intended to be run with 1.8 client
 * @author tomas
 */
public class SvnKitClientTest extends NbTestCase {
    // XXX test cancel

    public SvnKitClientTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public static Test suite() throws Exception {
        
        System.setProperty("svnClientAdapterFactory", "svnkit");
        // svnkit uses its own version of javahl types,
        // test needs to run with NB module system to load classes properly
        return NbModuleSuite.emptyConfiguration()
                .addTest(AddTestHidden.class)
                .addTest(BlameTestHidden.class)
                .addTest(CatTestHidden.class)
                .addTest(CheckoutTestHidden.class)
                .addTest(CommitTestHidden.class)
                .addTest(CopyTestHidden.class)
                .addTest(DifferentWorkingDirsTestHidden.class)
                .addTest(ImportTestHidden.class)
                .addTest(InfoTestHidden.class)
                .addTest(ListTestHidden.class)
                .addTest(LogTestHidden.class)
                .addTest(MergeTestHidden.class)
                .addTest(MkdirTestHidden.class)
                .addTest(MoveTestHidden.class)
                .addTest(ParsedStatusTestHidden.class)
                .addTest(PropertyTestHidden.class)
                .addTest(RelocateTestHidden.class)
                .addTest(RemoveTestHidden.class)
                .addTest(ResolvedTestHidden.class)
                .addTest(RevertTestHidden.class)
                .addTest(StatusTestHidden.class)
                .addTest(TreeConflictsTestHidden.class)
                .addTest(SwitchToTestHidden.class)
                .addTest(UpdateTestHidden.class)
                .gui(false)
                .suite();
    }
}
