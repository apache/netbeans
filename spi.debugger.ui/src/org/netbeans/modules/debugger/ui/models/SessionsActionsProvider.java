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

package org.netbeans.modules.debugger.ui.models;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * @author   Jan Jancura
 */
public class SessionsActionsProvider implements NodeActionsProvider {
    
    private RequestProcessor sessionActionsRP;
    
    private synchronized RequestProcessor getSessionActionsRP() {
        if (sessionActionsRP == null) {
            sessionActionsRP = new RequestProcessor("SessionsActionsProvider", 1, true);
        }
        return sessionActionsRP;
    }
    
    private final Action FINISH_ALL_ACTION = new AbstractAction 
        (NbBundle.getBundle(SessionsActionsProvider.class).getString("CTL_SessionAction_FinishAll_Label")) {
            @Override
            public boolean isEnabled() {
                return DebuggerManager.getDebuggerManager().getSessions().length > 0;
            }

            public void actionPerformed (ActionEvent e) {
                getSessionActionsRP().post(new Runnable() {
                    public void run() {
                        Session[] ss = DebuggerManager.getDebuggerManager ().
                            getSessions ();
                        int i, k = ss.length;
                        for (i = 0; i < k; i++)
                            ss [i].kill ();
                    }
                });
            }
    };
    private Action MAKE_CURRENT_ACTION = Models.createAction (
        NbBundle.getBundle(SessionsActionsProvider.class).getString("CTL_SessionAction_MakeCurrent_Label"), new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return DebuggerManager.getDebuggerManager ().
                    getCurrentSession () != node;
            }
            
            public void perform (Object[] nodes) {
                DebuggerManager.getDebuggerManager ().setCurrentSession (
                    (Session) nodes [0]
                );
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    private final Action FINISH_ACTION = Models.createAction (
        NbBundle.getBundle(SessionsActionsProvider.class).getString("CTL_SessionAction_Finish_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (final Object[] nodes) {
                getSessionActionsRP().post(new Runnable() {
                    public void run() {
                        int i, k = nodes.length;
                        for (i = 0; i < k; i++)
                            ((Session) nodes [i]).kill ();
                    }
                });
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [] {
                FINISH_ALL_ACTION
            };
        if (node instanceof Session)
            return new Action [] {
                MAKE_CURRENT_ACTION,
                FINISH_ACTION,
                null,
                FINISH_ALL_ACTION
            };
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof Session) {
            if (DebuggerManager.getDebuggerManager ().getCurrentSession () == 
                node
            ) return;
            DebuggerManager.getDebuggerManager ().setCurrentSession (
                (Session) node
            );
            return;
        }
        throw new UnknownTypeException (node);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }
}
