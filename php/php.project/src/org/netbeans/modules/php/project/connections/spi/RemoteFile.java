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

package org.netbeans.modules.php.project.connections.spi;

/**
 * Representation of a remote (FTP, SFTP etc.) file.
 * @author Tomas Mysik
 */
public interface RemoteFile {

    /**
     * Return the name of the file.
     * @return the file name.
     */
    String getName();

    /**
     * Return path of parent directory.
     * @return path of parent directory.
     */
    String getParentDirectory();

    /**
     * Return <code>true</code> if the remote file is directory, <code>false</code> otherwise.
     * @return <code>true</code> if the remote file is directory, <code>false</code> otherwise.
     * @see #isFile()
     * @see #isLink()
     */
    boolean isDirectory();

    /**
     * Return <code>true</code> if the remote file is file, <code>false</code> otherwise.
     * <p>
     * In most cases it is just opposite to {@link #isDirectory()}.
     * @return <code>true</code> if the remote file is file, <code>false</code> otherwise
     * @see #isDirectory()
     * @see #isLink()
     */
    boolean isFile();

    /**
     * Return <code>true</code> if the remote file is a symbolic link, <code>false</code> otherwise.
     * <p>
     * Symbolic links are not downloaded or uploaded, they are simply ignored.
     * @return <code>true</code> if the remote file is a symbolic link, <code>false</code> otherwise
     * @see #isDirectory()
     * @see #isFile()
     */
    boolean isLink();

    /**
     * Return the file size (in bytes) of the remote file.
     * <p>
     * Not guaranteed what is returned for directories.
     * @return the file size (in bytes) of the remote file.
     */
    long getSize();

    /**
     * Return the timestamp (in <b>seconds</b>) of the remote file last modification or <code>-1</code> if not known.
     * <p>
     * Not guaranteed what is returned for directories.
     * @return the timestamp (in <b>seconds</b>) of the remote file last modification or <code>-1</code> if not known.
     */
    long getTimestamp();

}
