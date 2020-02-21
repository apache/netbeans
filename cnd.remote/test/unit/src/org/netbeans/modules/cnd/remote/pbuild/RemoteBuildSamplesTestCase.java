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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.pbuild;

import java.io.File;
import org.netbeans.modules.cnd.remote.test.RemoteBuildTestBase;
import junit.framework.Test;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.openide.filesystems.FileUtil;
/**
 *
 */
public class RemoteBuildSamplesTestCase extends RemoteBuildTestBase {

    public RemoteBuildSamplesTestCase(String testName) {
        super(testName);
    }

    public RemoteBuildSamplesTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

//    @ForAllEnvironments
//    public void testBuildSample_Rfs_Gnu_Arguments_Once() throws Exception {
//        buildSample(Sync.RFS, Toolchain.GNU, "Arguments", "Args_Rfs_Gnu_Once", 1);
//    }
//
//    @ForAllEnvironments
//    public void testBuildSample_Rfs_Gnu_Arguments_Multy() throws Exception {
//        buildSample(Sync.RFS, Toolchain.GNU, "Arguments", "Args_Rfs_Gnu_Multy", 3, getSampleBuildTimeout(), getSampleBuildTimeout()/3);
//    }
//
//    @ForAllEnvironments
//    public void testBuildSample_Sftp_Gnu_Arguments_Once() throws Exception {
//        buildSample(Sync.FTP, Toolchain.GNU, "Arguments", "Args_Sftp_Gnu_Once", 1);
//    }
//
//    @ForAllEnvironments
//    public void testBuildSample_Sftp_Gnu_Arguments_Multy() throws Exception {
//        buildSample(Sync.FTP, Toolchain.GNU, "Arguments", "Args_Sftp_Gnu_Multy", 3, getSampleBuildTimeout(), getSampleBuildTimeout()/3);
//    }

    private class ExecutableSetter implements ProjectProcessor{
        private void setExecutable(File f) {
            if (f.isDirectory()) {
                for (File child : f.listFiles()) {
                    setExecutable(child);
                }
            } else {
                f.setExecutable(true);
            }
        }

        @Override
        public void processProject(MakeProject project) throws Exception {
            setExecutable(FileUtil.toFile(project.getProjectDirectory()));
        }
    }

    @ForAllEnvironments
    public void testBuildSample_Sftp_Gnu_Arguments_With_Space_Once() throws Exception {
        buildSample(Sync.FTP, Toolchain.GNU, "Arguments", "Args Sftp Gnu WithSpace Once", 1,
                getSampleBuildTimeout(), getSampleBuildTimeout()/3, new ExecutableSetter());
    }

    @ForAllEnvironments
    public void testBuildSample_Sftp_Gnu_Arguments_With_Space_Multy() throws Exception {
        buildSample(Sync.FTP, Toolchain.GNU, "Arguments", "Args Sftp Gnu With Space Multy", 3, 
                getSampleBuildTimeout(), getSampleBuildTimeout()/3, new ExecutableSetter());
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteBuildSamplesTestCase.class);
    }
}
