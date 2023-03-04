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
package org.netbeans.core.network.proxy.gnome;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.network.proxy.NetworkProxyResolver;
import org.netbeans.core.network.proxy.NetworkProxySettings;

/**
 *
 * @author lfischme
 */
public class GnomeNetworkProxy implements NetworkProxyResolver{
    
    private static final Logger LOGGER = Logger.getLogger(GnomeNetworkProxy.class.getName());
        
    @Override
    public NetworkProxySettings getNetworkProxySettings() {
        if (new File(GsettingsNetworkProxy.GSETTINGS_PATH).exists() && GsettingsNetworkProxy.isGsettingsValid()) {
            return GsettingsNetworkProxy.getNetworkProxySettings();
        } else if (new File(GconfNetworkProxy.GCONF_PATH).exists() && GconfNetworkProxy.isGconfValid()) {
            return GconfNetworkProxy.getNetworkProxySettings();
        } else {
            return new NetworkProxySettings(false);
        }
    }
    
    /**
     * 
     * 
     * @param command
     * @return 
     */
    protected static BufferedReader executeCommand(String command) {
        BufferedReader reader = null;
        
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Cannot execute command: " + command, ioe);
        } catch (InterruptedException ie) {
            LOGGER.log(Level.SEVERE, "Cannot execute command: " + command, ie);
        }
        
        return reader;
    }        
}
