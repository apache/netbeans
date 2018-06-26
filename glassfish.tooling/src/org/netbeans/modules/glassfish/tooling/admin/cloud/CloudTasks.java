/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.glassfish.tooling.admin.cloud;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.admin.Command;
import org.netbeans.modules.glassfish.tooling.admin.ResultString;
import org.netbeans.modules.glassfish.tooling.admin.ServerAdmin;
import org.netbeans.modules.glassfish.tooling.TaskStateListener;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;

/**
 * This class provides convenience methods for working with cloud (CPAS server).
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CloudTasks {

    /**
     * Deploy task that deploys application on server.
     *
     * @param server - server to deploy on
     * @param account - which account the application is deployed under
     * @param application - File object representing archive or directory where
     * the application is
     * @param listener - listener, that listens to command execution events
     * @return result object with task status and message
     */
    public static ResultString deploy(final GlassFishServer server,
            final String account, final File application,
            final TaskStateListener listener) {
        Command command = new CommandCloudDeploy(account, application);
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command, listener);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new GlassFishIdeException(
                    "Instance or cluster stop failed.", e);
        }
    }
}
