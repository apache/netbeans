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

import com.oracle.bmc.model.BmcException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.netbeans.modules.cloud.oracle.assets.Steps.Values;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.vault.KeyItem;
import org.netbeans.modules.cloud.oracle.vault.KeyNode;
import org.netbeans.modules.cloud.oracle.vault.VaultItem;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "NoKeys=No keys in this Vault. Select another one."
})
public class KeyStep extends AbstractStep<KeyItem> {
    private static final Logger LOG = Logger.getLogger(KeyStep.class.getName());
    private Map<String, KeyItem> keys = null;
    private KeyItem selected;
    private VaultItem vault;

    public KeyStep(VaultItem vault) {
        this.vault = vault;
    }

    public KeyStep() {
        vault = null;
    }
    
    @Override
    public void prepare(ProgressHandle h, Values values) {
        if (vault == null) {
            vault = values.getValueForStep(VaultStep.class);
        }
        keys = getKeys(vault);
    }

    @Override
    public boolean onlyOneChoice() {
        return keys.size() == 1;
    }

    @Override
    public NotifyDescriptor createInput() {
        if (keys.size() > 1) {
            return Steps.createQuickPick(keys, Bundle.SelectKey());
        }
        if (keys.isEmpty()) {
            return new NotifyDescriptor.QuickPick("", Bundle.NoKeys(), Collections.emptyList(), false);
        }
        throw new IllegalStateException("No data to create input"); // NOI18N
    }

    @Override
    public void setValue(String selected) {
        this.selected = keys.get(selected);
    }

    @Override
    public KeyItem getValue() {
        if (keys.size() == 1) {
            return keys.values().iterator().next();
        }
        return selected;
    }

    protected Map<String, KeyItem> getKeys(OCIItem parent) {
        Map<String, KeyItem> items = new HashMap<>();
        try {
            if (parent instanceof VaultItem) {
                KeyNode.getKeys().apply((VaultItem) parent).forEach(key -> items.put(key.getName(), key));
            }
        } catch (BmcException e) {
            LOG.log(Level.SEVERE, "Unable to load key list", e); //NOI18N
        }
        return items;
    }
    
}
