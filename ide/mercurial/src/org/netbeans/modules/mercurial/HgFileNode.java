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

package org.netbeans.modules.mercurial;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSFileNode;

/**
 * Represents real or virtual (non-local) file.
 *
 * @author Padraig O'Briain
 */
public final class HgFileNode extends VCSFileNode<FileInformation> {

    private final File file;
    private final File normalizedFile;
    private FileObject fileObject;

    public HgFileNode(File root, File file) {
        super(root, file);
        this.file = file;
        normalizedFile = FileUtil.normalizeFile(file);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public FileInformation getInformation() {
        return Mercurial.getInstance().getFileStatusCache().getStatus(file); 
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof HgFileNode && file.equals(((HgFileNode) o).file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public FileObject getFileObject() {
        if (fileObject == null) {
            fileObject = FileUtil.toFileObject(normalizedFile);
        }
        return fileObject;
    }

    @Override
    public Object[] getLookupObjects() {
        List<Object> list = new ArrayList<Object>(2);
        list.add(file);
        FileObject fo = getFileObject();
        if (fo != null) {
            list.add(fo);
        }
        return list.toArray(new Object[0]);
    }

    @Override
    public VCSCommitOptions getDefaultCommitOption (boolean withExclusions) {
        return VCSCommitOptions.COMMIT;
    }
}
