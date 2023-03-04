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

package org.netbeans.modules.debugger.jpda.truffle.source;

import org.netbeans.api.debugger.jpda.JPDADebugger;

/**
 * A script source position.
 */
public class SourcePosition {

    private final long id;
    private final Source src;
    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;
    
    public SourcePosition(JPDADebugger debugger, long id, Source src, String sourceSection) {
        this.id = id;
        this.src = src;
        int i1 = 0, i2 = sourceSection.indexOf(',');
        this.startLine = Integer.parseInt(sourceSection.substring(i1, i2));
        i1 = i2 + 1;
        i2 = sourceSection.indexOf(',', i1);
        this.startColumn = Integer.parseInt(sourceSection.substring(i1, i2));
        i1 = i2 + 1;
        i2 = sourceSection.indexOf(',', i1);
        this.endLine = Integer.parseInt(sourceSection.substring(i1, i2));
        i1 = i2 + 1;
        this.endColumn = Integer.parseInt(sourceSection.substring(i1));
    }

    public Source getSource() {
        return src;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }
    
}
