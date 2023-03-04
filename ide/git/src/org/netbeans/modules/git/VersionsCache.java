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

package org.netbeans.modules.git;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.util.Utils;

/**
 *
 * @author ondra
 */
public class VersionsCache {

    private static VersionsCache instance;

    /** Creates a new instance of VersionsCache */
    private VersionsCache() {
    }

    public static synchronized VersionsCache getInstance() {
        if (instance == null) {
            instance = new VersionsCache();
        }
        return instance;
    }

    /**
     * Loads the file in specified revision.
     *
     * @return null if the file does not exist in given revision
     */
    public File getFileRevision(File base, String revision, ProgressMonitor pm) throws IOException {
        if("-1".equals(revision)) return null; // NOI18N

        File repository = Git.getInstance().getRepositoryRoot(base);
        if (GitUtils.CURRENT.equals(revision)) {
            return base.exists() ? base : null;
        } else {
            File tempFile = new File(Utils.getTempFolder(), "nb-git-" + base.getName()); //NOI18N
            tempFile.deleteOnExit();
            GitClient client = null;
            try {
                client = Git.getInstance().getClient(repository);
                boolean result;
                FileOutputStream fos = new FileOutputStream(tempFile);
                try {
                    if (GitUtils.INDEX.equals(revision)) {
                        result = client.catIndexEntry(base, 0, fos, pm);
                    } else {
                        result = client.catFile(base, revision, fos, pm);
                    }
                } finally {
                    fos.close();
                }
                if (!result) {
                    tempFile.delete();
                    tempFile = null;
                }
            } catch (java.io.FileNotFoundException ex) {
                tempFile.delete();
                tempFile = null;
            } catch (GitException.MissingObjectException ex) {
                tempFile.delete();
                tempFile = null;
            } catch (GitException ex) {
                throw new IOException(ex);
            } finally {
                if (client != null) {
                    client.release();
                }
            }
            return tempFile;
        }
    }
}
