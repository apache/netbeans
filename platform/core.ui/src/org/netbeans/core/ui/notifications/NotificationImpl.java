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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.CharConversionException;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.xml.XMLUtil;

/**
 * Notification implementation.
 *
 * @author S. Aubrecht
 */
class NotificationImpl extends Notification implements Comparable<NotificationImpl> {

    private final String title;
    private final Icon icon;
    private final Priority priority;
    private JComponent balloonComp;
    private JComponent popupComponent;
    private String detailsText;
    private ActionListener al;

    NotificationImpl( String title, Icon icon, Priority priority ) {
        this.title = title;
        this.icon = icon;
        this.priority = priority;
    }

    @Override
    public void clear() {
        NotificationDisplayerImpl.getInstance().remove(this);
    }

    @Override
    public int compareTo(NotificationImpl n) {
        int res = priority.compareTo(n.priority);
        if( 0 == res )
            res = title.compareTo(n.title);
        return res;
    }

    public JComponent getBalloonComp() {
        return balloonComp;
    }

    public Icon getIcon() {
        return icon;
    }

    public JComponent getPopupComponent() {
        return popupComponent;
    }

    public String getTitle() {
        return title;
    }

    boolean showBallon() {
        return priority != Priority.SILENT;
    }

    void setDetails( String detailsText, ActionListener al ) {
        this.detailsText = detailsText;
        this.al = al;
    }

    void setDetails( JComponent balloonComp, JComponent popupComp ) {
        this.balloonComp = balloonComp;
        this.popupComponent = popupComp;
    }

    void initDecorations() {
        if( null != detailsText ) {
            balloonComp = createDetails( detailsText, al );
            popupComponent = createDetails( detailsText, al );
        }

        JComponent titleComp = createTitle(title);
        JComponent balloon = createContent( icon, titleComp, balloonComp );
        balloon.setBorder(BorderFactory.createEmptyBorder(8, 5, 0, 0));
        balloonComp = balloon;

        titleComp = createTitle(title);
        popupComponent = createContent( icon, titleComp, popupComponent );
    }


    private JComponent createContent(Icon icon, JComponent titleComp, JComponent popupDetails) {
        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque(false);
        panel.add( new JLabel(icon), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3,3,3,3), 0, 0));
        panel.add( titleComp, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,3,3,3), 0, 0));
        panel.add( popupDetails, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,3,3,3), 0, 0));
        panel.add( new JLabel(), new GridBagConstraints(2, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clear();
                PopupList.dismiss();
            }
        };
        addActionListener( popupDetails, actionListener );
        return panel;
    }

    private void addActionListener(Container c, ActionListener al) {
        if( c instanceof AbstractButton ) {
            ((AbstractButton)c).addActionListener(al);
        }
        for( Component child : c.getComponents() ) {
            if( child instanceof Container ) {
                addActionListener((Container)child, al);
            }
        }
    }

    private JComponent createTitle( String title ) {
        return new JLabel("<html>" + title); // NOI18N
    }

    private JComponent createDetails( String text, ActionListener action ) {
        if( null == action ) {
            return new JLabel(text);
        }
        try {
            text = "<html><u>" + XMLUtil.toElementContent(text); //NOI18N
        } catch( CharConversionException ex ) {
            throw new IllegalArgumentException(ex);
        }
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.addActionListener(action);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color c = UIManager.getColor("nb.html.link.foreground"); //NOI18N
        if (c != null) {
            btn.setForeground(c);
        }
        return btn;
    }
}
