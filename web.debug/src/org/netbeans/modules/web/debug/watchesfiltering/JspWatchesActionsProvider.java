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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.web.debug.watchesfiltering;

import org.netbeans.spi.viewmodel.*;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Watch;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.DialogDisplayer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.Dialog;
import java.util.*;

/**
 * Provides actions for JspElWatch nodes.
 *
 * @author Maros Sandor
 */
public class JspWatchesActionsProvider implements NodeActionsProvider {

    private static final Action NEW_WATCH_ACTION = new AbstractAction
        (NbBundle.getBundle(JspWatchesActionsProvider.class).getString("CTL_WatchAction_AddNew")) {
            public void actionPerformed (ActionEvent e) {
                newWatch();
            }
    };
    private static final Action DELETE_ALL_ACTION = new AbstractAction 
        (NbBundle.getBundle(JspWatchesActionsProvider.class).getString("CTL_WatchAction_DeleteAll")) {
            public void actionPerformed (ActionEvent e) {
                DebuggerManager.getDebuggerManager ().removeAllWatches ();
            }
    };
    private static final Action DELETE_ACTION = Models.createAction (
        NbBundle.getBundle(JspWatchesActionsProvider.class).getString("CTL_WatchAction_Delete"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    ((JspElWatch) nodes [i]).getWatch().remove ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    static { 
        DELETE_ACTION.putValue (
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke ("DELETE")
        );
    };
    private static final Action CUSTOMIZE_ACTION = Models.createAction (
        NbBundle.getBundle(JspWatchesActionsProvider.class).getString("CTL_WatchAction_Customize"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                customize(((JspElWatch) nodes[0]).getWatch());
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node instanceof JspElWatch)
            return new Action [] {
                NEW_WATCH_ACTION,
                null,
                DELETE_ACTION,
                DELETE_ALL_ACTION,
                null,
                CUSTOMIZE_ACTION
            };
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node instanceof JspElWatch) {
            customize(((JspElWatch) node).getWatch());
            return;
        }
        throw new UnknownTypeException (node);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }

    private static void customize(Watch w) {
        WatchPanel wp = new WatchPanel(w.getExpression());
        JComponent panel = wp.getPanel();

        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor(
            panel,
            NbBundle.getMessage(JspWatchesActionsProvider.class, "CTL_Edit_Watch_Dialog_Title", // NOI18N 
                                           w.getExpression() )
        );
        dd.setHelpCtx(new HelpCtx("debug.add.watch"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        dialog.dispose();

        if (dd.getValue() != org.openide.DialogDescriptor.OK_OPTION) return;
        w.setExpression(wp.getExpression());
    }
    
    private static void newWatch () {
        WatchPanel wp = new WatchPanel ("");
        JComponent panel = wp.getPanel ();

        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (
            panel,
            NbBundle.getMessage(JspWatchesActionsProvider.class, "CTL_New_Watch_Dialog_Title") // NOI18N
        );
        dd.setHelpCtx (new HelpCtx ("debug.new.watch"));
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (dd);
        dialog.setVisible (true);
        dialog.dispose ();

        if (dd.getValue () != org.openide.DialogDescriptor.OK_OPTION) return;
        DebuggerManager.getDebuggerManager ().createWatch (wp.getExpression ());
    }
    
}
