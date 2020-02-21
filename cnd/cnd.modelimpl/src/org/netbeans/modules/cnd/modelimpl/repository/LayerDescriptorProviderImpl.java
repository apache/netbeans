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
package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptorProvider;
import org.netbeans.modules.cnd.spi.project.NativeProjectLayerDescriptorProvider;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = LayerDescriptorProvider.class, position = 500, supersedes = "org.netbeans.modules.cnd.repository.impl.util.DefaultLayerDescriptorProvider")
public class LayerDescriptorProviderImpl implements LayerDescriptorProvider {

    @Override
    public List<LayerDescriptor> getLayerDescriptors(UnitDescriptor unitDescriptor) {
        Collection<NativeProject> projects = NativeProjectRegistry.getDefault().getOpenProjects();
        NativeProject np = findProjectsByDescriptor(projects, unitDescriptor);
        List<LayerDescriptor> res = new ArrayList<>();
        for (NativeProjectLayerDescriptorProvider provider : Lookup.getDefault().lookupAll(NativeProjectLayerDescriptorProvider.class)) {
            List<URI> uriList = provider.getLayerDescriptors(np);
            if (uriList != null) {
                for (URI uri : uriList) {
                    res.add(new LayerDescriptor(uri));
                }
            }
        }
        if (res.isEmpty()) {
            res.add(getDefault());
        }
        return res;
    }

    private NativeProject findProjectsByDescriptor(Collection<NativeProject> projects, UnitDescriptor unitDescriptor) {
        for (NativeProject np : projects) {
            UnitDescriptor currDescriptor = KeyUtilities.createUnitDescriptor(np);
            if (currDescriptor.equals(unitDescriptor)) {
                return np;
            }
        }
        return null;
    }

    public LayerDescriptor getDefault() {
        File file = Places.getCacheSubdirectory("cnd/model"); // NOI18N
        URI uri = Utilities.toURI(file);
        LayerDescriptor layerDescriptor = new LayerDescriptor(uri);
        return layerDescriptor;
    }

}
