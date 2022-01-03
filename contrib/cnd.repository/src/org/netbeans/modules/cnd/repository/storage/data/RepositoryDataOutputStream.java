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
package org.netbeans.modules.cnd.repository.storage.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.impl.spi.WriteLayerCapability;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.impl.spi.LayerConvertersProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public final class RepositoryDataOutputStream extends DataOutputStream implements RepositoryDataOutput {
    private final LayerConvertersProvider layersConverterProvider;
    private final WriteLayerCapability wc;
    private final LayerKey layerKey;
    private static final OutputStreamEx outputStream = new OutputStreamEx();

    private RepositoryDataOutputStream(OutputStream outputStream, LayerKey layerKey, WriteLayerCapability wc, LayerConvertersProvider layersConverterProvider) {
        super(outputStream);
        this.layersConverterProvider = layersConverterProvider;
        //wc can be null
        this.wc = wc;
        this.layerKey = layerKey;
    }

    public RepositoryDataOutputStream(LayerKey layerKey, WriteLayerCapability wc, LayerConvertersProvider layersConverterProvider) {
        this(outputStream.reset(), layerKey, wc,  layersConverterProvider);
    }

    public RepositoryDataOutputStream(OutputStream outputStream, LayerConvertersProvider layersConverterProvider) {
        this(outputStream, null, null, layersConverterProvider);
    }

    @Override
    public void writeCharSequenceUTF(CharSequence s) throws IOException {
        writeCharSequenceUTF(s);
    }

    @Override
    public void writeUnitId(int unitId) throws IOException {
        final int unitID = layersConverterProvider.getWriteUnitsConverter().clientToLayer(unitId);
        CndUtils.assertTrue(unitID > -1, "Impossible on disk unit id: ", unitID); //NOI18N
        writeInt(unitID);
    }

    @Override
    public void writeFileSystem(FileSystem fileSystem) throws IOException {
        writeInt(layersConverterProvider.getWriteFSConverter().clientToLayer(fileSystem));
    }

    @Override
    public void writeFilePath(CharSequence filePath) throws IOException {
        writeInt(layersConverterProvider.getWriteFilePathConverter().clientToLayer(filePath));
    }

    @Override
    public void writeFilePathForFileSystem(FileSystem fileSystem, CharSequence filePath) throws IOException {
        // for now we don't distinguish path dictionaries, but could in future
        // i.e. when system library is moved from local to remote fs
        writeFilePath(filePath);
    }

    @Override
    public void commit() {
        if (wc != null && layerKey != null) {
            wc.write(layerKey, outputStream.getBuffer());
        }
    }


    private static class OutputStreamEx extends OutputStream {

        private ByteBuffer buffer;

        public OutputStreamEx() {
            buffer = ByteBuffer.allocateDirect(1024);
        }

        public OutputStreamEx reset() {
            buffer.clear();
            return this;
        }

        public ByteBuffer getBuffer() {
            ByteBuffer result = buffer;
            if (buffer.capacity() > 4096) {
                buffer = ByteBuffer.allocateDirect(1024);
            }
            result.flip();
            return result;
        }

        @Override
        public void write(int b) throws IOException {
            int newposition = buffer.position() + 1;
            if (newposition > buffer.capacity()) {
                ByteBuffer newBuffer = ByteBuffer.allocateDirect(Math.max(buffer.capacity() << 1, newposition));
                buffer.flip();
                newBuffer.put(buffer);
                buffer = newBuffer;
            }
            buffer.put((byte) b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            assert off == 0;
            int newposition = buffer.position() + len;
            if (newposition > buffer.capacity()) {
                ByteBuffer newBuffer = ByteBuffer.allocateDirect(Math.max(buffer.capacity() << 1, newposition));
                buffer.flip();
                newBuffer.put(buffer);
                buffer = newBuffer;
            }
            buffer.put(b, off, len);
        }
    }
}
