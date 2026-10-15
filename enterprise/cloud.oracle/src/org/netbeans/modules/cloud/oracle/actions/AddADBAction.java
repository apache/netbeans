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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.netbeans.modules.cloud.oracle.actions.DownloadWalletDialog.WalletInfo;
import org.netbeans.modules.cloud.oracle.assets.CloudAssets;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.steps.CompartmentStep;
import org.netbeans.modules.cloud.oracle.steps.PasswordStep;
import org.netbeans.modules.cloud.oracle.steps.ReferenceNameStep;
import org.netbeans.modules.cloud.oracle.steps.SuggestedStep;
import org.netbeans.modules.cloud.oracle.steps.TenancyStep;
import org.netbeans.modules.cloud.oracle.steps.UsernameStep;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

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

@NbBundle.Messages({
    "AddADB=Add Oracle Autonomous DB"
})
public class AddADBAction implements ActionListener {

    private static final RequestProcessor RP = new RequestProcessor(AddADBAction.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        addADB();
    }
    
    public CompletableFuture<DatabaseItem> addADB() {
        CompletableFuture future = new CompletableFuture();
        boolean showSetRefNameStep = CloudAssets.getDefault().itemExistWithoutReferanceName(DatabaseItem.class);
        Steps.NextStepProvider nsProvider = Steps.NextStepProvider.builder()
                .stepForClass(TenancyStep.class, (s) -> new CompartmentStep())
                .stepForClass(CompartmentStep.class, (s) -> new SuggestedStep("Database"))
                .stepForClass(SuggestedStep.class, (s) -> new UsernameStep())
                .stepForClass(UsernameStep.class, (s) -> new PasswordStep(null, (String) s.getValue()))
                .stepForClass(PasswordStep.class, (s) -> showSetRefNameStep ? new ReferenceNameStep("Database") : null)
                .build();
        
        Steps.getDefault()
                .executeMultistep(new TenancyStep(), Lookups.fixed(nsProvider))
                .thenAccept(values -> {
                    String username = values.getValueForStep(UsernameStep.class);
                    String password = values.getValueForStep(PasswordStep.class);
                    DatabaseItem databaseItem = (DatabaseItem) values.getValueForStep(SuggestedStep.class);
                    String referenceName = values.getValueForStep(ReferenceNameStep.class);
                    if (showSetRefNameStep && referenceName == null) {
                        future.completeExceptionally(new IllegalArgumentException("Reference name not set"));
                        return;
                    }
                    if (referenceName != null) {
                        CloudAssets.getDefault().setReferenceName(databaseItem, referenceName);  
                    }
                    downloadWallet(databaseItem, username, password, future);
                });
        
        return future;
    }
    
    private void downloadWallet(DatabaseItem databaseItem, String username, String password, CompletableFuture future) {
        try {
            DownloadWalletAction action = new DownloadWalletAction(databaseItem);
            WalletInfo info = new WalletInfo(
                    DownloadWalletDialog.getWalletsDir().getAbsolutePath(),
                    AbstractPasswordPanel.generatePassword(),
                    username,
                    password.toCharArray(),
                    databaseItem);
            RP.post(() -> {
                action.addConnection(info);
                future.complete(databaseItem);
            });
        } catch (IOException ex) {
            future.completeExceptionally(ex);
        }
    }
}
