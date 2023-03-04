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
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.completion.inference.VariableFinderVisitor;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;

/**
 *
 * @author Martin Janicek
 */
public class LocalVarCompletion extends BaseCompletion {

    @Override
    public boolean complete(Map<Object, CompletionProposal> proposals, CompletionContext request, int anchor) {
        LOG.log(Level.FINEST, "-> completeLocalVars"); // NOI18N

        if (!(request.location == CaretLocation.INSIDE_CLOSURE || request.location == CaretLocation.INSIDE_METHOD)
                // handle $someprefix in string
                && !(request.location == CaretLocation.INSIDE_STRING && request.getPrefix().matches("\\$[^\\{].*"))) {
            LOG.log(Level.FINEST, "Not inside method, closure or in-string variable, bail out."); // NOI18N
            return false;
        }

        // If we are right behind a dot, there's no local-vars completion.
        if (request.isBehindDot()) {
            LOG.log(Level.FINEST, "We are invoked right behind a dot."); // NOI18N
            return false;
        }

        VariableFinderVisitor vis = new VariableFinderVisitor(((ModuleNode) request.path.root()).getContext(),
                request.path, request.doc, request.astOffset);
        vis.collect();

        boolean updated = false;

        // If we are dealing with GStrings, the prefix is prefixed ;-)
        // ... with the dollar sign $ See # 143295
        int anchorShift = 0;
        String varPrefix = request.getPrefix();

        if (request.getPrefix().startsWith("$")) {
            varPrefix = request.getPrefix().substring(1);
            anchorShift = 1;
        }

        for (Variable node : vis.getVariables()) {
            String varName = node.getName();
            LOG.log(Level.FINEST, "Node found: {0}", varName); // NOI18N

            if (varPrefix.length() < 1) {
                proposals.put("local:" + varName, new CompletionItem.LocalVarItem(node, anchor + anchorShift));
                updated = true;
            } else if (!varName.equals(varPrefix) && varName.startsWith(varPrefix)) {
                proposals.put("local:" + varName, new CompletionItem.LocalVarItem(node, anchor + anchorShift));
                updated = true;
            }
        }

        return updated;
    }
}
