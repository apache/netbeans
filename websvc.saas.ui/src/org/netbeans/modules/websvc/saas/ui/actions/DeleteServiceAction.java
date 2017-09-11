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
package org.netbeans.modules.websvc.saas.ui.actions;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 * Deletes a user-defined web service.
 * 
 * @author  nam
 */
public class DeleteServiceAction extends NodeAction {

    @Override
    protected boolean enable(Node[] nodes) {
        for (Node n : nodes) {
            Saas saas = n.getLookup().lookup(Saas.class);
            if (saas == null || !saas.isUserDefined()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected String iconResource() {
        return "org/netbeans/modules/websvc/saas/ui/resources/ActionIcon.gif"; // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(DeleteServiceAction.class, "DELETE"); // NOI18N
    }

    @Override
    protected void performAction(Node[] nodes) {
        final List<Saas> saases = new ArrayList<Saas>();
        for (Node n : nodes) {
            Saas saas = n.getLookup().lookup(Saas.class);
            saases.add(saas);
        }

        String msg = NbBundle.getMessage(this.getClass(), "WS_DELETE", saases); // NOI18N
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
        Object response = DialogDisplayer.getDefault().notify(d);
        if (NotifyDescriptor.YES_OPTION.equals(response)) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    for (Saas saas : saases) {
                        SaasServicesModel.getInstance().removeService(saas);
                    }
                }
            });
        }
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
