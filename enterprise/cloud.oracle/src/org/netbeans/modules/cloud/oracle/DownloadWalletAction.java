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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.DownloadWalletAction"
)
@ActionRegistration( 
        displayName = "#CTL_DownloadWalletAction", 
        asynchronous = true
)

@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/Databases/Actions", position = 250)
})
@NbBundle.Messages({
    "LBL_SaveWallet=Save DB Wallet",
    "CTL_DownloadWalletAction=Download Wallet",
    "MSG_WalletDownloaded=Database Wallet was downloaded to {0}"
    
})
public class DownloadWalletAction implements ActionListener {
    
    private final OCIItem context;

    public DownloadWalletAction(OCIItem context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Optional<Pair<String, char[]>> result = DownloadWalletDialog.showDialog(context);
        result.ifPresent((p) -> {
            try {
                Path walletPath = OCIManager.getDefault().downloadWallet(context, new String(p.second()), p.first());
                StatusDisplayer.getDefault().setStatusText(Bundle.MSG_WalletDownloaded(walletPath.toString()));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }
    
}
