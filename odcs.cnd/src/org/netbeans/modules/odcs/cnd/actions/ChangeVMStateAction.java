/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.odcs.cnd.actions;

import org.netbeans.modules.odcs.cnd.json.misc.State;
import org.netbeans.modules.odcs.cnd.json.misc.Response;
import java.awt.event.ActionEvent;
import org.netbeans.modules.odcs.cnd.http.HttpClientAdapter;
import org.openide.util.NbBundle;

/**
 *
 */
public class ChangeVMStateAction extends RestAction {

    private static final String URL_TEMPLATE = "api/cc/vms/{0}/state"; // NOI18N
    private final String machineId;
    private final State state;
    private final String actionName;

    public ChangeVMStateAction(String serverUrl, String machineId, String state, String actionName) {
        super(serverUrl, actionName);
        this.machineId = machineId;
        this.state = new State(state);
        this.actionName = actionName;
    }

    @Override
    public void actionPerformedImpl(HttpClientAdapter client, ActionEvent e) {
        Response response = client.postForObject(getRestUrl(), Response.class, state, "REST - Change VM state");

        System.out.println(response.isSuccess());
    }

    @Override
    public String getRestUrl() {
        return String.join("/", getServerUrl(), formatUrl(URL_TEMPLATE, machineId));
    }

    @NbBundle.Messages({
        "remotevm.startvm.action.text=Start VM"
    })
    public static ChangeVMStateAction startedAction(String serverUrl, String machineId) {
        return new ChangeVMStateAction(serverUrl, machineId, "STARTED", Bundle.remotevm_startvm_action_text()); // NOI18N
    }

    @NbBundle.Messages({
        "remotevm.stopvm.action.text=Stop VM"
    })
    public static ChangeVMStateAction stoppedAction(String serverUrl, String machineId) {
        return new ChangeVMStateAction(serverUrl, machineId, "STOPPED", Bundle.remotevm_stopvm_action_text()); // NOI18N
    }
}
