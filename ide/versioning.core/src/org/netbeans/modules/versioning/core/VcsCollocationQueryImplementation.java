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

import java.net.MalformedURLException;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.spi.queries.CollocationQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * Delegates the work to the owner of files in query.
 * 
 * @author Maros Sandor
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.CollocationQueryImplementation2.class, position=50)
public class VcsCollocationQueryImplementation implements CollocationQueryImplementation2 {

    @Override
    public boolean areCollocated(URI file1, URI file2) {
        VCSFileProxy proxy1 = Utils.toFileProxy(file1);
        VCSFileProxy proxy2 = Utils.toFileProxy(file2);
        
        if(proxy1 == null || proxy2 == null) return false;
        VersioningSystem vsa = VersioningManager.getInstance().getOwner(proxy1);
        VersioningSystem vsb = VersioningManager.getInstance().getOwner(proxy2);
        if (vsa == null || vsa != vsb) return false;
        
        CollocationQueryImplementation2 cqi = vsa.getCollocationQueryImplementation();
        return cqi != null && cqi.areCollocated(file1, file2);
    }

    @Override
    public URI findRoot(URI file) {
        VCSFileProxy proxy = Utils.toFileProxy(file);
        if(proxy != null) {
            VersioningSystem system = VersioningManager.getInstance().getOwner(proxy);
            CollocationQueryImplementation2 cqi = system != null ? system.getCollocationQueryImplementation() : null;
            return cqi != null ? cqi.findRoot(file) : null;
        }
        return null;
    }
    
}        
