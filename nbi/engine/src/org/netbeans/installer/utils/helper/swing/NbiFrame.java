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
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.helper.ExtendedUri;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiFrame extends JFrame {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Initial width of the frame.
     */
    protected int frameWidth;
    
    /**
     * Minimum width of the frame.
     */
    protected int frameMinimumWidth;
    
    /**
     * Maximum width of the frame.
     */
    protected int frameMaximumWidth;
    
    /**
     * Initial height of the frame.
     */
    protected int frameHeight;
    
    /**
     * Minimum height of the frame.
     */
    protected int frameMinimumHeight;
    
    /**
     * Maximum height of the frame.
     */
    protected int frameMaximumHeight;
    
    /**
     * Frame's icon.
     */
    protected File frameIcon;
    
    private NbiFrameContentPane contentPane;
    
    public NbiFrame() {
        super();        
        
        frameWidth  = UiUtils.getDimension(System.getProperties(),
                FRAME_WIDTH_PROPERTY, 
                DEFAULT_FRAME_WIDTH);
        frameHeight = UiUtils.getDimension(System.getProperties(),
                FRAME_HEIGHT_PROPERTY,
                DEFAULT_FRAME_HEIGHT);
        
        frameMinimumWidth = UiUtils.getDimension(System.getProperties(),
                FRAME_MINIMUM_WIDTH_PROPERTY, 
                DEFAULT_FRAME_MINIMUM_WIDTH);
        frameMinimumHeight = UiUtils.getDimension(System.getProperties(),
                FRAME_MINIMUM_HEIGHT_PROPERTY,
                DEFAULT_FRAME_MINIMUM_HEIGHT);
        
        frameMaximumWidth = UiUtils.getDimension(System.getProperties(),
                FRAME_MAXIMUM_WIDTH_PROPERTY, 
                DEFAULT_FRAME_MAXIMUM_WIDTH);
        frameMaximumHeight = UiUtils.getDimension(System.getProperties(),
                FRAME_MAXIMUM_HEIGHT_PROPERTY,
                DEFAULT_FRAME_MAXIMUM_HEIGHT);
        
        boolean customIconLoaded = false;
        if (System.getProperty(FRAME_ICON_URI_PROPERTY) != null) {
            final String frameIconUri =
                    System.getProperty(FRAME_ICON_URI_PROPERTY);
            
            try {
                frameIcon = FileProxy.getInstance().getFile(frameIconUri,true);
                customIconLoaded = true;
            } catch (DownloadException e) {
                ErrorManager.notifyWarning(ResourceUtils.getString(
                        NbiFrame.class,
                        RESOURCE_FAILED_TO_DOWNLOAD_WIZARD_ICON,
                        frameIconUri), e);
            }
        }
        
        if (!customIconLoaded) {
            final String frameIconUri = DEFAULT_FRAME_ICON_URI;
            
            try {
                frameIcon = FileProxy.getInstance().getFile(frameIconUri,true);
                customIconLoaded = true;
            } catch (DownloadException e) {
                ErrorManager.notifyWarning(ResourceUtils.getString(
                        NbiFrame.class,
                        RESOURCE_FAILED_TO_DOWNLOAD_WIZARD_ICON,
                        frameIconUri), e);
            }
        }
        
        initComponents();
    }
    
    
    
    public void setVisible(boolean visible) {
        final GraphicsDevice screen = GraphicsEnvironment.
                getLocalGraphicsEnvironment().
                getScreenDevices()[0];
        final GraphicsConfiguration config = screen.getDefaultConfiguration();
        
        final int screenWidth  = config.getBounds().width;
        final int screenHeight = config.getBounds().height;
        
        setLocation(
                (screenWidth - getSize().width) / 2,
                (screenHeight - getSize().height) / 2);
        
        super.setVisible(visible);
    }
    
    public Image getBackgroundImage() {
        return contentPane.getBackgroundImage();
    }
    
    public void setBackgroundImage(URL url) {
        contentPane.setBackgroundImage(url);
    }
    
    // protected ////////////////////////////////////////////////////////////////////
    private void initComponents() {
        // the frame itself
        try {
            setDefaultCloseOperation(EXIT_ON_CLOSE);
        } catch (SecurityException e) {
            // we might fail here with a custom security manager (e.g. the netbeans
            // one); in this case just log the exception and "let it be" (c)
            ErrorManager.notifyDebug(
                    "Cannot set the default close operation",
                    e);
        }
        
        setSize(frameWidth, frameHeight);
        
        try {
            setIconImage(new ImageIcon(frameIcon.toURI().toURL()).getImage());
        } catch (MalformedURLException e) {
            ErrorManager.notifyWarning(ResourceUtils.getString(
                    NbiFrame.class,
                    RESOURCE_FAILED_TO_SET_FRAME_ICON), e);
        }
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                if ((frameMinimumWidth != -1) &&
                        (getSize().width < frameMinimumWidth)) {
                    setSize(frameMinimumWidth, getSize().height);
                }
                if ((frameMinimumHeight != -1) &&
                        (getSize().height < frameMinimumHeight)) {
                    setSize(getSize().width, frameMinimumHeight);
                }
                
                if ((frameMaximumWidth != -1) &&
                        (getSize().width > frameMaximumWidth)) {
                    setSize(frameMaximumWidth, getSize().height);
                }
                if ((frameMaximumHeight != -1) &&
                        (getSize().height > frameMaximumHeight)) {
                    setSize(getSize().width, frameMaximumHeight);
                }
            }
        });
        
        // content pane
        contentPane = new NbiFrameContentPane();
        setContentPane(contentPane);
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class NbiFrameContentPane extends NbiPanel {
        private Image backgroundImage;
        
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            
            if (backgroundImage != null) {
                graphics.drawImage(backgroundImage, 0, 0, this);
            }
        }
        
        public Image getBackgroundImage() {
            return backgroundImage;
        }
        
        public void setBackgroundImage(URL url) {
            if (url != null) {
                backgroundImage = new ImageIcon(url).getImage();
            } else {
                backgroundImage = null;
            }
        }
        
        public void setBackgroundImage(Image image) {
            if (image != null) {
                backgroundImage = image;
            } else {
                backgroundImage = null;
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String FRAME_WIDTH_PROPERTY =
            "nbi.ui.swing.frame.width";
    
    public static final String FRAME_MINIMUM_WIDTH_PROPERTY =
            "nbi.ui.swing.frame.minimum.width";
    
    public static final String FRAME_MAXIMUM_WIDTH_PROPERTY =
            "nbi.ui.swing.frame.maximum.width";
    
    public static final String FRAME_HEIGHT_PROPERTY =
            "nbi.ui.swing.frame.height";
    
    public static final String FRAME_MINIMUM_HEIGHT_PROPERTY =
            "nbi.ui.swing.frame.minimum.height";
    
    public static final String FRAME_MAXIMUM_HEIGHT_PROPERTY =
            "nbi.ui.swing.frame.maximum.height";
    
    public static final String FRAME_ICON_URI_PROPERTY =
            "nbi.ui.swing.frame.icon.uri";    
    
    public static final int DEFAULT_FRAME_WIDTH  =
            650;
    
    public static final int DEFAULT_FRAME_MINIMUM_WIDTH =
            650;
       
    public static final int DEFAULT_FRAME_MAXIMUM_WIDTH =
            -1;
        
    public static final int DEFAULT_FRAME_HEIGHT =
            600;
    
    public static final int DEFAULT_FRAME_MINIMUM_HEIGHT =
            600;
    
    public static final int DEFAULT_FRAME_MAXIMUM_HEIGHT =
            -1;
    
    public static final String DEFAULT_FRAME_ICON_URI =
            ExtendedUri.RESOURCE_SCHEME +
            ":org/netbeans/installer/utils/helper/swing/frame-icon.png";
            
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_FAILED_TO_DOWNLOAD_WIZARD_ICON =
            "NF.error.failed.to.download.icon"; // NOI18N
    
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_FAILED_TO_SET_FRAME_ICON =
            "NF.error.failed.to.set.frame.icon"; // NOI18N
}
