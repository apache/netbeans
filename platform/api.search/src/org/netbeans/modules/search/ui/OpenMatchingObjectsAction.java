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
package org.netbeans.modules.search.ui;

import java.awt.EventQueue;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.TextDetail;
import org.openide.cookies.EditCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 * Action that opens currently selected matching objects in editor.
 *
 * @author jhavlin
 */
public class OpenMatchingObjectsAction extends NodeAction {

    @Override
    public String getName() {
        return UiUtils.getText("LBL_EditAction");                       //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {

        for (Node n : activatedNodes) {
            final MatchingObject mo = n.getLookup().lookup(
                    MatchingObject.class);
            if (mo != null) {
                if (mo.getTextDetails() != null
                        && !mo.getTextDetails().isEmpty()) { // #219428
                    EventQueue.invokeLater(() -> mo.getTextDetails().get(0).showDetail(TextDetail.DH_GOTO));
                } else {
                    DataObject dob = mo.getDataObject();
                    if (dob != null) {
                        EditCookie editCookie = dob.getLookup().lookup(
                                EditCookie.class);
                        if (editCookie != null) {
                            editCookie.edit();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes != null && activatedNodes.length > 0;
    }
}
