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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.api.model.CsmOffsetable;

/**
 * offset based CsmOffsetable.Position implementation
 * do not keep reference to this object for a long time to prevent memory leaks
 */
public final class LazyOffsPositionImpl implements CsmOffsetable.Position {
    private int line = -1;
    private int col = -1;
    private final int offset;
    private final FileImpl file;
    
    public LazyOffsPositionImpl(FileImpl file, int offset) {
        this.offset = offset;
        this.file = file;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getLine() {
        if (line == -1) {
            int[] res = file.getLineColumn(offset);
            line = res[0];
            col = res[1];
        }
        return line;
    }

    @Override
    public int getColumn() {
        if (col == -1) {
            int[] res = file.getLineColumn(offset);
            line = res[0];
            col = res[1];
        }
        return col;
    }
    
    @Override
    public String toString() {
        int end = getOffset();
        return "" + getLine() + ':' + getColumn() + '/' + (end == Integer.MAX_VALUE ? "FILE_LENGTH" : end); // NOI18N
    }
}       
