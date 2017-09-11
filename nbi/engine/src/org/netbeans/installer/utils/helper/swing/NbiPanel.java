/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.helper.swing;

import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.helper.Pair;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiPanel extends JPanel {
    private List <Pair <Integer, ImageIcon>> images ;
    public static final int ANCHOR_TOP_LEFT  = 1;
    public static final int ANCHOR_TOP_RIGHT = 2;
    public static final int ANCHOR_BOTTOM_LEFT = 3;
    public static final int ANCHOR_BOTTOM_RIGHT = 4;
    public static final int ANCHOR_LEFT  = 5;
    public static final int ANCHOR_RIGHT = 6;
    public static final int ANCHOR_BOTTOM = 7;
    public static final int ANCHOR_TOP = 8;
    public static final int ANCHOR_FULL = 9;
    
    public NbiPanel() {
        super();
        
        setLayout(new GridBagLayout());
        images = new ArrayList <Pair <Integer, ImageIcon>> ();
    }
    public void setBackgroundImage(String backgroundImageURI, int anchor) {
        if (backgroundImageURI != null) {
            try {
                File file = FileProxy.getInstance().getFile(backgroundImageURI,true);
                ImageIcon backgroundImage = new ImageIcon(file.getAbsolutePath());
                setBackgroundImage(backgroundImage,anchor);
            } catch (DownloadException e) {
                LogManager.log(e);
            }
        }
    }
    public void setBackgroundImage(ImageIcon backgroundImage, int anchor) {
        if (backgroundImage != null) {
            images.add(new Pair <Integer, ImageIcon> (anchor,backgroundImage));
        }
    }
    public ImageIcon getBackgroundImage(int anchor) {
        for(Pair <Integer, ImageIcon> pair : images) {
            if(pair.getFirst().intValue() == anchor) {
                return pair.getSecond();
            }
        }
        return null;
    }
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        
        for(Pair <Integer, ImageIcon> pair : images){
            final int anchor = pair.getFirst().intValue();
            Image backgroundImage = pair.getSecond().getImage();
            if (backgroundImage != null) {
                switch(anchor) {
                    case ANCHOR_TOP_LEFT :
                        graphics.drawImage(backgroundImage,
                                0,
                                0,
                                this);
                        break;
                    case ANCHOR_TOP_RIGHT:
                        graphics.drawImage(backgroundImage,
                                this.getWidth() - backgroundImage.getWidth(this),
                                0,
                                this);
                        break;
                    case ANCHOR_BOTTOM_LEFT:
                        graphics.drawImage(backgroundImage,
                                0,
                                this.getHeight() - backgroundImage.getHeight(this),
                                this);
                        break;
                    case ANCHOR_BOTTOM_RIGHT:
                        graphics.drawImage(backgroundImage,
                                this.getWidth() - backgroundImage.getWidth(this),
                                this.getHeight() - backgroundImage.getHeight(this),
                                this);
                        break;
                        
                    case ANCHOR_LEFT :
                        graphics.drawImage(backgroundImage,
                                0,
                                (this.getHeight() - backgroundImage.getHeight(this)) / 2 ,
                                this);
                        break;
                    case ANCHOR_RIGHT:
                        graphics.drawImage(backgroundImage,
                                this.getWidth() - backgroundImage.getWidth(this),
                                (this.getHeight() - backgroundImage.getHeight(this)) / 2,
                                this);
                        break;
                    case ANCHOR_BOTTOM:
                        graphics.drawImage(backgroundImage,
                                (this.getWidth() - backgroundImage.getWidth(this))/2,
                                this.getHeight() - backgroundImage.getHeight(this),
                                this);
                        break;
                    case ANCHOR_TOP:
                        graphics.drawImage(backgroundImage,
                                this.getWidth() - backgroundImage.getWidth(this),
                                0,
                                this);
                        break;
                    case ANCHOR_FULL:
                        graphics.drawImage(
                                backgroundImage,
                                0, 0,
                                this.getWidth(), this.getHeight(),
                                0, 0,
                                backgroundImage.getWidth(this), backgroundImage.getHeight(this),
                                this);
                        break;
                }
            }
        }
    }
}
