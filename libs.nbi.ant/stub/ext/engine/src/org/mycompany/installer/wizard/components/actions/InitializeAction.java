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
package org.mycompany.installer.wizard.components.actions;

import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;
import org.netbeans.installer.wizard.components.actions.*;

public class InitializeAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public InitializeAction() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);

        downloadLogic = new DownloadConfigurationLogicAction();
        initReg = new InitializeRegistryAction();
    }
    private DownloadConfigurationLogicAction downloadLogic;
    private InitializeRegistryAction initReg;
    
    public void execute() {
        final Progress progress = new Progress();
        
        //getWizardUi().setProgress(progress);
        

        progress.setTitle(getProperty(PROGRESS_TITLE_PROPERTY));

        //progress.synchronizeDetails(false);

        if (initReg.canExecuteForward()) {
            initReg.setWizard(getWizard());
            initReg.execute();
        }
    
        if (downloadLogic.canExecuteForward()) {
            downloadLogic.setWizard(getWizard());
            downloadLogic.execute();
        }
    }
    
    @Override
    public boolean isCancelable() {
        return false;
    }

    public WizardActionUi getWizardUi() {
        return null; // this action does not have a ui
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            InitializeAction.class, 
            "IA.title"); // NOI18N
    public static final String PROGRESS_TITLE_PROPERTY = ResourceUtils.getString(
            InitializeAction.class, 
            "IA.progress.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            InitializeAction.class, 
            "IA.description"); // NOI18N
    
}
