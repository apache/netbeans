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

package org.netbeans.modules.mercurial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public abstract class AbstractHgTestCase extends NbTestCase {

    protected static final OutputLogger NULL_LOGGER = Mercurial.getInstance().getLogger(null);

    public FileStatusCache getCache() {
        return cache;
    }
    private FileStatusCache cache;
//    private File workDir;
//    private File wc;

    public AbstractHgTestCase(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    protected File getWorkTreeDir () throws IOException {
        return new File(getWorkDir(), "wc");
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        clearWorkDir();
        
        FileUtil.refreshFor(getWorkTreeDir());
        Logger.getLogger("").addHandler(versionCheckBlocker);
        
        try {
            Mercurial.getInstance().asyncInit();
            for (int i = 0; i < 20; i++) {                
                Thread.sleep(200);
                if(versionCheckBlocker.versionChecked) break;
            }
            if(!versionCheckBlocker.versionChecked) throw new TimeoutException("hg version check timedout!");
        } finally {
            Logger.getLogger("").removeHandler(versionCheckBlocker);    
        }
        
//        workDir = new File(System.getProperty("work.dir")); 
//        FileUtil.refreshFor(workDir);          
        try {
            assertTrue(getWorkTreeDir().mkdirs());
            HgCommand.doCreate(getWorkTreeDir(), null);
            new File(getWorkTreeDir(), "empty").createNewFile();
        } catch (IOException iOException) {
            throw iOException;
        } catch (HgException hgException) {
//            if(!hgException.getMessage().contains("already exists")) {
//                throw hgException;
//            }
        }
//        wc = new File(workDir, getName() + "_wc");        
        cache = Mercurial.getInstance().getFileStatusCache();
    }

//    protected File getWC() {
//        return wc;
//    }    
    
    
    protected void commit(File... files) throws HgException, IOException {
        commitIntoRepository(getWorkTreeDir(), files);
    }

    protected void commitIntoRepository (File repository, File... files) throws HgException, IOException {

        List<File> filesToAdd = new ArrayList<File>();
        FileInformation status;
        for (File file : files) {
            if(findStatus(HgCommand.getStatus(repository, Collections.singletonList(file), null, null),
                    FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
                filesToAdd.add(file);
            }
        }

        HgCommand.doAdd(repository, filesToAdd, null);
        List<File> filesToCommit = new ArrayList<File>();
        for (File file : files) {
            if(file.isFile()) {
                filesToCommit.add(file);
            }
        }

        HgCommand.doCommit(repository, filesToCommit, "commit", null);
        for (File file : filesToCommit) {
            assertStatus(file, FileInformation.STATUS_VERSIONED_UPTODATE);
        }
    }

    protected File clone(File file) throws HgException, IOException {
        String path = file.getAbsolutePath() + "_cloned";
        HgCommand.doClone(getWorkTreeDir(), new File(path), null);
        return new File(path);
    }
    
    protected  void assertStatus(File f, int status) throws HgException, IOException {
        FileInformation s = HgCommand.getStatus(getWorkTreeDir(), Collections.singletonList(f), null, null).get(f);
        if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
            assertEquals(s, null);
        } else {
            assertEquals(status, s.getStatus());
        }
    }        
    
    protected void assertCacheStatus(File f, int status) throws HgException, IOException {
        assertEquals(status, cache.getStatus(f).getStatus());
    }

    protected File createFolder(String name) throws IOException {
        FileObject wd = FileUtil.toFileObject(getWorkTreeDir());
        FileObject folder = wd.createFolder(name);        
        return FileUtil.toFile(folder);
    }
    
    protected File createFolder(File parent, String name) throws IOException {
        FileObject parentFO = FileUtil.toFileObject(parent);
        FileObject folder = parentFO.createFolder(name);                
        return FileUtil.toFile(folder);
    }
    
    protected File createFile(File parent, String name) throws IOException {
        FileObject parentFO = FileUtil.toFileObject(parent);
        FileObject fo = parentFO.createData(name);
        return FileUtil.toFile(fo);
    }
    
    protected File createFile(String name) throws IOException {
        FileObject wd = FileUtil.toFileObject(getWorkTreeDir());
        FileObject fo = wd.createData(name);
        return FileUtil.toFile(fo);
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

    protected String read (File file) throws IOException {
        BufferedReader r = null;
        try {
            StringBuilder sb = new StringBuilder();
            r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(line);
            }
            return sb.toString();
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    private boolean findStatus(Map<File, FileInformation> statuses, int status) {
        for (Map.Entry<File, FileInformation> e : statuses.entrySet()) {
            if (e.getValue().getStatus() == status) {
                return true;
            }
        }
        return false;
    }
    
    private static class VersionCheckBlocker extends Handler {
        boolean versionChecked = false;
        public void publish(LogRecord record) {
            if(record.getMessage().indexOf("version: ") > -1) {
                versionChecked = true;                    
            }
        }
        public void flush() { }
        public void close() throws SecurityException { }        
    };
    private static VersionCheckBlocker versionCheckBlocker = new VersionCheckBlocker();
}
