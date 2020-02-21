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
package org.netbeans.modules.cnd.indexing.api;

/**
 *
 */
public final class CndTextIndexKey {
    private final int unitId;
    private final int fileNameIndex;

    public CndTextIndexKey(int unitId, int fileNameIndex) {
        this.unitId = unitId;
        this.fileNameIndex = fileNameIndex;
    }

    public int getUnitId() {
        return unitId;
    }

    public int getFileNameIndex() {
        return fileNameIndex;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.unitId;
        hash = 59 * hash + this.fileNameIndex;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CndTextIndexKey other = (CndTextIndexKey) obj;
        if (this.unitId != other.unitId) {
            return false;
        }
        if (this.fileNameIndex != other.fileNameIndex) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CndTextIndexKey{" + "unitId=" + unitId + ", fileNameIndex=" + fileNameIndex + '}'; //NOI18N
    }
}
