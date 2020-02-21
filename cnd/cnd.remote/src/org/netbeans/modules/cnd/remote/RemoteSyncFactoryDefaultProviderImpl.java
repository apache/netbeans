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
package org.netbeans.modules.cnd.remote;

import org.netbeans.modules.cnd.remote.sync.FtpSyncFactory;
import org.netbeans.modules.cnd.remote.sync.SharedSyncFactory;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.spi.remote.setup.RemoteSyncFactoryDefaultProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=RemoteSyncFactoryDefaultProvider.class, position=100)
public class RemoteSyncFactoryDefaultProviderImpl implements RemoteSyncFactoryDefaultProvider {
    @Override
    public RemoteSyncFactory getDefaultFactory(ExecutionEnvironment env) {
        String defaultID = System.getProperty("cnd.remote.default.sync");
        if (defaultID != null) {
            RemoteSyncFactory factory = RemoteSyncFactory.fromID(defaultID);
            if (factory != null) {
                return factory;
            }
        }
        if (Utilities.isUnix()) {
            return RemoteSyncFactory.fromID(SharedSyncFactory.ID);
        } else {
            return RemoteSyncFactory.fromID(FtpSyncFactory.ID);
        }
    }    
}
