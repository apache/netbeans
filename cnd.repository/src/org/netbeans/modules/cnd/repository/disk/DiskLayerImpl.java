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
package org.netbeans.modules.cnd.repository.disk;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.impl.spi.ReadLayerCapability;
import org.netbeans.modules.cnd.repository.impl.spi.UnitDescriptorsList;
import org.netbeans.modules.cnd.repository.impl.spi.WriteLayerCapability;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public final class DiskLayerImpl {

    private final FilesAccessStrategyImpl fas;
    private final LayerDescriptor layerDescriptor;
    private final LayerIndex layerIndex;

    public DiskLayerImpl(LayerDescriptor layerDescriptor, LayeringSupport layeringSupport) {
        this.layerDescriptor = layerDescriptor;
        URI cacheDirectory = layerDescriptor.getURI();
        layerIndex = new LayerIndex(cacheDirectory);
        fas = new FilesAccessStrategyImpl(layerIndex, cacheDirectory, layerDescriptor, layeringSupport);
    }
        
    

    public Collection<LayerKey> removedTableKeySet() {
        return fas.removedTableKeySet();
    }
        
    
    public boolean startup(int persistMechanismVersion, boolean recreate, boolean isWritable) {
        return layerIndex.load(persistMechanismVersion, recreate, isWritable);
    }

    public LayerDescriptor getLayerDescriptor() {
        return layerDescriptor;
    }

    public void shutdown() {
        fas.shutdown(layerDescriptor.isWritable());
    }

    public void openUnit(int unitIdx) {
        //does  nothing now
    }

    public void closeUnit(int unitIdx, boolean cleanRepository, Set<Integer> requiredUnits) {
        throw new InternalError();
    }

    void closeUnit(int unitIdInLayer, boolean cleanRepository, Set<Integer> requiredUnits, boolean isWritable) {
        layerIndex.closeUnit(unitIdInLayer, cleanRepository, requiredUnits);
        fas.closeUnit(unitIdInLayer, cleanRepository);
    }


    public UnitDescriptorsList getUnitsTable() {
        return layerIndex.getUnitsTable();
    }

    public List<FileSystem> getFileSystemsTable() {
        return layerIndex.getFileSystemsTable();
    }

    public ReadLayerCapability getReadCapability() {
        return fas;
    }

    public WriteLayerCapability getWriteCapability() {
        return fas;
    }

    /**
     * Only layer can do this matching...
     *
     * (i.e. RMI layer may know that enum:/tmp on client side should be treated
     * as 'localhost' on enum... )
     *
     * @param clientFileSystem
     * @return
     */
    public int findMatchedFileSystemIndexInLayer(FileSystem clientFileSystem) {
        return layerIndex.getFileSystemsTable().indexOf(clientFileSystem);
    }

    public void storeIndex() {
        try {
            layerIndex.store();
        } catch (IOException ex) {
            RepositoryExceptions.throwException(this, ex);
        }
    }
}
