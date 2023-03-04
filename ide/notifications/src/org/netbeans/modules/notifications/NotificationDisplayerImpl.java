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
package org.netbeans.modules.notifications;

import org.netbeans.modules.notifications.center.NotificationCenterManager;
import java.awt.event.ActionListener;
import java.io.CharConversionException;
import java.util.Calendar;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.xml.XMLUtil;

/**
 * Implementation of NotificationDisplayer which shows new Notifications as balloon-like tooltips and the list of all Notifications in a output window.
 *
 * @since 1.14
 * @author S. Aubrecht
 * @author jpeska
 */
@ServiceProviders({
    @ServiceProvider(service = NotificationDisplayer.class, position = 100),
    @ServiceProvider(service = NotificationDisplayerImpl.class)})
public final class NotificationDisplayerImpl extends NotificationDisplayer {

    private final NotificationCenterManager notificationCenter = NotificationCenterManager.getInstance();

    public NotificationDisplayerImpl() {
    }

    public static NotificationDisplayerImpl getInstance() {
        return Lookup.getDefault().lookup(NotificationDisplayerImpl.class);
    }

    @Override
    public Notification notify(String title, Icon icon, String detailsText, ActionListener detailsAction, Priority priority) {
        return notify(title, icon, detailsText, detailsAction, priority, Category.INFO);
    }

    @Override
    public Notification notify(String title, Icon icon, String detailsText, ActionListener detailsAction, Priority priority, Category category) {
        Parameters.notNull("detailsText", detailsText); //NOI18N

        NotificationImpl n = createNotification(title, icon, priority, category);
        n.setDetails(detailsText, detailsAction);
        add(n);
        return n;
    }

    @Override
    public Notification notify(String title, Icon icon, JComponent balloonDetails, JComponent popupDetails, Priority priority) {
        return notify(title, icon, balloonDetails, popupDetails, priority, Category.INFO);
    }

    @Override
    public Notification notify(String title, Icon icon, JComponent balloonDetails, JComponent popupDetails, Priority priority, Category category) {
        Parameters.notNull("balloonDetails", balloonDetails); //NOI18N
        Parameters.notNull("popupDetails", popupDetails); //NOI18N

        NotificationImpl n = createNotification(title, icon, priority, category);
        n.setDetails(balloonDetails, popupDetails);
        add(n);
        return n;
    }

    private void add(NotificationImpl n) {
        notificationCenter.add(n);
    }

    private NotificationImpl createNotification(String title, Icon icon, Priority priority, Category category) {
        Parameters.notNull("title", title); //NOI18N
        Parameters.notNull("icon", icon); //NOI18N
        Parameters.notNull("priority", priority); //NOI18N
        Parameters.notNull("category", category); //NOI18N

        try {
            title = XMLUtil.toElementContent(title);
        } catch (CharConversionException ex) {
            throw new IllegalArgumentException(ex);
        }
        return new NotificationImpl(title, icon, priority, category, Calendar.getInstance());
    }
}
