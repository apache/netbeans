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

package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
/**
 *
 */
public class CanonicalTestCase extends RemoteFileTestBase {

    public CanonicalTestCase(String testName) {
        super(testName);
    }
    
    public CanonicalTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }


    @ForAllEnvironments
    public void testCanonical() throws Exception {
        String baseDir = mkTempAndRefreshParent(true);
        try {
            String origDir = "orig-dir";
            String lnkDir1 = "lnk-dir-1";
            String lnkDir2 = "lnk-dir-2";
            String lnkDir3 = "lnk-dir-3";
            String lnkDirA = "lnk-dir-a";
            String lnkDirInextistent = "lnk-dir-inexistent";
            
            String origFile = "orig-file";
            String lnkFile1 = "lnk-file-1";
            String lnkFile2 = "lnk-file-2";
            String lnkFile3 = "lnk-file-3";
            String lnkFileA = "lnk-file-a";

            String script = 
                    "cd " + baseDir + "; " +
                    "mkdir -p " + origDir + "; " +
                    "ln -s " + origDir + ' ' + lnkDir1 + "; " +
                    "ln -s " + lnkDir1 + ' ' + lnkDir2 + "; " +
                    "ln -s " + lnkDir2 + ' ' + lnkDir3 + "; " +
                    "ln -s " + baseDir + '/' + origDir + ' ' + lnkDirA + "; " +
                    "ln -s " + baseDir + "/inexistent-dir" + ' ' + lnkDirInextistent + "; " +
                    "echo 123 > " + origFile + "; " +            
                    "ln -s " + origFile + ' ' + lnkFile1 + "; " +
                    "ln -s " + lnkFile1 + ' ' + lnkFile2 + "; " +
                    "ln -s " + lnkFile2 + ' ' + lnkFile3 + "; " +
                    "ln -s " + baseDir + '/' + origFile + ' ' + lnkFileA;
            
            execute("sh", "-c", script);
            
            FileObject baseDirFO = getFileObject(baseDir);
            FileObject origDirFO = getFileObject(baseDirFO, origDir);
            FileObject origFileFO = getFileObject(baseDirFO, origFile);
            
            checkCanonical(getFileObject(baseDirFO, lnkDir1), origDirFO);
            checkCanonical(getFileObject(baseDirFO, lnkDir2), origDirFO);
            checkCanonical(getFileObject(baseDirFO, lnkDir3), origDirFO);
            checkCanonical(getFileObject(baseDirFO, lnkDirA), origDirFO);
            
            checkCanonical(getFileObject(baseDirFO, lnkFile1), origFileFO);
            checkCanonical(getFileObject(baseDirFO, lnkFile2), origFileFO);
            checkCanonical(getFileObject(baseDirFO, lnkFile3), origFileFO);
            checkCanonical(getFileObject(baseDirFO, lnkFileA), origFileFO);

            String inexistent;
            inexistent = baseDir + "/inexistent1";
            checkCanonical(inexistent, inexistent);
            checkCanonical(inexistent + "/aa/bb/../cc", inexistent + "/aa/cc");
            inexistent = baseDir + '/' + lnkDir1 + "/inexistent2";
            checkCanonical(inexistent, inexistent);
            inexistent = baseDir + '/' + lnkDirInextistent;
            checkCanonical(inexistent, inexistent);
            inexistent = baseDir + '/' + lnkDirInextistent + "/inexistent3";
            checkCanonical(inexistent, inexistent);
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }
    
    @ForAllEnvironments
    public void testCyclicLinks() throws Exception {
        String baseDir = mkTempAndRefreshParent(true);
        try {
            String link0 = "link1";
            String link1 = "link2";
            String link3 = "link3";
            String script = 
                    "cd " + baseDir + "; " +
                    "ln -s " + link0 + ' ' + link1 + "; " +
                    "ln -s " + link1 + ' ' + link0 + "; " +
                    "ln -s " + "inexistent" + ' ' + link3;
            execute("sh", "-c", script);
            FileObject baseDirFO = getFileObject(baseDir);
            baseDirFO.refresh();
            final FileObject[] links = new FileObject[3];
            links[0] = (RemoteFileObject) getFileObject(baseDirFO, link0);
            links[1] = (RemoteFileObject) getFileObject(baseDirFO, link1);
            links[2] = (RemoteFileObject) getFileObject(baseDirFO, link3);
            
            final IOException[] exceptions = new IOException[3];
            final FileObject[] canonical = new FileObject[3];
            final CountDownLatch latch = new CountDownLatch(1);
            final String[] operations = new String[4];
            final AtomicReference<Integer> idx = new AtomicReference(Integer.valueOf(1));
            
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        idx.set(0);
                        operations[idx.get()] = "Resolvoing cyclic link " + links[idx.get()].getPath();
                        canonical[idx.get()] = FileSystemProvider.getCanonicalFileObject(links[idx.get()]);
                        System.err.printf("%s\n", canonical[idx.get()].getPath());
                    } catch (IOException ex) {
                        exceptions[0] = ex;
                    }
                    try {
                        idx.set(1);
                        operations[idx.get()] = "Resolvoing cyclic link " + links[idx.get()].getPath();
                        canonical[idx.get()] = FileSystemProvider.getCanonicalFileObject(links[idx.get()]);
                        System.err.printf("%s\n", canonical[idx.get()].getPath());
                    } catch (IOException ex) {
                        exceptions[1] = ex;
                    }
                    try {
                        idx.set(2);
                        operations[idx.get()] = "Resolvoing cyclic link " + links[idx.get()].getPath();
                        canonical[idx.get()] = FileSystemProvider.getCanonicalFileObject(links[idx.get()]);
                        System.err.printf("%s\n", canonical[idx.get()].getPath());
                    } catch (IOException ex) {
                        exceptions[2] = ex;
                    }
                    
                    idx.set(3);
                    operations[idx.get()] = "Refreshing cyclic link " + links[0].getPath();
                    links[0].refresh();
                    
                    latch.countDown();
                }
            };
            Thread thread = new Thread(r);
            thread.start();
            System.out.printf("Waiting... \n");
            boolean ok = latch.await(30, TimeUnit.SECONDS);
            assertTrue(operations[idx.get()] + " aborted by timeout ", ok);
            for (int i = 0; i < exceptions.length; i++) {
                assertNotNull(operations[i] + " should throw an exception", exceptions[i]);
            }
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    private void checkCanonical(String orig, String canonicalShouldBe) throws Exception {
        final String canonical = FileSystemProvider.getCanonicalPath(execEnv, orig);
        assertEquals("Canonical path differs for " + orig, canonicalShouldBe, canonical);
    }
    private void checkCanonical(FileObject orig, FileObject canonicalShouldBe) throws Exception {
        FileObject canonical = FileSystemProvider.getCanonicalFileObject(orig);
        assertEquals(canonical, orig.getCanonicalFileObject());
        assertNotNull("Null canonical file object for " + orig, canonical);
        assertEquals("Canonical file object differs for " + orig, canonicalShouldBe, canonical);
        String path = FileSystemProvider.getCanonicalPath(orig);
        assertEquals("Canonical path differ for " + orig, canonicalShouldBe.getPath(), path);
        path = FileSystemProvider.getCanonicalPath(fs, orig.getPath());
        assertEquals("Canonical path differ for " + orig, canonicalShouldBe.getPath(), path);        
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(CanonicalTestCase.class);
    }
}
