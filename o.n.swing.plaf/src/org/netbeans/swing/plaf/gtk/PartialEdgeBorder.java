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

package org.netbeans.swing.plaf.gtk;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import org.netbeans.swing.plaf.util.UIUtils;

/**
 * Drop shadow extension for right edges of tab displayers
 *
 * @author  Tim Boudreau
 */
public class PartialEdgeBorder implements Border {
    private Insets ins;
    /** Creates a new instance of PartialEdgeBorder */
    public PartialEdgeBorder(int width) {
        ins = new Insets (0,0,0,width);
    }

    public Insets getBorderInsets(Component c) {
        return ins;
    }
    
    public boolean isBorderOpaque() {
        return false;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color ctrl = UIManager.getColor ("control"); //NOI18N
        Color base = UIManager.getColor("controlShadow");
        Color curr;
        GradientPaint gp = UIUtils.getGradientPaint (x + width - ins.right, y + (height / 2), ctrl, x + width - ins.right, y + height, base, false);
        ((Graphics2D) g).setPaint (gp);
        g.drawLine (x + width - ins.right, y + (height / 2), x + width - ins.right, y + height);
        for (int i=1; i < ins.right-1; i++) {
            curr = AdaptiveMatteBorder.colorTowards (base, ctrl, ins.right, i + 1);
            int xpos = x + width - ins.right + i;
            int ypos = y + (height / 3) + (i * 2);
            gp = UIUtils.getGradientPaint(xpos, ypos, ctrl, xpos, y + height, curr, false);
            ((Graphics2D) g).setPaint (gp);
            g.drawLine (xpos, ypos, xpos, y + height);
        }
    }
    
}
