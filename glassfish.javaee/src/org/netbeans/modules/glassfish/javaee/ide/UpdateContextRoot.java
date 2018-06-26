/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.javaee.ide;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.TaskEvent;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.admin.CommandGetProperty;
import org.netbeans.modules.glassfish.tooling.admin.ResultMap;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
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
                            GlassFishServer server
                                    = si.getBasicNode().getLookup()
                                    .lookup(GlassfishModule.class).getInstance();
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
                        } catch (GlassFishIdeException gfie) {
                            Logger.getLogger("glassfish-javaee").log(Level.INFO,
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
