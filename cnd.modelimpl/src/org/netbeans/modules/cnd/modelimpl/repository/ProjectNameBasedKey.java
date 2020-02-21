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
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.CharSequences;

/**
 * A common ancestor for nearly all keys 
 */

/*package*/ abstract class ProjectNameBasedKey extends AbstractKey {

    private final int unitIndex;
    
    /*package*/ static final CharSequence NO_PROJECT = CharSequences.create("<No Project Name>"); // NOI18N

    protected ProjectNameBasedKey(int unitIndex) {
        this.unitIndex = unitIndex;
        CndUtils.assertTrue(this.unitIndex > 10000, "Impossible unit index: ", unitIndex); //NOI18N
    }

    protected ProjectNameBasedKey(KeyDataPresentation presentation) {
        unitIndex = presentation.getUnitPresentation();
        CndUtils.assertTrue(this.unitIndex > 10000, "Impossible unit index: ", unitIndex); //NOI18N
    }

    @Override
    public String toString() {
        return getProjectName().toString();
    }

    @Override
    public int hashCode(int unitID) {
        return 37*getHandler() + unitID;
    }

    @Override
    public int hashCode() {
        return hashCode(unitIndex);
    }

    @Override
    public final int getUnitId() {
        return unitIndex;
    }

   @Override
    public boolean equals(int thisUnitID, Key object, int objectUnitID) {
        if (this == object) {
            return true;
        }
        if (object == null || (this.getClass() != object.getClass())) {
            return false;
        }
        return thisUnitID == objectUnitID;
    }


    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || (this.getClass() != obj.getClass())) {
             return false;
         }
        ProjectNameBasedKey other = (ProjectNameBasedKey) obj;
        return equals(unitIndex, other, other.unitIndex);

    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        aStream.writeUnitId(this.unitIndex);
    }

    protected ProjectNameBasedKey(RepositoryDataInput aStream) throws IOException {
        this.unitIndex = aStream.readUnitId();
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public CharSequence getAt(int level) {
        throw new UnsupportedOperationException();
    }

    protected CharSequence getProjectName() {
        return KeyUtilities.getUnitNameSafe(this.unitIndex);
    }

    @Override
    public CharSequence getUnit() {
        if (this.unitIndex < 0) {
            return NO_PROJECT;
        }
        // having this functionality here to be sure unit is the same thing as project
        return KeyUtilities.getUnitName(this.unitIndex);
    }

    @Override
    public final int getUnitPresentation() {
        return unitIndex;
    }
}
