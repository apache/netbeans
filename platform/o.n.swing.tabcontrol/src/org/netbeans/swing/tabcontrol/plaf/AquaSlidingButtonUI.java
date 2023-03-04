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


package org.netbeans.swing.tabcontrol.plaf;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import org.netbeans.swing.tabcontrol.SlidingButtonUI;

/**
 *
 * @author  mkleint
 */
public class AquaSlidingButtonUI extends SlidingButtonUI {
    
    private static AquaSlidingButtonUI AQUA_INSTANCE = null;
    
    /** Creates a new instance of AquaSlidingButtonUI */
    private AquaSlidingButtonUI() {
    }
    
    /** Aqua ui for sliding buttons.  This class is public so it can be
     * instantiated by UIManager, but is of no interest as API. */
    public static ComponentUI createUI(JComponent c) {
        if (AQUA_INSTANCE == null) {
            AQUA_INSTANCE = new AquaSlidingButtonUI();
        }
        return AQUA_INSTANCE;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        //use the same font as view/editor tabs
        Font txtFont = (Font) UIManager.get("windowTitleFont"); //NOI18N
        if (txtFont == null) {
            txtFont = new Font("Dialog", Font.PLAIN, 11); //NOI18N
        } else if (txtFont.isBold()) {
            // don't use deriveFont() - see #49973 for details
            txtFont = new Font(txtFont.getName(), Font.PLAIN, txtFont.getSize());
        }
        c.setFont(txtFont);
    }
        
    @Override
    protected void paintIcon(Graphics g, AbstractButton b, Rectangle iconRect) {
        Graphics2D g2d = (Graphics2D) g;

        Composite comp = g2d.getComposite();
        if( b.getModel().isRollover() || b.getModel().isSelected() ) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }
        super.paintIcon(g, b, iconRect);
        g2d.setComposite(comp);
    }
}
