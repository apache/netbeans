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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerFactory;
import org.netbeans.modules.cnd.repository.impl.spi.Layer;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
/**
 * This factory creates "LayerProxies" that have WritableDiskLayerImpl
 * underneath.
 *
 * WritableDiskLayerImpl is always writable, but returned "proxy" could be
 * read-only (depending on layerDescriptor).
 *
 * But R/O and R/W layers for the same URI (identity part) will share the same
 * WritableDiskLayerImpl instance.
 * 
 * TODO: The above is wrong now. And this is correct. isn't it?
 * 
 */
@ServiceProvider(service = LayerFactory.class, position = 1000)
public final class DiskLayerImplFactory implements LayerFactory {

    @Override
    public boolean canHandle(LayerDescriptor layerDescriptor) {
        return "file".equals(layerDescriptor.getURI().getScheme()); // NOI18N
    }

    @Override
    public Layer createLayer(LayerDescriptor layerDescriptor, LayeringSupport layeringSupport) {
        DiskLayerImpl layer = new DiskLayerImpl(layerDescriptor, layeringSupport);
        return new DiskLayerImplDelegate(layer, layerDescriptor);
    }
}
