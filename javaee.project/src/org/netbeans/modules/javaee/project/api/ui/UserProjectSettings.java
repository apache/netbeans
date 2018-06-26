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

package org.netbeans.modules.javaee.project.api.ui;

import java.io.File;
import java.util.prefs.Preferences;

import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Globally stored miscellaneous project settings.
 */
public final class UserProjectSettings {
    private static final UserProjectSettings INSTANCE = new UserProjectSettings();
    private static final String NEW_PROJECT_COUNT = "newProjectCount"; //NOI18N

    private static final String NEW_APP_COUNT = "newApplicationCount";  //NOI18N

    private static final String LAST_USED_CP_FOLDER = "lastUsedClassPathFolder";    //NOI18N

    private static final String LAST_USED_ARTIFACT_FOLDER = "lastUsedArtifactFolder"; //NOI18N

    private static final String SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; //NOI18N
    
    private static final String SHOW_AGAIN_BROKEN_SERVER_ALERT = "showAgainBrokenServerAlert"; //NOI18N
    
    private static final String LAST_USED_CHOOSER_LOCATIONS = "lastUsedChooserLocations"; //NOI18N
    
    private static final String AGREED_SET_JDK_14 = "agreeSetJdk14"; // NOI18N
    
    private static final String AGREED_SET_SOURCE_LEVEL_14 = "agreeSetSourceLevel14"; // NOI18N
    
    private static final String AGREED_SET_JDK_15 = "agreeSetJdk15"; // NOI18N
    
    private static final String AGREED_SET_SOURCE_LEVEL_15 = "agreeSetSourceLevel15"; // NOI18N

    private static final String LAST_USED_SERVER = "lastUsedServer"; // NOI18N
    
    private static final String LAST_USED_IMPORT_LOCATION = "lastUsedImportLocation"; // NOI18N

    public static UserProjectSettings getDefault () {
        return INSTANCE;
    }
    
    public static Preferences getPreferences() {
        return NbPreferences.forModule(UserProjectSettings.class);
    }
    
    public String displayName() {
        return NbBundle.getMessage(UserProjectSettings.class, "TXT_ProjectFolderList");
    }

    public int getNewProjectCount () {
        return getPreferences().getInt(NEW_PROJECT_COUNT, 0);
    }

    public void setNewProjectCount (int count) {
        getPreferences().putInt(NEW_PROJECT_COUNT, count);
    }
    
    public int getNewApplicationCount () {
        return getPreferences().getInt(NEW_APP_COUNT, 0);
    }
    
    public void setNewApplicationCount (int count) {
        getPreferences().putInt(NEW_APP_COUNT, count);
    }
    
    public File getLastUsedClassPathFolder () {
        return new File(getPreferences().get(LAST_USED_CP_FOLDER, System.getProperty("user.home")));
    }

    public void setLastUsedClassPathFolder (File folder) {
        assert folder != null : "ClassPath root can not be null";
        String path = folder.getAbsolutePath();
        getPreferences().put(LAST_USED_CP_FOLDER, path);
    }

    public File getLastUsedArtifactFolder (final File defaultValue) {
        String val = getPreferences().get(LAST_USED_ARTIFACT_FOLDER, null);
        if (val != null) {
            return FileUtil.normalizeFile(new File (val));
        }
        if (defaultValue != null) {
            return defaultValue;
        }
        return FileUtil.normalizeFile(new File (System.getProperty("user.home")));
    }

    public void setLastUsedArtifactFolder (File folder) {
        assert folder != null : "Folder can not be null";
        String path = folder.getAbsolutePath();
        getPreferences().put(LAST_USED_ARTIFACT_FOLDER, path);        
    }

    public boolean isShowAgainBrokenRefAlert() {
        return getPreferences().getBoolean(SHOW_AGAIN_BROKEN_REF_ALERT, true);
    }
    
    public void setShowAgainBrokenRefAlert(boolean again) {
        getPreferences().putBoolean(SHOW_AGAIN_BROKEN_REF_ALERT, again);
    }
    
    public boolean isShowAgainBrokenServerAlert() {
        return getPreferences().getBoolean(SHOW_AGAIN_BROKEN_SERVER_ALERT, true);
    }
    
    public void setShowAgainBrokenServerAlert(boolean again) {
        getPreferences().putBoolean(SHOW_AGAIN_BROKEN_SERVER_ALERT, again);
    }
        
    public boolean isAgreedSetJdk14() {
        return getPreferences().getBoolean(AGREED_SET_JDK_14, true);
    }
    
    public void setAgreedSetJdk14(boolean agreed) {
        getPreferences().putBoolean(AGREED_SET_JDK_14, agreed);
    }
    
    public boolean isAgreedSetSourceLevel14() {
        return getPreferences().getBoolean(AGREED_SET_SOURCE_LEVEL_14, true);
    }
    
    public void setAgreedSetSourceLevel14(boolean agreed) {
        getPreferences().putBoolean(AGREED_SET_SOURCE_LEVEL_14, agreed);        
    }
    
    public boolean isAgreedSetJdk15() {
        return getPreferences().getBoolean(AGREED_SET_JDK_15, true);
    }
    
    public void setAgreedSetJdk15(boolean agreed) {
        getPreferences().putBoolean(AGREED_SET_JDK_15, agreed);                
    }
    
    public boolean isAgreedSetSourceLevel15() {
        return getPreferences().getBoolean(AGREED_SET_SOURCE_LEVEL_15, true);
    }
    
    public void setAgreedSetSourceLevel15(boolean agreed) {
        getPreferences().putBoolean(AGREED_SET_SOURCE_LEVEL_15, agreed);                
    }
    
    public void setLastUsedServer(String serverID) {
        getPreferences().put(LAST_USED_SERVER, serverID);                
    }

    public String getLastUsedServer() {
        return  getPreferences().get(LAST_USED_SERVER, null);                
    }
    
    public void setLastUsedImportLocation(File importLocation) {
        String path = importLocation.getAbsolutePath();
        getPreferences().put(LAST_USED_IMPORT_LOCATION, path);        
        
    }

    public File getLastUsedImportLocation() {
        String path = getPreferences().get(LAST_USED_IMPORT_LOCATION, null);
        if (path == null)
            return null;
        else
            return new File(path);
    }

    public File getLastChooserLocation() {
        String path = getPreferences().get(UserProjectSettings.LAST_USED_CHOOSER_LOCATIONS, null);
        return path != null ? new File(path) : null;
    }

    public void setLastChooserLocation(File folder) {
        getPreferences().put(UserProjectSettings.LAST_USED_CHOOSER_LOCATIONS, folder.getAbsolutePath());
    }

}
