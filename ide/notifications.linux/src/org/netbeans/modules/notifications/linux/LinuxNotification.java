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
package org.netbeans.modules.notifications.linux;

import com.sun.jna.Pointer;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Objects;
import static java.util.Objects.nonNull;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.modules.notifications.linux.jna.Libnotify;
import org.netbeans.modules.notifications.spi.Notification;
import org.netbeans.modules.notifications.spi.NotificationListener;
import org.openide.awt.NotificationDisplayer;

/**
 *
 * @author Hector Espert
 */
public class LinuxNotification extends Notification {

    private final Libnotify libnotify;

    private final String title;

    private final Icon icon;

    private final NotificationDisplayer.Category category;

    private final NotificationDisplayer.Priority priority;

    private final Calendar dateCreated;

    private final String detailsText;

    private final ActionListener actionListener;

    private boolean readed;

    private NotificationListener notificationListener;

    private Pointer pointer;

    public LinuxNotification(Libnotify libnotify,
            String title,
            Icon icon,
            NotificationDisplayer.Priority priority,
            NotificationDisplayer.Category category,
            Calendar dateCreated,
            String detailsText,
            ActionListener actionListener) {
        this.libnotify = libnotify;
        this.title = title;
        this.icon = icon;
        this.category = category;
        this.priority = priority;
        this.dateCreated = dateCreated;
        this.detailsText = detailsText;
        this.actionListener = actionListener;
        this.readed = false;
    }

    public LinuxNotification(Libnotify libnotify,
            String title,
            Icon icon,
            NotificationDisplayer.Priority priority,
            NotificationDisplayer.Category category,
            Calendar dateCreated,
            JComponent balloonComponent,
            JComponent detailsComponent) {
        this.libnotify = libnotify;
        this.title = title;
        this.icon = icon;
        this.category = category;
        this.priority = priority;
        this.dateCreated = dateCreated;
        this.detailsText = null;
        this.actionListener = null;
        this.readed = false;
    }

    @Override
    public void initDecorations() {
        if (isLibnotifyInitted()) {
            Pointer notificationPointer = libnotify.notify_notification_new(title, detailsText, null);
            if (nonNull(notificationPointer) && libnotify.notify_notification_show(notificationPointer, null)) {
                this.pointer = notificationPointer;
            }
        }
    }

    private boolean isLibnotifyInitted() {
        return nonNull(libnotify) && libnotify.notify_is_initted();
    }

    @Override
    public NotificationDisplayer.Priority getPriority() {
        return priority;
    }

    @Override
    public NotificationDisplayer.Category getCategory() {
        return category;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setNotificationListener(NotificationListener notificationListener) {
        this.notificationListener = notificationListener;
    }

    @Override
    public boolean isRead() {
        return readed;
    }

    @Override
    public void markAsRead(boolean readed) {
        if (this.readed != readed) {
            this.readed = readed;
            if (Objects.nonNull(notificationListener)) {
                notificationListener.wasRead(this);
            }
        }
    }

    @Override
    public boolean showBallon() {
        return false;
    }

    @Override
    public JComponent getBalloonComp() {
        return null;
    }

    @Override
    public Calendar getDateCreated() {
        return dateCreated;
    }

    @Override
    public JComponent getDetailsComponent() {
        return new JLabel("Test");
    }

    @Override
    public void clear() {
        if (Objects.nonNull(notificationListener)) {
            notificationListener.delete(this);
        }

        if (isLibnotifyInitted() && nonNull(pointer)) {
            libnotify.notify_notification_close(pointer, null);
            libnotify.g_object_unref(pointer);
        }
    }

}
