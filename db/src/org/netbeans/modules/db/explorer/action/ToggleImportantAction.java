/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer.action;

import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.node.ToggleImportantInfo;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Havlin
 */
public class ToggleImportantAction extends BaseAction {

    private String actionDisplayName;

    @Override
    public String getName() {
        return actionDisplayName;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean toggleImportant = false;
        boolean toggleUnImportant = false;

        for (Node node : activatedNodes) {
            ToggleImportantInfo tii = node.getLookup().lookup(ToggleImportantInfo.class);
            if (tii != null) {
                if (!tii.isDefault()) {
                    if (tii.isImportant()) {
                        toggleUnImportant = true;
                    } else {
                        toggleImportant = true;
                    }
                }
            }
        }

        if (toggleUnImportant && toggleImportant) {
            actionDisplayName = NbBundle.getMessage(ToggleImportantAction.class,
                    "ToggleImportant");                                 //NOI18N
            return true;
        } else if (toggleUnImportant) {
            actionDisplayName = NbBundle.getMessage(ToggleImportantAction.class,
                    "ToggleImportantRemove");                           //NOI18N
            return true;
        } else if (toggleImportant) {
            actionDisplayName = NbBundle.getMessage(ToggleImportantAction.class,
                    "ToggleImportantAdd");                              //NOI18N
            return true;
        }
        actionDisplayName = NbBundle.getMessage(ToggleImportantAction.class,
                "ToggleImportant");                                     //NOI18N

        return false;
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {

        for (Node node : activatedNodes) {
            DatabaseConnection conn = node.getLookup().lookup(DatabaseConnection.class);
            ToggleImportantInfo tii = node.getLookup().lookup(ToggleImportantInfo.class);
            if (conn != null && tii != null
                    && Catalog.class.isAssignableFrom(tii.getType())) {
                String name = node.getName();
                if (name.equals(conn.getDefaultCatalog())) {
                    tii.setDefault(true);
                    tii.setImportant(false);
                    conn.removeImportantCatalog(name);
                } else if (!conn.isImportantCatalog(name)) {
                    conn.addImportantCatalog(name);
                    tii.setDefault(false);
                    tii.setImportant(true);
                } else {
                    conn.removeImportantCatalog(name);
                    tii.setDefault(false);
                    tii.setImportant(false);
                }
            } else if (conn != null && tii != null
                    && Schema.class.isAssignableFrom(tii.getType())) {
                String name = node.getName();
                if (name.equals(conn.getDefaultSchema())) {
                    tii.setDefault(true);
                    tii.setImportant(false);
                    conn.removeImportantSchema(name);
                } else if (!conn.isImportantSchema(name)) {
                    conn.addImportantSchema(name);
                    tii.setDefault(false);
                    tii.setImportant(true);
                } else {
                    conn.removeImportantSchema(name);
                    tii.setDefault(false);
                    tii.setImportant(false);
                }
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
