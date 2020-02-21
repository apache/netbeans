/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
