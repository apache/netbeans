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

package org.netbeans.modules.cnd.asm.base.dis;

import org.netbeans.modules.cnd.asm.base.att.ATTParser;


class DisFuncNameDetector implements ATTParser.FunctionNameDetector {

    private static final ATTParser.FunctionNameDetector instance =
            new DisFuncNameDetector();
        
    
    public static ATTParser.FunctionNameDetector getInstance() {
        return instance;
    }
    
    private DisFuncNameDetector() { }
            
    public boolean isFunctionLabel(String name) {
        return name.endsWith("()");  // NOI18N
    }

    public String getCleanName(String name) {
        if (name.endsWith("()")) {   // NOI18N
            return name.substring(0, name.length() - 2);
        }
        else if(name.endsWith(":")) { // NOI18N
            return name.substring(0, name.length() - 1);
        }
        return name;
    }                 
}
