/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
