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
package org.netbeans.installer.wizard.components.sequences;

import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.helper.ExecutionMode;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.wizard.components.WizardSequence;
import org.netbeans.installer.wizard.components.actions.CreateBundleAction;
import org.netbeans.installer.wizard.components.actions.CreateMacOSAppLauncherAction;
import org.netbeans.installer.wizard.components.actions.CreateNativeLauncherAction;
import org.netbeans.installer.wizard.components.actions.DownloadConfigurationLogicAction;
import org.netbeans.installer.wizard.components.actions.DownloadInstallationDataAction;
import org.netbeans.installer.wizard.components.panels.ComponentsSelectionPanel;
import org.netbeans.installer.wizard.components.panels.PostCreateBundleSummaryPanel;
import org.netbeans.installer.wizard.components.panels.PreCreateBundleSummaryPanel;

/**
 *
 * @author Dmitry Lipin
 */
public class CreateBundleSequence extends WizardSequence {
    private DownloadConfigurationLogicAction downloadConfigurationLogicAction;
    private DownloadInstallationDataAction downloadInstallationDataAction;
    private PreCreateBundleSummaryPanel preCreateBundleSummaryPanel;
    private CreateBundleAction createBundleAction;
    private CreateNativeLauncherAction createNativeLauncherAction;
    private CreateMacOSAppLauncherAction createAppLauncherAction;
    private PostCreateBundleSummaryPanel postCreateBundleSummaryPanel;

    public CreateBundleSequence() {
        downloadConfigurationLogicAction = new DownloadConfigurationLogicAction();
        downloadInstallationDataAction = new DownloadInstallationDataAction();
        preCreateBundleSummaryPanel = new PreCreateBundleSummaryPanel();
        createBundleAction = new CreateBundleAction();
        createNativeLauncherAction = new CreateNativeLauncherAction();
        createAppLauncherAction = new CreateMacOSAppLauncherAction();
        postCreateBundleSummaryPanel = new PostCreateBundleSummaryPanel();
    }

    @Override
    public void executeForward() {
        final Registry registry = Registry.getInstance();

        // remove all current children (if there are any), as the components
        // selection has probably changed and we need to rebuild from scratch
        getChildren().clear();

        // we're creating a bundle - we only need to download and package things
        addChild(preCreateBundleSummaryPanel);
        addChild(downloadConfigurationLogicAction);
        addChild(downloadInstallationDataAction);
        addChild(createBundleAction);

        if (registry.getTargetPlatform().isCompatibleWith(Platform.MACOSX)) {
            addChild(createAppLauncherAction);
        } else {
            addChild(createNativeLauncherAction);
        }
        addChild(postCreateBundleSummaryPanel);

        super.executeForward();
    }
    
    @Override
    public boolean canExecuteForward() {
        return ExecutionMode.CREATE_BUNDLE == ExecutionMode.getCurrentExecutionMode();
    }    
}
