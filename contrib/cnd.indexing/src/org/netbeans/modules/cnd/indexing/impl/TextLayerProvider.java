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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.indexing.spi.TextIndexLayer;
import org.netbeans.modules.cnd.indexing.spi.TextIndexLayerFactory;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.openide.util.Lookup;

/**
 *
 */
final class TextLayerProvider {

    private final Map<LayerDescriptor, TextIndexLayer> cache = new HashMap<LayerDescriptor, TextIndexLayer>();

    TextLayerProvider() {
    }

    public TextIndexLayer getLayer(LayerDescriptor layerDescriptor) {
        TextIndexLayer layer;
        synchronized (cache) {
            layer = cache.get(layerDescriptor);
            if (layer == null) {
                Collection<? extends TextIndexLayerFactory> factories = Lookup.getDefault().lookupAll(TextIndexLayerFactory.class);
                for (TextIndexLayerFactory factory : factories) {
                    if (factory.canHandle(layerDescriptor)) {
                        layer = factory.createLayer(layerDescriptor);
                        cache.put(layerDescriptor, layer);
                        break;
                    }
                }
            }
        }
        return layer;
    }
    
    public void shutdown() {
        synchronized (cache) {
            cache.clear();
        }
    }
}
