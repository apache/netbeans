/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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

