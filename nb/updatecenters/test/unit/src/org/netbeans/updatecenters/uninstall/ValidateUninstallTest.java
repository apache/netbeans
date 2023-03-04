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
package org.netbeans.updatecenters.uninstall;

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
public class ValidateUninstallTest extends NbTestCase {

    private static final String TEST_INSTALLED_PLUGINS_EMPTY = "testInstalledPluginsEmpty";
    public static List<UpdateElement> newPlugins = null;
    public static List<UpdateElement> installedPlugins = null;
    public static List<UpdateElement> updatePlugins = null;
    private static List<UpdateUnit> availableUnits = null;
    private String currentElementToUninstall = null;

    public ValidateUninstallTest(String testName, String moduleName) {
        super(testName);
        this.currentElementToUninstall = moduleName;
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

        if (currentElementToUninstall == null) {
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
                if (updateElement.getCodeName().equals(currentElementToUninstall)) {
                    log("[" + updateElement.getDisplayName() + "] " + updateElement.getCodeName() + " is available as new plugin");
                }
            }
            for (UpdateElement updateElement : installedPlugins) {
                if (updateElement.getCodeName().equals(currentElementToUninstall)) {
                    log("[" + updateElement.getDisplayName() + "] " + updateElement.getCodeName() + " is installed plugin");
                    fail("[" + updateElement.getDisplayName() + "] " + updateElement.getCodeName() + " has not been uninstalled!");

                }
            }
            for (UpdateElement updateElement : updatePlugins) {
                if (updateElement.getCodeName().equals(currentElementToUninstall)) {
                    log("[" + updateElement.getDisplayName() + "] " + updateElement.getCodeName() + " is available as update plugin");                    
                }
            }
        }
    }


    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        OperationUtils.refreshProviders();
        OperationUtils.disableUCsExcept(OperationUtils.readSavedData(OperationUtils.UC_LIST_FILE_NAME), true, true);

        ArrayList<String> listOfCodeNames = OperationUtils.readSavedData(OperationUtils.UNINSTALL_DATA_FILE_NAME);

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


        suite.addTest(new ValidateUninstallTest(TEST_INSTALLED_PLUGINS_EMPTY, null));

        int testNumber = 0;
        for (String moduleCodeName : listOfCodeNames) {
            suite.addTest(new ValidateUninstallTest("test" + OperationUtils.getModuleCanonicalName(getDisplayName(moduleCodeName), testNumber), moduleCodeName));
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
        log("|  PLUGINS AVAILABLE FOR UNINSTALL :                      |");
        log("-----------------------------------------------------------");
        for (UpdateElement updateElement : updatePlugins) {
            log("[" + updateElement.getDisplayName() + "] " + updateElement.getCodeName());
        }
        log("-----------------------------------------------------------");
    }
}
