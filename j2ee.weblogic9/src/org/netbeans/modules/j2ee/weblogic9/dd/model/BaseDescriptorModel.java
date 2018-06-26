/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
