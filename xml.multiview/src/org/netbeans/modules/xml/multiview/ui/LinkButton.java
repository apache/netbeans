/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
