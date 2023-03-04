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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.historystore.Storage;
import org.netbeans.modules.versioning.historystore.StorageManager;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.test.MockLookup;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author ondra
 */
public class VersionsCacheTest extends AbstractSvnTestCase {

    private File workdir;

    public VersionsCacheTest (String arg0) throws Exception {
        super(arg0);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances();
        // create
        workdir = getWC();
    }
    
    public void testCache () throws Exception {
        File folder = new File(workdir, "folder");
        folder.mkdirs();
        commit(workdir);
        File file = new File(folder, "file");
        List<File> revisions = prepareVersions(file);
        testContents(revisions, file, false);
    }

    private List<File> prepareVersions (File file) throws Exception {
        List<File> revisionList = new LinkedList<File>();
        File dataDir = new File(getDataDir(), "versionscache");
        File[] revisions = dataDir.listFiles();
        for (File rev : revisions) {
            if (rev.isFile()) {
                revisionList.add(0, rev);
                Utils.copyStreamsCloseAll(new FileOutputStream(file), new FileInputStream(rev));
                commit(file);
            }
        }
        return revisionList;
    }

    private void testContents (List<File> revisions, File file, boolean cacheFilled) throws Exception {
        ISVNClientAdapter client = getFullWorkingClient();
        long lastRev = client.getInfo(file).getLastChangedRevision().getNumber();
        VersionsCache cache = VersionsCache.getInstance();
        SVNUrl repoUrl = SvnUtils.getRepositoryRootUrl(file);
        SVNUrl resourceUrl = SvnUtils.getRepositoryUrl(file);
        Storage storage = StorageManager.getInstance().getStorage(repoUrl.toString());
        for (File golden : revisions) {
            File content;
            if (!cacheFilled) {
                content = storage.getContent(repoUrl.toString(), file.getName(), String.valueOf(lastRev));
                assertEquals(0, content.length());
            }
            content = cache.getFileRevision(repoUrl, resourceUrl, String.valueOf(lastRev), file.getName());
            assertFile(content, golden, null);
            content = cache.getFileRevision(repoUrl, resourceUrl, String.valueOf(lastRev), String.valueOf(lastRev), file.getName());
            assertFile(content, golden, null);
            content = storage.getContent(resourceUrl.toString() + "@" + lastRev, file.getName(), String.valueOf(lastRev));
            assertFile(content, golden, null);
            --lastRev;
        }
    }
}
