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
package org.netbeans.modules.editor.lib2.document;

import javax.swing.text.Position;


/**
 * Implementation of a complex position. The clients should only use methods
 * in {@link org.netbeans.api.editor.document.ComplexPositions} and never check
 * for an instance of this particular implementation class.
 *
 * @author Miloslav Metelka
 */
public final class ComplexPos implements Position {

    private final Position pos;
    
    private final int splitOffset;

    public ComplexPos(Position pos, int splitOffset) {
        this.pos = pos;
        this.splitOffset = splitOffset;
    }
    
    public ComplexPos(ComplexPos complexPos, int splitOffset) {
        this.pos = complexPos.pos;
        this.splitOffset = complexPos.splitOffset + splitOffset;
    }

    public Position getPosition() {
        return pos;
    }
    
    public int getSplitOffset() {
        return splitOffset;
    }

    @Override
    public int getOffset() {
        return pos.getOffset();
    }

}
