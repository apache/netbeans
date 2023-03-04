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
