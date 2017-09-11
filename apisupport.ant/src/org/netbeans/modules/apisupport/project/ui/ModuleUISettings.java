/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.apisupport.project.ui;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Storage for settings used by a module's UI (wizards, properties, ...)
 *
 * @author Martin Krauskopf, Jesse Glick
 */
public class ModuleUISettings {

    private static final String LAST_CHOSEN_LIBRARY_LOCATION = "lastChosenLibraryLocation"; // NOI18N
    private static final String LAST_USED_NB_PLATFORM_LOCATION = "lastUsedNbPlatformLocation"; // NOI18N
    private static final String NEW_MODULE_COUNTER = "newModuleCounter";  //NOI18N
    private static final String NEW_SUITE_COUNTER = "newSuiteCounter";  //NOI18N
    private static final String NEW_APPLICATION_COUNTER = "newApplicationCounter";  //NOI18N
    private static final String CONFIRM_RELOAD_IN_IDE = "confirmReloadInIDE"; // NOI18N
    private static final String LAST_USED_PLATFORM_ID = "lastUsedPlatformID"; // NOI18N
    private static final String HARNESSES_UPGRADED = "harnessesUpgraded"; // NOI18N
    private static final String LAST_USED_CLUSTER_LOCATION = "lastUsedClusterLocation";    // NOI18N

    public static ModuleUISettings getDefault() {
        return new ModuleUISettings(); // stateless
    }

    private Preferences prefs() {
        return NbPreferences.forModule(ModuleUISettings.class);
    }

    public int getNewModuleCounter() {
        return prefs().getInt(NEW_MODULE_COUNTER, 0);
    }

    public void setNewModuleCounter(int count) {
        prefs().putInt(NEW_MODULE_COUNTER, count);
    }

    public int getNewSuiteCounter() {
        return prefs().getInt(NEW_SUITE_COUNTER, 0);
    }

    public void setNewSuiteCounter(int count) {
        prefs().putInt(NEW_SUITE_COUNTER, count);
    }
    
    public int getNewApplicationCounter() {
        return prefs().getInt(NEW_APPLICATION_COUNTER, 0);
    }

    public void setNewApplicationCounter(int count) {
        prefs().putInt(NEW_APPLICATION_COUNTER, count);
    }

    public String getLastUsedNbPlatformLocation() {
        return prefs().get(LAST_USED_NB_PLATFORM_LOCATION, System.getProperty("user.home")); // NOI18N
    }

    public void setLastUsedNbPlatformLocation(String location) {
        assert location != null : "Location can not be null"; // NOI18N
        prefs().put(LAST_USED_NB_PLATFORM_LOCATION, location);
    }

    public String getLastUsedClusterLocation() {
        return prefs().get(LAST_USED_CLUSTER_LOCATION, System.getProperty("user.home")); // NOI18N
    }

    public void setLastUsedClusterLocation(String location) {
        assert location != null : "Location can not be null"; // NOI18N
        prefs().put(LAST_USED_CLUSTER_LOCATION, location);
    }

    public boolean getConfirmReloadInIDE() {
        return prefs().getBoolean(CONFIRM_RELOAD_IN_IDE, true);
    }

    public void setConfirmReloadInIDE(boolean b) {
        prefs().putBoolean(CONFIRM_RELOAD_IN_IDE, b);
    }

    public String getLastChosenLibraryLocation() {
        return prefs().get(LAST_CHOSEN_LIBRARY_LOCATION, System.getProperty("user.home")); // NOI18N
    }

    public void setLastChosenLibraryLocation(String location) {
        assert location != null : "Location can not be null"; // NOI18N
        prefs().put(LAST_CHOSEN_LIBRARY_LOCATION, location);
    }

    public String getLastUsedPlatformID() {
        return prefs().get(LAST_USED_PLATFORM_ID, "default"); // NOI18N
    }

    public void setLastUsedPlatformID(String id) {
        assert id != null : "Platform ID can not be null"; // NOI18N
        prefs().put(LAST_USED_PLATFORM_ID, id);
    }

    public boolean getHarnessesUpgraded() {
        return prefs().getBoolean(HARNESSES_UPGRADED, false);
    }

    public void setHarnessesUpgraded(boolean b) {
        prefs().putBoolean(HARNESSES_UPGRADED, b);
    }

}
