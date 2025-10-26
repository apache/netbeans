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
package org.netbeans.modules.notifications.center;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.notifications.NotificationImpl;
import org.netbeans.modules.notifications.filter.FilterRepository;
import org.netbeans.modules.notifications.filter.TitleFilter;
import org.netbeans.modules.notifications.filter.NotificationFilter;
import org.openide.awt.NotificationDisplayer.Category;

/**
 *
 * @author jpeska
 */
public class NotificationCenterManager {

    public static final String PROP_NOTIFICATIONS_CHANGED = "notificationsChanged"; //NOI18N
    public static final String PROP_NOTIFICATION_ADDED = "notificationAdded"; //NOI18N
    public static final String PROP_NOTIFICATION_READ = "notificationRead"; //NOI18N
    private static final int NOTIFICATIONS_CAPACITY = 100;
    private static final PropertyChangeSupport propSupport = new PropertyChangeSupport(NotificationCenterManager.class);
    private static NotificationCenterManager instance = null;
    private final List<NotificationImpl> notifications = new ArrayList<>();
    private final List<NotificationImpl> filteredNotifications = new ArrayList<>();
    private NotificationTable notificationTable;
    private final FilterRepository filterRepository;
    private NotificationFilter notificationFilter;
    private TitleFilter titleFilter;

    private NotificationCenterManager() {
        filterRepository = FilterRepository.getInstance();
        loadFilters();
    }

    public static NotificationCenterManager getInstance() {
        if (instance == null) {
            instance = new NotificationCenterManager();
        }
        return instance;
    }

    public void add(NotificationImpl notification) {
        boolean capacityFull;
        synchronized (notifications) {
            capacityFull = notifications.size() == NOTIFICATIONS_CAPACITY;
            if (capacityFull) {
                notifications.remove(0).clear();
            }
            notifications.add(notification);
        }
            if (isEnabled(notification)) {
                filteredNotifications.add(notification);
            firePropertyChange(PROP_NOTIFICATION_ADDED, notification);
        }
        updateTable(capacityFull);
    }

    public void delete(NotificationImpl notification) {
        synchronized (notifications) {
            if (!notifications.remove(notification)) {
                return;
            }
        }
            if (isEnabled(notification)) {
                filteredNotifications.remove(notification);
            if (!notification.isRead()) {
                firePropertyChange(PROP_NOTIFICATION_READ, notification);
            }
        }
        updateTable(false);
    }

    public void updateTable(boolean filter) {
        if (filter) {
            filterNotifications();
        }
        SwingUtilities.invokeLater(() -> {
            NotificationTableModel model = getModel();
            synchronized (notifications) {
                model.setEntries(filteredNotifications);
            }
        });
    }

    public void update(NotificationImpl n) {
        final int index;
        synchronized (notifications) {
            index = filteredNotifications.indexOf(n);
        }
        if (index != -1) {
            SwingUtilities.invokeLater(() -> {
                NotificationTableModel model = getModel();
                model.updateIndex(index);
            });
        }
    }

    public void deleteAll() {
        synchronized (notifications) {
            notifications.clear();
            filteredNotifications.clear();
            firePropertyChange(PROP_NOTIFICATIONS_CHANGED, null);
            updateTable(false);
        }
    }

    public void markAllRead() {
        synchronized (notifications) {
            for (NotificationImpl n : notifications) {
                n.markAsRead(true);
            }
        }
    }

    public List<Category> getCategories() {
        return Category.getCategories();
    }

    public void wasRead(NotificationImpl notification) {
        firePropertyChange(PROP_NOTIFICATION_READ, notification);
        update(notification);
    }

    public NotificationFilter getActiveFilter() {
        return notificationFilter;
    }

    public JComponent getComponent() {
        return getTable();
    }

    private NotificationTableModel getModel() {
        return (NotificationTableModel) getTable().getModel();
    }

    public int getUnreadCount() {
        int count = 0;
        synchronized (notifications) {
            for (NotificationImpl notification : notifications) {
                if (!notification.isRead()) {
                    count++;
                }
            }
        }
        return count;
    }

    public NotificationImpl getLastUnreadNotification() {
        synchronized (notifications) {
            for (int i = filteredNotifications.size() - 1; i >= 0; i--) {
                NotificationImpl n = filteredNotifications.get(i);
                if (!n.isRead()) {
                    return n;
                }
            }
        }
        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propSupport.removePropertyChangeListener(l);
    }

    private void firePropertyChange(final String propName, final NotificationImpl notification) {
        Runnable r = () -> {
            if (PROP_NOTIFICATION_ADDED.equals(propName)) {
                notification.initDecorations();
            }
            propSupport.firePropertyChange(propName, null, notification);
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    static void tcClosed() {
        try {
            FilterRepository.getInstance().save();
        } catch (IOException ioE) {
            getLogger().log(Level.INFO, null, ioE);
        }
    }

    static Logger getLogger() {
        return Logger.getLogger(NotificationCenterManager.class.getName());
    }

    public boolean isEnabled(NotificationImpl notification) {
        boolean categoryEnabled = notificationFilter == null || (notificationFilter != null && notificationFilter.isEnabled(notification));
        boolean titleEnabled = true;
        if (categoryEnabled) {//save unnecessary condition check
            titleEnabled = titleFilter == null ? true : titleFilter.isEnabled(notification.getTitle());
        }
        return categoryEnabled && titleEnabled;
    }

    private void filterNotifications() {
        notificationFilter = filterRepository.getActive();
        synchronized (notifications) {
            filteredNotifications.clear();
            for (NotificationImpl notification : notifications) {
                if (isEnabled(notification)) {
                    filteredNotifications.add(notification);
                }
            }
        }
        firePropertyChange(PROP_NOTIFICATIONS_CHANGED, null);
    }

    void setMessageFilter(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            titleFilter = null;
        } else {
            titleFilter = new TitleFilter(searchText);
        }
        updateTable(true);
    }

    private void loadFilters() {
        new Thread(() -> {
            try {
                filterRepository.load();
            } catch (IOException ioE) {
                getLogger().log(Level.INFO, ioE.getMessage(), ioE);
            }
            if (notificationTable != null) {
                updateTable(true);
            } else {
                notificationFilter = filterRepository.getActive();
            }
        }).start();
    }

    private NotificationTable getTable() {
        if (notificationTable == null) {
            notificationTable = new NotificationTable();
        }
        return notificationTable;
    }

    /**
     * for testing
     */
    void setActiveFilter(NotificationFilter notificationFilter) {
        this.notificationFilter = notificationFilter;
        filterNotifications();
    }

    /**
     * for testing
     */
    int getTotalCount() {
        int count;
        synchronized (notifications) {
            count = notifications.size();
        }
        return count;
    }

    /**
     * for testing
     */
    int getFilteredCount() {
        int count;
        synchronized (notifications) {
            count = filteredNotifications.size();
        }
        return count;
    }

    /**
     * for testing
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    List<NotificationImpl> getFilteredNotifications() {
        return filteredNotifications;
    }
}
