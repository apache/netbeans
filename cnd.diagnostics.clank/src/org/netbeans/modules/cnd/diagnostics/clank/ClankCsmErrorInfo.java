/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
