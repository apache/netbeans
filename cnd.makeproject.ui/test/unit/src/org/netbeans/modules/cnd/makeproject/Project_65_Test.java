/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Teste for #198129 - Error opening a project created with NB 6.5
 */
public class Project_65_Test extends CndBaseTestCase {

    public Project_65_Test(String testName) {
        super(testName);
    }

    @Override
    protected boolean addEditorSupport() {
        return false;
    }

    protected MakeProject openProject(String projectName) throws IOException, Exception, IllegalArgumentException {
        File scriptFile = new File(getTestCaseDataDir(), "pre-process.sh");
        if (scriptFile.exists()) {
            ExitStatus res = ProcessUtils.executeInDir(getTestCaseDataDir().getAbsolutePath(), 
                    ExecutionEnvironmentFactory.getLocal(), "/bin/sh", scriptFile.getAbsolutePath());
            assertTrue(res.getErrorString(), res.isOK());
        }        
        FileObject projectDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(getDataFile(projectName)));
        MakeProject makeProject = (MakeProject) ProjectManager.getDefault().findProject(projectDirFO);
        assertNotNull("project is null", makeProject);
        return makeProject;
    }
    
    public void testMakeProj65() throws Throwable {
        if (Utilities.isWindows()) {
            return;
        }
        MakeProject makeProject = openProject("makeproj-with-link-PRJ-65");
        OpenProjectList.getDefault().open(makeProject);
        ConfigurationDescriptorProvider cdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        assertNotNull("Null ConfigurationDescriptorProvider", cdp);
        cdp.getConfigurationDescriptor();
        assertTrue("cdp.gotDescriptor returned false", cdp.gotDescriptor());
        Throwable lastAssertion = CndUtils.getLastAssertion();
        if (lastAssertion != null) {
            throw lastAssertion;
        }
    }

    protected int getSampleBuildTimeout() throws Exception {
        int result = 180;
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String timeout = rcFile.get("makeproject", "build.timeout");
        if (timeout != null) {
            result = Integer.parseInt(timeout);
        }
        return result;
    }    
}
