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
package org.netbeans.modules.cloud.oracle;

import org.netbeans.modules.cloud.oracle.items.OCIItem;
import javax.swing.JComponent;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Horvath
 */
public class TenancyInstance implements ServerInstanceImplementation, Lookup.Provider {

    private final OCIItem tenancy;
    private final Lookup lkp;
    final OCIProfile profile;
    
    public TenancyInstance(OCIItem tenancy, OCIProfile profile) {
        this.tenancy = tenancy;
        this.profile = profile;
        lkp = tenancy != null ? Lookups.fixed(profile, tenancy) : Lookups.fixed(profile);
    }
    
    @NbBundle.Messages({
        "# {0} - tenancy ID",
        "# {1} - profile ID",
        "MSG_TenancyDesc={0} ({1})",
        "# {0} - tenancy ID",
        "# {1} - profile ID",
        "MSG_BrokenTenancy=Unavailable tenancy {0}",
        "# {0} - profile ID",
        "MSG_BrokenProfile=Broken profile {0}",
    })
    @Override
    public String getDisplayName() {
        if (tenancy != null) {
            return Bundle.MSG_TenancyDesc(tenancy.getName(), profile.getId());
        } else if (profile.getTenancy().isPresent()) {
            return Bundle.MSG_BrokenTenancy(profile.getTenancy().get().getKey().getValue(), profile.getId());
        } else {
            return Bundle.MSG_BrokenProfile(profile.getId());
        }
    }

    @Override
    public Lookup getLookup() {
        return lkp;
    }

    @Override
    public String getServerDisplayName() {
        return profile.getTenancy().isPresent() ? profile.getTenancy().get().getKey().getValue() 
                : Bundle.MSG_BrokenProfile(profile);
    }
    
    public OCIProfile getProfile() {
        return profile;
    }

    @Override
    public Node getFullNode() {
        return getBasicNode();
    }

    @Override
    public Node getBasicNode() {
        return tenancy == null ? new BrokenProfileNode(this) : new TenancyNode(tenancy, getDisplayName(), profile);
    }

    @Override
    public JComponent getCustomizer() {
        return null;
    }

    @Override
    public void remove() {
    }

    @Override
    public boolean isRemovable() {
        return false;
    }
    
}
