/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
