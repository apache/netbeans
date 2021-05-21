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

package org.netbeans.modules.autoupdate.ui.wizards;

import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.modules.autoupdate.ui.Containers;
import org.openide.WizardDescriptor;

/**
 *
 * @author Jiri Rechtacek
 */
public class UninstallUnitWizardModel extends OperationWizardModel {
    private OperationType operationType;
    /**
     * The enable/disable/uninstall operation
     */
    private OperationContainer  container;
    /**
     * The nested install operation
     */
    private OperationContainer<InstallSupport>  installContainer;
    private OperationContainer<OperationSupport> customContainer;
    
    /**
     * Supplemental model that serves for implied install process
     */
    private InstallUnitWizardModel nestedInstall;
    
    /** 
     @param doAction if is null it means doUninstall, false means doDisable, true means doEnable
     */
    public UninstallUnitWizardModel (OperationType doOperation) {
        this.operationType = doOperation;
        this.customContainer = Containers.forCustomUninstall ();
        switch (operationType) {
            case UNINSTALL :
                this.container = Containers.forUninstall ();
                break;
            case ENABLE :
                this.container = Containers.forEnable ();
                this.installContainer = Containers.forAvailable();
                break;
            case DISABLE :
                this.container = Containers.forDisable ();
                break;
            default:
                assert false;
        }

        assert container!=null ;
    }

    @Override
    public void recognizeButtons(WizardDescriptor wd) {
        super.recognizeButtons(wd);
        if (nestedInstall != null) {
            nestedInstall.recognizeButtons(wd);
        }
    }
    
    /**
     * Creates a delegating Install model. The model should satisfy 'install'
     * steps and delegate to this model and/or override necessary operations
     * @return 
     */
    public InstallUnitWizardModel createInstallModel() {
        if (nestedInstall == null) {
            nestedInstall = new InstallUnitWizardModel(OperationType.INSTALL, getInstallContainer()) {
                @Override
                public void modifyOptionsForEndInstall(WizardDescriptor wd) {
                    modifyOptionsForContinue(wd, false);
                    modifyOptionsContinueWithCancel(wd);
                }
                
                @Override
                protected void performRefresh() {
                    UninstallUnitWizardModel.this.performRefresh();
                }
                
                
            };
        }
        return nestedInstall;
    }
    
    public OperationType getOperation () {
        return operationType;
    }
    
    public OperationContainer getBaseContainer () {
        return container;
    }
    
    @Override
    public OperationContainer<InstallSupport> getInstallContainer() {
        return installContainer;
    }
    
    @Override
    public OperationContainer<OperationSupport> getCustomHandledContainer () {
        return customContainer;
    }
    
    /**
     * Resets the containers. Will reset the main container and install container.
     * @param c the new container
     */
    @Override
    protected void refresh(OperationContainer c) {
        if (container != c) {
            this.container = c;
        }
        if (installContainer != null) {
            installContainer.removeAll();
        }
    }
    
}
