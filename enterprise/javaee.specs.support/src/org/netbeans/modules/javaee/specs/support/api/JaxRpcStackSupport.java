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

package org.netbeans.modules.javaee.specs.support.api;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.javaee.specs.support.bridge.IdeJaxRpcStack;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;

/**
 *
 * @author mkuchtiak
 * @author ads
 */
public class JaxRpcStackSupport {
    
    public static WSStack<JaxRpc> getJaxWsStack(J2eePlatform j2eePlatform) {
        return WSStack.findWSStack(j2eePlatform.getLookup(), JaxRpc.class);
    }
    
    public static WSTool getJaxWsStackTool(J2eePlatform j2eePlatform, 
            JaxRpc.Tool toolId) 
    {
        WSStack<JaxRpc> wsStack = WSStack.findWSStack(j2eePlatform.getLookup(), 
                JaxRpc.class);
        if (wsStack != null) {
            return wsStack.getWSTool(toolId);
        } else {
            return null;
        }
    }

    public static WSStack<JaxRpc> getIdeJaxWsStack() {
        return RpcAccessor.IDE_STACK;
    }
    
    private static class RpcAccessor {
        private static final WSStack<JaxRpc> IDE_STACK = WSStackFactory.
            createWSStack(JaxRpc.class, new IdeJaxRpcStack(new JaxRpc()), 
                    WSStack.Source.IDE);
    }

}
