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

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.WindowManager;

import java.beans.*;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import javax.swing.UIManager;
import javax.swing.event.*;
import javax.swing.undo.*;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;


/** Context aware undo and redo actions.
*
* @author   Jaroslav Tulach
*/
final class UndoRedoAction extends AbstractAction
implements ContextAwareAction, PropertyChangeListener, ChangeListener, LookupListener, Runnable, HelpCtx.Provider {
    private static final Logger LOG = Logger.getLogger(UndoRedoAction.class.getName());
    /** last edit */
    private UndoRedo last = UndoRedo.NONE;
    private final boolean doUndo;
    private final Lookup.Result<UndoRedo.Provider> result;
    private final boolean fallback;
    private PropertyChangeListener weakPCL;
    private ChangeListener weakCL;
    private LookupListener weakLL;


    UndoRedoAction(Lookup context, boolean doUndo, boolean fallback) {
        this.doUndo = doUndo;
        this.fallback = fallback;
        this.result = context.lookupResult(UndoRedo.Provider.class);
    }

    @Override
    public String toString() {
        return super.toString() + "[undo=" + doUndo + ", fallback: " + fallback + "]"; // NOI18N
    }

    public static Action create(Map<?,?> map) {
        if (Boolean.TRUE.equals(map.get("redo"))) { // NOI18N
            return new UndoRedoAction(Utilities.actionsGlobalContext(), false, true);
        }
        if (Boolean.TRUE.equals(map.get("undo"))) { // NOI18N
            return new UndoRedoAction(Utilities.actionsGlobalContext(), true, true);
        }
        throw new IllegalStateException();
    }


    @Override
    public boolean isEnabled() {
        initializeUndoRedo();
        return super.isEnabled();
    }

    void initializeUndoRedo() {
        assert EventQueue.isDispatchThread();
        if (weakLL != null) {
            return;
        }
        String res;
        if (doUndo) {
            res = "org/openide/resources/actions/undo.gif"; // NOI18N
        } else {
            res = "org/openide/resources/actions/redo.gif"; // NOI18N
        }
        putValue("iconBase", res); // NOI18N
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(res, true));
        if (fallback) {
            Registry r = WindowManager.getDefault().getRegistry();
            weakPCL = WeakListeners.propertyChange(this, r);
            r.addPropertyChangeListener(weakPCL);
        }
        weakCL = WeakListeners.change(this, null);
        weakLL = WeakListeners.create(LookupListener.class, this, result);
        result.addLookupListener(weakLL);
        last = UndoRedo.NONE;

        run();
    }
    
    @Override
    public void run() {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(this);
            return;
        }

        UndoRedo ur = getUndoRedo();
        last.removeChangeListener(weakCL);

        if (doUndo) {
            setEnabled(ur.canUndo());
        } else {
            setEnabled(ur.canRedo());
        }
        putValue(NAME, getName());
        
        last = ur;
        last.addChangeListener(weakCL);
    }

    private UndoRedo getUndoRedo() {
        assert EventQueue.isDispatchThread();
        for (UndoRedo.Provider provider : result.allInstances()) {
            UndoRedo ur = provider.getUndoRedo();
            if (ur != null) {
                return ur;
            }
        }

        if (fallback) {
            TopComponent el = WindowManager.getDefault().getRegistry().getActivated();
            if (el != null) {
                UndoRedo ur = el.getUndoRedo();
                if (ur != null) {
                    return ur;
                }
            }
        }
        return UndoRedo.NONE;
    }

    private String getName() {
        assert EventQueue.isDispatchThread();
        //#40823 related. AbstractUndoableEdit prepends "Undo/Redo" strings before the custom text,
        // resulting in repetitive text in UndoAction/RedoAction. attempt to remove the AbstractUndoableEdit text
        // keeping our text because it has mnemonics.
        String undo = doUndo ? getUndoRedo().getUndoPresentationName() : getUndoRedo().getRedoPresentationName();
        LOG.log (Level.FINE, doUndo ? "getUndoRedo().getUndoPresentationName() returns {0}" : 
                                      "getUndoRedo().getRedoPresentationName() returns {0}", undo);

        if ((undo != null) && (getDefaultSwingText() != null) && undo.startsWith(getDefaultSwingText())) {
            undo = undo.substring(getDefaultSwingText().length()).trim();
        }
        
        LOG.log (Level.FINE, "Name adapted by SWING_DEFAULT_LABEL is {0}", undo);
        String presentationName = null;
        if (undo == null || undo.trim ().length () == 0) {
            presentationName = NbBundle.getMessage(UndoRedoAction.class, doUndo ? "UndoSimple" : "RedoSimple");
        } else {
            presentationName = NbBundle.getMessage(UndoRedoAction.class, doUndo ? "UndoWithParameter" : "RedoWithParameter", undo);
        }
        
        LOG.log (Level.FINE, "Result name is {0}", presentationName);

        return presentationName;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(UndoRedoAction.class);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        UndoRedo undoRedo = getUndoRedo();
        if (doUndo) try {
            if (undoRedo.canUndo()) {
                undoRedo.undo();
            }
        } catch (CannotUndoException ex) {
            cannotUndoRedo(ex);
        } else try {
            if (undoRedo.canRedo()) {
                undoRedo.redo();
            }
        } catch (CannotRedoException ex) {
            cannotUndoRedo(ex);
        }
        run();
    }
    
    static void cannotUndoRedo(RuntimeException ex) throws MissingResourceException, HeadlessException {
        if (ex.getMessage() != null) {
            JOptionPane.showMessageDialog(
                    WindowManager.getDefault().getMainWindow(),
                    ex.getMessage(),
                    NbBundle.getMessage(UndoRedoAction.class, ex instanceof CannotUndoException ? "LBL_CannotUndo" : "LBL_CannotRedo"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    @Override
    public void propertyChange(PropertyChangeEvent ev) {
        if (TopComponent.Registry.PROP_ACTIVATED.equals(ev.getPropertyName())) {
            run();
        }
    }

    @Override
    public void stateChanged(ChangeEvent ev) {
        run();
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        run();
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new UndoRedoAction(actionContext, doUndo, false);
    }

    private String getDefaultSwingText() {
        return doUndo ? UIManager.getString("AbstractUndoableEdit.undoText") : //NOI18N
            UIManager.getString("AbstractUndoableEdit.redoText"); //NOI18N
    }
}
