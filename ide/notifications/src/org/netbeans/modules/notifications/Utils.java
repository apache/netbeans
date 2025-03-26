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
        return actions.toArray(new Action[0]);
    }

    public static Action[] getGlobalNotificationActions() {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new MarkAllReadAction());
        actions.add(new DeleteAllAction());
        return actions.toArray(new Action[0]);
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
