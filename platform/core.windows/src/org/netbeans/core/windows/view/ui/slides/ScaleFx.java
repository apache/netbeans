/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.core.windows.view.ui.slides;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.CellRendererPane;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;

/** 
 * Scale effect with customizable alpha and effect speed.
 *
 * @author Dafe Simonek
 */
final class ScaleFx implements SlidingFx, ActionListener {
    
    private float initialAlpha = 0.1f;
    private float finishAlpha = 0.9f;
    
    private int iterCount = 9;
    
    private int curIter = 0;
    
    private static final float DIVIDING_FACTOR = 2.0f;
    
    private static final int FRAME_DELAY = 20;
    
    private Rectangle middle = new Rectangle();
    private Rectangle current = new Rectangle();
    
    private Timer timer = null;
    
    private StretchedImageComp stretchedImage = new StretchedImageComp();
    
    private Rectangle[] path;
    
    private JLayeredPane pane;
    
    private SlideOperation operation;
    
    private Image preparedImage;
    
    private ChangeListener finishL;
    
    private final boolean shouldOperationWait;
    
    public ScaleFx(float initialAlpha, float finishAlpha, boolean shouldOperationWait) {
        setTransparency(initialAlpha, finishAlpha);
        this.shouldOperationWait = shouldOperationWait;
    }
    
    public void prepareEffect(SlideOperation operation) {
        Component comp = operation.getComponent();
        preparedImage = createCompImage(operation.getComponent(), operation.getComponent().getSize());
    }    
    
    public void showEffect(JLayeredPane pane, Integer layer, SlideOperation operation) {
        this.pane = pane;
        this.operation = operation;
        Component comp = operation.getComponent();
        Graphics2D gr2d = (Graphics2D)pane.getGraphics();
        Rectangle start = operation.getStartBounds();
        Rectangle finish = operation.getFinishBounds();
        Dimension finishSize = finish.getSize();
        Dimension startSize = start.getSize();
        Rectangle current = start;
        Image compImage = preparedImage;

       /* if (compImage == null) {
            if (finishSize.width * finishSize.height > startSize.width * startSize.height) {
                compImage = renderCompIntoImage(comp, finishSize, pane);
            } else {
                compImage = renderCompIntoImage(comp, startSize, pane);
            }
        }*/
        pane.add(stretchedImage, layer);
        
        path = computePath(start, finish);
        
        curIter = 1;
        if (compImage != null) {
            stretchedImage.setOrigImage(compImage);
        } else {
            if (finishSize.width * finishSize.height > startSize.width * startSize.height) {
                stretchedImage.setComp(comp, finishSize);
            } else {
                stretchedImage.setComp(comp, startSize);
            }
        }
        stretchedImage.setBoundsAndAlpha(start, initialAlpha);
        
        getTimer().start();
    }
    
    public void actionPerformed(ActionEvent e) {
        float coef = (float)curIter / (float)(iterCount - 1);
        float curAlpha = (1 - coef) * initialAlpha + coef * finishAlpha;
        
        stretchedImage.setBoundsAndAlpha(path[curIter], curAlpha);
        
        curIter++;
        
        if (curIter >= iterCount) {
            getTimer().stop();
            finish();
        }
    }
    
    private void finish () {
        pane.remove(stretchedImage);
        stretchedImage.cleanup();
        // notify about end of effect
        if (finishL != null) {
            finishL.stateChanged(null);
        }
    }
    
    public void setTransparency(float initialAlpha, float finishAlpha) {
        this.initialAlpha = initialAlpha;
        this.finishAlpha = finishAlpha;
    }
    
    private void setSuggestedIterations(int count) {
        if (count < 3) {
            count = 3;
        }
        // make iterations odd number for easier path computing, see computePath
        this.iterCount = count % 2 == 0 ? count + 1 : count;
    }
    
    private Rectangle[] computePath(Rectangle start, Rectangle finish) {
        Rectangle[] path = new Rectangle[iterCount];
        
        middle.x = Math.abs((finish.x + start.x) / 2);
        middle.y = Math.abs((finish.y + start.y) / 2);
        middle.width = Math.abs((finish.width + start.width) / 2);
        middle.height = Math.abs((finish.height + start.height) / 2);
        
        current = new Rectangle(middle);
        for (int i = iterCount / 2 - 1; i >= 0; i--) {
            current.x = (int)Math.abs((current.x + start.x) / DIVIDING_FACTOR);
            current.y = (int)Math.abs((current.y + start.y) / DIVIDING_FACTOR);
            current.width = (int)Math.abs((current.width + start.width) / DIVIDING_FACTOR);
            current.height = (int)Math.abs((current.height + start.height) / DIVIDING_FACTOR);
            path[i] = new Rectangle(current);
        }
        path[iterCount / 2] = new Rectangle(middle);
        current = middle;
        for (int i = iterCount / 2 + 1; i < iterCount; i++) {
            current.x = (int)Math.abs((current.x + finish.x) / DIVIDING_FACTOR);
            current.y = (int)Math.abs((current.y + finish.y) / DIVIDING_FACTOR);
            current.width = (int)Math.abs((current.width + finish.width) / DIVIDING_FACTOR);
            current.height = (int)Math.abs((current.height + finish.height) / DIVIDING_FACTOR);
            path[i] = new Rectangle(current);
        }
        
        return path;
    }
    
