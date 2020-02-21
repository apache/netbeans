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

package org.netbeans.modules.cnd.remote.projectui.wizard.ide;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.filechooser.FileSystemView;
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
    private static final String OPEN_AS_MAIN = "openAsMain"; //NOI18N - boolean
    private static final String OPEN_PROJECTS_URLS = "openProjectsURLs"; //NOI18N - List of URLs
    private static final String OPEN_PROJECTS_DISPLAY_NAMES = "openProjectsDisplayNames"; //NOI18N - List of names
    private static final String OPEN_PROJECTS_ICONS = "openProjectsIcons"; //NOI18N - List of icons
    private static final String OPEN_SUBPROJECTS = "openSubprojects"; //NOI18N - boolean
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
    
    protected final List<URL> getURLList(String key) {
        List<String> strs = getStringList(key);
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
    
    protected final List<String> getStringList(String key) {
        Preferences pref = getPreferences();
        int count = 0;
        String val = pref.get(key + "." + count, null); //NOI18N
        List<String> toRet = new ArrayList<String>();
        while (val != null) {
            toRet.add(val);
            count = count + 1;
            val = pref.get(key + "." + count, null); //NOI18N
        }
        return toRet;
    }
    
    protected final List<ExtIcon> getIconList(String key) {
        Preferences pref = getPreferences();
        int count = 0;
        byte[] val = pref.getByteArray(key + "." + count, null); //NOI18N
        List<ExtIcon> toRet = new ArrayList<ExtIcon>();
        while (val != null) {
            toRet.add(val.length > 0 ? new ExtIcon(val) : new ExtIcon());
            count = count + 1;
            val = pref.getByteArray(key + "." + count, null); //NOI18N
        }
        return toRet;
    }
    
    protected final void setIconList(String basekey, List<ExtIcon> list) throws IOException {
        assert list != null;
        Preferences pref = getPreferences();
        int count = 0;
        String key = basekey + "." + count; //NOI18N
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
            key = basekey + "." + count; //NOI18N
            val = pref.get(key, null);
        }
    }
    
    
    protected final void setStringList(String basekey, List<String> list) {
        assert list != null;
        Preferences pref = getPreferences();
        int count = 0;
        String key = basekey + "." + count; //NOI18N
        String val = pref.get(key, null);
        Iterator<String> it = list.iterator();
        while (val != null || it.hasNext()) {
            if (it.hasNext()) {
                pref.put(key, it.next());
            } else {
                pref.remove(key);
            }
            count = count + 1;
            key = basekey + "." + count; //NOI18N
            val = pref.get(key, null);
        }
    }
    
    protected final void setURLList(String basekey, List<URL> list) {
        assert list != null;
        List<String> strs = new ArrayList<String>(list.size());
        for (URL url : list) {
            strs.add(url.toExternalForm());
        }
        setStringList(basekey, strs);
    }
    
    protected final Preferences getPreferences() {
        return NbPreferences.forModule(OpenProjectListSettings.class);
    }

    public List<URL> getOpenProjectsURLs() {
        return getURLList(OPEN_PROJECTS_URLS);
    }

    public void setOpenProjectsURLs( List<URL> list ) {
        setURLList( OPEN_PROJECTS_URLS, list);
    }
    public void setOpenProjectsURLsAsStrings(List<String> list) {
        setStringList(OPEN_PROJECTS_URLS, list);
    }

    public List<String> getOpenProjectsDisplayNames() {
        return getStringList(OPEN_PROJECTS_DISPLAY_NAMES);
    }

    public void setOpenProjectsDisplayNames( List<String> list ) {
        setStringList( OPEN_PROJECTS_DISPLAY_NAMES, list);
    }
    public List<ExtIcon> getOpenProjectsIcons() {
        return getIconList(OPEN_PROJECTS_ICONS);
    }

    public void setOpenProjectsIcons( List<ExtIcon> list ) {
        try {
            setIconList(OPEN_PROJECTS_ICONS, list);
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
    
    public boolean isOpenAsMain() {        
        return getPreferences().getBoolean(OPEN_AS_MAIN, false);
    }
    
    public void setOpenAsMain( boolean openAsMain ) {
        getPreferences().putBoolean(OPEN_AS_MAIN, openAsMain);
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
        String result = getProperty( LAST_OPEN_PROJECT_DIR );
        if (result == null) {
            result = getProjectsFolder(/* #89624 */false).getAbsolutePath();
        }
        return result;
    }
    
    public void setLastOpenProjectDir( String path ) {
        putProperty( LAST_OPEN_PROJECT_DIR, path, true  );
    }
    
    public List<URL> getRecentProjectsURLs() {
        return getURLList(RECENT_PROJECTS_URLS);
    }
    
    public List<String> getRecentProjectsDisplayNames() {
        return getStringList(RECENT_PROJECTS_DISPLAY_NAMES);
    }
    
    public List<ExtIcon> getRecentProjectsIcons() {
        return getIconList(RECENT_PROJECTS_DISPLAY_ICONS);
    }
    
    public void setRecentProjectsURLs( List<URL> list ) {
        setURLList(RECENT_PROJECTS_URLS, list);
    }
    
    public void setRecentProjectsDisplayNames(List<String> list) {
        setStringList(RECENT_PROJECTS_DISPLAY_NAMES, list);
    }
    
    public void setRecentProjectsIcons(List<ExtIcon> list) {
        try {
            setIconList(RECENT_PROJECTS_DISPLAY_ICONS, list);
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
        return getStringList(RECENT_TEMPLATES);
    }
    
    public void setRecentTemplates( List<String> templateNames ) {
        setStringList( RECENT_TEMPLATES, templateNames );
    }
    
    public String getLastSelectedProjectCategory () {
        return getProperty (PROP_PROJECT_CATEGORY);
    }
    
    public void setLastSelectedProjectCategory (String category) {
        putProperty(PROP_PROJECT_CATEGORY,category,true);
    }
    
    public String getLastSelectedProjectType () {
        return getProperty (PROP_PROJECT_TYPE);
    }
    
    public void setLastSelectedProjectType (String type) {
        putProperty(PROP_PROJECT_TYPE,type,true);
    }

}
