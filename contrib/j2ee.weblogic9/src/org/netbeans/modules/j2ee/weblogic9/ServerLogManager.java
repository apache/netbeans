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
