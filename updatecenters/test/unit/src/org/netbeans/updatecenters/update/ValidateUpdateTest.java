/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.updatecenters.update;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.updatecenters.OperationUtils;

/**
 *
 * @author Jaromir.Uhrik@Sun.Com
 */
public class ValidateUpdateTest extends NbTestCase {

    private static final String TEST_INSTALLED_PLUGINS_EMPTY = "testInstalledPluginsEmpty";
    public static List<UpdateElement> newPlugins = null;
    public static List<UpdateElement> installedPlugins = null;
    public static List<UpdateElement> updatePlugins = null;
    private static List<UpdateUnit> availableUnits = null;
    private String currentElementToUpdate = null;

    public ValidateUpdateTest(String testName, String moduleName) {
        super(testName);
        this.currentElementToUpdate = moduleName;
    }

    public static void initLists() {
        installedPlugins = null;
        installedPlugins = new ArrayList<UpdateElement>();
        newPlugins = null;
        newPlugins = new ArrayList<UpdateElement>();
        updatePlugins = null;
        updatePlugins = new ArrayList<UpdateElement>();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void runTest() throws Throwable {

        if (currentElementToUpdate == null) {
            if (getName().equals(TEST_INSTALLED_PLUGINS_EMPTY)) {
                OperationUtils.logProperties(this);
                OperationUtils.logRegisteredUCs(true, this);
                logCurrentValues();
                assertFalse("The list of installed plugins is empty!", installedPlugins.isEmpty());
            } else {
                fail("Specified plugin '" + getName().substring(OperationUtils.INVALID_FILTER_PREFIX.length()) + "' is not available - maybe the filter is wrong");
            }
        } else {
            logCurrentValues();
            for (UpdateElement updateElement : newPlugins) {
                if (updateElement.getCodeName().equals(currentElementToUpdate)) {
                    log("[" + updateElement.getDisplayName() + "] " + updateElement.getCodeName() + " is available as new plugin");
                    fail("[" + updateElement.getDisplayName() + "] " + updateElement.getCodeName() + " has not been updated!");
                }
            }
            for (UpdateElement updateElement : installedPlugins) {
                if (updateElement.getCodeName().equals(currentElementToUpdate)) {
                    log("[" + updateElement.getDisplayName() + "] " + updateElement.getCodeName() + " is installed plugin");
                }
            }
            for (UpdateElement updateElement : updatePlugins) {
                if (updateElement.getCodeName().equals(currentElementToUpdate)) {
                    log("[" + updateElement.getDisplayName() + "] " + updateElement.getCodeName() + " is available as update plugin");
                    fail("[" + updateElement.getDisplayName() + "] " + updateElement.getCodeName() + " has not been updated!");
                }
            }
        }
    }


    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        OperationUtils.refreshProviders();
        OperationUtils.disableUCsExcept(OperationUtils.readSavedData(OperationUtils.UC_LIST_FILE_NAME), true, true);

        ArrayList<String> listOfCodeNames = OperationUtils.readSavedData(OperationUtils.UPDATE_DATA_FILE_NAME);

        OperationUtils.setProperUpdateCenters(true, true);
        OperationUtils.refreshProviders();

        initLists();

        availableUnits = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.KIT_MODULE);

        for (UpdateUnit updateUnit : availableUnits) {
            UpdateElement element = updateUnit.getInstalled();
            if (updateUnit.getInstalled() != null) {
                //all plugins that are installed or available updates
                List<UpdateElement> availableUpdates = updateUnit.getAvailableUpdates();
                if (!availableUpdates.isEmpty()) {
                    //updatePlugins
                    updatePlugins.add(availableUpdates.get(0));
                } else {
                    //installedPlugins
                    installedPlugins.add(element);
                }
            } else {
                //newPlugins (available not installed plugins)
                List<UpdateElement> availableUpdates = updateUnit.getAvailableUpdates();
                if (!availableUpdates.isEmpty()) {
                    UpdateElement elementToInstall = availableUpdates.get(0);
                    newPlugins.add(elementToInstall);
                }
            }
        }
//sleep 5 sec
        try {
            Thread.sleep(5000);
        } catch (Exception ec) {
            ec.printStackTrace();
        }


        suite.addTest(new ValidateUpdateTest(TEST_INSTALLED_PLUGINS_EMPTY, null));

        int testNumber = 0;
        for (String moduleCodeName : listOfCodeNames) {
            suite.addTest(new ValidateUpdateTest("test" + OperationUtils.getModuleCanonicalName(getDisplayName(moduleCodeName), testNumber), moduleCodeName));
            testNumber++;
        }
        return suite;
    }

    public static String getDisplayName(String codeName) {
        String foundName = "";
        for (UpdateElement updateElement : newPlugins) {
            if (updateElement.getCodeName().equals(codeName)) {
                foundName = updateElement.getDisplayName();
                return foundName;
            }
        }
        for (UpdateElement updateElement : installedPlugins) {
            if (updateElement.getCodeName().equals(codeName)) {
                foundName = updateElement.getDisplayName();
                return foundName;
            }
        }
        for (UpdateElement updateElement : updatePlugins) {
            if (updateElement.getCodeName().equals(codeName)) {
                foundName = updateElement.getDisplayName();
                return foundName;
            }
        }
        return foundName;

    }

    public void logCurrentValues() {
        log("");
        log("-----------------------------------------------------------");
        log("|  PLUGINS COUNTS IN DETAIL :                             |");
        log("-----------------------------------------------------------");
        log("NEW ELEMENTS:" + newPlugins.size());
        log("INSTALLED ELEMENTS:" + installedPlugins.size());
        log("UPDATE ELEMENTS:" + updatePlugins.size());
        log("-----------------------------------------------------------");
        log("-----------------------------------------------------------");
        log("|  PLUGINS AVAILABLE FOR UPDATE :                         |");
        log("-----------------------------------------------------------");
        for (UpdateElement updateElement : updatePlugins) {
            log("[" + updateElement.getDisplayName() + "] " + updateElement.getCodeName());
        }
        log("-----------------------------------------------------------");
    }
}
