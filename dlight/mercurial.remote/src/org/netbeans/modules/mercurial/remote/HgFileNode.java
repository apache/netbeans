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

package org.netbeans.modules.mercurial.remote;

import org.openide.filesystems.FileObject;

import java.util.List;
import java.util.ArrayList;
import org.netbeans.modules.remotefs.versioning.util.common.VCSFileNode;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;

/**
 * Represents real or virtual (non-local) file.
 *
 * 
 */
public final class HgFileNode extends VCSFileNode<FileInformation> {

    private final VCSFileProxy file;
    private final VCSFileProxy normalizedFile;
    private FileObject fileObject;

    public HgFileNode(VCSFileProxy root, VCSFileProxy file) {
        super(root, file);
        this.file = file;
        normalizedFile = file.normalizeFile();
    }
    
    @Override
    public FileInformation getInformation() {
        return Mercurial.getInstance().getFileStatusCache().getStatus(file); 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof HgFileNode && file.equals(((HgFileNode) o).file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }


    public FileObject getFileObject() {
        if (fileObject == null) {
            fileObject = normalizedFile.toFileObject();
        }
        return fileObject;
    }

    @Override
    public Object[] getLookupObjects() {
        List<Object> list = new ArrayList<>(2);
        list.add(file);
        FileObject fo = getFileObject();
        if (fo != null) {
            list.add(fo);
        }
        return list.toArray(new Object[list.size()]);
    }

    @Override
    public VCSCommitOptions getDefaultCommitOption (boolean withExclusions) {
        return VCSCommitOptions.COMMIT;
    }
 }
