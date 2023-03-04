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

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTestCase;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;

/**
 *
 * @author tomas
 */
public class LogTestHidden extends AbstractCommandTestCase {

    private enum Log {
        file,
        url
    }
    
    public LogTestHidden(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        try {
            super.setUp();
        } catch (Exception e) {
            stopSvnServer();
        }
        if(getName().equals("testLogNullAuthor")) {
            setAnnonWriteAccess();
            runSvnServer();
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        if(getName().equals("testLogNullAuthor")) {
            restoreAuthSettings();
        }        
        super.tearDown();        
    }    

    @Override
    protected String getRepoURLProtocol() {
        if(getName().equals("testLogNullAuthor")) {        
            return "svn://localhost/";
        }
        return super.getRepoURLProtocol();
    }    
   
    public void testLogWrong() throws Exception {
        // XXX add ref client
        // no file
        File file = new File(getWC(), "file");

        ISVNClientAdapter c = getNbClient();

        SVNClientException e = null;
        try {
            c.getLogMessages(file, new SVNRevision.Number(0), SVNRevision.HEAD);
        } catch (SVNClientException ex) {
            e = ex;
        }
        assertNotNull(e);

        // unversioned file
        file = createFile("file");
        e = null;
        try {
            c.getLogMessages(file, new SVNRevision.Number(0), SVNRevision.HEAD);
        } catch (SVNClientException ex) {
            e = ex;
        }
        assertNotNull(e);

        // wrong url
        file = createFile("file");
        e = null;
        try {
            c.getLogMessages(getFileUrl(file), new SVNRevision.Number(0), SVNRevision.HEAD);
        } catch (SVNClientException ex) {
            e = ex;
        }
        assertNotNull(e);
    }

    // XXX no idea how to push a null username through svnclientadapter
//    public void testLogNullAuthor() throws Exception {
//        File file = createFile("file");
//        add(file);
//        write(file, "hira bancha");
//        commit(file, "hira bancha");
//        ISVNInfo info1 = getInfo(file);
//
//        write(file, "bancha muso");
//        commit(file, "bancha muso");
//        ISVNInfo info2 = getInfo(file);
//
//        ISVNLogMessage[] logsRef = getReferenceClient().getLogMessages(file, new SVNRevision.Number(0), SVNRevision.HEAD, false, false);
//        ISVNLogMessage[] logsNb = getNbClient().getLogMessages(file, new SVNRevision.Number(0), SVNRevision.HEAD, false, false);
//
//        // test
//        assertEquals(2, logsNb.length);
//        assertEquals("", logsNb[0].getAuthor());
//        assertEquals("", logsNb[1].getAuthor());
//
//        assertEquals("", logsNb[0].getAuthor());
//        assertEquals(info1.getLastChangedDate(), logsNb[0].getDate());
//        assertEquals("hira bancha", logsNb[0].getMessage());
//        assertEquals(info1.getRevision(), logsNb[0].getRevision());
//
//        assertEquals("", logsNb[1].getAuthor());
//        assertEquals(info2.getLastChangedDate(), logsNb[1].getDate());
//        assertEquals("bancha muso", logsNb[1].getMessage());
//        assertEquals(info2.getRevision(), logsNb[1].getRevision());
//
//        assertLogs(logsRef, logsNb);
//    }

    public void testLogCopied() throws Exception {
        File file = createFile("file");
        add(file);
        write(file, "hira bancha");
        commit(file, "hira bancha");
        ISVNInfo info1 = getInfo(file);

        write(file, "bancha muso");
        File copy = createFolder(file.getParentFile(), "copy");

        commit(file.getParentFile(), "bancha muso");
        ISVNInfo info2 = getInfo(file);

        copy(file, getFileUrl(copy));
        update(file.getParentFile());
        copy = new File(copy, file.getName());
        assertTrue(copy.exists());
        ISVNInfo info3 = getInfo(copy);

        ISVNLogMessage[] logsNb = getNbClient().getLogMessages(copy, new SVNRevision.Number(0), SVNRevision.HEAD, false, false);

        // test
        assertEquals(3, logsNb.length);
        assertLogMessage(info1, logsNb[0], "hira bancha",  new ISVNLogMessageChangePath[] { });
        assertLogMessage(info2, logsNb[1], "bancha muso",  new ISVNLogMessageChangePath[] { });
        assertLogMessage(info3, logsNb[2], "copy",  new ISVNLogMessageChangePath[] { });
    }

    public void testLogFileChangePaths() throws Exception {
        log(Log.file, true, false);
    }

    public void testLogUrlChangePaths() throws Exception {
        log(Log.url, true, false);
    }

    public void testLogFileChangePathsStopCopy() throws Exception {
        log(Log.file, true, true);
    }

    public void testLogUrlChangePathsStopCopy() throws Exception {
        log(Log.url, true, true);
    }

    public void testLogFileNoChangePaths() throws Exception {
        log(Log.file, false, false);
    }

    public void testLogUrlNoChangePaths() throws Exception {
        log(Log.url, false, false);
    }

    public void testLogFileNoChangePathsStopCopy() throws Exception {
        log(Log.file, false, true);
    }

    public void testLogUrlNoChangePathsStopCopy() throws Exception {
        log(Log.url, false, true);
    }

    public void testLogFileLimit() throws Exception {
        logLimit(Log.file);
    }

    public void testLogUrlLimit() throws Exception {
        logLimit(Log.url);
    }
    
    public void testLogPaths() throws Exception {                                
        File folder1 = createFolder("folder1");
        File file1 = createFile("file1");
        File folder2 = createFolder("folder2");
        File file2 = createFile("file2");
        
        add(folder1);
        add(folder2);
        add(file1);
        add(file2);
        write(file1, "1");        
        write(file2, "1");           
        commit(getWC(), "msg1");
        ISVNInfo info11 = getInfo(file1);        
        ISVNInfo info12 = getInfo(file2);        
        
        write(file1, "2");        
        write(file2, "2");      
        commit(getWC(), "msg2");
        ISVNInfo info21 = getInfo(file1);        
        ISVNInfo info22 = getInfo(file2);        
        
        write(file1, "3");        
        write(file2, "3");      
        commit(getWC(), "msg3");        
        ISVNInfo info31 = getInfo(file1);        
        ISVNInfo info32 = getInfo(file2);        
        
        // test
        String testName = getName();    
        String changePath1 = testName + "/" + testName + "_wc/file1";
        String changePath2 = testName + "/" + testName + "_wc/file2";
        ISVNLogMessage[] logsNb = 
            getNbClient().getLogMessages(
                getRepoUrl(),
                new String [] {changePath1, changePath2},
                new SVNRevision.Number(1),
                SVNRevision.HEAD, 
                false, 
                true);
        if (isCommandLine()) {
            assertEquals(3, logsNb.length);
        } else {
            // SvnClientAdapter completely ignores String[] argument, see AbstractClientAdapter.getLogMessages()
            assertFalse(3 == logsNb.length);
        }
        changePath1 = "/" + changePath1;
        changePath2 = "/" + changePath2;
        assertLogMessage(
            info11, 
            logsNb[logsNb.length - 3],
            "msg1",  
            new ISVNLogMessageChangePath[] { 
                new ChangePath('A', "/" + testName + "/" + testName + "_wc/folder1", null, null),
                new ChangePath('A', "/" + testName + "/" + testName + "_wc/folder2", null, null),
                new ChangePath('A', changePath1, null, null),                     
                new ChangePath('A', changePath2, null, null) 
            }
        );
        assertLogMessage(
            info12, 
            logsNb[logsNb.length - 3],
            "msg1",  
            new ISVNLogMessageChangePath[] { 
                new ChangePath('A', "/" + testName + "/" + testName + "_wc/folder1", null, null),
                new ChangePath('A', "/" + testName + "/" + testName + "_wc/folder2", null, null),
                new ChangePath('A', changePath1, null, null),                     
                new ChangePath('A', changePath2, null, null) 
            }
        );
        assertLogMessage(
            info21, 
            logsNb[logsNb.length - 2],
            "msg2",  
            new ISVNLogMessageChangePath[] { 
                new ChangePath('M', changePath1, null, null),
                new ChangePath('M', changePath2, null, null) 
            }
        );
        assertLogMessage(
            info22, 
            logsNb[logsNb.length - 2],
            "msg2",  
            new ISVNLogMessageChangePath[] { 
                new ChangePath('M', changePath1, null, null),
                new ChangePath('M', changePath2, null, null) 
            }
        );
        assertLogMessage(
            info31, 
            logsNb[logsNb.length - 1],
            "msg3",  new
            ISVNLogMessageChangePath[] { 
                new ChangePath('M', changePath1, null, null),
                new ChangePath('M', changePath2, null, null) 
            }
        );            
        assertLogMessage(
            info32, 
            logsNb[logsNb.length - 1],
            "msg3",  
            new ISVNLogMessageChangePath[] { 
                new ChangePath('M', changePath1, null, null),
                new ChangePath('M', changePath2, null, null) 
            }
        );            
    }
        
    private void log(Log log, boolean changePaths, boolean stopOnCopy) throws Exception {                                
        File file = createFile("file");
        add(file);
        write(file, "1");        
        commit(file, "msg1");
        ISVNInfo info1 = getInfo(file);        
        
        write(file, "2");
        commit(file, "msg2");
        ISVNInfo info2 = getInfo(file);
        
        write(file, "3");
        commit(file, "msg3");
        ISVNInfo info3 = getInfo(file);

        File copy = null;
        ISVNInfo info4 = null;
        ISVNInfo info5 = null;
        
        copy = new File(getWC(), "copy");
        copy(file, copy);
        write(copy, "4");
        commit(copy, "copy1");
        info4 = getInfo(copy);

        write(copy, "5");
        commit(copy, "copy2");
        info5 = getInfo(copy);            
                
        ISVNLogMessage[] logsNb = null;
        switch(log) {
            case file:
                logsNb = getNbClient().getLogMessages(copy, new SVNRevision.Number(0), SVNRevision.HEAD, stopOnCopy, changePaths);
                break;
            case url:
                logsNb = getNbClient().getLogMessages(getFileUrl(copy), null, new SVNRevision.Number(0), SVNRevision.HEAD, stopOnCopy, changePaths, 0);
                break;
            default:
                fail("no idea!");
        }
        
        // test
        assertEquals(stopOnCopy ? 2: 5, logsNb.length);         
        int s = stopOnCopy ? 0 : 3;
        if(changePaths) {
            String testName = getName();                        
            if(!stopOnCopy) {            
                assertLogMessage(info1, logsNb[0], "msg1",  new ISVNLogMessageChangePath[] { new ChangePath('A', "/" + testName + "/" + testName + "_wc/file", null, null) });
                assertLogMessage(info2, logsNb[1], "msg2",  new ISVNLogMessageChangePath[] { new ChangePath('M', "/" + testName + "/" + testName + "_wc/file", null, null) });
                assertLogMessage(info3, logsNb[2], "msg3",  new ISVNLogMessageChangePath[] { new ChangePath('M', "/" + testName + "/" + testName + "_wc/file", null, null) });            
            }                        
            assertLogMessage(info4, logsNb[0 + s], "copy1", new ISVNLogMessageChangePath[] { new ChangePath('A', "/" + testName + "/" + testName + "_wc/copy", info3.getRevision(), "/" + testName + "/" + testName + "_wc/file")});
            assertLogMessage(info5, logsNb[1 + s], "copy2", new ISVNLogMessageChangePath[] { new ChangePath('M', "/" + testName + "/" + testName + "_wc/copy", null, null)});

        } else {            
            if(!stopOnCopy) {            
                assertLogMessage(info1, logsNb[0], "msg1",  new ISVNLogMessageChangePath[] { });
                assertLogMessage(info2, logsNb[1], "msg2",  new ISVNLogMessageChangePath[] { });
                assertLogMessage(info3, logsNb[2], "msg3",  new ISVNLogMessageChangePath[] { });
            }
            assertLogMessage(info4, logsNb[0 + s], "copy1", new ISVNLogMessageChangePath[] { });
            assertLogMessage(info5, logsNb[1 + s], "copy2", new ISVNLogMessageChangePath[] { });            
        }                    
    }

    private void logLimit(Log log) throws Exception {                                
        File file = createFile("file");
        add(file);
        write(file, "1");        
        commit(file, "msg1");
        ISVNInfo info1 = getInfo(file);        
        
        write(file, "2");
        commit(file, "msg2");
        ISVNInfo info2 = getInfo(file);
        
        write(file, "3");
        commit(file, "msg3");

        ISVNLogMessage[] logsNb = null;
        switch(log) {
            case file:
                logsNb = getNbClient().getLogMessages(file, new SVNRevision.Number(0), SVNRevision.HEAD, false, true, 2);
                break;
            case url:
                logsNb = getNbClient().getLogMessages(getFileUrl(file), null, new SVNRevision.Number(0), SVNRevision.HEAD, false, true, 2);
                break;
            default:
                fail("no idea!");
        }
        
        // test
        assertEquals(2, logsNb.length);         
        String testName = getName();        
        assertLogMessage(info1, logsNb[0], "msg1",  new ISVNLogMessageChangePath[] { new ChangePath('A', "/" + testName + "/" + testName + "_wc/file", null, null) });
        assertLogMessage(info2, logsNb[1], "msg2",  new ISVNLogMessageChangePath[] { new ChangePath('M', "/" + testName + "/" + testName + "_wc/file", null, null) });
    }

    private void assertLogMessage(ISVNInfo info, ISVNLogMessage logsNb, String msg, ISVNLogMessageChangePath[] changePaths) {
        assertEquals(info.getLastCommitAuthor(), logsNb.getAuthor());
        assertEquals(info.getLastChangedDate().toString(), logsNb.getDate().toString());
        assertEquals(msg, logsNb.getMessage());
        assertEquals(info.getRevision(), logsNb.getRevision());
        assertChangePaths(changePaths, logsNb.getChangedPaths());
    }
    
    private void assertLogs(ISVNLogMessage[] logsRef, ISVNLogMessage[] logsNb) {
        assertEquals(logsRef.length, logsNb.length);
        for (int i = 0; i < logsNb.length; i++) {
            ISVNLogMessage lognb = logsNb[i];
            ISVNLogMessage logref = logsRef[i];            
            
            assertEquals(logref.getAuthor(), lognb.getAuthor());
            assertEquals(logref.getDate().toString(), lognb.getDate().toString());
            assertEquals(logref.getMessage(), lognb.getMessage());
            assertEquals(logref.getRevision(), lognb.getRevision());
            
            ISVNLogMessageChangePath[] pathsnb = lognb.getChangedPaths();
            ISVNLogMessageChangePath[] pathsref = lognb.getChangedPaths();
            assertChangePaths(pathsref, pathsnb);
        }
    }

    private void assertChangePaths(ISVNLogMessageChangePath[] pathsref, ISVNLogMessageChangePath[] pathsnb) {

        assertEquals(pathsref.length, pathsnb.length);

        Arrays.sort(pathsref, new ChangePathComparator());
        Arrays.sort(pathsnb, new ChangePathComparator());

        for (int j = 0; j < pathsref.length; j++) {
            ISVNLogMessageChangePath pathref = pathsref[j];
            ISVNLogMessageChangePath pathnb = pathsnb[j];

            assertEquals(pathref.getAction(), pathnb.getAction());
            assertEquals(pathref.getCopySrcPath(), pathnb.getCopySrcPath());
            assertEquals(pathref.getCopySrcRevision(), pathnb.getCopySrcRevision());
            assertEquals(pathref.getPath(), pathnb.getPath());
        }
    }    
    
    private class ChangePath implements ISVNLogMessageChangePath {
        private char action;
        private String path;
        private Number copySrcRevision;
        private String copySrcPath;
        public ChangePath(char action, String path, Number copySrcRevision, String copySrcPath) {
            this.action = action;
            this.path = path;
            this.copySrcRevision = copySrcRevision;
            this.copySrcPath = copySrcPath;
        }        
        public String getPath() {
            return path;
        }
        public Number getCopySrcRevision() {
            return copySrcRevision;
        }
        public String getCopySrcPath() {
            return copySrcPath;
        }
        public char getAction() {
            return action;
        }        
    }

    private class ChangePathComparator implements Comparator<ISVNLogMessageChangePath> {
        @Override
        public int compare(ISVNLogMessageChangePath o1, ISVNLogMessageChangePath o2) {
            return o1.getPath().compareTo(o2.getPath());
        }
    }

}
