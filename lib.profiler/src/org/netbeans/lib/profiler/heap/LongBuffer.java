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

package org.netbeans.lib.profiler.heap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * LongBuffer is a special kind of buffer for storing longs. It uses array of longs if there is only few longs
 * stored, otherwise longs are saved to backing temporary file.
 * @author Tomas Hurka
 */
class LongBuffer {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private DataInputStream readStream;
    private boolean readStreamClosed;
    private DataOutputStream writeStream;
    private File backingFile;
    private long[] buffer;
    private boolean useBackingFile;
    private int bufferSize;
    private int readOffset;
    private int longs;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    LongBuffer(int size) {
        buffer = new long[size];
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    void delete() {
        if (backingFile != null) {
            backingFile.delete();
            useBackingFile = false;
            backingFile = null;
            longs = 0;
        }
    }

    boolean hasData() {
        return longs > 0;
    }

    long readLong() throws IOException {
        if (!useBackingFile) {
            if (readOffset < bufferSize) {
                return buffer[readOffset++];
            } else {
                return 0;
            }
        }
        if (readStreamClosed) {
            return 0;
        }
        try {
            return readStream.readLong();
        } catch (EOFException ex) {
            readStreamClosed = true;
            readStream.close();
            return 0L;
        }
    }

    void reset() throws IOException {
        bufferSize = 0;
        if (writeStream != null) {
            writeStream.close();
        }
        if (readStream != null) {
            readStream.close();
        }
        writeStream = null;
        readStream = null;
        readStreamClosed = false;
        longs = 0;
        useBackingFile = false;
        readOffset = 0;
    }

    void startReading() {
        if (useBackingFile) {
            try {
                writeStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        writeStream = null;
        rewind();
    }

    void rewind() {
        readOffset = 0;

        if (useBackingFile) {
            try {
                if (readStream != null) {
                    readStream.close();
                }
                readStream = new DataInputStream(new BufferedInputStream(new FileInputStream(backingFile), buffer.length * 8));
                readStreamClosed = false;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    void writeLong(long data) throws IOException {
        longs++;
        if (bufferSize < buffer.length) {
            buffer[bufferSize++] = data;
            return;
        }

        if (backingFile == null) {
            backingFile = File.createTempFile("NBProfiler", ".gc"); // NOI18N
            backingFile.deleteOnExit();
        }

        if (writeStream == null) {
            writeStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(backingFile), buffer.length * 8));

            for (int i = 0; i < buffer.length; i++) {
                writeStream.writeLong(buffer[i]);
            }

            useBackingFile = true;
        }

        writeStream.writeLong(data);
    }
    
    LongBuffer revertBuffer() throws IOException {
        LongBuffer reverted = new LongBuffer(buffer.length);
        
        if (bufferSize < buffer.length) {
            for (int i=0;i<bufferSize;i++) {
                reverted.writeLong(buffer[bufferSize - 1 - i]);
            }
        } else {
            writeStream.flush();
            RandomAccessFile raf = new RandomAccessFile(backingFile,"r");
            long offset = raf.length();
            while(offset > 0) {
                offset-=8;
                raf.seek(offset);
                reverted.writeLong(raf.readLong());
            }
        }
        reverted.startReading();
        return reverted;
    }
    
    int getSize() {
        return longs;
    }
    
}
