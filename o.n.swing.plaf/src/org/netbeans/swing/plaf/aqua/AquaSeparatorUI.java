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
    private final static Color lineColorHorizontal = new Color(215, 215, 215);
    private final static Color lineColorVertical = new Color(128, 128, 128);
    
    private static ComponentUI separatorui = new AquaSeparatorUI();
    
    public static ComponentUI createUI(JComponent c) {
        return separatorui;
    }
    
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
    
    public Dimension getPreferredSize(JComponent c) {
        if (((JSeparator) c).getOrientation() == JSeparator.HORIZONTAL) {
            return new Dimension( 0, 12 );
        } else {
            return new Dimension( 1, 11 );
        }
    }
    
    public Dimension getMinimumSize( JComponent c ) { return null; }
    public Dimension getMaximumSize( JComponent c ) { return null; }
}
