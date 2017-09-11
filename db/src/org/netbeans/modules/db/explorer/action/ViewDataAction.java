/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer.action;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.sql.editor.SQLEditorSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Rob Englander
 */
public class ViewDataAction extends QueryAction {
    private static final RequestProcessor RP = new RequestProcessor(ViewDataAction.class.getName(), 1);

    @Override
    public String getName() {
        return NbBundle.getMessage (ViewDataAction.class, "ViewData"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ViewDataAction.class);
    }

    @Override
    public void performAction (final Node[] activatedNodes) {
        final DatabaseConnection connection = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);
        if (connection != null) {
            RP.post(
                new Runnable() {
                    @Override
                    public void run() {
                        String expression = null;
                        try {
                            expression = getDefaultQuery(activatedNodes);
                            SQLEditorSupport.openSQLEditor(connection.getDatabaseConnection(), expression + ";\n", true); //NOI18N
                        } catch(Exception exc) {
                            Logger.getLogger(ViewDataAction.class.getName()).log(Level.INFO, exc.getLocalizedMessage() + " while executing expression " + expression, exc); // NOI18N
                            String message = NbBundle.getMessage (ViewDataAction.class, "ShowDataError", exc.getMessage()); // NOI18N
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                        }
                    }
                }
            );
        }
    }

}
