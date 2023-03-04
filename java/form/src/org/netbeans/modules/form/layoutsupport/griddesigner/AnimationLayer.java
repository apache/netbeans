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

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.Timer;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;

/**
 * Support for animation of layout changes.
 *
 * @author Jan Stola
 */
public class AnimationLayer implements ActionListener {
    /** The length of the animation (in milliseconds). */
    private static final long ANIMATION_LENGTH_MILLIS = Math.max(1, Integer.getInteger("netbeans.modules.form.grid.animationlength", 300)); // NOI18N
    /** Initial bounds of components in the animated container. */
    private Map<Component, Rectangle> startBounds = new HashMap<Component, Rectangle>();
    /** Final bounds of components in the animated container. */
    private Map<Component, Rectangle> endBounds = new HashMap<Component, Rectangle>();
    /** All (non-VIP) components that appear in the initial or final layout of the animated container. */
    private Set<Component> components = new HashSet<Component>();
    /** VIP components - components that are painted at the end (in the specified order). */
    private List<Component> vipComponents = Collections.EMPTY_LIST;
    /** Phase of the animation (between 0 and 1 inclusive). */
    private float phase;
    /** Start time of the animation (in nanoseconds). */
    private long startTime;
    /** End time of the animation (in nanoseconds). */
    private long endTime;
    /** Timer responsible for the animation. */
    private Timer timer = new Timer(0, this);
    /** Animated container. */
    private Container container;
    /** Glasspane over the animated container. Repaints are invoked on the glasspane. */
    private JComponent glassPane;
    /** Dimension of the container during animation. */
    private Dimension maxContDim = new Dimension();
    /** Image of the empty container. */
    private Image containerImage;

    /**
     * Sets the animated container.
     *
     * @param container container being animated.
     */
    public void setContainer(Container container) {
        this.container = container;
    }

    /**
     * Sets the glass pane over the animated container.
     *
     * @param glassPane glass pane over the animated container.
     */
    public void setGlassPane(JComponent glassPane) {
        this.glassPane = glassPane;
    }

    /**
     * Reads the initial bounds of components from the animated container.
     */
    public void loadStart() {
        loadBounds(startBounds);
    }

    /**
     * Reads the final bounds of components from the animated container.
     */
    public void loadEnd() {
        loadBounds(endBounds);
    }

    /**
     * Loads the current bounds of components from the animated container.
     *
     * @param map where to store the loaded bounds.
     */
    private void loadBounds(Map<Component, Rectangle> map) {
        map.clear();
        for (Component comp : container.getComponents()) {
            if (!GridUtils.isPaddingComponent(comp) && comp.isVisible()) {
                map.put(comp, comp.getBounds());
            }
        }
        Dimension contDim = container.getSize();
        maxContDim = new Dimension(Math.max(maxContDim.width, contDim.width), Math.max(maxContDim.height, contDim.height));
    }

    /**
     * Sets VIP components. VIP components are painted at the end
     * (in the specified order). This ensures that they appear above
     * other components.
     *
     * @param vipComponents VIP components.
     */
    public void setVIPComponents(List<Component> vipComponents) {
        this.vipComponents = vipComponents;
    }

    /**
     * Starts the animation.
     */
    public void animate() {
        phase = 0;
        components.clear();
        components.addAll(startBounds.keySet());
        components.addAll(endBounds.keySet());
        components.remove(container);
        createContainerImage();
        components.removeAll(vipComponents);
        startTime = System.nanoTime();
        endTime = startTime + ANIMATION_LENGTH_MILLIS*1000000;
        timer.restart();
    }

    /**
     * Creates the image of empty animated container.
     */
    private void createContainerImage() {
        Map<Component, Boolean> map = new HashMap<Component, Boolean>();
        for (Component comp : components) {
            map.put(comp, comp.isVisible());
            comp.setVisible(false);
        }
        Dimension oldSize = container.getSize();
        container.setSize(maxContDim);
        containerImage = container.createImage(maxContDim.width, maxContDim.height);
        if (containerImage != null) { // should not be null after fix of bug 225537, but just fo sure
            container.paint(containerImage.getGraphics());
        }
        container.setSize(oldSize);
        for (Component comp : components) {
            comp.setVisible(map.get(comp));
        }
    }

