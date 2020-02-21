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

package org.netbeans.modules.cnd.api.remote;

import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 *
 */
public interface RemoteProject {

    // FIXUP. Think over how to get correct factory
    static final String FULL_REMOTE_SYNC_ID = "full"; //NOI18N
    
    ExecutionEnvironment getDevelopmentHost();
    
    ExecutionEnvironment getSourceFileSystemHost();
    
    FileSystem getSourceFileSystem();

    RemoteSyncFactory getSyncFactory();
    
    /**
     * Base project directory (to resolve relative paths against)
     * @return 
     */
    String getSourceBaseDir();

    /**
     * NB: since 8.01 this is the same as Project.getProjectDirectory()
     * and is used to create a storage for timestamps, etc. when synchronizing
     * TODO: rename appropriately
     * 
     * @return 
     */
    FileObject getSourceBaseDirFileObject();
}
