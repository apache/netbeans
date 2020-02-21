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

package org.netbeans.modules.cnd.modelimpl.debug;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ParserQueue;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.openide.LifecycleManager;
import org.openide.util.RequestProcessor;


/**
 * For testing purposes, allow safe closing IDE at the time of parsing finish.
 *
 */
public class Terminator implements Runnable {
    
    private static final RequestProcessor RP = new RequestProcessor("CND.Terminator", 1); // NOI18N
    
    private final ProjectBase project;
    private static boolean timeout = false;
    
    private Terminator(ProjectBase project) {
        super();
        this.project = project;
    }
    
    private static final class Lock {}
    private static final Object lock = new Lock();
    private static int inParse = 0;
    
    public static void create(ProjectBase project) {
        RP.post(new Terminator(project));
    }
    
    @Override
    public void run() {
        synchronized (lock) {
            inParse++;
        }
        System.err.println("Parse started. " + inParse + " projects in list"); // NOI18N
        if (TraceFlags.CLOSE_TIMEOUT > 0) {
            ActionListener terminator2 = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((ModelImpl)CsmModelAccessor.getModel()).shutdown();
                    synchronized (lock) {
                        timeout = true;
                    }
                }
            };
            new javax.swing.Timer(TraceFlags.CLOSE_TIMEOUT*1000, terminator2).start();
        }
        project.waitParse();
        synchronized (lock) {
            inParse--;
            System.err.println("Parse finished. " + inParse + " projects left"); // NOI18N
            if (inParse == 0) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        terminate();
                    }
                });
            }
        }
    }
    
    private void terminate() {
        synchronized (lock) {
            long ptime = ParserQueue.instance().getStopWatchTime();
            System.err.println("disposing at " + ptime); // NOI18N
            String xmlOutput = System.getProperty("cnd.close.report.xml"); // NOI18N
            if (xmlOutput != null) {
                BufferedWriter out;
                try {
                    out = new BufferedWriter(new FileWriter(xmlOutput, true));
                    String result = timeout ? "failed" : "passed"; // NOI18N
                    out.write("<result>" + result + "</result>"); // NOI18N
                    out.write("<parsetime>" + ptime + "</parsetime>"); // NOI18N
                    out.close();
                } catch (IOException ex) {
                    DiagnosticExceptoins.register(ex);
                }
            }
        }
        LifecycleManager.getDefault().exit();
    }
}
