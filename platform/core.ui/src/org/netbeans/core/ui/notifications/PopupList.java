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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

/**
 * Popup window with a list of all Notifications.
 *
 * @author S. Aubrecht
 */
public class PopupList {

    private static JPopupMenu current;
    private static final int MAX_VISIBLE_ROWS = 10;

    static void show( Component invoker ) {
        dismiss();
        BalloonManager.dismiss();
        synchronized( PopupList.class ) {
            current = createPopup();
            if( null != current ) {
                Dimension size = current.getPreferredSize();
                current.show(invoker, -size.width+invoker.getWidth(), -size.height);
            }
        }
    }

    static void dismiss() {
        synchronized( PopupList.class ) {
            if( null != current ) {
                current.setVisible(false);
            }
            current = null;
        }
    }

    static JPopupMenu createPopup() {
        NotificationDisplayerImpl displayer = NotificationDisplayerImpl.getInstance();
        List<NotificationImpl> notifications = displayer.getNotifications();
        if( notifications.isEmpty() )
            return null;

        JPopupMenu res = new JPopupMenu();
        Dimension prefSize = new Dimension();
        int avgRowHeight = 0;
        int rowCount = 0;

        final JPanel panel = new JPanel( new GridBagLayout());
        final JScrollPane scroll = new JScrollPane(panel);
        panel.setOpaque(true);
        Color panelBackground = UIManager.getColor("nb.core.ui.popupList.background"); //NOI18N
        if( null == panelBackground )
            panelBackground = UIManager.getColor("Tree.background"); //NOI18N
        panel.setBackground( panelBackground );
        for( NotificationImpl n : notifications ) {
            final JPanel row = new JPanel(new GridBagLayout());
            row.setOpaque(false);

            JComponent component = n.getPopupComponent();
            if( null == component ) {
                continue; //just in case...
            }
            row.add(component, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2,2,2,2), 1, 1));
            JButton btnDismiss = new BalloonManager.DismissButton();
            final NotificationImpl notification = n;
            btnDismiss.addActionListener( new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    notification.clear();
                    panel.remove(row);
                    panel.getParent().invalidate();
                    panel.getParent().repaint();
                    panel.getParent().getParent().invalidate();
                    panel.getParent().getParent().repaint();
                    if( panel.getComponentCount() == 1 )
                        PopupList.dismiss();
                }
            });
            row.add(btnDismiss, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5,3,3,3), 1, 1));
            
            panel.add(row, new GridBagConstraints(0, rowCount, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 1, 1));

            Dimension size = row.getPreferredSize();
            if( size.width > prefSize.width )
                prefSize.width = size.width;
            if( rowCount++ < MAX_VISIBLE_ROWS )
                prefSize.height += size.height;
            avgRowHeight += size.height;
        }

        panel.add(new JLabel(), new GridBagConstraints(0, rowCount, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 1, 1));

        avgRowHeight /= notifications.size();
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(avgRowHeight);
        scroll.getVerticalScrollBar().setBlockIncrement(MAX_VISIBLE_ROWS*avgRowHeight);
        int scrollBarWidth = scroll.getVerticalScrollBar().getPreferredSize().width;
        if( scrollBarWidth <= 0 )
            scrollBarWidth = UIManager.getInt("ScrollBar.width"); //NOI18N
        if( scrollBarWidth <= 0 )
            scrollBarWidth = 18; //let's take some reasonable guess
        prefSize.width += scrollBarWidth;
        Insets i = scroll.getInsets();
        if( null != i ) {
            prefSize.width += i.left + i.right;
            prefSize.height += i.top + i.bottom;
        }
        if( rowCount <= MAX_VISIBLE_ROWS )
            prefSize = panel.getPreferredSize();
        prefSize.height = Math.min( prefSize.height, 600 );
        if( prefSize.width > 800 ) {
            prefSize.width = 800;
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
        scroll.getViewport().setPreferredSize(prefSize);
        scroll.getViewport().setMinimumSize(prefSize);
        res.add( scroll );
        return res;
    }
}
