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
