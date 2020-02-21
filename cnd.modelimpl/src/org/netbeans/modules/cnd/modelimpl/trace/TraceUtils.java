/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
