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
package org.netbeans.modules.javaee.wildfly.nodes.actions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.config.WildflyMessageDestination;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Michal Mocnak
 */
public class UndeployModuleCookieImpl implements UndeployModuleCookie {

    private static final RequestProcessor PROCESSOR = new RequestProcessor("JBoss undeploy", 1); // NOI18N

    private final String fileName;

    private final Lookup lookup;

    private final ResourceType type;

    private boolean isRunning;

    public UndeployModuleCookieImpl(String fileName, Lookup lookup) {
        this(fileName, ResourceType.EJB, lookup);
    }

    public UndeployModuleCookieImpl(String fileName, ResourceType type, Lookup lookup) {
        this.lookup = lookup;
        this.fileName = fileName;
        this.type = type;
        this.isRunning = false;
    }

    @Override
    public Task undeploy() {
        final WildflyDeploymentManager dm = (WildflyDeploymentManager) lookup.lookup(WildflyDeploymentManager.class);

        final String nameWoExt;
        if(fileName.indexOf('.') > 0) {
            nameWoExt = fileName.substring(0, fileName.lastIndexOf('.'));
        } else {
            nameWoExt = fileName;
        }
        final ProgressHandle handle = ProgressHandle.createHandle(NbBundle.getMessage(UndeployModuleCookieImpl.class,
                "LBL_UndeployProgress", nameWoExt));

        Runnable r = new Runnable() {
            public void run() {
                isRunning = true;
                try {
                    switch(type) {
                        case EJB:
                        case EAR:
                        case CAR:
                        case RAR:
                        case WAR:
                            dm.getClient().undeploy(fileName);
                            break;
                        case QUEUE:
                            dm.getClient().removeMessageDestination(new WildflyMessageDestination(fileName, MessageDestination.Type.QUEUE));
                            break;
                        case TOPIC:
                            dm.getClient().removeMessageDestination(new WildflyMessageDestination(fileName, MessageDestination.Type.TOPIC));
                            break;
                        case DATASOURCE:
                            dm.getClient().removeDatasource(fileName);
                            break;

                    }
                } catch (IOException ex) {
                    Logger.getLogger(UndeployModuleCookieImpl.class.getName()).log(Level.INFO, null, ex);
                }
                handle.finish();
                isRunning = false;
            }
        };
        handle.start();
        return PROCESSOR.post(r);
    }

    public boolean isRunning() {
        return isRunning;
    }

}
