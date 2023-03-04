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

import java.util.HashSet;
import java.util.Set;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 *
 * @author Martin Entlicher
 */
public final class V8Script extends V8Value {
    
    private final String name;
    private final long id;
    private final long lineOffset;
    private final long columnOffset;
    private final long lineCount;
    private final Object data;
    private final String source;
    private final String sourceStart;
    private final PropertyLong sourceLength;
    private final ReferencedValue context;
    private final Type scriptType;
    private final CompilationType compilationType;
    private final ReferencedValue evalFromScript;
    private final EvalFromLocation evalFromLocation;
    
    public V8Script(long handle, String name, long id, long lineOffset, long columnOffset,
                    long lineCount, Object data, String source, String sourceStart,
                    Long sourceLength, ReferencedValue context, String text,
                    Type scriptType, CompilationType compilationType,
                    ReferencedValue evalFromScript, EvalFromLocation evalFromLocation) {
        super(handle, V8Value.Type.Script, text);
        this.name = name;
        this.id = id;
        this.lineOffset = lineOffset;
        this.columnOffset = columnOffset;
        this.lineCount = lineCount;
        this.data = data;
        this.source = source;
        this.sourceStart = sourceStart;
        this.sourceLength = new PropertyLong(sourceLength);
        this.context = context;
        this.scriptType = scriptType;
        this.compilationType = compilationType;
        this.evalFromScript = evalFromScript;
        this.evalFromLocation = evalFromLocation;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public long getLineOffset() {
        return lineOffset;
    }

    public long getColumnOffset() {
        return columnOffset;
    }

    public long getLineCount() {
        return lineCount;
    }

    public Object getData() {
        return data;
    }

    public String getSource() {
        return source;
    }

    public String getSourceStart() {
        return sourceStart;
    }

    public PropertyLong getSourceLength() {
        return sourceLength;
    }
    
    public ReferencedValue getContext() {
        return context;
    }
    
    public Type getScriptType() {
        return scriptType;
    }

    public CompilationType getCompilationType() {
        return compilationType;
    }

    public ReferencedValue getEvalFromScript() {
        return evalFromScript;
    }

    public EvalFromLocation getEvalFromLocation() {
        return evalFromLocation;
    }
    
    public static enum Type {
        NATIVE,
        EXTENSION,
        NORMAL;
        
        public static Type valueOf(int i) {
            if (i >= values().length) {
                return null;
            } else {
                return values()[i];
            }
        }
    }
    
    public static final class Types {
        
        private final int type;
        
        public Types(int type) {
            this.type = type;
        }
        
        public Types(boolean isNative, boolean isExtension, boolean isNormal) {
            this.type = (isNative ? 1 : 0) | (isExtension ? 2 : 0) | (isNormal ? 4 : 0);
        }

        public int getIntTypes() {
            return type;
        }
        
        public Set<Type> getTypes() {
            Set<Type> types = new HashSet<>();
            for (Type t : Type.values()) {
                if ((type & (0x1 << t.ordinal())) != 0) {
                    types.add(t);
                }
            }
            return types;
        }
    }
    
    public static enum CompilationType {
        
        API,
        EVAL;
        
        public static CompilationType valueOf(int i) {
            return CompilationType.values()[i];
        }
    }
    
    public static final class EvalFromLocation {
        
        private final long line;
        private final long column;
        
        public EvalFromLocation(long line, long column) {
            this.line = line;
            this.column = column;
        }

        public long getLine() {
            return line;
        }

        public long getColumn() {
            return column;
        }
    }
}
