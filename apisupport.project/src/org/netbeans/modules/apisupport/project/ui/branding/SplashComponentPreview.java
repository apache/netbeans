/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.apisupport.project.ui.branding;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.net.URL;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NullAllowed;


/**
 * @author Radek Matous
 */
class SplashComponentPreview extends JLabel {
    private FontMetrics fm;
    private Rectangle view;
    private Color color_text;
    private Color color_bar;
    private Color color_edge;
    private Color color_corner;
    
    private boolean draw_bar;
    
    @NullAllowed Image image;
    private Rectangle dirty = new Rectangle();
    private String text;
    private Rectangle rect = new Rectangle();
    private Rectangle bar = new Rectangle();
    private Rectangle bar_inc = new Rectangle();
    
    private int progress = 0;
    private int maxSteps = 0;
    private int tmpSteps = 0;
    private int barStart = 0;
    private int barLength = 0;
    
    private DragManager dragManager;
    private DragManager.DragItem textDragItem;
    private DragManager.DragItem progressDragItem;
        
    /**
     * Creates a new splash screen component.
     */
    public SplashComponentPreview() {                
        //setBorder(new TitledBorder(NbBundle.getMessage(getClass(),"LBL_SplashPreview")));
        dragManager = new DragManager(this);
        textDragItem = dragManager.createNewItem();
        progressDragItem = dragManager.createNewItem();
    }
    
    void setFontSize(final String fontSize) throws NumberFormatException {
        int size;
        String sizeStr = fontSize;
        size = Integer.parseInt(sizeStr);
        
        Font font = new Font("Dialog", Font.PLAIN, size);//NOI18N
        
        setFont(font); // NOI18N
        fm = getFontMetrics(font);
    }
    
    void setSplashImageIcon(final @NullAllowed URL url) {
        this.image = url != null ? new ImageIcon(url).getImage() : null;
        //this.image = image.getScaledInstance(398, 299, Image.SCALE_DEFAULT);
    }
    
    void setDropHandletForProgress (DragManager.DropHandler dHandler) {
        this.progressDragItem.setDropHandler(dHandler);
    }

    void setDropHandletForText (DragManager.DropHandler dHandler) {
        this.textDragItem.setDropHandler(dHandler);
    }
    
    void setFontSize(final int size) throws NumberFormatException {
        Font font = new Font("Dialog", Font.PLAIN, size); // NOI18N
        
        setFont(font); // NOI18N
        fm = getFontMetrics(font);
    }
    
    
    void setRunningTextBounds(final Rectangle bounds) throws NumberFormatException {        
        view = bounds;
    }

    
    void setProgressBarEnabled(final boolean enabled) {
        draw_bar = enabled; // NOI18N
        progressDragItem.setEnabled(enabled);
    }
    
    void setProgressBarBounds(final Rectangle bounds) throws NumberFormatException {
        bar = bounds;
        progressDragItem.setRectangle(bar);
    }
    
    void setColorCorner(final Color color) throws NumberFormatException {
        color_corner = color;
    }
    
    void setColorEdge(final Color color) throws NumberFormatException {
        color_edge = color;
    }

    
    void setTextColor(final Color color) throws NumberFormatException {
        color_text = color;
    }
    
    void setColorBar(final Color color) throws NumberFormatException {
        color_bar = color;
    }
    
