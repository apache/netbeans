/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.javaee.ide;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.glassfish.spi.VMIntrospector;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author vkraemer
 */
@ServiceProvider(service=VMIntrospector.class,path="Servers/GlassFish") // NOI18N
public class VMIntrospectorImpl implements VMIntrospector {
    
    public VMIntrospectorImpl() {
        Logger.getLogger("glassfish-javaee").log(Level.FINE, "VMIntrospector created"); // NOI18N
    }

    
        /**
     * Returns true if this server is started in debug mode AND debugger is attached to it
     * AND threads are suspended (e.g. debugger stopped on breakpoint)
     */
    @Override
    public boolean isSuspended(String host, String port) {
        Logger.getLogger("glassfish-javaee").log(Level.FINE, "VMIntrospector called {0}, {1}", new Object[] { host, port }); // NOI18N
        boolean retVal = false;
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        
        for (int i=0; ! retVal && i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null,AttachingDICookie.class);
                if (o != null) {
                    Object d = s.lookupFirst(null,JPDADebugger.class);
                    if (d != null) {
                        JPDADebugger jpda = (JPDADebugger)d;
                        if (jpda.getState() == JPDADebugger.STATE_STOPPED) { // the session is suspended.
                            AttachingDICookie attCookie = (AttachingDICookie)o;
                            String shmName = attCookie.getSharedMemoryName();
                            if (shmName!=null) {
                                if (shmName.startsWith(port)) {
                                    retVal = true;
                                }
                            } else {//test the machine name and port number
                                int attachedPort = attCookie.getPortNumber();
                                    if (sameMachine(attCookie.getHostName(), host) &&
                                            Integer.parseInt(port) == attachedPort) {
                                        retVal = true;
                                    }                                
                            }
                        }
                    }
                }
            }
        }
        return retVal;
    }

    private static  final  String LOCALHOST="localhost";//NOI18N
    private static  final  String LOCALADDRESS="127.0.0.1";//NOI18N
    

    
    boolean sameMachine(String host1, String host2){
        try {
            if (host1.equals(host2)){
                return true;
            }
            if (host1.equals(LOCALHOST)){
                if (host2.equals(LOCALADDRESS)){
                    return true;
                }
                String localCanonicalHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
                String h2 = java.net.InetAddress.getByName(host2).getCanonicalHostName();
                if (localCanonicalHostName.equals(h2)){
                    return true;
                }
            }
            if (host1.equals(LOCALADDRESS)){
                if (host2.equals(LOCALHOST)){
                    return true;
                }
                return true;
            }
            if (host2.equals(LOCALHOST)){
                if (host1.equals(LOCALADDRESS)){
                    return true;
                }
                String localCanonicalHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
                String h1 = java.net.InetAddress.getByName(host1).getCanonicalHostName();
                if (localCanonicalHostName.equals(h1)){
                    return true;
                }
            }
            if (host2.equals(LOCALADDRESS)){
                if (host1.equals(LOCALHOST)){
                    return true;
                }
                String localCanonicalHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
                String h1 = java.net.InetAddress.getByName(host1).getCanonicalHostName();
                if (localCanonicalHostName.equals(h1)){
                    return true;
                }
            }
            String h1 = java.net.InetAddress.getByName(host1).getCanonicalHostName();
            String h2 = java.net.InetAddress.getByName(host2).getCanonicalHostName();
            if (h1.equals(h2)){
                return true;
            }
        } catch (java.net.UnknownHostException ex) {
            //ex.printStackTrace();
        }
        return false;
    }
}
