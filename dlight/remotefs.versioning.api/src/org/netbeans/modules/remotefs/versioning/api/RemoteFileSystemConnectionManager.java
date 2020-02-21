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
package org.netbeans.modules.remotefs.versioning.api;

import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class RemoteFileSystemConnectionManager {
    private static final DefaultRemoteFileSystemConnectionManager instance = new DefaultRemoteFileSystemConnectionManager();
    
    public static RemoteFileSystemConnectionManager getInstance() {
        return instance;
    }

    public abstract void addRemoteFileSystemConnectionListener(RemoteFileSystemConnectionListener listener);

    public abstract void removeRemoteFileSystemConnectionListener(RemoteFileSystemConnectionListener listener);
    
    public abstract boolean isConnectedRemoteFileSystem(FileSystem fs);

    private static final class DefaultRemoteFileSystemConnectionManager extends RemoteFileSystemConnectionManager{
        private final Lookup.Result<RemoteFileSystemConnectionManager> res;
        private static final boolean FIX_SERVICE = true;
        private RemoteFileSystemConnectionManager fixedSelector;
        
        DefaultRemoteFileSystemConnectionManager() {
            res = Lookup.getDefault().lookupResult(RemoteFileSystemConnectionManager.class);
        }

        private RemoteFileSystemConnectionManager getService(){
            RemoteFileSystemConnectionManager service = fixedSelector;
            if (service == null) {
                for (RemoteFileSystemConnectionManager selector : res.allInstances()) {
                    service = selector;
                    break;
                }
                if (FIX_SERVICE && service != null) {
                    fixedSelector = service;
                }
            }
            return service;
        }

        @Override
        public void addRemoteFileSystemConnectionListener(RemoteFileSystemConnectionListener listener) {
            getService().addRemoteFileSystemConnectionListener(listener);
        }

        @Override
        public void removeRemoteFileSystemConnectionListener(RemoteFileSystemConnectionListener listener) {
            getService().removeRemoteFileSystemConnectionListener(listener);
        }

        @Override
        public boolean isConnectedRemoteFileSystem(FileSystem fs) {
            return getService().isConnectedRemoteFileSystem(fs);
        }
    }
}
