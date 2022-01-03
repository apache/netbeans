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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Trivial CsmOffsetable implementation
 */
public class SimpleOffsetableImpl implements CsmOffsetable {
    
        private LineColOffsPositionImpl stPos = null;
        private LineColOffsPositionImpl endPos = null;

        public SimpleOffsetableImpl(int line, int col, int offset) {
            stPos = new LineColOffsPositionImpl(line, col, offset);
        }

        public SimpleOffsetableImpl(CsmOffsetable offsetable) {
            stPos = new LineColOffsPositionImpl(offsetable.getStartPosition());
            endPos = new LineColOffsPositionImpl(offsetable.getEndPosition());
        }

        public void setEndPosition(Position startPosition) {
            endPos = new LineColOffsPositionImpl(startPosition);
        }
        
        public void setEndPosition(int line, int col, int offset) {
            endPos = new LineColOffsPositionImpl(line, col, offset);
        }
    
    @Override
        public CsmFile getContainingFile() {
            return null;
        }

    @Override
        public int getStartOffset() {
            return stPos.getOffset();
        }

    @Override
        public int getEndOffset() {
            return endPos.getOffset();
        }

    @Override
        public CsmOffsetable.Position getStartPosition() {
            return stPos;
        }

    @Override
        public CsmOffsetable.Position getEndPosition() {
            return endPos;
        }    

    @Override
        public CharSequence getText() {
            return null;
        }

        protected SimpleOffsetableImpl(RepositoryDataInput input) throws IOException {
            stPos = new LineColOffsPositionImpl(input.readInt(), input.readInt(), input.readInt());
            endPos = new LineColOffsPositionImpl(input.readInt(), input.readInt(), input.readInt());
        }

        protected void write(RepositoryDataOutput output) throws IOException {
            output.writeInt(stPos.getLine());
            output.writeInt(stPos.getColumn());
            output.writeInt(stPos.getOffset());
            output.writeInt(endPos.getLine());
            output.writeInt(endPos.getColumn());
            output.writeInt(endPos.getOffset());
        }
}
