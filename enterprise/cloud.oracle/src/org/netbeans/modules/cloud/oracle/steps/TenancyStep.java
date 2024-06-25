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
package org.netbeans.modules.cloud.oracle.steps;

import com.oracle.bmc.identity.model.Tenancy;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.netbeans.modules.cloud.oracle.assets.Steps.Values;
import org.netbeans.modules.cloud.oracle.items.TenancyItem;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "SelectProfile=Select OCI Profile",
    "SelectProfile_Description={0} (region: {1})",
    "NoProfile=There is not any OCI profile in the config",
    "CollectingProfiles_Text=Loading OCI Profiles",
    "SelectKey=Select Key"
})
public final class TenancyStep extends AbstractStep<TenancyItem> {
    
    private List<OCIProfile> profiles = new LinkedList<>();
    private final AtomicReference<TenancyItem> selected = new AtomicReference<>();

    @Override
    public NotifyDescriptor createInput() {
        if (onlyOneChoice()) {
            throw new IllegalStateException("No data to create input"); // NOI18N
        }
        String title = Bundle.SelectProfile();
        List<NotifyDescriptor.QuickPick.Item> items = new ArrayList<>(profiles.size());
        for (OCIProfile p : profiles) {
            Tenancy t = p.getTenancyData();
            if (t != null) {
                items.add(new NotifyDescriptor.QuickPick.Item(p.getId(), Bundle.SelectProfile_Description(t.getName(), t.getHomeRegionKey())));
            }
        }
        if (profiles.stream().filter(p -> p.getTenancy().isPresent()).count() == 0) {
            title = Bundle.NoProfile();
        }
        return new NotifyDescriptor.QuickPick(title, title, items, false);
    }

    @Override
    public void prepare(ProgressHandle h, Values values) {
        h.progress(Bundle.CollectingProfiles_Text());
        profiles = OCIManager.getDefault().getConnectedProfiles();
    }

    @Override
    public void setValue(String value) {
        for (OCIProfile profile : profiles) {
            if (profile.getId().equals(value)) {
                profile.getTenancy().ifPresent(t -> this.selected.set(t));
                break;
            }
        }
    }

    @Override
    public TenancyItem getValue() {
        if (onlyOneChoice()) {
            return profiles.stream().map(p -> p.getTenancy()).filter(Optional::isPresent).map(Optional::get).findFirst().get();
        }
        return selected.get();
    }

    @Override
    public boolean onlyOneChoice() {
        return profiles.stream().filter(p -> p.getTenancy().isPresent()).count() == 1;
    }
    
}
