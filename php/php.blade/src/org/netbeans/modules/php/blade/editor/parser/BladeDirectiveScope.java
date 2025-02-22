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
package org.netbeans.modules.php.blade.editor.parser;

import java.util.HashSet;
import java.util.Set;
/**
 *
 * @author bogdan
 */
public class BladeDirectiveScope {

    private final int bladeAntlrTokenType;
    private final Set<String> variables = new HashSet<>();
    private BladeDirectiveScope child;

    public BladeDirectiveScope(int tokenType) {
        this.bladeAntlrTokenType = tokenType;
    }

    public void addVariable(String varName) {
        variables.add(varName);
    }

    public Set<String> getScopeVariables() {
        return variables;
    }

    public int getScopeType() {
        return bladeAntlrTokenType;
    }

    public void setChild(BladeDirectiveScope child) {
        this.child = child;
    }

    public BladeDirectiveScope getChild() {
        return child;
    }
}