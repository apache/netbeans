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

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import org.netbeans.modules.visual.laf.DefaultLookFeel;

/**
 * @author David Kaspar
 */
public class BirdViewWindow extends JWindow implements MouseMotionListener {

    private Scene scene;
    private BirdViewComponent birdView;

    private boolean shown = false;
    private double zoomFactor = 3.0;
    private Point scenePoint;

    private WidgetAction action = new SceneTrackAction ();
    private ViewAncestorListener ancestorListener = new ViewAncestorListener ();

    public BirdViewWindow (final Scene scene) {
        this.scene = scene;
        setSize (new Dimension (256, 256));
        setLayout (new BorderLayout ());
        setAlwaysOnTop (true);

        JPanel pane = new JPanel ();
        pane.setBorder (new CompoundBorder (new LineBorder ((new DefaultLookFeel()).getForeground()/*Color.BLACK*/, 1), new EmptyBorder (1, 1, 1, 1)));
        pane.setLayout (new BorderLayout ());
        add (pane, BorderLayout.CENTER);

        birdView = new BirdViewComponent ();
        birdView.setDoubleBuffered (true);
        pane.add (birdView, BorderLayout.CENTER);
    }

    public void invokeShow () {
        if (scene.getView () == null)
            return;
        if (shown)
            return;
        shown = true;
        birdView.addMouseMotionListener (this);
        scene.getPriorActions ().addAction (action);
        scene.getView ().addAncestorListener (ancestorListener);
        updateForViewPoint (null);
    }

    public void invokeHide () {
        if (! shown)
            return;
        shown = false;
        birdView.removeMouseMotionListener (this);
        scene.getPriorActions ().removeAction (action);
        scene.getView ().removeAncestorListener (ancestorListener);
        updateForViewPoint (null);
    }

    public void invokeRepaint () {
        birdView.repaint ();
    }

    public void setZoomFactor (double zoomFactor) {
        this.zoomFactor = zoomFactor;
        invokeRepaint ();
    }

    public void setWindowSize (Dimension size) {
        Dimension previousSize = getSize ();
        setSize (size);
        if (isShowing ()) {
            Point location = getLocation ();
            setLocation (location.x + (previousSize.width - size.width) / 2, location.y + (previousSize.height - size.height) / 2);
            validate ();
        }
    }

    private void updateForViewPoint (Point viewPoint) {
        JComponent view = scene.getView ();
        if (! shown  ||  viewPoint == null  ||  ! view.getVisibleRect ().contains (viewPoint)) {
            scenePoint = null;
            setVisible (false);
            dispose ();
            return;
        }
        scenePoint = scene.convertViewToScene (viewPoint);
        Point viewOrigin = view.getLocationOnScreen ();
        Dimension size = getSize ();
        setBounds (viewOrigin.x + viewPoint.x - size.width / 2, viewOrigin.y + viewPoint.y - size.height / 2, size.width, size.height);
        setVisible (true);
        birdView.repaint();
    }

    private void updateForBirdViewPoint (Point birdViewPoint) {
        JComponent view = scene.getView ();
        if (view.isShowing ()  &&  isShowing ()) {
            Point viewOrigin = view.getLocationOnScreen ();
            Point birdViewOrigin = getLocationOnScreen ();
            Dimension size = getSize ();
            updateForViewPoint (new Point (birdViewPoint.x + birdViewOrigin.x - viewOrigin.x, birdViewPoint.y + birdViewOrigin.y - viewOrigin.y));
        } else
            updateForViewPoint (null);
    }

    public void mouseDragged (MouseEvent e) {
        updateForBirdViewPoint (e.getPoint ());
    }

    public void mouseMoved (MouseEvent e) {
        updateForBirdViewPoint (e.getPoint ());
    }

    private class SceneTrackAction implements WidgetAction {

        public State mouseClicked (Widget widget, WidgetMouseEvent event) {
            return State.CONSUMED;
        }

        public State mousePressed (Widget widget, WidgetMouseEvent event) {
            return State.CONSUMED;
        }

        public State mouseReleased (Widget widget, WidgetMouseEvent event) {
            return State.CONSUMED;
        }

        public State mouseEntered (Widget widget, WidgetMouseEvent event) {
            return State.CONSUMED;
        }

        public State mouseExited (Widget widget, WidgetMouseEvent event) {
            return State.CONSUMED;
        }

        public State mouseDragged (Widget widget, WidgetMouseEvent event) {
            updateForViewPoint (widget.getScene ().convertSceneToView (widget.convertLocalToScene (event.getPoint ())));
            return State.CONSUMED;
        }

        public State mouseMoved (Widget widget, WidgetMouseEvent event) {
            updateForViewPoint (widget.getScene ().convertSceneToView (widget.convertLocalToScene (event.getPoint ())));
            return State.CONSUMED;
        }

        public State mouseWheelMoved (Widget widget, WidgetMouseWheelEvent event) {
            return State.CONSUMED;
        }

        public State keyTyped (Widget widget, WidgetKeyEvent event) {
            return State.CONSUMED;
        }

        public State keyPressed (Widget widget, WidgetKeyEvent event) {
            return State.CONSUMED;
        }

        public State keyReleased (Widget widget, WidgetKeyEvent event) {
            return State.CONSUMED;
        }

        public State focusGained (Widget widget, WidgetFocusEvent event) {
            return State.CONSUMED;
        }

        public State focusLost (Widget widget, WidgetFocusEvent event) {
            return State.CONSUMED;
        }

        public State dragEnter (Widget widget, WidgetDropTargetDragEvent event) {
            return State.CONSUMED;
        }

        public State dragOver (Widget widget, WidgetDropTargetDragEvent event) {
            return State.CONSUMED;
        }

        public State dropActionChanged (Widget widget, WidgetDropTargetDragEvent event) {
            return State.CONSUMED;
        }

        public State dragExit (Widget widget, WidgetDropTargetEvent event) {
            return State.CONSUMED;
        }

        public State drop (Widget widget, WidgetDropTargetDropEvent event) {
            return State.CONSUMED;
        }

    }

    private class BirdViewComponent extends JComponent {

        public void paint (Graphics g) {
            Graphics2D gr = (Graphics2D) g;
            super.paint (g);
            if (scenePoint == null) {
                gr.setColor ((new DefaultLookFeel()).getForeground()/*Color.BLACK*/);
                gr.fill (getBounds ());
                return;
            }
            Dimension size = getSize ();
            AffineTransform previousTransform = gr.getTransform ();
            gr.translate (size.width / 2, size.height / 2);
            gr.scale (zoomFactor, zoomFactor);
            gr.translate (- scenePoint.x, - scenePoint.y);
            scene.paint (gr);
            gr.setTransform (previousTransform);
        }

    }

    private class ViewAncestorListener implements AncestorListener {

        public void ancestorAdded (AncestorEvent event) {
        }

        public void ancestorRemoved (AncestorEvent event) {
            invokeHide ();
        }

        public void ancestorMoved (AncestorEvent event) {
        }

    }

}
