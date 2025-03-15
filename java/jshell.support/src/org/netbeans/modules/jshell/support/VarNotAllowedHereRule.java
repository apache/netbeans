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
package org.netbeans.modules.jshell.support;

import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.tools.Diagnostic;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.hints.friendapi.OverrideErrorMessage;

/**
 * Suppresses error warning that var is not allowed here. The parsing of a
 * `var x = ...` statement without a trailing semicolon leads to this warning,
 * which is invalid.
 */
public class VarNotAllowedHereRule implements OverrideErrorMessage {
    private static final Set<String> CODES = new HashSet<>(Arrays.asList("compiler.err.restricted.type.not.allowed.here"));
    
    @Override
    public String createMessage(CompilationInfo info, Diagnostic d, int offset, TreePath treePath, Data data) {
        if (!info.getSnapshot().getMimeType().equals("text/x-repl")) {
            MimePath mp = info.getSnapshot().getMimePath();
            if (!"text/x-repl".equals(mp.getMimeType(0))) {
                return null;
            }
        }
        Object dp = SourceUtils.getDiagnosticParam(d, 0);
        if("var".equals(String.valueOf(dp))) {
            return "";
        } else {
            return null;
        }
    }

    @Override
    public Set getCodes() {
        return CODES;
    }

    @Override
    public List run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data data) {
        return null;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
