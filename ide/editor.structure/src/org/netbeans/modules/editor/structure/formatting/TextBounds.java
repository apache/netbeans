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
package org.netbeans.modules.editor.structure.formatting;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class TextBounds {

    private int absoluteStart; // start offset regardless of white spaces
    private int absoluteEnd; // end --
    private int startPos = -1;
    private int endPos = -1;
    private int startLine = -1;
    private int endLine = -1;

    public TextBounds(int absoluteStart, int absoluteEnd) {
        this.absoluteStart = absoluteStart;
        this.absoluteEnd = absoluteEnd;
    }

    public TextBounds(int absoluteStart, int absoluteEnd, int startPos, int endPos, int startLine, int endLine) {
        this.absoluteStart = absoluteStart;
        this.absoluteEnd = absoluteEnd;
        this.startPos = startPos;
        this.endPos = endPos;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public int getEndPos() {
        return endPos;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getAbsoluteEnd() {
        return absoluteEnd;
    }

    public int getAbsoluteStart() {
        return absoluteStart;
    }

    @Override
    public String toString() {
        return "pos " + startPos + "-" + endPos + ", lines " + startLine + "-" + endLine; //NOI18N
    }
}
