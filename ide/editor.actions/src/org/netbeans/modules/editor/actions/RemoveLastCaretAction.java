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
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.spi.editor.AbstractEditorAction;

/**
 *
 * @author Ralph Ruijs <ralphbenjamin@netbeans.org>
 */
@EditorActionRegistrations({
    @EditorActionRegistration(name = EditorActionNames.removeLastCaret, category = "edit.multicaret")
})
public class RemoveLastCaretAction extends AbstractEditorAction {

    @Override
    protected void actionPerformed(ActionEvent evt, JTextComponent component) {
        if (component != null) {
            Caret caret = component.getCaret();
            if(caret instanceof EditorCaret) {
                EditorCaret editorCaret = (EditorCaret) caret;
                editorCaret.removeLastCaret();
            }
        }
    }
    
}
