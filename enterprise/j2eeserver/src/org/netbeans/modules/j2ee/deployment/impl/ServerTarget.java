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


package org.netbeans.modules.j2ee.deployment.impl;

import javax.enterprise.deploy.spi.status.ProgressObject;
import org.openide.nodes.Node;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.openide.util.NbBundle;

// PENDING use environment providers, not Cookies
// PENDING issue  --   Target <==> J2EEDomain relationship 1 to many, many to 1, 1 to 1, or many to many
public class ServerTarget implements Node.Cookie {

    private final ServerInstance instance;
    private final Target target;
    //PENDING: caching state, sync, display through icon and action list.

    public ServerTarget(ServerInstance instance, Target target) {
        this.instance = instance;
        this.target = target;
    }

    public ServerInstance getInstance() {
        return instance;
    }

    public String getName() {
        return target.getName();
    }

    public Target getTarget() {
        return target;
    }

    public boolean isAlsoServerInstance() {
        return instance.getStartServer().isAlsoTargetServer(target);
    }

    public boolean isRunning() {
        if (isAlsoServerInstance())
            return instance.isRunning();

        StartServer ss = instance.getStartServer();
        if (ss != null) {
            return ss.isRunning(target);
        }
        return false;
    }

    public ProgressObject start() {
        StartServer ss = instance.getStartServer();
        if (ss != null && ss.supportsStartTarget(target)) {
            ProgressObject po = ss.startTarget(target);
            if (po != null) {
                return po;
            }
        }
        String name = target == null ? "null" : target.getName(); //NOI18N
        String msg = NbBundle.getMessage(ServerTarget.class, "MSG_StartStopTargetNotSupported", name);
        throw new UnsupportedOperationException(msg);
    }

    public ProgressObject stop() {
        StartServer ss = instance.getStartServer();
        if (ss != null && ss.supportsStartTarget(target)) {
            ProgressObject po = ss.stopTarget(target);
            if (po != null) {
                return po;
            }
        }
        String name = target == null ? "null" : target.getName(); //NOI18N
        String msg = NbBundle.getMessage(ServerTarget.class, "MSG_StartStopTargetNotSupported", name);
        throw new UnsupportedOperationException(msg);
    }
}
