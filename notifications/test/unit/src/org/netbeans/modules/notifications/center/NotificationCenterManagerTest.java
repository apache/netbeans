/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.notifications.center;

import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.notifications.NotificationDisplayerImpl;
import org.netbeans.modules.notifications.NotificationImpl;
import org.netbeans.modules.notifications.filter.NotificationFilter;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Category;
import org.openide.util.ImageUtilities;

/**
 *
 * @author jpeska
 */
public class NotificationCenterManagerTest extends NbTestCase {

    NotificationCenterManager manager;

    public NotificationCenterManagerTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        manager = NotificationCenterManager.getInstance();
        manager.deleteAll();
        manager.setActiveFilter(NotificationFilter.EMPTY);
        manager.setMessageFilter("");
    }

    public void testCreateNotification() {
        int size = manager.getTotalCount();
        assertEquals(0, size);

        NotificationImpl n = (NotificationImpl) createNotification(Category.INFO, "Title1");
        size = manager.getTotalCount();
        assertEquals(1, size);

        NotificationImpl last = manager.getLastUnreadNotification();
        assertNotNull(last);
        assertEquals(n, last);

        assertEquals(n.getTitle(), last.getTitle());
        assertEquals(n.getCategory(), last.getCategory());
    }

    public void testClearNotification() {
        int size = manager.getTotalCount();
        assertEquals(0, size);

        Notification n1 = createNotification();
        Notification n2 = createNotification();
        size = manager.getTotalCount();
        assertEquals(2, size);

        n2.clear();
        size = manager.getTotalCount();
        assertEquals(1, size);
    }

    public void testMarkAsRead() {
        int unread = manager.getUnreadCount();
        assertEquals(0, unread);

        Notification n1 = createNotification();
        NotificationImpl n2 = (NotificationImpl) createNotification();
        unread = manager.getUnreadCount();
        assertEquals(2, unread);

        n2.markAsRead(true);
        unread = manager.getUnreadCount();
        assertEquals(1, unread);
    }

    public void testTitleFilter() {
        int filtered = manager.getFilteredCount();
        assertEquals(0, filtered);

        Notification n1 = createNotification(Category.INFO, "Title1");
        NotificationImpl n2 = (NotificationImpl) createNotification(Category.INFO, "Title2");
        filtered = manager.getFilteredCount();
        assertEquals(2, filtered);

        manager.setMessageFilter("Title2");
        filtered = manager.getFilteredCount();
        assertEquals(1, filtered);
        NotificationImpl get = manager.getFilteredNotifications().get(0);
        assertEquals(n2, get);

        manager.setMessageFilter("Title12");
        filtered = manager.getFilteredCount();
        assertEquals(0, filtered);
    }

    public void testLastNotification() {
        assertEquals(null, manager.getLastUnreadNotification());
        
        NotificationImpl n1 = (NotificationImpl) createNotification(Category.INFO, "Title1");
        assertEquals(n1, manager.getLastUnreadNotification());

        NotificationImpl n2 = (NotificationImpl) createNotification(Category.INFO, "Title2");
        assertEquals(n2, manager.getLastUnreadNotification());

        n2.markAsRead(true);
        assertEquals(n1, manager.getLastUnreadNotification());
    }

    private Notification createNotification() {
        String title = "Title";
        return createNotification(Category.INFO, title);
    }

    private Notification createNotification(Category category, String title) {
        String dummyText = "<html>The Netbeans IDE has detected that your system is using most of your available system resources. We recommend shutting down other applications and windows.</html>";
        return NotificationDisplayerImpl.getInstance().notify(title,
                new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/notifications/resources/filter.png")),
                new JLabel(dummyText), new JLabel(dummyText),
                NotificationDisplayer.Priority.NORMAL,
                category);
    }
}
