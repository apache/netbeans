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
package org.netbeans.modules.versioning.ui.history;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.core.util.Utils;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class History {

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.versioning.ui.history"); // NOI18N
    private static History instance;
    private Result<? extends VCSHyperlinkProvider> hpResult;
    private RequestProcessor rp;
    
    private History() { }
    
    public static synchronized History getInstance() {
        if(instance == null) {
            instance = new History();
        }
        return instance;
    }
    
    public RequestProcessor getRequestProcessor() {
        if (rp == null) {
            rp = new RequestProcessor("History.ParallelTasks", 5, true); //NOI18N
        }
        return rp;
    }    
    
    /**
     *
     * @return registered hyperlink providers
     */
    public List<VCSHyperlinkProvider> getHyperlinkProviders() {
        if (hpResult == null) {
            hpResult = (Lookup.Result<? extends VCSHyperlinkProvider>) Lookup.getDefault().lookupResult(VCSHyperlinkProvider.class);
        }
        if (hpResult == null) {
            return Collections.emptyList();
        }
        Collection<? extends VCSHyperlinkProvider> providersCol = hpResult.allInstances();
        List<VCSHyperlinkProvider> providersList = new ArrayList<VCSHyperlinkProvider>(providersCol.size());
        providersList.addAll(providersCol);
        return Collections.unmodifiableList(providersList);
    }
    
    VersioningSystem getLocalHistory(FileObject fo) {
        VersioningSystem vs = Utils.getLocalHistory(VCSFileProxy.createFileProxy(fo));
        if(vs == null) {
            LOG.log(Level.FINE, "local history not available for file {0}", fo); // NOI18N
        } 
        return vs;
    }
    
    static VCSHistoryProvider getHistoryProvider(VersioningSystem versioningSystem) {
        if(versioningSystem == null) {
            return null;
        }
        return  versioningSystem.getVCSHistoryProvider();
    }
    
    static VCSFileProxy[] toProxies(FileObject[] files) {
        if(files == null) {
            return new VCSFileProxy[0];
        }
        List<VCSFileProxy> l = new ArrayList<VCSFileProxy>(files.length);
        for (FileObject f : files) {
            VCSFileProxy proxy = VCSFileProxy.createFileProxy(f);
            if(proxy != null) {
                l.add(proxy);
            }
        }
        return l.toArray(new VCSFileProxy[0]);
    }

}
