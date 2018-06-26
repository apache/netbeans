/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
