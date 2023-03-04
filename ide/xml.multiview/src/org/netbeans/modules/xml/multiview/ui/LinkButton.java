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

package org.netbeans.modules.xml.multiview.ui;

import java.awt.Color;
import org.netbeans.modules.xml.multiview.cookies.LinkCookie;

import javax.swing.*;

/** 
 * A button that represents a link.
 *
 *@see {@org.netbeans.modules.xml.multiview.cookies.LinkCookie}.
 *
 * Created on November 19, 2004, 8:06 AM
 * @author mkuchtiak
 */
public class LinkButton extends JButton {

    /** Creates a new instance of LinkButton */
    public LinkButton(LinkCookie panel, Object ddBean, String ddProperty) {
        super();
        initLinkButton(this, panel, ddBean, ddProperty);
    }

    public static void initLinkButton(final AbstractButton button, LinkCookie panel, Object ddBean, String ddProperty) {
        button.setForeground(SectionVisualTheme.hyperlinkColor);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMargin(new java.awt.Insets(2, 2, 2, 2));
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        String text = getFormatedLinkText(button.getText());
        button.setAction(new LinkAction(panel, ddBean, ddProperty));
        button.setText(text);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                button.setForeground(SectionVisualTheme.hyperlinkColorFocused);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setCursor(java.awt.Cursor.getDefaultCursor());
                button.setForeground(SectionVisualTheme.hyperlinkColor);
            }
        });
    }

    public void setText(String text) {
        super.setText(getFormatedLinkText(text));
    }

    private static String getFormatedLinkText(String text) {
        return "<html><b><u>" + getColorizedText(text) + "</u></b></html>"; //NOI18N
    }

    private static String getColorizedText(String text) {
        Color linkColor = UIManager.getColor("nb.html.link.foreground"); //NOI18N
        if (linkColor == null) {
            return text;
        } else {
            return "<font color=\"#" + Integer.toHexString(linkColor.getRGB()).substring(2) + "\">" + text + "</font>";
        }
    }

    public static class LinkAction extends AbstractAction {
        LinkCookie panel;
        Object ddBean;
        String ddProperty;

        public LinkAction(LinkCookie panel, Object ddBean, String ddProperty) {
            this.panel=panel;
            this.ddBean=ddBean;
            this.ddProperty=ddProperty;
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            panel.linkButtonPressed(ddBean, ddProperty);
        }
    }
}
