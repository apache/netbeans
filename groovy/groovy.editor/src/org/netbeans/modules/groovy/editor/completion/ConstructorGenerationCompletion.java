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

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import org.codehaus.groovy.ast.ClassNode;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.completion.util.CamelCaseUtil;
import org.netbeans.modules.groovy.editor.api.completion.util.ContextHelper;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;

/**
 * This should complete constructor generation.
 *  We are processing e.g. SB, StrB both results in:
 *
 *      StringBuilder() {
 *      }
 * 
 * @author Martin Janicek
 */
public class ConstructorGenerationCompletion extends BaseCompletion {

    @Override
    public boolean complete(Map<Object, CompletionProposal> proposals, CompletionContext request, int anchor) {
        LOG.log(Level.FINEST, "-> constructor generation completion"); // NOI18N

        if (!isValidLocation(request)) {
            return false;
        }

        ClassNode requestedClass = ContextHelper.getSurroundingClassNode(request);
        if (requestedClass == null) {
            LOG.log(Level.FINEST, "No surrounding class found, bail out ..."); // NOI18N
            return false;
        }
        String className = GroovyUtils.stripPackage(requestedClass.getName());

        boolean camelCaseMatch = CamelCaseUtil.compareCamelCase(className, request.getPrefix());
        if (camelCaseMatch) {
            LOG.log(Level.FINEST, "Prefix matches Class's CamelCase signature. Adding."); // NOI18N
            CompletionItem.ConstructorItem ci = new CompletionItem.ConstructorItem(className, Collections.emptyList(), anchor, true);
            proposals.putIfAbsent(new MethodSignature("<init>", new String[0]) , ci);
        }

        return camelCaseMatch;
    }

    private boolean isValidLocation(CompletionContext request) {
        if (!(request.location == CaretLocation.INSIDE_CLASS)) {
            LOG.log(Level.FINEST, "Not inside a class"); // NOI18N
            return false;
        }

        // We don't want to offer costructor generation when creating new instance
        if (request.context.before1 != null && request.context.before1.text().toString().equals("new") && request.getPrefix().length() > 0) {
            return false;
        }

        // We are after either implements or extends keyword
        if ((request.context.beforeLiteral != null && request.context.beforeLiteral.id() == GroovyTokenId.LITERAL_implements) ||
            (request.context.beforeLiteral != null && request.context.beforeLiteral.id() == GroovyTokenId.LITERAL_extends)) {
            return false;
        }

        if (request.getPrefix() == null || request.getPrefix().length() < 0) {
            return false;
        }

        // We can be either in 'String ^' or in 'NoSE^' situation
        if (request.context.before1 != null && request.context.before1.id() == GroovyTokenId.IDENTIFIER) {
            request.context.ts.movePrevious();
            Token<?> caretToken = request.context.ts.token();

            if (" ".equals(caretToken.text().toString())) {
                // 'String ^' situation --> No constructor generation proposals
                return false;
            }
        }

        // We are after class definition
        if (request.context.beforeLiteral != null && request.context.beforeLiteral.id() == GroovyTokenId.LITERAL_class) {
            return false;
        }

        return true;
    }
}
