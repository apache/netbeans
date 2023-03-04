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

package org.netbeans.lib.profiler.wireprotocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


/**
 * This command is issued by the profiling back-end when an event buffer is dumped into the shared-memory file
 * for natural reasons (capacity exceeded).
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 * @author Tomas Hurka
 */
public class EventBufferDumpedCommand extends Command {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    
    private int bufSize;
    private String eventBufferFileName;
    private byte[] buffer;
    private int startPos;
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------
    
    public EventBufferDumpedCommand(int bufSize, byte[] buf, int start) {
        super(EVENT_BUFFER_DUMPED);
        this.bufSize = bufSize;
        buffer = buf;
        startPos = start;
        eventBufferFileName = "";
    }

    public EventBufferDumpedCommand(int bufSize, String bufferName) {
        super(EVENT_BUFFER_DUMPED);
        this.bufSize = bufSize;
        buffer = null;
        startPos = -1;
        eventBufferFileName = bufferName;
    }
    
    // Custom serialization support
    EventBufferDumpedCommand() {
        super(EVENT_BUFFER_DUMPED);
    }
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    public int getBufSize() {
        return bufSize;
    }
    
    public byte[] getBuffer() {
        return buffer;
    }

    // For debugging
    public String toString() {
        return super.toString() + ", bufSize: " + bufSize + (eventBufferFileName.length()>0 ? ", eventBufferFileName:" + eventBufferFileName : ""); // NOI18N
    }

    public String getEventBufferFileName() {
        return eventBufferFileName;
    }
    
    void readObject(ObjectInputStream in) throws IOException {
        boolean hasBuffer;
        
        bufSize = in.readInt();
        hasBuffer = in.readBoolean();
        if (hasBuffer) {
            int compressedSize = in.readInt();
            byte[] compressedBuf = new byte[compressedSize];
            Inflater decompressor = new Inflater();
            
            buffer = new byte[bufSize];
            in.readFully(compressedBuf);
            decompressor.setInput(compressedBuf);
            try {
                int originalSize = decompressor.inflate(buffer);
                assert originalSize==bufSize;
            } catch (DataFormatException ex) {
                throw new IOException(ex.getMessage());
            } finally {
                decompressor.end();
            }
            eventBufferFileName = "";
        } else {
            eventBufferFileName = in.readUTF();
        }
    }
    
    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(bufSize);
        out.writeBoolean(buffer != null);
        if (buffer != null) {
            Deflater compressor = new Deflater();
            // for small buffers, the compressed size can be somewhat larger than the original  
            byte[] compressedBytes = new byte[bufSize + 32]; 
            int compressedSize;
            
            compressor.setInput(buffer,startPos,bufSize);
            compressor.finish();
            compressedSize = compressor.deflate(compressedBytes);
            out.writeInt(compressedSize);
            out.write(compressedBytes,0,compressedSize);
        } else {
            out.writeUTF(eventBufferFileName);
        }
    }
}
