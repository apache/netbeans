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
/*
 * AquaSeparatorUI.java
 *
 * Created on March 14, 2004, 4:57 AM
 */

package org.netbeans.swing.plaf.aqua;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.SeparatorUI;

/**
 * Aqua SeparatorUI in JPopupMenu has a height of 12px. The line has a
 * padding-left and padding-right of 1px. And the line is draw at px 6.
 *
 * Only JPopupMenu Separator get draw, all other are 0x0 px.
 *
 * @author  Christopher Atlan
 */
public class AquaSeparatorUI extends SeparatorUI {
    private static final Color lineColorHorizontal = new Color(215, 215, 215);
    private static final Color lineColorVertical = new Color(128, 128, 128);
    
    private static ComponentUI separatorui = new AquaSeparatorUI();
    
    public static ComponentUI createUI(JComponent c) {
        return separatorui;
    }

    @Override
    public void paint( Graphics g, JComponent c ) {
        Dimension s = c.getSize();

        if (((JSeparator) c).getOrientation() == JSeparator.HORIZONTAL) {
            g.setColor(lineColorHorizontal);
            g.drawLine(1, 5, s.width - 2, 5);
        } else {
            g.setColor(lineColorVertical);
            g.drawLine(0, 1, 0, s.height - 2);
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        if (((JSeparator) c).getOrientation() == JSeparator.HORIZONTAL) {
            return new Dimension( 0, 12 );
        } else {
            return new Dimension( 1, 11 );
        }
    }

    @Override
    public Dimension getMinimumSize( JComponent c ) { return null; }
    @Override
    public Dimension getMaximumSize( JComponent c ) { return null; }
}
