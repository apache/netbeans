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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.netbeans.modules.php.project.connections.RemoteException;

/**
 * The particular implementation of the remote client (e.g. FTP, SFTP).
 * @author Tomas Mysik
 */
public interface RemoteClient {

    /**
     * Connect to a remote server.
     * @throws RemoteException if any unexpected error occurs.
     */
    void connect() throws RemoteException;

    /**
     * Possibly disconnect from a remote server. Typically do nothing
     * if keep-alive is used and <tt>force</tt> is {@code false}.
     * @param force if {@code true}, always disconnect from the server
     * @throws RemoteException if any unexpected error occurs.
     */
    void disconnect(boolean force) throws RemoteException;

    /**
     * Change working directory to the given file path.
     * @param pathname file path to change directory to.
     * @return <code>true</code> if the directory change was successful, <code>false</code> otherwise.
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean changeWorkingDirectory(String pathname) throws RemoteException;

    /**
     * Get the file path of the current working directory.
     * @return the file path of the current working directory.
     * @throws RemoteException if any unexpected error occurs.
     */
    String printWorkingDirectory() throws RemoteException;

    /**
     * Create a directory for the given file path.
     * <p>
     * Note that a remote server can support only creating directory in the current working directory.
     * @param pathname file path of the directory to be created.
     * @return <code>true</code> if the directory creation was successful, <code>false</code> otherwise.
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean makeDirectory(String pathname) throws RemoteException;

    /**
     * Get the last message from a remote server (from the last operation).
     * @return the last message from a remote server or <code>null</code> if the server does not support it.
     */
    String getReplyString();

    /**
     * Get the last negative message from a remote server (from the last operation).
     * @return the last negative message from a remote server or <code>null</code> if the server does not support it.
     */
    String getNegativeReplyString();

    /**
     * Return <code>true</code> if the remote client is connected, <code>false</code> otherwise.
     * @return <code>true</code> if the remote client is connected, <code>false</code> otherwise.
     */
    boolean isConnected();

    /**
     * Return <code>true</code> if the remote file exists, <code>false</code> otherwise.
     * @return <code>true</code> if the remote file exists, <code>false</code> otherwise.
     * @param parent path of the parent folder.
     * @param name name of the file.
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean exists(String parent, String name) throws RemoteException;

    /**
     * Store a file on a remote server.
     * <p>
     * <b>Avoid closing of the given {@link InputStream input stream}!</b>
     * @param remote the name of the file to be stored on a server.
     * @param local {@link InputStream input stream} of the local file to be stored on a server.
     * @return <code>true</code> if the file was successfully uploaded, <code>false</code> otherwise.
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean storeFile(String remote, InputStream local) throws RemoteException;

    /**
     * Retrieve a file from a remote server.
     * <p>
     * <b>Avoid closing of the given {@link OutputStream output stream}!</b>
     * @param remote the name of the file to be retrieved from a server.
     * @param local {@link OutputStream output stream} of the local file to be retrieved from a server.
     * @return <code>true</code> if the file was successfully downloaded, <code>false</code> otherwise.
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean retrieveFile(String remote, OutputStream local) throws RemoteException;

    /**
     * Delete file from a remote server.
     * @param pathname file path of the file to be deleted.
     * @return <code>true</code> if the file deletion was successful, <code>false</code> otherwise.
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean deleteFile(String pathname) throws RemoteException;

    /**
     * Delete directory from a remote server.
     * @param pathname path of the directory to be deleted.
     * @return <code>true</code> if the directory deletion was successful, <code>false</code> otherwise (typically for non-empty folders).
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean deleteDirectory(String pathname) throws RemoteException;

    /**
     * Rename the file on a remote server.
     * @param from the old name.
     * @param to the new name.
     * @return <code>true</code> if the file renaming was successful, <code>false</code> otherwise.
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean rename(String from, String to) throws RemoteException;

    /**
     * Get permissions of the given file. The behaviour for directories is not defined.
     * @param path the file path (relative or absolute).
     * @return permissions (usually number with 3 ciphers, UNIX-like (4 - read, 2 - write, 1 - execute; &lt;user&gt;&lt;group&gt;&lt;other&gt;)),
     *         <tt>-1</tt> if unknown (e.g. file not exists).
     * @throws RemoteException if any unexpected error occurs.
     * @see #setPermissions(int, java.lang.String)
     */
    int getPermissions(String path) throws RemoteException;

    /**
     * Set permissions of the given file. The behaviour for directories is not defined.
     * @param permissions permissions (usually number with 3 ciphers, UNIX-like (4 - read, 2 - write, 1 - execute; &lt;user&gt;&lt;group&gt;&lt;other&gt;)).
     * @param path the file path (relative or absolute).
     * @return <code>true</code> if the permissions was set, <code>false</code> otherwise.
     * @throws RemoteException if any unexpected error occurs.
     * @see #getPermissions(java.lang.String)
     */
    boolean setPermissions(int permissions, String path) throws RemoteException;

    /**
     * Get the list of the {@link RemoteFile files} (including hidden!) of the current directory.
     * @return the list of the {@link RemoteFile files} of the current directory, never <code>null</code>.
     * @throws RemoteException if any unexpected error occurs.
     */
    List<RemoteFile> listFiles() throws RemoteException;

    /**
     * Get {@link RemoteFile} if the the given path is file or <code>null</code>.
     * <p>
     * If the file is symbolic link then it is simply returned since cannot be decided
     * whether it is a file or a directory.
     * @param path <b>absolute</b> file path
     * @return {@link RemoteFile} for the path if the path is file (or symbolic link) or <code>null</code>
     * @throws RemoteException if any unexpected error occurs.
     */
    RemoteFile listFile(String absolutePath) throws RemoteException;

}
