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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private Map<Object, CompletionProposal> proposals;
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
        
        proposals = new LinkedHashMap<Object, CompletionProposal>();
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
        (new SpockBlockNamesCompletion()).complete(proposals, completionRequest, context.getAnchor());
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
        (new SpockMethodParamCompletion()).complete(proposals, request, context.getAnchor());
    }

    public void completeNewVars(CompletionContext request) {
        newVarCompletion.complete(proposals, request, context.getAnchor());
    }

    public void completeNamedParams(CompletionContext request) {
        namedParamCompletion.complete(proposals, request, context.getAnchor());
    }

    public List<CompletionProposal> getCollectedProposals() {
        return new ArrayList<>(proposals.values());
    }

    public void clearProposals() {
        proposals.clear();
    }
}
