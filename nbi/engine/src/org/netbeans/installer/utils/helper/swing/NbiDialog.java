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
