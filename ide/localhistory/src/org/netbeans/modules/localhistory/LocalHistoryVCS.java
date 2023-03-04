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
package org.netbeans.modules.localhistory;

import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.spi.VersioningSystem;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;

/**
 *
 * Provides the VersioningSystem functionality to the IDE
 * 
 * @author Tomas Stupka
 */
@VersioningSystem.Registration(
    displayName="#CTL_DisplayName", 
    menuLabel="#CTL_MainMenuItem", 
    metadataFolderNames={}, 
    actionsCategory="History"
)
public class LocalHistoryVCS extends VersioningSystem {
        
    public LocalHistoryVCS() {
        LocalHistory.getInstance().addVersioningListener(new VersioningListener() {
            @Override
            public void versioningEvent(VersioningEvent event) {
                if(event.getId().equals(LocalHistory.EVENT_PROJECTS_CHANGED)) {
                    fireVersionedFilesChanged();   
                }                
            }
        });
    }
    
    @Override
    public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {    
        if(file == null) {
            return null;
        }                

        LocalHistory lh = LocalHistory.getInstance();

        if(lh.isOpenedOrTouched(file)) {
            return file;
        }
    
        VCSFileProxy a = lh.isManagedByParent(file);
        if(a != null) {
            return a;
        }
        return null;
    }

    @Override
    public VCSAnnotator getVCSAnnotator() {
        return LocalHistory.getInstance().getVCSAnnotator();
    }
    
    @Override
    public VCSInterceptor getVCSInterceptor() {
        return LocalHistory.getInstance().getVCSInterceptor();
    }

    @Override
    public VCSHistoryProvider getVCSHistoryProvider() {
        return LocalHistory.getInstance().getVCSHistoryProvider();
    }
    
    void managedFilesChanged() {
        fireVersionedFilesChanged();
    }
}
