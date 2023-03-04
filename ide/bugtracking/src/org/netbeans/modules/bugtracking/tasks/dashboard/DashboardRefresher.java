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
package org.netbeans.modules.bugtracking.tasks.dashboard;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.settings.DashboardSettings;
import org.netbeans.modules.bugtracking.tasks.DashboardUtils;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author jpeska
 */
public class DashboardRefresher {

    private static final Logger LOG = Logger.getLogger(DashboardRefresher.class.getName());
    
    private static final RequestProcessor RP = new RequestProcessor(DashboardRefresher.class.getName());
    private final Task refreshDashboard;
    private final Task refreshSchedule;
    private static DashboardRefresher instance;
    private boolean refreshEnabled;
    private boolean dashboardBusy = false;
    private boolean refreshWaiting = false;

    private DashboardRefresher() {
        refreshDashboard = RP.create(new Runnable() {
            @Override
            public void run() {
                
                LOG.finer("dashbord refresh start:");
                LOG.log(Level.FINER, "  dashboardBusy: {0}", dashboardBusy);
                LOG.log(Level.FINER, "  refreshEnabled: {0}", refreshEnabled);
                
                if (dashboardBusy || !refreshEnabled) {
                    refreshWaiting = true;
                    return;
                }
                try {
                    refreshDashboard();
                } finally {
                    setupDashboardRefresh();
                }
            }
        });

        refreshSchedule = RP.create(new Runnable() {
            @Override
            public void run() {
                try {
                    DashboardViewer.getInstance().updateScheduleCategories();
                } finally {
                    setupScheduleRefresh();
                }
            }
        });
    }

    public static DashboardRefresher getInstance() {
        if (instance == null) {
            instance = new DashboardRefresher();
        }
        return instance;
    }

    public void setupDashboardRefresh() {
        final DashboardSettings settings = DashboardSettings.getInstance();
        if (!settings.isAutoSync() || !refreshEnabled) {
            return;
        }
        refreshDashboard.cancel();
        scheduleDashboardRefresh();
    }

    public void setupScheduleRefresh() {
        refreshSchedule.cancel();
        refreshSchedule.schedule(DashboardUtils.getMillisToTomorrow());
    }

    private void scheduleDashboardRefresh() {
        final DashboardSettings settings = DashboardSettings.getInstance();
        int delay = settings.getAutoSyncValue();
        delay = delay * 60 * 1000;
        refreshDashboard.schedule(delay); // given in minutes
        LOG.log(Level.FINE, "tasks dashboard refresh scheduled in {0}", delay); 
    }

    public void setRefreshEnabled(boolean refreshEnabled) {
        this.refreshEnabled = refreshEnabled;
    }

    public void setDashboardBusy(boolean dashboardBusy) {
        this.dashboardBusy = dashboardBusy;
        if (!dashboardBusy && refreshWaiting) {
            refreshWaiting = false;
            refreshDashboard.schedule(0);
        }
    }

    private void refreshDashboard() {
        List<RepositoryImpl> repositories = DashboardViewer.getInstance().getRepositories(true);
        List<Category> categories = DashboardViewer.getInstance().getCategories(true, true);
        for (RepositoryImpl<?, ?, ?> repository : repositories) {
            for (QueryImpl query : repository.getQueries()) {
                if(DashboardUtils.isQueryAutoRefresh(query)) {
                    LOG.log(Level.INFO, "refreshing query {0} - {1}", new Object[]{query.getRepositoryImpl().getDisplayName(), query.getDisplayName()});
                    query.refresh();
                } else {
                    LOG.log(Level.FINE, "skipped refreshing query {0} - {1}", new Object[]{query.getRepositoryImpl().getDisplayName(), query.getDisplayName()});
                    
                }
            }
        }

        for (Category category : categories) {
            category.refresh();
        }
    }
}
