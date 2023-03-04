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

package org.netbeans.modules.j2ee.weblogic9.dd.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class BaseDescriptorModel {

    protected static final Version VERSION_10_3_1 = Version.fromJsr277NotationWithFallback("10.3.1"); // NOI18N

    protected static final Version VERSION_10_3_0 = Version.fromJsr277NotationWithFallback("10.3.0"); // NOI18N

    protected static final Version VERSION_12_1_1 = Version.fromJsr277NotationWithFallback("12.1.1"); // NOI18N

    protected static final Version VERSION_12_2_1 = Version.fromJsr277NotationWithFallback("12.2.1"); // NOI18N
    
    private final CommonDDBean bean;

    public BaseDescriptorModel(CommonDDBean bean) {
        this.bean = bean;
    }    
    
    public final void write(final OutputStream os) throws IOException {
        bean.write(os);
    }
    
    public final void write(final File file) throws ConfigurationException {
        try {
            FileObject cfolder = FileUtil.toFileObject(file.getParentFile());
            if (cfolder == null) {
                File parentFile = file.getParentFile();
                try {
                    cfolder = FileUtil.createFolder(parentFile);
                } catch (IOException ioe) {
                    String msg = NbBundle.getMessage(EjbJarModel.class, "MSG_FailedToCreateConfigFolder", parentFile.getPath());
                    throw new ConfigurationException(msg, ioe);
                }
            }
            final FileObject folder = cfolder;
            FileSystem fs = folder.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    String name = file.getName();
                    FileObject configFO = FileUtil.createData(folder, name);
                    FileLock lock = configFO.lock();
                    try {
                        OutputStream os = new BufferedOutputStream(configFO.getOutputStream(lock), 4086);
                        try {
                            // TODO notification needed
                            if (bean != null) {
                                bean.write(os);
                            }
                        } finally {
                            os.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            });
            FileUtil.refreshFor(file);
        } catch (IOException e) {
            String msg = NbBundle.getMessage(EjbJarModel.class, "MSG_WriteToFileFailed", file.getPath());
            throw new ConfigurationException(msg, e);
        }
    }      
}
