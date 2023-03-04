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

package org.netbeans.modules.javaee.wildfly.config;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Libor Kotouc
 */
public class ResourceConfigurationHelper {
    private static final Logger LOGGER = Logger.getLogger(ResourceConfigurationHelper.class.getName());
    private ResourceConfigurationHelper() {
    }

    public static void writeFile(final File file, final BaseBean bean) throws ConfigurationException {
        assert file != null : "File to write can't be null"; // NOI18N
        assert file.getParentFile() != null : "File parent folder can't be null"; // NOI18N

        try {
            FileObject cfolder = FileUtil.toFileObject(FileUtil.normalizeFile(file.getParentFile()));
            if (cfolder == null) {
                try {
                    cfolder = FileUtil.createFolder(FileUtil.normalizeFile(file.getParentFile()));
                } catch (IOException ex) {
                    throw new ConfigurationException(NbBundle.getMessage(ResourceConfigurationHelper.class,
                            "MSG_FailedToCreateConfigFolder", file.getParentFile().getAbsolutePath()));
                }
            }

            final FileObject folder = cfolder;
            FileSystem fs = folder.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        String name = file.getName();
                        FileObject configFO = folder.getFileObject(name);
                        if (configFO == null) {
                            configFO = folder.createData(name);
                        }
                        lock = configFO.lock();
                        os = new BufferedOutputStream (configFO.getOutputStream(lock), 4086);
                        // TODO notification needed
                        if (bean != null) {
                            bean.write(os);
                        }
                    } finally {
                        if (os != null) {
                            try { os.close(); } catch(IOException ioe) {}
                        }
                        if (lock != null)
                            lock.releaseLock();
                    }
                }
            });
        } catch (IOException e) {
            throw new ConfigurationException (e.getLocalizedMessage ());
        }
    }

    /**
     * Replace the content of the document by the graph.
     */
    public static void replaceDocument(final StyledDocument doc, BaseBean graph) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            graph.write(out);
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, null, ioe);
        }
        NbDocument.runAtomic(doc, new Runnable() {
            public void run() {
                try {
                    doc.remove(0, doc.getLength());
                    doc.insertString(0, out.toString(), null);
                } catch (BadLocationException ble) {
                    LOGGER.log(Level.WARNING, null, ble);
                }
            }
        });
    }

}
