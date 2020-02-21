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

import java.util.Collection;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.repository.api.FilePath;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.spi.RepositoryPathMapperImplementation;
import org.netbeans.modules.cnd.repository.spi.UnitDescriptorsMatcherImplementation;
import org.netbeans.modules.cnd.spi.project.NativeProjectRelocationMapperProvider;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 */
@ServiceProviders({
    @ServiceProvider(service = UnitDescriptorsMatcherImplementation.class, position = 100),
    @ServiceProvider(service = RepositoryPathMapperImplementation.class, position = 100)
})
public class RepositoryMapperProvider implements RepositoryPathMapperImplementation, UnitDescriptorsMatcherImplementation {

    @Override
    public CharSequence map(UnitDescriptor unitDescriptor, FilePath sourceFilePath) {
        Collection<NativeProject> projects = NativeProjectRegistry.getDefault().getOpenProjects();
        NativeProject np = findProjectsByDescriptor(projects, unitDescriptor);
        if (np == null) {
            return sourceFilePath.getPath();
        }
        Collection<? extends NativeProjectRelocationMapperProvider> providers =
                Lookup.getDefault().lookupAll(NativeProjectRelocationMapperProvider.class);
        for (NativeProjectRelocationMapperProvider provider : providers) {
            CharSequence destinationPath = provider.getDestinationPath(np, sourceFilePath.getPath());
            if (destinationPath != null) {
                return destinationPath;
            }
        }

        return sourceFilePath.getPath();

    }

    @Override
    public boolean matches(UnitDescriptor descriptor1, UnitDescriptor descriptor2) {
        //descriptor1 can be source and descriptor2 can be destination
        //or vice versa
        UnitDescriptor mapDescriptor = destinationDescriptor(descriptor1.getFileSystem(), descriptor2);
        boolean matches =  mapDescriptor != null && mapDescriptor.equals(descriptor1);
        if (matches) {
            return true;
        }
        mapDescriptor = sourceDescriptor(descriptor1.getFileSystem(), descriptor2);
        matches =  mapDescriptor.equals(descriptor1);
        return matches;
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

    @Override
    public UnitDescriptor destinationDescriptor(FileSystem targetFileSystem, UnitDescriptor sourceUnitDescriptor) {
        Collection<NativeProject> projects = NativeProjectRegistry.getDefault().getOpenProjects();
        //go through the projects and try to map
        //1. try to find project by descriptor
        NativeProject sourceProject = findProjectsByDescriptor(projects, sourceUnitDescriptor);
        //if it is NOT null we need to understand how the name of project is mapped
        if (sourceProject != null) {
            Collection<? extends NativeProjectRelocationMapperProvider> providers =
                    Lookup.getDefault().lookupAll(NativeProjectRelocationMapperProvider.class);
            for (NativeProjectRelocationMapperProvider provider : providers) {
                CharSequence destinationName = provider.getSourceProjectName(sourceProject);
                if (destinationName != null) {
                    return KeyUtilities.createUnitDescriptor(targetFileSystem, destinationName);
                }
            }
            //if not mapping required just create UnitDescriptor for target file system
            return new UnitDescriptor(sourceUnitDescriptor.getName(), targetFileSystem);
        }
        return null;
    }

    @Override
    public UnitDescriptor sourceDescriptor(FileSystem targetFileSystem, UnitDescriptor destinationDescriptor) {
        Collection<? extends NativeProjectRelocationMapperProvider> providers =
                Lookup.getDefault().lookupAll(NativeProjectRelocationMapperProvider.class);
        for (NativeProjectRelocationMapperProvider provider : providers) {
            NativeProject destinationProject = provider.findDestinationProject(destinationDescriptor.getName());
            if (destinationProject != null &&
                    destinationProject.getFileSystem().equals(targetFileSystem)) {
                return KeyUtilities.createUnitDescriptor(destinationProject);
            }
        }
        return new UnitDescriptor(destinationDescriptor.getName(), targetFileSystem);
    }

}
