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

package org.netbeans.lib.profiler.heap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Tomas Hurka
 */
abstract class AbstractLongMap {

    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    abstract class Entry {
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final int VALUE_SIZE;
    final int ENTRY_SIZE;
    private File tempFile;
    long fileSize;
    private long keys;
    final int KEY_SIZE;
    final int ID_SIZE;
    final int FOFFSET_SIZE;
    Data dumpBuffer;
    CacheDirectory cacheDirectory;
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    AbstractLongMap(int size,int idSize,int foffsetSize,int valueSize,CacheDirectory cacheDir) throws FileNotFoundException, IOException {
        assert idSize == 4 || idSize == 8;
        assert foffsetSize == 4 || foffsetSize == 8;
        keys = (size * 4L) / 3L;
        ID_SIZE = idSize;
        FOFFSET_SIZE = foffsetSize;
        KEY_SIZE = ID_SIZE;
        VALUE_SIZE = valueSize;
        ENTRY_SIZE = KEY_SIZE + VALUE_SIZE;
        fileSize = keys * ENTRY_SIZE;
        tempFile = cacheDir.createTempFile("NBProfiler", ".map"); // NOI18N

        RandomAccessFile file = tempFile.newRandomAccessFile("rw"); // NOI18N
        if (Boolean.getBoolean("org.netbeans.lib.profiler.heap.zerofile")) {    // NOI18N
            byte[] zeros = new byte[512*1024];
            while(file.length()<fileSize) {
                file.write(zeros);
            }
            file.write(zeros,0,(int)(fileSize-file.length()));
        }
        file.setLength(fileSize);
        setDumpBuffer(file);
        cacheDirectory = cacheDir;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    protected void finalize() throws Throwable {
        if (cacheDirectory.isTemporary()) {
            tempFile.delete();
        }
        super.finalize();
    }

    Entry get(long key) {
        long index = getIndex(key);

        while (true) {
            long mapKey = getID(index);

            if (mapKey == key) {
                return createEntry(index);
            }

            if (mapKey == 0L) {
                return null;
            }

            index = getNextIndex(index);
        }
    }

    Entry put(long key, long value) {
        long index = getIndex(key);

        while (true) {
            long mapKey = getID(index);
            if (mapKey == 0L) {
                putID(index, key);
                return createEntry(index,value);
            } else if (mapKey == key) {
                return createEntry(index);
            }

            index = getNextIndex(index);
        }
    }

    private void setDumpBuffer(RandomAccessFile file) throws IOException {
        long length = file.length();

        try {
            if (length > Integer.MAX_VALUE) {
                dumpBuffer = new LongMemoryMappedData(file, length, ENTRY_SIZE);
            } else {
                dumpBuffer = new MemoryMappedData(file, length);
            }
        } catch (IOException ex) {
            if (ex.getCause() instanceof OutOfMemoryError) {
                dumpBuffer = new FileData(file, length);
            } else {
                throw ex;
            }
        }
    }

    long getID(long index) {
        if (ID_SIZE == 4) {
            return ((long)dumpBuffer.getInt(index)) & 0xFFFFFFFFL;
        }
        return dumpBuffer.getLong(index);
    }
    
    void putID(long index,long key) {
        if (ID_SIZE == 4) {
            dumpBuffer.putInt(index,(int)key);
        } else {
            dumpBuffer.putLong(index,key);
        }
    }
    
    long getFoffset(long index) {
        if (FOFFSET_SIZE == 4) {
            return dumpBuffer.getInt(index);
        }
        return dumpBuffer.getLong(index);
    }
    
    void putFoffset(long index,long key) {
        if (FOFFSET_SIZE == 4) {
            dumpBuffer.putInt(index,(int)key);
        } else {
            dumpBuffer.putLong(index,key);
        }
    }

    //---- Serialization support
    void writeToStream(DataOutputStream out) throws IOException {
        out.writeLong(keys);
        out.writeInt(ID_SIZE);
        out.writeInt(FOFFSET_SIZE);
        out.writeInt(VALUE_SIZE);
        out.writeUTF(tempFile.getAbsolutePath());
        dumpBuffer.force(tempFile);
    }

    AbstractLongMap(DataInputStream dis, CacheDirectory cacheDir) throws IOException {
        keys = dis.readLong();
        ID_SIZE = dis.readInt();
        FOFFSET_SIZE = dis.readInt();
        VALUE_SIZE = dis.readInt();
        tempFile = cacheDir.getCacheFile(dis.readUTF());
        
        KEY_SIZE = ID_SIZE;
        ENTRY_SIZE = KEY_SIZE + VALUE_SIZE;
        fileSize = keys * ENTRY_SIZE;
        RandomAccessFile file = tempFile.newRandomAccessFile("rw"); // NOI18N
        setDumpBuffer(file);
        cacheDirectory = cacheDir;
    }
    
    private long getIndex(long key) {
        long hash = key & 0x7FFFFFFFFFFFFFFFL;
        return (hash % keys) * ENTRY_SIZE;
    }

    private long getNextIndex(long index) {
        index += ENTRY_SIZE;
        if (index >= fileSize) {
            index = 0;
        }
        return index;
    }
    
    abstract Entry createEntry(long index);
    
    abstract Entry createEntry(long index,long value);
    
    interface Data {
        //~ Methods --------------------------------------------------------------------------------------------------------------
        
        byte getByte(long index);
        
        int getInt(long index);

        long getLong(long index);

        void putByte(long index, byte data);

        void putInt(long index, int data);

        void putLong(long index, long data);

        void force(File bufferFile) throws IOException;
    }

    private class FileData implements Data {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        RandomAccessFile file;
        byte[] buf;
        boolean bufferModified;
        long offset;
        static final int BUFFER_SIZE = 128;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        FileData(RandomAccessFile f, long length) throws IOException {
            file = f;
            buf = new byte[ENTRY_SIZE*BUFFER_SIZE];
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public synchronized byte getByte(long index) {
            int i = loadBufferIfNeeded(index);
            return buf[i];
        }

        public synchronized int getInt(long index) {
            int i = loadBufferIfNeeded(index);
            int ch1 = ((int) buf[i++]) & 0xFF;
            int ch2 = ((int) buf[i++]) & 0xFF;
            int ch3 = ((int) buf[i++]) & 0xFF;
            int ch4 = ((int) buf[i]) & 0xFF;

            return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        }

        public synchronized long getLong(long index) {
           int i = loadBufferIfNeeded(index);
           return (((long)buf[i++] << 56) +
                  ((long)(buf[i++] & 255) << 48) +
                  ((long)(buf[i++] & 255) << 40) +
                  ((long)(buf[i++] & 255) << 32) +
                  ((long)(buf[i++] & 255) << 24) +
                  ((buf[i++] & 255) << 16) +
                  ((buf[i++] & 255) <<  8) +
                  ((buf[i++] & 255) <<  0));
        }

        public synchronized void putByte(long index, byte data) {
            int i = loadBufferIfNeeded(index);
            buf[i] = data;
            bufferModified = true;
        }

        public synchronized void putInt(long index, int data) {
            int i = loadBufferIfNeeded(index);
            buf[i++] = (byte) (data >>> 24);
            buf[i++] = (byte) (data >>> 16);
            buf[i++] = (byte) (data >>> 8);
            buf[i++] = (byte) (data >>> 0);
            bufferModified = true;
        }

        public synchronized void putLong(long index, long data) {
            int i = loadBufferIfNeeded(index);
            buf[i++] = (byte) (data >>> 56);
            buf[i++] = (byte) (data >>> 48);
            buf[i++] = (byte) (data >>> 40);
            buf[i++] = (byte) (data >>> 32);
            buf[i++] = (byte) (data >>> 24);
            buf[i++] = (byte) (data >>> 16);
            buf[i++] = (byte) (data >>> 8);
            buf[i++] = (byte) (data >>> 0);
            bufferModified = true;
        }

        private int loadBufferIfNeeded(long index) {
            int i = (int) (index % (ENTRY_SIZE * BUFFER_SIZE));
            long newOffset = index - i;

            if (offset != newOffset) {
                try {
                    flush();
                    file.seek(newOffset);
                    file.readFully(buf,0,getBufferSize(newOffset));
                } catch (IOException ex) {
                    Systems.printStackTrace(ex);
                }

                offset = newOffset;
            }

            return i;
        }

        private int getBufferSize(long off) {
            int size = buf.length;
            
            if (fileSize-off<buf.length) {
                size = (int)(fileSize-off);
            }
            return size;
        }

        private void flush() throws IOException {
            if (bufferModified) {
                file.seek(offset);
                file.write(buf,0,getBufferSize(offset));
                bufferModified = false;
            }
        }

        @Override
        public void force(File bufferFile) throws IOException {
            flush();
        }
    }
    
    private static class MemoryMappedData implements Data {
        
        private static final FileChannel.MapMode MAP_MODE = Systems.isLinux() ? FileChannel.MapMode.PRIVATE : FileChannel.MapMode.READ_WRITE;

        //~ Instance fields ------------------------------------------------------------------------------------------------------

        ByteBuffer buf;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        MemoryMappedData(RandomAccessFile file, long length)
                  throws IOException {
            buf = file.mmap(MAP_MODE, length, true);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public byte getByte(long index) {
            return buf.get((int) index);
        }

        public int getInt(long index) {
            return buf.getInt((int) index);
        }

        public long getLong(long index) {
            return buf.getLong((int) index);
        }

        public void putByte(long index, byte data) {
            buf.put((int) index, data);
        }

        public void putInt(long index, int data) {
            buf.putInt((int) index, data);
        }

        public void putLong(long index, long data) {
            buf.putLong((int) index, data);
        }

        @Override
        public void force(File bufferFile) throws IOException {
            buf = bufferFile.force(MAP_MODE, buf);
        }
    }

    private static class LongMemoryMappedData implements Data {

        private static int BUFFER_SIZE_BITS = 30;
        private static long BUFFER_SIZE = 1L << BUFFER_SIZE_BITS;
        private static int BUFFER_SIZE_MASK = (int) ((BUFFER_SIZE) - 1);
        private static int BUFFER_EXT = 32 * 1024;

        //~ Instance fields ----------------------------------------------------------------------------------------------------------

        private ByteBuffer[] dumpBuffer;
        private final int entrySize;


        //~ Constructors ---------------------------------------------------------------------------------------------------------

        LongMemoryMappedData(RandomAccessFile file, long length, int entry)
                  throws IOException {
            dumpBuffer = file.mmapAsBuffers(MemoryMappedData.MAP_MODE, length, BUFFER_SIZE, BUFFER_EXT);
            entrySize = entry;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public byte getByte(long index) {
            return dumpBuffer[getBufferIndex(index)].get(getBufferOffset(index));
        }

        public int getInt(long index) {
            return dumpBuffer[getBufferIndex(index)].getInt(getBufferOffset(index));
        }

        public long getLong(long index) {
            return dumpBuffer[getBufferIndex(index)].getLong(getBufferOffset(index));
        }

        public void putByte(long index, byte data) {
            dumpBuffer[getBufferIndex(index)].put(getBufferOffset(index),data);
        }

        public void putInt(long index, int data) {
            dumpBuffer[getBufferIndex(index)].putInt(getBufferOffset(index),data);
        }

        public void putLong(long index, long data) {
            dumpBuffer[getBufferIndex(index)].putLong(getBufferOffset(index),data);
        }

        private int getBufferIndex(long index) {
            return (int) (index >> BUFFER_SIZE_BITS);
        }

        private int getBufferOffset(long index) {
            return (int) (index & BUFFER_SIZE_MASK);
        }

        @Override
        public void force(File bufferFile) throws IOException {
            dumpBuffer = bufferFile.force(MemoryMappedData.MAP_MODE, dumpBuffer, BUFFER_SIZE, BUFFER_EXT, entrySize);
        }
    }
}
