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

package org.netbeans.modules.debugger.jpda.truffle.source;

import org.netbeans.api.debugger.jpda.JPDADebugger;

/**
 * A script source position.
 */
public class SourcePosition {

    private final long id;
    private final Source src;
    private final int line;
    
    public SourcePosition(JPDADebugger debugger, long id, Source src, int line) {
        this.id = id;
        this.src = src;
        this.line = line;
    }

    public Source getSource() {
        return src;
    }
    
    public int getLine() {
        return line;
    }

}
