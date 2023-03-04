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
package org.netbeans.modules.editor.lib2.view;

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Position;

/**
 *
 * @author Miloslav Metelka
 */
final class TestHighlight {
    
    public static TestHighlight create(Document doc, int startOffset, int endOffset, AttributeSet attrs) {
        assert (startOffset <= endOffset);
        Position startPos = ViewUtils.createPosition(doc, startOffset);
        Position endPos = ViewUtils.createPosition(doc, endOffset);
        return new TestHighlight(startPos, endPos, attrs);
    }
    
    public static TestHighlight create(Position startPos, Position endPos, AttributeSet attrs) {
        return new TestHighlight(startPos, endPos, attrs);
    }

    final Position startPos;
    
    final Position endPos;
    
    final AttributeSet attrs;
    
    private TestHighlight(Position startPos, Position endPos, AttributeSet attrs) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.attrs = attrs;
    }

    int startOffset() {
        return startPos.getOffset();
    }

    int endOffset() {
        return endPos.getOffset();
    }

    @Override
    public String toString() {
        return "<" + startPos.getOffset() + "," + endPos.getOffset() + ">; " + attrs;
    }
    
}
