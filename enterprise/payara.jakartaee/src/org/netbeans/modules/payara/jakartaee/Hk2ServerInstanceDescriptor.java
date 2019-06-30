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

package org.netbeans.modules.payara.jakartaee;

import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor;

/**
 *
 * @author Peter Williams
 */
public class Hk2ServerInstanceDescriptor implements ServerInstanceDescriptor {

    private final PayaraModule commonSupport;

    public Hk2ServerInstanceDescriptor(Hk2DeploymentManager dm) {
        commonSupport = dm.getCommonServerSupport();
    }

    public int getHttpPort() {
        // By now, should be guaranteed to parse w/o format exception.
        // XXX Another reason to expose more specific property methods inside
        // CommonServerSupport impl class.
        return Integer.parseInt(commonSupport.getInstanceProperties().get(PayaraModule.HTTPPORT_ATTR));
    }

    public String getHostname() {
        return commonSupport.getInstanceProperties().get(PayaraModule.HOSTNAME_ATTR);
    }

    public boolean isLocal() {
        return !commonSupport.isRemote();
    }

}
