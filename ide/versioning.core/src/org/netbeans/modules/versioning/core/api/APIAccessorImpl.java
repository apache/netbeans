/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.versioning.core.api;

import java.io.File;
import org.netbeans.modules.versioning.core.APIAccessor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author tomas
 */
class APIAccessorImpl extends APIAccessor {

    @Override
    public VCSFileProxy createFlatFileProxy(FileObject fo) {
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        if(proxy != null) {
            proxy.setFlat(true);
        }
        return proxy;
    }

    @Override
    public boolean isFlat(VCSFileProxy file) {
        return file.isFlat();
    }
    
    @Override
    public VCSFileProxy createFileProxy(VCSFileProxy parent, String name, boolean isDirectory) {
        return VCSFileProxy.createFileProxy(parent, name, isDirectory);
    }

    @Override
    public boolean isLocalFile(VCSFileProxy file) {
        return file.getFileProxyOperations() == null;
    }

    @Override
    public VCSFileProxy createFileProxy(String path) {
        return VCSFileProxy.createFileProxy(path);
    }
}
