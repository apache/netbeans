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
package org.openide.actions;

import java.awt.EventQueue;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.*;


/** Rename a node.
* @see Node#setName
*
* @author   Petr Hamernik, Dafe Simonek
*/
public class RenameAction extends NodeAction {

    private static final RequestProcessor RP = new RequestProcessor(RenameAction.class); // NOI18N

    protected boolean surviveFocusChange() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(RenameAction.class, "Rename");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(RenameAction.class);
    }

    protected boolean enable(Node[] activatedNodes) {
        // exactly one node should be selected
        if ((activatedNodes == null) || (activatedNodes.length != 1)) {
            return false;
        }

        // and must support renaming
        return activatedNodes[0].canRename();
    }

    protected void performAction(final Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0) {
            return;
        }
        Node n = activatedNodes[0]; // we supposed that one node is activated
        
        // for slow FS perform rename out of EDT
        if (EventQueue.isDispatchThread() && Boolean.TRUE.equals(n.getValue("slowRename"))) { // NOI18N
            RP.post(new Runnable() {
                @Override
                public void run() {
                    performAction(activatedNodes);
                }
            });
            return;
        }

        NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(RenameAction.class, "CTL_RenameLabel"),
                NbBundle.getMessage(RenameAction.class, "CTL_RenameTitle")
            );
        dlg.setInputText(n.getName());

        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dlg))) {
            String newname = null;

            try {
                newname = dlg.getInputText();

                if (!newname.equals("")) {
                    n.setName(dlg.getInputText()); // NOI18N
                }
            } catch (IllegalArgumentException e) {
                // determine if "printStackTrace"  and  "new annotation" of this exception is needed
                boolean needToAnnotate = Exceptions.findLocalizedMessage(e) == null;

                // annotate new localized message only if there is no localized message yet
                if (needToAnnotate) {
                    Exceptions.attachLocalizedMessage(e,
                                                      NbBundle.getMessage(RenameAction.class,
                                                                          "MSG_BadFormat",
                                                                          n.getName(),
                                                                          newname));
                }

                Exceptions.printStackTrace(e);
            }
        }
    }

    protected boolean asynchronous() {
        return false;
    }
}
