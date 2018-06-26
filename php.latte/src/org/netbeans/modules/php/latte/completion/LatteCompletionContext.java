/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
