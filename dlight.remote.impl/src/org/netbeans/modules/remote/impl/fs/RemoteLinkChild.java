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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.openide.filesystems.FileLock;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 */
public class RemoteLinkChild extends RemoteLinkBase {

    private final RemoteFileObjectBase delegate;

    private static final boolean NO_CYCLIC_LINKS = RemoteFileSystemUtils.getBoolean("remote.block.cyclic.links", true);

    /*package*/ RemoteLinkChild(RemoteFileObject wrapper, RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, RemoteLinkBase parent, String remotePath, RemoteFileObjectBase delegate) {
        super(wrapper, fileSystem, execEnv, parent, remotePath);
        Parameters.notNull("delegate", delegate);
        this.delegate = delegate;
    }

    @Override
    protected void initListeners(boolean add) {
        if (add) {
            if (NO_CYCLIC_LINKS && hasCycle()) {
                setFlag(MASK_CYCLIC_LINK, true);
            }
            delegate.addFileChangeListener(this);
        } else {
            delegate.removeFileChangeListener(this);
        }
    }

    private static RemoteFileObjectBase findSemiCanonicalDelegate(RemoteLinkChild fo) {
        RemoteFileObjectBase d = fo.delegate;
        while (d instanceof RemoteLinkChild) {
            d = ((RemoteLinkChild) d).delegate;
        }
        return d;
    }
    
    private boolean hasCycle() {
        RemoteFileObjectBase dlg = findSemiCanonicalDelegate(this);
        RemoteFileObjectBase p = getParent();
        while (p instanceof RemoteLinkChild) {
            if (p == dlg) {
                return true;
            }
            if (findSemiCanonicalDelegate((RemoteLinkChild) p) == dlg) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    @Override
    public RemoteFileObject[] getChildren() {
        if (getFlag(MASK_CYCLIC_LINK)) {
            return new RemoteFileObject[0];
        } else {
            return super.getChildren();
        }
    }

    @Override
    public RemoteFileObject getFileObject(String relativePath, Set<String> antiLoop) {
        if (getFlag(MASK_CYCLIC_LINK)) {
            return null;
        } else {
            return super.getFileObject(relativePath, antiLoop);
        }
    }

    @Override
    public boolean canWriteImpl(RemoteFileObjectBase orig) {
        return getFlag(MASK_CYCLIC_LINK) ? false : super.canWriteImpl(orig);
    }

    @Override
    public RemoteFileObjectBase getCanonicalDelegate() {
// It seems that the fix below is not complete and leads to inconsistency.
// Soon after I wrote it I got exception
// IllegalArgumentException: All children must have the same parent - see
// http://statistics.netbeans.org/analytics/exception.do?id=812814
// This should be moved into constructor or factory method,
// but this needs RemoteFileObjectFactory top be refactored
//        // We previously returned just a delegate.
//        // In the case of links to links or cyclic links this leads to too deep delegation
//        RemoteFileObjectBase d = delegate;
//        while (d instanceof RemoteLinkChild) {
//            d = ((RemoteLinkChild) d).delegate;
//        }
//        return d;
        return delegate;
    }

    @Override
    protected RemoteFileObjectBase getDelegateImpl() {
        return delegate;
    }
        
    @Override
    protected String getDelegateNormalizedPath() {
        return delegate.getPath();
    }

    @Override
    public FileType getType() {
        return delegate.getType();
    }    
    
    @Override
    public boolean isValid() {
        if (isValidFastWithParents()) {
            return delegate.isValid();
        }
        return false;
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
        RemoteFileObjectBase canonicalDelegate = getCanonicalDelegate();
        if (canonicalDelegate != null) {
            return canonicalDelegate.deleteImpl(lock);
        } else {
            throw new FileNotFoundException(getDisplayName() + " does not exist"); //NOI18Ns
        }
    }

    protected void renameImpl(FileLock lock, String name, String ext, RemoteFileObjectBase orig) throws IOException {
        // all work in delegate
        RemoteFileObjectBase dlg = getCanonicalDelegate();
        if (dlg != null) {
            dlg.renameImpl(lock, name, ext, orig);
        } else {
            throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteLinkChild.class,
                    "EXC_CanNotRenameFO", getDisplayName())); //NOI18N
        }
    }
}
