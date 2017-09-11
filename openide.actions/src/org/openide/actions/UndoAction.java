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

import javax.swing.Action;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.WindowManager;

import java.beans.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.JOptionPane;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.*;
import javax.swing.undo.*;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;


/** Undo an edit. Since version 6.18 this class
* implements {@link ContextAwareAction}.
*
* @see UndoRedo
* @author   Ian Formanek, Jaroslav Tulach
*/
public class UndoAction extends CallableSystemAction
implements ContextAwareAction {
    /** initialized listener */
    private static Listener listener;

    /** last edit */
    private static UndoRedo last = UndoRedo.NONE;
    private static String SWING_DEFAULT_LABEL = UIManager.getString("AbstractUndoableEdit.undoText"); //NOI18N
    private static UndoAction undoAction = null;
    private static RedoAction redoAction = null;

    @Override
    public boolean isEnabled() {
        initializeUndoRedo();

        return super.isEnabled();
    }

    /** Initializes the object.
    */
    static synchronized void initializeUndoRedo() {
        if (listener != null) {
            return;
        }

        listener = new Listener();

        Registry r = WindowManager.getDefault().getRegistry();

        r.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(listener, r));
        last = getUndoRedo();
        last.addChangeListener(listener);

        updateStatus();
    }

    /** Update status of action.
    */
    static synchronized void updateStatus() {
        if (undoAction == null) {
            undoAction = findObject (UndoAction.class, false);
        }

        if (redoAction == null) {
            redoAction = findObject (RedoAction.class, false);
        }

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    UndoRedo ur = getUndoRedo();

                    if (undoAction != null) {
                        undoAction.setEnabled(ur.canUndo());
                    }

                    if (redoAction != null) {
                        redoAction.setEnabled(ur.canRedo());
                    }
                }
            }
        );
    }

    /** Finds current undo/redo.
    */
    static UndoRedo getUndoRedo() {
        TopComponent el = WindowManager.getDefault().getRegistry().getActivated();

        return (el == null) ? UndoRedo.NONE : el.getUndoRedo();
    }

    public String getName() {
        //#40823 related. AbstractUndoableEdit prepends "Undo/Redo" strings before the custom text,
        // resulting in repetitive text in UndoAction/RedoAction. attempt to remove the AbstractUndoableEdit text
        // keeping our text because it has mnemonics.
        String undo = getUndoRedo().getUndoPresentationName();
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "getUndoRedo().getUndoPresentationName() returns " + undo);
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "SWING_DEFAULT_LABEL is " + SWING_DEFAULT_LABEL);

        if ((undo != null) && (SWING_DEFAULT_LABEL != null) && undo.startsWith(SWING_DEFAULT_LABEL)) {
            undo = undo.substring(SWING_DEFAULT_LABEL.length()).trim();
        }
        
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "Name adapted by SWING_DEFAULT_LABEL is " + undo);
        String presentationName = null;
        if (undo == null || undo.trim ().length () == 0) {
            presentationName = NbBundle.getMessage(UndoAction.class, "UndoSimple");
        } else {
            presentationName = NbBundle.getMessage(UndoAction.class, "UndoWithParameter", undo);
        }
        
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "Result name is " + presentationName);

        return presentationName;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(UndoAction.class);
    }

    @Override
    protected String iconResource() {
        return "org/openide/resources/actions/undo.gif"; // NOI18N
    }

    public void performAction() {
        try {
            UndoRedo undoRedo = getUndoRedo();

            if (undoRedo.canUndo()) {
                undoRedo.undo();
            }
        } catch (CannotUndoException ex) {
            UndoRedoAction.cannotUndoRedo(ex);
        }

        updateStatus();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new UndoRedoAction(actionContext, true, false);
    }

    /** Listener on changes of selected workspace element and
    * its changes.
    */
    private static final class Listener implements PropertyChangeListener, ChangeListener {
        Listener() {
        }

        public void propertyChange(PropertyChangeEvent ev) {
            updateStatus();
            last.removeChangeListener(this);
            last = getUndoRedo();
            last.addChangeListener(this);
        }

        public void stateChanged(ChangeEvent ev) {
            updateStatus();
        }
    }
}
