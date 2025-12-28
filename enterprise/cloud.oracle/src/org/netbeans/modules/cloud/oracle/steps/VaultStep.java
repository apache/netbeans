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

import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.netbeans.modules.cloud.oracle.assets.Steps.Values;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.vault.VaultItem;
import org.netbeans.modules.cloud.oracle.vault.VaultNode;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "FetchingVaults=Fetching OCI Vaults",
    "SelectVault=Select Vault",
})
public class VaultStep extends AbstractStep<VaultItem> {
    private static final Logger LOG = Logger.getLogger(VaultStep.class.getName());
    private Map<String, VaultItem> vaults = null;
    private VaultItem selected;

    @Override
    public void prepare(ProgressHandle h, Values values) {
        h.progress(Bundle.FetchingVaults());
        CompartmentItem compartment = values.getValueForStep(CompartmentStep.class);
        vaults = VaultNode.getVaults().apply((CompartmentItem) compartment).stream().collect(Collectors.toMap(VaultItem::getName, vault -> vault));
    }

    @Override
    public NotifyDescriptor createInput() {
        return Steps.createQuickPick(vaults, Bundle.SelectVault());
    }

    @Override
    public void setValue(String selected) {
        this.selected = vaults.get(selected);
    }

    @Override
    public VaultItem getValue() {
        if (onlyOneChoice()) {
            selected = vaults.values().iterator().next();
        }
        return selected;
    }

    @Override
    public boolean onlyOneChoice() {
        return vaults.size() == 1;
    }
    
}
