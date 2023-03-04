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

package org.netbeans.modules.php.project.connections.sync;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectSettings;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.runconfigs.RunConfigRemote;

public final class TimeStamps {

    private static final Logger LOGGER = Logger.getLogger(TimeStamps.class.getName());


    private final Storage storage;
    private final String pathPrefix;


    public TimeStamps(final PhpProject project) {
        // XXX not perfect since remote connection hint already contains project upload directory
        this(new StorageImpl(project), RunConfigRemote.forProject(project).getRemoteConnectionHint());
        assert project != null;
    }

    TimeStamps(Storage storage, String pathPrefix) {
        assert storage != null;
        assert pathPrefix != null;
        this.storage = storage;
        this.pathPrefix = pathPrefix;
    }

    /**
     * Get timestamp of remote synchronization for the given remote file. If there is no
     * timestamp for the given file, timestamp of the parent(s) file is returned, or -1
     * if none is found.
     * @return timestamp <b>in seconds</b> of the last synchronization of the given file
     *         and the current remote configuration or {@code -1} if not found
     */
    public long getSyncTimestamp(TransferFile transferFile) {
        assert transferFile != null;
        final String path = getFullPath(transferFile.getRemoteAbsolutePath());
        LinkedList<String> paths = new LinkedList<>(StringUtils.explode(path, TransferFile.REMOTE_PATH_SEPARATOR));
        assert !paths.isEmpty() : path;
        if (transferFile.isFile()) {
            LOGGER.log(Level.FINE, "File {0} is not a directory, changing to its parent", path);
            paths.removeLast();
        }
        for (;;) {
            String tmp = StringUtils.implode(paths, TransferFile.REMOTE_PATH_SEPARATOR);
            if (pathPrefix.length() > tmp.length()) {
                LOGGER.fine("Path prefix length reached, exiting loop");
                break;
            }
            LOGGER.log(Level.FINE, "Getting sync timestamp of {0}", tmp);
            long timestamp = storage.getLong(tmp);
            if (timestamp != -1) {
                LOGGER.log(Level.FINE, "Sync timestamp of {0} is {1}", new Object[] {tmp, timestamp});
                return timestamp;
            }
            paths.removeLast();
        }
        LOGGER.log(Level.FINE, "Sync timestamp of {0} not found", path);
        return -1;
    }

    /**
     * Set timestamp of remote synchronization to current time for the given file. Timestamp is saved only for directories.
     * @param transferFile transfer file of the synchronization
     * @see #setSyncTimestamp(TransferFile, long)
     */
    public void setSyncTimestamp(TransferFile transferFile) {
        setSyncTimestamp(transferFile, TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    /**
     * Set timestamp of remote synchronization for the given file. Timestamp is saved only for directories.
     * @param transferFile transfer file of the synchronization
     * @param timestamp timestamp to be saved, <b>in seconds</b>
     * @see #setSyncTimestamp(TransferFile)
     */
    void setSyncTimestamp(TransferFile transferFile, long timestamp) {
        assert transferFile != null;
        if (!transferFile.isDirectory()) {
            LOGGER.log(Level.FINE, "Ignoring non-directory {0}", transferFile.getRemoteAbsolutePath());
            return;
        }
        String path = getFullPath(transferFile.getRemoteAbsolutePath());
        LOGGER.log(Level.FINE, "Setting sync timestamp of {0} to {1}", new Object[] {path, timestamp});
        storage.setLong(path, timestamp);
    }

    String getFullPath(String path) {
        return pathPrefix + path;
    }

    //~ Inner classes

    interface Storage {
        long getLong(String key);
        void setLong(String key, long value);
    }

    private static final class StorageImpl implements Storage {

        private final Project project;


        public StorageImpl(Project project) {
            assert project != null;
            this.project = project;
        }

        @Override
        public long getLong(String key) {
            return ProjectSettings.getSyncTimestamp(project, getHash(key));
        }

        @Override
        public void setLong(String key, long value) {
            ProjectSettings.setSyncTimestamp(project, getHash(key), value);
        }

        private String getHash(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5"); // NOI18N
                byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : hash) {
                    sb.append(Integer.toHexString((int) (b & 0xff)));
                }
                String result = sb.toString();
                LOGGER.log(Level.FINE, "Hashing \"{0}\" to \"{1}\"", new Object[] {input, result});
                return result;
            } catch (NoSuchAlgorithmException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            LOGGER.log(Level.FINE, "No hashing for \"{0}\"", input);
            return input;
        }

    }

}
