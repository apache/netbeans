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

import java.io.File;
import java.net.URI;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.*;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.UiMode;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 
 */
public class NbShowUninstallationSurveyAction extends WizardAction {

    @Override
    public void execute() {
        try {
            LogManager.logEntry("... execute show uninstallation survey action");
            for (Product product : Registry.getInstance().getProducts("nb-base")) {
                if (product.getStatus() == Status.NOT_INSTALLED && product.hasStatusChanged()) {
                    String startTime = product.getProperty("installation.timestamp");
                    String endTime = product.getProperty("uninstallation.timestamp");
                    if (startTime != null && endTime != null) {
                        LogManager.log("... " + product.getLogic().getSystemDisplayName() + " was uninstalled");
                        Long lifetime = new Long(endTime) - new Long(startTime);
                        LogManager.log("... lifetime (milliseconds) : " + lifetime);
                        if(lifetime < 0) {
                            LogManager.log("... time is less than zero (?), skipping the product");
                            continue;
                        }                        
                        Long days = 1 + (lifetime / (1000L * 60L * 60L * 24L));
                        LogManager.log("... lifetime (days, rounded up) : " + days);
                        final String version =
                                product.getVersion().getMajor() +
                                StringUtils.DOT +
                                product.getVersion().getMinor();

                        LogManager.log("... version : " + version);
                        final File superId = getSuperId();
                        String id = "";
                        if (FileUtils.exists(superId)) {
                            id =  FileUtils.readStringList(superId).get(0);
                        }
                        
                        String url = StringUtils.format(UNINSTALLATION_SURVEY_URL, version, id, days.toString());
                        LogManager.log("... URL  : " + url);
                        if(BrowserUtils.isBrowseSupported()) {
                            BrowserUtils.openBrowser(new URI(url));
                        } else {
                            LogManager.log("... browser is not supported");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LogManager.log(ex);
        } finally {
            LogManager.logExit("... finished show uninstallation survey action");
        }
    }
    
    private File getSuperId() {
        // 1. check new OS specific place
        File superId = new File(SystemUtils.getDefaultUserdirRoot() + File.separator + ".superId");
        // 2. if doesn't exist => use former place
        if (! superId.exists()) {
            superId = new File(SystemUtils.getUserHomeDirectory(), ".netbeans" + File.separator + ".superId");
        }
        return superId;
    }

    @Override
    public boolean canExecuteForward() {
        return UiMode.getCurrentUiMode() != UiMode.SILENT && Boolean.getBoolean(SHOW_UNINSTALLATION_SURVEY_PROPERTY);
    }

    @Override
    public WizardActionUi getWizardUi() {
        return null;
    }
    public static final String UNINSTALLATION_SURVEY_URL =
            "http://quality.netbeans.org/survey/uninstall?version={0}&id={1}&life={2}";//NOI18N
    public static final String SHOW_UNINSTALLATION_SURVEY_PROPERTY = 
            "show.uninstallation.survey";
}

