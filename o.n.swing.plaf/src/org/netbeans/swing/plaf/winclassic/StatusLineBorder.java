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

package org.netbeans.swing.plaf.winclassic;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 *
 * @author  Dafe Simonek
 */
class StatusLineBorder extends AbstractBorder {

    /** Constants for sides of status line border */
    public static final int LEFT = 1;
    public static final int TOP = 2;
    public static final int RIGHT = 4;

    private Insets insets;

    private int type;

    /** Constructs new status line border of specified type. Type is bit
     * mask specifying which sides of border should be visible */
    public StatusLineBorder(int type) {
        this.type = type;
    }

    public void paintBorder(Component c, Graphics g, int x, int y,
    int w, int h) {
        g.translate(x, y);
        Color shadowC = UIManager.getColor("controlShadow"); //NOI18N
        Color highlightC = UIManager.getColor("controlLtHighlight"); //NOI18N
        Color middleC = UIManager.getColor("control"); //NOI18N
        // top
        if ((type & TOP) != 0) {
            g.setColor(shadowC);
            g.drawLine(0, 0, w - 1, 0);
            g.drawLine(0, 3, w - 1, 3);
            g.setColor(highlightC);
            g.drawLine(0, 1, w - 1, 1);
            g.setColor(middleC);
            g.drawLine(0, 2, w - 1, 2);
        }
        // left side
        if ((type & LEFT) != 0) {
            g.setColor(middleC);
            g.drawLine(0, 2, 0, h - 1);
            g.setColor(shadowC);
            g.drawLine(1, 3, 1, h - 1);
        }
        // right side
        if ((type & RIGHT) != 0) {
            g.setColor(shadowC);
            g.drawLine(w - 2, 3, w - 2, h - 1);
            g.setColor(highlightC);
            g.drawLine(w - 1, 4, w - 1, h - 1);
            g.setColor(middleC);
            g.drawLine(w - 1, 3, w - 1, 3);
        }

        g.translate(-x, -y);
    }

    public Insets getBorderInsets(Component c) {
        if (insets == null) {
            insets = new Insets((type & TOP) != 0 ? 4 : 0,
            (type & LEFT) != 0 ? 2 : 0, 0,
            (type & RIGHT) != 0 ? 2 : 0);
        }
        return insets;
    }

}
