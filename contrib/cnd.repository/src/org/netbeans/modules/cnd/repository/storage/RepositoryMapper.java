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
package org.netbeans.modules.cnd.repository.storage;

import java.util.Collection;
import org.netbeans.modules.cnd.repository.api.FilePath;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.spi.RepositoryPathMapperImplementation;
import org.netbeans.modules.cnd.repository.spi.UnitDescriptorsMatcherImplementation;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;
import org.openide.util.Lookup;

/**
 *
 */
public final class RepositoryMapper {

    private static final RepositoryMapper instance = new RepositoryMapper();

    private RepositoryMapper() {
    }

    public static boolean matches(UnitDescriptor descriptor1, UnitDescriptor descriptor2) {
        return instance.matchesImpl(descriptor1, descriptor2);
    }
//    public static UnitDescriptor map(FileSystem targetFileSystem, UnitDescriptor layerUnitDescriptor) {
//        return instance.mapImpl(targetFileSystem, layerUnitDescriptor);
//    }
    
    public static UnitDescriptor mapToClient(FileSystem targetFileSystem, UnitDescriptor layerUnitDescriptor) {
        return instance.mapToClientImpl(targetFileSystem, layerUnitDescriptor);
    }

    public static UnitDescriptor mapToLayer(FileSystem targetFileSystem, UnitDescriptor clientUnitDescriptor) {
        //in fact if we support relocation of both (project and repository)
        //we should map layer unit descriptor to client
        return instance.mapToLayerImpl(targetFileSystem, clientUnitDescriptor);
    }

    public static CharSequence map(UnitDescriptor clientUnitDescriptor, FilePath sourceFilePath) {
        return instance.mapImpl(clientUnitDescriptor, sourceFilePath);
    }

    public CharSequence mapImpl(UnitDescriptor clientUnitDescriptor, FilePath sourceFilePath) {
        Collection<? extends RepositoryPathMapperImplementation> impls =
                Lookup.getDefault().lookupAll(RepositoryPathMapperImplementation.class);

        for (RepositoryPathMapperImplementation impl : impls) {
            CharSequence result = impl.map(clientUnitDescriptor, sourceFilePath);
            if (result != null) {
                return CharSequences.create(result);
            }
        }

        return CharSequences.create(sourceFilePath.getPath());
    }

    private boolean matchesImpl(UnitDescriptor descriptor1, UnitDescriptor descriptor2) {
        if (descriptor1.equals(descriptor2)) {
            return true;
        }

        Collection<? extends UnitDescriptorsMatcherImplementation> matchers =
                Lookup.getDefault().lookupAll(UnitDescriptorsMatcherImplementation.class);

        for (UnitDescriptorsMatcherImplementation unitDescriptorsMatcher : matchers) {
            if (unitDescriptorsMatcher.matches(descriptor1, descriptor2)) {
                return true;
            }
        }

        return false;
    }
    
    
    private UnitDescriptor mapToLayerImpl(FileSystem destFileSystem, UnitDescriptor sourceUnitDescriptor) {
        return new UnitDescriptor(sourceUnitDescriptor.getName(), destFileSystem);
    }
    
    private UnitDescriptor mapToClientImpl(FileSystem destFileSystem, UnitDescriptor layerUnitDescriptor) {
        //leyerUnitDescriptor is source, nned to find deestination
        Collection<? extends UnitDescriptorsMatcherImplementation> matchers =
                Lookup.getDefault().lookupAll(UnitDescriptorsMatcherImplementation.class);
         for (UnitDescriptorsMatcherImplementation unitDescriptorsMatcher : matchers) {
            UnitDescriptor clientUnitDescriptor = unitDescriptorsMatcher.destinationDescriptor(destFileSystem, layerUnitDescriptor);
            if (clientUnitDescriptor != null) {
                return clientUnitDescriptor;
            }
        }
        return new UnitDescriptor(layerUnitDescriptor.getName(), destFileSystem);
    }    

    private UnitDescriptor mapImpl(FileSystem destFileSystem, UnitDescriptor sourceUnitDescriptor) {
        Collection<? extends UnitDescriptorsMatcherImplementation> matchers =
                Lookup.getDefault().lookupAll(UnitDescriptorsMatcherImplementation.class);
         for (UnitDescriptorsMatcherImplementation unitDescriptorsMatcher : matchers) {
            UnitDescriptor clientUnitDescriptor = unitDescriptorsMatcher.destinationDescriptor(destFileSystem, sourceUnitDescriptor);
            if (clientUnitDescriptor != null) {
                return clientUnitDescriptor;
            }
        }
         //default
        return new UnitDescriptor(sourceUnitDescriptor.getName(), destFileSystem);
    }
}
