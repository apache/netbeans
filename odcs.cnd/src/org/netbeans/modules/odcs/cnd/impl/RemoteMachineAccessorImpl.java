/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.cnd.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.cnd.http.HttpClientAdapter;
import org.netbeans.modules.odcs.cnd.http.HttpClientAdapterFactory;
import org.netbeans.modules.odcs.cnd.json.VMList;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.server.ui.spi.RemoteMachineAccessor;
import org.netbeans.modules.team.server.ui.spi.RemoteMachineHandle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = RemoteMachineAccessor.class)
public class RemoteMachineAccessorImpl extends RemoteMachineAccessor<ODCSProject> {

    private static final String REST_URL_GET_VMS = "/api/cc/vms"; // NOI18N

    @Override
    public Class<ODCSProject> type() {
        return ODCSProject.class;
    }

    @Override
    public boolean hasRemoteMachines(ProjectHandle<ODCSProject> project) {
        // XXX should check if there are any for the given project
        return true;
    }

    @Override
    public List<RemoteMachineHandle> getRemoteMachines(ProjectHandle<ODCSProject> project) {
        if (!hasRemoteMachines(project)) {
            return null;
        }

        ODCSServer server = project.getTeamProject().getServer();

        // TODO clear
//        server.addPropertyChangeListener(ODCSServer.PROP_LOGIN, (PropertyChangeEvent evt) -> {
//            if (evt.getNewValue() == null) {
//                clients.remove(evt.)
//            }
//        });
        HttpClientAdapter client = HttpClientAdapterFactory.create(server.getUrl().toExternalForm(), server.getPasswordAuthentication());

        VMList vms = client.getForObject(server.getUrl() + REST_URL_GET_VMS, VMList.class);

        List<RemoteMachineHandle> result = vms.getMapList()
                .stream()
                .map(desc -> new RemoteMachineHandleImpl(server.getUrl(), desc))
                .collect(Collectors.toList());

        return result;
    }
}
