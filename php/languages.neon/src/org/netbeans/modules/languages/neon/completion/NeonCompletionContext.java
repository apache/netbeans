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
package org.netbeans.modules.languages.neon.completion;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.languages.neon.spi.completion.MethodCompletionProvider;
import org.netbeans.modules.php.spi.templates.completion.CompletionProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public enum NeonCompletionContext {

    ALL {
        @Override
        public void complete(List<CompletionProposal> completionProposals, NeonCompletionProposal.CompletionRequest request) {
            completeServiceConfigOpts(completionProposals, request);
            completeTypes(completionProposals, request);
            completeMethods(completionProposals, request);
        }
    },
    SERVICE_CONFIG_OPTS {
        @Override
        public void complete(List<CompletionProposal> completionProposals, NeonCompletionProposal.CompletionRequest request) {
            completeServiceConfigOpts(completionProposals, request);
        }
    },
    TYPES {
        @Override
        public void complete(List<CompletionProposal> completionProposals, NeonCompletionProposal.CompletionRequest request) {
            completeTypes(completionProposals, request);
        }
    },
    METHODS {
        @Override
        public void complete(List<CompletionProposal> completionProposals, NeonCompletionProposal.CompletionRequest request) {
            completeMethods(completionProposals, request);
        }
    };

    protected void completeServiceConfigOpts(List<CompletionProposal> completionProposals, NeonCompletionProposal.CompletionRequest request) {
        for (NeonElement serviceConfigOpts : NeonCompletionHandler.SERVICE_CONFIG_OPTS) {
            if (CompletionUtils.startsWith(serviceConfigOpts.getName(), request.prefix)) {
                completionProposals.add(new NeonCompletionProposal.ServiceConfigOptCompletionProposal(serviceConfigOpts, request));
            }
        }
    }

    protected void completeTypes(List<CompletionProposal> completionProposals, NeonCompletionProposal.CompletionRequest request) {
        Collection<? extends CompletionProvider> typeCompletionProviders = CompletionProviders.getTypeProviders();
        FileObject fileObject = request.parserResult.getSnapshot().getSource().getFileObject();
        for (CompletionProvider typeCompletionProvider : typeCompletionProviders) {
            Set<String> types = typeCompletionProvider.getItems(fileObject, request.prefix);
            for (String typeName : types) {
                completionProposals.add(new NeonCompletionProposal.TypeCompletionProposal(NeonElement.Factory.createType(typeName), request));
            }
        }
    }

    protected void completeMethods(List<CompletionProposal> completionProposals, NeonCompletionProposal.CompletionRequest request) {
        Collection<? extends MethodCompletionProvider> methodCompletionProviders = CompletionProviders.getMethodProviders();
        FileObject fileObject = request.parserResult.getSnapshot().getSource().getFileObject();
        String typeName = CompletionUtils.extractTypeName(request.prefix);
        String methodPrefix = CompletionUtils.extractMethodPrefix(request.prefix);
        for (MethodCompletionProvider methodCompletionProvider : methodCompletionProviders) {
            Set<String> methods = methodCompletionProvider.complete(methodPrefix, typeName, fileObject);
            for (String methodName : methods) {
                completionProposals.add(new NeonCompletionProposal.MethodCompletionProposal(NeonElement.Factory.createMethod(methodName, typeName), request));
            }
        }
    }

    public abstract void complete(List<CompletionProposal> completionProposals, NeonCompletionProposal.CompletionRequest request);

}
