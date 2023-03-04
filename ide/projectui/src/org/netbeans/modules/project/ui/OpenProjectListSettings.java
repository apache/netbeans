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

package org.netbeans.modules.project.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.modules.project.ui.groups.Group;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/** SystemOption to store the list of open projects
 */
public class OpenProjectListSettings {

    private static OpenProjectListSettings INSTANCE = new OpenProjectListSettings();
    
    private static final String RECENT_PROJECTS_DISPLAY_NAMES = "RecentProjectsDisplayNames"; //NOI18N
    private static final String RECENT_PROJECTS_DISPLAY_ICONS = "RecentProjectsIcons"; //NOI18N
    private static final String LAST_OPEN_PROJECT_DIR = "lastOpenProjectDir"; //NOI18N - String
    private static final String PROP_PROJECT_CATEGORY = "lastSelectedProjectCategory"; //NOI18N - String
    private static final String PROP_PROJECT_TYPE = "lastSelectedProjectType"; //NOI18N - String
    private static final String MAIN_PROJECT_URL = "mainProjectURL"; //NOI18N -URL
    private static final String OPEN_PROJECTS_URLS = "openProjectsURLs"; //NOI18N - List of URLs
    private static final String OPEN_PROJECTS_DISPLAY_NAMES = "openProjectsDisplayNames"; //NOI18N - List of names
    private static final String OPEN_PROJECTS_ICONS = "openProjectsIcons"; //NOI18N - List of icons
    private static final String OPEN_SUBPROJECTS = "openSubprojects"; //NOI18N - boolean
    private static final String TRUST_AND_PRIME = "trustAndPrime"; //NOI18N - boolean
    private static final String PROP_PROJECTS_FOLDER = "projectsFolder"; //NOI18N - String
    private static final String RECENT_PROJECTS_URLS = "recentProjectsURLs"; //NOI18N List of URLs
    private static final String RECENT_TEMPLATES = "recentTemplates"; // NOI18N -List of Strings
    
    public static final String PROP_CREATED_PROJECTS_FOLDER = "createdProjectsFolderInWizard"; // NOI18N
    
    private OpenProjectListSettings() {
    }
    
    public static OpenProjectListSettings getInstance() {
        return INSTANCE;
    }
    
    protected final String putProperty(String key, String value, boolean notify) {
        String retval = getProperty(key);
        if (value != null) {
            getPreferences().put(key, value);
        } else {
            getPreferences().remove(key);
        }
        return retval;
    }

    protected final String getProperty(String key) {
        return getPreferences().get(key, null);
    }    
    
    private String getGroupedProperty(String key) {
        return getPreferences(true).get(key, null);
    }
    
