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

package org.netbeans.modules.cnd.remote.pbuild;

import org.netbeans.modules.cnd.remote.test.RemoteBuildTestBase;
import java.io.File;
import java.util.concurrent.TimeUnit;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Utilities;

/**
 *
 */
public class RemoteBuildLinksTestCase extends RemoteBuildTestBase {

    public RemoteBuildLinksTestCase(String testName) {
        super(testName);
    }

    public RemoteBuildLinksTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void postCopyProject(File origBase, File copiedBase, String projectName) throws Exception {
        String dir = copiedBase.getAbsolutePath();
        String scriptName = "create_links.sh";
        ProcessUtils.ExitStatus rc = ProcessUtils.executeInDir(dir, ExecutionEnvironmentFactory.getLocal(), "sh", scriptName);
        assertTrue("Can not execute " + scriptName + " in " + dir + ":\n" + rc.getErrorString(), rc.isOK());
    }

    private void doTest(Toolchain toolchain) throws Exception {
        if (Utilities.isWindows()) {
            System.out.printf("%s: skipping test on Windows\n", getName());
        }
        final ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        MakeProject project = openProject("makefile_proj_w_links", execEnv, Sync.RFS, toolchain);
        buildProject(project, ActionProvider.COMMAND_BUILD, getSampleBuildTimeout(), TimeUnit.SECONDS);
    }

    @ForAllEnvironments
    public void testSymLinksBuild() throws Exception {
        if (Utilities.isWindows()) {
            System.err.printf("Skipping %s on Windows\n", getName());
            return;
        }
        doTest(Toolchain.GNU);
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteBuildLinksTestCase.class);
    }
}
