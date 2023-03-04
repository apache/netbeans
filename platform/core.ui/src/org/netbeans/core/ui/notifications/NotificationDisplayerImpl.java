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

package org.netbeans.core.ui.notifications;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.xml.XMLUtil;

/**
 * Implementation of NotificationDisplayer which shows new Notifications as
 * balloon-like tooltips and the list of all Notifications in a popup window.
 *
 * @since 1.14
 * @author S. Aubrecht
 */
@ServiceProviders({@ServiceProvider(service=NotificationDisplayer.class), 
    @ServiceProvider(service=NotificationDisplayerImpl.class)})
public final class NotificationDisplayerImpl extends NotificationDisplayer {

    static final String PROP_NOTIFICATION_ADDED = "notificationAdded"; //NOI18N
    static final String PROP_NOTIFICATION_REMOVED = "notificationRemoved"; //NOI18N

    private static final List<NotificationImpl> model = new LinkedList<NotificationImpl>();
    private static final PropertyChangeSupport propSupport = new PropertyChangeSupport(NotificationDisplayerImpl.class);
    
    public NotificationDisplayerImpl() {
    }

    static NotificationDisplayerImpl getInstance() {
        return Lookup.getDefault().lookup(NotificationDisplayerImpl.class);
    }

    @Override
    public Notification notify(String title, Icon icon, String detailsText, ActionListener detailsAction, Priority priority) {
        Parameters.notNull("detailsText", detailsText); //NOI18N

        NotificationImpl n = createNotification( title, icon, priority );
        n.setDetails( detailsText, detailsAction );
        add( n );
        return n;
    }

    @Override
    public Notification notify(String title, Icon icon, JComponent balloonDetails, JComponent popupDetails, Priority priority) {
        Parameters.notNull("balloonDetails", balloonDetails); //NOI18N
        Parameters.notNull("popupDetails", popupDetails); //NOI18N

        NotificationImpl n = createNotification(title, icon, priority);
        n.setDetails( balloonDetails, popupDetails );
        add( n );
        return n;
    }

    /**
     * Adds given Notification to the model, fires property change.
     * @param n
     */
    void add( NotificationImpl n ) {
        synchronized( model ) {
            model.add(n);
            Collections.sort(model);
        }
        firePropertyChange( PROP_NOTIFICATION_ADDED, n );
    }

    /**
     * Removes given Notification from the model, fires property change.
     * @param n
     */
    void remove( NotificationImpl n ) {
        synchronized( model ) {
            if( !model.contains(n) )
                return;
            model.remove(n);
        }
        firePropertyChange( PROP_NOTIFICATION_REMOVED, n );
    }

    /**
     * @return The count of active notifications.
     */
    int size() {
        synchronized( model ) {
            return model.size();
        }
    }

    /**
     * @return List of all notifications.
     */
    List<NotificationImpl> getNotifications() {
        List<NotificationImpl> res = null;
        synchronized( model ) {
            res = new ArrayList<NotificationImpl>(model);
        }
        return res;
    }

    /**
     * @return The most important notification.
     */
    NotificationImpl getTopNotification() {
        NotificationImpl res = null;
        synchronized( model ) {
            if( !model.isEmpty() )
                res = model.get(0);
        }
        return res;
    }

    void addPropertyChangeListener( PropertyChangeListener l ) {
        propSupport.addPropertyChangeListener(l);
    }

    void removePropertyChangeListener( PropertyChangeListener l ) {
        propSupport.removePropertyChangeListener(l);
    }

    private void firePropertyChange(final String propName, final NotificationImpl notification) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if( PROP_NOTIFICATION_ADDED.equals(propName) ) {
                    notification.initDecorations();
                }
                propSupport.firePropertyChange(propName, null, notification);
            }
        };
        if( SwingUtilities.isEventDispatchThread() ) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private NotificationImpl createNotification(String title, Icon icon, Priority priority) {
        Parameters.notNull("title", title); //NOI18N
        Parameters.notNull("icon", icon); //NOI18N
        Parameters.notNull("priority", priority); //NOI18N

        try {
            title = XMLUtil.toElementContent(title);
        } catch( CharConversionException ex ) {
            throw new IllegalArgumentException(ex);
        }

        return new NotificationImpl(title, icon, priority);
    }
}
