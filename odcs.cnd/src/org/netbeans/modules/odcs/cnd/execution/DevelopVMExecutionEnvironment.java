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

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
public abstract class DevelopVMExecutionEnvironment implements ExecutionEnvironment {

    public static final String CLOUD_PREFIX = "cloud.oracle";

    public abstract String getServerUrl();

    public abstract String getMachineId();

    public static String encode(String user, String machineId, int port, String serverUrl) {
        return String.format("%s://%s@%s:%d@%s", CLOUD_PREFIX, user, machineId, port, serverUrl);
    }

    public static DevelopVMExecutionEnvironment decode(String hostKey) {
        String userAtmachineAtHost = hostKey.substring((CLOUD_PREFIX + "://").length());

        String[] split = userAtmachineAtHost.split("@", 3);

        String user = split[0];
        String machineAndPort = split[1];
        String host = split[2];

        String machineId = machineAndPort.split(":")[0];
        int port = Integer.valueOf(machineAndPort.split(":")[1]);

        return new DevelopVMExecutionEnvironmentImpl(user, machineId, port, host);
    }

    public abstract void initializeOrWait();
}
