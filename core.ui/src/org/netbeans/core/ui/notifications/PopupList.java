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
