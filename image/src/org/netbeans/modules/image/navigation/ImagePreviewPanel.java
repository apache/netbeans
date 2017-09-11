/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.image.navigation;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.openide.util.NbBundle;

/**
 * JPanel used for image preview in Navigator window
 *
 * @author jpeska
 */
public class ImagePreviewPanel extends JPanel {

    BufferedImage image;
    private final int stringGapSize = 10;
    private final Color background = UIManager.getColor("Table.background");
    private final Color foreground = UIManager.getColor("Table.foreground");

    public void setImage(BufferedImage image) {
        this.image = image;
        this.setBackground(background);
        this.revalidate();
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.setColor(foreground);

            int width = image.getWidth();
            int height = image.getHeight();
            String sizes = "Dimensions: " + width + " x " + height;

            g.drawString(sizes, (int) (this.getWidth() * 0.05), this.getHeight() - stringGapSize);
            // adapt image width and height to the size of Navigator window
            double widthRatio = ((double) image.getWidth()) / (((double) this.getWidth()) * 0.9);
            double heightRatio = ((double) image.getHeight()) / (((double) this.getHeight()) * 0.9 - stringGapSize - 20);
            if (widthRatio > 1 || heightRatio > 1) {
                double ratio = widthRatio > heightRatio ? widthRatio : heightRatio;
                width = (int) (((double) image.getWidth()) / ratio);
                height = (int) (((double) image.getHeight()) / ratio);
            }
            g.drawImage(image, (this.getWidth() - width) / 2, (this.getHeight() - height) / 2, width, height, this);
        } else {
            g.setColor(Color.RED);
            FontMetrics fm = this.getFontMetrics(g.getFont()) ;
            String errMessage = NbBundle.getMessage(ImagePreviewPanel.class, "ERR_Thumbnail");
            int stringWidth = fm.stringWidth(errMessage);
            g.drawString(errMessage, (this.getWidth() - stringWidth) / 2, this.getHeight() / 2);
        }
    }
}
