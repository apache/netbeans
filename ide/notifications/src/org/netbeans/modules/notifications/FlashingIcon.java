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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.modules.notifications.center.NotificationCenterManager;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * An icon representing the last Notification on status bar. Clicking the icon shows a list of all Notifications. A balloon-like tooltip is shown for this icon when a new Notification is created. When
 * balloons are disabled (-Dnb.notification.balloon.disable=true) then this icon is flashing for a moment when a new Notification is created.
 *
 * @author S. Aubrecht
 */
class FlashingIcon extends JLabel implements MouseListener, PropertyChangeListener {

    protected int STOP_FLASHING_DELAY = 5 * 1000;
    protected int DISAPPEAR_DELAY_MILLIS = STOP_FLASHING_DELAY + 50 * 1000;
    protected int FLASHING_FREQUENCY = 500;
    private NotificationImpl currentNotification;
    private final NotificationCenterManager manager;

    /**
     * Creates a new instance of FlashingIcon
     *
     * @param icon The icon that will be flashing (blinking)
     */
    protected FlashingIcon() {
        addMouseListener(this);
        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        manager = NotificationCenterManager.getInstance();

    }

    @Override
    public void addNotify() {
        super.addNotify();
        int unreadCount = manager.getUnreadCount();
        currentNotification = manager.getLastUnreadNotification();
        setIcon(getNotificationsIcon(unreadCount, currentNotification != null ? currentNotification.getCategory() == NotificationDisplayer.Category.ERROR : false));
        setToolTipText(getToolTip(unreadCount, currentNotification));
        setVisible(unreadCount > 0);
        manager.addPropertyChangeListener(this);
    }

    @Override
    public void removeNotify() {
        NotificationCenterManager manager = NotificationCenterManager.getInstance();
        if (manager != null) {
            manager.removePropertyChangeListener(this);
        }
        currentNotification = null;
        super.removeNotify();
    }

    @Override
    public void setIcon(Icon icon) {
        if (null != icon) {
            icon = new MyIcon(icon);
        }
        super.setIcon(icon);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        onMouseClick();
    }

    /**
     * Invoked when the user clicks the icon.
     */
    protected void onMouseClick() {
        TopComponent tc = WindowManager.getDefault().findTopComponent("NotificationCenterTopComponent");
        tc.open();
        tc.requestActive();
    }

    @Override
    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    @Override
    public Point getToolTipLocation(MouseEvent event) {

        JToolTip tip = createToolTip();
        tip.setTipText(getToolTipText());
        Dimension d = tip.getPreferredSize();


        Point retValue = new Point(getWidth() - d.width, -d.height);
        return retValue;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (NotificationCenterManager.PROP_NOTIFICATION_ADDED.equals(evt.getPropertyName())) {
            final NotificationImpl ni = (NotificationImpl) evt.getNewValue();
            setNotification(ni, ni.showBallon());
        } else if (NotificationCenterManager.PROP_NOTIFICATION_READ.equals(evt.getPropertyName())) {
            NotificationImpl top = manager.getLastUnreadNotification();
            setNotification(top, false);
            BalloonManager.dismiss();
        } else if (NotificationCenterManager.PROP_NOTIFICATIONS_CHANGED.equals(evt.getPropertyName())) {
            NotificationImpl top = manager.getLastUnreadNotification();
            setNotification(top, false);
        }
    }

    private boolean canShowBalloon() {
        return !Boolean.getBoolean("nb.notification.balloon.disable");
    }

    private void setNotification(final NotificationImpl n, boolean showBalloon) {
        int notificationCount = manager.getUnreadCount();
        setToolTipText(getToolTip(notificationCount, n));
        setIcon(getNotificationsIcon(notificationCount, n != null ? n.getCategory() == NotificationDisplayer.Category.ERROR : false));
        setVisible(notificationCount > 0);
        currentNotification = n;
        if (null != currentNotification) {
            if (showBalloon) {
                if (canShowBalloon()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (null == currentNotification || null == currentNotification.getBalloonComp()) {
                                return;
                            }
                            BalloonManager.show(FlashingIcon.this,
                                    currentNotification.getBalloonComp(),
                                    null,
                                    new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    n.markAsRead(true);
                                }
                            }, 3 * 1000);
                        }
                    });
                }
            }
        } else {
            BalloonManager.dismiss();
        }
    }

    private String getToolTip(int unread, NotificationImpl n) {
        if (unread < 1) {
            return null;
        }
        String tooltip = "<b>" + NbBundle.getMessage(FlashingIcon.class, unread == 1 ? "LBL_UnreadNotification" : "LBL_UnreadNotifications", unread) + "</b>";
        if (n != null) {
            tooltip += "<br>" + NbBundle.getMessage(FlashingIcon.class,"LBL_LastNotification") + " " + n.getTitle();
        }
        tooltip = "<html>" + tooltip + "</html>";

        return tooltip;
    }

    private Icon getNotificationsIcon(int unread, boolean isError) {
        ImageIcon icon;
        if (isError) {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/notifications/resources/notificationsError.png", true);
        } else {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/notifications/resources/notifications.png", true);
        }
        BufferedImage countIcon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = countIcon.createGraphics();
        g.setFont(getFont().deriveFont(10f));
        Color color;
        if ("Nimbus".equals(UIManager.getLookAndFeel().getID())) {
            color = isError ? Color.RED : Color.BLACK;
        } else {
            color = isError ? UIManager.getColor("nb.errorForeground") : UIManager.getColor("Label.foreground");
        }
        g.setColor(color);
        if (unread < 10) {
            g.setFont(g.getFont().deriveFont(Font.BOLD));
            g.drawString(Integer.toString(unread), 5, 10);
        } else if (unread < 100) {
            g.drawString(Integer.toString(unread), 3, 10);
        } else {
            g.drawString("...", 2, 10);
        }
        return new ImageIcon(ImageUtilities.mergeImages(icon.getImage(), countIcon, 0, 0));
    }

    private class MyIcon implements Icon {

        private Icon orig;

        public MyIcon(Icon orig) {
            this.orig = orig;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            orig.paintIcon(c, g, x, y);
        }

        @Override
        public int getIconWidth() {
            return orig.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return orig.getIconHeight();
        }
    }
}
