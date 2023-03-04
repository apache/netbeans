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

package org.netbeans.modules.subversion.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.modules.subversion.AbstractSvnTestCase;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.cli.CommandlineClient;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.subversion.utils.TestUtilities;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;
//import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;

/**
 *
 * @author tomas
 */
public abstract class AbstractCommandTestCase extends AbstractSvnTestCase {
    
    protected boolean importWC;
    protected String CI_FOLDER = "cifolder";    
    protected FileNotifyListener fileNotifyListener;
    private Process server;

    public AbstractCommandTestCase(String testName) throws Exception {
        super(testName);
    }

    protected void runSvnServer () throws IOException {
        stopSvnServer();
        String[] cmd = new String[]{"svnserve", "-d"};
        server = Runtime.getRuntime().exec(cmd);
    }

    protected void stopSvnServer () {
        if (server != null) {
            server.destroy();
            server = null;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if(getName().startsWith("testCheckout") ) {
            cleanUpRepo(new String[] {CI_FOLDER});
        }
        //CmdLineClientAdapterFactory.setup();
    }
    
    @Override
    protected void tearDown() throws Exception {
        stopSvnServer();
        if(getName().startsWith("testInfoLocked")) { 
            try {
                unlock(createFile("lockfile"), "unlock", true);
            } catch (Exception e) {
                // ignore
            }
        }
        super.tearDown();
    }    
 

    protected void setAnnonWriteAccess() throws IOException {
        FileUtils.copyFile(new File(getRepoDir().getAbsolutePath() + "/conf/svnserve.conf"), new File(getRepoDir().getAbsolutePath() + "/conf/svnserve.conf.bk"));
        write(new File(getRepoDir().getAbsolutePath() + "/conf/svnserve.conf"), "[general]\nanon-access = write\nauth-access = write\nauthz-db = authz");
        FileUtils.copyFile(new File(getRepoDir().getAbsolutePath() + "/conf/authz"), new File(getRepoDir().getAbsolutePath() + "/conf/authz.bk"));        
        write(new File(getRepoDir().getAbsolutePath() + "/conf/authz"), "[/]\n* = rw");
    }

    protected void restoreAuthSettings() throws IOException {
        FileUtils.copyFile(new File(getRepoDir().getAbsolutePath() + "/conf/svnserve.conf.bk"), new File(getRepoDir().getAbsolutePath() + "/conf/svnserve.conf"));
        FileUtils.copyFile(new File(getRepoDir().getAbsolutePath() + "/conf/authz.bk"), new File(getRepoDir().getAbsolutePath() + "/conf/authz"));        
    }

    protected void anoncommit(File file) throws IOException, InterruptedException {
        // no way to push empty user
        String[] cmd = new String[]{"svn", "ci", file.getAbsolutePath(), "-m", "\"commit\"", "--username="};
        Runtime.getRuntime().exec(cmd).waitFor();
    }

    protected void assertInfos(ISVNInfo info, ISVNInfo refInfo) {
        assertNotNull(info);   
        assertNotNull(refInfo);   
        assertEquals(refInfo.getCopyRev(), info.getCopyRev());
        assertEquals(refInfo.getCopyUrl(), info.getCopyUrl());
        //assertEquals(refInfo.getFile(), info.getFile());
        assertEquals(DateFormat.getDateTimeInstance().format(refInfo.getLastChangedDate()), DateFormat.getDateTimeInstance().format(info.getLastChangedDate()));
        assertEquals(refInfo.getLastChangedRevision(), info.getLastChangedRevision());
        assertEquals(refInfo.getLastCommitAuthor(), info.getLastCommitAuthor());
        assertEquals(refInfo.getLastDatePropsUpdate(), info.getLastDatePropsUpdate());
        if (info.getLastDateTextUpdate() == null || refInfo.getLastDateTextUpdate() == null) {
            assertTrue("" + refInfo.getLastDateTextUpdate() + " --- " + info.getLastDateTextUpdate(), 
                    (refInfo.getLastDateTextUpdate() == null || refInfo.getLastDateTextUpdate().getTime() == 0)
                    && ((info.getLastDateTextUpdate() == null || info.getLastDateTextUpdate().getTime() == 0)));
        } else {
            assertEquals(refInfo.getLastDateTextUpdate(), info.getLastDateTextUpdate());
        }
        assertEquals(refInfo.getLockComment() != null ? refInfo.getLockComment().trim() : null, 
                     info.getLockComment() != null    ? info.getLockComment().trim()    : null);
        assertEquals(refInfo.getLockCreationDate(), info.getLockCreationDate());
        assertEquals(refInfo.getLockOwner(), info.getLockOwner());
        assertEquals(refInfo.getNodeKind(), info.getNodeKind());
        assertEquals(refInfo.getRepository(), info.getRepository());
        assertEquals(refInfo.getRevision(), info.getRevision());
        assertEquals(refInfo.getUrl(), info.getUrl());
        assertEquals(refInfo.getUrlString(), info.getUrlString());
        assertEquals(refInfo.getUuid(), info.getUuid());
    }
    
    protected void assertEntryArrays(ISVNDirEntry[] listedArray, ISVNDirEntry[] refArray) {
        assertEquals(listedArray.length, refArray.length);
        Map<String, ISVNDirEntry> entriesMap = new HashMap<String, ISVNDirEntry>();
        for (ISVNDirEntry e : listedArray) {
            entriesMap.put(e.getPath(), e);
        }
        ISVNDirEntry entry;
        for (int i = 0; i < refArray.length; i++) {
            entry = entriesMap.get(refArray[i].getPath());

            assertNotNull(entry);
            assertEquals(refArray[i].getPath(), entry.getPath());
            assertEquals(refArray[i].getHasProps(), entry.getHasProps());
            assertEquals(refArray[i].getLastChangedRevision(), entry.getLastChangedRevision());
            assertEquals(refArray[i].getLastCommitAuthor(), entry.getLastCommitAuthor());
            assertEquals(refArray[i].getNodeKind(), entry.getNodeKind());
            assertEquals(refArray[i].getSize(), entry.getSize());
            assertEquals(refArray[i].getLastChangedDate().toString(), entry.getLastChangedDate().toString());
        }
    }
        
    protected void assertNotifiedFiles(File... files) {

        Collection<File> sortedExpectedFiles = relativizeAndSortFiles(files);
        Collection<File> sortedNotifierFiles = relativizeAndSortFiles(fileNotifyListener.getFiles());
        
        if (!sortedExpectedFiles.equals(sortedNotifierFiles)) {
            // we will be fine if at least all given files were notified ...
            boolean weAreFine = true;
            for (File f : sortedExpectedFiles) {
                if(!sortedNotifierFiles.contains(f)) {
                    weAreFine = false;
                    break;
                }
            }
            if(weAreFine) return;

            String expectedNames = makeFilesList(sortedExpectedFiles);
            String actualNames   = makeFilesList(sortedNotifierFiles);

            System.err.println("Expected files: " + expectedNames);
            System.err.println("Notifier files: " + actualNames);

            String failureMsg = format("File lists do not match:", expectedNames, actualNames);
            Subversion.LOG.warning("assertNotifiedFiles: " + failureMsg);
            fail(failureMsg);
        }
    }

    private Collection<File> relativizeAndSortFiles(File[] files) {
        return relativizeAndSortFiles(Arrays.asList(files));
    }

    private Collection<File> relativizeAndSortFiles(Collection<File> files) {
        if (files.isEmpty()) {
            return Collections.<File>emptyList();
        }

        final File wc = getWC();

        List<File> result = new ArrayList<File>(files.size());
        for (File file : files) {
            result.add(getRelativePath(wc, file));
        }
        return sortFiles(result);
    }

    private static Collection<File> sortFiles(Collection<File> files) {

        final class FileComparator implements Comparator<File> {
            public int compare(File f1, File f2) {
                return f1.compareTo(f2);
            }
        }

        if (files.isEmpty()) {
            return Collections.<File>emptyList();
        }
        if (files.size() == 1) {
            return Collections.singletonList(files.iterator().next());
        }

        SortedSet<File> sortedFiles = new TreeSet<File>(new FileComparator());
        sortedFiles.addAll(files);
        return sortedFiles;
    }

    private static String makeFilesList(Collection<File> files) {
        if (files.isEmpty()) {
            return "";
        }

        StringBuilder buf = new StringBuilder(120);
        String separator = "";
        for (File file : files) {
            buf.append(separator).append(file.getPath());
            separator = ", ";
        }
        return buf.toString();
    }

    protected class FileNotifyListener implements ISVNNotifyListener {
        private Set<File> files = new HashSet<File>();
        public void setCommand(int arg0) { }
        public void logCommandLine(String arg0) { }
        public void logMessage(String arg0) { }
        public void logError(String arg0) { }
        public void logRevision(long arg0, String arg1) { }
        public void logCompleted(String arg0) { }
        public void onNotify(File file, SVNNodeKind arg1) {
            files.add(file);
        }
        public Set<File> getFiles() {            
            return files;
        }        
    }
        
    protected File createFolder(String name) throws IOException {
        File file = new File(getWC(), name);
        file.mkdirs();
        return file;
    }
    
    protected File createFolder(File folder, String name) throws IOException {
        File file = new File(folder, name);
        file.mkdirs();
        return file;
    }
    
    protected File createFile(File folder, String name) throws IOException {
        File file = new File(folder, name);
        file.createNewFile();
        return file;
    }
    
    protected File createFile(String name) throws IOException {
        File file = new File(getWC(), name);
        file.createNewFile();
        return file;
    }

    protected static String renameFile(String path, String requestedName) {
        File currentFile = new File(path);
        File parentDir = currentFile.getParentFile();
        File renamedFile = (parentDir == null) ? new File(requestedName)
                                               : new File(parentDir, requestedName);
        return renamedFile.getPath();
    }

    protected void createAndAddParentFolders(String path) throws Exception {
        checkIsRelativePath(path);
        File parentFolder = new File(path).getParentFile();
        if (parentFolder != null) {
            createAndAddFolder(parentFolder);
        }
    }

    private void createAndAddFolder(File folder) throws Exception {
        File parent = folder.getParentFile();
        if (parent != null) {
            createAndAddFolder(parent);
        }

        File absFolder = createFolder(folder.getPath());

        if (!isVersioned(absFolder)) {
            getNbClient().addDirectory(absFolder, false);
        }
    }

    protected void createAndCommitParentFolders(String path) throws Exception {
        checkIsRelativePath(path);
        File parentFolder = new File(path).getParentFile();
        if (parentFolder != null) {
            createAndCommitFolder(parentFolder);
        }
    }

    private void createAndCommitFolder(File folder) throws Exception {
        File parent = folder.getParentFile();
        if (parent != null) {
            createAndCommitFolder(parent);
        }

        File absFolder = createFolder(folder.getPath());

        if (!isVersioned(absFolder)) {
            getNbClient().addDirectory(absFolder, false);
        }

        if (!isCommitted(absFolder)) {
            getNbClient().commit(new File[] {absFolder},
                                 "added directory " + absFolder.getPath(),
                                 false);
        }
    }

    private boolean isVersioned(File file) throws Exception {
        ISVNStatus[] statusValues = getFullWorkingClient().getStatus(new File[]{file});
        return !((statusValues.length == 1)
               && (statusValues[0].getTextStatus() == SVNStatusKind.UNVERSIONED));
    }

    private boolean isCommitted(File file) throws Exception {
        ISVNStatus[] statusValues = getFullWorkingClient().getStatus(new File[]{file});
        if (statusValues.length == 1) {
            if (statusValues[0].getTextStatus() == SVNStatusKind.ADDED) {
                return false;
            }
            if (statusValues[0].getTextStatus() == SVNStatusKind.NORMAL) {
                return true;
            }
        }

        throw new IllegalStateException("Unexpected state of file " + file
                                        + ": " + statusValues);
    }

    protected SvnClient getNbClient() throws Exception {
        //        SvnClient c = SvnClientTestFactory.getInstance().createSvnClient();
        String fac = System.getProperty("svnClientAdapterFactory", "javahl");
        SvnClient c = SvnClientFactory.getInstance().createSvnClient();
        if ("svnkit".equals(fac)) {
            assertTrue(c.toString().contains("SvnKitClientAdapter"));
        } else if ("commandline".equals(fac)) {
            assertTrue(c.toString().contains("CommandlineClient"));
        } else {
            assertTrue(c.toString().contains("JhlClientAdapter"));
        }
        fileNotifyListener = new FileNotifyListener();
        c.addNotifyListener(fileNotifyListener);
        return c;
    }
    
    
    @Override
    protected SvnClient getFullWorkingClient() throws SVNClientException {
        String fac = System.getProperty("svnClientAdapterFactory", "javahl");
        boolean resetNeeded = !"javahl".equals(fac); // for javahl setup, there's no need to change anything
        try {
            if (resetNeeded) {
                SvnModuleConfig.getDefault().setPreferredFactoryType("javahl");
                System.setProperty("svnClientAdapterFactory", "javahl");
                SvnClientFactory.resetClient();
            }
            SvnClient c = SvnClientFactory.getInstance().createSvnClient();
            assertTrue(c.toString().contains("JhlClientAdapter"));
            return c;
        } finally {
            if (resetNeeded) {
                SvnModuleConfig.getDefault().setPreferredFactoryType(fac);
                System.setProperty("svnClientAdapterFactory", fac);
                SvnClientFactory.resetClient();
            }
        }
    }
    
//    protected ISVNClientAdapter getReferenceClient() throws Exception {
////        ISVNClientAdapter c = SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
////        fileNotifyListener = new FileNotifyListener();
////        c.addNotifyListener(fileNotifyListener);
////        return c;
//        return getFullWorkingClient();
//    }

    protected void clearNotifiedFiles() {
        fileNotifyListener.files.clear();
    }
                
    protected void write(File file, int data) throws IOException {
        OutputStream os = null;
        try {            
            os = new FileOutputStream(file);            
            os.write(data);
            os.flush();
        } finally {
            if (os != null) {
                os.close();
            }
        }        
    }

    protected void write(File file, String str) throws IOException {
        FileWriter w = null;
        try {            
            w = new FileWriter(file);            
            w.write(str);
            w.flush();
        } finally {
            if (w != null) {
                w.close();
            }
        }        
    }
    
    protected String read(File file) throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader r = null;
        try {            
            r = new BufferedReader(new FileReader(file));
            String s = r.readLine();
            while( true ) {
                sb.append(s);
                s = r.readLine();
                if (s == null) break;
                sb.append('\n');
            }
        } finally {
            if (r != null) {
                r.close();
            }
        }        
        return sb.toString();
    }
    
    
    
