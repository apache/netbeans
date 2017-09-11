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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
