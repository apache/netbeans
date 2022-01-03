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

import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.CharSequences;

/**
 * A common ancestor for keys 
 * that are based on (project, file) pair
 */

/*package*/ abstract class ProjectFileNameBasedKey extends ProjectNameBasedKey {

    protected static final CharSequence NO_FILE = CharSequences.create("<No File Name>"); // NOI18N

    protected final int fileNameIndex;
    
    protected ProjectFileNameBasedKey(int unitID, int fileID) {
        super(unitID);
        this.fileNameIndex = fileID;
    }
    
    protected ProjectFileNameBasedKey(FileImpl file) {
        this(getFileUnitId(file), getFileNameId(file));
    }

    protected ProjectFileNameBasedKey(KeyDataPresentation presentation) {
        super(presentation);
        fileNameIndex = presentation.getFilePresentation();
    }

    static int getFileUnitId(FileImpl file) {
        // judging by #208877 null might occur here, although it's definitely wrong
        if (file == null) {
            CndUtils.assertUnconditional("Null file"); //NOI18N
            return -1;
        } else {
            return file.getUnitId();
        }
    }

    private static int getFileNameId(FileImpl file) {
        // extra check for #208877
        if (file == null) {
            CndUtils.assertUnconditional("Null file"); //NOI18N
            return -1;
        } else {
            return file.getFileId();
        }
    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        super.write(aStream);
        aStream.writeInt(fileNameIndex);
    }

    protected ProjectFileNameBasedKey(RepositoryDataInput aStream) throws IOException {
        super(aStream);
        this.fileNameIndex = aStream.readInt();
    }

    @Override
    public int hashCode() {
        return hashCode(getUnitId());
    }
    

    @Override
    public int hashCode(int unitID) {
        return 17*fileNameIndex + super.hashCode(unitID);
    }

    @Override
    public boolean equals(int thisUnitID, Key object, int objectUnitID) {
        if (!super.equals(thisUnitID, object, objectUnitID)) {
             return false;
         }
         ProjectFileNameBasedKey other = (ProjectFileNameBasedKey) object;
         return this.fileNameIndex == other.fileNameIndex;

    }

    /*package-local*/int getProjectFileIndex(){
        return fileNameIndex;
    }

    protected CharSequence getFileName() {
        return KeyUtilities.getFileNameById(getUnitId(), this.fileNameIndex);
    }

    /** A special safe method, mainly for toString / tracing */
    protected CharSequence getFileNameSafe() {
        return KeyUtilities.getFileNameByIdSafe(getUnitId(), this.fileNameIndex);
    }

    @Override
    public int getDepth() {
        assert super.getDepth() == 0;
        return 1;
    }

    @Override
    public CharSequence getAt(int level) {
        assert super.getDepth() == 0 && level < getDepth();
        return getFileName();
    }

    @Override
    public final int getFilePresentation() {
        return fileNameIndex;
    }
}
