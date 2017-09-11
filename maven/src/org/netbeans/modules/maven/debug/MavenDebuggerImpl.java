/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.debug;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.spi.debug.MavenDebugger;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=MavenDebugger.class, projectType="org-netbeans-modules-maven")
public class MavenDebuggerImpl implements MavenDebugger {
    private final RequestProcessor RP = new RequestProcessor(MavenDebuggerImpl.class);
    private final Project nbproject;
    
    /** Creates a new instance of MavenDebuggerImpl */
    public MavenDebuggerImpl(Project prj) {
        nbproject = prj;
    }

    @Override
    public void attachDebugger(InputOutput logger, String name,
            final String transport,
            final String host, 
            final String address) throws Exception {
//        JPDAStart.verifyPaths(getProject(), classpath);
//        JPDAStart.verifyPaths(getProject(), sourcepath);

        final Object[] lock = new Object [1];
        ClassPath sourcePath = Utils.createSourcePath(nbproject);
        ClassPath jdkSourcePath = Utils.createJDKSourcePath(nbproject);

        final Map properties = new HashMap();
        properties.put("sourcepath", sourcePath); //NOI18N
        properties.put("name", name); //NOI18N
        properties.put("jdksources", jdkSourcePath); //NOI18N
        properties.put("baseDir", FileUtil.toFile(nbproject.getProjectDirectory())); // NOI18N
        if (RunUtils.isCompileOnSaveEnabled(nbproject)) {
            properties.put ("listeningCP", "sourcepath"); // NOI18N
        }

        synchronized(lock) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    synchronized(lock) {
                        try {
                            // VirtualMachineManagerImpl can be initialized
                            // here, so needs to be inside RP thread.
                            if (transport.equals("dt_socket")) {//NOI18N
                                try {
                                    JPDADebugger.attach(
                                            host,
                                            Integer.parseInt(address),
                                            new Object[] {properties}
                                    );
                                } catch (NumberFormatException e) {
                                    throw new Exception(
                                            "address attribute must specify port " + //NOI18N
                                            "number for dt_socket connection"); //NOI18N
                                }
                            }
                            else {
                                    JPDADebugger.attach(
                                            address,
                                            new Object[] {properties}
                                    );
                            }
                        } catch (Throwable e) {
                            lock[0] = e;
                        } finally {
                            lock.notify();
                        }
                    }
                }
            });
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw e;
            }
            if (lock[0] != null)  {
                throw new Exception("", (Throwable) lock[0]);
            }

        }
    }
    
}
