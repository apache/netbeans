/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
            if (caret != null && caret instanceof EditorCaret) {
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
