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


package org.netbeans.modules.cnd.asm.base;

import org.netbeans.modules.cnd.asm.model.AsmModelProvider;

public class X86ModelProvider extends BaseModelProvider {
     
    private static AsmModelProvider instance;
    
    public static synchronized AsmModelProvider getInstance() {
        if (instance == null) {
            instance = new X86ModelProvider();
        }
        return instance;
    }
    
    private X86ModelProvider() {   
        super("amd64.xml"); // NOI18N
    }
                
    @Override
    public String toString() {
        return "X86"; // NOI18N
    }
}

