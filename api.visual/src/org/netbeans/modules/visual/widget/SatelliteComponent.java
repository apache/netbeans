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
package org.netbeans.modules.visual.widget;

import org.netbeans.api.visual.widget.Scene;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.*;
import org.netbeans.modules.visual.laf.DefaultLookFeel;

/**
 * @author David Kaspar
 */
public final class SatelliteComponent extends JComponent implements MouseListener, MouseMotionListener, Scene.SceneListener, ComponentListener {

    private Scene scene;

    public SatelliteComponent (Scene scene) {
        this.scene = scene;
        setDoubleBuffered (true);
        setPreferredSize (new Dimension (128, 128));
        addMouseListener (this);
        addMouseMotionListener (this);
    }

    public void addNotify () {
        super.addNotify ();
        scene.addSceneListener (this);
        JComponent viewComponent = scene.getView ();
        if (viewComponent == null)
            viewComponent = scene.createView ();
        viewComponent.addComponentListener (this);
        repaint ();
    }

    public void removeNotify () {
        scene.getView ().removeComponentListener (this);
        scene.removeSceneListener (this);
        super.removeNotify ();
    }

    public void paint (Graphics g) {
        Graphics2D gr = (Graphics2D) g;
        super.paint (g);
        Rectangle bounds = scene.getBounds ();
        Dimension size = getSize ();

        double sx = bounds.width > 0 ? (double) size.width / bounds.width : 0.0;
        double sy = bounds.width > 0 ? (double) size.height / bounds.height : 0.0;
        double scale = Math.min (sx, sy);

        int vw = (int) (scale * bounds.width);
        int vh = (int) (scale * bounds.height);
        int vx = (size.width - vw) / 2;
        int vy = (size.height - vh) / 2;

        AffineTransform previousTransform = gr.getTransform ();
        gr.translate (vx, vy);
        gr.scale (scale, scale);

        scene.paint (gr);
        gr.setTransform (previousTransform);

        JComponent component = scene.getView ();
        double zoomFactor = scene.getZoomFactor ();
        Rectangle viewRectangle = component != null ? component.getVisibleRect () : null;
        if (viewRectangle != null) {
            Rectangle window = new Rectangle (
                (int) ((double) viewRectangle.x * scale / zoomFactor),
                (int) ((double) viewRectangle.y * scale / zoomFactor),
                (int) ((double) viewRectangle.width * scale / zoomFactor),
                (int) ((double) viewRectangle.height * scale / zoomFactor)
            );
            window.translate (vx, vy);
//            Area area = new Area (new Rectangle (vx, vy, vw, vh));
//            area.subtract (new Area (window));
            gr.setColor (new Color (200, 200, 200, 128));
            gr.fill (window);
            gr.setColor ((new DefaultLookFeel()).getForeground()/*Color.BLACK*/);
            gr.drawRect (window.x, window.y, window.width - 1, window.height - 1);
        }
    }

    public void mouseClicked (MouseEvent e) {
    }

    public void mousePressed (MouseEvent e) {
        moveVisibleRect (e.getPoint ());
    }

    public void mouseReleased (MouseEvent e) {
        moveVisibleRect (e.getPoint ());
    }

    public void mouseEntered (MouseEvent e) {
    }

    public void mouseExited (MouseEvent e) {
    }

    public void mouseDragged (MouseEvent e) {
        moveVisibleRect (e.getPoint ());
    }

    public void mouseMoved (MouseEvent e) {
    }

    private void moveVisibleRect (Point center) {
        JComponent component = scene.getView ();
        if (component == null)
            return;
        double zoomFactor = scene.getZoomFactor ();
        Rectangle bounds = scene.getBounds ();
        Dimension size = getSize ();

        double sx = bounds.width > 0 ? (double) size.width / bounds.width : 0.0;
        double sy = bounds.width > 0 ? (double) size.height / bounds.height : 0.0;
        double scale = Math.min (sx, sy);

        int vw = (int) (scale * bounds.width);
        int vh = (int) (scale * bounds.height);
        int vx = (size.width - vw) / 2;
        int vy = (size.height - vh) / 2;

        int cx = (int) ((double) (center.x - vx) / scale * zoomFactor);
        int cy = (int) ((double) (center.y - vy) / scale * zoomFactor);

        Rectangle visibleRect = component.getVisibleRect ();
        visibleRect.x = cx - visibleRect.width / 2;
        visibleRect.y = cy - visibleRect.height / 2;
        component.scrollRectToVisible (visibleRect);

    }

    public void sceneRepaint () {
        repaint ();
    }

    public void sceneValidating () {
    }

    public void sceneValidated () {
    }

    public void componentResized (ComponentEvent e) {
        repaint ();
    }

    public void componentMoved (ComponentEvent e) {
        repaint ();
    }

    public void componentShown (ComponentEvent e) {
    }

    public void componentHidden (ComponentEvent e) {
    }
}
