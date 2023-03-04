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

package org.netbeans.modules.versioning.core;

import java.net.URI;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor;
import org.netbeans.spi.queries.VersioningQueryImplementation;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.VersioningQueryImplementation.class)
public class VersioningQueryImplementationImpl implements VersioningQueryImplementation{

    @Override
    public boolean isManaged(URI uri) {
        VCSFileProxy proxy = Utils.toFileProxy(uri);
        return proxy != null ? 
                VersioningManager.getInstance().getOwner(proxy) != null : 
                false;
    }

    @Override
    public String getRemoteLocation(URI uri) {
        VCSFileProxy proxy = Utils.toFileProxy(uri);
        if (proxy == null) {
            return null;
        }
        return (String) VCSFilesystemInterceptor.getAttribute(proxy, VersioningManager.ATTRIBUTE_REMOTE_LOCATION);
    }
    
}
