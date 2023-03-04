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

package org.netbeans.libs.git.jgit;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.libs.git.GitEnumsStateTest;
import org.netbeans.libs.git.jgit.commands.AddTest;
import org.netbeans.libs.git.jgit.commands.BlameTest;
import org.netbeans.libs.git.jgit.commands.BranchTest;
import org.netbeans.libs.git.jgit.commands.CatTest;
import org.netbeans.libs.git.jgit.commands.CheckoutTest;
import org.netbeans.libs.git.jgit.commands.CherryPickTest;
import org.netbeans.libs.git.jgit.commands.CleanTest;
import org.netbeans.libs.git.jgit.commands.CommitTest;
import org.netbeans.libs.git.jgit.commands.CompareCommitTest;
import org.netbeans.libs.git.jgit.commands.CopyTest;
import org.netbeans.libs.git.jgit.commands.ExportCommitTest;
import org.netbeans.libs.git.jgit.commands.ExportDiffTest;
import org.netbeans.libs.git.jgit.commands.FetchTest;
import org.netbeans.libs.git.jgit.commands.GetCommonAncestorTest;
import org.netbeans.libs.git.jgit.commands.GetPreviousRevisionTest;
import org.netbeans.libs.git.jgit.commands.GetRemotesTest;
import org.netbeans.libs.git.jgit.commands.GetUserTest;
import org.netbeans.libs.git.jgit.commands.IgnoreTest;
import org.netbeans.libs.git.jgit.commands.InitTest;
import org.netbeans.libs.git.jgit.commands.ListModifiedIndexEntriesTest;
import org.netbeans.libs.git.jgit.commands.LogTest;
import org.netbeans.libs.git.jgit.commands.MergeTest;
import org.netbeans.libs.git.jgit.commands.PullTest;
import org.netbeans.libs.git.jgit.commands.PushTest;
import org.netbeans.libs.git.jgit.commands.RebaseTest;
import org.netbeans.libs.git.jgit.commands.RemotesTest;
import org.netbeans.libs.git.jgit.commands.RemoveTest;
import org.netbeans.libs.git.jgit.commands.RenameTest;
import org.netbeans.libs.git.jgit.commands.ResetTest;
import org.netbeans.libs.git.jgit.commands.RevertTest;
import org.netbeans.libs.git.jgit.commands.SetUpstreamBranchTest;
import org.netbeans.libs.git.jgit.commands.StashTest;
import org.netbeans.libs.git.jgit.commands.StatusTest;
import org.netbeans.libs.git.jgit.commands.SubmoduleTest;
import org.netbeans.libs.git.jgit.commands.TagTest;
import org.netbeans.libs.git.jgit.commands.UnignoreTest;
import org.netbeans.libs.git.jgit.commands.UpdateRefTest;

/**
 *
 * @author ondra
 */
public class CommandsTestSuite extends NbTestSuite {

    public CommandsTestSuite (String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(GitEnumsStateTest.class);
        suite.addTestSuite(AddTest.class);
        suite.addTestSuite(BlameTest.class);
        suite.addTestSuite(BranchTest.class);
        suite.addTestSuite(CatTest.class);
        suite.addTestSuite(CheckoutTest.class);
        suite.addTestSuite(CherryPickTest.class);
        suite.addTestSuite(CleanTest.class);
        suite.addTestSuite(CommitTest.class);
        suite.addTestSuite(CompareCommitTest.class);
        suite.addTestSuite(CopyTest.class);
        suite.addTestSuite(ExportCommitTest.class);
        suite.addTestSuite(ExportDiffTest.class);
        suite.addTestSuite(FetchTest.class);
        suite.addTestSuite(GetCommonAncestorTest.class);
        suite.addTestSuite(GetPreviousRevisionTest.class);
        suite.addTestSuite(GetRemotesTest.class);
        suite.addTestSuite(GetUserTest.class);
        suite.addTestSuite(IgnoreTest.class);
        suite.addTestSuite(InitTest.class);
        suite.addTestSuite(ListModifiedIndexEntriesTest.class);
        suite.addTestSuite(LogTest.class);
        suite.addTestSuite(MergeTest.class);
        suite.addTestSuite(PullTest.class);
        suite.addTestSuite(PushTest.class);
        suite.addTestSuite(RebaseTest.class);
        suite.addTestSuite(RemotesTest.class);
        suite.addTestSuite(RemoveTest.class);
        suite.addTestSuite(RenameTest.class);
        suite.addTestSuite(RevertTest.class);
        suite.addTestSuite(ResetTest.class);
        suite.addTestSuite(SetUpstreamBranchTest.class);
        suite.addTestSuite(StashTest.class);
        suite.addTestSuite(StatusTest.class);
        suite.addTestSuite(SubmoduleTest.class);
        suite.addTestSuite(TagTest.class);
        suite.addTestSuite(UnignoreTest.class);
        suite.addTestSuite(UpdateRefTest.class);
        return suite;
    }

}
