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

import com.oracle.bmc.identity.model.Tenancy;
import com.oracle.bmc.model.BmcException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.progress.ProgressHandle;
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
    "SelectProfile=Select OCI Profile",
    "# {0} - tenancy name",
    "# {1} - region id",
    "SelectProfile_Description={0} (region: {1})",
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

    @NbBundle.Messages({
        "MSG_CollectingProfiles=Searching for OCI Profiles",
        "MSG_CollectingProfiles_Text=Loading OCI Profiles",
        "MSG_CollectingItems=Loading OCI contents",
        "MSG_CollectingItems_Text=Listing compartments and databases",
    })
    @Override
    public void actionPerformed(ActionEvent e) {
        Map<String, Object> result = new HashMap<> ();
        
        NotifyDescriptor.ComposedInput ci = new NotifyDescriptor.ComposedInput(Bundle.AddADB(), 3, new Callback() {
            Map<Integer, Map> values = new HashMap<> ();

            @Override
            public NotifyDescriptor createInput(NotifyDescriptor.ComposedInput input, int number) {
                if (number == 1) {
                    ProgressHandle h = ProgressHandle.createHandle(Bundle.MSG_CollectingProfiles());
                    h.start();
                    h.progress(Bundle.MSG_CollectingProfiles_Text());
        
                    Map<OCIProfile, Tenancy> profiles = new LinkedHashMap<>();
                    Map<String, TenancyItem> tenancyItems = new LinkedHashMap<>();
                    try {
                        for (OCIProfile p : OCIManager.getDefault().getConnectedProfiles()) {
                            TenancyItem t = p.getTenancy().orElse(null);
                            if (t != null) {
                                Tenancy data = p.getTenancyData();
                                profiles.put(p, data);
                                tenancyItems.put(p.getId(), t);
                            }
                        }
                    } finally {
                        h.finish();
                    }
                    String title;
                    if (profiles.size() == 1) {
                        values.put(1, getCompartmentsAndDbs(profiles.keySet().iterator().next().getTenancy().get()));
                        title = Bundle.SelectCompartment();
                        return createQuickPick(values.get(1), title);
                    } else {
                        title = Bundle.SelectProfile();
                        List<Item> items = new ArrayList<>(profiles.size());
                        for (OCIProfile p : profiles.keySet()) {
                            Tenancy t = profiles.get(p);
                            items.add(new Item(p.getId(), Bundle.SelectProfile_Description(t.getName(), t.getHomeRegionKey())));
                        }
                        values.put(1, tenancyItems);
                        return new NotifyDescriptor.QuickPick(title, title, items, false);
                    }
                } else {
                    NotifyDescriptor prev = input.getInputs()[number - 2];
                    OCIItem prevItem = null;
                    if (prev instanceof NotifyDescriptor.QuickPick) {
                        for (QuickPick.Item item : ((QuickPick)prev).getItems()) {
                            if (item.isSelected()) {
                                prevItem = (OCIItem)values.get(number - 1).get(item.getLabel());
                                break;
                            }
                        }
                        if (prevItem == null) {
                            return null;
                        }
                        if (prevItem instanceof DatabaseItem) {
                            result.put(DB, prevItem);
                            return new NotifyDescriptor.InputLine(Bundle.EnterUsername(), Bundle.EnterUsername());
                        }
                        ProgressHandle h = ProgressHandle.createHandle(Bundle.MSG_CollectingItems());
                        h.start();
                        h.progress(Bundle.MSG_CollectingItems_Text());
                        try {
                            values.put(number, getCompartmentsAndDbs(prevItem));
                            input.setEstimatedNumberOfInputs(input.getEstimatedNumberOfInputs() + 1);
                            return createQuickPick(values.get(number), Bundle.SelectDatabase());
                        } finally {
                            h.finish();
                        }
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
    
    private <T extends OCIItem> NotifyDescriptor.QuickPick createQuickPick(Map<String, T> ociItems, String title) {
        
        List<Item> items = ociItems.values().stream()
                .map(tenancy -> new Item(tenancy.getName(), tenancy.getDescription()))
                .collect(Collectors.toList());
        return new NotifyDescriptor.QuickPick(title, title, items, false);
    }
    
    private Map<String, OCIItem> getCompartmentsAndDbs(OCIItem parent) {
        Map<String, OCIItem> items = new HashMap<> ();
        try {
            if (parent instanceof CompartmentItem) {
                DatabaseNode.getDatabases().apply((CompartmentItem) parent).forEach((db) -> items.put(db.getName(), db));
            }
        } catch (BmcException e) {
            LOGGER.log(Level.SEVERE, "Unable to load compartment list", e); // NOI18N
        }
        CompartmentNode.getCompartments().apply(parent).forEach(c -> items.put(c.getName(), c));
        return items;
    }
    
}
