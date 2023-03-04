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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientFactory;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.utils.TestUtilities;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author pvcs
 */
public class TestKit {

    /*
     * method compares arrays of objects. Returns -1 if they differs else return the count of equal items.
     *
     */
    public static int compareThem(Object[] expected, Object[] actual, boolean sorted) {
        int result = 0;
        if (expected == null || actual == null)
            return -1;
        if (sorted) {
            if (expected.length != actual.length) {
                return -1;
            }
            for (int i = 0; i < expected.length; i++) {
                if (((String) expected[i]).equals((String) actual[i])) {
                    result++;
                } else {
                    return -1;
                }
            }
        } else {
            if (expected.length > actual.length) {
                return -1;
            }
            Arrays.sort(expected);
            Arrays.sort(actual);
            boolean found = false;
            for (int i = 0; i < expected.length; i++) {
                if (((String) expected[i]).equals((String) actual[i])) {
                    result++;
                } else {
                    return -1;
                }
            }
            return result;
        }
        return result;
    }

    public static void initRepo(File repoDir, File path) throws MalformedURLException, IOException, InterruptedException, SVNClientException {                
        ISVNDirEntry[] list;
        SVNUrl repoUrl;
        if(!repoDir.exists()) {
            repoDir.mkdirs();            
            String[] cmd = {"svnadmin", "create", repoDir.getAbsolutePath()};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();   
        } else {
            repoUrl = new SVNUrl(TestUtilities.formatFileURL(repoDir));
            list = getClient().getList(repoUrl, SVNRevision.HEAD, false);            
            if(list != null) {
                for (ISVNDirEntry entry : list) {
                    if(entry.getPath().equals(path.getName())) {
                        try {
                            getClient().remove(new SVNUrl[] {repoUrl.appendPath(path.getName())}, "remove");
                        } catch (SVNClientException e) {
                            if(e.getMessage().indexOf("does not exist") < 0) {
                                throw e;
                            }
                        }
                    }
                }
            }
        }
    }

    public static SvnClient getClient() throws SVNClientException {        
        return SvnClientFactory.getInstance().createSvnClient();
    }

    public static void svnimport(File repoDir, File wc) throws SVNClientException, MalformedURLException {
        ISVNClientAdapter client = getClient();
        String url = TestUtilities.formatFileURL(repoDir);
        SVNUrl repoUrl = new SVNUrl(url);
        client.mkdir(repoUrl.appendPath(wc.getName()), "msg");        
        client.checkout(repoUrl.appendPath(wc.getName()), wc, SVNRevision.HEAD, true);        
        File[] files = wc.listFiles();
        if(files != null) {
            for (File file : files) {
                if(!isMetadata(file)) {
                    client.addFile(file);
                }                
            }
            client.commit(new File[] {wc}, "commit", true);                    
        }
        Subversion.getInstance().versionedFilesChanged();
    }        

    public static void mkdirs(File repoDir, String folder) throws SVNClientException, MalformedURLException {
        ISVNClientAdapter client = getClient();
        String url = TestUtilities.formatFileURL(repoDir);
        SVNUrl repoUrl = new SVNUrl(url);
        repoUrl = repoUrl.appendPath(folder);
        client.mkdir(repoUrl, true, "creating folder " + folder);
    }

    public static void commit(File folder) throws SVNClientException {
        add(folder);
        getClient().commit(new File[]{ folder }, "commit", true);
    }

    public static void delete(File file) throws SVNClientException {
        getClient().remove(new File[]{ file }, true);
    }
    
    public static void add(File file) throws SVNClientException {
        ISVNStatus status = getSVNStatus(file);
        if(status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
            getClient().addFile(file);
        }
        if(file.isFile()) {
            return; 
        }
        File[] files = file.listFiles();
        if(files != null) {
            for (File f : files) {
                if(!isMetadata(f)) {
                    add(f);
                }
            }            
        }
    }

    public static void write(File file, String str) throws IOException {
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

    public static ISVNStatus getSVNStatus(File file) throws SVNClientException {            
        return SvnUtils.getSingleStatus(getClient(), file);
    }

    public static ISVNInfo getSVNInfo(String url) throws SVNClientException, MalformedURLException {
        return getClient().getInfo(new SVNUrl(url));
    }

    
    static boolean isMetadata(File file) {     
        return SvnUtils.isAdministrative(file) || SvnUtils.isPartOfSubversionMetadata(file);
    }
}
