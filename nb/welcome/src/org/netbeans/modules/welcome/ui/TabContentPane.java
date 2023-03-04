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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.netbeans.modules.welcome.content.Utils;

/**
 *
 * @author S. Aubrecht
 */
class TabContentPane extends JPanel {

    public TabContentPane() {
        super( new GridBagLayout() );
        setOpaque(false);
        setMinimumSize(new Dimension(41, 85));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        g2d.setColor(UIManager.getColor( "Tree.background") );
        g2d.fillRect(0, 0, width, height);
        g2d.setColor( Utils.getBorderColor() );
        g2d.drawLine( 0,0, 0, height);
        g2d.drawLine( width-1,0, width-1, height);
    }
}
