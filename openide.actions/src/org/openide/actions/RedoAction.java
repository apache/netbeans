/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
