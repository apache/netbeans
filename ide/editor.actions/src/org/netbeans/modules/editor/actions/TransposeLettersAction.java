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

import java.awt.event.ActionEvent;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.lib2.DocUtils;

/**
 * Transpose letter at caret offset with the next one (useful when making typo).
 *
 * @since 1.12
 */
@EditorActionRegistration(name = EditorActionNames.transposeLetters)
public class TransposeLettersAction extends BaseAction {
    
    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        if (target != null) {
            final Document doc = target.getDocument();
            DocUtils.runAtomicAsUser(doc, new Runnable() {
                @Override
                public void run() {
                    Caret caret = target.getCaret();
                    if(caret instanceof EditorCaret) {
                        EditorCaret editorCaret = (EditorCaret) caret;
                        for (CaretInfo caretInfo : editorCaret.getSortedCarets()) {
                            if (!DocUtils.transposeLetters(doc, caretInfo.getDot())) {
                                // Cannot transpose (at end of doc) => beep
                                target.getToolkit().beep();
                            }
                        }
                    } else {
                        if (!DocUtils.transposeLetters(doc, target.getCaretPosition())) {
                            // Cannot transpose (at end of doc) => beep
                            target.getToolkit().beep();
                        }
                    }
                }
            });
        }
    }
    
}
