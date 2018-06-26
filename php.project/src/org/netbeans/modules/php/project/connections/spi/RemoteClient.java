/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
