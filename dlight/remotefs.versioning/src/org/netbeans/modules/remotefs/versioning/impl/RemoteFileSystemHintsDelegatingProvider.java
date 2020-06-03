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
package org.netbeans.modules.remotefs.versioning.impl;

import org.netbeans.modules.remote.spi.RemoteFileSystemHintsProvider;
import org.netbeans.modules.remotefs.versioning.spi.RemoteVcsFileSystemHintsProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Translates RemoteVcsFileSystemHintsProvider -> RemoteFileSystemHintsProvider
 */
@ServiceProvider(service = RemoteFileSystemHintsProvider.class)
public class RemoteFileSystemHintsDelegatingProvider implements RemoteFileSystemHintsProvider {

    @Override
    public boolean isSniffing(String fileNameExt) {
        if ("CVS".equals(fileNameExt)) { //NOI18N
            // "CVS" metadata subfolder name
            // is provided via layer in versioning.system.cvss.installer
            // I don't know a proper place to place the appropriate hint provider, so let it stay here
            return true;
        }
        for (RemoteVcsFileSystemHintsProvider provider :
                Lookup.getDefault().lookupAll(RemoteVcsFileSystemHintsProvider.class)) {
            if (provider.isSniffing(fileNameExt)) {
                return true;
            }
        }
        return false;
    }
}
