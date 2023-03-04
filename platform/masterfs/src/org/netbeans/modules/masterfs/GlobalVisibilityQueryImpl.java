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

package org.netbeans.modules.masterfs;

import java.io.File;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.openide.filesystems.FileObject;
import javax.swing.event.ChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.netbeans.spi.queries.VisibilityQueryImplementation2;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

// XXX - one would expect this class in core but there is problem that 
//core can't depened on project/queries at the moment 

/**
 * 
 * Implemenent VisibilityQueryImplementation based on regular expression provided
 * by users via property PROP_IGNORED_FILES in Tools/Options/Miscellaneous/Files.
 * 
 * This class has hidden dependency on IgnoredFilesPreferences module org.netbeans.core.ui.
 */ 
@ServiceProviders({
    @ServiceProvider(service=org.netbeans.spi.queries.VisibilityQueryImplementation.class),
    @ServiceProvider(service=GlobalVisibilityQueryImpl.class)
})
public class GlobalVisibilityQueryImpl implements VisibilityQueryImplementation2 {
    private final ChangeSupport cs = new ChangeSupport(this);
    
    /**
     * Keep it synchronized with IgnoredFilesPreferences.PROP_IGNORED_FILES
     */ 
    private static final String PROP_IGNORED_FILES = "IgnoredFiles"; // NOI18N
    private static final String PROP_IGNORE_HIDDEN_FILES_IN_USER_HOME
            = "IgnoreHiddenFilesInUserHome";                           // NOI18N
    private Pattern ignoreFilesPattern = null;

    private boolean ignoreHiddenInHome = true;
    private boolean ignoreHiddenInHomeInitialized = false;

    private FileObject home = null;
    private String homePath = null;
    private boolean homeInitialized = false;

    private PreferenceChangeListener preferencesListener = null;

    /** Default instance for lookup. */
    public GlobalVisibilityQueryImpl() {
    }

    private static Preferences getPreferences() {
        return NbPreferences.root().node("/org/netbeans/core");
    }
    
    public boolean isVisible(FileObject file) {
        String name = file.getNameExt();
        if (isIgnoreHiddenInHome() && isHidden(name) && isInHomeFolder(file)) {
            return false;
        } else {
            return isVisible(name);
        }
    }
    
    public boolean isVisible(File file) {
        String name = file.getName();
        if (isIgnoreHiddenInHome() && isHidden(name) && isInHomeFolder(file)) {
            return false;
        } else {
            return isVisible(name);
        }
    }
    

    boolean isVisible(final String fileName) {
        Pattern pattern = getIgnoreFilesPattern();
        return (pattern != null) ? !(pattern.matcher(fileName).find()) : true;
    }

    /**
     * Add a listener to changes.
     * @param l a listener to add
     */
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    /**
     * Stop listening to changes.
     * @param l a listener to remove
     */
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    private boolean isIgnoreHiddenInHome() {
        if (!ignoreHiddenInHomeInitialized) {
            ignoreHiddenInHome = getPreferences().getBoolean(
                    PROP_IGNORE_HIDDEN_FILES_IN_USER_HOME, true);
            ignoreHiddenInHomeInitialized = true;
        }
        return ignoreHiddenInHome;
    }

    private Pattern getIgnoreFilesPattern() {
        if (ignoreFilesPattern == null) {
            String ignoredFiles = getIgnoredFiles();
            ignoreFilesPattern = (ignoredFiles != null && ignoredFiles.length() > 0) ? Pattern.compile(ignoredFiles) : null;
        }
        return ignoreFilesPattern;
    }

    protected String getIgnoredFiles() {
        // \.(cvsignore|svn|DS_Store) is covered by ^\..*$
        String retval = getPreferences().get(PROP_IGNORED_FILES, "^(CVS|SCCS|vssver.?\\.scc|#.*#|%.*%|_svn)$|~$|^\\.(git|hg|svn|cache|gradle|DS_Store)$|^Thumbs.db$");//NOI18N;
        PreferenceChangeListener listenerToAdd;
        synchronized (this) {
            if (preferencesListener == null) {
                preferencesListener = new PreferenceChangeListener() {
                    @Override
                    public void preferenceChange(PreferenceChangeEvent evt) {
                        if (PROP_IGNORED_FILES.equals(evt.getKey())) {
                            ignoreFilesPattern = null;
                            cs.fireChange();
                        } else if (PROP_IGNORE_HIDDEN_FILES_IN_USER_HOME.equals(
                                evt.getKey())) {
                            ignoreHiddenInHomeInitialized = false;
                            cs.fireChange();
                        }
                    }
                };
                listenerToAdd = preferencesListener;
            } else {
                listenerToAdd = null;
            }
        }
        if (listenerToAdd != null) {
            getPreferences().addPreferenceChangeListener(listenerToAdd);
        }
        return retval;
    }

    /**
     * Check if the file is hidden in Unix file systems.
     *
     * @param String File name.
     * @return True if file name starts with a dot.
     */
    private boolean isHidden(String fileName) {
        return fileName.startsWith(".");                                //NOI18N
    }

    /**
     * Check if the file is located directly in user's home folder.
     *
     * @param fo The FileObject.
     * @return True if this is a file located directly in user's home folder.
     */
    private boolean isInHomeFolder(FileObject fo) {
        if (!homeInitialized) {
            initializeHome();
        }
        FileObject parent = fo.getParent();
        return parent != null && parent == home;
    }

    /**
     * Check if the file is located directly in user's home folder.
     *
     * @param f The file.
     * @return True if this is a file located directly in user's home folder.
     */
    private boolean isInHomeFolder(File f) {
        if (!homeInitialized) {
            initializeHome();
        }
        String parentPath = f.getParent();
        return parentPath != null && parentPath.equals(homePath);
    }

    /**
     * Initialize variables holding the user directory.
     */
    private void initializeHome() {
        String homeRaw = System.getProperty("user.home");               //NOI18N
        if (homeRaw != null) {
            homePath = FileUtil.normalizePath(homeRaw);
            home = FileUtil.toFileObject(new File(homePath));
        }
        homeInitialized = true;
    }
}
