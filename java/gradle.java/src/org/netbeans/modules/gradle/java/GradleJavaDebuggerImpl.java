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
package org.netbeans.modules.gradle.java;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.java.execute.JavaRunUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.gradle.java.spi.debug.GradleJavaDebugger;

/**
 * Provides a convenient way to attach debugger to a Gradle Project.
 * The implementation is mostly based on MavenDebuggerImpl.
 * 
 * @author lkishalmi
 */
public final class GradleJavaDebuggerImpl implements GradleJavaDebugger {

    private final RequestProcessor RP = new RequestProcessor(GradleJavaDebuggerImpl.class);
    final Project project;

    public GradleJavaDebuggerImpl(Project project) {
        this.project = project;
    }

    @Override
    public void attachDebugger(String name, String transport, String host, String address) throws Exception {
        final Object[] lock = new Object [1];
        ClassPath sourcePath = Utils.getSources(project);
        ClassPath jdkSourcePath = Utils.getJdkSources(project);

        final Map properties = new HashMap();
        properties.put("sourcepath", sourcePath); //NOI18N
        properties.put("name", name); //NOI18N
        properties.put("jdksources", jdkSourcePath); //NOI18N
        properties.put("baseDir", FileUtil.toFile(project.getProjectDirectory())); // NOI18N
        if (JavaRunUtils.isCompileOnSaveEnabled(project)) {
            properties.put ("listeningCP", "sourcepath"); // NOI18N
        }

        synchronized(lock) {
            RP.post(() -> {
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
