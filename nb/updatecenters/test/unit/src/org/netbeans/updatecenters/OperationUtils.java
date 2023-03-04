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
package org.netbeans.updatecenters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.services.UpdateUnitProviderImpl;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaromir.Uhrik@Sun.Com
 */
public class OperationUtils {

    public static final String INVALID_FILTER_PREFIX = "testInvalidFilter_";
    public static final String INCLUDE_ALL = "*";
    public static final String NO_EXCLUDES = "";
    public static final String TOKEN_SEPARATOR = "||";
    public static final String CUSTOM_PROVIDER_PREFIX = "Custom_Provider_";
    //properties
    public static final String INCLUDE_PLUGINS = "include.plugins";
    public static final String EXCLUDE_PLUGINS = "exclude.plugins";//forced
    public static final String DISABLE_DEFAULT_UC = "disable.default.uc";    
    public static final String ADDITIONAL_UC = "additional.uc";
    public static final String EXTERNAL_WORKSPACE_PROPERTY ="external.workspace";
    //files
    public static final String INSTALL_DATA_FILE_NAME = "installed_list.txt";
    public static final String UPDATE_DATA_FILE_NAME = "updated_list.txt";
    public static final String UNINSTALL_DATA_FILE_NAME = "uninstalled_list.txt";
    public static final String UC_LIST_FILE_NAME = "uc_list.txt";

    /**
     * Disables default update centers according to value of the property DISABLE_DEFAULT_UC
     * and enables update centers if the ADDITIONAL_UC is not empty
     * @param disableOnlyEnabled
     * @param enableNewlyAdded
     */
    public static void setProperUpdateCenters(boolean disableOnlyEnabled, boolean enableNewlyAdded) {
        if (isDisableRequired()) {
            disableDefaultUCs(disableOnlyEnabled);
        }
        String[] uc = parseAdditionalUCs();
        if (uc != null) {
            enableUCs(uc, enableNewlyAdded);
        }
    }

    public static void refreshProviders() {
        try {
            UpdateUnitProviderFactory.getDefault().refreshProviders(null, true);
//sleep 5 sec            
            Thread.sleep(5000);
        } catch (Exception ec) {
            ec.printStackTrace();
        }
    }

    /**
     * 
     * @return null if no filter should be accepted otherwise list of filters
     */
    public static String[] parseExcludePluginFilters() {
        String filterString = System.getProperty(EXCLUDE_PLUGINS);
        if (filterString == null) {
            return null;
        }
        if (filterString.trim().equals(NO_EXCLUDES)) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(filterString, TOKEN_SEPARATOR);
        int tokenCount = st.countTokens();
        String[] filters = new String[tokenCount];
        int index = 0;
        while (st.hasMoreTokens()) {
            filters[index] = st.nextToken().trim();
            index++;
        }
        return filters;
    }

    /**
     * 
     * @return null if no filter should be accepted otherwise list of filters
     */
    public static String[] parseIncludePluginFilters() {
        String filterString = System.getProperty(INCLUDE_PLUGINS);
        if (filterString == null) {
            return null;
        }
        if (filterString.trim().equals("") || filterString.trim().equals(INCLUDE_ALL)) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(filterString, TOKEN_SEPARATOR);
        int tokenCount = st.countTokens();
        String[] filters = new String[tokenCount];
        int index = 0;
        while (st.hasMoreTokens()) {
            filters[index] = st.nextToken().trim();
            index++;
        }
        return filters;
    }

    /**
     * Disables default update centers
     * @param onlyEnabled
     */
    private static void disableDefaultUCs(boolean onlyEnabled) {
        List<UpdateUnitProvider> providers = UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(onlyEnabled);
        for (UpdateUnitProvider updateUnitProvider : providers) {
            UpdateUnitProviderFactory.getDefault().remove(updateUnitProvider);
        }
    }

    public static void disableUCsExcept(ArrayList<String> list, boolean onlyEnabled, boolean enableNewlyAdded) {
        setProperUpdateCenters(onlyEnabled, enableNewlyAdded);
        List<UpdateUnitProvider> providers = UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(onlyEnabled);
        for (UpdateUnitProvider updateUnitProvider : providers) {
            if (list == null) {
                UpdateUnitProviderFactory.getDefault().remove(updateUnitProvider);
            } else {
                if (!list.contains(updateUnitProvider.getProviderURL().toString())) {
                    UpdateUnitProviderFactory.getDefault().remove(updateUnitProvider);
                }
            }
        }
    }

    /**
     * Enables the all update centers specified in the array uc
     * @param uc - string array of update centers
     * @param enable - enable the update centers
     */
    private static void enableUCs(String[] uc, boolean enable) {
        for (int i = 0; i < uc.length; i++) {
            try {
                UpdateUnitProviderFactory fac = UpdateUnitProviderFactory.getDefault();
                UpdateUnitProvider provider = fac.create(CUSTOM_PROVIDER_PREFIX + i, CUSTOM_PROVIDER_PREFIX + i, new URL(uc[i]));
                provider.setEnable(enable);
            } catch (MalformedURLException ex1) {
                Exceptions.printStackTrace(ex1);
            }
        }
    }

