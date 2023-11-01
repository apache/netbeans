/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.git;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.netbeans.libs.git.GitURI;
import org.netbeans.modules.git.FileInformation.Mode;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.repository.remote.ConnectionSettings;
import org.netbeans.modules.versioning.util.KeyringSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbPreferences;

/**
 *
 * @author ondra
 */
public final class GitModuleConfig {
    
    private static GitModuleConfig instance;
    private static final String AUTO_OPEN_OUTPUT_WINDOW = "autoOpenOutput";     // NOI18N
    private static final String AUTO_REPLACE_INVALID_BRANCH_NAME_CHARACTERS = "autoReplaceInvalidBranchNameCharacters";
    public static final String PROP_COMMIT_EXCLUSIONS          = "commitExclusions";   // NOI18N
    private static final String PROP_LAST_USED_MODE     = "lastUsedMode";       // NOI18N
    private static final String EXCLUDE_NEW_FILES       = "excludeNewFiles";    // NOI18N
    private static final String RECENT_COMMIT_AUTHORS   = "recentCommitAuhtors";// NOI18N
    private static final String RECENT_COMMITERS        = "recentCommiters";    // NOI18N
    private static final String RECENT_GURI             = "recent_guri";    
    private static final String SIGN_OFF                = "signOff";            // NOI18N
    private static final String REVERT_ALL              = "revertAll";          // NOI18N
    private static final String REMOVE_ALL_NEW          = "removeAllNew";       // NOI18N
    private static final String REVERT_INDEX            = "revertIndex";        // NOI18N
    private static final String REVERT_WT               = "revertWT";           // NOI18N
    private static final String REMOVE_WT_NEW           = "removeWTNew";        // NOI18N
    private static final String PROP_LAST_USED_COMMIT_VIEW_MODE = "lastUsedCommitViewMode"; //NOI18N
    private static final String AUTO_IGNORE_FILES       = "autoIgnoreFiles"; //NOI18N
    private static final String SHOW_CLONE_COMPLETED    = "cloneCompleted.showCloneCompleted";        // NOI18N  
    private static final String GURI_PASSWORD           = "guri_password";
    private static final String GURI_PASSPHRASE           = "guri_passphrase";
    private static final String PROP_STATUS_VIEW_MODE = "statusViewMode"; //NOI18N
    private static final String PROP_DIFF_VIEW_MODE = "diffViewMode"; //NOI18N
    private static final String DELIMITER               = "<=~=>";              // NOI18N
    private static final String KEY_SHOW_HISTORY_MERGES = "showHistoryMerges"; //NOI18N
    private static final String KEY_SHOW_FILE_INFO = "showFileInfo"; //NOI18N
    private static final String KEY_SEARCH_ON_BRANCH = "searchOnBranch.enabled"; //NOI18N
    private static final String PROP_ANNOTATIONFORMAT_PROJECT = "annotationFormat.project"; //NOI18N
    private static final String KEY_ANNOTATION_DISPLAYED_FIELDS = "annotate.displayedFields"; //NOI18N
    
    private String lastCanceledCommitMessage;
    private static final String DEFAULT_ANNOTATION_PROJECT = Annotator.DEFAULT_ANNOTATION_PROJECT;
    
    public static GitModuleConfig getDefault () {
        if (instance == null) {
            instance = new GitModuleConfig();
        }
        return instance;
    }
    private Set<String> exclusions;
    
    private GitModuleConfig() { }

    public boolean isExcludedFromCommit(String path) {
        Set<String> commitExclusions = getCommitExclusions();        
        return commitExclusions.contains(path);
    }

    public Color getColor(String colorName, Color defaultColor) {
         int colorRGB = getPreferences().getInt(colorName, defaultColor.getRGB());
         return new Color(colorRGB);
    }

    public void setColor(String colorName, Color value) {
         getPreferences().putInt(colorName, value.getRGB());
    }

    public Preferences getPreferences() {
        return NbPreferences.forModule(GitModuleConfig.class);
    }

    public boolean getExludeNewFiles() {
        return getPreferences().getBoolean(EXCLUDE_NEW_FILES, false);
    }

