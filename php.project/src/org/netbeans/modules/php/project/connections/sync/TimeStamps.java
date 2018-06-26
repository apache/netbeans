/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections.sync;

import java.io.UnsupportedEncodingException;
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
                byte[] hash = md.digest(input.getBytes("UTF-8")); // NOI18N
                StringBuilder sb = new StringBuilder();
                for (byte b : hash) {
                    sb.append(Integer.toHexString((int) (b & 0xff)));
                }
                String result = sb.toString();
                LOGGER.log(Level.FINE, "Hashing \"{0}\" to \"{1}\"", new Object[] {input, result});
                return result;
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            LOGGER.log(Level.FINE, "No hashing for \"{0}\"", input);
            return input;
        }

    }

}
