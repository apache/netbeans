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
package org.netbeans.modules.cnd.api.toolchain;

import org.openide.util.NbBundle;

public enum PredefinedToolKind implements ToolKind {
    CCompiler, //0
    CCCompiler, //1
    FortranCompiler, //2
    CustomTool, //3
    Assembler, //4
    MakeTool, //5
    DebuggerTool, //6
    QMakeTool, //7
    CMakeTool, //8
    UnknownTool; //9
    
    @Override
    public String getDisplayName(){
        return NbBundle.getBundle(PredefinedToolKind.class).getString(name());
    }

    public static PredefinedToolKind getTool(int ordinal){
        for (PredefinedToolKind tool : PredefinedToolKind.values()){
            if (tool.ordinal() == ordinal) {
                return tool;
            }
        }
        return UnknownTool;
    }

    public static PredefinedToolKind getTool(String name){
        for (PredefinedToolKind tool : PredefinedToolKind.values()){
            if (tool.getDisplayName().equals(name)) {
                return tool;
            }
        }
        return UnknownTool;
    }

}
