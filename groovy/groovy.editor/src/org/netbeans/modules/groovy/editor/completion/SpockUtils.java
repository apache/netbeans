/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.groovy.editor.completion;

import java.util.HashSet;
import java.util.Set;
import org.codehaus.groovy.ast.ClassNode;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;

/**
 *
 * @author Petr Pisl
 */
public class SpockUtils {

    public static boolean isInSpecificationClass(CompletionContext context) {
        
        ClassNode classNode = context.declaringClass;
        if (classNode == null) {
            // This shouldn't happen, because this cc is invoked only INSIDE_METHOD cc location.
            // But I have seed the NPE
            return false;
        }
        ParserResult pr = context.getParserResult();
        if (pr instanceof GroovyParserResult) {
            GroovyParserResult gpr = (GroovyParserResult) pr;
            ClassNode specCN = gpr.resolveClassName("spock.lang.Specification");   //NOI18N
            if (specCN != null) {
                if (classNode.isDerivedFrom(specCN)) {   
                    return true;
                }
            } else {
                // this branch is mainly for tests, which doesn't have Spock on classpath
                String name;
                Set<String> visited = new HashSet<>();
                while (classNode != null && !visited.contains(name = classNode.getName())) {
                    if ("spock.lang.Specification".equals(name)) {  //NOI18N
                        return true;
                    }
                    visited.add(name);
                    classNode = classNode.getSuperClass();
                }
            }
        }
        return false;
    }

    static boolean isFirstStatement(final CompletionContext request) {
        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(request.doc, 1);

        if (ts != null) {
            ts.move(request.lexOffset);
            if (ts.movePrevious()) {
                while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
                    Token<GroovyTokenId> t = ts.token();
                    if (!(t.id() == GroovyTokenId.NLS || t.id() == GroovyTokenId.WHITESPACE
                            || t.id() == GroovyTokenId.SH_COMMENT || t.id() == GroovyTokenId.SL_COMMENT
                            || t.id() == GroovyTokenId.BLOCK_COMMENT || t.id() == GroovyTokenId.LINE_COMMENT)) {
                        return false;
                    }
                }
            }
            return true;
        }

        return false;
    }

}
