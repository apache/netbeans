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
package org.netbeans.modules.options.ui;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;

/**
 *
 * @author Jan Jancura
 */
public class LoweredBorder extends AbstractBorder {

    private Color darker = getLabelBackgroundColor().darker ().darker ();
    private Color brighter = getLabelBackgroundColor().brighter ().brighter ();

    @Override
    public void paintBorder (
        Component c,
        Graphics g,
        int x, int y, int w, int h
    ) {
        Color oldColor = g.getColor ();
        g.translate (x, y);
        g.setColor (darker);
        g.drawLine (0, 0, 0, h - 1);
        g.drawLine (1, 0, w - 1, 0);
        g.setColor (brighter);
        g.drawLine (1, h - 1, w - 1, h - 1);
        g.drawLine (w - 1, 1, w - 1, h - 2);
        g.translate (-x, -y);
        g.setColor (oldColor);
    }

    @Override
    public Insets getBorderInsets (Component c) {
	return new Insets (1, 1, 1, 1);
    }

    @Override
    public boolean isBorderOpaque () {
        return true; 
    }
    
    private static Color getLabelBackgroundColor() {
        Color retval = new JLabel ().getBackground ();
        return (retval != null) ? retval : UIManager.getDefaults().getColor("Label.background");//NOI18N;
    }
}
