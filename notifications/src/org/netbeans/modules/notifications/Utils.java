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
package org.netbeans.modules.notifications;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.ACCELERATOR_KEY;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import org.netbeans.modules.notifications.center.NotificationCenterManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author jpeska
 */
public class Utils {

    private static final int DATE_MINUTES_AGO = 0;
    private static final int DATE_TODAY = 1;
    private static final int DATE_YESTERDAY = 2;
    private static final int DATE_THIS_WEEK = 3;
    private static final int DATE_FULL = 4;
    private static final DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
    private static final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
    private static final KeyStroke DELETE_KEY = KeyStroke.getKeyStroke("DELETE"); //NOI18N

    public static String getFormatedDate(Calendar calendar) {
        if (calendar == null) {
            return "";
        }
        int evaluation = evaluateDate(calendar);
        switch (evaluation) {
            case DATE_MINUTES_AGO:
                int minutes = calculateMinutes(calendar, Calendar.getInstance());
                return NbBundle.getMessage(Utils.class, minutes == 1 ? "LBL_MinuteAgo" : "LBL_MinutesAgo", minutes);
            case DATE_TODAY:
                return NbBundle.getMessage(Utils.class, "LBL_Today", timeFormat.format(calendar.getTime()));
            case DATE_YESTERDAY:
                return NbBundle.getMessage(Utils.class, "LBL_Yesterday", timeFormat.format(calendar.getTime()));
            default:
                return dateFormat.format(calendar.getTime());
        }
    }

    public static String getFullFormatedDate(Calendar calendar) {
        if (calendar == null) {
            return "";
        }
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(calendar.getTime());
    }

    private static int evaluateDate(Calendar calendar) {
        Calendar rightNow = Calendar.getInstance();
        if (calendar.get(Calendar.MONTH) == rightNow.get(Calendar.MONTH)
                && calendar.get(Calendar.YEAR) == rightNow.get(Calendar.YEAR)
                && calendar.get(Calendar.WEEK_OF_MONTH) == rightNow.get(Calendar.WEEK_OF_MONTH)) {
            if (calendar.get(Calendar.DAY_OF_MONTH) == rightNow.get(Calendar.DAY_OF_MONTH)) {
                if (calculateMinutes(calendar, rightNow) <= 60) {
                    return DATE_MINUTES_AGO;
                } else {
                    return DATE_TODAY;
                }
            } else if (calendar.get(Calendar.DAY_OF_MONTH) == rightNow.get(Calendar.DAY_OF_MONTH) - 1) {
                return DATE_YESTERDAY;
            } else {
                return DATE_THIS_WEEK;
            }
        } else {
            return DATE_FULL;
        }
    }

    private static int calculateMinutes(Calendar c1, Calendar c2) {
        int minutesC1 = c1.get(Calendar.HOUR_OF_DAY) * 60 + c1.get(Calendar.MINUTE);
        int minutesC2 = c2.get(Calendar.HOUR_OF_DAY) * 60 + c2.get(Calendar.MINUTE);
        int result = minutesC2 - minutesC1;
        if (result == 0) {
            return 1;
        } else if (result < 0) {
            return -result;
        } else {
            return result;
        }
    }

    public static Color getTextBackground() {
        Color textB = UIManager.getColor("Table.background"); //NOI18N
        if ("Aqua".equals(UIManager.getLookAndFeel().getID())) //NOI18N
        {
            textB = UIManager.getColor("NbExplorerView.background"); //NOI18N
        }
        return textB != null ? textB : Color.WHITE;
    }

    public static Color getComboBorderColor() {
        Color shadow = UIManager.getColor(
                Utilities.isWindows() ? "Nb.ScrollPane.Border.color" : "TextField.shadow"); //NOI18N
        return shadow != null ? shadow : getPopupBorderColor();
    }

    public static Color getPopupBorderColor() {
        Color shadow = UIManager.getColor("controlShadow"); //NOI18N
        return shadow != null ? shadow : Color.GRAY;
    }

    public static Action[] getNotificationActions(NotificationImpl notification) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new MarkUnreadAction(notification));
        actions.add(new DeleteAction(notification));
        actions.add(null);
        actions.add(new MarkAllReadAction());
        actions.add(new DeleteAllAction());
        return actions.toArray(new Action[actions.size()]);
    }

    public static Action[] getGlobalNotificationActions() {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new MarkAllReadAction());
        actions.add(new DeleteAllAction());
        return actions.toArray(new Action[actions.size()]);
    }

    private static class MarkUnreadAction extends AbstractAction {

        private final NotificationImpl notification;

        public MarkUnreadAction(NotificationImpl notification) {
            super(NbBundle.getMessage(Utils.class, "LBL_MarkUnread"));
            this.notification = notification;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            notification.markAsRead(false);
        }
    }

    private static class DeleteAction extends AbstractAction {

        private final NotificationImpl notification;

        public DeleteAction(NotificationImpl notification) {
            super(NbBundle.getMessage(Utils.class, "LBL_Delete"));
            putValue(ACCELERATOR_KEY, DELETE_KEY);
            this.notification = notification;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            notification.clear();
        }
    }

    private static class DeleteAllAction extends AbstractAction {


        public DeleteAllAction() {
            super(NbBundle.getMessage(Utils.class, "LBL_DeleteAll"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NotificationCenterManager.getInstance().deleteAll();
        }
    }

    private static class MarkAllReadAction extends AbstractAction {

        public MarkAllReadAction() {

            super(NbBundle.getMessage(Utils.class, "LBL_MarkAllRead"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NotificationCenterManager.getInstance().markAllRead();
        }
    }
}
