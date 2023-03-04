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

package org.netbeans.modules.autoupdate.updateprovider;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.ServiceProvider;

/** Default implementation of InstalledUpdateProvider.
 *
 * @author Jiri Rechtacek
 */
@ServiceProvider(service=InstalledUpdateProvider.class)
public final class InstalledModuleProvider extends InstalledUpdateProvider {
    private LookupListener  lkpListener;
    private Lookup.Result<ModuleInfo> result;
    private Map<String, ModuleInfo> moduleInfos;

    @Override
    protected synchronized  Map<String, ModuleInfo> getModuleInfos (boolean force) {
        if (moduleInfos == null || force) {
            Collection<? extends ModuleInfo> infos = Collections.unmodifiableCollection (result.allInstances ());
            moduleInfos = new HashMap<String, ModuleInfo> ();
            for (ModuleInfo info: infos) {
                moduleInfos.put (info.getCodeNameBase (), info);
            }            
        }
        assert moduleInfos != null;
        return new HashMap<String, ModuleInfo> (moduleInfos);
    }

    public InstalledModuleProvider() {
        result = Lookup.getDefault().lookup(new Lookup.Template<ModuleInfo> (ModuleInfo.class));
        lkpListener = new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                clearModuleInfos();
            }
        };
        result.addLookupListener(lkpListener);
    }

    private synchronized void clearModuleInfos() {
        moduleInfos = null;
    }
}
