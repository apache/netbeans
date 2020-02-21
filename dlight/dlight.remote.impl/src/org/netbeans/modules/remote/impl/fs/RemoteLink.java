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

package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import java.util.HashSet;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.openide.filesystems.FileLock;

/**
 *
 */
public final class RemoteLink extends RemoteLinkBase {
    
    private String normalizedTargetPath;

    /*package*/ RemoteLink(RemoteFileObject wrapper, RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, RemoteFileObjectBase parent, String remotePath, String link) {
        super(wrapper, fileSystem, execEnv, parent, remotePath);        
        this.normalizedTargetPath = normalize(link, parent);
    }

    private static String normalize(String link, RemoteFileObjectBase parent) {
        String path = link;
        if (!path.startsWith("/")) { // NOI18N
            String parentPath = parent.getPath();
            if (!parentPath.startsWith("/")) { // NOI18N
                parentPath = "/" + parentPath; // NOI18N
            }            
            path = parentPath + '/' + link;
        }
        return PathUtilities.normalizeUnixPath(path);
    }

    @Override
    public FileType getType() {
        return FileType.SymbolicLink;
    }

    @Override
    public RemoteFileObjectBase getCanonicalDelegate() {
        try {
            return RemoteFileSystemUtils.getCanonicalFileObject(this);
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    protected String getDelegateNormalizedPath() {
        return normalizedTargetPath;
    }

    /*package*/ final void setLink(String link, RemoteFileObjectBase parent) {
        initListeners(false);
        this.normalizedTargetPath = normalize(link, parent);
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null && delegate.isFolder()) {
            for (RemoteFileObject child: delegate.getChildren()) {
                String childAbsPath = getPath() + '/' + child.getNameExt();
                getFileSystem().getFactory().createRemoteLinkChild(this, childAbsPath, child.getImplementor());
            }
        }
        initListeners(true);
    }

    @Override
    protected void postDeleteOrCreateChild(RemoteFileObject child, DirEntryList entryList) {
        RemoteFileObjectBase canonicalDelegate = getCanonicalDelegate();
        if (canonicalDelegate != null) {
            canonicalDelegate.postDeleteOrCreateChild(child, entryList);
        }
    }

    @Override
    protected DirEntryList deleteImpl(FileLock lock) throws IOException {
        return RemoteFileSystemTransport.delete(getExecutionEnvironment(), getPath(), false);
    }

    @Override
    protected RemoteFileObjectBase getDelegateImpl() {
        HashSet<String> antiLoop = new HashSet<>();
        antiLoop.add(getPath());
        RemoteFileObjectBase delegate = getFileSystem().findResourceImpl(normalizedTargetPath, antiLoop);
        return delegate;
    }
    
    @Override
    public RemoteFileObjectBase readSymbolicLink()  {
        return getDelegateImpl();
    }

    @Override
    public String readSymbolicLinkPath()  {
        return getDelegateNormalizedPath();
    }    
}