    /**
     * Updates the animation phase according to the current time.
     */
    private void updatePhase() {
        long currentTime = System.nanoTime();
        float linPhase = ((float)(currentTime-startTime))/(endTime-startTime);
        linPhase = Math.min(linPhase, 1f);
        if (linPhase <= 0.5f) {
            // Acceleration
            phase = 2*linPhase*linPhase;
        } else {
            // Deceleration
            linPhase = 1-linPhase;
            phase = 1-2*linPhase*linPhase;
        }
        phase = Math.min(Math.max(0,phase), 1f);
    }

    /**
     * Returns the current animation phase.
     *
     * @return the current animation phase.
     */
    public float getPhase() {
        return phase;
    }

    /**
     * Returns the current bounds (i.e., the bounds corresponding
     * to the current phase) of the given component.
     *
     * @param comp component whose bounds should be returned.
     * @return current bounds of the given component.
     */
    private Rectangle currentBounds(Component comp) {
        Rectangle start = startBounds.get(comp);
        Rectangle end = endBounds.get(comp);
        Rectangle bounds;
        if (start == null) {
            bounds = end;
        } else if (end == null) {
            bounds = start;
        } else {
            bounds = new Rectangle(
                currentValue(start.x, end.x),
                currentValue(start.y, end.y),
                currentValue(start.width, end.width),
                currentValue(start.height, end.height)
            );
        }
        return bounds;
    }

    /**
     * Returns the current value (i.e., the value corresponding
     * to the current phase) of the given component.
     *
     * @param start initial value.
     * @param end final value.
     * @return current value.
     */
    private int currentValue(int start, int end) {
        return Math.round(start*(1-phase)+end*phase);
    }

    /**
     * Returns the current opacity (i.e., the opacity corresponding
     * to the current phase) of the given component.
     *
     * @param comp component whose opacity should be returned.
     * @return opacity of the given component.
     */
    private float currentAlpha(Component comp) {
        boolean start = startBounds.containsKey(comp);
        boolean end = endBounds.containsKey(comp);
        float alpha;
        if (!start) {
            alpha = phase;
        } else if (!end) {
            alpha = 1f-phase;
        } else {
            alpha = 1f;
        }
        return alpha;
    }

    /**
     * Initial point of the next frame (invoked by the animation timer).
     *
     * @param e action event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        updatePhase();
        RepaintManager manager = RepaintManager.currentManager(glassPane);
        manager.markCompletelyDirty(glassPane);
        manager.paintDirtyRegions();
    }

    /**
     * Paints the current state (i.e., the state corresponding to the current
     * phase) of the animated container.
     *
     * @param g graphics context.
     */
    public void paint(Graphics g) {
        if (timer.isRunning() && containerImage != null) {
            Graphics gClip = g.create();
            gClip.setClip(0, 0, containerImage.getWidth(null), containerImage.getHeight(null));
            gClip.drawImage(containerImage, 0, 0, null);
            for (Component comp : components) {
                paintComponent(gClip, comp);
            }
            for (Component comp : vipComponents) {
                paintComponent(gClip, comp);
            }
            if (phase == 1f) {
                timer.stop();
                maxContDim = new Dimension();
                for (Map.Entry<Component,Rectangle> entry : endBounds.entrySet()) {
                    Component comp = entry.getKey();
                    comp.setBounds(entry.getValue());
                    comp.validate();
                }
            }
            gClip.dispose();
        }
    }

    /**
     * Paints the current state (i.e. the state corresponding to the current
     * phase) of the given component.
     *
     * @param g graphics context.
     * @param comp component to paint.
     */
    private void paintComponent(Graphics g, Component comp) {
        Rectangle bounds = currentBounds(comp);
        float alpha = currentAlpha(comp);
        Graphics gg = g.create(bounds.x, bounds.y, bounds.width, bounds.height);
        if (alpha != 1f) {
            AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            ((Graphics2D)gg).setComposite(alphaComposite);
        }
        comp.setBounds(bounds);
        comp.validate();
        // Intentionally using print instead of paint.
        // Print doesn't use double buffering and it solves some mysterious
        // problems with modified clip during painting of containers.
        // BTW: animated transitions library also uses print()
        if (comp instanceof JComponent) {
            comp.print(gg);
        } else {
            java.awt.peer.ComponentPeer peer = FakePeerSupport.getPeer(comp);
            if (peer != null) {
                peer.paint(gg);
            }
        }
        gg.dispose();
    }

}
