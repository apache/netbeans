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
package org.netbeans.modules.cnd.apt.impl.support;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;

/**
 * iterator which encapsulates two lists ans start index of combined collection
 */
final class PathsCollectionIterator implements Iterator<IncludeDirEntry> {
    private final List<IncludeDirEntry> col1;
    private final List<IncludeDirEntry> col2;
    private int startIndex;
    private final int size1;
    private final int size;
    
    public PathsCollectionIterator(List<IncludeDirEntry> col1, List<IncludeDirEntry> col2, int startIndex) {
        this.col1 = col1;
        this.size1 = col1.size();
        this.col2 = col2;
        this.size = size1 + col2.size();
        this.startIndex = startIndex;
    }

    @Override
    public boolean hasNext() {
        return startIndex < size;
    }

    @Override
    public IncludeDirEntry next() {
        if (hasNext()) {
            int index = startIndex++;
            if (index < size1) {
                return col1.get(index);
            } else {
                return col2.get(index - size1);
            }
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported."); // NOI18N
    }
}
