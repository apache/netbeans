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
package org.netbeans.modules.cnd.indexing.impl;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.indexing.spi.TextIndexLayer;
import org.netbeans.modules.cnd.indexing.spi.TextIndexLayerFactory;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = TextIndexLayerFactory.class)
public final class DistTextIndexLayerFactory implements TextIndexLayerFactory {

    private static final String INDEX_FOLDER_NAME = "text_index"; // NOI18N

    @Override
    public boolean canHandle(final LayerDescriptor layerDescriptor) {
        String scheme = layerDescriptor.getURI().getScheme();
        return "file".equals(scheme);//NOI18N
    }

    @Override
    public TextIndexLayer createLayer(final LayerDescriptor layerDescriptor) {
        try {
            File indexRoot = new File(new File(layerDescriptor.getURI().getPath()), INDEX_FOLDER_NAME);
            final boolean isWritable = layerDescriptor.isWritable();
            if (!indexRoot.exists() && isWritable) {
                indexRoot.mkdirs();
            }
            if (!indexRoot.exists()) {
                return null;
            }
            DocumentIndex index = IndexManager.createDocumentIndex(indexRoot, isWritable);
            return new DiskTextIndexLayer(layerDescriptor, index);
        } catch (IOException ex) {
            Logger.getLogger(DistTextIndexLayerFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
