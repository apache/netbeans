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

package org.netbeans.modules.debugger.jpda.truffle.vars;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.truffle.LanguageName;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleVariableImpl;

/**
 * Representation of <code>DebugValue</code>.
 */
public interface TruffleVariable {
    
    String getName();

    LanguageName getLanguage();

    String getType();

    boolean isReadable();
    
    boolean isWritable();
    
    boolean isInternal();
    
    Object getValue();
    
    String getDisplayValue();
    
    boolean hasValueSource();
    
    SourcePosition getValueSource();
    
    boolean hasTypeSource();
    
    SourcePosition getTypeSource();
    
    boolean isLeaf();
    
    Object[] getChildren();

    ObjectVariable setValue(JPDADebugger debugger, String newExpression);

    default boolean isReceiver() {
        return false;
    }

    public static TruffleVariable get(Variable var) {
        return TruffleVariableImpl.get(var);
    }
}
