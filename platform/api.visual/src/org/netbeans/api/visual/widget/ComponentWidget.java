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
package org.netbeans.api.visual.widget;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * This widget allows to use an AWT/Swing component in the scene. The widget itself just represents and reserve the place
 * occupied by the component. When a component is resized, then reserved place is recalculated. The component placement
 * is automatically update based on the placement of the widget.
 * The widget also paints the component in the satelite views.
 * <p>
 * When a widget is added into the scene, it has to attach a scene listener for automatic recalculation. The attaching
 * is done automatically for the first time.
 *
 * @author David Kaspar
 */
// TODO - addComponentResizeListener
// TODO - fix calculateClientArea method - use convertViewToScene instead
public class ComponentWidget extends Widget {

    private ComponentWrapper componentWrapper;
    private Component component;
    private boolean componentAdded;
    private boolean widgetAdded;
    private double zoomFactor = Double.MIN_VALUE;
    private ComponentSceneListener validateListener;
    private ComponentComponentListener componentListener;
    private boolean componentVisible = false;

    private static class ComponentWrapper extends JComponent {
        @Override
        public void paint(Graphics g) {
        }

        void doPaint(Graphics g) {
            super.paint(g);
        }
    };

    /**
     * Creates a component widget.
     * @param scene the scene
     * @param component the AWT/Swing component
     */
    public ComponentWidget (Scene scene, Component component) {
        super(scene);
        componentWrapper = new ComponentWrapper();
        componentWrapper.setLayout(new BorderLayout(0, 0));
        componentWrapper.add(component);
        this.component = component;
        validateListener = null;
        componentListener = new ComponentComponentListener ();
        setComponentVisible (true);
    }

    /**
     * Returns a AWT/Swing component.
     * @return the AWT/Swing component
     */
    public final Component getComponent () {
        return component;
    }

    /**
     * Returns whether the component should be visible.
     * @return true if the component is visible
     */
    public final boolean isComponentVisible () {
        return componentVisible;
    }

    /**
     * Sets whether the component should be visible.
     * @param componentVisible if true, then the component is visible
     */
    public final void setComponentVisible (boolean componentVisible) {
        if (this.componentVisible == componentVisible)
            return;
        this.componentVisible = componentVisible;
        attach ();
        revalidate ();
    }

    @Override
    protected final void notifyAdded () {
        widgetAdded = true;
        attach ();
    }

    @Override
    protected final void notifyRemoved () {
        widgetAdded = false;
    }

    private void attach () {
        if (validateListener != null)
            return;
        validateListener = new ComponentSceneListener ();
        getScene ().addSceneListener (validateListener);
    }

    private void detach () {
        if (validateListener == null)
            return;
        getScene ().removeSceneListener (validateListener);
        validateListener = null;
    }

    /**
     * Calculates a client area from the preferred size of the component.
     * @return the calculated client area
     */
    @Override
    protected final Rectangle calculateClientArea () {
        Dimension preferredSize = component.getPreferredSize ();
        zoomFactor = getScene ().getZoomFactor ();
        preferredSize.width = (int) Math.floor (preferredSize.width / zoomFactor);
        preferredSize.height = (int) Math.floor (preferredSize.height / zoomFactor);
        return new Rectangle (preferredSize);
    }

    private void addComponent () {
        Scene scene = getScene ();
        if (! componentAdded) {
            scene.getView().add(componentWrapper);
            scene.getView ().revalidate ();
            component.addComponentListener (componentListener);
            componentAdded = true;
        }
        component.removeComponentListener (componentListener);
        componentWrapper.setBounds(scene.convertSceneToView(convertLocalToScene(getClientArea())));
        component.addComponentListener (componentListener);
        component.repaint();
    }

    private void removeComponent () {
        Scene scene = getScene ();
        if (componentAdded) {
            component.removeComponentListener (componentListener);
            scene.getView().remove(componentWrapper);
            scene.getView ().revalidate ();
            componentAdded = false;
        }
    }

    /**
     * Paints the component widget.
     */
    @Override
    protected final void paintWidget() {
        RepaintManager rm = RepaintManager.currentManager(null);
        boolean isDoubleBuffered = component instanceof JComponent && rm.isDoubleBufferingEnabled();
        if (isDoubleBuffered) {
            rm.setDoubleBufferingEnabled(false);
        }
        Graphics2D graphics = getGraphics();
        Rectangle bounds = getClientArea();
        AffineTransform previousTransform = graphics.getTransform();
        graphics.translate(bounds.x, bounds.y);
        double zoomFactor = getScene().getZoomFactor();
        graphics.scale(1 / zoomFactor, 1 / zoomFactor);
        if (componentVisible) {
            componentWrapper.doPaint(graphics);
        } else {
            component.paint(graphics);
        }
        graphics.setTransform(previousTransform);
        if (isDoubleBuffered) {
            rm.setDoubleBufferingEnabled(true);
        }
    }

    private final class ComponentSceneListener implements Scene.SceneListener {

        public void sceneRepaint () {
        }

        public void sceneValidating () {
            double newZoomFactor = getScene ().getZoomFactor ();
            if (Math.abs (newZoomFactor - zoomFactor) != 0.0) {
                revalidate ();
                zoomFactor = newZoomFactor;
            }
        }

        public void sceneValidated () {
            if (widgetAdded  &&  componentVisible)
                addComponent ();
            else {
                removeComponent ();
                detach ();
            }
        }
    }

    private final class ComponentComponentListener implements ComponentListener {

        public void componentResized (ComponentEvent e) {
            revalidate ();
        }

        public void componentMoved (ComponentEvent e) {
            revalidate ();
        }

        public void componentShown (ComponentEvent e) {
        }

        public void componentHidden (ComponentEvent e) {
        }

    }

}
