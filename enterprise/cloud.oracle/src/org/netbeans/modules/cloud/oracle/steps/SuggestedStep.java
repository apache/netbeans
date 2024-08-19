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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.CreateNewResourceItem;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.netbeans.modules.cloud.oracle.assets.Steps.Values;
import org.netbeans.modules.cloud.oracle.bucket.BucketNode;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.compute.ClusterNode;
import org.netbeans.modules.cloud.oracle.compute.ComputeInstanceNode;
import org.netbeans.modules.cloud.oracle.database.DatabaseNode;
import org.netbeans.modules.cloud.oracle.developer.ContainerRepositoryNode;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.vault.VaultNode;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Show list of items for a suggested type.
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "Databases=Oracle Autonomous Database",
    "Vault=OCI Vault",
    "Bucket=Object Storage Bucket",
    "Cluster=Oracle Container Engine",
    "Compute=Compute Instance",
    "SelectItem=Select {0}",
    "ContainerRepository=Container Repository",
})
public class SuggestedStep extends AbstractStep<OCIItem> {
    private static final Logger LOG = Logger.getLogger(SuggestedStep.class.getName());
    private final Map<String, OCIItem> items = new HashMap<>();
    private OCIItem selected;
    private String suggestedType;

    public SuggestedStep(String suggestedType) {
        this.suggestedType = suggestedType;
    }

    @Override
    public void prepare(ProgressHandle h, Values values) {
        h.progress(Bundle.CollectingItems_Text());
        if (suggestedType == null) {
            suggestedType = values.getValueForStep(ItemTypeStep.class);
        }
        CompartmentItem compartment = values.getValueForStep(CompartmentStep.class);
        getItemsByPath(compartment, suggestedType)
                .forEach(db -> items.put(db.getName(), db));
    }

    private String getSuggestedItemName() {
        switch (suggestedType) {
            case "Databases":
                return Bundle.Databases();
            case "Vault":
                return Bundle.Vault();
            case "Bucket":
                return Bundle.Bucket();
            case "Cluster":
                return Bundle.Cluster();
            case "ComputeInstance":
                return Bundle.Compute();
            case "ContainerRepository":
                return Bundle.ContainerRepository();
        }
        throw new MissingResourceException("Missing OCI type", null, suggestedType);
    }

    @Override
    public NotifyDescriptor createInput() {
        return Steps.createQuickPick(items, Bundle.SelectItem(getSuggestedItemName()));
    }

    @Override
    public void setValue(String selected) {
        this.selected = items.get(selected);
    }

    @Override
    public OCIItem getValue() {
        if (onlyOneChoice()) {
            selected = items.values().iterator().next();
        }
        return selected;
    }

    @Override
    public boolean onlyOneChoice() {
        return false;
    }
    
    /**
     * Retrieve items of a given type from a specified compartment.
     * @param parent Compartment to search for items
     * @param path Type of the items
     * @return  List of items found
     */
    protected static List<? extends OCIItem> getItemsByPath(CompartmentItem parent, String path) {
        List<OCIItem> items = new ArrayList<>();
        try {
            switch (path) {
                case "Databases": //NOI18N
                    return DatabaseNode.getDatabases().apply(parent);
                case "Vault": //NOI18N
                    return VaultNode.getVaults().apply(parent);
                case "Bucket": //NOI18N
                    return BucketNode.getBuckets().apply(parent);
                case "Cluster": //NOI18N
                    return ClusterNode.getClusters().apply(parent);
                case "ComputeInstance": //NOI18N
                    return ComputeInstanceNode.getComputeInstances().apply(parent);
                case "ContainerRepository": //NOI18N
                    items.add(new CreateNewResourceItem());
                    items.addAll(ContainerRepositoryNode.getContainerRepositories().apply(parent));
                    return items;
                default:
                    return Collections.emptyList();
            }
        } catch (BmcException e) {
            LOG.log(Level.SEVERE, "Unable to load vault list", e); //NOI18N
        }
        return Collections.emptyList();
    }
    
}
