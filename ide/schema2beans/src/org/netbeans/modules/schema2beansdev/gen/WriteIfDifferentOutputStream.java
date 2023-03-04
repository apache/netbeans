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

package org.netbeans.modules.schema2beansdev.gen;

import java.util.*;
import java.io.*;

public class WriteIfDifferentOutputStream extends OutputStream {
    private RandomAccessFile randFile;
    private long initialRandFileLength;
    private boolean justWrite = false;

    public WriteIfDifferentOutputStream(RandomAccessFile randFile) throws IOException {
        this.randFile = randFile;
        this.initialRandFileLength = randFile.length();
    }

    public WriteIfDifferentOutputStream(String filename) throws IOException {
        this(new RandomAccessFile(filename, "rw"));	// NOI18N
    }

    public WriteIfDifferentOutputStream(File file) throws IOException {
        this(new RandomAccessFile(file, "rw"));	// NOI18N
    }

    public void write(int b) throws IOException {
        if (justWrite) {
            randFile.write(b);
            return;
        }
        long fp = randFile.getFilePointer();
        if (fp + 1 > initialRandFileLength) {
            justWrite = true;
            randFile.write(b);
            return;
        }
        int fromFile = randFile.read();
        if (fromFile != b) {
            //System.out.println("different: fromFile="+fromFile+" b="+b);
            randFile.seek(fp);
            randFile.write(b);
            justWrite = true;
            return;
        }
    }

    private byte[] writeBuf;
    private int lastLen = -1;
    public void write(byte[] b, int off, int len) throws IOException {
        if (justWrite) {
            randFile.write(b, off, len);
            return;
        }
        long fp = randFile.getFilePointer();
        if (fp + len > initialRandFileLength) {
            justWrite = true;
            randFile.write(b, off, len);
            return;
        }
        if (len > lastLen) {
            // Allocate a new buffer only if the last one wasn't big enough.
            writeBuf = new byte[len];
            lastLen = len;
        }
        randFile.read(writeBuf, 0, len);
        for (int i = off, j = 0; j < len; ++i, ++j) {
            if (writeBuf[j] != b[i]) {
                //System.out.println("different: i="+i+" j="+j);
                randFile.seek(fp);
                randFile.write(b, off, len);
                justWrite = true;
                return;
            }
        }
    }

    /*
      public void flush() throws IOException {
      randFile.flush();
      }
    */

    /**
     * At this point, have we changed the file?
     * It's possible to not change a file until close() is called.
     */
    public boolean isChanged() {
        return justWrite;
    }

    public void close() throws IOException {
        // Truncate it.
        //System.out.println("truncating to "+randFile.getFilePointer());
        if (randFile.getFilePointer() < randFile.length()) {
            justWrite = true;
            randFile.setLength(randFile.getFilePointer());
        }
        randFile.close();
    }
}
