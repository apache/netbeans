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

package org.netbeans.modules.cnd.apt.support;

import org.netbeans.modules.cnd.antlr.TokenStream;
import java.util.Collection;
import org.netbeans.modules.cnd.apt.structure.APTDefine;

/**
 * macros for APT macro map
 */
public interface APTMacro {
    public Kind getKind();
    public boolean isFunctionLike();
    public APTToken getName();
    public Collection<APTToken> getParams();
    public TokenStream getBody();
    public CharSequence getFile(); // macro defined in code using #define directive has file information
    public APTDefine getDefineNode(); // macro defined in code using #define directive has this information
    
    public enum Kind {
        COMPILER_PREDEFINED, // compiler predefined macro, for example __STDC__
        POSITION_PREDEFINED, // predefined macro names changing it's value based on position in file __FILE__, __LINE__, ...
        USER_SPECIFIED, // macro defined in project properties or in command line with -D compile option
        DEFINED // macro defined in code using #define directive
    }
}
