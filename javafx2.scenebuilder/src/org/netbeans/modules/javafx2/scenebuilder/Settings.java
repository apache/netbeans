/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
final public class Settings {
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
        final private static Settings INSTANCE = new Settings();
    }
    
    static public Settings getInstance() {
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