/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.discovery.projectimport;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public final class DoubleFile {
    private final File localFile;
    private final String remotePath;
    private final ExecutionEnvironment remoteEE;
    private boolean validLocal = true;
    private boolean validRemote = true;

    private DoubleFile(File localPath, String remotePath, ExecutionEnvironment remoteEE) {
        this.localFile = localPath;
        this.remotePath = remotePath;
        this.remoteEE = remoteEE;
    }

    public static DoubleFile createFile(String prefix, FSPath path) {
        ExecutionEnvironment ee = FileSystemProvider.getExecutionEnvironment(path.getFileSystem());
        if (ee.isLocal()) {
            return new DoubleFile(new File(path.getPath()), null, ee);
        } else {
            File local;
            try {
                local = File.createTempFile(prefix, ".log"); // NOI18N
                local.deleteOnExit();
            } catch (IOException ex) {
                return null;
            }
            return new DoubleFile(local, path.getPath(), ee);
        }
    }

    public static DoubleFile createFile(File path, ExecutionEnvironment ee) {
        File local = path;
        local.deleteOnExit();
        String remotePath = null;
        if (ee.isRemote()) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(ee);
                if (ee.isRemote()) {
                    remotePath = hostInfo.getTempDir() + "/" + local.getName(); // NOI18N
                }
            } catch (IOException | ConnectionManager.CancellationException ex) {
            }
        }
        return new DoubleFile(local, remotePath, ee);
    }

    public static DoubleFile createTmpFile(String prefix, ExecutionEnvironment ee) {
        File local;
        try {
            local = File.createTempFile(prefix, ".log"); // NOI18N
            local.deleteOnExit();
        } catch (IOException ex) {
            return null;
        }
        local.deleteOnExit();
        String remotePath = null;
        if (ee.isRemote()) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(ee);
                if (ee.isRemote()) {
                    remotePath = hostInfo.getTempDir() + "/" + local.getName(); // NOI18N
                }
            } catch (IOException | ConnectionManager.CancellationException ex) {
            }
        }
        return new DoubleFile(local, remotePath, ee);
    }

    public boolean isValidRemote() {
        return validRemote;
    }

    public FileObject getLocalFileObject() {
        return FileUtil.toFileObject(localFile);
    }

    public File getLocalFile() {
        return localFile;
    }

    public boolean existLocalFile() {
        return localFile.exists() && validLocal;
    }

    public String getLocalPath() {
        return localFile.getAbsolutePath();
    }

    public String getRemotePath() {
        return remotePath;
    }

    void upload() {
        try {
            Future<CommonTasksSupport.UploadStatus> task = CommonTasksSupport.uploadFile(localFile, remoteEE, remotePath, 0555);
            if (ImportProject.TRACE) {
                ImportProject.logger.log(Level.INFO, "#upload file {0}->{1}", new Object[]{localFile.getAbsolutePath(), remotePath}); // NOI18N
            }
            /*int rc =*/
            task.get();
        } catch (Throwable ex) {
            ImportProject.logger.log(Level.INFO, "Cannot upload file {0}->{1}. Exception {2}", new Object[]{localFile.getAbsolutePath(), remotePath, ex.getMessage()}); // NOI18N
            validRemote = false;
        }
    }

    void download() {
        try {
            if (HostInfoUtils.fileExists(remoteEE, remotePath)) {
                Future<Integer> task = CommonTasksSupport.downloadFile(remotePath, remoteEE, localFile.getAbsolutePath(), null);
                if (ImportProject.TRACE) {
                    ImportProject.logger.log(Level.INFO, "#download file {0}->{1}", new Object[]{remotePath, localFile.getAbsolutePath()}); // NOI18N
                }
                /*int rc =*/
                task.get();
            }
        } catch (Throwable ex) {
            ImportProject.logger.log(Level.INFO, "Cannot download file {0}->{1}. Exception {2}", new Object[]{remotePath, localFile.getAbsolutePath(), ex.getMessage()}); // NOI18N
            validLocal = false;
        }
    }
}