    public void setExcludeNewFiles(boolean value) {
        getPreferences().putBoolean(EXCLUDE_NEW_FILES, value);
    }
    
    public String getLastCanceledCommitMessage() {
        return lastCanceledCommitMessage == null ? "" : lastCanceledCommitMessage; //NOI18N
    }

    public void setLastCanceledCommitMessage(String message) {
        lastCanceledCommitMessage = message;
    }

    /**
     * @param paths collection of paths, of File.getAbsolutePath()
     */
    public void addExclusionPaths(Collection<String> paths) {
        Set<String> commitExclusions = getCommitExclusions();
        if (commitExclusions.addAll(paths)) {
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<String>(commitExclusions));
        }
    }

    /**
     * @param paths collection of paths, File.getAbsolutePath()
     */
    public void removeExclusionPaths(Collection<String> paths) {
        Set<String> commitExclusions = getCommitExclusions();
        if (commitExclusions.removeAll(paths)) {
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<String>(commitExclusions));
        }
    }   

    public String getProjectAnnotationFormat () {
        return getPreferences().get(PROP_ANNOTATIONFORMAT_PROJECT, DEFAULT_ANNOTATION_PROJECT);
    }

    public void setProjectAnnotationFormat (String text) {
        getPreferences().put(PROP_ANNOTATIONFORMAT_PROJECT, text);
    }

    public boolean getAutoSyncBranch (File repository, String branch) {
        RepositoryInfo.NBGitConfig cfg = RepositoryInfo.getInstance(repository).getNetbeansConfig();
        return cfg.getAutoSyncBranch(branch);
    }

    public void setAutoSyncBranch (File repository, String branch, boolean autoSync) {
        RepositoryInfo.NBGitConfig cfg = RepositoryInfo.getInstance(repository).getNetbeansConfig();
        cfg.setAutoSyncBranch(branch, autoSync);
    }
    
    synchronized Set<String> getCommitExclusions() {
        if (exclusions == null) {
            exclusions = new HashSet<String>(Utils.getStringList(getPreferences(), PROP_COMMIT_EXCLUSIONS));
        }
        return exclusions;
    }  
    
    public Mode getLastUsedModificationContext () {
        Mode mode;
        try {
            mode = Mode.valueOf(getPreferences().get(PROP_LAST_USED_MODE, Mode.HEAD_VS_WORKING_TREE.name()));
        } catch (IllegalArgumentException ex) {
            mode = null;
        }
        return mode == null ? Mode.HEAD_VS_WORKING_TREE : mode;
    }

    public void setLastUsedModificationContext (Mode mode) {
        getPreferences().put(PROP_LAST_USED_MODE, mode.name());
    }    

    public Mode getLastUsedCommitViewMode () {
        Mode mode;
        try {
            mode = Mode.valueOf(getPreferences().get(PROP_LAST_USED_COMMIT_VIEW_MODE, Mode.HEAD_VS_WORKING_TREE.name()));
        } catch (IllegalArgumentException ex) {
            mode = null;
        }
        return mode == null ? Mode.HEAD_VS_WORKING_TREE : mode;
    }

    public void setLastUsedCommitViewMode (Mode mode) {
        getPreferences().put(PROP_LAST_USED_COMMIT_VIEW_MODE, mode.name());
    }
    
    public boolean getAutoOpenOutput() {
        return getPreferences().getBoolean(AUTO_OPEN_OUTPUT_WINDOW, true);
    }

    public void setAutoOpenOutput(boolean value) {
        getPreferences().putBoolean(AUTO_OPEN_OUTPUT_WINDOW, value);
    }

    public boolean getAutoReplaceInvalidBranchNameCharacters() {
        return getPreferences().getBoolean(AUTO_REPLACE_INVALID_BRANCH_NAME_CHARACTERS, true);
    }

    public void setAutoReplaceInvalidBranchNameCharacters(boolean value) {
        getPreferences().putBoolean(AUTO_REPLACE_INVALID_BRANCH_NAME_CHARACTERS, value);
    }    
    
    public void putRecentCommitAuthors(String author) {
        if(author == null) return;
        author = author.trim();
        if(author.isEmpty()) return;
        Utils.insert(getPreferences(), RECENT_COMMIT_AUTHORS, author, 10);
    }
    
    public void putRecentCommiter(String commiter) {
        if(commiter == null) return;
        commiter = commiter.trim();
        if(commiter.isEmpty()) return;
        Utils.insert(getPreferences(), RECENT_COMMITERS, commiter, 10);
    }
    
    public List<String> getRecentCommitAuthors() {
        return Utils.getStringList(getPreferences(), RECENT_COMMIT_AUTHORS);
    }
    
    public List<String> getRecentCommiters() {
        return Utils.getStringList(getPreferences(), RECENT_COMMITERS);
    }

    public void setSignOff(boolean value) {
        getPreferences().putBoolean(SIGN_OFF, value);
    }
    
    public boolean getSignOff() {
        return getPreferences().getBoolean(SIGN_OFF, false);
    }
    
    public void putRevertAll(boolean value) {
        getPreferences().putBoolean(REVERT_ALL, value);        
    }
    
    public void putRevertIndex(boolean value) {
        getPreferences().putBoolean(REVERT_INDEX, value);        
    }
    
    public void putRevertWT(boolean value) {
        getPreferences().putBoolean(REVERT_WT, value);        
    }
    
    public void putRemoveAllNew(boolean value) {
        getPreferences().putBoolean(REMOVE_ALL_NEW, value);        
    }
    
    public void putRemoveWTNew(boolean value) {
        getPreferences().putBoolean(REMOVE_WT_NEW, value);        
    }
    
    public boolean getRevertAll() {
        return getPreferences().getBoolean(REVERT_ALL, true);        
    }
    
    public boolean getRevertIndex() {
        return getPreferences().getBoolean(REVERT_INDEX, false);        
    }
    
    public boolean getRevertWT() {
        return getPreferences().getBoolean(REVERT_WT, true);        
    }
        
    public boolean getRemoveWTNew() {
        return getPreferences().getBoolean(REMOVE_WT_NEW, false);                
    }
    
    public boolean getRemoveAllNew() {
        return getPreferences().getBoolean(REMOVE_ALL_NEW, false);                
    }

    public boolean getAutoIgnoreFiles () {
        return getPreferences().getBoolean(AUTO_IGNORE_FILES, true);
    }

    public void setAutoIgnoreFiles (boolean flag) {
        getPreferences().putBoolean(AUTO_IGNORE_FILES, flag);
    }

    public boolean getShowCloneCompleted() {
        return getPreferences().getBoolean(SHOW_CLONE_COMPLETED, true);
    }
    
    public void setShowCloneCompleted(boolean bl) {
        getPreferences().putBoolean(SHOW_CLONE_COMPLETED, bl);
    }

    public boolean getShowHistoryMerges() {
        return getPreferences().getBoolean(KEY_SHOW_HISTORY_MERGES, true);
    }

    public void setShowHistoryMerges(boolean bShowMerges) {
        getPreferences().putBoolean(KEY_SHOW_HISTORY_MERGES, bShowMerges);
    }
    public boolean getShowFileInfo() {
        return getPreferences().getBoolean(KEY_SHOW_FILE_INFO, false);
    }
    public void setShowFileInfo(boolean info) {
        getPreferences().putBoolean(KEY_SHOW_FILE_INFO, info);
    }
    
    private final HashMap<String, ConnectionSettings> cachedConnectionSettings = new HashMap<String, ConnectionSettings>(5);
    public void insertRecentConnectionSettings (ConnectionSettings toStore) {
        assert !EventQueue.isDispatchThread();
        String guriString = getUriStringWithoutCredentials(toStore.getUri());
        if (guriString == null) {
            return;
        }        
        Preferences prefs = getPreferences();
        removeStaleEntries(prefs, guriString);
        
        if (toStore.isSaveCredentials() && (toStore.getPassphrase() != null || toStore.getPassword() != null)) {
            // keep permanently and remove from cache
            storeCredentials(toStore);
            cachedConnectionSettings.remove(guriString);
        } else {
            // remove from perm storage, we should not keep it forever
            deleteCredentials(toStore.getUri());
            // but keep until end of session
            cachedConnectionSettings.put(guriString, toStore);
        }
        
        if (!guriString.isEmpty()) {
            Utils.insert(prefs, RECENT_GURI, new GitConnectionSettingsEntry(guriString, toStore).toString(), -1);
        }
    }

    public void removeConnectionSettings (GitURI toRemove) {
        assert !EventQueue.isDispatchThread();
        String guriString = getUriStringWithoutCredentials(toRemove);
        if (guriString == null) {
            return;
        }        
        Preferences prefs = getPreferences();
        removeStaleEntries(prefs, guriString);
        
        cachedConnectionSettings.remove(guriString);
        deleteCredentials(toRemove);
    }

    private void removeStaleEntries (Preferences prefs, String guriString) {
        List<String> urlValues = Utils.getStringList(prefs, RECENT_GURI);
        for (Iterator<String> it = urlValues.iterator(); it.hasNext();) {
            String rcOldString = it.next();
            GitURI guriOld = null;
            try {
                GitConnectionSettingsEntry entry = GitConnectionSettingsEntry.create(rcOldString);
                if(entry != null) {
                    guriOld = new GitURI(entry.guriString);
                }
            } catch (URISyntaxException ex) {
                Git.LOG.log(Level.WARNING, rcOldString, ex);
            }
            if(guriString.equals(guriOld.toString())) {
                Utils.removeFromArray(prefs, RECENT_GURI, rcOldString);
            }
        }
    }    
    
    public List<ConnectionSettings> getRecentConnectionSettings () {
        assert !EventQueue.isDispatchThread();
        
        Preferences prefs = getPreferences();
        List<String> urls = Utils.getStringList(prefs, RECENT_GURI);
        List<ConnectionSettings> ret = new ArrayList<ConnectionSettings>(urls.size());
        for (String guriString : urls) {
            GitConnectionSettingsEntry entry = GitConnectionSettingsEntry.create(guriString);
            if (entry == null) {
                continue;
            }
            // before we contact keyring, check the cache:
            ConnectionSettings connSettings = cachedConnectionSettings.get(entry.guriString);
            if (connSettings == null) {
                connSettings = entry.toConnectionSettings();
                if (connSettings.isPrivateKeyAuth()) {
                    char[] passphrase = KeyringSupport.read(GURI_PASSPHRASE, connSettings.getUri().toString());
                    connSettings.setPassphrase(passphrase == null ? new char[0] : passphrase);
                } else {
                    char[] password = KeyringSupport.read(GURI_PASSWORD, connSettings.getUri().toString());
                    connSettings.setPassword(password == null ? new char[0] : password);
                }
            }
            ret.add(connSettings);
        }
        return ret;
    }
    
    public ConnectionSettings getConnectionSettings (String uriString) {
        assert !EventQueue.isDispatchThread();
        ConnectionSettings retval = null;
        String username = null;
        try {
            GitURI uri = new GitURI(uriString);
            username = uri.getUser();
            uriString = getUriStringWithoutCredentials(uri);
        } catch (URISyntaxException ex) {
            //
        }
        
        // before we contact keyring, check the cache:
        ConnectionSettings cachedSetting = cachedConnectionSettings.get(uriString);
        if (cachedSetting != null && (username == null || cachedSetting.getUser() == null || username.equals(cachedSetting.getUser()))) {
            return cachedSetting.copy();
        }
        
        Preferences prefs = getPreferences();
        List<String> urls = Utils.getStringList(prefs, RECENT_GURI);
        for (String guriString : urls) {
            GitConnectionSettingsEntry entry = GitConnectionSettingsEntry.create(guriString);
            if (entry == null) {
                continue;
            }
            ConnectionSettings storedSettings = entry.toConnectionSettings();
            if (uriString.equals(entry.guriString) && (username == null || storedSettings.getUser() == null || username.equals(storedSettings.getUser()))) {
                if (storedSettings.isPrivateKeyAuth()) {
                    char[] passphrase = KeyringSupport.read(GURI_PASSPHRASE, storedSettings.getUri().toString());
                    storedSettings.setPassphrase(passphrase == null ? new char[0] : passphrase);
                } else {
                    char[] password = KeyringSupport.read(GURI_PASSWORD, storedSettings.getUri().toString());
                    storedSettings.setPassword(password == null ? new char[0] : password);
                }
                retval = storedSettings;
                break;
            }
        }
        return retval;
    }

    private void storeCredentials (ConnectionSettings settings) {
        assert !EventQueue.isDispatchThread();
        GitURI uri = settings.getUri().setUser(settings.getUser());
        KeyringSupport.save(GURI_PASSWORD, uri.toString(), settings.getPassword(), null);
        KeyringSupport.save(GURI_PASSPHRASE, uri.toString(), settings.getPassphrase(), null);
    }
    
    private void deleteCredentials (GitURI guri) {
        assert !EventQueue.isDispatchThread();
        KeyringSupport.save(GURI_PASSWORD, guri.toString(), null, null);
        KeyringSupport.save(GURI_PASSPHRASE, guri.toString(), null, null);
    }

    private String getUriStringWithoutCredentials (GitURI guri) {
        String guriString = null;
        if (guri != null) {
            guriString = guri.setUser(null).setPass(null).toString();
        }
        return guriString;
    }

    public int getDiffViewMode (int def) {
        return getPreferences().getInt(PROP_DIFF_VIEW_MODE, def);
    }

    public void setDiffViewMode (int value) {
        getPreferences().putInt(PROP_DIFF_VIEW_MODE, value);
    }

    public int getStatusViewMode (int def) {
        return getPreferences().getInt(PROP_STATUS_VIEW_MODE, def);
    }

    public void setStatusViewMode (int value) {
        getPreferences().putInt(PROP_STATUS_VIEW_MODE, value);
    }
    
    public boolean isSearchOnlyCurrentBranchEnabled () {
        return getPreferences().getBoolean(KEY_SEARCH_ON_BRANCH, true);
    }

    public void setSearchOnlyCurrentBranchEnabled (boolean enabled) {
        getPreferences().putBoolean(KEY_SEARCH_ON_BRANCH, enabled);
    }
    
    public void setAnnotationDisplayedFields (int value) {
        getPreferences().putInt(KEY_ANNOTATION_DISPLAYED_FIELDS, value);
    }
    
    public int getAnnotationDisplayedFields (int defaultValue) {
        return getPreferences().getInt(KEY_ANNOTATION_DISPLAYED_FIELDS, defaultValue);
    }
    
    private static class GitConnectionSettingsEntry {
        final String guriString;
        private String stringValue;
        private final ConnectionSettings settings;
        static GitConnectionSettingsEntry create (String entryString) {
            String[] s = entryString.split(DELIMITER);
            assert s.length > 0;
            try {
                return new GitConnectionSettingsEntry(s[0], parse(s));
            } catch (URISyntaxException ex) {
                Git.LOG.log(Level.WARNING, "Cannot parse stored connection settings: {0}, {1}", new Object[] { s, ex.getMessage() });
                return null;
            }
        }
        GitConnectionSettingsEntry (String guriString, ConnectionSettings setts) {
            this.guriString = guriString;
            this.settings = setts;
        }
        @Override
        public String toString() {
            if(stringValue == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(guriString);
                sb.append(DELIMITER);
                sb.append(settings.getUser() == null ? "" : settings.getUser());
                sb.append(DELIMITER);
                sb.append(settings.isSaveCredentials() ? "1" : "0"); //NOI18N
                sb.append(DELIMITER);
                sb.append(settings.isPrivateKeyAuth() ? "1" : "0"); //NOI18N
                sb.append(DELIMITER);
                sb.append(settings.getIdentityFile());
                stringValue = sb.toString();
            }
            return stringValue;
        }

        private static ConnectionSettings parse (String[] s) throws URISyntaxException {
            String uri = s[0].trim();
            ConnectionSettings setts = new ConnectionSettings(new GitURI(uri));
            if (s.length > 1) {
                setts.setUser(s[1].isEmpty() ? null : s[1]);
            }
            if (s.length > 2) {
                setts.setSaveCredentials("1".equals(s[2]));
            }
            if (s.length > 3) {
                setts.setPrivateKeyAuth("1".equals(s[3]));
            }
            if (s.length > 4) {
                setts.setIdentityFile(s[4]);
            }
            return setts;
        }

        private ConnectionSettings toConnectionSettings () {
            return settings;
        }
    }
    
}
