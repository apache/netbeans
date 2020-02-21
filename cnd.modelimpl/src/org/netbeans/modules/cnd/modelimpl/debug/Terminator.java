/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
