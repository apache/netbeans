/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.fs;

import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.indexing.impl.TextIndexStorageManager;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.remote.test.RemoteBuildTestBase;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.repository.support.RepositoryTestUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class FullRemoteCodeModelTestCase extends RemoteBuildTestBase {

    public FullRemoteCodeModelTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        startupModel();
        System.setProperty("cnd.mode.unittest", "true");
        System.setProperty("org.netbeans.modules.cnd.apt.level", "OFF"); // NOI18N
        Logger.getLogger("org.netbeans.modules.editor.settings.storage.Utils").setLevel(Level.SEVERE);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        shutdownModel();
    }

    private void shutdownModel() {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        model.shutdown();
        ModelSupport.instance().shutdown();
        TextIndexStorageManager.shutdown();
        RepositoryTestUtils.deleteDefaultCacheLocation();
    }

    private void startupModel() {
        RepositoryTestUtils.deleteDefaultCacheLocation();
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        model.startup();
        ModelSupport.instance().startup();
    }

    protected void processSample(String sampleName) throws Exception {
        final ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        String remoteTempDir = null;
        try {
            remoteTempDir = mkTempAndRefreshParent(true);
            FileObject remoteProjectDirBase = getFileObject(remoteTempDir);            
            FileObject localProjFO = prepareSampleProject(sampleName, sampleName);
            FileObject remoteProjectFO = FileUtil.copyFile(localProjFO, remoteProjectDirBase, sampleName);            
            assertTrue("Should be remote: " + remoteProjectFO, FileSystemProvider.getExecutionEnvironment(remoteProjectFO).isRemote());            
            MakeProject makeProject = (MakeProject) ProjectManager.getDefault().findProject(remoteProjectFO);            
            assertTrue("Should be remote: " + makeProject.getProjectDirectory(), FileSystemProvider.getExecutionEnvironment(makeProject.getProjectDirectory()).isRemote());
            assertTrue("Host should be connected at this point", ConnectionManager.getInstance().isConnectedTo(execEnv));
            OpenProjects.getDefault().open(new Project[]{makeProject}, false);
            checkCodeModel(makeProject);
        } finally {
            if (remoteTempDir != null) {
                CommonTasksSupport.rmDir(execEnv, remoteTempDir, true, new OutputStreamWriter(System.err));
            }
        }        
    }
    
    @Override
    protected void checkCodeModel(MakeProject makeProject) throws Exception {
        CsmProject csmProject = getCsmProject(makeProject);
        assertNotNull("Null CsmProject", csmProject);
        csmProject.waitParse();
        Collection<CsmFile> allFiles = csmProject.getAllFiles();
        assertEquals("Expected amount of csm files ", 1, allFiles.size());
        CsmFile mainFile = allFiles.iterator().next();
        Collection<CsmOffsetableDeclaration> decls = mainFile.getDeclarations();
        assertEquals("Excpected amount of declarations", 1, decls.size());
        CsmOffsetableDeclaration decl = decls.iterator().next();
        assertEquals("Declaration name", "main", decl.getName().toString());
        assertTrue("File object should be remote", FileSystemProvider.getExecutionEnvironment(mainFile.getFileObject()).isRemote());
    }

    @ForAllEnvironments
    public void testArguments() throws Exception {
        processSample("Arguments");
    }
    
    public static Test suite() {
        return new RemoteDevelopmentTest(FullRemoteCodeModelTestCase.class);
    }    
}
