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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.remote.test.RemoteBuildTestBase;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.openide.util.Utilities;
/**
 *
 */
public class RemoteBuildSharedTestCase extends RemoteBuildTestBase {

    private File sharedWorkDir;
    
    public RemoteBuildSharedTestCase(String testName) {
        super(testName);
    }

    public RemoteBuildSharedTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String sharedHome = getSharedHome();
        sharedWorkDir = (sharedHome == null) ? null : new File(sharedHome);
    }

    @Override
    public File getWorkDir() throws IOException {
        return (sharedWorkDir == null) ? super.getWorkDir() : sharedWorkDir;
    }

    @Override
    public String getWorkDirPath() {
        return (sharedWorkDir == null) ? super.getWorkDirPath() : sharedWorkDir.getAbsolutePath();
    }

    @ForAllEnvironments
    public void testBuildSample_Shared_Gnu_Arguments_Once() throws Exception {
        if (sharedWorkDir == null) {
            System.err.printf("Can not get shared home for %s\n", getTestExecutionEnvironment());
            return;
        }
        buildSample(Sync.SHARED, Toolchain.GNU, "Arguments", "Args_01", 1);
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteBuildSharedTestCase.class);
    }
    
    protected String getSharedHome() {
        String user = ExecutionEnvironmentFactory.getLocal().getUser();
        String[] sharedDirectories = getSharedDirectories();
        for (String dir : sharedDirectories) {
            String home = dir + '/' + user;
            ExitStatus rc = ProcessUtils.execute(ExecutionEnvironmentFactory.getLocal(), "test", "-w", home);
            if (rc.isOK()) {
                return home;
            }
        }
        return null;
    }
            
    protected String[] getSharedDirectories() {
        // Linux & Solaris:
        // /usr/sbin/exportfs
        // -               /export/home   rw   "export/home"
        // -               /export/opt   rw   "export/opt"
        if (Utilities.isUnix()) {
            ExitStatus rc = ProcessUtils.execute(ExecutionEnvironmentFactory.getLocal(), "/usr/sbin/exportfs");
            if (rc.isOK()) {
                List<String> res = new ArrayList<>();
                String[] lines = rc.getOutputString().split("\n");
                Pattern pattern = Pattern.compile("\t+| +"); // NOI18N
                for (String line : lines) {
                    String[] parts = pattern.split(line);
                    if (parts.length > 1) {
                        res.add(parts[1]);
                    }
                }
                return res.toArray(new String[res.size()]);
            }
        }
        return new String[0];
    }
}
