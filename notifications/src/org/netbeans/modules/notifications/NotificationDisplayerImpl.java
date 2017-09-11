/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
