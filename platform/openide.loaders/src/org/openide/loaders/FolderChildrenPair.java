/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.loaders;

import org.openide.filesystems.FileObject;

/**
 * Pair of dataobject invalidation sequence # and primary file.
 * It serves as a key for the given data object.
 * It is here to create something different then data object,
 * because the data object should be finalized when not needed and
 * that is why it should not be used as a key.
 */
final class FolderChildrenPair extends Object {

    public final FileObject primaryFile;
    public final int seq;

    FolderChildrenPair(FileObject primaryFile) {
        super();
        this.primaryFile = primaryFile;
        this.seq = DataObjectPool.getPOOL().registrationCount(primaryFile);
    }

    @Override
    public int hashCode() {
        return primaryFile.hashCode() ^ seq;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FolderChildrenPair) {
            FolderChildrenPair p = (FolderChildrenPair) o;
            if (!primaryFile.equals(p.primaryFile)) {
                return false;
            }
            if (seq == -1 || p.seq == -1) {
                return true;
            }
            return seq == p.seq;
        }
        return false;
    }

    @Override
    public String toString() {
        return "FolderChildren.Pair[" + primaryFile + "," + seq + "]";
    }
}
