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
package org.netbeans.modules.cnd.diagnostics.clank.impl;

import org.clang.tools.services.ClankDiagnosticInfo;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.diagnostics.clank.ClankCsmErrorInfo;

/**
 *
 */
abstract public class ClankCsmErrorInfoAccessor {
    private static volatile ClankCsmErrorInfoAccessor DEFAULT;

    public static ClankCsmErrorInfoAccessor getDefault() {
        ClankCsmErrorInfoAccessor a = DEFAULT;
        if (a != null) {
            return a;
        }

        try {
            Class.forName(ClankCsmErrorInfo.class.getName(), true,
                    ClankCsmErrorInfo.class.getClassLoader());//
        } catch (Exception e) {
        }
        return DEFAULT;
    }

    public static void setDefault(ClankCsmErrorInfoAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException();
        }
        DEFAULT = accessor;
    }

    public ClankCsmErrorInfoAccessor() {
    }
    
    abstract public CsmFile getCsmFile(ClankCsmErrorInfo info);
    abstract public ClankDiagnosticInfo getDelegate(ClankCsmErrorInfo info);

}
