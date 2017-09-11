/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.welcome.content;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.border.Border;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht
 */
public class ButtonBorder implements Border {

    private final Image imgLeft;
    private final Image imgRight;
    private final Image imgCenter;

    private ButtonBorder( boolean mouseOver ) {
        if( mouseOver ) {
            imgLeft = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/sel_btn_left.png");
            imgRight = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/sel_btn_right.png");
            imgCenter = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/sel_btn_center.png");
        } else {
            imgLeft = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/btn_left.png");
            imgRight = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/btn_right.png");
            imgCenter = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/btn_center.png");
        }
    }

    private static Border regularBorder;
    private static Border mouseoverBorder;

    public static Border createRegular() {
        if( null == regularBorder )
            regularBorder = new ButtonBorder(false);
        return regularBorder;
    }

    public static Border createMouseOver() {
        if( null == mouseoverBorder )
            mouseoverBorder = new ButtonBorder(true);
        return mouseoverBorder;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.drawImage(imgLeft, x, y, imgLeft.getWidth(c), height, c);

        g.drawImage(imgRight, x+width-imgRight.getWidth(c), y, imgRight.getWidth(c), height, c);

        g.drawImage(imgCenter, x+imgLeft.getWidth(c), y,
                x+width-imgLeft.getWidth(c)-imgRight.getWidth(c), height, c);

    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(12, 12, 12, 12);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

}
