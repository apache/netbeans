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
