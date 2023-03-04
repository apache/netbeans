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
package org.netbeans.lib.v8debug;

/**
 *
 * @author Martin Entlicher
 */
public final class V8ScriptLocation {
    
    private final long id;
    private final String name;
    private final long line;
    private final long column;
    private final long lineCount;
    
    public V8ScriptLocation(long id, String name, long line, long column, long lineCount) {
        this.id = id;
        this.name = name;
        this.line = line;
        this.column = column;
        this.lineCount = lineCount;
    }
    
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getLine() {
        return line;
    }

    public long getColumn() {
        return column;
    }

    public long getLineCount() {
        return lineCount;
    }
    
}
