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
package org.netbeans.modules.autoupdate.cli;

import java.util.List;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Status {

    static String[] toArray(UpdateUnit uu) {
        if (uu.getInstalled() != null) {
            return new String[] { 
                uu.getCodeName(),
                uu.getInstalled().getSpecificationVersion(), 
                Status.installed(uu).toString()
            };
        } else {
            List<UpdateElement> updates = uu.getAvailableUpdates();
            if (updates.isEmpty()) {
                return new String[] {
                    uu.getCodeName(),
                    "N/A", "N/A"
                };
            }
            UpdateElement first = updates.get(0);
            return new String[] { 
                uu.getCodeName(),
                first.getSpecificationVersion(), 
                Status.update(uu).toString()
            };
        }
    }

    final String txt;

    private Status(String txt) {
        this.txt = txt;
    }
    
    @NbBundle.Messages({
        "MSG_Enabled=Enabled",
        "MSG_Disabled=Installed",
        "# {0} - available version",
        "MSG_UpdateAvailable=Upgrade to {0}",
        "MSG_Unknown=Unknown state"
    })
    private static Status installed(UpdateUnit uu) {
        assert uu.getInstalled() != null;
        String install;
        ModuleInfo mi = find(uu.getCodeName());
        List<UpdateElement> updates = uu.getAvailableUpdates();
        if (!updates.isEmpty()) {
            install = Bundle.MSG_UpdateAvailable(updates.get(0).getSpecificationVersion());
        } else {
            if (mi == null) {
                install = Bundle.MSG_Unknown();
            } else {
                install = mi.isEnabled() ? Bundle.MSG_Enabled() : Bundle.MSG_Disabled();
            }
        }
        
        return new Status(install);
    }
    @NbBundle.Messages({
        "MSG_Updateable=Available"
    })
    private static Status update(UpdateUnit uu) {
        assert uu.getInstalled() == null && !uu.getAvailableUpdates().isEmpty();
        return new Status(Bundle.MSG_Updateable());
    }

    @Override
    public String toString() {
        return txt;
    }
    
    private static ModuleInfo find(String cnb) {
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (cnb.equals(mi.getCodeNameBase())) {
                return mi;
            }
        }
        return null;
    }
}
