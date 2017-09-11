/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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

import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Rob
 */
public class RefreshAction extends BaseAction {
    private static final RequestProcessor RP = new RequestProcessor(RefreshAction.class);
    @Override
    public String getName() {
        return NbBundle.getMessage (RefreshAction.class, "Refresh"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RefreshAction.class);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;

        if (activatedNodes.length == 1) {
            enabled = null != activatedNodes[0].getLookup().lookup(BaseNode.class);
        }

        return enabled;
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        if (activatedNodes[0] == null) {
            return;
        }
        final BaseNode baseNode = activatedNodes[0].getLookup().lookup(BaseNode.class);
        RP.post(
            new Runnable() {
                @Override
                public void run() {
                    MetadataModel model = baseNode.getLookup().lookup(DatabaseConnection.class).getMetadataModel();
                    if (model != null) {
                        try {
                            model.runReadAction(
                                new Action<Metadata>() {
                                    @Override
                                    public void run(Metadata metaData) {
                                        metaData.refresh();
                                    }
                                }
                            );
                        } catch (MetadataModelException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }

                    baseNode.refresh();
                }
            }
        );
    }

}
