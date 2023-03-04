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

package org.netbeans.lib.profiler.server;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * Class that contains the premain() method, needed by the java.lang.instrument Java agent
 * mechanism, that we use for "attach on startup" operation with JDK 1.5.
 *
 * @author Tomas Hurka
 * @author  Misha Dmitriev
 */
public class ProfilerActivate15 {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static void agentmain(final String agentArgs, final Instrumentation inst) {
        activate(agentArgs, inst, ProfilerServer.ATTACH_DYNAMIC);
    }

    /**
     * This method is called after the VM has been initialized, but before the TA's main() method.
     * A single arguments string passed to it is the "options" string specified to the -javaagent
     * argument, as java -javaagent:jarpath=options. It should contain the communication port number
     * and optional timeout separated by a comma.
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        activate(agentArgs, inst, ProfilerServer.ATTACH_DIRECT);
    }

    private static File getArchiveFile(URL url) {
        String protocol = url.getProtocol();

        if ("jar".equals(protocol)) { //NOI18N            

            String path = url.getPath();
            int index = path.indexOf("!/"); //NOI18N

            if (index >= 0) {
                try {
                    return new File(new URI(path.substring(0, index)));
                } catch (URISyntaxException ex) {
                    throw new IllegalArgumentException(url.toString());
                }
            }
        }

        throw new IllegalArgumentException(url.toString());
    }

    private static void activate(String agentArgs, Instrumentation inst, int activateCode) {
        URL classUrl = getSelfClassUrl();
        File jar = getArchiveFile(classUrl);
        String fullJFluidPath = jar.getParent();

        if ((agentArgs == null) || (agentArgs.length() == 0)) { // no options, just load the native library. This is used for remote-pack calibration
            ProfilerServer.loadNativeLibrary(fullJFluidPath, false);

            return;
        }

        int timeOut = 0;
        int commaPos = agentArgs.indexOf(',');

        if (commaPos != -1) { // optional timeout is specified

            String timeOutStr = agentArgs.substring(commaPos + 1);

            try {
                timeOut = Integer.parseInt(timeOutStr);
            } catch (NumberFormatException ex) {
                System.err.println("*** Profiler Engine: invalid timeout number specified to premain(): " + timeOutStr); // NOI18N
                System.exit(-1);
            }

            agentArgs = agentArgs.substring(0, commaPos);
        }

        String portStr = agentArgs;
        int portNo = 0;

        try {
            portNo = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            System.err.println("*** Profiler Engine: invalid port number specified to premain(): " + portStr); // NOI18N
            System.exit(-1);
        }
        
        ProfilerServer.loadNativeLibrary(fullJFluidPath, false);
        ProfilerServer.activate(fullJFluidPath, portNo, activateCode, timeOut);
    }

    private static URL getSelfClassUrl() {
        String SELF_CLASS_NAME = "org/netbeans/lib/profiler/server/ProfilerActivate15.class"; // NOI18N
        
        URL classUrl = ClassLoader.getSystemClassLoader().getResource(SELF_CLASS_NAME);
        if (classUrl == null) {
            classUrl = Thread.currentThread().getContextClassLoader().getResource(SELF_CLASS_NAME);
        }
        return classUrl;
    }
}
