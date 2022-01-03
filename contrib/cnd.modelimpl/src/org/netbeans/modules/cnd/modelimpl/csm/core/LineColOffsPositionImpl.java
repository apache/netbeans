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

import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * (line, col, offset) based CsmOffsetable.Position implementation
 */
public class LineColOffsPositionImpl implements CsmOffsetable.Position {
    private final int line;
    private final int col;
    private final int offset;

    public LineColOffsPositionImpl() {
        this(0,0,0);
    }

    public LineColOffsPositionImpl(CsmOffsetable.Position pos) {
        if (pos != null) {
            this.line = pos.getLine();
            this.col = pos.getColumn();
            this.offset = pos.getOffset();            
        } else {
            this.line = 0;
            this.col = 0;
            this.offset = 0;
        }
    }
    
    public LineColOffsPositionImpl(int line, int col, int offset) {
        this.line = line;
        this.col = col;
        this.offset = offset;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return col;
    }
    
    @Override
    public String toString() {
        return "" + getLine() + ':' + getColumn() + '/' + getOffset();
    }

    /* package */ void toStream(RepositoryDataOutput output) throws IOException {
        output.writeInt(line);
        output.writeInt(col);
        output.writeInt(offset);
    }

    /* package */ LineColOffsPositionImpl(RepositoryDataInput input) throws IOException {
        line = input.readInt();
        col = input.readInt();
        offset = input.readInt();
    }
}       
