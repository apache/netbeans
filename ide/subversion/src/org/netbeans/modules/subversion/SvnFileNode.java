/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.subversion;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * Represents real or virtual (non-local) file.
 *
 * @author Maros Sandor
 */
public class SvnFileNode {

    /**
     * Careful, may not be normalized
     * @return 
     */
    private final File file;
    private final File normalizedFile;
    private FileObject fileObject;
    private String relativePath;
    private String copy;
    private boolean copyScanned;
    private Boolean fileFlag;
    private String mimeType;

    public SvnFileNode(File file) {
        this.file = file;
        File norm = FileUtil.normalizeFile(file);
        if (Utilities.isMac() || Utilities.isUnix()) {
            FileInformation fi = Subversion.getInstance().getStatusCache().getStatus(file);
            FileInformation fiNorm = Subversion.getInstance().getStatusCache().getStatus(norm);
            if (fi.getStatus() != fiNorm.getStatus()) {
                norm = null;
            }
        }
        normalizedFile = norm;
    }

    public String getName() {
        return file.getName();
    }

    public FileInformation getInformation() {
        return Subversion.getInstance().getStatusCache().getStatus(file); 
    }

    /**
     * Careful, returned file may not be normalized
     * @return 
     */
    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof SvnFileNode && file.equals(((SvnFileNode) o).file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    public FileObject getFileObject() {
        if (fileObject == null && normalizedFile != null) {
            fileObject = FileUtil.toFileObject(normalizedFile);
        }
        return fileObject;
    }

    public Object[] getLookupObjects() {
        List<Object> list = new ArrayList<Object>(2);
        list.add(file);
        FileObject fo = getFileObject();
        if (fo != null) {
            list.add(fo);
        }
        return list.toArray(new Object[0]);
    }

    /**
     * Returns relativePath of this node's file or the full resourceUrl if explicitly selected in Options.
     * @return relative path of this node's file.
     */
    public String getLocation() {
        if (relativePath == null) {
            try {
                assert !java.awt.EventQueue.isDispatchThread();
                relativePath = SvnModuleConfig.getDefault().isRepositoryPathPrefixed()
                        ? SvnUtils.decodeToString(SvnUtils.getRepositoryUrl(getFile())) : SvnUtils.getRelativePath(getFile());
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
            }
            if (relativePath == null) {
                relativePath = NbBundle.getMessage(SvnFileNode.class, "SvnFileNode.relativePath.unknown"); //NOI18N
            }
        }
        return relativePath;
    }

    public String getCopy () {
        if (!copyScanned) {
            assert !java.awt.EventQueue.isDispatchThread();
            copy = SvnUtils.getCopy(getFile());
            copyScanned = true;
        }
        return copy;
    }

    public boolean isFile () {
        if (fileFlag == null) {
            fileFlag = file.isFile();
        }
        return fileFlag;
    }

    public String getMimeType () {
        if (isFile() && mimeType == null) {
            mimeType = SvnUtils.getMimeType(normalizedFile == null ? FileUtil.normalizeFile(file) : normalizedFile);
        }
        return mimeType;
    }

    public void initializeProperties() {
        getLocation();
        getCopy();
        isFile();
        getMimeType();
        getInformation().getEntry(file); // CommitTableModel.getValueAt may trigger status
    }
}
