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
package org.netbeans.modules.editor.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.api.editor.caret.CaretMoveContext;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.netbeans.spi.editor.caret.CaretMoveHandler;

/**
 *
 * @author Ralph Ruijs
 */
@EditorActionRegistrations({
    @EditorActionRegistration(name = EditorActionNames.addCaretUp, category = "edit.multicaret"),
    @EditorActionRegistration(name = EditorActionNames.addCaretDown, category = "edit.multicaret")
})
public class AddCaretAction extends AbstractEditorAction {

    @Override
    protected void actionPerformed(ActionEvent evt, final JTextComponent target) {
        if (target != null) {
            Caret caret = target.getCaret();
            if (caret instanceof EditorCaret) {
                final EditorCaret editorCaret = (EditorCaret) caret;
                final BaseDocument doc = (BaseDocument) target.getDocument();
                final boolean upAction = EditorActionNames.addCaretUp.equals(actionName());
                doc.runAtomicAsUser(new Runnable() {
                    @Override
                    public void run() {
                        final List<Position> dotAndMarks = new ArrayList<>(editorCaret.getCarets().size() << 1);
                        final List<Position.Bias> dotAndMarkBiases = new ArrayList<>(editorCaret.getCarets().size() << 1);
                        editorCaret.moveCarets(new CaretMoveHandler() {
                            @Override
                            public void moveCarets(CaretMoveContext context) {
                                for (org.netbeans.api.editor.caret.CaretInfo caretInfo : context.getOriginalCarets()) {
                                    try {
                                        int dot = caretInfo.getDot();
                                        Position.Bias dotBias = caretInfo.getDotBias();
                                        Point p = caretInfo.getMagicCaretPosition();
                                        if (p == null) {
                                            Rectangle r = target.getUI().modelToView(target, dot, dotBias);
                                            if (r != null) {
                                                p = new Point(r.x, r.y);
                                                context.setMagicCaretPosition(caretInfo, p);
                                            } else {
                                                return; // model to view failed
                                            }
                                        }
                                        try {
                                            dot = upAction
                                                    ? Utilities.getPositionAbove(target, dot, p.x)
                                                    : Utilities.getPositionBelow(target, dot, p.x);
                                            // TODO compute proper bias by using getNextVisualPositionFrom() either in target.navifilter or target.ui
                                            Position dotPos = doc.createPosition(dot);
                                            dotAndMarks.add(dotPos);
                                            dotAndMarks.add(dotPos);
                                            dotAndMarkBiases.add(dotBias);
                                            dotAndMarkBiases.add(dotBias);
                                            
                                        } catch (BadLocationException e) {
                                            // the position stays the same
                                        }
                                    } catch (BadLocationException ex) {
                                        target.getToolkit().beep();
                                    }
                                }
                            }
                        });

                        editorCaret.addCarets(dotAndMarks, dotAndMarkBiases);
                    }
                });
            }
        }
    }

}
