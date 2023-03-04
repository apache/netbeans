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
package org.netbeans.modules.cpplite.debugger;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.netbeans.modules.cnd.debugger.gdb2.mi.MIConst;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;
import org.netbeans.modules.nativeimage.api.debug.NIFrame;
import org.netbeans.modules.nativeimage.api.debug.NIVariable;

/**
 * Representation of a variable.
 */
public final class CPPVariable implements NIVariable {

    private final CPPFrame frame;
    private final CPPVariable parentVariable;
    private final String uniqueName;
    private final String name;
    private final String type;
    private final String value;
    private final int numChildren;
    private volatile Map<String, NIVariable> children;

    CPPVariable(CPPFrame frame, CPPVariable parentVariable, String uniqueName, String name, String type, MIValue value, int numChildren) {
        this.frame = frame;
        this.parentVariable = parentVariable;
        this.uniqueName = uniqueName;
        this.name = name;
        this.type = type;
        this.value = (value instanceof MIConst) ? ((MIConst) value).value() : Objects.toString(value);
        this.numChildren = numChildren;
    }

    @Override
    public NIFrame getFrame() {
        return frame.getFrame();
    }

    @Override
    public CPPVariable getParent() {
        return parentVariable;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int getNumChildren() {
        return numChildren;
    }

    public Map<String, NIVariable> getChildrenByNames() {
        Map<String, NIVariable> vars = children;
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

    @Override
    public NIVariable[] getChildren(int from, int to) {
        Map<String, NIVariable> childrenVariables = getChildrenByNames();
        NIVariable[] array = childrenVariables.values().toArray(new NIVariable[0]);
        if (array.length == 1 && array[0] == null) {
            return new NIVariable[0]; // Error
        }
        if (from >= 0) {
            to = Math.min(to, array.length);
            if (from < to) {
                array = Arrays.copyOfRange(array, from, to);
            } else {
                array = new NIVariable[0];
            }
        }
        return array;
    }

    @Override
    public String getExpressionPath() {
        MIRecord pathRecord;
        try {
            pathRecord = frame.getThread().getDebugger().sendAndGet("-var-info-path-expression \"" + uniqueName + "\"");
        } catch (InterruptedException ex) {
            return null;
        }
        String pathExpression = pathRecord.results().getConstValue("path_expr");
        return pathExpression;
    }

}
