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

package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.Md5checker.Result;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 *
 * @author Vladimir Kvashin
 */
public class Md5checkerTest extends NativeExecutionBaseTestCase {

    public Md5checkerTest(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
    }

    @SuppressWarnings("unchecked")
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(Md5checkerTest.class);
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testChecker() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
        clearRemoteTmpDir();
        String remoteTmpDir = createRemoteTmpDir();
        File localFile = File.createTempFile("test_checker_", ".dat");
        localFile.deleteOnExit();
        writeFile(localFile, "1\n2\n3\n");

        String remotePath = remoteTmpDir + "/" + localFile.getName();
        assertFalse("File " + env + ":" + remotePath + " should not exist at this point", HostInfoUtils.fileExists(env, remotePath));

        Md5checker checker = new Md5checker(env);
        Result res = checker.check(localFile, remotePath);
        assertEquals("Check result", Md5checker.Result.INEXISTENT, res);

        int rc = CommonTasksSupport.uploadFile(localFile, env, remotePath, 0777).get().getExitCode();
        assertEquals("Error copying " + localFile + " file to " + env + ":" + remotePath, 0, rc);
        res = checker.check(localFile, remotePath);
        assertEquals("Check result", Md5checker.Result.UPTODATE, res);

        writeFile(localFile, "4\n5\n6\n");
        res = checker.check(localFile, remotePath);
        assertEquals("Check result", Md5checker.Result.DIFFERS, res);

        clearRemoteTmpDir();
        localFile.delete();
    }
}
