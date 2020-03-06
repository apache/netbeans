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
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.lookup.ServiceProvider;
import org.netbeans.modules.notifications.linux.jna.Libnotify;
import org.netbeans.modules.notifications.spi.Notification;
import org.netbeans.modules.notifications.spi.NotificationFactory;

/**
 *
 * @author Hector Espert
 */
@ServiceProvider(service = LinuxNotificationFactory.class, position = 50)
public class LinuxNotificationFactory extends NotificationFactory {

    private static final Logger LOG = Logger.getLogger(LinuxNotificationFactory.class.getName());

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
    public Notification createNotification(String title, Icon icon, NotificationDisplayer.Priority priority, NotificationDisplayer.Category category, String detailsText, ActionListener actionListener) {
        return new LinuxNotification(optionalLibnotify.orElse(null), title, icon, priority, category, Calendar.getInstance(), detailsText, actionListener);
    }

    @Override
    public Notification createNotification(String title, Icon icon, NotificationDisplayer.Priority priority, NotificationDisplayer.Category category, JComponent balloonComponent, JComponent detailsComponent) {
        return new LinuxNotification(optionalLibnotify.orElse(null), title, icon, priority, category, Calendar.getInstance(), balloonComponent, detailsComponent);
    }

}
