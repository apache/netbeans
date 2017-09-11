/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            mo.getTextDetails().get(0).showDetail(
                                    TextDetail.DH_GOTO);
                        }
                    });
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