    /**
     * Checks whether default update centers are required according to value of property DISABLE_DEFAULT_UC
     * @return true if the DISABLE_DEFAULT_UC is like true; false otherwise
     */
    private static boolean isDisableRequired() {
        String disableUCs = System.getProperty(DISABLE_DEFAULT_UC);
        if (disableUCs == null) {
            return false;
        }
        return disableUCs.trim().equalsIgnoreCase("true") ? true : false;
    }

    /**
     * Parses the property ADDITIONAL_UC, tokens are separated by TOKEN_SEPARATOR
     * @return array of strings which are update centers or null if no update centers are specified
     */
    private static String[] parseAdditionalUCs() {
        String[] uc = null;
        String ucString = System.getProperty(ADDITIONAL_UC);
        //if property not set then return null
        if (ucString == null) {
            return null;
        }
        //if property is empty then return null
        if (ucString.trim().equals("")) {
            return null;
        }
        //else parse the uc names
        StringTokenizer st = new StringTokenizer(ucString, TOKEN_SEPARATOR);
        int tokenCount = st.countTokens();
        uc = new String[tokenCount];
        int index = 0;
        while (st.hasMoreTokens()) {
            uc[index] = st.nextToken().trim();
            index++;
        }
        return uc;
    }

    public static void savePluginsList(List<UpdateElement> elements, String filename) {
        PrintWriter pw = null;
        try {
            File f = new File(System.getProperty(EXTERNAL_WORKSPACE_PROPERTY), filename);
            pw = new PrintWriter(f);
            for (UpdateElement updateElement : elements) {
                pw.println(updateElement.getUpdateUnit().getCodeName());
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            pw.close();
        }
    }

    public static void saveUCList(String filename, boolean onlyEnabled) {
        PrintWriter pw = null;
        try {
            File f = new File(System.getProperty(EXTERNAL_WORKSPACE_PROPERTY), filename);
            pw = new PrintWriter(f);

            List<UpdateUnitProvider> providers = UpdateUnitProviderImpl.getUpdateUnitProviders(onlyEnabled);
            for (UpdateUnitProvider provider : providers) {
                pw.println(provider.getProviderURL().toString());
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            pw.close();
        }
    }

    public static ArrayList<String> readSavedData(String filename) {
        BufferedReader br = null;
        ArrayList<String> list = new ArrayList<String>();
        try {
            File f = new File(System.getProperty(EXTERNAL_WORKSPACE_PROPERTY), filename);
            br = new BufferedReader(new FileReader(f));
            String line = null;
            try {
                while ((line = br.readLine()) != null) {
                    list.add(line);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return (list);
    }

    public static String getModuleCanonicalName(UpdateElement element, int number) {
        return getModuleCanonicalName(element.getDisplayName(), number);
    }

    public static String getModuleCanonicalName(String displayName, int number) {
        StringBuffer displayNameBuffer = new StringBuffer(displayName);
        for (int i = 0; i < displayNameBuffer.length(); i++) {
            if (displayNameBuffer.charAt(i) < 65 || displayNameBuffer.charAt(i) > 122) {
                displayNameBuffer.setCharAt(i, '_');
            }
        }
        return "_" + number + "_" + displayNameBuffer.toString();
    }

    public static void logProperties(NbTestCase testCase) {
        testCase.log("");
        testCase.log("-----------------------------------------------------------");
        testCase.log("|  SETUP PROPERTIES:                                      |");
        testCase.log("-----------------------------------------------------------");
        testCase.log(EXCLUDE_PLUGINS + "=" + System.getProperty(EXCLUDE_PLUGINS));
        testCase.log(INCLUDE_PLUGINS + "=" + System.getProperty(INCLUDE_PLUGINS));
        testCase.log(DISABLE_DEFAULT_UC + "=" + System.getProperty(DISABLE_DEFAULT_UC));
        testCase.log(ADDITIONAL_UC + "=" + System.getProperty(ADDITIONAL_UC));
        testCase.log("-----------------------------------------------------------");
    }

    public static void logRegisteredUCs(boolean onlyEnabled, NbTestCase testCase) {
        List<UpdateUnitProvider> providers = UpdateUnitProviderImpl.getUpdateUnitProviders(onlyEnabled);
        testCase.log("");
        testCase.log("-----------------------------------------------------------");
        testCase.log("|  CURRENTLY REGISTERED UPDATE CENTERS:                   |");
        testCase.log("-----------------------------------------------------------");
        for (UpdateUnitProvider provider : providers) {
            testCase.log(provider.getProviderURL().toString() + "[" + provider.getCategory().toString() + "]");
        }
        testCase.log("-----------------------------------------------------------");
    }
}
