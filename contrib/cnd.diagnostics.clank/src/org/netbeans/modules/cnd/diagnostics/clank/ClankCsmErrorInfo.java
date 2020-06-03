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
package org.netbeans.modules.cnd.diagnostics.clank;

import org.clang.tools.services.ClankDiagnosticInfo;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.diagnostics.clank.impl.ClankCsmErrorInfoAccessor;

public final class ClankCsmErrorInfo implements CsmErrorInfo {
    
    static {
        ClankCsmErrorInfoAccessor.setDefault(new ClankCsmErrorInfoAcessorImpl());
    }

    private final ClankDiagnosticInfo errorInfo;
    private final CsmFile csmFile;

    ClankCsmErrorInfo(CsmFile csmFile, ClankDiagnosticInfo info) {
        this.errorInfo = info;
        this.csmFile = csmFile;
    }

    CsmFile getCsmFile() {
        return csmFile;
    }

    @Override
    public String getMessage() {
        return errorInfo.getMessage();
    }

    @Override
    public CsmErrorInfo.Severity getSeverity() {
        return errorInfo.getSeverity() == ClankDiagnosticInfo.Severity.Error ? CsmErrorInfo.Severity.ERROR : CsmErrorInfo.Severity.WARNING;
    }

    @Override
    public int getStartOffset() {
        return errorInfo.getStartOffsets()[0];
    }

    @Override
    public String getCustomType() {
        String warningString = getSeverity() == CsmErrorInfo.Severity.WARNING ? "-warning" : "";//NOI18N
        return this.errorInfo.hasFixes() || this.errorInfo.hasNotes() ? "clank-diagnostics-annotations" + warningString + "-fixable" ://NOI18N
        "clank-diagnostics-annotations" + warningString;//NOI18N
    }

    @Override
    public int[] getStartOffsets() {
        return errorInfo.getStartOffsets(); 
    }

    @Override
    public int[] getEndOffsets() {
        return errorInfo.getEndOffsets(); 
    }

    @Override
    public int getEndOffset() {
        //return (int) CsmFileInfoQuery.getDefault().getOffset(file, errorInfo.getLine(), errorInfo.getColumn() + 1);
        return errorInfo.getEndOffsets()[0];
    }

    ClankDiagnosticInfo getDelegate() {
        return errorInfo;
    }
    
    private static class ClankCsmErrorInfoAcessorImpl extends ClankCsmErrorInfoAccessor {

        @Override
        public CsmFile getCsmFile(ClankCsmErrorInfo info) {
            return info.getCsmFile();
        }

        @Override
        public ClankDiagnosticInfo getDelegate(ClankCsmErrorInfo info) {
            return info.getDelegate();
        }
        
    }
    
}
