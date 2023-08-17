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
package org.netbeans.modules.gradle.java.execute;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.Transport;
import com.sun.jdi.connect.Connector;
import java.io.PrintWriter;
import java.util.Iterator;

import org.netbeans.api.debugger.jpda.DebuggerStartException;

import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.java.Utils;

/**
 * Start the JPDA debugger.
 *
 * @author Arunava Sinha
 */
public class JPDAStart implements Runnable {

    private static final RequestProcessor RP = new RequestProcessor(JPDAStart.class);
    private static final String TRANSPORT = "dt_socket"; //NOI18N

    private final Object[] lock = new Object[2];
    private final PrintWriter out;
    private final Project project;

    JPDAStart(PrintWriter out, Project project) {
        this.out = out;
        this.project = project;
    }

    /**
     * returns the port that the debugger listens to..
     */
    public String execute() throws Exception {
        synchronized (lock) {
            RP.post(this);
            lock.wait();
            if (lock[1] != null) {
                throw ((Exception) lock[1]); //NOI18N
            }
        }
        return (String) lock[0];
    }

    @Override
    public void run() {
        synchronized (lock) {

            try {

                ListeningConnector lc = null;
                Iterator i = Bootstrap.virtualMachineManager().
                        listeningConnectors().iterator();
                for (; i.hasNext();) {
                    lc = (ListeningConnector) i.next();
                    Transport t = lc.transport();
                    if (t != null && t.name().equals(getTransport())) {
                        break;
                    }
                }
                if (lc == null) {
                    throw new RuntimeException("No trasports named " + getTransport() + " found!"); //NOI18N
                }

                final Map args = lc.defaultArguments();
                String address = lc.startListening(args);
                try {
                    int port = Integer.parseInt(address.substring(address.indexOf(':') + 1));
                    Connector.IntegerArgument portArg = (Connector.IntegerArgument) args.get("port"); //NOI18N
                    portArg.setValue(port);
                    lock[0] = Integer.toString(port);
                } catch (NumberFormatException e) {
                    lock[0] = address;
                }

                final Map properties = new HashMap();

                ClassPath sourcePath = Utils.getSources(project);
                ClassPath jdkSourcePath = Utils.getJdkSources(project);

                properties.put("sourcepath", sourcePath); //NOI18N
                properties.put("jdksources", jdkSourcePath); //NOI18N
                File baseDir = FileUtil.toFile(project.getProjectDirectory()); 
                properties.put("baseDir", baseDir); //NOI18N
                properties.put("name", ProjectUtils.getInformation(project).getDisplayName()); //NOI18N

                final ListeningConnector flc = lc;
                RP.post(() -> {
                    try {
                        JPDADebugger.startListening(flc, args,
                                new Object[]{properties});
                    } catch (DebuggerStartException ex) {
                        out.println("Debugger Start Error."); //NOI18N
                    }
                });
            } catch (java.io.IOException ioex) {
                out.println("IO Error:"); //NOI18N
//                org.openide.ErrorManager.getDefault().notify(ioex);
                lock[1] = ioex;
            } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException icaex) {
                out.println("Illegal Connector"); //NOI18N
                lock[1] = icaex;
            } finally {
                lock.notify();
            }
        }
    }

    public String getTransport() {
        return TRANSPORT;
    }
}
