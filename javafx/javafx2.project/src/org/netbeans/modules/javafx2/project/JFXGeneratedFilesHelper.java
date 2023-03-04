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
package org.netbeans.modules.javafx2.project;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.UserQuestionException;
import org.openide.util.Utilities;

/**
 * Modified from org.netbeans.spi.project.support.ant.GeneratedFilesHelper
 * to enable FX customization of build.xml contents without changes in SE code.
 * 
 * @author Petr Somol
 */
public final class JFXGeneratedFilesHelper {

    /**
     * Create <code>build.xml</code> from <code>project.xml</code> plus a supplied XSLT stylesheet,
     * overwriting the one coming from SE project. This method is a modified copy of
     * org.netbeans.spi.project.support.ant.GeneratedFilesHelper.generateBuildScriptFromStylesheet().
     * It attempts to overwrite, but does not react in case of failure and never updates genfiles
     * (in order to prevent SE project open hook from regenerating build.xml back to SE version).
     * <p>
     * Acquires write access.
     * </p>
     * @param h project's AntProjectHelper
     * @param path a project-relative file path - expectedly {@link #BUILD_XML_PATH}
     * @param stylesheet a URL to an XSLT stylesheet accepting <code>project.xml</code>
     *                   as input and producing the build script as output
     * @throws IOException if transforming or writing the output failed
     */
    public static void generateBuildScriptFromStylesheet(final AntProjectHelper h, final String path, final URL stylesheet) throws IOException {
        if (h == null) {
            throw new IllegalArgumentException("Null AntProjectHelper"); // NOI18N
        }
        if (path == null) {
            throw new IllegalArgumentException("Null path"); // NOI18N
        }
        if (stylesheet == null) {
            throw new IllegalArgumentException("Null stylesheet"); // NOI18N
        }
        final FileObject dir = h.getProjectDirectory();
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    // Need to use an atomic action since otherwise creating new build scripts might
                    // cause them to not be recognized as Ant scripts.
                    dir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            FileObject projectXml = dir.getFileObject(AntProjectHelper.PROJECT_XML_PATH);
                            if (projectXml == null) {
                                throw new IOException("Missing project metadata: " + h.resolveFile(AntProjectHelper.PROJECT_XML_PATH)); // NOI18N
                            }                            
                            byte[] projectXmlData;
                            InputStream is = projectXml.getInputStream();
                            try {
                                projectXmlData = load(is);
                            } finally {
                                is.close();
                            }
                            byte[] stylesheetData;
                            is = stylesheet.openStream();
                            try {
                                stylesheetData = load(is);
                            } finally {
                                is.close();
                            }
                            final byte[] resultData;
                            TransformerFactory tf = TransformerFactory.newInstance();
                            try {
                                StreamSource stylesheetSource = new StreamSource(
                                    new ByteArrayInputStream(stylesheetData), stylesheet.toExternalForm());
                                Transformer t = tf.newTransformer(stylesheetSource);
                                File projectXmlF = FileUtil.toFile(projectXml);
                                assert projectXmlF != null;
                                StreamSource projectXmlSource = new StreamSource(
                                    new ByteArrayInputStream(projectXmlData), projectXmlF.toURI().toString());
                                ByteArrayOutputStream result = new ByteArrayOutputStream();
                                t.transform(projectXmlSource, new StreamResult(result));
                                resultData = result.toByteArray();
                            } catch (TransformerException e) {
                                throw (IOException)new IOException(e.toString()).initCause(e);
                            }
                            final FileSystem.AtomicAction body = new FileSystem.AtomicAction() {
                                public void run() throws IOException {
                                    final FileObject buildScriptXml = FileUtil.createData(dir, path);
                                    assert buildScriptXml.isValid();
                                    FileLock lock1 = buildScriptXml.lock();
                                    try {
                                        OutputStream os1 = new EolFilterOutputStream(buildScriptXml.getOutputStream(lock1));
                                        try {
                                            os1.write(resultData);
                                        } finally {
                                            os1.close();
                                        }
                                    } finally {
                                        lock1.releaseLock();
                                    }
                                }
                            };
                            try {
                                body.run();
                            } catch (UserQuestionException uqe) {
                                // no reaction needed here
                            }
                        }
                    });
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }

    /**
     * Load data from a stream into a buffer.
     */
    private static byte[] load(InputStream is) throws IOException {
        int size = Math.max(1024, is.available()); // #46235
        ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
        byte[] buf = new byte[size];
        int read;
        while ((read = is.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        return baos.toByteArray();
    }

     // #45373 - workaround: on Windows make sure that all lines end with CRLF.
     // marcow: Use at least some buffered output!
    private static class EolFilterOutputStream extends BufferedOutputStream {

        private boolean isActive = Utilities.isWindows();
        private int last = -1;
        
        public EolFilterOutputStream(OutputStream os) {
            super(os, 4096);
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (isActive) {
                for (int i = off; i < off + len; i++) {
                    write(b[i]);
                }
            }
            else {
                super.write(b, off, len);
            }
        }

        @Override
        public void write(int b) throws IOException {
            if (isActive) {
                if (b == '\n' && last != '\r') {
                    super.write('\r');
                }
                last = b;
            }
            super.write(b);
        }

    }

}
