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

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.awt.event.ActionListener;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.netbeans.modules.notifications.linux.jna.Libnotify;

/**
 *
 * @author Hector Espert
 */
@ServiceProvider(service = LinuxNotificationDisplayer.class, position = 50)
public class LinuxNotificationDisplayer extends NotificationDisplayer {

    private static final Logger LOG = Logger.getLogger(LinuxNotificationDisplayer.class.getName());

    private static final String LIBNOTIFY = "libnotify.so.4";

    private static final String APP_NAME = "netbeans";

    private Optional<Libnotify> optionalLibnotify = Optional.empty();

    /**
     * Loads libnotify native library.
     */
    public void load() {
        if (isLoaded()) {
            LOG.log(Level.INFO, "Libnotify is already loaded");
        } else {
            try {
                this.optionalLibnotify = Optional.of(Native.load(LIBNOTIFY, Libnotify.class));
                LOG.log(Level.FINE, "Libnotify library loaded");
            } catch (UnsatisfiedLinkError unsatisfiedLinkError) {
                LOG.log(Level.WARNING, "Libnotify library not found", unsatisfiedLinkError);
                this.optionalLibnotify = Optional.empty();
            }
        }
    }

    public boolean isLoaded() {
        return optionalLibnotify.isPresent();
    }

    public boolean notLoaded() {
        return !isLoaded();
    }

    public boolean isStarted() {
        return optionalLibnotify.map(libnotify -> libnotify.notify_is_initted()).orElse(false);
    }

    public boolean notStarted() {
        return !isStarted();
    }

    public void start() {
        optionalLibnotify.filter(libnotify -> !libnotify.notify_is_initted())
                .ifPresent(libnotify -> {
                    if (libnotify.notify_init(APP_NAME)) {
                        LOG.log(Level.FINE, "Libnotify initted");
                    } else {
                        LOG.log(Level.WARNING, "Unable to init libnotify");
                    }
                });
    }

    public void stop() {
        optionalLibnotify.filter(libnotify -> libnotify.notify_is_initted())
                .ifPresent(libnotify -> {
                    libnotify.notify_uninit();
                    LOG.log(Level.FINE, "Libnotify uninit");
                });
    }

    @Override
    public Notification notify(String title, Icon icon, String detailsText, ActionListener detailsAction, Priority priority) {
        optionalLibnotify.filter(libnotify -> libnotify.notify_is_initted())
                .ifPresent(libnotify -> {
                    Pointer notification = libnotify.notify_notification_new(title, detailsText, null);
                    libnotify.notify_notification_show(notification, null);
                });

        return getFallbackNotificationDisplayer()
                .map(diplayer -> diplayer.notify(title, icon, detailsText, detailsAction, priority))
                .orElseThrow(FallbackLinuxNotificationException::new);
    }

    @Override
    public Notification notify(String title, Icon icon, JComponent balloonDetails, JComponent popupDetails, Priority priority) {
        return getFallbackNotificationDisplayer()
                .map(diplayer -> diplayer.notify(title, icon, balloonDetails, popupDetails, priority))
                .orElseThrow(FallbackLinuxNotificationException::new);
    }

    private Optional<NotificationDisplayer> getFallbackNotificationDisplayer() {
        return searchFallbackNotificationDisplayerInLookup(Lookup.getDefault());
    }

    private Optional<NotificationDisplayer> searchFallbackNotificationDisplayerInLookup(Lookup lookup) {
        return lookup.lookupAll(NotificationDisplayer.class)
                .stream()
                .filter((notificationDisplayer) -> !LinuxNotificationDisplayer.class.isInstance(notificationDisplayer))
                .map(NotificationDisplayer.class::cast)
                .findAny();
    }

}
