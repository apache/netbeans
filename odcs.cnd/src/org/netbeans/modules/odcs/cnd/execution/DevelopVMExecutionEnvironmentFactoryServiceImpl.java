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
package org.netbeans.modules.odcs.cnd.execution;

import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.spi.ExecutionEnvironmentFactoryService;
import org.netbeans.modules.odcs.api.ODCSManager;
import org.netbeans.modules.odcs.api.ODCSServer;
import static org.netbeans.modules.odcs.cnd.execution.DevelopVMExecutionEnvironment.CLOUD_PREFIX;
import org.netbeans.modules.odcs.cnd.impl.ODCSAuthManager;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ExecutionEnvironmentFactoryService.class, position = 50)
public class DevelopVMExecutionEnvironmentFactoryServiceImpl implements ExecutionEnvironmentFactoryService {

    private static final RequestProcessor RP = new RequestProcessor("Fetching a cloud execution environment", 3);

    private static final Map<String, ExecutionEnvironment> CACHE = new HashMap<>();

    @Override
    public ExecutionEnvironment getLocal() {
        return null;
    }

    @Override
    public ExecutionEnvironment createNew(String uri) {
        return fromUniqueID(uri);
    }

    @Override
    public ExecutionEnvironment createNew(String user, String host) {
        return null;
    }

    @Override
    public ExecutionEnvironment createNew(String user, String host, int port) {
        return null;
    }

    @Override
    public String toUniqueID(ExecutionEnvironment executionEnvironment) {
        if (executionEnvironment instanceof DevelopVMExecutionEnvironment) {
            DevelopVMExecutionEnvironment env = (DevelopVMExecutionEnvironment) executionEnvironment;
            return DevelopVMExecutionEnvironment.encode(env.getUser(), env.getMachineId(), env.getSSHPort(), env.getServerUrl());
        }
        return null;
    }

    @Override
    public ExecutionEnvironment fromUniqueID(String hostKey) {
        if (!hostKey.startsWith(CLOUD_PREFIX)) {
            return null;
        }
        return CACHE.computeIfAbsent(hostKey, (key) -> {
            DevelopVMExecutionEnvironment env = DevelopVMExecutionEnvironment.decode(key);

            ODCSServer server = ODCSManager.getDefault().getServer(env.getServerUrl());

            boolean loggedInNow = ODCSAuthManager.getInstance().onLogin(env.getServerUrl(), (PasswordAuthentication pa) -> {
                RP.post(env::initializeOrWait);
            });

            if (!loggedInNow) {
                ImageIcon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/cnd/resources/odcs.png", true);
                NotificationDisplayer.getDefault().notify(Bundle.connection_title(), icon, Bundle.connection_text(env.getDisplayName(), env.getServerUrl()), null);
            }

            return env;
        });
    }
}
