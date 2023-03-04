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

package org.netbeans.modules.subversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.utils.TestUtilities;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 *
 * @author Tomas Stupka
 */
public class ApiTest extends NbTestCase {
    private String username;
    private String password;
    private File workDir;
    private File repoDir;
          
    public ApiTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {          
        super.setUp();
        MockServices.setServices(new Class[] {
            SubversionVCS.class});
        workDir = new File(getWorkDir(), "wc");

        File dataRootDir = new File(getWorkDirPath()); ;
        repoDir = new File(dataRootDir, "repo");
        FileUtils.deleteRecursively(repoDir);
        TestKit.initRepo(repoDir, workDir);

        System.setProperty("svnClientAdapterFactory", "commandline");
        System.setProperty("netbeans.user", getDataDir().getAbsolutePath());
        
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
            br.readLine(); // kenai username, just skip it
            br.readLine(); // kenai password, just skip it

            String proxy = br.readLine();
            String port = br.readLine();

            if(proxy != null) {
                System.setProperty("https.proxyHost", proxy);
                System.setProperty("https.proxyPort", port);
            }
        } finally {
            if(br != null) br.close();        
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteRecursively(workDir);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public void testCheckout() throws MalformedURLException, SVNClientException, IOException {
        FileUtils.deleteRecursively(workDir);
        // XXX use onw repo
        TestKit.mkdirs(repoDir, "folder1");
        TestKit.mkdirs(repoDir, "folder2");

        org.netbeans.modules.subversion.api.Subversion.checkoutRepositoryFolder(
                TestUtilities.formatFileURL(repoDir),
                new String[0],
                workDir,
                username,
                password,
                false);

        assertTrue(workDir.exists());
        assertEquals(3, workDir.list().length); // two folders + metadata

        org.netbeans.modules.subversion.api.Subversion.checkoutRepositoryFolder(
                TestUtilities.formatFileURL(repoDir),
                new String[] {""},
                workDir,
                username,
                password,
                false);

        assertTrue(workDir.exists());
        assertEquals(3, workDir.list().length); // two folders + metadata

        FileUtils.deleteRecursively(workDir);
        org.netbeans.modules.subversion.api.Subversion.checkoutRepositoryFolder(
                TestUtilities.formatFileURL(repoDir),
                new String[] {"folder1"},
                workDir,
                username,
                password,
                false);

        assertTrue(workDir.exists());
        assertEquals(1, workDir.list().length); // one folder

    }

    @RandomlyFails
    public void testCheckoutLocalLevel() throws MalformedURLException, SVNClientException, IOException {
        FileUtils.deleteRecursively(workDir);
        TestKit.mkdirs(repoDir, "testCheckoutLocalLevelfolder1/folder2/folder3");
        String url = TestUtilities.formatFileURL(repoDir) + "/testCheckoutLocalLevelfolder1/folder2";

        org.netbeans.modules.subversion.api.Subversion.checkoutRepositoryFolder(
                url,
                new String[0],
                workDir,
                username,
                password,
                true,
                false);

        assertTrue(workDir.exists());
        String[] files = workDir.list();
        assertEquals(2, files.length); // one folder + metadata

        String s = null;
        for (String f : files) {
            if(f.equals("folder3")) {
                s = f;
                break;
            }
        }
        assertEquals("folder3", s);
    }

    public void testMkdirMalformed() throws SVNClientException, IOException {
        MalformedURLException mue = null;
        try {
            org.netbeans.modules.subversion.api.Subversion.mkdir("crap", "", "", "creating dir");
        } catch (MalformedURLException e) {
            mue = e;
        }
        assertNotNull(mue);
    }

    public void testMkdir() throws SVNClientException, IOException {
        String url1 = TestUtilities.formatFileURL(repoDir) + "/foldertestMkdir";
        org.netbeans.modules.subversion.api.Subversion.mkdir(
                url1,
                "", "", "creating dir");
        ISVNInfo info = TestKit.getSVNInfo(url1);
        assertNotNull(info);

        String url2 = TestUtilities.formatFileURL(repoDir) + "/foldertestMkdir/foldertestMkdir2";
        org.netbeans.modules.subversion.api.Subversion.mkdir(
                url2,
                "", "", "creating dir");
        info = TestKit.getSVNInfo(url2);
        assertNotNull(info);
    }

    public void testCommit() throws SVNClientException, IOException {
        File folder = new File(workDir, "testCommitFolder");
        folder.mkdirs();
        TestKit.svnimport(repoDir, folder);

        ISVNStatus s = TestKit.getSVNStatus(folder);
        assertEquals(SVNStatusKind.NORMAL, s.getTextStatus());

        File file = new File(folder, "file");
        file.createNewFile();
        TestKit.add(file);
        s = TestKit.getSVNStatus(file);
        assertEquals(SVNStatusKind.ADDED, s.getTextStatus());

        Subversion.getInstance().versionedFilesChanged();
        SvnUtils.refreshParents(folder);
        Subversion.getInstance().getStatusCache().refreshRecursively(folder);

        org.netbeans.modules.subversion.api.Subversion.commit(new File[] {folder}, "", "", "msg");
        s = TestKit.getSVNStatus(file);
        assertEquals(SVNStatusKind.NORMAL, s.getTextStatus());

    }

    public void testValidateUrl () {
        assertEquals(false, org.netbeans.modules.subversion.api.Subversion.isRepository("blablabla"));
        assertEquals(false, org.netbeans.modules.subversion.api.Subversion.isRepository("http://"));
        assertEquals(false, org.netbeans.modules.subversion.api.Subversion.isRepository("file://"));
        assertEquals(false, org.netbeans.modules.subversion.api.Subversion.isRepository("file:///"));
        assertEquals(false, org.netbeans.modules.subversion.api.Subversion.isRepository("svn://"));
        assertEquals(false, org.netbeans.modules.subversion.api.Subversion.isRepository("svn+ssh://"));
        assertEquals(false, org.netbeans.modules.subversion.api.Subversion.isRepository("file:///home/nowhere"));
        assertEquals(true, org.netbeans.modules.subversion.api.Subversion.isRepository(TestUtilities.formatFileURL(repoDir)));
        assertEquals(false, org.netbeans.modules.subversion.api.Subversion.isRepository("http://www.netbeans.org/"));
        assertEquals(false, org.netbeans.modules.subversion.api.Subversion.isRepository("https://www.netbeans.org/"));
        assertEquals(true, org.netbeans.modules.subversion.api.Subversion.isRepository("svn://vcs-test.cz.oracle.com:9630"));
        assertEquals(true, org.netbeans.modules.subversion.api.Subversion.isRepository("svn+ssh://vcs-test.cz.oracle.com/usr/svnrepo"));
        assertEquals(false, org.netbeans.modules.subversion.api.Subversion.isRepository("https://vcs-test.cz.oracle.com/svnsecure"));
        assertEquals(false, org.netbeans.modules.subversion.api.Subversion.isRepository("http://vcs-test.cz.oracle.com/svnrepo"));
    }

//    public void testOpenCheckoutWizard () throws MalformedURLException, IOException {
//        SvnModuleConfig.getDefault().getPreferences().put(CheckoutStep.CHECKOUT_DIRECTORY, getWorkDirPath());
//        org.netbeans.modules.subversion.api.Subversion.openCheckoutWizard("https://kenai.com/svn/motorbikediary~source-code-repository");
//    }
}