    private String putGroupedProperty(String key, String value, boolean notify) {
        Preferences prefs = getPreferences(true);
        String retval = prefs.get(key, null);
        if (value != null) {
            prefs.put(key, value);
        } else {
            prefs.remove(key);
        }
        return retval;
    }
    
    
    protected final List<URL> getURLList(String key, boolean allowGrouped) {
        List<String> strs = getStringList(key, allowGrouped);
        List<URL> toRet = new ArrayList<URL>();
        for (String val : strs) {
            try {
                toRet.add(new URL(val));
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return toRet;
    }
    
    protected final List<String> getStringList(String key, boolean allowGrouped) {
        Preferences pref = getPreferences(allowGrouped);
        int count = 0;
        String val = pref.get(key + "." + count, null);
        List<String> toRet = new ArrayList<String>();
        while (val != null) {
            toRet.add(val);
            count = count + 1;
            val = pref.get(key + "." + count, null);
        }
        return toRet;
    }
    
    protected final List<ExtIcon> getIconList(String key, boolean allowGrouped) {
        Preferences pref = getPreferences(allowGrouped);
        int count = 0;
        byte[] val = pref.getByteArray(key + "." + count, null);
        List<ExtIcon> toRet = new ArrayList<ExtIcon>();
        while (val != null) {
            toRet.add(val.length > 0 ? new ExtIcon(val) : new ExtIcon());
            count = count + 1;
            val = pref.getByteArray(key + "." + count, null);
        }
        return toRet;
    }
    
    protected final void setIconList(String basekey, List<ExtIcon> list, boolean allowGrouped) throws IOException {
        assert list != null;
        Preferences pref = getPreferences(allowGrouped);
        int count = 0;
        String key = basekey + "." + count;
        String val = pref.get(key, null);
        Iterator<ExtIcon> it = list.iterator();
        while (val != null || it.hasNext()) {
            if (it.hasNext()) {
                byte[] data = it.next().getBytes();
                if (data == null) {
                    data = new byte[0];
                }
                pref.putByteArray(key, data);
            } else {
                pref.remove(key);
            }
            count = count + 1;
            key = basekey + "." + count;
            val = pref.get(key, null);
        }
    }
    
    
    protected final void setStringList(String basekey, List<String> list, boolean allowGrouped) {
        assert list != null;
        Preferences pref = getPreferences(allowGrouped);
        int count = 0;
        String key = basekey + "." + count;
        String val = pref.get(key, null);
        Iterator<String> it = list.iterator();
        while (val != null || it.hasNext()) {
            if (it.hasNext()) {
                pref.put(key, it.next());
            } else {
                pref.remove(key);
            }
            count = count + 1;
            key = basekey + "." + count;
            val = pref.get(key, null);
        }
    }
    
    protected final void setURLList(String basekey, List<URL> list, boolean allowGrouped) {
        assert list != null;
        List<String> strs = new ArrayList<String>(list.size());
        for (URL url : list) {
            strs.add(url.toExternalForm());
        }
        setStringList(basekey, strs, allowGrouped);
    }
    
    protected final Preferences getPreferences() {
        return NbPreferences.forModule(OpenProjectListSettings.class);
    }

    protected final Preferences getPreferences(boolean allowGrouped) {
        if (allowGrouped) {
            Group act = Group.getActiveGroup();
            if (act != null) {
                //TODO replace with NbPreferences.forModule()
                return act.prefs().node(OpenProjectListSettings.class.getPackage().getName().replace(".", "/"));
            }   
        }
        return NbPreferences.forModule(OpenProjectListSettings.class);
    }

    public List<URL> getOpenProjectsURLs() {
        return getURLList(OPEN_PROJECTS_URLS, false);
    }

    public void setOpenProjectsURLs( List<URL> list ) {
        setURLList( OPEN_PROJECTS_URLS, list, false);
    }
    public void setOpenProjectsURLsAsStrings(List<String> list) {
        setStringList(OPEN_PROJECTS_URLS, list, false);
    }

    public List<String> getOpenProjectsDisplayNames() {
        return getStringList(OPEN_PROJECTS_DISPLAY_NAMES, false);
    }

    public void setOpenProjectsDisplayNames( List<String> list ) {
        setStringList( OPEN_PROJECTS_DISPLAY_NAMES, list, false);
    }
    public List<ExtIcon> getOpenProjectsIcons() {
        return getIconList(OPEN_PROJECTS_ICONS, false);
    }

    public void setOpenProjectsIcons( List<ExtIcon> list ) {
        try {
            setIconList(OPEN_PROJECTS_ICONS, list, false);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public boolean isOpenSubprojects() {        
        return getPreferences().getBoolean( OPEN_SUBPROJECTS, false);
    }
    
    public void setOpenSubprojects( boolean openSubprojects ) {
        getPreferences().putBoolean(OPEN_SUBPROJECTS, openSubprojects);
    }

    public boolean isTrustAndPrime() {
        return getPreferences().getBoolean(TRUST_AND_PRIME, false);
    }

    public void setTrustAndPrime( boolean openSubprojects ) {
        getPreferences().putBoolean(TRUST_AND_PRIME, openSubprojects);
    }

    public URL getMainProjectURL() {
        String str = getProperty(MAIN_PROJECT_URL);
        if (str != null) {
            try {
                return new URL(str);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    
    public void setMainProjectURL( URL mainProjectURL ) {
        setMainProjectURL(mainProjectURL != null ? mainProjectURL.toString() : null);
    }
    public void setMainProjectURL(String mainProjectURL) {
        putProperty(MAIN_PROJECT_URL, mainProjectURL, true);
    }
    
    public String getLastOpenProjectDir() {
        String result = getGroupedProperty( LAST_OPEN_PROJECT_DIR );
        if (result == null) {
            result = getProjectsFolder(/* #89624 */false).getAbsolutePath();
        }
        return result;
    }
    
    public void setLastOpenProjectDir( String path ) {
        putGroupedProperty( LAST_OPEN_PROJECT_DIR, path, true );
    }
    
    public List<URL> getRecentProjectsURLs() {
        return getURLList(RECENT_PROJECTS_URLS, true);
    }
    
    public List<String> getRecentProjectsDisplayNames() {
        return getStringList(RECENT_PROJECTS_DISPLAY_NAMES, true);
    }
    
    public List<ExtIcon> getRecentProjectsIcons() {
        return getIconList(RECENT_PROJECTS_DISPLAY_ICONS, true);
    }
    
    public void setRecentProjectsURLs( List<URL> list ) {
        setURLList(RECENT_PROJECTS_URLS, list, true);
    }
    
    public void setRecentProjectsDisplayNames(List<String> list) {
        setStringList(RECENT_PROJECTS_DISPLAY_NAMES, list, true);
    }
    
    public void setRecentProjectsIcons(List<ExtIcon> list) {
        try {
            setIconList(RECENT_PROJECTS_DISPLAY_ICONS, list, true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public File getProjectsFolder(boolean create) {
        String result = getProperty (PROP_PROJECTS_FOLDER);
        if (result == null || !(new File(result)).exists()) {
            // property for overriding default projects dir location
            String userPrjDir = System.getProperty("netbeans.projects.dir"); // NOI18N
            if (userPrjDir != null) {
                File f = new File(userPrjDir);
                if (f.exists() && f.isDirectory()) {
                    return FileUtil.normalizeFile(f);
                }
            }
            if (Boolean.getBoolean("netbeans.full.hack")) { // NOI18N
                return FileUtil.normalizeFile(new File(System.getProperty("java.io.tmpdir", ""))); // NOI18N
            }
            File defaultDir = FileSystemView.getFileSystemView().getDefaultDirectory();
            if (defaultDir != null && defaultDir.exists() && defaultDir.isDirectory()) {
                String nbPrjDirName = NbBundle.getMessage(OpenProjectListSettings.class, "DIR_NetBeansProjects");
                File nbPrjDir = new File(defaultDir, nbPrjDirName);
                if (nbPrjDir.exists() && nbPrjDir.canWrite()) {
                    return nbPrjDir;
                } else {
                    boolean created = create && nbPrjDir.mkdir();
                    if (created) {
                        // #75960 - using Preferences to temporarily save created projects folder path,
                        // folder will be deleted after wizard is finished if nothing was created in it
                        getPreferences().put(PROP_CREATED_PROJECTS_FOLDER, nbPrjDir.getAbsolutePath());
                        return nbPrjDir;
                    } 
                }
            }
            result = System.getProperty("user.home");   //NOI18N
        }
        return FileUtil.normalizeFile(new File(result));
    }

    public void setProjectsFolder (File folder) {
        if (folder == null) {
            putProperty(PROP_PROJECTS_FOLDER, (String)null, true);
        }
        else {
            putProperty(PROP_PROJECTS_FOLDER, folder.getAbsolutePath(), true);
        }
    }
    
    public List<String> getRecentTemplates() {        
        return getStringList(RECENT_TEMPLATES, true);
    }
    
    public void setRecentTemplates( List<String> templateNames ) {
        setStringList( RECENT_TEMPLATES, templateNames, true );
    }
    
    public String getLastSelectedProjectCategory () {
        return getGroupedProperty(PROP_PROJECT_CATEGORY);
    }
    
    public void setLastSelectedProjectCategory (String category) {
        putGroupedProperty(PROP_PROJECT_CATEGORY,category,true);
    }
    
    public String getLastSelectedProjectType () {
        return getGroupedProperty (PROP_PROJECT_TYPE);
    }
    
    public void setLastSelectedProjectType (String type) {
        putGroupedProperty(PROP_PROJECT_TYPE,type,true);
    }

}
