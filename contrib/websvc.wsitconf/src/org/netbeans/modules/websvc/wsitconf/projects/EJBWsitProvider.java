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

package org.netbeans.modules.websvc.wsitconf.projects;

import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.websvc.wsitconf.spi.WsitProvider;
import org.netbeans.modules.websvc.wsitconf.util.ServerUtils;
import org.netbeans.modules.websvc.wsstack.api.WSStack;

/**
 *
 * @author Martin Grebac
 */
public class EJBWsitProvider extends WsitProvider {

    private static final Logger logger = Logger.getLogger(EJBWsitProvider.class.getName());

    public EJBWsitProvider(Project p) {
        this.project = p;
    }

    @Override
    public boolean isJsr109Project() {
        J2eePlatform j2eePlatform = ServerUtils.getJ2eePlatform(project);
        if (j2eePlatform != null){
            Collection<WSStack> wsStacks = (Collection<WSStack>)
                    j2eePlatform.getLookup().lookupAll(WSStack.class);
            for (WSStack stack : wsStacks) {
                if (stack.isFeatureSupported(JaxWs.Feature.JSR109)) {
                    return true;
                }
            }
        }
        return false;
    }

}
