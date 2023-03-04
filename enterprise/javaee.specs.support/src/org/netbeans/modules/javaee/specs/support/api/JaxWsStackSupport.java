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

import java.util.regex.Pattern;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.javaee.specs.support.bridge.IdeJaxWsStack;
import org.netbeans.modules.javaee.specs.support.bridge.JdkJaxWsStack;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;

/**
 *
 * @author mkuchtiak
 * @author ads
 */
public class JaxWsStackSupport {
    
    public static WSStack<JaxWs> getJaxWsStack(J2eePlatform j2eePlatform) {
        return WSStack.findWSStack(j2eePlatform.getLookup(), JaxWs.class);
    }
    
    public static WSTool getJaxWsStackTool(J2eePlatform j2eePlatform, 
            JaxWs.Tool toolId) 
    {
        WSStack<JaxWs> wsStack = WSStack.findWSStack(j2eePlatform.getLookup(), 
                JaxWs.class);
        if (wsStack != null) {
            return wsStack.getWSTool(toolId);
        } else {
            return null;
        }
    }
    
    public static WSStack<JaxWs> getJdkJaxWsStack() {
        return WsAccessor.JDK_JAX_WS_STACK;
    }
    
    public static WSStack<JaxWs> getIdeJaxWsStack() {
        return WsAccessor.IDE_JAX_WS_STACK;
    }
            
    private static String getJaxWsStackVersion(String javaVersion) {
        if (javaVersion.startsWith("1.6")) { //NOI18N
            int index = javaVersion.indexOf("_"); //NOI18N
            if (index > 0) {
                String releaseVersion = parseReleaseVersion(
                        javaVersion.substring(index+1));
                Integer rv = Integer.valueOf(releaseVersion);
                if (rv >= 4) {
                    return "2.1.1"; //NOI18N
                } else {
                    return "2.0"; //NOI18N
                }
            } else {
                // return null for some strange jdk versions
                return null;
            }
        } else {
            try {
                if (javaVersion.startsWith("1.")) { // NOI18N
                    Float version = Float.valueOf(javaVersion.substring(0, 3));
                    if (version > 1.6) {
                        return "2.1.3"; //NOI18N
                    } else {
                        return null;
                    }
                } else {
                    // XXX should this be updated ?
                    return "2.1.3"; //NOI18N
                }
            } catch (NumberFormatException ex) {
                // return null for some strange jdk versions
                return null;
            }
        }
    }

    private static String parseReleaseVersion(String releaseVersion) {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<releaseVersion.length(); i++) {
            char c = releaseVersion.charAt(i);
            if (Character.isDigit(c)) {
                buf.append(c);
            } else {
                break;
            }
        }
        return buf.toString();
    }
    
    private static class WsAccessor {
        private static WSStack<JaxWs> JDK_JAX_WS_STACK;
        private static final WSStack<JaxWs> IDE_JAX_WS_STACK = WSStackFactory.
            createWSStack(JaxWs.class, new IdeJaxWsStack(), WSStack.Source.IDE);
        
        static {
            String jaxWsVersion = getJaxWsStackVersion(
                    System.getProperty("java.version")); //NOI18N
            if (jaxWsVersion != null) {
                JDK_JAX_WS_STACK = WSStackFactory.createWSStack(JaxWs.class, 
                        new JdkJaxWsStack(jaxWsVersion), WSStack.Source.JDK);
            }
        }
    }
}
