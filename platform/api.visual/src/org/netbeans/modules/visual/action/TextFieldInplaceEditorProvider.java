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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.EnumSet;

/**
 * @author David Kaspar
 */
public final class TextFieldInplaceEditorProvider implements InplaceEditorProvider<JTextField> {

    private TextFieldInplaceEditor editor;
    private EnumSet<InplaceEditorProvider.ExpansionDirection> expansionDirections;

    private KeyListener keyListener;
    private FocusListener focusListener;
    private DocumentListener documentListener;

    public TextFieldInplaceEditorProvider (TextFieldInplaceEditor editor, EnumSet<InplaceEditorProvider.ExpansionDirection> expansionDirections) {
        this.editor = editor;
        this.expansionDirections = expansionDirections;
    }

    public JTextField createEditorComponent (EditorController controller, Widget widget) {
        if (! editor.isEnabled (widget))
            return null;
        JTextField field = new JTextField (editor.getText (widget));
        field.selectAll ();
        Scene scene = widget.getScene();
        double zoomFactor = scene.getZoomFactor ();
        if (zoomFactor > 1.0) {
            Font font = scene.getDefaultFont();
            font = font.deriveFont((float) (font.getSize2D() * zoomFactor));
            field.setFont (font);
        }
        return field;
    }

    public void notifyOpened (final EditorController controller, Widget widget, JTextField editor) {
        editor.setMinimumSize (new Dimension (64, 19));
        keyListener = new KeyAdapter() {
            @Override
            public void keyPressed (KeyEvent e) {
                switch (e.getKeyChar ()) {
                    case KeyEvent.VK_ESCAPE:
                        e.consume ();
                        controller.closeEditor (false);
                        break;
                    case KeyEvent.VK_ENTER:
                        e.consume ();
                        controller.closeEditor (true);
                        break;
                }
            }
        };
        focusListener = new FocusAdapter() {
            @Override
            public void focusLost (FocusEvent e) {
                controller.closeEditor (true);
            }
        };
        documentListener = new DocumentListener () {
            public void insertUpdate (DocumentEvent e) {
                controller.notifyEditorComponentBoundsChanged ();
            }

            public void removeUpdate (DocumentEvent e) {
                controller.notifyEditorComponentBoundsChanged ();
            }

            public void changedUpdate (DocumentEvent e) {
                controller.notifyEditorComponentBoundsChanged ();
            }
        };
        editor.addKeyListener (keyListener);
        editor.addFocusListener (focusListener);
        editor.getDocument ().addDocumentListener (documentListener);
        editor.selectAll ();
    }

    public void notifyClosing (EditorController controller, Widget widget, JTextField editor, boolean commit) {
        editor.getDocument ().removeDocumentListener (documentListener);
        editor.removeFocusListener (focusListener);
        editor.removeKeyListener (keyListener);
        if (commit) {
            this.editor.setText (widget, editor.getText ());
            if (widget != null)
                widget.getScene ().validate ();
        }
    }

    public Rectangle getInitialEditorComponentBounds(EditorController controller, Widget widget, JTextField editor, Rectangle viewBounds) {
        return null;
    }

    public EnumSet<ExpansionDirection> getExpansionDirections (EditorController controller, Widget widget, JTextField editor) {
        return expansionDirections;
    }

}
