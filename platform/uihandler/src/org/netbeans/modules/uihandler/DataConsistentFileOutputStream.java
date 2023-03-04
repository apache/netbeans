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
package org.netbeans.modules.uihandler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This output stream assures, that data written to it by a single write() call,
 * will either be all written into the file, or none of them will be.
 * 
 * @author Martin Entlicher
 */
class DataConsistentFileOutputStream extends BufferedOutputStream {
    
    private FileOutputStream fos;
    private long lastConsistentLength;
    
    public DataConsistentFileOutputStream(File file, boolean append) throws FileNotFoundException {
        this(file, append, new FileOutputStream[] { null });
    }
    
    private DataConsistentFileOutputStream(File file, boolean append, FileOutputStream[] fosPtr) throws FileNotFoundException {
        super(fosPtr[0] = new FileOutputStream(file, append));
        this.fos = fosPtr[0];
        if (append) {
            lastConsistentLength = file.length();
        } else {
            lastConsistentLength = 0L;
        }
    }

    @Override
    public synchronized void write(int b) throws IOException {
        super.write(b);
        lastConsistentLength++;
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        try {
            super.write(b, off, len);
            lastConsistentLength = lastConsistentLength + len;
        } catch (IOException ioex) {
            truncateFileToConsistentSize(fos, lastConsistentLength);
            throw ioex;
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        super.flush();
        FileChannel fch = fos.getChannel();
        fch.force(true);
    }
    
    static void truncateFileToConsistentSize(FileOutputStream fos, long size) {
        try {
            FileChannel fch = fos.getChannel();
            fch.truncate(size);
            fch.force(true);
        } catch (IOException ex) {
            Logger.getLogger(DataConsistentFileOutputStream.class.getName()).log(
                    Level.INFO,
                    "Not able to truncate file to the data consistent size of "+size+" bytes.",
                    ex);
        }
    }

}
