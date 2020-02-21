/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fileoperations.spi;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import junit.framework.Test;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.impl.fs.RemoteFileObject;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemManager;
import org.netbeans.modules.remote.impl.fs.RemoteFileTestBase;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class RemoteVcsSupportUtilTestCase extends RemoteFileTestBase {

//    static {
//        System.setProperty("remote.fs_server.verbose", "8");
//        System.setProperty("remote.fs_server.suppress.stderr", "false");
//    }

    public RemoteVcsSupportUtilTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testCanRead() throws Exception {
        String basePath = mkTemp(execEnv, true);
        try {
            final String origFileName = "orig.file";
            final String origFilePath = basePath + "/" + origFileName;
            final String inexistentPath = basePath + "/inexustent_file";
            final String origNotReadableFileName = "not.readab.e.file";
            final String origNotReadableFilePath = basePath + "/" + origNotReadableFileName;
//            final String origSubdirPath = path + "/orig.dir";
//            final String origFileInSubdirName = "orig.file_in_dir";
//            final String origFileInSubdirPath = origSubdirPath + "/" + origFileInSubdirName;
//
//            final String lnkFilePathAbs = path + "/lnk.file.abs";
//            final String lnkFilePathRel1 = path + "/lnk.file.rel.1";
//            final String lnkFilePathRel2 = path + "/lnk.file.rel.2";
//
//            final String lnkFileInSubdirPathAbs = origSubdirPath + "/lnk.file_in_dir.abs";
//            final String lnkFileInSubdirPathRel = origSubdirPath + "/lnk.file_in_dir.rel";

            final String script = 
                    "echo abcd > " + origFilePath + "; " +
//                    "mkdir -p " + origSubdirPath + "; " +
//                    "echo qwerty > " + origFileInSubdirPath + "; " +
                    "echo asdf22 > " + origNotReadableFilePath + "; " +
                    "chmod -r " + origNotReadableFilePath + "; " +
//                    "cd " + origSubdirPath + "; " +
//                    "ln -s " + origFilePath + " " + lnkFilePathAbs + "; " +
//                    "ln -s " + origFileName + " " + lnkFilePathRel1 + "; " +
//                    "ln -s ./" + origFileName + " " + lnkFilePathRel2 + "; " +
//                    "cd " + origSubdirPath + "; " +
//                    "ln -s " + origFileInSubdirPath + " " + lnkFileInSubdirPathAbs + "; " +
//                    "ln -s " + origFileInSubdirName + " " + lnkFileInSubdirPathRel + "; " +
                    "";
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing sc    ript \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);

            assertEquals(true, RemoteVcsSupportUtil.canRead(fs, origFilePath));
            assertEquals(false, RemoteVcsSupportUtil.canRead(fs, origNotReadableFilePath));
            assertEquals(false, RemoteVcsSupportUtil.canRead(fs, inexistentPath));

            refreshParentAndRecurse(basePath);
            
            assertEquals(true, RemoteVcsSupportUtil.canRead(fs, origFilePath));
            assertEquals(false, RemoteVcsSupportUtil.canRead(fs, origNotReadableFilePath));
            assertEquals(false, RemoteVcsSupportUtil.canRead(fs, inexistentPath));

        } finally {
            if (basePath != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), 
                        "sh", "-c", "chmod -R 700" + basePath + "; rm -rf " + basePath);
            }            
        }
    }
    

    @ForAllEnvironments
    public void testGetCanonicalPath() throws Exception {
        String basePath = mkTemp(execEnv, true);
        try {
            final String origFileName = "orig.file";
            final String origFilePath = basePath + "/" + origFileName;
            final String origSubdirPath = basePath + "/orig.dir";
            final String origFileInSubdirName = "orig.file_in_dir";
            final String origFileInSubdirPath = origSubdirPath + "/" + origFileInSubdirName;
            
            final String lnkFilePathAbs = basePath + "/lnk.file.abs";
            final String lnkFilePathRel1 = basePath + "/lnk.file.rel.1";
            final String lnkFilePathRel2 = basePath + "/lnk.file.rel.2";
            
            final String lnkFileInSubdirPathAbs = origSubdirPath + "/lnk.file_in_dir.abs";
            final String lnkFileInSubdirPathRel = origSubdirPath + "/lnk.file_in_dir.rel";

            final String script = 
                    "echo abcd > " + origFilePath + "; " +
                    "mkdir -p " + origSubdirPath + "; " +
                    "echo q qwerty > " + origFileInSubdirPath + "; " +
                    "cd " + origSubdirPath + "; " +
                    "ln -s " + origFilePath + " " + lnkFilePathAbs + "; " +
                    "ln -s " + origFileName + " " + lnkFilePathRel1 + "; " +
                    "ln -s ./" + origFileName + " " + lnkFilePathRel2 + "; " +
                    "cd " + origSubdirPath + "; " +
                    "ln -s " + origFileInSubdirPath + " " + lnkFileInSubdirPathAbs + "; " +
                    "ln -s " + origFileInSubdirName + " " + lnkFileInSubdirPathRel + "; " +
                    "";
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing sc    ript \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);
            
            assertEquals(null, RemoteVcsSupportUtil.getCanonicalPath(fs, origFilePath));
            assertEquals (origFilePath, RemoteVcsSupportUtil.getCanonicalPath(fs, lnkFilePathAbs));

            refreshParentAndRecurse(basePath);

            assertEquals(null, RemoteVcsSupportUtil.getCanonicalPath(fs, origFilePath));
            assertEquals (origFilePath, RemoteVcsSupportUtil.getCanonicalPath(fs, lnkFilePathAbs));
            
        } finally {
            if (basePath != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), 
                        "sh", "-c", "chmod -R 700" + basePath + "; rm -rf " + basePath);
            }
            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);
        }
    }

    @ForAllEnvironments
    public void testIsSymbolicLink() throws Exception {
        String basePath = mkTemp(execEnv, true);
        try {
            final String origFileName = "orig.file";
            final String origFilePath = basePath + "/" + origFileName;
            final String origSubdirPath = basePath + "/orig.dir";
            final String origFileInSubdirName = "orig.file_in_dir";
            final String origFileInSubdirPath = origSubdirPath + "/" + origFileInSubdirName;

            final String lnkFilePathAbs = basePath + "/lnk.file.abs";
            final String lnkFilePathRel1 = basePath + "/lnk.file.rel.1";
            final String lnkFilePathRel2 = basePath + "/lnk.file.rel.2";

            final String lnkFileInSubdirPathAbs = origSubdirPath + "/lnk.file_in_dir.abs";
            final String lnkFileInSubdirPathRel = origSubdirPath + "/lnk.file_in_dir.rel";

            final String script =
                    "echo abcd > " + origFilePath + "; " +
                    "mkdir -p " + origSubdirPath + "; " +
                    "echo q qwerty > " + origFileInSubdirPath + "; " +
                    "cd " + origSubdirPath + "; " +
                    "ln -s " + origFilePath + " " + lnkFilePathAbs + "; " +
                    "ln -s " + origFileName + " " + lnkFilePathRel1 + "; " +
                    "ln -s ./" + origFileName + " " + lnkFilePathRel2 + "; " +
                    "cd " + origSubdirPath + "; " +
                    "ln -s " + origFileInSubdirPath + " " + lnkFileInSubdirPathAbs + "; " +
                    "ln -s " + origFileInSubdirName + " " + lnkFileInSubdirPathRel + "; " +
                    "";
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing sc    ript \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            assertEquals(false, RemoteVcsSupportUtil.isSymbolicLink(fs, origFilePath));
            assertEquals(true, RemoteVcsSupportUtil.isSymbolicLink(fs, lnkFilePathAbs));
            assertEquals(true, RemoteVcsSupportUtil.isSymbolicLink(fs, lnkFilePathRel1));

            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);


            refreshParentAndRecurse(basePath);


        } finally {
            if (basePath != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(),
                        "sh", "-c", "chmod -R 700" + basePath + "; rm -rf " + basePath);
            }
            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);
        }
    }

    @ForAllEnvironments
    public void testGetOutputStream() throws Exception {
        String basePath = mkTemp(execEnv, true);
        try {
            String subdir = basePath + "/subdir.1/subdir.2";
            String path = subdir + "/file.1";
            String text = "12345\n";
            final String script =
                    "mkdir -p " + subdir + "; " +
                    "echo 123 > " + path + "; ";
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing sc    ript \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);
            OutputStream os = RemoteVcsSupportUtil.getOutputStream(fs, path);
            PrintWriter w = new PrintWriter(new OutputStreamWriter(os));
            w.append(text);
            w.close();
            RemoteFileObject fo = getFileObject(path);
            String actualText = readFile(fo);
            assertEquals(text, actualText);
        } finally {
            if (basePath != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(),
                        "sh", "-c", "chmod -R 700" + basePath + "; rm -rf " + basePath);
            }
            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);
        }
    }

    @ForAllEnvironments
    public void testSetLastModified() throws Exception {
        String basePath = mkTemp(execEnv, true);
        try {
            final String file = "file.1";
            final String refFile = "file.2";
            final String absPath = basePath + '/' + file;
            final String absRefPath = basePath + '/' + refFile;
            final String script =
                    "touch " + file + "; " +
                    "sleep 2; " +
                    "touch " + refFile + ";";
            ProcessUtils.ExitStatus res = ProcessUtils.executeInDir(basePath, execEnv, "sh", "-c", script);
            assertEquals("Error executing sc    ript \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);
            //VCSFileProxy baseProxy = VCSFileProxy.createFileProxy(getFileObject(basePath));
            //VCSFileProxy proxy = VCSFileProxy.createFileProxy(baseProxy, file);
            //VCSFileProxy refProxy = VCSFileProxy.createFileProxy(baseProxy, refFile);
            refreshParentAndRecurse(basePath);
            RemoteVcsSupportUtil.setLastModified(fs, absPath, absRefPath);
            RemoteFileObject fo = getFileObject(absPath);
            RemoteFileObject refFo = getFileObject(absRefPath);
            assertEquals(refFo.lastModified(), fo.lastModified());            
        } finally {
            if (basePath != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(),
                        "sh", "-c", "chmod -R 700" + basePath + "; rm -rf " + basePath);
            }
            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);
        }
    }

    private void refreshParentAndRecurse(String path) throws Exception {
        String parentPath = PathUtilities.getDirName(path);
        final RemoteFileObject parentFO = getFileObject(parentPath);
        FileSystemProvider.refresh(parentFO, false);
        FileObject fo = getFileObject(path);
        fo.refresh();
        recurse(fo);
    }

    private void recurse(FileObject fo) {
        if (fo.isFolder()) {
            for (FileObject child : fo.getChildren()) {
                recurse(child);
            }
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteVcsSupportUtilTestCase.class);
    }
}
