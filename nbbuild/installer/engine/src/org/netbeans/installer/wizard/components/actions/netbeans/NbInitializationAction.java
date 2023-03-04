/**
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
package org.netbeans.installer.wizard.components.actions.netbeans;

import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ExecutionMode;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.actions.DownloadConfigurationLogicAction;
import org.netbeans.installer.wizard.components.actions.InitializeRegistryAction;
import org.netbeans.installer.wizard.components.actions.SearchForJavaAction;

/**
 *
 
 */
public class NbInitializationAction extends WizardAction {
    
    private InitializeRegistryAction initReg;
    private DownloadConfigurationLogicAction downloadLogic;
    private SearchForJavaAction searchJava;
    private WizardAction currentAction;
    
    public NbInitializationAction() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        initReg = new InitializeRegistryAction();
        downloadLogic = new DownloadConfigurationLogicAction();
        searchJava = new SearchForJavaAction();
    }
    
    public void execute() {
        final CompositeProgress progress = new CompositeProgress(this.getWizardUi());
        progress.setTitle(getProperty(TITLE_PROPERTY));
        progress.setPercentage(10);
        progress.synchronizeDetails(false);
        if (initReg.canExecuteForward()) {
            currentAction = initReg;
            initReg.setWizard(getWizard());
            initReg.execute();
        }
        
        if (downloadLogic.canExecuteForward()) {
            currentAction = downloadLogic;
            downloadLogic.setWizard(getWizard());
            downloadLogic.execute();
        }
        
        if (searchJava.canExecuteForward() &&
                ExecutionMode.getCurrentExecutionMode() == ExecutionMode.NORMAL) {
            boolean doSearch = false;
            List<Product> toInstall = Registry.getInstance().getProductsToInstall();
            for (Product product : toInstall) {
                try {
                    if(product.getUid().equals("jdk")) {
                        if(JavaUtils.findJDKHome(product.getVersion())==null) {                            
                            try {
                                if(!SystemUtils.isWindows() || SystemUtils.isCurrentUserAdmin()) {
                                    doSearch = false;
                                    break;
                                }
                            } catch (NativeException e) {
                                LogManager.log(e);
                            }
                        }
                    } else if (product.getUid().equals("jre-nested")) {
                        //SearchForJavaAction.addJavaLocation(product.getInstallationLocation());
                        doSearch = false;
                        break;
                    } else {
                        for (WizardComponent component : product.getLogic().getWizardComponents()) {
                            if (component instanceof SearchForJavaAction) {
                                doSearch = true;
                                break;
                            }
                        }
                    }
                } catch (InitializationException e) {
                    LogManager.log(e);
                }
            }
            if (doSearch) {
                currentAction = searchJava;
                Progress javaSearchProgress = new Progress();
                progress.addChild(javaSearchProgress, 90);
                searchJava.setWizard(getWizard());
                searchJava.execute(javaSearchProgress);
            }
        }
    }
    public static final String DEFAULT_TITLE = ResourceUtils.getString(NbInitializationAction.class,
            "NIA.title"); // NOI18N
    
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(NbInitializationAction.class,
            "NIA.description"); // NOI18N*/
    
}
