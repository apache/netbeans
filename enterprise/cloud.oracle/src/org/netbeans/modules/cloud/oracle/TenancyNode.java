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
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Horvath
 */
public class TenancyNode extends OCINode implements PropertyChangeListener {
    
    private static final String ORCL_ICON = "org/netbeans/modules/cloud/oracle/resources/tenancy.svg"; // NOI18N
    
    private final OCISessionInitiator session;
    
    public TenancyNode(OCIItem tenancy, String disp, OCISessionInitiator session) {
        super(tenancy, session);
        this.session = session;
        setName(tenancy.getName()); 
        setDisplayName(disp);
        setIconBaseWithExtension(ORCL_ICON);
        // home region will be set as a description
        setShortDescription(tenancy.getDescription());
        OCIManager.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this, OCIManager.getDefault()));
        if (session instanceof OCIProfile) {
            ((OCIProfile) session).addPropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (OCIManager.PROP_ACTIVE_PROFILE.equals(e.getPropertyName()) ||
            OCIProfile.PROP_ACTIVE_REGION_CODE.equals(e.getPropertyName())    ) {
            fireDisplayNameChange(null, null);
        }
    }

    @NbBundle.Messages({
        "# {0} - tenancy ID",
        "# {1} - profile ID",
        "MSG_TenancyDesc={0} (profile: {1}, region: {2})",
        "# {0} - tenancy ID",
        "# {1} - profile ID",
        "MSG_BrokenTenancy=Unavailable tenancy {0}",
        "# {0} - profile ID",
        "MSG_BrokenProfileNode=Broken profile {0}",
    })
    @Override
    public String getDisplayName() {
        OCIProfile profile = (OCIProfile) session;
        if (item != null) {
            return Bundle.MSG_TenancyDesc(item.getName(), profile.getId(), profile.getRegion());
        } else if (profile.getTenantId() != null) {
            return Bundle.MSG_BrokenTenancy(profile.getTenantId(), profile.getId());
        } else {
            return Bundle.MSG_BrokenProfileNode(profile.getId());
        }
    }
    
    @NbBundle.Messages({
        "HTML_EmphasizeName=<b>{0}</b>"
    })
    @Override
    public String getHtmlDisplayName() {
        if (OCIManager.getDefault().getActiveProfile() == session) {
            return Bundle.HTML_EmphasizeName(getDisplayName());
        } else {
            return null;
        }
    }
    
    @Override
    public Image getIcon(int type) {
        return badgeIcon(super.getIcon(type));
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return badgeIcon(super.getOpenedIcon(type));
    }
    
    private Image badgeIcon(Image origImg) {
        return origImg;
    }

    @Override
    public boolean canDestroy() {
        return OCIManager.getDefault().isConfiguredProfile((OCIProfile)session);
    }

    @Override
    public void destroy() throws IOException {
        OCIManager.getDefault().removeConnectedProfile((OCIProfile)session);
    }
}
