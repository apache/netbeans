/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.completion;

import java.util.List;
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
    public boolean complete(List<CompletionProposal> proposals, CompletionContext request, int anchor) {
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
                proposals.add(new CompletionItem.LocalVarItem(node, anchor + anchorShift));
                updated = true;
            } else if (!varName.equals(varPrefix) && varName.startsWith(varPrefix)) {
                proposals.add(new CompletionItem.LocalVarItem(node, anchor + anchorShift));
                updated = true;
            }
        }

        return updated;
    }
}