    private Image createCompImage(Component comp, Dimension targetSize) {
        // component won't paint if not showing anyway, so don't create
        // empty image but honestly return null 
        if (!comp.isShowing()) {
            return null;
        }
        
        Image image = comp.createImage(comp.getWidth(), comp.getHeight());
        
        /*BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration().
                createCompatibleImage(comp.getWidth(), comp.getHeight());*/
        //BufferedImage image = new BufferedImage (targetSize.width, targetSize.height, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D gr2d = (Graphics2D)image.getGraphics();
        
        comp.paint(gr2d);
        
        gr2d.dispose();
        
        return image;
    }        
    
    /*private Image renderCompIntoImage(Component comp, Dimension targetSize, Container parent) {
        BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration().
                createCompatibleImage(targetSize.width, targetSize.height);
        
        if (comp.isShowing()) {
            // just paint component directly without double buffering into image
            boolean wasDoubleBuffered = false;
            if ((comp instanceof JComponent) && ((JComponent)comp).isDoubleBuffered()) {
                wasDoubleBuffered = true;
                ((JComponent)comp).setDoubleBuffered(false);
            }

            comp.setBounds(0, 0, targetSize.height, targetSize.height);
            
            Graphics2D gr2d = image.createGraphics();
            try {
                comp.paint(gr2d);
            } finally {
                gr2d.dispose();
            }
            
            if (wasDoubleBuffered && (comp instanceof JComponent)) {
                ((JComponent)comp).setDoubleBuffered(true);
            }
        } else {
            // use renderer to make component believe that it's showing and
            // paint itself correctly
System.out.println("Rendering using comp renderer...");            
            compRenderer.setRenderee(comp);
            parent.add(compRenderer);
            Graphics2D gr2d = image.createGraphics();
            rendererPane.paintComponent(gr2d, compRenderer, parent, 0, 0, targetSize.width, targetSize.height, false);
            parent.remove(compRenderer);
            gr2d.dispose();
        }
        
        return image;
    }*/
    
    private Timer getTimer () {
        if (timer == null) {
            timer = new Timer (FRAME_DELAY, this);
            timer.setRepeats(true);
        }
        return timer;
    }
    
    public void setFinishListener(ChangeListener finishL) {
        this.finishL = finishL;
    }
    
    public boolean shouldOperationWait() {
        return shouldOperationWait;
    }
    
    
    // XXX - TBD - this component should be changed to glass pane to not interfere
    // with layered pane real components in any situation
    private class StretchedImageComp extends JComponent {

        private Image origImage;
        
        private float alpha = 1.0f;
        
        private Component comp;
        
        private Dimension scaleSource, targetSize;
        
        public void setComp(Component comp, Dimension targetSize) {
            this.comp = comp;
            this.targetSize = targetSize;
        }
        
        public void setOrigImage(Image origImage) {
            this.origImage = origImage;
        }
        
        public void setScaleSource(Dimension scaleSource) {
            this.scaleSource = scaleSource;
        }
        
        public void setBoundsAndAlpha(Rectangle bounds, float alpha) {
            this.alpha = alpha;
            setBounds(bounds);
            if (origImage == null) {
                origImage = tryCreateImage();
            }
        }
        
        private Image tryCreateImage () {
            Image result = null;
            if (comp != null && isDisplayable()) {
                comp.setSize(targetSize);
                add(comp);
                result = createCompImage(comp, targetSize);
                remove(comp);
            }
            return result;
        }
        
        public void cleanup() {
            comp = null;
            origImage = null;
            scaleSource = null;
            targetSize = null;
        }

        @Override
        public void paint(Graphics g) {
            Rectangle bounds = getBounds();
            
            if (origImage == null) {
                if (comp == null) {
                    return;
                }
                origImage = tryCreateImage();
                if (origImage == null) {
                    return;
                }
            }
            Image img = origImage;
            Graphics2D g2d = (Graphics2D) g;
            Composite origComposite = g2d.getComposite();
            g2d.setComposite (AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            /*AffineTransform at = AffineTransform.getScaleInstance(
                (double)bounds.width / (double)scaleSource.width,
                (double)bounds.height / (double)scaleSource.height);
            g2d.setTransform(at);*/
            g2d.drawImage(img, 0, 0, bounds.width, bounds.height, null);
            //SwingUtilities.paintComponent(g, getComponent(0), this, 0, 0, bounds.width, bounds.height);
            //super.paint(g2d);
            
            if (origComposite != null) {
                g2d.setComposite(origComposite);
            }
        }
        

    } // StretchedImageComp
    
    
}
