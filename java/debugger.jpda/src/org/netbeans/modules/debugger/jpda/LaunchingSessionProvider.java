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

package org.netbeans.modules.debugger.jpda;

import java.util.HashSet;
import java.util.Map;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LaunchingDICookie;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
@SessionProvider.Registration(path="netbeans-jpda-LaunchingDICookie")
public class LaunchingSessionProvider extends SessionProvider {

    private ContextProvider         contextProvider;
    private LaunchingDICookie       launchingCookie;
    
    public LaunchingSessionProvider (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        launchingCookie = contextProvider.lookupFirst(null, LaunchingDICookie.class);
    };
    
    public String getSessionName () {
        Map arguments = contextProvider.lookupFirst(null, Map.class);
        if (arguments != null) {
            String processName = (String) arguments.get ("name");
            if (processName != null)
                return findUnique (processName);
        }
        String sessionName = launchingCookie.getClassName ();
        int i = sessionName.lastIndexOf ('.');
        if (i >= 0) 
            sessionName = sessionName.substring (i + 1);
        return findUnique (sessionName);
    };
    
    public String getLocationName () {
        return NbBundle.getMessage 
            (LaunchingSessionProvider.class, "CTL_Localhost");
    }
    
    public String getTypeID () {
        return JPDADebugger.SESSION_ID;
    }
    
    public Object[] getServices () {
        return new Object [0];
    }
    
    static String findUnique (String sessionName) {
        DebuggerManager cd = DebuggerManager.getDebuggerManager ();
        Session[] ds = cd.getSessions ();
        
        // 1) finds all already used indexes and puts them to HashSet
        int i, k = ds.length;
        HashSet<Integer> m = new HashSet<Integer>();
        for (i = 0; i < k; i++) {
            String pn = ds [i].getName ();
            if (!pn.startsWith (sessionName)) continue;
            if (pn.equals (sessionName)) {
                m.add (Integer.valueOf(0));
                continue;
            }

            try {
                int t = Integer.parseInt (pn.substring (sessionName.length () + 3)); // 3 = " - ".length
                m.add (Integer.valueOf(t));
            } catch (Exception e) {
            }
        }
        
        // 2) finds first unused index in m
        k = m.size ();
        for (i = 0; i < k; i++)
           if (!m.contains (Integer.valueOf(i)))
               break;
        if (i > 0) sessionName = sessionName + " - " + i; //NOI18N
        return sessionName;
    };
}

