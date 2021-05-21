/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.payara.jakartaee.ide;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.admin.CommandGetProperty;
import org.netbeans.modules.payara.tooling.admin.ResultMap;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.openide.util.RequestProcessor;

/**
 *
 * @author vkraemer
 */
public class UpdateContextRoot implements ProgressListener {

    private MonitorProgressObject returnProgress;
    private Hk2TargetModuleID moduleId;
    private ServerInstance si;
    private boolean needToDo;

    private static final RequestProcessor RP = new RequestProcessor("UpdateContextRoot",5); // NOI18N

    public UpdateContextRoot(MonitorProgressObject returnProgress, Hk2TargetModuleID moduleId,
            ServerInstance si, boolean needToDo) {
        this.returnProgress = returnProgress;
        this.moduleId = moduleId;
        this.si = si;
        this.needToDo = needToDo;
    }

    @Override
    public void handleProgressEvent(ProgressEvent event) {
        if (event.getDeploymentStatus().isCompleted()) {
            if (needToDo) {
                returnProgress.operationStateChanged(TaskState.RUNNING,
                        TaskEvent.CMD_RUNNING,
                        event.getDeploymentStatus().getMessage());
                // let's update the context-root
                //
                RP.post(new Runnable() {

                    @Override
                    public void run() {
                        // Maven projects like to embed a '.' into the ModuleID
                        // that played havoc with the get command, so we started
                        // to use a different get pattern,
                        try {
                            PayaraServer server
                                    = si.getBasicNode().getLookup()
                                    .lookup(PayaraModule.class).getInstance();
                            ResultMap<String, String> result
                                    = CommandGetProperty.getProperties( server,
                                    "applications.application.*.context-root");
                            if (result.getState() == TaskState.COMPLETED) {
                                Map<String, String> retVal = result.getValue();
                                String newCR = retVal.get(
                                        "applications.application."
                                        + moduleId.getModuleID()
                                        + ".context-root");
                                if (null != newCR) {
                                    moduleId.setPath(newCR); //e.getValue());
                                    returnProgress.operationStateChanged(
                                            TaskState.COMPLETED,
                                            TaskEvent.CMD_COMPLETED,
                                            "updated the moduleid");
                                } else {
                                    returnProgress.operationStateChanged(
                                            TaskState.COMPLETED,
                                            TaskEvent.CMD_COMPLETED,
                                            "no moduleid update necessary");
                                }
                            } else {
                                // there are no context-root values to be had...
                                // the query failed... but the update has been successful
                                returnProgress.operationStateChanged(
                                        TaskState.COMPLETED,
                                        TaskEvent.CMD_COMPLETED,
                                        "no moduleid update necessary");
                            }
                        } catch (PayaraIdeException gfie) {
                            Logger.getLogger("payara-jakartaee").log(Level.INFO,
                                    "Could not retrieve property from server"
                                    + " when updating module id.", gfie);
                        }

                    }
                });
            } else {
                returnProgress.operationStateChanged(TaskState.COMPLETED,
                        TaskEvent.CMD_COMPLETED, event.getDeploymentStatus().getMessage());
            }
        }else if (event.getDeploymentStatus().isFailed()) {
            returnProgress.operationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED, event.getDeploymentStatus().getMessage());
        } else {
            returnProgress.operationStateChanged(TaskState.RUNNING,
                    TaskEvent.CMD_RUNNING, event.getDeploymentStatus().getMessage());
        }
    }
}
