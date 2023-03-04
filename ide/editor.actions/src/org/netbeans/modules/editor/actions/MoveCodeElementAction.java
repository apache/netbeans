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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.api.editor.EditorUtilities;
import org.netbeans.editor.BaseKit;
import org.netbeans.spi.editor.AbstractEditorAction;

/**
 * Move entire code elements (statements and class members) up or down.
 *
 * @author Dusan Balek
 */
@EditorActionRegistrations({
    @EditorActionRegistration(name = EditorActionNames.moveCodeElementUp,
                              menuPath = "Source",
                              menuPosition = 840,
                              menuText = "#" + EditorActionNames.moveCodeElementUp + "_menu_text"),
    @EditorActionRegistration(name = EditorActionNames.moveCodeElementDown,
                              menuPath = "Source",
                              menuPosition = 860,
                              menuText = "#" + EditorActionNames.moveCodeElementDown + "_menu_text")
})
public class MoveCodeElementAction extends AbstractEditorAction {

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent component) {
        if (component != null) {
            String actionName = EditorActionNames.moveCodeElementUp.equals(actionName())
                    ? BaseKit.moveSelectionElseLineUpAction
                    : BaseKit.moveSelectionElseLineDownAction;
            Action action = EditorUtilities.getAction(component.getUI().getEditorKit(component), actionName);
            if (action != null) {
                action.actionPerformed(evt);
                return;
            }
        }
        Toolkit.getDefaultToolkit().beep();
    }
}
