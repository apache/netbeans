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
package org.netbeans.modules.java.api.common.singlesourcefile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Arunava Sinha
 */
class AttachDebuggerProcess {

    private final RequestProcessor RP = new RequestProcessor(AttachDebuggerProcess.class);

    public void attach(String name, String transport, String host, String address, FileObject fileObject) throws Exception {

        final Object[] lock = new Object[1];
        ClassPath sourcePath = ClassPathSupport.createClassPath(fileObject.getParent());
        final Map properties = new HashMap();
        properties.put("sourcepath", sourcePath); //NOI18N
        File baseDir = FileUtil.toFile(fileObject.getParent());
        properties.put("baseDir", baseDir); //NOI18N
        properties.put("name", name); //NOI18N
        synchronized (lock) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        try {
                            // VirtualMachineManagerImpl can be initialized
                            // here, so needs to be inside RP thread.
                            if (transport.equals("dt_socket")) {//NOI18N
                                try {
                                    JPDADebugger.attach(
                                            host,
                                            Integer.parseInt(address),
                                            new Object[]{properties}
                                    );
                                } catch (NumberFormatException e) {
                                    throw new Exception(
                                            "address attribute must specify port " //NOI18N
                                            + "number for dt_socket connection"); //NOI18N
                                }
                            } else {
                                JPDADebugger.attach(
                                        address,
                                        new Object[]{properties}
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
            if (lock[0] != null) {
                throw new Exception("", (Throwable) lock[0]);
            }

        }
    }
}
