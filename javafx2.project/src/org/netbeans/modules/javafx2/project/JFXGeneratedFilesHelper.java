/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
