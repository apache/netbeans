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

package org.netbeans.libs.svnclientadapter.svnkit;

import java.util.logging.Level;
import org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

/**
 *
 * @author Tomas Stupka
 */
@ServiceProviders({@ServiceProvider(service=SvnClientAdapterFactory.class)})
public class SvnKitClientAdapterFactory extends SvnClientAdapterFactory {
    
    private boolean available = false;
    
    public SvnKitClientAdapterFactory() {
        super();
    }

    @Override
    public Client provides() {
        return Client.SVNKIT;
    }

    @Override
    protected boolean isAvailable() {
        if(!available) {
            try {
                org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory.setup();        
            } catch (Throwable t) {
                LOG.log(Level.WARNING, t.getMessage());
            }
            if(org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory.isAvailable()) {
                available = true;
            }
        }
        return available;
    }

    @Override
    public ISVNClientAdapter createClient() {
        // is this really needed? this clears the credentials cache
        SVNClientImpl.setRuntimeCredentialsStorage(null);
        org.tmatesoft.svn.core.javahl17.SVNClientImpl.setRuntimeCredentialsStorage(null);
        return org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory
                .createSVNClient(org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory.SVNKIT_CLIENT);
    }
    
}