    protected void assertContents(File file, int contents) throws FileNotFoundException, IOException {        
        assertContents(new FileInputStream(file), contents);
    }
    
    protected void assertContents(InputStream is, int contents) throws FileNotFoundException, IOException {        
        try {
            int i = is.read();
            assertEquals(contents, i);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    
    protected void assertInputStreams(InputStream isref, InputStream is) throws FileNotFoundException, IOException {        
        if(isref == null || is == null) {
            assertNull(isref);
            assertNull(is);
        }
        int iref = -1;
        int i = -1;
        while( (iref = isref.read()) > -1 ) {
            i = is.read(); 
            assertEquals(iref, i);
        }
        i = is.read();
        assertEquals(iref, i);
    }
    
    protected void assertInfo(File file, SVNUrl url) throws SVNClientException {
        ISVNInfo info = getInfo(file);
        assertNotNull(info);
        assertEquals(url, TestUtilities.decode(info.getUrl()));
    }

    protected void assertCopy(SVNUrl url) throws SVNClientException {
        ISVNInfo info = getInfo(url);
        assertNotNull(info);
        assertEquals(url, TestUtilities.decode(info.getUrl()));
    }    

    protected boolean shouldBeTestedWithCurrentClient(boolean checkCLI, boolean checkJavaHl) throws Exception {
        if( checkCLI && isCommandLine() ||
            checkJavaHl && (isJavahl() || isSvnkit()))
        {
            checkAcceptedVersion();
            return false;
        }
        return true;
    }

    private void checkAcceptedVersion() throws SVNClientException {
        CommandlineClient c = new CommandlineClient();
        String version = c.getVersion();
        if (version.indexOf("version 0.") == -1  &&
            version.indexOf("version 1.0") == -1 &&
            version.indexOf("version 1.1") == -1 &&
            version.indexOf("version 1.2") == -1 &&
            version.indexOf("version 1.3") == -1 &&
            version.indexOf("version 1.4") == -1 &&
            version.indexOf("version 1.5") == -1 &&
            version.indexOf("version 1.6") == -1 &&
            version.indexOf("version 1.7") == -1 &&
            version.indexOf("version 1.8") == -1 &&
            version.indexOf("version 1.9") == -1) {
            fail("svn cli client seems to be > 1.9. Some tests might pass now...");
        }
    }

}
