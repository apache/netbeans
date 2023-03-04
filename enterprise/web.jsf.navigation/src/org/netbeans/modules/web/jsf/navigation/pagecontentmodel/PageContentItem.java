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

package org.netbeans.modules.web.jsf.navigation.pagecontentmodel;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.nodes.Node.Cookie;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author joelle lam
 */
public class PageContentItem {
    
    private Image icon;
    private String fromAction;
    private String fromOutcome;
    private String name;
    private List<Action> actions;
    
    /**
     *
     * @return
     */
    public Action[] getActions() {
        return new Action[]{};
    }
    
    /**
     *
     * @param name
     * @param fromAction
     * @param icon
     */
    public PageContentItem( String name, String fromAction, String fromOutcome, Image icon ) {
        this.name = name;
        this.fromAction = fromAction;
        this.fromOutcome = fromOutcome;
        this.icon = icon;
    }
    
    /**
     *
     * @param name
     * @param fromString
     * @param icon
     * @param isOutcome
     */
    public PageContentItem( String name, String fromOutcome, Image icon ) {
        this.name = name;
        this.fromOutcome = fromOutcome;
        this.icon = icon;
    }
    
    
    /**
     *
     * @return
     */
    public Image getIcon() {
        return icon;
    }
    
    //    /**
    //     *
    //     * @param icon
    //     */
    //    public void setIcon(Image icon) {
    //        this.icon = icon;
    //    }
    //
    /**
     *
     * @return
     */
    public String getFromAction() {
        return fromAction;
    }
    
    /**
     *
     * @param fromAction
     */
    public void setFromAction(String fromAction) {
        this.fromAction = fromAction;
    }
    
    /**
     *
     * @return
     */
    public String getFromOutcome() {
        return fromOutcome;
    }
    
    /**
     *
     * @param fromOutcome
     */
    public void setFromOutcome(String fromOutcome) {
        this.fromOutcome = fromOutcome;
    }
    
    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }
    
    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    
    public String toString() {
        return "PageBean[" + getName() + ", fromOutcome=" + getFromOutcome() + ", fromAction=" + getFromAction() + "," + getIcon() + "]";
    }
    
    
    private Image bufferedIcon = null;
    public Image getBufferedIcon(){
        if (bufferedIcon == null){
            bufferedIcon = toBufferedImage(getIcon());
            //bufferedIcon =  new javax.swing.ImageIcon(icon).getImage();
        }
        return bufferedIcon;
    }
     private static final Image UNKONWN_ICON = ImageUtilities.loadImage("org/netbeans/modules/web/jsf/navigation/graph/resources/question.png"); // NOI18N
     private static final Logger LOG = Logger.getLogger(PageContentItem.class.toString());
     // private final Image backupImage = new ImageIcon
    
    /** The method creates a BufferedImage which represents the same Image as the
     * original but buffered to avoid repeated loading of the icon while repainting.
     */
    private Image toBufferedImage(Image img) {
        // load the image
        if( img == null ){
            LOG.fine("Page Content Item does not have Image: " + toString());
            img = UNKONWN_ICON;
        }
        new javax.swing.ImageIcon(img);
        BufferedImage rep = createBufferedImage(img.getWidth(null), img.getHeight(null));
        Graphics g = rep.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        img.flush();
        return rep;
    }
    
    /** Creates BufferedImage with Transparency.TRANSLUCENT */
    private BufferedImage createBufferedImage(int width, int height) {
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        }
        ColorModel model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration().getColorModel(Transparency.TRANSLUCENT);
        BufferedImage buffImage = new BufferedImage(model,
                model.createCompatibleWritableRaster(width, height), model.isAlphaPremultiplied(), null);
        return buffImage;
    }
    
     public  <T extends Cookie> T getCookie(Class<T> type){
        return null;
     }
    
    
}
