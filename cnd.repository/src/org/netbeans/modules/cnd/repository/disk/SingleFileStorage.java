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
package org.netbeans.modules.cnd.repository.disk;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;

/**
 *
 */
public final class SingleFileStorage implements FileStorage {

    private final File baseDir;

    SingleFileStorage(File baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public boolean open(boolean forWriting) {
        if (forWriting && !baseDir.isDirectory()) {
            baseDir.mkdirs();
        }
        return true;
    }

    @Override
    public boolean hasKey(LayerKey key) throws IOException{
        File file = getFile(key);

        if (!file.exists() || !file.canRead()) {
            return false;
        }
        return true;    
    }
    
    
    @Override
    public ByteBuffer read(LayerKey key) throws IOException {
        File file = getFile(key);

        if (!file.canRead()) {
            return null;
        }

        final long fileSize = file.length();

        if (fileSize >= Integer.MAX_VALUE) {
            throw new InternalError();
        }

        ByteBuffer result = null;
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(file, "r"); // NOI18N
            result = ByteBuffer.allocate((int) fileSize);
                    //SharedReadByteBuffer.get((int) fileSize);
            f.getChannel().read(result);
        } finally {
            if (f != null) {
                f.close();
            }
        }
        return result;
    }

    @Override
    public void write(LayerKey key, ByteBuffer data) throws IOException {
        File file = getFile(key);

        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw"); // NOI18N
            raf.getChannel().write(data);
        } finally {
            if (raf != null) {
                raf.close();
            }
        }
    }

    @Override
    public void remove(LayerKey key) throws IOException {
        File removedFile = getFile(key);
        if (removedFile == null) {
            return;
        }
        removedFile.delete();
    }

    @Override
    public void close() throws IOException {
    }

    private File getFile(LayerKey key) throws IOException {
        assert key != null;
        String fileName = RepositoryImplUtil.getKeyFileName(key);
        return new File(baseDir, fileName);
    }

    @Override
    public void debugDump(LayerKey key) {
        throw new UnsupportedOperationException("Not implemented yet."); // NOI18N
    }


    @Override
    public void dump(PrintStream ps) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet."); // NOI18N
    }

    @Override
    public void dumpSummary(PrintStream ps) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet."); // NOI18N
    }

    @Override
    public int getObjectsCount() throws IOException {
        throw new UnsupportedOperationException("Not implemented yet."); // NOI18N
    }

    @Override
    public boolean maintenance(long timeout) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet."); // NOI18N
    }

    @Override
    public String toString() {
        return "SnglFileStorage: " + baseDir; // NOI18N
    }
}
