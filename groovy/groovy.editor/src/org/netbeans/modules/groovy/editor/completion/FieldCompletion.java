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

import java.util.Map;
import java.util.logging.Level;
import org.codehaus.groovy.ast.ClassNode;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.completion.util.ContextHelper;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.completion.provider.CompleteElementHandler;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;

/**
 * Complete the fields for a class. There are two principal completions for fields:
 *
 * 1.) We are invoked right behind a dot. Then we have to retrieve the type in front of this dot.
 * 2.) We are located inside a type. Then we gotta get the fields for this class.
 *
 * @author Martin Janicek
 */
public class FieldCompletion extends BaseCompletion {

    @Override
    public boolean complete(Map<Object, CompletionProposal> proposals, CompletionContext context, int anchor) {
        LOG.log(Level.FINEST, "-> completeFields"); // NOI18N

        if (context.location == CaretLocation.INSIDE_PARAMETERS && context.isBehindDot() == false) {
            LOG.log(Level.FINEST, "no fields completion inside of parameters-list"); // NOI18N
            return false;
        }

        if (context.dotContext != null && context.dotContext.isMethodsOnly()) {
            return false;
        }

        // We are after either implements or extends keyword
        if ((context.context.beforeLiteral != null && context.context.beforeLiteral.id() == GroovyTokenId.LITERAL_implements) ||
            (context.context.beforeLiteral != null && context.context.beforeLiteral.id() == GroovyTokenId.LITERAL_extends)) {
            return false;
        }
        
        if (context.context.beforeLiteral != null && context.context.beforeLiteral.id() == GroovyTokenId.LITERAL_class) {
            return false;
        }

        if (context.isBehindDot()) {
            LOG.log(Level.FINEST, "We are invoked right behind a dot."); // NOI18N

            PackageCompletionRequest packageRequest = getPackageRequest(context);

            if (packageRequest.basePackage.length() > 0) {
                ClasspathInfo pathInfo = getClasspathInfoFromRequest(context);

                if (isValidPackage(pathInfo, packageRequest.basePackage)) {
                    LOG.log(Level.FINEST, "The string before the dot seems to be a valid package"); // NOI18N
                    return false;
                }
            }
        } else {
            context.setDeclaringClass(ContextHelper.getSurroundingClassNode(context), context.isStaticMembers());
        }

        // If we are dealing with GStrings, the prefix is prefixed ;-)
        // ... with the dollar sign $ See # 143295
        if (context.getPrefix().startsWith("$")) {
            context.setPrefix(context.getPrefix().substring(1)); // Remove $ from prefix
            context.setAnchor(context.getAnchor() + 1);          // Add 1 for anchor position
        }

        Map<FieldSignature, CompletionItem> result = new CompleteElementHandler(context).getFields();
        
        FieldSignature prefixFieldSignature = new FieldSignature(context.getPrefix());
        result.remove(prefixFieldSignature);
        for (Map.Entry<FieldSignature, CompletionItem> e :result.entrySet()) {
            proposals.putIfAbsent(e.getKey(), e.getValue());
        }

        return true;
    }
}
