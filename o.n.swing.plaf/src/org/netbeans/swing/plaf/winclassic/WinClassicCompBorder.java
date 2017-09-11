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
 * WinClassicCompBorder.java
 *
 * Created on March 14, 2004, 8:34 PM
 */

package org.netbeans.swing.plaf.winclassic;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/** Lower border for the tab control
 *
 * @author  Dafe Simonek
 */
public class WinClassicCompBorder implements Border {

    private static final Insets insets = new Insets(0, 2, 2, 2);

    public Insets getBorderInsets(Component c) {
        return insets;
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int topOffset = 0;
        // TODO - other option is to let the painting of the dark border to the inner component and make this border's insets smaller.
        if (c instanceof JComponent) {
            JComponent jc = (JComponent)c;
            Integer in = (Integer)jc.getClientProperty("MultiViewBorderHack.topOffset");
            topOffset = in == null ? topOffset : in.intValue();
        }
        g.translate(x, y);
        g.setColor(UIManager.getColor("InternalFrame.borderShadow")); //NOI18N
        g.drawLine(0, 0, 0, height - 1); 
        if (topOffset != 0) {
            g.drawLine(1, topOffset - 1, 1, topOffset);
        }
        g.setColor(UIManager.getColor("InternalFrame.borderDarkShadow")); //NOI18N
        g.drawLine(1, topOffset, 1, height - 2);
        g.setColor(UIManager.getColor("InternalFrame.borderHighlight")); //NOI18N
        g.drawLine(1, height - 1, width - 1, height - 1);
        g.drawLine(width - 1, height - 2, width - 1, 0);
        g.setColor(UIManager.getColor("InternalFrame.borderLight")); //NOI18N
        g.drawLine(2, height - 2, width - 2, height - 2);
        g.drawLine(width - 2, height - 3, width - 2, 0);
        g.translate(-x, -y);
    }
    
}
