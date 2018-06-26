/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
