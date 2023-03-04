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

    private static final Color COLOR_TOP_START = new Color(46, 110, 172);
    private static final Color COLOR_TOP_END = new Color(255, 255, 255);
    private static final Color COLOR_BOTTOM_START = new Color(255, 255, 255);
    private static final Color COLOR_BOTTOM_END = new Color(241, 246, 252);

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
