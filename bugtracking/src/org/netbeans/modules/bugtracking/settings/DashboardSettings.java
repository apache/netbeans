/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.settings;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.modules.bugtracking.tasks.TaskAttribute;
import org.openide.util.NbPreferences;

/**
 *
 * @author jpeska
 */
public class DashboardSettings {

    public static final String TASKS_LIMIT_SETTINGS_CHANGED = "dashboard.task_limit_changed"; //NOI18N
    public static final String AUTO_SYNC_SETTINGS_CHANGED = "dashboard.auto_sync_changed"; //NOI18N
    public static final String SORT_ATTRIBUTES_SETTINGS_CHANGED = "dashboard.sort_attributes_changed"; //NOI18N

    private static DashboardSettings instance = null;
    private static final String AUTO_SYNC = "dashboard.auto_sync"; //NOI18N
    private static final String AUTO_SYNC_VALUE = "dashboard.auto_sync_value"; //NOI18N
    private static final String TASKS_LIMIT = "dashboard.tasks_limit"; //NOI18N
    private static final String TASKS_LIMIT_VALUE = "dashboard.tasks_limit_value"; //NOI18N
    private static final String TASKS_LIMIT_CATEGORY = "dashboard.tasks_limit_category"; //NOI18N
    private static final String TASKS_LIMIT_QUERY = "dashboard.tasks_limit_query"; //NOI18N
    private static final String FINISHED_TASK_FILTER = "dashboard.finished_task_filter"; //NOI18N
    private static final String SHOW_SCHEDULE = "dashboard.show_schedule"; //NOI18N
    /*
     * default values in fields
     */
    private static final boolean DEFAULT_AUTO_SYNC = true;
    private static final int DEFAULT_AUTO_SYNC_VALUE = Integer.parseInt(System.getProperty("bugtracking.dashboard.autosync.interval", "15"));
    private static final boolean DEFAULT_TASKS_LIMIT = Boolean.parseBoolean(System.getProperty("bugtracking.dashboard.autosync", "true"));
    private static final int DEFAULT_TASKS_LIMIT_VALUE = Integer.parseInt(System.getProperty("bugtracking.dashboard.tasks.limit", "50"));
    private static final boolean DEFAULT_TASKS_LIMIT_CATEGORY = false;
    private static final boolean DEFAULT_TASKS_LIMIT_QUERY = true;
    private static final boolean DEFAULT_FINISHED_TASK_FILTER = true;
    private static final boolean DEFAULT_SHOW_SCHEDULE = true;

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    private DashboardSettings() {
    }

    public static DashboardSettings getInstance() {
        if (instance == null) {
            instance = new DashboardSettings();
        }
        return instance;
    }

    public void addPropertyChangedListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangedListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public boolean isAutoSync() {
        return getPreferences().getBoolean(AUTO_SYNC, DEFAULT_AUTO_SYNC);
    }

    public void setAutoSync(boolean autoSync, boolean fireEvent) {
        getPreferences().putBoolean(AUTO_SYNC, autoSync);
        if (fireEvent) {
            fireSyncChangedEvent();
        }
    }

    public int getAutoSyncValue() {
        return getPreferences().getInt(AUTO_SYNC_VALUE, DEFAULT_AUTO_SYNC_VALUE);
    }

    public void setAutoSyncValue(int autoSyncValue, boolean fireEvent) {
        getPreferences().putInt(AUTO_SYNC_VALUE, autoSyncValue);
        if (fireEvent) {
            fireSyncChangedEvent();
        }
    }

    public boolean isTasksLimit() {
        return getPreferences().getBoolean(TASKS_LIMIT, DEFAULT_TASKS_LIMIT);
    }

    public void setTasksLimit(boolean tasksLimit, boolean fireEvent) {
        getPreferences().putBoolean(TASKS_LIMIT, tasksLimit);
        if (fireEvent) {
            fireLimitChangedEvent();
        }
    }

    public int getTasksLimitValue() {
        return getPreferences().getInt(TASKS_LIMIT_VALUE, DEFAULT_TASKS_LIMIT_VALUE);
    }

    public void setTasksLimitValue(int tasksLimitValue, boolean fireEvent) {
        getPreferences().putInt(TASKS_LIMIT_VALUE, tasksLimitValue);
        if (fireEvent) {
            fireLimitChangedEvent();
        }
    }

    public boolean isTasksLimitCategory() {
        return getPreferences().getBoolean(TASKS_LIMIT_CATEGORY, DEFAULT_TASKS_LIMIT_CATEGORY);
    }

    public void setTasksLimitCategory(boolean tasksLimitCategory, boolean fireEvent) {
        getPreferences().putBoolean(TASKS_LIMIT_CATEGORY, tasksLimitCategory);
        if (fireEvent) {
            fireLimitChangedEvent();
        }
    }

    public boolean isTasksLimitQuery() {
        return getPreferences().getBoolean(TASKS_LIMIT_QUERY, DEFAULT_TASKS_LIMIT_QUERY);
    }

    public void setTasksLimitQuery(boolean tasksLimitQuery, boolean fireEvent) {
        getPreferences().putBoolean(TASKS_LIMIT_QUERY, tasksLimitQuery);
        if (fireEvent) {
            fireLimitChangedEvent();
        }
    }

    public boolean showFinishedTasks() {
        return getPreferences().getBoolean(FINISHED_TASK_FILTER, DEFAULT_FINISHED_TASK_FILTER);
    }

    public void setShowFinishedTasks(boolean tasksLimitQuery) {
        getPreferences().putBoolean(FINISHED_TASK_FILTER, tasksLimitQuery);
    }

    public void updateSortingAttributes(List<TaskAttribute> attributes) {
        for (TaskAttribute taskAttribute : attributes) {
            taskAttribute.setRank(getPreferences().getInt(taskAttribute.getId(), taskAttribute.getRank()));
            taskAttribute.setAsceding(getPreferences().getBoolean(getAttributeAscedingId(taskAttribute.getId()), taskAttribute.isAsceding()));
        }
    }

    public void setSortingAttributes(List<TaskAttribute> attributes) {
        for (TaskAttribute taskAttribute : attributes) {
            getPreferences().putInt(taskAttribute.getId(), taskAttribute.getRank());
            getPreferences().putBoolean(getAttributeAscedingId(taskAttribute.getId()), taskAttribute.isAsceding());
        }
        fireSortChangedEvent();
    }

    public boolean showSchedule(String settingId) {
        return getPreferences().getBoolean(SHOW_SCHEDULE + "." + settingId, DEFAULT_FINISHED_TASK_FILTER);
    }

    public void updateShowSchedule(String settingId, boolean selected) {
        getPreferences().putBoolean(SHOW_SCHEDULE + "." + settingId, selected);
    }

    private String getAttributeAscedingId(String attributeId) {
        return attributeId + ".asceding";
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(DashboardSettings.class);
    }

    private void fireLimitChangedEvent() {
        support.firePropertyChange(TASKS_LIMIT_SETTINGS_CHANGED, null, null);
    }

    private void fireSyncChangedEvent() {
        support.firePropertyChange(AUTO_SYNC_SETTINGS_CHANGED, null, null);
    }

    private void fireSortChangedEvent() {
        support.firePropertyChange(SORT_ATTRIBUTES_SETTINGS_CHANGED, null, null);
    }
}
