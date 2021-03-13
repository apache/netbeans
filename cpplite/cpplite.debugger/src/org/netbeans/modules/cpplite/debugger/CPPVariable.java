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
package org.netbeans.modules.cpplite.debugger;

import java.util.Map;
import java.util.Objects;

import org.netbeans.modules.cnd.debugger.gdb2.mi.MIConst;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;

/**
 * Representation of a variable.
 */
public final class CPPVariable {

    private final CPPFrame frame;
    private final String uniqueName;
    private final String name;
    private final String type;
    private final String value;
    private final int numChildren;
    private volatile Map<String, CPPVariable> children;

    CPPVariable(CPPFrame frame, String uniqueName, String name, String type, MIValue value, int numChildren) {
        this.frame = frame;
        this.uniqueName = uniqueName;
        this.name = name;
        this.type = type;
        this.value = (value instanceof MIConst) ? ((MIConst) value).value() : Objects.toString(value);
        this.numChildren = numChildren;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getNumChildren() {
        return numChildren;
    }

    public Map<String, CPPVariable> getChildrenVariables() {
        Map<String, CPPVariable> vars = children;
        if (vars == null) {
            synchronized (this) {
                vars = children;
                if (vars == null) {
                    children = vars = CPPFrame.retrieveVariables(frame, this);
                }
            }
        }
        return vars;
    }
}
