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
package org.mycompany.installer.wizard.components.sequences;

import org.netbeans.installer.wizard.components.sequences.ProductWizardSequence;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mycompany.installer.wizard.components.panels.PostInstallSummaryPanel;
import org.mycompany.installer.wizard.components.panels.PreInstallSummaryPanel;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.ExecutionMode;
import org.netbeans.installer.wizard.components.WizardSequence;
import org.netbeans.installer.wizard.components.actions.DownloadConfigurationLogicAction;
import org.netbeans.installer.wizard.components.actions.DownloadInstallationDataAction;
import org.netbeans.installer.wizard.components.actions.InstallAction;
import org.netbeans.installer.wizard.components.actions.UninstallAction;
import org.netbeans.installer.wizard.components.panels.LicensesPanel;

public class MainSequence extends WizardSequence {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance

    private DownloadConfigurationLogicAction downloadConfigurationLogicAction;
    private LicensesPanel licensesPanel;
    private PreInstallSummaryPanel preInstallSummaryPanel;
    private UninstallAction uninstallAction;
    private DownloadInstallationDataAction downloadInstallationDataAction;
    private InstallAction installAction;
    private PostInstallSummaryPanel postInstallSummaryPanel;
    private Map<Product, ProductWizardSequence> productSequences;

    public MainSequence() {
        downloadConfigurationLogicAction = new DownloadConfigurationLogicAction();
        licensesPanel = new LicensesPanel();
        preInstallSummaryPanel = new PreInstallSummaryPanel();
        uninstallAction = new UninstallAction();
        downloadInstallationDataAction = new DownloadInstallationDataAction();
        installAction = new InstallAction();
        postInstallSummaryPanel = new PostInstallSummaryPanel();
        productSequences = new HashMap<Product, ProductWizardSequence>();

        installAction.setProperty(InstallAction.TITLE_PROPERTY,
                DEFAULT_IA_TITLE);
        installAction.setProperty(InstallAction.DESCRIPTION_PROPERTY,
                DEFAULT_IA_DESCRIPTION);
    }
    
    @Override
    public void executeForward() {
        final Registry registry = Registry.getInstance();
        final List<Product> toInstall = registry.getProductsToInstall();
        final List<Product> toUninstall = registry.getProductsToUninstall();

        // remove all current children (if there are any), as the components
        // selection has probably changed and we need to rebuild from scratch
        getChildren().clear();

        // the set of wizard components differs greatly depending on the execution
        // mode - if we're installing, we ask for input, run a wizard sequence for
        // each selected component and then download and install; if we're creating
        // a bundle, we only need to download and package things

        if (toInstall.size() > 0) {
            addChild(downloadConfigurationLogicAction);
            addChild(licensesPanel);

            for (Product product : toInstall) {
                if (!productSequences.containsKey(product)) {
                    productSequences.put(
                            product,
                            new ProductWizardSequence(product));
                }

                addChild(productSequences.get(product));
            }
        }

        addChild(preInstallSummaryPanel);

        if (toUninstall.size() > 0) {
            addChild(uninstallAction);
        }

        if (toInstall.size() > 0) {
            addChild(downloadInstallationDataAction);
            addChild(installAction);            
        }

        addChild(postInstallSummaryPanel);
        
        super.executeForward();
    }

    @Override
    public boolean canExecuteForward() {
        return ExecutionMode.NORMAL == ExecutionMode.getCurrentExecutionMode() &&
                (Registry.getInstance().getProductsToInstall().size() > 0 ||
                Registry.getInstance().getProductsToUninstall().size() > 0);
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_IA_TITLE =
            ResourceUtils.getString(
            MainSequence.class,
            "MS.IA.title"); // NOI18N
    public static final String DEFAULT_IA_DESCRIPTION =
            ResourceUtils.getString(
            MainSequence.class,
            "MS.IA.description"); // NOI18N   
    
}
