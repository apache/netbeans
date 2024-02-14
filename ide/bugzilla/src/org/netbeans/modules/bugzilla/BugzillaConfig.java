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

package org.netbeans.modules.bugzilla;

import java.net.URL;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.openide.modules.Places;
import org.openide.util.ImageUtilities;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaConfig {

    private static BugzillaConfig instance = null;
    private static final String LAST_CHANGE_FROM    = "bugzilla.last_change_from";      // NOI18N // XXX
    private static final String QUERY_NAME          = "bugzilla.query_";                // NOI18N
    private static final String QUERY_LAST_REFRESH  = "bugzilla.query_last_refresh";    // NOI18N
    private static final String DELIMITER           = "<=>";                            // NOI18N
    private static final String ATTACH_LOG          = "bugzilla.attach_log";            // NOI18N;
    private static final String PREF_SECTION_COLLAPSED = "collapsedSection"; //NOI18N
    private static final String PREF_TASK = "task."; //NOI18N
    private static final Level LOG_LEVEL = BugzillaUtil.isAssertEnabled() ? Level.SEVERE : Level.INFO;

    public static final int DEFAULT_QUERY_REFRESH = 30;
    public static final int DEFAULT_ISSUE_REFRESH = 15;
    private Map<String, Icon> priorityIcons;
    private Map<String, URL> priorityIconsURL;

    private BugzillaConfig() { }

    public static BugzillaConfig getInstance() {
        if(instance == null) {
            instance = new BugzillaConfig();
        }
        return instance;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(BugzillaConfig.class);
    }

    public boolean getAttachLogFile() {
        return getPreferences().getBoolean(ATTACH_LOG, true);
    }
    
    public void putAttachLogFile(boolean attach) {
        getPreferences().putBoolean(ATTACH_LOG, attach);
    }
    
    public void putQuery(BugzillaRepository repository, BugzillaQuery query) {
        getPreferences().put(
                getQueryKey(repository.getID(), query.getDisplayName()),
                query.getUrlParameters() + DELIMITER + /* skip query.getLastRefresh() + */ DELIMITER + query.isUrlDefined());
    }

    public void removeQuery(BugzillaRepository repository, BugzillaQuery query) {
        getPreferences().remove(getQueryKey(repository.getID(), query.getDisplayName()));
        try {
            String storedName = query.getStoredQueryName();
            IRepositoryQuery iquery = storedName == null ? null 
                    : MylynSupport.getInstance().getRepositoryQuery(repository.getTaskRepository(), storedName);
            if (iquery != null) {
                MylynSupport.getInstance().deleteQuery(iquery);
            }
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.WARNING, null, ex);
        }
    }

    public BugzillaQuery getQuery(BugzillaRepository repository, String queryName) {
        String value = getStoredQuery(repository, queryName);
        if(value == null) {
            return null;
        }
        String[] values = value.split(DELIMITER);
        assert values.length >= 2 : "wrong amount of stored query data [" + values.length + "] in query '" + queryName + "'"; // NOI18N
        String urlParams = values[0];
        boolean urlDef = values.length > 2 ? Boolean.parseBoolean(values[2]) : false;
        return repository.createPersistentQuery(queryName, urlParams, urlDef);
    }

    public String getUrlParams(BugzillaRepository repository, String queryName) {
        String value = getStoredQuery(repository, queryName);
        if(value == null) {
            return null;
        }
        String[] values = value.split(DELIMITER);
        assert values.length >= 2;
        return values[0];
    }

    public String[] getQueries(String repoID) {
        return getKeysWithPrefix(QUERY_NAME + repoID + DELIMITER);
    }

    public long getLastQueryRefresh(BugzillaRepository repository, String queryName) {
        return getPreferences().getLong(QUERY_LAST_REFRESH + "_" + getQueryKey(repository.getID(), queryName), -1); // NOI18N
    }
    
    public void putLastQueryRefresh(BugzillaRepository repository, String queryName, long lastRefresh) {
        getPreferences().putLong(QUERY_LAST_REFRESH + "_" + getQueryKey(repository.getID(), queryName), lastRefresh); // NOI18N
    }
    
    private String[] getKeysWithPrefix(String prefix) {
        String[] keys = null;
        try {
            keys = getPreferences().keys();
        } catch (BackingStoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex); // XXX
        }
        if (keys == null || keys.length == 0) {
            return new String[0];
        }
        List<String> ret = new ArrayList<>();
        for (String key : keys) {
            if (key.startsWith(prefix)) {
                ret.add(key.substring(prefix.length()));
            }
        }
        return ret.toArray(new String[0]);
    }

    private String getQueryKey(String repositoryID, String queryName) {
        return QUERY_NAME + repositoryID + DELIMITER + queryName;
    }

    private String getStoredQuery(BugzillaRepository repository, String queryName) {
        String value = getPreferences().get(getQueryKey(repository.getID(), queryName), null);
        return value;
    }

    public void setLastChangeFrom(String value) {
        getPreferences().put(LAST_CHANGE_FROM, value);
    }

    public String getLastChangeFrom() {
        return getPreferences().get(LAST_CHANGE_FROM, "");                      // NOI18N
    }

    public Icon getPriorityIcon(String priority) {
        if(priorityIcons == null) {
            priorityIcons = new HashMap<String, Icon>();
            priorityIcons.put("P1", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p1.png", true)); // NOI18N
            priorityIcons.put("P2", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p2.png", true)); // NOI18N
            priorityIcons.put("P3", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p3.png", true)); // NOI18N
            priorityIcons.put("P4", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p4.png", true)); // NOI18N
            priorityIcons.put("P5", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p5.png", true)); // NOI18N
        }
        return priorityIcons.get(priority);
    }

    public URL getPriorityIconURL(String priority) {
        if(priorityIconsURL == null) {
            priorityIconsURL = new HashMap<>();
            priorityIconsURL.put("P1", BugzillaConfig.class.getClassLoader().getResource("org/netbeans/modules/bugzilla/resources/p1.png")); // NOI18N
            priorityIconsURL.put("P2", BugzillaConfig.class.getClassLoader().getResource("org/netbeans/modules/bugzilla/resources/p2.png")); // NOI18N
            priorityIconsURL.put("P3", BugzillaConfig.class.getClassLoader().getResource("org/netbeans/modules/bugzilla/resources/p3.png")); // NOI18N
            priorityIconsURL.put("P4", BugzillaConfig.class.getClassLoader().getResource("org/netbeans/modules/bugzilla/resources/p4.png")); // NOI18N
            priorityIconsURL.put("P5", BugzillaConfig.class.getClassLoader().getResource("org/netbeans/modules/bugzilla/resources/p5.png")); // NOI18N
        }
        return priorityIconsURL.get(priority);
    }
    
    public void setEditorSectionCollapsed (String repositoryId, String taskId, String sectionName, boolean collapsed) {
        String key = getTaskKey(repositoryId, taskId) + PREF_SECTION_COLLAPSED + sectionName;
        getPreferences().putBoolean(key, collapsed);
    }

    public boolean isEditorSectionCollapsed (String repositoryId, String taskId, String sectionName, boolean defaultValue) {
        String key = getTaskKey(repositoryId, taskId) + PREF_SECTION_COLLAPSED + sectionName;
        return getPreferences().getBoolean(key, defaultValue);
    }

    private String getTaskKey (String repositoryId, String taskId) {
        return PREF_TASK + repositoryId + "." + taskId + ".";
    }

    /**
     * Returns the path for the Bugzilla configuration directory.
     *
     * @return the path
     *
     */
    private static String getNBConfigPath() {
        //T9Y - nb bugzilla confing should be changable
        String t9yNbConfigPath = System.getProperty("netbeans.t9y.bugzilla.nb.config.path"); //NOI18N
        if (t9yNbConfigPath != null && t9yNbConfigPath.length() > 0) {
            return t9yNbConfigPath;
        }
        String nbHome = Places.getUserDirectory().getAbsolutePath();            //NOI18N
        return nbHome + "/config/issue-tracking/org-netbeans-modules-bugzilla"; //NOI18N
    }
}
