/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.base.input.InputReaderTask;
import org.netbeans.api.extexecution.print.LineProcessors;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.optional.ErrorLineConvertor;
import org.netbeans.modules.j2ee.weblogic9.optional.NonProxyHostsHelper;
import org.netbeans.modules.weblogic.common.api.WebLogicRuntime;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Petr Hejl
 */
public class ServerLogManager {

    private static final Logger LOGGER = Logger.getLogger(ServerLogManager.class.getName());

    private final WLDeploymentManager dm;

    private InputReaderTask task;

    public ServerLogManager(WLDeploymentManager dm) {
        this.dm = dm;
    }

    public synchronized void openLog() {
        InputOutput io = UISupport.getServerIO(dm.getUri());
        if (io != null) {
            io.select();
        }
        if (task == null) {
            WebLogicRuntime runtime = WebLogicRuntime.getInstance(dm.getCommonConfiguration());
            final OutputWriter writer = io.getOut();
            if (dm.isRemote() || !runtime.isProcessRunning()) {
                try {
                    writer.reset();
                } catch (IOException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
            }
            if (!runtime.isProcessRunning()) {
                task = runtime.createLogReaderTask(LineProcessors.printing(writer, new ErrorLineConvertor(), true), new Callable<String>() {

                    @Override
                    public String call() throws Exception {
                        return NonProxyHostsHelper.getNonProxyHosts();
                    }
                });
                // FIXME processor
                RequestProcessor.getDefault().post(task);
            }
        }
    }

    public synchronized void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
