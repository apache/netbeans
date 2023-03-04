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
package org.openide.actions;

import java.awt.HeadlessException;
import java.util.MissingResourceException;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import javax.swing.UIManager;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Redo an edit. Since version 6.18 this class implements {@link ContextAwareAction}.
 * 
 * @see UndoAction
 * @author Ian Formanek, Jaroslav Tulach
 */
public class RedoAction extends CallableSystemAction implements ContextAwareAction {
    private static String SWING_DEFAULT_LABEL = UIManager.getString("AbstractUndoableEdit.redoText"); //NOI18N

    @Override
    public boolean isEnabled() {
        UndoAction.initializeUndoRedo();

        return super.isEnabled();
    }

    public String getName() {
        //#40823 related. AbstractUndoableEdit prepends "Undo/Redo" strings before the custom text,
        // resulting in repetitive text in UndoAction/RedoAction. attempt to remove the AbstractUndoableEdit text
        // keeping our text because it has mnemonics.
        String redo = UndoAction.getUndoRedo().getRedoPresentationName();

        if ((redo != null) && (SWING_DEFAULT_LABEL != null) && redo.startsWith(SWING_DEFAULT_LABEL)) {
            redo = redo.substring(SWING_DEFAULT_LABEL.length()).trim();
        }

        if (redo == null || redo.trim ().length () == 0) {
            return NbBundle.getMessage(RedoAction.class, "RedoSimple");
        } else {
            return NbBundle.getMessage(RedoAction.class, "RedoWithParameter", redo);
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(RedoAction.class);
    }

    @Override
    protected String iconResource() {
        return "org/openide/resources/actions/redo.gif"; // NOI18N
    }

    public void performAction() {
        try {
            UndoRedo undoRedo = UndoAction.getUndoRedo();

            if (undoRedo.canRedo()) {
                undoRedo.redo();
            }
        } catch (CannotRedoException ex) {
            UndoRedoAction.cannotUndoRedo(ex);
        }

        UndoAction.updateStatus();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new UndoRedoAction(actionContext, false, false);
    }
}
