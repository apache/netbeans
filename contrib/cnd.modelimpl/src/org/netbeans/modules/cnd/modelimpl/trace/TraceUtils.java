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

package org.netbeans.modules.cnd.modelimpl.trace;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.util.Collection;
import java.util.Map;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTMacroCallback;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPParser;

/**
 * Misc trace-related utilities
 */
public class TraceUtils {

    public static String getTokenTypeName(Token token) {
        return getTokenTypeName(token.getType());
    }

    public static String getTokenTypeName(AST ast) {
        return getTokenTypeName(ast.getType());
    }

    public static String getTokenTypeName(int tokenType) {
        try {
            return CPPParser._tokenNames[tokenType];
        }
        catch( Exception e ) {
            return "";
        }
    }

    public static final String getMacroString(PreprocHandler preprocHandler, Collection<String> logMacros) {
        StringBuilder sb = new StringBuilder();
        if (logMacros != null) {
            for (String macro : logMacros) {
                sb.append(String.format(" #defined(%s)=%b", //NOI18N
                        macro, ((APTMacroCallback)preprocHandler.getMacroMap()).isDefined(macro)));
            }
        }        
        return sb.toString();
    }

    public static final String getPreprocStateString(PreprocHandler.State preprocState) {
        return String.format("valid=%b, compile-context=%b, cleaned=%b", preprocState.isValid(), preprocState.isCompileContext(), preprocState.isCleaned());//NOI18N
    }   

    public static final String getPreprocStartEntryString(PreprocHandler.State preprocState) {
        StartEntry startEntry = APTHandlersSupport.extractStartEntry(preprocState);
        if (startEntry == null) {
            return "no start entry info"; // NOI18N
        } else {
            return String.format("start file=%s, start-prj=%s", startEntry.getStartFile(), startEntry.getStartFileProject()); // NOI18N
        }
    }
    
    public static String traceMap(Map<?, ?> mapping, int indent) {
        StringBuilder out = new StringBuilder();
        if (mapping != null && !mapping.isEmpty()) {
            for (Map.Entry<?, ?> entry : mapping.entrySet()) {
                repeat(out, indent, ' ').append("[").append(entry.getKey()).append("]=>{"); // NOI18N
                out.append(entry.getValue()).append("}\n"); // NOI18N
            }
        }
        return out.toString();
    }
    
    public static StringBuilder repeat(StringBuilder b, int level, char character) {
        for (int i = 0; i < level; i++) {
            b.append(character);
        }
        return b;
    }        
    
    public static void updateTraceFlag(String flag, boolean value) {
        TraceFlags.validate(flag, value);
    }
}
