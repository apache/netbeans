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

package org.netbeans.modules.debugger.jpda.truffle.vars;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;

/**
 * Representation of DebugScope.
 */
public final class TruffleScope {

    private final JPDADebugger debugger;
    private final String name;
    private final boolean function;
    private final ObjectVariable debugScope;
    private TruffleVariable[] arguments;
    private TruffleVariable[] variables;

    public TruffleScope(String name, boolean function, TruffleVariable[] arguments, TruffleVariable[] variables) {
        this.name = name;
        this.function = function;
        this.arguments = arguments;
        this.variables = variables;
        this.debugger = null;
        this.debugScope = null;
    }

    public TruffleScope(String name, boolean function, boolean hasArgs, boolean hasVars, JPDADebugger debugger, ObjectVariable debugScope) {
        this.name = name;
        this.function = function;
        if (!hasArgs) {
            arguments = new TruffleVariable[] {};
        }
        if (!hasVars) {
            variables = new TruffleVariable[] {};
        }
        this.debugger = debugger;
        this.debugScope = debugScope;
    }

    public String getName() {
        return name;
    }

    public boolean isFunction() {
        return function;
    }

    public synchronized TruffleVariable[] getArguments() {
        if (arguments == null) {
            loadArgsAndVars();
        }
        return arguments;
    }

    public synchronized TruffleVariable[] getVariables() {
        if (variables == null) {
            loadArgsAndVars();
        }
        return variables;
    }

    private void loadArgsAndVars() {
        assert Thread.holdsLock(this);
        TruffleVariable[][] argsAndVars = TruffleAccess.getScopeArgsAndVars(debugger, debugScope);
        arguments = argsAndVars[0];
        variables = argsAndVars[1];
    }
}
