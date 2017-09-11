/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
