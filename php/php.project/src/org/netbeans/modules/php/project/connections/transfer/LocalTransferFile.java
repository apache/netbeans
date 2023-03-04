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

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.project.connections.RemoteClientImplementation;

/**
 * {@link TransferFile Transfer file} implementation for {@link File local file}.
 */
public final class LocalTransferFile extends TransferFile {

    private static final Logger LOGGER = Logger.getLogger(LocalTransferFile.class.getName());

    // considered to be thread-safe, see Javadoc and sources
    private final File file;
    private final boolean forceDirectory;

    private volatile Boolean isFile = null;
    private volatile Boolean isDirectory = null;


    LocalTransferFile(RemoteClientImplementation remoteClient, File file, TransferFile parent, boolean forceDirectory) {
        super(remoteClient, parent);
        this.file = file;
        this.forceDirectory = forceDirectory;

        if (file == null) {
            throw new NullPointerException("Local file cannot be null");
        }
        String baseLocalDirectory = remoteClient.getBaseLocalDirectory();
        if (!file.getAbsolutePath().startsWith(baseLocalDirectory)) {
            throw new IllegalArgumentException("File '" + file.getAbsolutePath() + "' must be underneath base directory '" + baseLocalDirectory + "'");
        }
        if (forceDirectory && file.isFile()) {
            throw new IllegalArgumentException("File '" + file.getAbsolutePath() + "' can't be forced as a directory since it is a file");
        }
        // get attributes so we know them even if the file is later deleted
        getSize();
        getTimestamp();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getRemotePath() {
        String absolutePath = file.getAbsolutePath();
        if (absolutePath.equals(remoteClient.getBaseLocalDirectory())) {
            return REMOTE_PROJECT_ROOT;
        }
        // remove file-separator from the beginning of the relative path
        String remotePath = absolutePath.substring(remoteClient.getBaseLocalDirectory().length() + File.separator.length());
        if (File.separator.equals(REMOTE_PATH_SEPARATOR)) {
            return remotePath;
        }
        return remotePath.replace(File.separator, REMOTE_PATH_SEPARATOR);
    }

    @Override
    protected long getSizeImpl() {
        if (isFile()) {
            return file.length();
        }
        return 0L;
    }

    @Override
    public boolean isDirectory() {
        if (isDirectory != null) {
            return isDirectory;
        }
        if (file.exists()) {
            isDirectory = file.isDirectory();
            if (forceDirectory && !isDirectory && file.isFile()) {
                assert false : "File forced as directory but is regular existing file";
            }
            return isDirectory;
        }
        isDirectory = forceDirectory;
        return isDirectory;
    }

    @Override
    public boolean isFile() {
        if (isFile != null) {
            return isFile;
        }
        if (file.exists()) {
            isFile = file.isFile();
            if (isFile && forceDirectory) {
                assert false : "File forced as directory but is regular existing file";
            }
            return isFile;
        }
        isFile = !forceDirectory;
        return isFile;
    }

    @Override
    public boolean isLink() {
        return FileUtils.isDirectoryLink(file);
    }

    @Override
    protected long getTimestampImpl() {
        return TimeUnit.SECONDS.convert(file.lastModified(), TimeUnit.MILLISECONDS);
    }

}
