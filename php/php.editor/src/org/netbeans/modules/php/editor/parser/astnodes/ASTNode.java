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
package org.netbeans.modules.php.editor.parser.astnodes;

/**
 *
 * @author petr
 */
public abstract class ASTNode {

    private int startOffset;
    private int endOffset;
    //private ASTNode parent = null;

    public ASTNode(int start, int end) {
        assert start >= 0;
        assert end >= start;

        this.startOffset = start;
        this.endOffset = end;
    }

    public final int getStartOffset() {
        return startOffset;
    }

    public final int getEndOffset() {
        return endOffset;
    }

    public final void setSourceRange(int startOffset, int endOffset) {
        if (startOffset >= 0 && endOffset < 0) {
            throw new IllegalArgumentException();
        }
        if (startOffset < 0 && endOffset != 0) {
            throw new IllegalArgumentException();
        }
        assert startOffset >= 0;
        assert endOffset >= startOffset;

        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public abstract void accept(Visitor visitor);
}
