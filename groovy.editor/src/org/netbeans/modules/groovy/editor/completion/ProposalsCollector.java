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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;

/**
 * ProposalsCollector is responsible for collecting code completion proposals.
 * 
 * Provides various method for different types of completion (completeMethods,
 * completeTypes, completeFields etc.).
 *
 * @author Martin Janicek
 */
public class ProposalsCollector {

    private List<CompletionProposal> proposals;
    private CompletionContext context;

    private BaseCompletion typesCompletion;
    private BaseCompletion fieldCompletion;
    private BaseCompletion methodCompletion;
    private BaseCompletion newVarCompletion;
    private BaseCompletion keywordCompletion;
    private BaseCompletion packageCompletion;
    private BaseCompletion localVarCompletion;
    private BaseCompletion camelCaseCompletion;
    private BaseCompletion namedParamCompletion;


    public ProposalsCollector(CompletionContext context) {
        this.context = context;
        
        proposals = new ArrayList<CompletionProposal>();
        typesCompletion = new TypesCompletion();
        fieldCompletion = new FieldCompletion();
        methodCompletion = new MethodCompletion();
        newVarCompletion = new NewVarCompletion();
        keywordCompletion = new KeywordCompletion();
        packageCompletion = new PackageCompletion();
        localVarCompletion = new LocalVarCompletion();
        camelCaseCompletion = new ConstructorGenerationCompletion();
        namedParamCompletion = new NamedParamsCompletion();
    }

    public void completeKeywords(CompletionContext completionRequest) {
        keywordCompletion.complete(proposals, completionRequest, context.getAnchor());
    }

    public void completeMethods(CompletionContext completionRequest) {
        methodCompletion.complete(proposals, completionRequest, context.getAnchor());
    }

    public void completeFields(CompletionContext completionRequest) {
        fieldCompletion.complete(proposals, completionRequest, context.getAnchor());
    }

    public void completeCamelCase(CompletionContext request) {
        camelCaseCompletion.complete(proposals, request, context.getAnchor());
    }

    public void completeTypes(CompletionContext request) {
        typesCompletion.complete(proposals, request, context.getAnchor());
    }

    public void completePackages(CompletionContext request) {
        packageCompletion.complete(proposals, request, context.getAnchor());
    }

    public void completeLocalVars(CompletionContext request) {
        localVarCompletion.complete(proposals, request, context.getAnchor());
    }

    public void completeNewVars(CompletionContext request) {
        newVarCompletion.complete(proposals, request, context.getAnchor());
    }

    public void completeNamedParams(CompletionContext request) {
        namedParamCompletion.complete(proposals, request, context.getAnchor());
    }

    public List<CompletionProposal> getCollectedProposals() {
        return proposals;
    }

    public void clearProposals() {
        proposals.clear();
    }
}
