/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
