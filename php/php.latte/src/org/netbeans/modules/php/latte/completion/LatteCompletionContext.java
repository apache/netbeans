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
package org.netbeans.modules.php.latte.completion;

import java.util.List;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.php.spi.templates.completion.CompletionProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public enum LatteCompletionContext {

    ALL {
        @Override
        public void complete(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
            completeMacros(completionProposals, request);
            completeHelpers(completionProposals, request);
            completeKeywords(completionProposals, request);
        }
    },
    MACRO {
        @Override
        public void complete(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
            completeMacros(completionProposals, request);
            completeVariables(completionProposals, request);
        }
    },
    END_MACRO {
        @Override
        public void complete(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
            completeEndMacros(completionProposals, request);
        }
    },
    HELPER {
        @Override
        public void complete(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
            completeHelpers(completionProposals, request);
        }
    },
    ITERATOR_ITEM {
        @Override
        public void complete(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
            completeIteratorItems(completionProposals, request);
        }
    },
    VARIABLE {
        @Override
        public void complete(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
            completeVariables(completionProposals, request);
        }
    },
    EMPTY_DELIMITERS {
        @Override
        public void complete(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
            completeMacros(completionProposals, request);
            completeVariables(completionProposals, request);
            completeEndMacros(completionProposals, request);
        }
    },
    CONTROL_MACRO {
        @Override
        public void complete(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
            completeControls(completionProposals, request);
        }
    },
    NONE {
        @Override
        public void complete(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
        }

    };

    public abstract void complete(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request);

    protected void completeMacros(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
        for (LatteElement macro : LatteCompletionHandler.MACROS) {
            if (startsWith(macro.getName(), request.prefix)) {
                completionProposals.add(new LatteCompletionProposal.StartMacroCompletionProposal(macro, request));
            }
        }
    }

    protected void completeEndMacros(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
        for (LatteElement endMacro : LatteCompletionHandler.END_MACROS) {
            if (startsWith(endMacro.getName(), request.prefix)) {
                completionProposals.add(new LatteCompletionProposal.EndMacroCompletionProposal(endMacro, request));
            }
        }
    }

    protected void completeHelpers(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
        for (LatteElement helper : LatteCompletionHandler.HELPERS) {
            if (startsWith(helper.getName(), request.prefix)) {
                completionProposals.add(new LatteCompletionProposal.HelperCompletionProposal(helper, request));
            }
        }
    }

    protected void completeKeywords(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
        for (LatteElement keyword : LatteCompletionHandler.KEYWORDS) {
            if (startsWith(keyword.getName(), request.prefix)) {
                completionProposals.add(new LatteCompletionProposal.KeywordCompletionProposal(keyword, request));
            }
        }
    }

    protected void completeIteratorItems(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
        completeIteratorFieldItems(completionProposals, request);
        completeIteratorMethodItems(completionProposals, request);
    }

    private void completeIteratorFieldItems(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
        for (LatteElement iteratorItem : LatteCompletionHandler.ITERATOR_FIELD_ITEMS) {
            if (startsWith(iteratorItem.getName(), request.prefix)) {
                completionProposals.add(new LatteCompletionProposal.IteratorFieldItemCompletionProposal(iteratorItem, request));
            }
        }
    }

    private void completeIteratorMethodItems(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
        for (LatteElement iteratorItem : LatteCompletionHandler.ITERATOR_METHOD_ITEMS) {
            if (startsWith(iteratorItem.getName(), request.prefix)) {
                completionProposals.add(new LatteCompletionProposal.IteratorMethodItemCompletionProposal(iteratorItem, request));
            }
        }
    }

    protected void completeVariables(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
        completeDefaultVariables(completionProposals, request);
        completeProvidedVariables(completionProposals, request);
    }

    private void completeDefaultVariables(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
        for (LatteElement variable : LatteCompletionHandler.DEFAULT_VARIABLES) {
            if (startsWith(variable.getName(), request.prefix)) {
                completionProposals.add(new LatteCompletionProposal.DefaultVariableCompletionProposal(variable, request));
            }
        }
    }

    private void completeProvidedVariables(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
        FileObject sourceFileObject = request.parserResult.getSnapshot().getSource().getFileObject();
        List<CompletionProvider> variableProviders = CompletionProviders.getVariableProviders();
        for (CompletionProvider variableProvider : variableProviders) {
            for (String variable : variableProvider.getItems(sourceFileObject, request.prefix)) {
                completionProposals.add(new LatteCompletionProposal.UserVariableCompletionProposal(LatteElement.VariableFactory.create(variable), request));
            }
        }
    }

    protected void completeControls(List<CompletionProposal> completionProposals, LatteCompletionProposal.CompletionRequest request) {
        FileObject sourceFileObject = request.parserResult.getSnapshot().getSource().getFileObject();
        List<CompletionProvider> controlProviders = CompletionProviders.getControlProviders();
        for (CompletionProvider controlProvider : controlProviders) {
            for (String item : controlProvider.getItems(sourceFileObject, request.prefix)) {
                completionProposals.add(new LatteCompletionProposal.ControlCompletionProposal(LatteElement.ControlFactory.create(item), request));
            }
        }
    }

    private static boolean startsWith(String theString, String prefix) {
        return prefix.length() == 0 ? true : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

}
