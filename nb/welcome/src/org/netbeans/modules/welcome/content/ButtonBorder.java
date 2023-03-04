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
