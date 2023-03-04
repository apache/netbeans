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
package org.netbeans.lib.v8debug.events;

import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 *
 * @author Martin Entlicher
 */
public final class ExceptionEventBody extends V8Body {
    
    private final boolean uncaught;
    private final V8Value exception;
    private final long sourceLine;
    private final long sourceColumn;
    private final String sourceLineText;
    private final V8Script script;
    
    public ExceptionEventBody(boolean uncaught, V8Value exception,
                              long sourceLine, long sourceColumn,
                              String sourceLineText, V8Script script) {
        this.uncaught = uncaught;
        this.exception = exception;
        this.sourceLine = sourceLine;
        this.sourceColumn = sourceColumn;
        this.sourceLineText = sourceLineText;
        this.script = script;
    }

    public boolean isUncaught() {
        return uncaught;
    }

    public V8Value getException() {
        return exception;
    }

    public long getSourceLine() {
        return sourceLine;
    }

    public long getSourceColumn() {
        return sourceColumn;
    }

    public String getSourceLineText() {
        return sourceLineText;
    }

    public V8Script getScript() {
        return script;
    }
}
