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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.indexing.api.CndTextIndexKey;
import org.netbeans.modules.cnd.indexing.spi.TextIndexLayer;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerListener;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.impl.spi.UnitsConverter;

/**
 *
 */
public final class TextIndexStorage implements LayerListener{

    private final List<TextIndexLayer> layers;
    private final LayeringSupport layeringSupport;
    private final TextLayerProvider textLayerProvider;

    TextIndexStorage(LayeringSupport layeringSupport) {
        this.layeringSupport = layeringSupport;
        this.textLayerProvider = new TextLayerProvider();
        layers = Collections.unmodifiableList(createLayers(layeringSupport.getLayerDescriptors()));
    }

    public void put(final CndTextIndexKey indexKey, final Set<CharSequence> ids) {
        for (TextIndexLayer layer : layers) {
            if (layer.getDescriptor().isWritable()) {
                CndTextIndexKey layerKey = toLayerKey(layer.getDescriptor(), indexKey);
                layer.put(layerKey, ids);
            }
        }
    }

    public List<CndTextIndexKey> query(final CharSequence text) {
        List<CndTextIndexKey> result = new ArrayList<CndTextIndexKey>();
        for (TextIndexLayer layer : layers) {
            Collection<CndTextIndexKey> data = layer.query(text);
            if (data != null) {
                for (CndTextIndexKey key : data) {
                    result.add(toClientKey(layer.getDescriptor(), key));
                }
            }
        }
        return result;
    }

    public void remove(final CndTextIndexKey indexKey) {
        for (TextIndexLayer layer : layers) {
            if (layer.getDescriptor().isWritable()) {
                CndTextIndexKey layerKey = toLayerKey(layer.getDescriptor(), indexKey);
                layer.remove(layerKey);
            }
        }
    }

    private List<TextIndexLayer> createLayers(final List<LayerDescriptor> layerDescriptors) {
        List<TextIndexLayer> result = new ArrayList<TextIndexLayer>();

        for (LayerDescriptor layerDescriptor : layerDescriptors) {
            TextIndexLayer layer = textLayerProvider.getLayer(layerDescriptor);
            if (layer != null) {
                result.add(layer);
            }
        }

        return result;
    }

    private CndTextIndexKey toLayerKey(LayerDescriptor layerDescriptor, CndTextIndexKey clientKey) {
        int clientUnitID = clientKey.getUnitId();
        UnitsConverter writeUnitsConverter = layeringSupport.getWriteUnitsConverter(layerDescriptor);
        return new CndTextIndexKey(writeUnitsConverter.clientToLayer(clientUnitID), clientKey.getFileNameIndex());
    }

    @Override
    public boolean layerOpened(LayerDescriptor layerDescriptor) {
        //find the layer
        for (TextIndexLayer layer : layers) {
            if (layerDescriptor.equals(layer.getDescriptor())) {
                return layer.isValid();
            }
        }
        return true;
    }
    
    
    
    public boolean isValid() {
        boolean isOK = true;
        for (TextIndexLayer layer : layers) {
            isOK &= layer.isValid();
        }        
        return isOK;
    }

    private CndTextIndexKey toClientKey(LayerDescriptor layerDescriptor, CndTextIndexKey layerKey) {
        int layerUnitID = layerKey.getUnitId();
        UnitsConverter readUnitsConverter = layeringSupport.getReadUnitsConverter(layerDescriptor);
        int clientUnitID = readUnitsConverter.layerToClient(layerUnitID);
        return new CndTextIndexKey(clientUnitID, layerKey.getFileNameIndex());
    }

    void shutdown() {
        for (TextIndexLayer layer : layers) {
            if (layer.getDescriptor().isWritable()) {
                layer.shutdown();
            }
        }
        textLayerProvider.shutdown();
    }

    void unitRemoved(int unitId) {
        if (unitId < 0) {
            return;
        }
        for (TextIndexLayer layer : layers) {
            //otherwise need to implement Removed objects
            if (layer.getDescriptor().isWritable()) {
                layer.unitRemoved(unitId);
            }
        }            
    }
}
