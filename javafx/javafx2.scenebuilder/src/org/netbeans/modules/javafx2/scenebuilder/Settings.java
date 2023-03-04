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
package org.netbeans.modules.javafx2.scenebuilder;

import org.netbeans.modules.javafx2.scenebuilder.impl.SBHomeFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik@oracle.com>
 */
public final class Settings {
    private static final String SAVE_BEFORE_LAUNCH = "saveBeforeLaunch";
    private static final String SELECTED_HOME = "selectedHome";
    private static final String USER_DEFINED_HOMES = "userDefinedHomes";
    private Home selectedHome;
    private Home predefinedHome;
    private boolean saveBeforeLaunch;
    
    private List<Home> userDefinedHomes = new ArrayList<Home>();
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(Settings.class);
    }

    private static class Singleton {
        private static final Settings INSTANCE = new Settings();
    }
    
    public static Settings getInstance() {
        return Singleton.INSTANCE;
    }
    
    private Settings() {
        String homeDef = getPreferences().get(SELECTED_HOME, null);
        predefinedHome = SBHomeFactory.getDefault().defaultHome();
        
        boolean isDefault = (homeDef != null && predefinedHome != null && homeDef.equals(predefinedHome.getPath()));
        
        if (isDefault || homeDef == null) { // default or no home has been selected; try to use the one provided by the platform
            selectedHome = predefinedHome;
        } else {
            StringTokenizer st = new StringTokenizer(homeDef, "#");
            if (st.countTokens() > 0) {
                selectedHome = SBHomeFactory.getDefault().loadHome(st.nextToken());
            }
        }
        
        loadUserDefinedHomes();
        
        saveBeforeLaunch = getPreferences().getBoolean(SAVE_BEFORE_LAUNCH, false);
    }
    
    public Home getSelectedHome() {
        return selectedHome;
    }
    
    public void setSelectedHome(Home home) {
        selectedHome = home;
    }
    
    public Home getPredefinedHome() {
        return predefinedHome;
    }
    
    public List<Home> getUserDefinedHomes() {
        return Collections.unmodifiableList(userDefinedHomes);
    }
    
    public void setUserDefinedHomes(List<Home> homes) {
        userDefinedHomes = new ArrayList<Home>(homes);
    }
    
    public boolean isSaveBeforeLaunch() {
        return saveBeforeLaunch;
    }
    
    public void setSaveBeforeLaunch(boolean val) {
        saveBeforeLaunch = val;
    }
    
    public void store() {
        if (selectedHome != null) {
            getPreferences().put(SELECTED_HOME, selectedHome.getPath() + "#" + selectedHome.getVersion());
        } else {
            getPreferences().remove(SELECTED_HOME);
        }
        storeUserDefinedHomes();
        getPreferences().putBoolean(SAVE_BEFORE_LAUNCH, saveBeforeLaunch);
        try {
            getPreferences().sync();
        } catch (BackingStoreException e) {
            
        }
    }
    
    private void loadUserDefinedHomes() {
        String userDefinedHomesStr = getPreferences().get(USER_DEFINED_HOMES, "");
        StringTokenizer st = new StringTokenizer(userDefinedHomesStr, File.pathSeparator);
        while (st.hasMoreTokens()) {
            String homeDef = st.nextToken();
            StringTokenizer st1 = new StringTokenizer(homeDef, "#");
            if (st1.countTokens() == 4) {
                Home h = new Home(st1.nextToken(), st1.nextToken(), st1.nextToken(), st1.nextToken());
                if (h.isValid()) {
                    userDefinedHomes.add(h);
                }
            }
        }
    }
    
    private void storeUserDefinedHomes() {
        if (userDefinedHomes.isEmpty()) {
            getPreferences().put(USER_DEFINED_HOMES, "");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(Home h : userDefinedHomes) {
            if (h.isValid()) {
                sb.append(sb.length() > 0 ? File.pathSeparator : "");
                sb.append(h.getPath()).append("#");
                sb.append(h.getLauncherPath(true)).append("#");
                sb.append(h.getPropertiesPath(true)).append("#");
                sb.append(h.getVersion());
            }
        }
        getPreferences().put(USER_DEFINED_HOMES, sb.toString());
    }
}