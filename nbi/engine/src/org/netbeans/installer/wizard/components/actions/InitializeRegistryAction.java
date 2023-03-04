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

package org.netbeans.installer.wizard.components.actions;

import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Kirill Sorokin
 */
public class InitializeRegistryAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public InitializeRegistryAction() {
        setProperty(TITLE_PROPERTY, 
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, 
                DEFAULT_DESCRIPTION);
        setProperty(REGISTRY_INITIALIZATION_FAILED_PROPERTY,
                DEFAULT_REGISTRY_INITIALIZATION_FAILED_MESSAGE);
    }
    
    public void execute() {
        try {
            final Progress progress = new Progress();
            
            getWizardUi().setProgress(progress);
            Registry.getInstance().initializeRegistry(progress);
        } catch (InitializationException e) {
            ErrorManager.notifyError(
                    StringUtils.format(
                    getProperty(REGISTRY_INITIALIZATION_FAILED_PROPERTY)), e);
        }
    }
    
    @Override
    public boolean isCancelable() {
        return false;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            InitializeRegistryAction.class, 
            "IRA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            InitializeRegistryAction.class, 
            "IRA.description"); // NOI18N
    public static final String DEFAULT_REGISTRY_INITIALIZATION_FAILED_MESSAGE = 
            ResourceUtils.getString(
            InitializeRegistryAction.class, 
            "IRA.registry.initialization.failed"); // NOI18N    
    public static final String REGISTRY_INITIALIZATION_FAILED_PROPERTY =             
            "registry.initialization.failed"; // NOI18N    
    
}
