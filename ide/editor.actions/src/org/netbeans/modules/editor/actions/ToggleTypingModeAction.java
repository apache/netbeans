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
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorUtilities;
import org.netbeans.spi.editor.AbstractEditorAction;

/**
 * Switch caret from insert mode to overwrite mode or vice versa.
 *
 * @author Miloslav Metelka
 */

@EditorActionRegistration(name = EditorActionNames.toggleTypingMode)
public final class ToggleTypingModeAction extends AbstractEditorAction {

    private static final long serialVersionUID = 1L;

    public ToggleTypingModeAction() {
        super();
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            Boolean overwriteMode = (Boolean) target.getClientProperty(EditorUtilities.CARET_OVERWRITE_MODE_PROPERTY);
            // Now toggle
            overwriteMode = (overwriteMode == null || !overwriteMode) ? Boolean.TRUE : Boolean.FALSE;
            target.putClientProperty(EditorUtilities.CARET_OVERWRITE_MODE_PROPERTY, overwriteMode);
        }
    }

}
