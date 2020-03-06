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
package org.netbeans.modules.notifications.spi;

import java.util.Calendar;
import javax.swing.JComponent;
import org.openide.awt.NotificationDisplayer;

/**
 * Represents a notification that can be managed in a @see NotificationCenterManager
 * @author Hector Espert
 */
public abstract class Notification extends org.openide.awt.Notification implements Comparable<Notification> {

    /**
     * Get notification priority.
     * @return NotificationDisplayer.Priority
     */
    public abstract NotificationDisplayer.Priority getPriority();
    
    /**
     * Get notification category.
     * @return NotificationDisplayer.Category
     */
    public abstract NotificationDisplayer.Category getCategory();
    
    /**
     * Notification title.
     * @return String
     */
    public abstract String getTitle();
    
    public abstract void setNotificationListener(NotificationListener notificationListener);
    
    public abstract boolean isRead();
    
    public abstract void markAsRead(boolean readed);
    
    public abstract boolean showBallon();

    public abstract JComponent getBalloonComp();

    public abstract void initDecorations();

    public abstract Calendar getDateCreated();

    public abstract JComponent getDetailsComponent();
    
    @Override
    public int compareTo(Notification notification) {
        int res = getPriority().compareTo(notification.getPriority());
        if (0 == res) {
            res = getCategory().getDisplayName().compareTo(notification.getCategory().getDisplayName());
        }
        if (0 == res) {
            res = getTitle().compareTo(notification.getTitle());
        }
        return res;
    }
    
}
