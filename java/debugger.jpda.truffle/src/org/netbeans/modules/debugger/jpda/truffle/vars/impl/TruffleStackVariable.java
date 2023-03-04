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

package org.netbeans.modules.debugger.jpda.truffle.vars.impl;

import java.util.function.Supplier;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.truffle.LanguageName;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;

public class TruffleStackVariable implements TruffleVariable {
    
    private final JPDADebugger debugger;
    private final String name;
    private final LanguageName language;
    private String type;
    private final boolean readable;
    private final boolean writable;
    private final boolean internal;
    private String valueStr;
    private boolean hasValueSource;
    private boolean hasTypeSource;
    private Supplier<SourcePosition> valueSourceSupp;
    private Supplier<SourcePosition> typeSourceSupp;
    private SourcePosition valueSource;
    private SourcePosition typeSource;
    private ObjectVariable guestObj;
    private final boolean isReceiver;
    private boolean leaf;
    
    public TruffleStackVariable(JPDADebugger debugger, String name, LanguageName language,
                                String type, boolean readable, boolean writable, boolean internal,
                                String valueStr, boolean hasValueSource, Supplier<SourcePosition> valueSource,
                                boolean hasTypeSource, Supplier<SourcePosition> typeSource,
                                boolean isReceiver, ObjectVariable truffleObj) {
        this.debugger = debugger;
        this.name = name;
        this.language = language;
        this.type = type;
        this.readable = readable;
        this.writable = writable;
        this.internal = internal;
        this.valueStr = valueStr;
        this.hasValueSource = hasValueSource;
        this.hasTypeSource = hasTypeSource;
        this.valueSourceSupp = valueSource;
        this.typeSourceSupp = typeSource;
        this.guestObj = truffleObj;
        this.isReceiver = isReceiver;
        this.leaf = TruffleVariableImpl.isLeaf(truffleObj);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LanguageName getLanguage() {
        return language;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isReadable() {
        return readable;
    }

    @Override
    public boolean isWritable() {
        return writable;
    }

    @Override
    public boolean isInternal() {
        return internal;
    }
    
    @Override
    public Object getValue() {
        return valueStr;
    }

    @Override
    public String getDisplayValue() {
        return valueStr;
    }

    @Override
    public ObjectVariable setValue(JPDADebugger debugger, String newExpression) {
        if (this.valueStr.equals(newExpression)) {
            return null;
        }
        ObjectVariable newGuestObject = TruffleVariableImpl.setValue(debugger, guestObj, newExpression);
        if (newGuestObject != null) {
            this.guestObj = newGuestObject;
            TruffleVariable newVar = TruffleVariableImpl.get(newGuestObject);
            this.type = newVar.getType();
            this.valueStr = newVar.getValue().toString();
            this.valueSource = this.typeSource = null;
            this.hasValueSource = newVar.hasValueSource();
            this.hasTypeSource = newVar.hasTypeSource();
            this.valueSourceSupp = () -> newVar.getValueSource();
            this.typeSourceSupp = () -> newVar.getTypeSource();
            this.leaf = TruffleVariableImpl.isLeaf(guestObj);
        }
        return newGuestObject;
    }

    @Override
    public boolean hasValueSource() {
        return hasValueSource;
    }

    @Override
    public synchronized SourcePosition getValueSource() {
        if (valueSource == null) {
            valueSource = valueSourceSupp.get();
        }
        return valueSource;
    }

    @Override
    public boolean hasTypeSource() {
        return hasTypeSource;
    }

    @Override
    public synchronized SourcePosition getTypeSource() {
        if (typeSource == null) {
            typeSource = typeSourceSupp.get();
        }
        return typeSource;
    }
    
    @Override
    public boolean isLeaf() {
        return leaf;
    }
    
    @Override
    public Object[] getChildren() {
        return TruffleVariableImpl.getChildren(guestObj);
    }

    @Override
    public boolean isReceiver() {
        return isReceiver;
    }
}
