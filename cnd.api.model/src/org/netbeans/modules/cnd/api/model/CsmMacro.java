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

package org.netbeans.modules.cnd.api.model;

import java.util.List;

/**
 * Represents C/C++ macros
 */
public interface CsmMacro extends CsmNamedElement, CsmOffsetable {

    List<CharSequence> getParameters();
    CsmParameterList<CsmMacroParameter> getParameterList();
    
    CharSequence getBody();
    
    /**
     * kind of macro
     */
    Kind getKind();

    public enum Kind {
        COMPILER_PREDEFINED, // compiler predefined macro, for example __STDC__
        POSITION_PREDEFINED, // predefined macro names changing it's value based on position in file __FILE__, __LINE__, ...
        USER_SPECIFIED, // macro defined in project properties or in command line with -D compile option
        DEFINED, // macro defined in code using #define directive
        INVALID // invalid macro
    }
}
