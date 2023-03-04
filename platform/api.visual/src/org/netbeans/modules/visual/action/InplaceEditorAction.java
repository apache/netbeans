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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.modules.visual.util.GeomUtil;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.EnumSet;

/**
 * @author David Kaspar
 */
public final class InplaceEditorAction<C extends JComponent> extends WidgetAction.LockedAdapter implements InplaceEditorProvider.TypedEditorController {

    private InplaceEditorProvider<C> provider;
    private C editor = null;
    private Widget widget = null;
    private Rectangle rectangle = null;
    private InplaceEditorProvider.EditorInvocationType invocationType;

    public InplaceEditorAction(InplaceEditorProvider<C> provider) {
        this.provider = provider;
    }

    protected boolean isLocked() {
        return editor != null;
    }

    @Override
    public State mouseClicked(Widget widget, WidgetMouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
            if (openEditor(widget, InplaceEditorProvider.EditorInvocationType.MOUSE)) {
                return State.createLocked(widget, this);
            }
        }
        return State.REJECTED;
    }

    @Override
    public State mousePressed(Widget widget, WidgetMouseEvent event) {
//        if (editor != null)
//            closeEditor (true);
//        return State.REJECTED;

        if (editor != null) {
            Container parent = editor.getParent();
            if (parent != null) {
                parent.requestFocusInWindow();
            }
            closeEditor(true);
        }
        return State.REJECTED;
    }

    @Override
    public State mouseReleased(Widget widget, WidgetAction.WidgetMouseEvent event) {
//        if (editor != null)
//            closeEditor (true);
//        return State.REJECTED;

        if (editor != null) {
            Container parent = editor.getParent();
            if (parent != null) {
                parent.requestFocusInWindow();
            }
            closeEditor(true);
        }
        return State.REJECTED;
    }

    @Override
    public State keyPressed(Widget widget, WidgetKeyEvent event) {
        if (event.getKeyChar() == KeyEvent.VK_ENTER) {
            if (openEditor(widget, InplaceEditorProvider.EditorInvocationType.KEY)) {
                return State.createLocked(widget, this);
            }
        }
        return State.REJECTED;
    }

    public final boolean isEditorVisible() {
        return editor != null;
    }

    public final boolean openEditor(Widget widget) {
        return openEditor (widget, InplaceEditorProvider.EditorInvocationType.CODE);
    }

    private boolean openEditor (Widget widget, InplaceEditorProvider.EditorInvocationType invocationType) {
        if (editor != null) {
            return false;
        }
        Scene scene = widget.getScene();
        JComponent component = scene.getView();
        if (component == null) {
            return false;
        }
        this.invocationType = invocationType;
        editor = provider.createEditorComponent(this, widget);
        if (editor == null) {
            this.invocationType = null;
            return false;
        }
        this.widget = widget;

        component.add(editor);
        provider.notifyOpened(this, widget, editor);

        Rectangle rectangle = widget.getScene().convertSceneToView(widget.convertLocalToScene(widget.getBounds()));

        Point center = GeomUtil.center(rectangle);
        Dimension size = editor.getMinimumSize();
        if (rectangle.width > size.width) {
            size.width = rectangle.width;
        }
        if (rectangle.height > size.height) {
            size.height = rectangle.height;
        }
        int x = center.x - size.width / 2;
        int y = center.y - size.height / 2;

        rectangle = new Rectangle(x, y, size.width, size.height);
        updateRectangleToFitToView(rectangle);

        Rectangle r = provider.getInitialEditorComponentBounds(this, widget, editor, rectangle);
        this.rectangle = r != null ? r : rectangle;

        editor.setBounds(x, y, size.width, size.height);
        notifyEditorComponentBoundsChanged();
        editor.requestFocusInWindow();

        return true;
    }

    private void updateRectangleToFitToView(Rectangle rectangle) {
        JComponent component = widget.getScene().getView();
        if (rectangle.x + rectangle.width > component.getWidth()) {
            rectangle.x = component.getWidth() - rectangle.width;
        }
        if (rectangle.y + rectangle.height > component.getHeight()) {
            rectangle.y = component.getHeight() - rectangle.height;
        }
        if (rectangle.x < 0) {
            rectangle.x = 0;
        }
        if (rectangle.y < 0) {
            rectangle.y = 0;
        }
    }

    public final void closeEditor(boolean commit) {
        if (editor == null) {
            return;
        }
        Container parent = editor.getParent();
//        boolean hasFocus = editor.hasFocus();

        Rectangle bounds = parent != null ? editor.getBounds() : null;
        provider.notifyClosing(this, widget, editor, commit);

        if (bounds != null) {
            parent.remove(editor);
            parent.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
        }
//        if (hasFocus) {
            if (parent != null) {
                parent.requestFocusInWindow();
            }
//        }
        editor = null;
        widget = null;
        rectangle = null;
        invocationType = null;
    }

    public void notifyEditorComponentBoundsChanged() {
        EnumSet<InplaceEditorProvider.ExpansionDirection> directions = provider.getExpansionDirections(this, widget, editor);
        if (directions == null) {
            directions = EnumSet.noneOf(InplaceEditorProvider.ExpansionDirection.class);
        }
        Rectangle rectangle = this.rectangle;
        Dimension size = editor.getPreferredSize();
        Dimension minimumSize = editor.getMinimumSize();
        if (minimumSize != null) {
            if (size.width < minimumSize.width) {
                size.width = minimumSize.width;
            }
            if (size.height < minimumSize.height) {
                size.height = minimumSize.height;
            }
        }

        int heightDiff = rectangle.height - size.height;
        int widthDiff = rectangle.width - size.width;

        boolean top = directions.contains(InplaceEditorProvider.ExpansionDirection.TOP);
        boolean bottom = directions.contains(InplaceEditorProvider.ExpansionDirection.BOTTOM);

        if (top) {
            if (bottom) {
                rectangle.y += heightDiff / 2;
                rectangle.height = size.height;
            } else {
                rectangle.y += heightDiff;
                rectangle.height = size.height;
            }
        } else {
            if (bottom) {
                rectangle.height = size.height;
            } else {
            }
        }

        boolean left = directions.contains(InplaceEditorProvider.ExpansionDirection.LEFT);
        boolean right = directions.contains(InplaceEditorProvider.ExpansionDirection.RIGHT);

        if (left) {
            if (right) {
                rectangle.x += widthDiff / 2;
                rectangle.width = size.width;
            } else {
                rectangle.x += widthDiff;
                rectangle.width = size.width;
            }
        } else {
            if (right) {
                rectangle.width = size.width;
            } else {
            }
        }

        updateRectangleToFitToView(rectangle);

        editor.setBounds(rectangle);
        editor.repaint();
    }

    public InplaceEditorProvider.EditorInvocationType getEditorInvocationType () {
        return invocationType;
    }

}
