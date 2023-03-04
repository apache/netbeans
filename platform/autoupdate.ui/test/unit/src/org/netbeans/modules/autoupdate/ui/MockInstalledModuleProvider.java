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

package org.netbeans.modules.autoupdate.ui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.autoupdate.updateprovider.InstalledUpdateProvider;
import org.openide.modules.ModuleInfo;

public final class MockInstalledModuleProvider extends InstalledUpdateProvider {
    private static List<ModuleInfo> moduleItems;
    
    public static void setModuleItems(ModuleInfo... items) {
        moduleItems = Arrays.asList(items);
    }

    @Override
    protected Map<String, ModuleInfo> getModuleInfos(boolean force) {
        Map<String, ModuleInfo> mf = new HashMap<String, ModuleInfo>();
        for (ModuleInfo mi : moduleItems) {
            mf.put(mi.getCodeName(), mi);
        }
        return mf;
    }


}
