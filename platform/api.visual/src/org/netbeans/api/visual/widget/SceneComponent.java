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
package org.netbeans.api.visual.widget;

import org.netbeans.api.visual.action.WidgetAction;

import javax.swing.*;
import javax.accessibility.AccessibleContext;
import javax.accessibility.Accessible;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.List;
import org.netbeans.modules.visual.laf.DefaultLookFeel;
import org.openide.awt.GraphicsUtils;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
final class SceneComponent extends JComponent implements Accessible, MouseListener, MouseMotionListener, KeyListener, MouseWheelListener,FocusListener, DropTargetListener {

    private Scene scene;
    private Widget lockedWidget;
    private WidgetAction lockedAction;
    private long eventIDcounter = 0;
    private AccessibleJComponent accessible = new AccessibleSceneComponent ();

    public SceneComponent (Scene scene) {
        this.scene = scene;
        setOpaque (false);
        setDoubleBuffered (true);
        setLayout (null);
        addMouseListener (this);
        addMouseMotionListener (this);
        addMouseWheelListener (this);
        addKeyListener (this);
        setDropTarget (new DropTarget (this, DnDConstants.ACTION_COPY_OR_MOVE, this));
        setAutoscrolls (true);
        setRequestFocusEnabled (true);
        setFocusable (true);
        setFocusTraversalKeysEnabled (false);
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SceneComponent.class, "ACS_SceneComponentName"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SceneComponent.class, "ACS_SceneComponentDesc"));
    }

    @Override
    public void addNotify () {
        super.addNotify ();
        ToolTipManager.sharedInstance ().registerComponent (this);
        scene.setGraphics ((Graphics2D) getGraphics ());
        scene.revalidate ();
        scene.setViewShowing (true);
        scene.validate ();
    }

    @Override
    public void removeNotify () {
        super.removeNotify ();
        ToolTipManager.sharedInstance ().unregisterComponent (this);
        scene.setViewShowing (false);
    }

    @Override
    public AccessibleContext getAccessibleContext () {
        return accessible;
    }

    @Override
    public void setBounds (int x, int y, int width, int height) {
        super.setBounds (x, y, width, height);

        Rectangle bounds = scene.getBounds ();
        double zoomFactor = scene.getZoomFactor();
        if (bounds != null  &&  width == (int) (bounds.width * zoomFactor) && height == (int) (bounds.height * zoomFactor))
            return;

        scene.revalidate ();
        scene.validate ();
    }

    @Override
    public void paint (Graphics g) {
//        System.out.println ("CLIP: " + g.getClipBounds ());
//        long s = System.currentTimeMillis ();
        Graphics2D gr = (Graphics2D) g;
        GraphicsUtils.configureDefaultRenderingHints(gr);
        scene.setGraphics (gr);

        AffineTransform previousTransform = gr.getTransform ();
        double zoomFactor = scene.getZoomFactor ();
        gr.scale (zoomFactor, zoomFactor);
        scene.setPaintEverything (false);
        scene.paint ();
        scene.setPaintEverything (true);
        gr.setTransform (previousTransform);

        g.setColor ((new DefaultLookFeel()).getForeground()/*Color.BLACK*/);
        super.paint (g);
//        System.out.println ("PAINT Time: " + (System.currentTimeMillis () - s));
    }

    public void focusGained(FocusEvent e) {
        processOperator (Operator.FOCUS_GAINED, new WidgetAction.WidgetFocusEvent (++ eventIDcounter, e));
    }

    public void focusLost(FocusEvent e) {
        processOperator (Operator.FOCUS_LOST, new WidgetAction.WidgetFocusEvent (++ eventIDcounter, e));
    }

    public void mouseClicked (MouseEvent e) {
        processLocationOperator (Operator.MOUSE_CLICKED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mousePressed (MouseEvent e) {
        processLocationOperator (Operator.MOUSE_PRESSED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mouseReleased (MouseEvent e) {
        processLocationOperator (Operator.MOUSE_RELEASED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mouseEntered (MouseEvent e) {
        processLocationOperator (Operator.MOUSE_ENTERED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mouseExited (MouseEvent e) {
        processLocationOperator (Operator.MOUSE_EXITED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mouseDragged (MouseEvent e) {
        processLocationOperator (Operator.MOUSE_DRAGGED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mouseMoved (MouseEvent e) {
        MouseContext context = new MouseContext ();
        Point point = scene.convertViewToScene (e.getPoint ());
        resolveContext (scene, point, context);
        context.commit (this);
        processLocationOperator (Operator.MOUSE_MOVED, new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e));
    }

    public void mouseWheelMoved (MouseWheelEvent e) {
        processLocationOperator (Operator.MOUSE_WHEEL, new WidgetAction.WidgetMouseWheelEvent (++ eventIDcounter, e));
    }

    public void keyTyped (KeyEvent e) {
        WidgetAction.State state = processKeyOperator (Operator.KEY_TYPED, new WidgetAction.WidgetKeyEvent (++ eventIDcounter, e));
        if (state.isConsumed ())
            e.consume ();
    }

    public void keyPressed (KeyEvent e) {
        // HACK for invoking tooltip using Ctrl+F1 because a condition in ToolTipManager.shouldRegisterBindings cannot be satisfied
        if (e.getKeyCode () == KeyEvent.VK_F1  && (e.getModifiers () & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) {
            MouseContext context = new MouseContext ();
            resolveContext (scene.getFocusedWidget (), context);
            context.commit (this);

            Widget focusedWidget = scene.getFocusedWidget ();
            Point location = focusedWidget.getScene ().convertSceneToView (focusedWidget.convertLocalToScene (focusedWidget.getBounds ().getLocation ()));
            MouseEvent event = new MouseEvent (this, 0, 0, 0, location.x, location.y, 0, false);

            ToolTipManager manager = ToolTipManager.sharedInstance ();
            manager.mouseEntered (event);
            manager.mouseMoved (event);
        }

        WidgetAction.State state = processKeyOperator (Operator.KEY_PRESSED, new WidgetAction.WidgetKeyEvent (++ eventIDcounter, e));
        if (state.isConsumed ())
            e.consume ();
    }

    public void keyReleased (KeyEvent e) {
        WidgetAction.State state = processKeyOperator (Operator.KEY_RELEASED, new WidgetAction.WidgetKeyEvent (++ eventIDcounter, e));
        if (state.isConsumed ())
            e.consume ();
    }

    public void dragEnter (DropTargetDragEvent e) {
        WidgetAction.State state = processLocationOperator (Operator.DRAG_ENTER, new WidgetAction.WidgetDropTargetDragEvent (++ eventIDcounter, e));
        if (! state.isConsumed ())
            e.rejectDrag ();
    }

    public void dragOver (DropTargetDragEvent e) {
        WidgetAction.State state = processLocationOperator (Operator.DRAG_OVER, new WidgetAction.WidgetDropTargetDragEvent (++ eventIDcounter, e));
        if (! state.isConsumed ())
            e.rejectDrag ();
    }

    public void dropActionChanged (DropTargetDragEvent e) {
        WidgetAction.State state = processLocationOperator (Operator.DROP_ACTION_CHANGED, new WidgetAction.WidgetDropTargetDragEvent (++ eventIDcounter, e));
        if (! state.isConsumed ())
            e.rejectDrag ();
    }

    public void dragExit (DropTargetEvent e) {
        processOperator (Operator.DRAG_EXIT, new WidgetAction.WidgetDropTargetEvent (++ eventIDcounter, e));
    }

    public void drop (DropTargetDropEvent e) {
        WidgetAction.State state = processLocationOperator (Operator.DROP, new WidgetAction.WidgetDropTargetDropEvent (++ eventIDcounter, e));
        if (! state.isConsumed ())
            e.rejectDrop ();
        else
            e.dropComplete (true);
    }

    private WidgetAction.State processLocationOperator (Operator operator, WidgetAction.WidgetLocationEvent event) {
        Point oldSceneLocation = scene.getLocation ();
        Rectangle oldVisibleRect = getVisibleRect ();
        Point viewPoint = event.getPoint ();
        Point oldScenePoint = scene.convertViewToScene (viewPoint);
        event.setPoint (new Point (oldScenePoint));

        WidgetAction.State state;
        Point location;
        String tool = scene.getActiveTool ();

        WidgetAction.Chain priorActions = scene.getPriorActions ();
        if (! priorActions.getActions ().isEmpty ()) {
            location = scene.getLocation ();
            event.translatePoint (location.x, location.y);
            if (operator.operate (priorActions, scene, event).isConsumed ())
                return WidgetAction.State.CONSUMED;
            event.translatePoint (- location.x, - location.y);
        }

        if (lockedAction != null) {
            location = lockedWidget.convertSceneToLocal (new Point ());
            event.translatePoint (location.x, location.y);
            state = operator.operate (lockedAction, lockedWidget, event);
            event.translatePoint (- location.x, - location.y);

            if (! state.isConsumed ()) {
                location = scene.getLocation ();
                event.translatePoint (location.x, location.y);
                state = processLocationOperator (operator, tool, scene, event);
            }
        } else {
            location = scene.getLocation ();
            event.translatePoint (location.x, location.y);
            state = processLocationOperator (operator, tool, scene, event);
        }

        lockedWidget = state.getLockedWidget ();
        lockedAction = state.getLockedAction ();
        scene.validate ();

        if (lockedAction != null) {
            Point sceneLocation = scene.getLocation ();
            Rectangle visibleRect = getVisibleRect ();
            int xadd = (int) ((sceneLocation.x - oldSceneLocation.x) * scene.getZoomFactor ());
            int yadd = (int) ((sceneLocation.y - oldSceneLocation.y) * scene.getZoomFactor ());
            if (xadd != 0  ||  yadd != 0)
                scrollRectToVisible (new Rectangle (oldVisibleRect.x + xadd, oldVisibleRect.y + yadd, visibleRect.width, visibleRect.height));
            scrollRectToVisible (new Rectangle (scene.convertSceneToView (oldScenePoint)));
        }

        return state;
    }

    private WidgetAction.State processLocationOperator (Operator operator, String tool, Widget widget, WidgetAction.WidgetLocationEvent event) {
        if (! widget.isVisible ()  ||  ! widget.isEnabled ())
            return WidgetAction.State.REJECTED;

        Point location = widget.getLocation ();
        event.translatePoint (- location.x, - location.y);

        Rectangle bounds = widget.getBounds ();
        assert bounds != null : Widget.MESSAGE_NULL_BOUNDS;
        if (bounds.contains (event.getPoint ())) {
            WidgetAction.State state;

            List<Widget> children = widget.getChildren ();
            Widget[] childrenArray = children.toArray (new Widget[0]);

            for (int i = childrenArray.length - 1; i >= 0; i --) {
                Widget child = childrenArray[i];
                state = processLocationOperator (operator, tool, child, event);
                if (state.isConsumed ())
                    return state;
            }

            if (widget.isHitAt (event.getPoint ())) {
                WidgetAction.Chain actions;
                actions = widget.getActions ();
                state = operator.operate (actions, widget, event);
                if (state.isConsumed ())
                    return state;

                actions = widget.getActions (tool);
                if (actions != null) {
                    state = operator.operate (actions, widget, event);
                    if (state.isConsumed ())
                        return state;
                }
            }
        }

        event.translatePoint (location.x, location.y);
        return WidgetAction.State.REJECTED;
    }

    private WidgetAction.State processOperator (Operator operator, WidgetAction.WidgetEvent event) {
        WidgetAction.State state;
        String tool = scene.getActiveTool ();

        WidgetAction.Chain priorActions = scene.getPriorActions ();
        if (! priorActions.getActions ().isEmpty ())
            if (operator.operate (priorActions, scene, event).isConsumed ())
                return WidgetAction.State.CONSUMED;

        if (lockedAction != null) {
            state = operator.operate (lockedAction, lockedWidget, event);
            if (! state.isConsumed ())
                state = processOperator (operator, tool, scene, event);
        } else
            state = processOperator (operator, tool, scene, event);

        lockedWidget = state.getLockedWidget ();
        lockedAction = state.getLockedAction ();
        scene.validate ();

        if (lockedWidget != null)
            scrollRectToVisible (scene.convertSceneToView (lockedWidget.convertLocalToScene (lockedWidget.getBounds ())));

        return state;
    }

    private WidgetAction.State processOperator (Operator operator, String tool, Widget widget, WidgetAction.WidgetEvent event) {
        if (! widget.isVisible ()  ||  ! widget.isEnabled ())
            return WidgetAction.State.REJECTED;

        WidgetAction.State state;

        List<Widget> children = widget.getChildren ();
        Widget[] childrenArray = children.toArray (new Widget[0]);

        for (int i = childrenArray.length - 1; i >= 0; i --) {
            Widget child = childrenArray[i];
            state = processOperator (operator, tool, child, event);
            if (state.isConsumed ())
                return state;
        }

        state = operator.operate (widget.getActions (), widget, event);
        if (state.isConsumed ())
            return state;

        WidgetAction.Chain actions = widget.getActions (tool);
        if (actions != null) {
            state = operator.operate (actions, widget, event);
            if (state.isConsumed ())
                return state;
        }

        return WidgetAction.State.REJECTED;
    }

    private WidgetAction.State processSingleOperator (Operator operator, String tool, Widget widget, WidgetAction.WidgetEvent event) {
        WidgetAction.State state;

        state = operator.operate (widget.getActions (), widget, event);
        if (state.isConsumed ())
            return state;

        WidgetAction.Chain actions = widget.getActions (tool);
        if (actions != null) {
            state = operator.operate (actions, widget, event);
            if (state.isConsumed ())
                return state;
        }

        return WidgetAction.State.REJECTED;
    }

    private WidgetAction.State processParentOperator (Operator operator, String tool, Widget widget, WidgetAction.WidgetKeyEvent event) {
        while (widget != null) {
            WidgetAction.State state;

            state = operator.operate (widget.getActions (), widget, event);
            if (state.isConsumed ())
                return state;

            WidgetAction.Chain actions = widget.getActions (tool);
            if (actions != null) {
                state = operator.operate (actions, widget, event);
                if (state.isConsumed ())
                    return state;
            }

            widget = widget.getParentWidget ();
        }

        return WidgetAction.State.REJECTED;
    }

    private Widget resolveTopMostDisabledWidget (Widget widget) {
        Widget disabledWidget = null;
        Widget tempWidget = widget;
        while (tempWidget != null) {
            if (! tempWidget.isVisible ()  ||  ! tempWidget.isEnabled ())
                disabledWidget = tempWidget;
            tempWidget = tempWidget.getParentWidget ();
        }
        return disabledWidget;
    }

    private WidgetAction.State processKeyOperator (Operator operator, WidgetAction.WidgetKeyEvent event) {
        WidgetAction.State state;
        String tool = scene.getActiveTool ();

        WidgetAction.Chain priorActions = scene.getPriorActions ();
        if (! priorActions.getActions ().isEmpty ())
            if (operator.operate (priorActions, scene, event).isConsumed ())
                return WidgetAction.State.CONSUMED;

        if (lockedAction != null) {
            state = operator.operate (lockedAction, lockedWidget, event);
            if (! state.isConsumed ())
                state = processKeyOperator (operator, tool, scene, event);
        } else
            state = processKeyOperator (operator, tool, scene, event);

        lockedWidget = state.getLockedWidget ();
        lockedAction = state.getLockedAction ();
        scene.validate ();

        if (lockedWidget != null)
            scrollRectToVisible (scene.convertSceneToView (lockedWidget.convertLocalToScene (lockedWidget.getBounds ())));

        return state;
    }

    private WidgetAction.State processKeyOperator (Operator operator, String tool, Scene scene, WidgetAction.WidgetKeyEvent event) {
        Widget focusedWidget = scene.getFocusedWidget ();
        WidgetAction.State state;
        Widget disabledWidget;
        switch (scene.getKeyEventProcessingType ()) {
            case ALL_WIDGETS:
                return processOperator (operator, tool, scene, event);
            case FOCUSED_WIDGET_AND_ITS_PARENTS:
                disabledWidget = resolveTopMostDisabledWidget (focusedWidget);
                return processParentOperator (operator, tool, disabledWidget != null ? disabledWidget.getParentWidget () : focusedWidget, event);
            case FOCUSED_WIDGET_AND_ITS_CHILDREN:
                disabledWidget = resolveTopMostDisabledWidget (focusedWidget);
                if (disabledWidget != null)
                    return WidgetAction.State.REJECTED;
                state = processSingleOperator (operator, tool, focusedWidget, event);
                if (state.isConsumed ())
                    return state;
                return processOperator (operator, tool, focusedWidget, event);
            case FOCUSED_WIDGET_AND_ITS_CHILDREN_AND_ITS_PARENTS:
                disabledWidget = resolveTopMostDisabledWidget (focusedWidget);
                if (disabledWidget == null) {
                    state = processSingleOperator (operator, tool, focusedWidget, event);
                    if (state.isConsumed ())
                        return state;
                    state = processOperator (operator, tool, focusedWidget, event);
                    if (state.isConsumed ())
                        return state;
                }
                return processParentOperator (operator, tool, disabledWidget != null ? disabledWidget.getParentWidget () : focusedWidget.getParentWidget (), event);
            default:
                throw new IllegalStateException ();
        }
    }

    private boolean resolveContext (Widget widget, Point point, MouseContext context) {
//        Point location = widget.getLocation ();
//        point.translate (- location.x, - location.y);

        if (widget.getBounds ().contains (point)) {
            List<Widget> children = widget.getChildren ();
            for (int i = children.size () - 1; i >= 0; i --) {
                Widget child = children.get (i);
                Point location = child.getLocation ();
                point.translate (- location.x, - location.y);
                boolean resolved = resolveContext (child, point, context);
                point.translate (location.x, location.y);
                if (resolved)
                    return true;
            }
            if (widget.isHitAt (point))
                context.update (widget, point);
        }

//        point.translate (location.x, location.y);
        return false;
    }

    private void resolveContext (Widget widget, MouseContext context) {
        if (widget == null)
            return;
        context.update (widget, null);
        resolveContext (widget.getParentWidget (), context);
    }


    private interface Operator {

        public static final Operator MOUSE_CLICKED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseClicked (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_PRESSED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mousePressed (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_RELEASED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseReleased (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_ENTERED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseEntered (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_EXITED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseExited (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_DRAGGED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseDragged (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_MOVED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseMoved (widget, (WidgetAction.WidgetMouseEvent) event);
            }
        };

        public static final Operator MOUSE_WHEEL = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.mouseWheelMoved (widget, (WidgetAction.WidgetMouseWheelEvent) event);
            }
        };

        public static final Operator KEY_TYPED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.keyTyped (widget, (WidgetAction.WidgetKeyEvent) event);
            }
        };

        public static final Operator KEY_PRESSED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.keyPressed (widget, (WidgetAction.WidgetKeyEvent) event);
            }
        };

        public static final Operator KEY_RELEASED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.keyReleased (widget, (WidgetAction.WidgetKeyEvent) event);
            }
        };

        public static final Operator FOCUS_GAINED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.focusGained (widget, (WidgetAction.WidgetFocusEvent) event);
            }
        };

        public static final Operator FOCUS_LOST = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.focusLost (widget, (WidgetAction.WidgetFocusEvent) event);
            }
        };

        public static final Operator DRAG_ENTER = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.dragEnter (widget, (WidgetAction.WidgetDropTargetDragEvent) event);
            }
        };

        public static final Operator DRAG_OVER = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.dragOver (widget, (WidgetAction.WidgetDropTargetDragEvent) event);
            }
        };

        public static final Operator DROP_ACTION_CHANGED = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.dropActionChanged (widget, (WidgetAction.WidgetDropTargetDragEvent) event);
            }
        };

        public static final Operator DRAG_EXIT = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.dragExit (widget, (WidgetAction.WidgetDropTargetEvent) event);
            }
        };

        public static final Operator DROP = new Operator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event) {
                return action.drop (widget, (WidgetAction.WidgetDropTargetDropEvent) event);
            }
        };

        public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetEvent event);

    }

    private static final class MouseContext {

        private String toolTipText;
        private Cursor cursor;
//        private AccessibleContext accessibleContext;

        public boolean update (Widget widget, Point localLocation) {
            if (cursor == null  &&  localLocation != null)
                cursor = widget.getCursorAt (localLocation);
            if (toolTipText == null)
                toolTipText = widget.getToolTipText ();
            return cursor == null  ||  toolTipText == null;//  ||  accessibleContext == null;
        }

        public void commit (SceneComponent component) {
            component.setToolTipText (toolTipText);
            component.setCursor (cursor);
        }

    }

    private class AccessibleSceneComponent extends AccessibleJComponent {

        @Override
        public int getAccessibleChildrenCount () {
            return 1;
        }

        @Override
        public Accessible getAccessibleChild (int i) {
            return i == 0 ? scene : null;
        }
    }

}
