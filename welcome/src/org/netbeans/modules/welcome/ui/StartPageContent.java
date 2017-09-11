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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.welcome.ui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.netbeans.modules.welcome.content.Constants;

/**
 *
 * @author S. Aubrecht
 */
public class StartPageContent extends JPanel implements Constants {

    private final static Color COLOR_TOP_START = new Color(46, 110, 172);
    private final static Color COLOR_TOP_END = new Color(255, 255, 255);
    private final static Color COLOR_BOTTOM_START = new Color(255, 255, 255);
    private final static Color COLOR_BOTTOM_END = new Color(241, 246, 252);

    public StartPageContent() {
        super( new GridBagLayout() );

        JComponent tabs = new TabbedPane( new LearnAndDiscoverTab(),
                new MyNetBeansTab(),
                new WhatsNewTab());
        tabs.setBorder(BorderFactory.createEmptyBorder(10,15,15,15));
        tabs.setOpaque(false);

        add( tabs, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(27,0,0,0), 0, 0) );

        add( new JLabel(), new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0) );
    }

    @Override
    protected void paintComponent(Graphics g) {
        if( UIManager.getBoolean( "nb.startpage.defaultbackground" ) ) { //NOI18N
            super.paintComponent( g );
        } else {
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();
            int gradientStop = height / 2;
            int bottomStart = gradientStop + gradientStop/2;

            g2d.setPaint(new GradientPaint(0, 0, COLOR_TOP_START, 0, gradientStop, COLOR_TOP_END));
            g2d.fillRect(0, 0, width, gradientStop);
            g2d.setPaint( COLOR_TOP_END );
            g2d.fillRect( 0, gradientStop, width, bottomStart );

            g2d.setPaint(new GradientPaint(0, bottomStart, COLOR_BOTTOM_START, 0, height, COLOR_BOTTOM_END));
            g2d.fillRect(0, bottomStart, width, height);
        }
    }
}
