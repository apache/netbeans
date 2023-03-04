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
package org.netbeans.modules.cloud.oracle.actions;

import com.oracle.bmc.model.BmcException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.netbeans.modules.cloud.oracle.actions.DownloadWalletDialog.WalletInfo;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentNode;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.database.DatabaseNode;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.items.TenancyItem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.ComposedInput.Callback;
import org.openide.NotifyDescriptor.QuickPick;
import org.openide.NotifyDescriptor.QuickPick.Item;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
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
    "SelectDatabase=Select Compartment or Database",
    "EnterUsername=Enter Username",
    "EnterPassword=Enter Password"
})
public class AddADBAction implements ActionListener {
    private static final Logger LOGGER = Logger.getLogger(AddADBAction.class.getName());
    
    private static final String DB = "db"; //NOI18N
    private static final String USERNAME = "username"; //NOI18N
    private static final String PASSWORD = "password"; //NOI18N

    @Override
    public void actionPerformed(ActionEvent e) {
        Map<String, Object> result = new HashMap<> ();
        
        NotifyDescriptor.ComposedInput ci = new NotifyDescriptor.ComposedInput(Bundle.AddADB(), 3, new Callback() {
            Map<Integer, List> values = new HashMap<> ();
            
            @Override
            public NotifyDescriptor createInput(NotifyDescriptor.ComposedInput input, int number) {
                if (number == 1) {
                    List<TenancyItem> tenancies = new ArrayList<>();
                    for (OCIProfile p : OCIManager.getDefault().getConnectedProfiles()) {
                        p.getTenancy().ifPresent(tenancies::add);
                    }
                    String title;
                    if (tenancies.size() == 1) {
                        values.put(1, getCompartmentsAndDbs(tenancies.get(0)));
                        title = Bundle.SelectCompartment();
                    } else {
                        values.put(1, tenancies);
                        title = Bundle.SelectTenancy();
                    }
                    return createQuickPick(values.get(1), title);
                } else {
                    NotifyDescriptor prev = input.getInputs()[number - 2];
                    OCIItem prevItem = null;
                    if (prev instanceof NotifyDescriptor.QuickPick) {
                        Optional<String> selected = ((QuickPick) prev).getItems().stream().filter(item -> item.isSelected()).map(item -> item.getLabel()).findFirst();
                        if (selected.isPresent()) {
                            Optional<? extends OCIItem> ti = values.get(number - 1).stream().filter(t -> ((OCIItem) t).getName().equals(selected.get())).findFirst();
                            if (ti.isPresent()) {
                                prevItem = ti.get();
                            }
                        }
                        if (prevItem instanceof DatabaseItem) {
                            result.put(DB, prevItem);
                            return new NotifyDescriptor.InputLine(Bundle.EnterUsername(), Bundle.EnterUsername());
                        }
                        values.put(number, getCompartmentsAndDbs(prevItem));
                        input.setEstimatedNumberOfInputs(input.getEstimatedNumberOfInputs() + 1);
                        return createQuickPick(values.get(number), Bundle.SelectDatabase());
                    } else if (prev instanceof NotifyDescriptor.PasswordLine) {
                        result.put(PASSWORD, ((NotifyDescriptor.PasswordLine) prev).getInputText());
                        return null;
                    } else if (prev instanceof NotifyDescriptor.InputLine) {
                        String username = ((NotifyDescriptor.InputLine) prev).getInputText();
                        if (username == null || username.trim().isEmpty()) {
                            return prev;
                        }
                        result.put(USERNAME, username);
                        return new NotifyDescriptor.PasswordLine(Bundle.EnterPassword(), Bundle.EnterPassword());
                    }
                    return null;
                }
            }
            
        });
        if (DialogDescriptor.OK_OPTION ==  DialogDisplayer.getDefault().notify(ci)) {
            try {
                DatabaseItem selectedDatabase = (DatabaseItem) result.get(DB);
                DownloadWalletAction action = new DownloadWalletAction(selectedDatabase);
                WalletInfo info = new WalletInfo(
                        DownloadWalletDialog.getWalletsDir().getAbsolutePath(),
                        AbstractPasswordPanel.generatePassword(),
                        (String) result.get(USERNAME),
                        ((String) result.get(PASSWORD)).toCharArray());
                action.addConnection(info);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private <T extends OCIItem> NotifyDescriptor.QuickPick createQuickPick(List<T> ociItems, String title) {
        
        List<Item> items = ociItems.stream()
                .map(tenancy -> new Item(tenancy.getName(), tenancy.getDescription()))
                .collect(Collectors.toList());
        return new NotifyDescriptor.QuickPick(title, title, items, false);
    }
    
    private List<OCIItem> getCompartmentsAndDbs(OCIItem parent) {
        List<OCIItem> items = new ArrayList<> ();
        try {
            if (parent instanceof CompartmentItem) {
                items.addAll(DatabaseNode.getDatabases().apply((CompartmentItem) parent));
            }
        } catch (BmcException e) {
            LOGGER.log(Level.SEVERE, "Unable to load compartment list", e); // NOI18N
        }
        items.addAll(CompartmentNode.getCompartments().apply(parent));
        return items;
    }
    
}