    /**
     * Defines the single line of text this component will display.
     */
    public void setText(final String text) {
        // run in AWT, there were problems with accessing font metrics
        // from now AWT thread
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (text == null) {
                    repaint(dirty);
                    return;
                }
                
                if (fm == null)
                    return;
                
                adjustText(text);
                
                SwingUtilities.layoutCompoundLabel(fm, text, null,
                        SwingConstants.BOTTOM, SwingConstants.LEFT, SwingConstants.BOTTOM, SwingConstants.LEFT,
                        SplashComponentPreview.this.view, new Rectangle(), rect, 0);
                //textDragItem.setRectangle(rect);
                textDragItem.setRectangle(SplashComponentPreview.this.view);
                dirty = dirty.union(rect);
                // update screen (assume repaint manager optimizes unions;)
//                repaint(dirty);
                repaint();
                dirty = new Rectangle(rect);
            }
        });
    }
    
    // Defines a max value for splash progress bar.
    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }
    
    // Adds temporary steps to create a max value for splash progress bar later.
    public void addToMaxSteps(int steps) {
        tmpSteps += steps;
    }
    
    // Adds temporary steps and creates a max value for splash progress bar.
    public void addAndSetMaxSteps(int steps) {
        tmpSteps += steps;
        maxSteps = tmpSteps;
    }
    
    // Increments a current value of splash progress bar by given steps.
    public void increment(int steps) {
        if (draw_bar) {
            progress += steps;
            if (progress > maxSteps)
                progress = maxSteps;
            else if (maxSteps > 0) {
                int bl = bar.width * progress / maxSteps - barStart;
                if (bl > 1 || barStart % 2 == 0) {
                    barLength = bl;
                    bar_inc = new Rectangle(bar.x + barStart, bar.y, barLength + 1, bar.height);
//                    System.out.println("progress: " + progress + "/" + maxSteps);
                    repaint(bar_inc);
                    //System.err.println("(painting " + bar_inc + ")");
                } else {
                    // too small, don't waste time painting it
                }
            }
        }
    }
    
    public void resetSteps() {
        progress = 0;
        barStart = 0;
        barLength = 0;
        increment(maxSteps);
    }
    
    
    //Creates new text with the ellipsis at the end when text width is
    // bigger than allowed space
    private void adjustText(String text){
        String newText = null;
        String newString;
        
        if (text == null)
            return ;
        
        if (fm == null)
            return;
        
        int width = fm.stringWidth(text);
        
        if (width > view.width) {
            StringTokenizer st = new StringTokenizer(text);
            while (st.hasMoreTokens()) {
                String element = st.nextToken();
                if (newText == null)
                    newString = element;
                else
                    newString = newText + " " + element; // NOI18N
                if (fm.stringWidth(newString + "...") > view.width) { // NOI18N
                    this.text = newText + "..."; // NOI18N
                    break;
                } else
                    newText = newString;
                
            }
        } else
            this.text = text;
    }
    /**
     * Override update to *not* erase the background before painting.
     */
    public void update(Graphics g) {
        paint(g);
    }
    
    /**
     * Renders this component to the given graphics.
     */
    public void paint(Graphics g) {
        super.paint(g);
        /*int width = image.getWidth(null);//BasicBrandingModel.SPLASH_WIDTH;
        int height = image.getHeight(null);//BasicBrandingModel.SPLASH_HEIGHT;
        int x = (getWidth()/2)-(width/2);
        int y = (getHeight()/2)-(height/2);
        
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform tx = g2d.getTransform();
        
        
        tx.translate(x, y);
        dragManager.setTranslate(x,y);
        g2d.setTransform(tx);
        */
        dragManager.setTranslate(0,0);
        originalPaint(g);
        dragManager.paint(g);
    }
    
    public void originalPaint(Graphics graphics) {
        Graphics2D g2d = (Graphics2D)graphics;
        if (!isEnabled()) {
            g2d.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.3f));
        }
        
        graphics.setColor(color_text);
        graphics.drawImage(image, 0, 0, null);
        
        if (text == null) {
            // no text to draw
            return;
        }
        
        if (fm == null) {
            // XXX(-ttran) this happened on Japanese Windows NT, don't
            // fully understand why
            return;
        }
        
        SwingUtilities.layoutCompoundLabel(fm, text, null,
                SwingConstants.BOTTOM, SwingConstants.LEFT, SwingConstants.BOTTOM, SwingConstants.LEFT,
                this.view, new Rectangle(), rect, 0);
        // turn anti-aliasing on for the splash text
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.drawString(text, rect.x, rect.y + fm.getAscent());
        // Draw progress bar if applicable
        
        if (draw_bar && Boolean.getBoolean("netbeans.splash.nobar") == false && maxSteps > 0/* && barLength > 0*/) {
            graphics.setColor(color_bar);
            graphics.fillRect(bar.x, bar.y, barStart + barLength, bar.height);
            graphics.setColor(color_corner);
            graphics.drawLine(bar.x, bar.y, bar.x, bar.y + bar.height);
            graphics.drawLine(bar.x + barStart + barLength, bar.y, bar.x + barStart + barLength, bar.y + bar.height);
            graphics.setColor(color_edge);
            graphics.drawLine(bar.x, bar.y + bar.height / 2, bar.x, bar.y + bar.height / 2);
            graphics.drawLine(bar.x + barStart + barLength, bar.y + bar.height / 2, bar.x + barStart + barLength, bar.y + bar.height / 2);
            barStart += barLength;
            barLength = 0;
        }
    }
    
    public @Override Dimension getPreferredSize() {
        return image != null ? new Dimension(image.getWidth(null), image.getHeight(null)) : super.getPreferredSize();
    }
    
    /*public boolean isOpaque() {
        return true;
    }*/
    
    public Rectangle getView() {
        return view;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textDragItem.setEnabled(enabled);
        progressDragItem.setEnabled(enabled & draw_bar);
    }
}
