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
package org.netbeans.modules.cnd.makeproject.api.support;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import org.netbeans.modules.cnd.api.xml.LineSeparatorDetector;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Project system writes project metadata even it has not been changed.
 * With full remote this is dangerous - if a connection broke while writing we break remote project
 * I see no no way to change this behavior reliably right now.
 * So this class writes to a temp file instead, compares checksums and makes real write
 * only if checksum changes.
 * 
 * TODO: make project system smart enough to understand whether we need to write project metadata
 * 
 */
public class SmartOutputStream extends OutputStream {

    public static byte[] convertLineSeparator(ByteArrayOutputStream in, FileObject fo, FileObject dir) {
        String lineSeparator = new LineSeparatorDetector(fo, dir).getInitialSeparator();
        byte[] data = in.toByteArray();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), "UTF-8")); // NOI18N
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while(true){
                String line = reader.readLine();
                if (line == null){
                    break;
                }
                baos.write(line.getBytes("UTF-8")); // NOI18N
                baos.write(lineSeparator.getBytes("UTF-8")); // NOI18N
            }
            reader.close();
            baos.close();
            data = baos.toByteArray();
        } catch (IOException ex) {
        }
        return data;
    }
    
    public static OutputStream getSmartOutputStream(FileObject fileObject) throws IOException {
        return getSmartOutputStream(fileObject, null);
    }
    
    public static OutputStream getSmartOutputStream(FileObject fileObject, FileLock lock) throws IOException {
        //if (FileSystemProvider.getExecutionEnvironment(fileObject).isLocal()) {
        //    return fileObject.getOutputStream(lock);
        //} else {
            return new SmartOutputStream(fileObject, lock);
        //}
    }
    
    private final FileObject fileObject;
    private final FileLock lock; // can be null
    private final FileOutputStream delegate;
    private final File tempFile;
    private final Checksum checksum;
    private boolean closed = false;
    
    private static final Logger LOG = Logger.getLogger("remote.support.logger"); // NOI18N

    private SmartOutputStream(FileObject fileObject, FileLock lock) throws IOException {
        this.fileObject = fileObject;
        this.lock = lock;
        this.tempFile = File.createTempFile(fileObject.getName(), "."+fileObject.getExt()); // NOI18N
        this.delegate = new FileOutputStream(tempFile);
        this.checksum = new Adler32();
    }
    
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
            checksum.update(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
            checksum.update(b);
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            delegate.close();
            try {
                long oldCheckSum = calculateCheckSum(fileObject);
                if (oldCheckSum != checksum.getValue()) {
                    LOG.log(Level.FINEST, "Check sums differ for {0} - perform real writing", fileObject);
                    realWrite();
                } else {
                    LOG.log(Level.FINEST, "Check sums are same for {0} - no real writing is needed", fileObject);
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
                LOG.log(Level.FINEST, "Exceptions occur for {0} - perform real writing", fileObject);
                realWrite();
            } finally {
                closed = true;
                tempFile.delete();
            }
        }
        
        private void realWrite() throws IOException {
            OutputStream os = null;
            InputStream is = null;
            IOException exceptionToThrow = null;
            try {
                if (lock == null) {
                    os = fileObject.getOutputStream();
                } else {
                    os = fileObject.getOutputStream(lock);
                }
                is = new FileInputStream(tempFile);
                FileUtil.copy(is, os);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ex) {
                        exceptionToThrow = ex;
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        exceptionToThrow = ex;
                    }
                }                
                if (exceptionToThrow != null) {
                    throw exceptionToThrow;
                }
            }
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }    
        
        private static long calculateCheckSum(FileObject fileObject) throws IOException {
            Checksum checkSum = new Adler32();
            InputStream in = null;
            try {
                in = fileObject.getInputStream();
                int bufSize = 1024*16;
                byte[] buffer = new byte[bufSize];
                int read;
                while ((read = in.read(buffer, 0, bufSize)) > 0) {
                    checkSum.update(buffer, 0, read);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
            }
            return checkSum.getValue();
        }
}
