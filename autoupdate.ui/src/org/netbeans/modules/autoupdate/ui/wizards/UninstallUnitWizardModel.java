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

import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.modules.autoupdate.ui.Containers;

/**
 *
 * @author Jiri Rechtacek
 */
public class UninstallUnitWizardModel extends OperationWizardModel {
    private OperationType operationType;
    private OperationContainer container;
    private OperationContainer<OperationSupport> customContainer;
    
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
                break;
            case DISABLE :
                this.container = Containers.forDisable ();
                break;
            default:
                assert false;
        }

        assert container!=null ;
    }
    
    public OperationType getOperation () {
        return operationType;
    }
    
    public OperationContainer getBaseContainer () {
        return container;
    }
    
    public OperationContainer<OperationSupport> getCustomHandledContainer () {
        return customContainer;
    }
    
}
