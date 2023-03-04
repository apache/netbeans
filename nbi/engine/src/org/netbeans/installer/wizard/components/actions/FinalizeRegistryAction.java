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
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Kirill Sorokin
 */
public class FinalizeRegistryAction extends WizardAction {
    public FinalizeRegistryAction() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        setProperty(REGISTRY_FINALIZATION_FAILED_PROPERTY,
                DEFAULT_REGISTRY_FINALIZATION_FAILED_MESSAGE);
    }
    
    public void execute() {
        try {
            Registry.getInstance().finalizeRegistry(new Progress());
        } catch (FinalizationException e) {
            ErrorManager.notifyError(StringUtils.format(
                    getProperty(REGISTRY_FINALIZATION_FAILED_PROPERTY)), e);
        }
    }
    
    public WizardActionUi getWizardUi() {
        return null; // this action does not have a ui
    }
    
    @Override
    public boolean isCancelable() {
        return false;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            FinalizeRegistryAction.class,
            "FRA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            FinalizeRegistryAction.class,
            "FRA.description"); // NOI18N
    public static final String DEFAULT_REGISTRY_FINALIZATION_FAILED_MESSAGE =
            ResourceUtils.getString(
            FinalizeRegistryAction.class,
            "FRA.registry.finalization.failed"); // NOI18N
    public static final String REGISTRY_FINALIZATION_FAILED_PROPERTY =
            "registry.finalization.failed"; // NOI18N
}
