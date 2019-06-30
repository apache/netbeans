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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.payara.spi.VMIntrospector;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author vkraemer
 */
@ServiceProvider(service=VMIntrospector.class,path="Servers/Payara") // NOI18N
public class VMIntrospectorImpl implements VMIntrospector {
    
    public VMIntrospectorImpl() {
        Logger.getLogger("payara-jakartaee").log(Level.FINE, "VMIntrospector created"); // NOI18N
    }

    
        /**
     * Returns true if this server is started in debug mode AND debugger is attached to it
     * AND threads are suspended (e.g. debugger stopped on breakpoint)
     */
    @Override
    public boolean isSuspended(String host, String port) {
        Logger.getLogger("payara-jakartaee").log(Level.FINE, "VMIntrospector called {0}, {1}", new Object[] { host, port }); // NOI18N
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
