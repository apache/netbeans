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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.UnitDescriptorsList;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.Utilities;

/**
 *
 */
public final class LayerIndex {

    private final File cacheDirectoryFile;
    private int version;
    private static final String INDEX_FILE_NAME = "index";//NOI18N
    private long lastModificationTime;
    private final List<FileSystem> fileSystems = new ArrayList<FileSystem>();
    private final UnitDescriptorsListImpl units = new UnitDescriptorsListImpl();
    private final Map<Integer, List<Integer>> dependencies = new HashMap<Integer, List<Integer>>();

    LayerIndex(URI cacheDirectory) {
        this.cacheDirectoryFile = Utilities.toFile(cacheDirectory);
    }

    boolean load(int persistMechanismVersion, boolean recreate, boolean recreateOnFail) {
        this.version = persistMechanismVersion;

        File indexFile = new File(cacheDirectoryFile, INDEX_FILE_NAME); 
        if (recreate) {
            return clearDataImpl();
        }

        // If no index file - it's OK.
        if (!indexFile.exists()) {
            //it is not OK if there is a content in this repository but index doesn't exist
            //as in this case we will have broken tables for filsystems and units
            //so let's just delete the content of the cache, otherwise we will get a bunch of exceptions
            if (recreateOnFail) {
                return clearDataImpl();
            }
        }

        DataInputStream in = null;
        try {
            in = RepositoryImplUtil.getBufferedDataInputStream(indexFile);
            int storedVersion = in.readInt();
            if (storedVersion != persistMechanismVersion) {
                if (recreateOnFail) {
                    return clearDataImpl();
                }                
                return false;
            }
            lastModificationTime = in.readLong();
            int unitsCount = in.readInt();
            int unitsMax = in.readInt();
            units.setMaxValue(unitsMax);
            int fileSystemsCount = in.readInt();

            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                trace("Reading master index (%d) units\n", unitsCount); // NOI18N
                trace("Reading master index (%d) fileSystems\n", fileSystemsCount); // NOI18N
            }

            // Read list of filesystems
            for (int i = 0; i < fileSystemsCount; i++) {
                String fs = in.readUTF();
                fileSystems.add(CndFileUtils.decodeFileSystem(fs));
            }

            for (int i = 0; i < unitsCount; i++) {
                int unitID = in.readInt();
                String unitName = in.readUTF();
                int fsIdx = in.readInt();
                long unitModificationTime = in.readLong();
                if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                    trace("\tRead %d@%s@%s ts=%d\n", unitID, unitName, fileSystems.get(fsIdx), unitModificationTime); // NOI18N
                } 
                units.addUnitDescriptor(unitID, new UnitDescriptor(unitName, fileSystems.get(fsIdx)));
                List<Integer> depList = new ArrayList<Integer>();
                int depListSize = in.readInt();
                for (int j = 0; j < depListSize; j++) {
                    unitID = in.readInt();
                    long ts = in.readLong();
                    // TODO: timestamp for validation!
                    depList.add(unitID);
                }

                dependencies.put(unitID, depList);
            }
            // make sure all is consistent
            if (checkConsistency()) {
                return true;
            }
            CndUtils.assertTrueInConsole(false, "corrupted load of repository index", this.toString());
        } catch (IOException ex) {
            RepositoryExceptions.throwException(this, ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    RepositoryExceptions.throwException(this, ex);
                }
            }
            if (recreateOnFail) {
                //need to delete the index file, next time IDE will be started  we will understand if it will not be 
                //saved on disk that repository is broken
                if (!indexFile.delete()) {
                    System.err.println("Cannot delete repository index File " + indexFile.getAbsolutePath()); 
                }
            }
        }

        if (recreateOnFail) {
            return clearDataImpl();
        }

        return false;
    }

    private boolean clearDataImpl() {
        if (cacheDirectoryFile.canWrite()) {
            RepositoryImplUtil.deleteDirectory(cacheDirectoryFile, false);
            fileSystems.clear();
            units.clear();
            dependencies.clear();
            return true;
        }
        return false;
    }

    void store() throws IOException {
        final long currentTime = System.currentTimeMillis();
        File indexFile = new File(cacheDirectoryFile, INDEX_FILE_NAME); 
        DataOutputStream out = null;
        try {
            out = RepositoryImplUtil.getBufferedDataOutputStream(indexFile);
            out.writeInt(version);
            out.writeLong(currentTime);
            Map<UnitDescriptor, Integer> map = units.getMap();
            int unitsCount = map.size();
            //ned to know how many records we have
            out.writeInt(unitsCount);
            out.writeInt(units.getMaxValue());

            int fileSystemsCount = fileSystems.size();
            out.writeInt(fileSystemsCount);

            // Read list of filesystems
            for (int i = 0; i < fileSystemsCount; i++) {
                out.writeUTF(CndFileUtils.codeFileSystem(fileSystems.get(i)).toString());
            }

            for (Map.Entry<UnitDescriptor, Integer> entry : map.entrySet()) {
                int unitID = entry.getValue();
                UnitDescriptor ud = entry.getKey();
                out.writeInt(unitID);
                out.writeUTF(ud.getName().toString());
                out.writeInt(fileSystems.indexOf(ud.getFileSystem()));

                out.writeLong(currentTime); // TODO: This is wrong ;)

                List<Integer> depMap = dependencies.get(unitID);
                out.writeInt(depMap == null ? 0 : depMap.size());
                if (depMap != null) {
                    for (Integer depID : depMap) {
                        out.writeInt(depID);
                        out.writeLong(currentTime);
                    }
                }
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private void trace(String format, Object... args) {
        Object[] newArgs = new Object[args.length + 1];
        newArgs[0] = Long.valueOf(System.currentTimeMillis());
        System.arraycopy(args, 0, newArgs, 1, args.length);
        System.err.printf("MasterIndex [%d] " + format, newArgs);
    }

    UnitDescriptorsList getUnitsTable() {
        return units;
    }

    List<FileSystem> getFileSystemsTable() {
        return Collections.unmodifiableList(fileSystems);
    }


    int registerUnit(UnitDescriptor unitDescriptor) {
        int unitID = units.registerNewUnitDescriptor(unitDescriptor);
        return unitID;
    }

    void removeUnit(int unitIDInLayer) {
        //DO NOTHING HERE, we will not remove unit ever frim the list because of preproc state
//        if (!units.getUnitIDs().contains(unitIDInLayer)) {
//            return;
//        }
//        units.remove(unitIDInLayer);
    }


    int registerFileSystem(FileSystem fileSystem) {
        synchronized (fileSystems) {
            int index = fileSystems.indexOf(fileSystem);
            if (index == -1) {
                index = fileSystems.size();
                fileSystems.add(fileSystem);
            }
            return index;
        }
    }

    void closeUnit(int unitIdx, boolean cleanRepository, Set<Integer> requiredUnits) {
        dependencies.put(unitIdx, requiredUnits == null ? new ArrayList<Integer>() : new ArrayList<Integer>(requiredUnits));
    }

    private boolean checkConsistency() {
        Collection<Integer> unitIDs = units.getUnitIDs();
        for (Integer unit : unitIDs) {
            if (unit == null || unit < 0) {
                return false;
            }
            for (Integer dep : dependencies.get(unit)) {
                if (dep == null || dep < 0) {
                    return false;
                }
                if (!unitIDs.contains(dep)) {
                    return false;
                }
            }
        }
        for (FileSystem fileSystem : fileSystems) {
            if (fileSystem == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "LayerIndex{" + "cacheDirectoryFile=" + cacheDirectoryFile + //NOI18N
                "\n, version=" + version + ", lastModificationTime=" + lastModificationTime + //NOI18N
                "\n, fileSystems=" + fileSystems + //NOI18N
                "\n, units=" + units + //NOI18N
                "\n, dependencies=" + dependencies + //NOI18N
                '}';//NOI18N
    }
    
    
}
