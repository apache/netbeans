/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
