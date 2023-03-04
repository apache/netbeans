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
package org.netbeans.modules.php.project.connections.transfer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.project.connections.RemoteClientImplementation;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.spi.RemoteFile;

/**
 * {@link TransferFile Transfer file} implementation for {@link RemoteFile remote file}.
 */
final class RemoteTransferFile extends TransferFile {

    private static final Logger LOGGER  = Logger.getLogger(RemoteTransferFile.class.getName());

    // @GuardedBy(file)
    private final RemoteFile file;



    public RemoteTransferFile(RemoteClientImplementation remoteClient, RemoteFile file, TransferFile parent) {
        super(remoteClient, parent);
        this.file = file;

        if (file == null) {
            throw new NullPointerException("Remote file cannot be null");
        }
        String parentDirectory = getParentDirectory();
        if (!parentDirectory.startsWith(REMOTE_PATH_SEPARATOR)) {
            throw new IllegalArgumentException("Parent directory '" + parentDirectory + "' must start with '" + REMOTE_PATH_SEPARATOR + "'");
        }
        checkParentDirectory(remoteClient.getBaseRemoteDirectory(), parentDirectory);
        if (LOGGER.isLoggable(Level.FINE)) {
            // #204874 (non-standard ssh server?)
            LOGGER.log(Level.FINE, "Absolute remote path \"{0}\" -> remote path \"{1}\" (base directory \"{2}\")",
                    new Object[] {getAbsolutePath(), getRemotePath(), remoteClient.getBaseRemoteDirectory()});
        }
    }

    @Override
    public String getName() {
        synchronized (file) {
            return file.getName();
        }
    }

    @Override
    public String getRemotePath() {
        String absolutePath = getAbsolutePath();
        if (absolutePath.equals(remoteClient.getBaseRemoteDirectory())) {
            return REMOTE_PROJECT_ROOT;
        }
        String relativePath = absolutePath.substring(remoteClient.getBaseRemoteDirectory().length());
        if (relativePath.startsWith(REMOTE_PATH_SEPARATOR)) {
            // happens for base directory different from "/", see #205399
            relativePath = relativePath.substring(REMOTE_PATH_SEPARATOR.length());
        }
        return relativePath;
    }

    @Override
    protected long getSizeImpl() {
        if (isFile()) {
            synchronized (file) {
                return file.getSize();
            }
        }
        return 0L;
    }

    @Override
    public boolean isDirectory() {
        synchronized (file) {
            return file.isDirectory();
        }
    }

    @Override
    public boolean isFile() {
        synchronized (file) {
            return file.isFile();
        }
    }

    @Override
    public boolean isLink() {
        synchronized (file) {
            return file.isLink();
        }
    }

    @Override
    protected long getTimestampImpl() {
        synchronized (file) {
            return file.getTimestamp();
        }
    }

    // #204874 - some servers return ending '/' for directories => remove it
    private String getParentDirectory() {
        synchronized (file) {
            return RemoteUtils.sanitizeDirectoryPath(file.getParentDirectory());
        }
    }

    String getAbsolutePath() {
        synchronized (file) {
            String parentDirectory = getParentDirectory();
            if (!parentDirectory.endsWith(REMOTE_PATH_SEPARATOR)) {
                // does not apply for base directory "/", see #205399
                parentDirectory = parentDirectory + REMOTE_PATH_SEPARATOR;
            }
            return parentDirectory + getName();
        }
    }

    static void checkParentDirectory(String baseRemoteDirectory, String parentDirectory) {
        boolean root = baseRemoteDirectory.equals(REMOTE_PATH_SEPARATOR);
        if ((root && !parentDirectory.startsWith(REMOTE_PATH_SEPARATOR))
                || (!root && !(parentDirectory + REMOTE_PATH_SEPARATOR).startsWith(baseRemoteDirectory + REMOTE_PATH_SEPARATOR))) {
            throw new IllegalArgumentException("Parent directory '" + parentDirectory + "' must be underneath base directory '" + baseRemoteDirectory + "'");
        }
    }

}
