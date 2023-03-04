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

import java.util.Map;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Jancura
 */
@SessionProvider.Registration(path="netbeans-jpda-ListeningDICookie")
public class ListeningSessionProvider extends SessionProvider {

    private ContextProvider contextProvider;
    private ListeningDICookie smadic;

    public ListeningSessionProvider (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        smadic = contextProvider.lookupFirst(null, ListeningDICookie.class);
    };
    
    public String getSessionName () {
        Map arguments = contextProvider.lookupFirst(null, Map.class);
        if (arguments != null) {
            String processName = (String) arguments.get ("name");
            if (processName != null)
                return LaunchingSessionProvider.findUnique (processName);
        }
        if (smadic.getSharedMemoryName () != null)
            return NbBundle.getMessage 
                (ListeningSessionProvider.class, "CTL_Listening") + 
                ":" + smadic.getSharedMemoryName ();
        return NbBundle.getMessage 
            (ListeningSessionProvider.class, "CTL_Listening") + 
            ":" + smadic.getPortNumber ();
    }
    
    public String getLocationName () {
        return NbBundle.getMessage 
            (ListeningSessionProvider.class, "CTL_Localhost");
    }
    
    public String getTypeID () {
        return JPDADebugger.SESSION_ID;
    }
    
    public Object[] getServices () {
        return new Object [0];
    }
}

