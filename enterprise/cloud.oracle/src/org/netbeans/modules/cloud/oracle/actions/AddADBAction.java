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
package org.netbeans.modules.cloud.oracle.actions;

import com.oracle.bmc.model.BmcException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentNode;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.database.DatabaseNode;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.items.TenancyItem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.QuickPick.Item;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.AddADBAction"
)
@ActionRegistration( 
        displayName = "#AddADB", 
        asynchronous = true
)

@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/Common/Actions", position = 260)
})
@NbBundle.Messages({
    "AddADB=Add Oracle Autonomous DB",
    "SelectTenancy=Select Tenancy",
    "SelectCompartment=Select Compartment",
    "SelectDatabase=Select Database"
})
public class AddADBAction implements ActionListener {
    private static final Logger LOGGER = Logger.getLogger(AddADBAction.class.getName());
    

    @Override
    public void actionPerformed(ActionEvent e) {
        List<TenancyItem> tenancies = new ArrayList<>();
        for (OCIProfile p : OCIManager.getDefault().getConnectedProfiles()) {
           p.getTenancy().ifPresent(tenancies::add);
        }
        Optional<TenancyItem> selectedTenancy = chooseOneItem(tenancies, Bundle.SelectTenancy());
        
        Optional<CompartmentItem> selectedCompartment = Optional.empty();
                
        if (!selectedTenancy.isPresent()) {
            return;
        }
        
        List<CompartmentItem> compartments = CompartmentNode.getCompartments().apply(selectedTenancy.get());
        selectedCompartment = chooseOneItem(compartments, Bundle.SelectCompartment());
        DatabaseItem selectedDatabase = null;
        
        if (selectedCompartment.isPresent()) {
            while(selectedDatabase == null) {
                OCIItem item = chooseCopartmentOrDb(selectedCompartment.get());
                if (item == null) {
                    return;
                }
                if (item instanceof DatabaseItem) {
                    selectedDatabase = (DatabaseItem) item;
                }
                if (item instanceof CompartmentItem) {
                    selectedCompartment = Optional.of((CompartmentItem) item);
                }
            }
        }
        if (selectedDatabase != null) {
            DownloadWalletAction action = new DownloadWalletAction(selectedDatabase);
            action.actionPerformed(null);
        }
    }
    
    private <T extends OCIItem> Optional<T> chooseOneItem(List<T> ociItems, String title) {
        Optional<T> result = Optional.empty();
        if (ociItems.size() == 1) {
            result = Optional.of(ociItems.get(0));
        } else if (ociItems.size() > 0) {
            List<Item> items = ociItems.stream()
                    .map(tenancy -> new Item(tenancy.getName(), tenancy.getDescription()))
                    .collect(Collectors.toList());
            NotifyDescriptor.QuickPick qp = new NotifyDescriptor.QuickPick(title, title, items, false);
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(qp)) {
                Optional<String> selected = qp.getItems().stream().filter(item -> item.isSelected()).map(item -> item.getLabel()).findFirst();
                if (selected.isPresent()) {
                    result = ociItems.stream().filter(t -> t.getName().equals(selected.get())).findFirst();
                }
                
            }
        } 
        return result;
    }
    
    
    private OCIItem chooseCopartmentOrDb(CompartmentItem compartment) {
        List<OCIItem> items = new ArrayList<> ();
        try {
            items.addAll(DatabaseNode.getDatabases().apply(compartment));
        } catch (BmcException e) {
            LOGGER.log(Level.SEVERE, "Unable to load compartment list", e); // NOI18N
        }
        items.addAll(CompartmentNode.getCompartments().apply(compartment));
        return chooseOneItem(items, Bundle.SelectDatabase()).orElseGet(() -> null);
    }
    
}
