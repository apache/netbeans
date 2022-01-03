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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.netbeans.modules.cnd.repository.impl.spi.UnitsConverter;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.impl.spi.FSConverter;
import org.netbeans.modules.cnd.repository.impl.spi.FilePathConverter;
import org.netbeans.modules.cnd.repository.impl.spi.LayerConvertersProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public final class RepositoryDataInputStream extends DataInputStream implements RepositoryDataInput {
    private final LayerConvertersProvider layersConverterProvider;
    private static final UnitsConverter noConversionUnits = new UnitsConverter() {
        @Override
        public int clientToLayer(int unitID) {
            return unitID;
        }

        @Override
        public int layerToClient(int unitID) {
            return unitID;
        }
    };
    private static final FSConverter noConversionsFS = new FSConverter() {
        @Override
        public FileSystem layerToClient(int readInt) {
            throw new InternalError();
        }

        @Override
        public int clientToLayer(FileSystem fileSystem) {
            throw new InternalError();
        }
    };
    private static final FilePathConverter noConversionsFilePath = new FilePathConverter() {

        @Override
        public CharSequence layerToClient(int fileIdx) {
            throw new InternalError();
        }

        @Override
        public int clientToLayer(CharSequence filePath) {
            throw new InternalError();
        }

    };

    public RepositoryDataInputStream(InputStream in, LayerConvertersProvider layersConverterProvider) {
        super(in);
        this.layersConverterProvider = layersConverterProvider;
    }

    /**
     * Creates an instance of RepositoryDataInputStream that does not perform
     * any conversions
     *
     * @param in
     */
    public RepositoryDataInputStream(DataInputStream in) {
        this(in, LayerConvertersProvider.getReadInstance(noConversionUnits, noConversionsFS, noConversionsFilePath));
    }

    @Override
    public CharSequence readCharSequenceUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    @Override
    public int readUnitId() throws IOException {
        int rawData = readInt();
        UnitsConverter unitIDConverter = layersConverterProvider.getReadUnitsConverter();
        final int clientUnitID = unitIDConverter == null ? rawData : unitIDConverter.layerToClient(rawData);
        CndUtils.assertTrue(rawData > -1, "Impossible on disk unit id: ", rawData); //NOI18N
        return clientUnitID;
    }

    @Override
    public FileSystem readFileSystem() throws IOException {
        FSConverter fsConverter = layersConverterProvider.getReadFSConverter();
        final int fsIdx = readInt();
        final FileSystem fs = fsConverter.layerToClient(fsIdx);
        if (fs == null) {
            // I don't like this null check very much; but without it,
            // once fileSystem was read or written as null, it will be null forever,
            // code assistance not functional at all and even IDE restart doesn't help
            // see NPEs #248225, #251527, #247726
            // the assertion is moved to Storage.FSReadConverterImpl.layerToClient
            throw new IOException("Can not restore file system from persistence", new NullPointerException()); //NOI18N            
        }
        return fs;
    }

    @Override
    public CharSequence readFilePath() throws IOException {
        FilePathConverter pathConverter = layersConverterProvider.getReadFilePathConverter();
        return FilePathCache.getManager().getString(pathConverter.layerToClient(readInt()));
    }

    @Override
    public CharSequence readFilePathForFileSystem(FileSystem fs) throws IOException {
        // for now we don't distinguish path dictionaries, but could in future
        // i.e. when system library is moved from local to remote fs
        return readFilePath();
    }
}
