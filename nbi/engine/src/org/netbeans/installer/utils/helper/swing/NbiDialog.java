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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.net.URL;
import javax.swing.JDialog;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiDialog extends JDialog {
    protected NbiFrame owner;
    
    protected int dialogWidth;
    protected int dialogHeight;
    protected URL dialogIcon;
    
    protected NbiDialogContentPane contentPane;
    
    public NbiDialog() {
        super();
        
        initComponents();
    }
    
    public NbiDialog(NbiFrame owner) {
        super(owner);
        
        this.owner = owner;
        
        initComponents();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        contentPane = new NbiDialogContentPane();
        setContentPane(contentPane);
        
        setSize(DEFAULT_DIALOG_WIDTH, DEFAULT_DIALOG_HEIGHT);
    }
    
    public void setVisible(boolean visible) {
        if (owner == null) {
            final GraphicsDevice screen = GraphicsEnvironment.
                    getLocalGraphicsEnvironment().
                    getScreenDevices()[0];
            final GraphicsConfiguration config = screen.getDefaultConfiguration();
            
            final int screenWidth  = config.getBounds().width;
            final int screenHeight = config.getBounds().height;
            
            setLocation(
                    (screenWidth - getSize().width) / 2,
                    (screenHeight - getSize().height) / 2);
        } else {
            setLocation(
                    owner.getLocation().x + DIALOG_FRAME_WIDTH_DELTA,
                    owner.getLocation().y + DIALOG_FRAME_WIDTH_DELTA);
        }
        
        super.setVisible(visible);
    }
    
    public class NbiDialogContentPane extends NbiPanel {
        private Image backgroundImage;
        
        public NbiDialogContentPane() {
            super();
            
            if (NbiDialog.this.owner != null) {
                backgroundImage = NbiDialog.this.owner.getBackgroundImage();
            }
        }
        
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            
            Graphics2D graphics2d = (Graphics2D) graphics;
            
            if (backgroundImage != null) {
                graphics2d.drawImage(backgroundImage, 0, 0, this);
                
                Composite oldComposite = graphics2d.getComposite();
                
                graphics2d.setComposite(
                        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                graphics2d.setColor(Color.WHITE);
                graphics2d.fillRect(0, 0, this.getWidth(), this.getHeight());
                
                graphics2d.setComposite(oldComposite);
            }
        }
    }
    
    public static final int DIALOG_FRAME_WIDTH_DELTA = 100;
    
    public static final int DIALOG_FRAME_HEIGHT_DELTA = 100;
    
    public static final int DEFAULT_DIALOG_WIDTH =
            NbiFrame.DEFAULT_FRAME_WIDTH - DIALOG_FRAME_WIDTH_DELTA;
    
    public static final int DEFAULT_DIALOG_HEIGHT =
            NbiFrame.DEFAULT_FRAME_HEIGHT - DIALOG_FRAME_HEIGHT_DELTA;
    
    public static final URL DEFAULT_DIALOG_ICON = NbiDialog.class.
            getClassLoader().
            getResource("org/netbeans/installer/wizard/wizard-icon.png");
}
