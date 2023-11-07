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
package org.netbeans.modules.java.file.launcher.actions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.Transport;
import com.sun.jdi.connect.Connector;
import java.util.Iterator;
import java.util.logging.Level;

import org.netbeans.api.debugger.jpda.DebuggerStartException;

import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.java.file.launcher.SingleSourceFileUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.windows.InputOutput;

/**
 * Start the JPDA debugger.
 *
 * @author Arunava Sinha
 */
public class JPDAStart implements Runnable {

    private static final RequestProcessor RP = new RequestProcessor(JPDAStart.class);
    private static final String TRANSPORT = "dt_socket"; //NOI18N

    private final Object[] lock = new Object[2];
    private final InputOutput io;
    private final FileObject fileObject;

    JPDAStart(InputOutput inputOutput, FileObject fileObject) {
        io = inputOutput;
        this.fileObject = fileObject;
    }

    /**
     * returns the port that the debugger listens to..
     */
    public String execute() throws Exception {
        SingleSourceFileUtil.LOG.log(Level.INFO, "JPDA Listening Start"); //NOI18N
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
                SingleSourceFileUtil.LOG.log(Level.INFO, "Debug Port:{0}", lock[0]);  //NOI18N

                final Map properties = new HashMap();

                ClassPath sourcePath = ClassPathSupport.createClassPath(fileObject.getParent());
                ClassPath jdkPath = ClassPathSupport.createClassPath(System.getProperty("java.class.path"));

                properties.put("sourcepath", sourcePath); //NOI18N
                File baseDir = FileUtil.toFile(fileObject.getParent());
                properties.put("baseDir", baseDir); //NOI18N
                properties.put("name", fileObject.getName()); //NOI18N

                final ListeningConnector flc = lc;
                RP.post(() -> {
                    try {
                        JPDADebugger.startListening(flc, args,
                                new Object[]{properties});
                    } catch (DebuggerStartException ex) {
                        io.getErr().println("Debugger Start Error."); //NOI18N
                        SingleSourceFileUtil.LOG.log(Level.SEVERE, "Debugger Start Error.", ex);
                    }
                });
            } catch (java.io.IOException ioex) {
                io.getErr().println("IO Error:"); //NOI18N
//                org.openide.ErrorManager.getDefault().notify(ioex);
                lock[1] = ioex;
            } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException icaex) {
                io.getErr().println("Illegal Connector"); //NOI18N
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
