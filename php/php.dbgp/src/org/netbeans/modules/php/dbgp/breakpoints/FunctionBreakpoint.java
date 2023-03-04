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
package org.netbeans.modules.php.dbgp.breakpoints;

import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand;

/**
 * Represent breakpoint for method call and return types.
 *
 * @author ads
 *
 */
public class FunctionBreakpoint extends AbstractBreakpoint {
    private final Type myType;
    private final String myFunctionName;

    public enum Type {
        CALL(BrkpntSetCommand.Types.CALL),
        RETURN(BrkpntSetCommand.Types.RETURN);

        Type(BrkpntSetCommand.Types type) {
            myType = type;
        }

        public BrkpntSetCommand.Types getType() {
            return myType;
        }

        public static Type forType(BrkpntSetCommand.Types type) {
            Type[] types = Type.values();
            for (Type typ : types) {
                if (type == typ.getType()) {
                    return typ;
                }
            }
            return null;
        }

        public static Type forString(String str) {
            for (Type type : Type.values()) {
                if (type.toString().equals(str)) {
                    return type;
                }
            }
            return null;
        }
        private final BrkpntSetCommand.Types myType;

    }

    public FunctionBreakpoint(Type type, String functionName) {
        myType = type;
        myFunctionName = functionName;
    }

    public Type getType() {
        return myType;
    }

    public String getFunction() {
        return myFunctionName;
    }

    @Override
    public boolean isSessionRelated(DebugSession session) {
        return true;
    }

}
