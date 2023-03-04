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
package org.netbeans.modules.html.angular.editor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.angular.index.AngularJsController;
import org.netbeans.modules.html.angular.index.AngularJsIndex;
import org.netbeans.modules.html.angular.model.Directive;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;
import org.netbeans.modules.javascript2.model.api.Index;
import org.netbeans.modules.javascript2.model.api.IndexedElement;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
@CompletionProvider.Registration(priority=5)
public class AngularJsCodeCompletion implements CompletionProvider {

    private enum AngularContext {
        CONTROLLER, // controller name
        LINK, // component name
        UNKNOWN
    };
    
    private static final String MIMETYPE = "text/html/text/javascript";
    @Override
    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        if (!MIMETYPE.equals(ccContext.getParserResult().getSnapshot().getMimePath().getPath())) {
            // we are interested only in html documets
            return Collections.emptyList();
        }
        TokenSequence<HTMLTokenId> htmlTs = getHtmlTs(ccContext);
        if (htmlTs == null) {
            return Collections.emptyList();
        }
        List<CompletionProposal> result = new ArrayList<>();
        // this should be document offset
        htmlTs.move(ccContext.getCaretOffset());
        if (htmlTs.movePrevious()) {
            AngularContext angularContext = findHtmlContext(htmlTs);
            switch (angularContext) {
                case CONTROLLER:
                    if (jsCompletionContext == CompletionContext.GLOBAL) {
                        result.addAll(findControllerNames(ccContext));
                    }
                    break;
                case LINK:
                    if (jsCompletionContext == CompletionContext.GLOBAL) {
                        result.addAll(findComponentNames(ccContext));
                    }
                    break;
                default:
            }
        }
        
        return result;
    }

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        return null;
    }
    
    private TokenSequence<HTMLTokenId> getHtmlTs(CodeCompletionContext ccContext) {
        final Document document = ccContext.getParserResult().getSnapshot().getSource().getDocument(false);
        TokenSequence<HTMLTokenId> result = null;
        if (document != null) {
            final TokenSequence<HTMLTokenId>[] value = new TokenSequence[1];
            document.render(new Runnable() {

                @Override
                public void run() {
                    TokenHierarchy<Document> th = TokenHierarchy.get(document);
                    value[0] = th.tokenSequence(HTMLTokenId.language());
                }
            });
            result = value[0];
        }
        return result;
    }
    
    private AngularContext findHtmlContext(TokenSequence<HTMLTokenId> htmlTs) {
        Token<HTMLTokenId> token = htmlTs.token();
        HTMLTokenId id = token.id();
        switch (id) {
            case OPERATOR:
                if (htmlTs.movePrevious()) {
                    return findHtmlContext(htmlTs);
                }
                break;
            case ARGUMENT:
                String argument = token.text().toString();   
                Directive directive = Directive.getDirective(argument);
                if (directive != null) {
                    switch (directive) {
                        case controller :
                            return AngularContext.CONTROLLER;
                        case link:
                            return AngularContext.LINK;
                    }
                }
        }
        return AngularContext.UNKNOWN;
    }
    
    private Collection<? extends CompletionProposal> findControllerNames(CodeCompletionContext ccContext) {
        FileObject fo = ccContext.getParserResult().getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return Collections.emptyList();
        }
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return Collections.emptyList();
        }
        Collection<CompletionProposal> result = new ArrayList<>();
        AngularJsIndex angularIndex = null;
        try {
            angularIndex = AngularJsIndex.get(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (angularIndex != null) {
            Collection<AngularJsController> controllers = angularIndex.getControllers(ccContext.getPrefix(), false);
            for (AngularJsController controller : controllers) {
                int anchor = ccContext.getCaretOffset() - ccContext.getPrefix().length();
                String name = controller.getName();
                URL url = controller.getDeclarationFile();
                File file = new File(url.toString());
                AngularJsElement element = new AngularJsElement(name, ElementKind.METHOD);
                result.add(new AngularJsCompletionItem.AngularFOCompletionItem(element, anchor, FileUtil.toFileObject(FileUtil.normalizeFile(file))));
            }
        }
        
        Index jsIndex = Index.get(fo);
        if (jsIndex != null) {
            Collection<IndexedElement> globalVars = jsIndex.getGlobalVar(ccContext.getPrefix());
            for (IndexedElement variable : globalVars) {
                if (!variable.isAnonymous() && (variable.getJSKind() == JsElement.Kind.FUNCTION || variable.getJSKind() == JsElement.Kind.CONSTRUCTOR) && variable instanceof IndexedElement.FunctionIndexedElement) {
                    IndexedElement.FunctionIndexedElement function = (IndexedElement.FunctionIndexedElement)variable;
                    // pick up all functions that has at least one parameter and one of the paramets is $scope
                    if (!function.isAnonymous() && function.getParameters().size() > 0 && function.getParameters().containsKey("$scope")) {
                        AngularJsElement element = new AngularJsElement(function.getName(), ElementKind.METHOD);
                        int anchor = ccContext.getCaretOffset() - ccContext.getPrefix().length();
                        result.add(new AngularJsCompletionItem.AngularFOCompletionItem(element, anchor, function.getFileObject()));
                    }
                }
            }
        }
        return result;
    }

    private Collection<? extends CompletionProposal> findComponentNames(CodeCompletionContext ccContext) {
        FileObject fo = ccContext.getParserResult().getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return Collections.emptyList();
        }
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return Collections.emptyList();
        }
        Collection<CompletionProposal> result = new ArrayList<>();
        AngularJsIndex angularIndex = null;
        try {
            angularIndex = AngularJsIndex.get(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (angularIndex != null) {
            Collection<String> components = angularIndex.getComponents(ccContext.getPrefix(), false);
            for (String component : components) {
                int anchor = ccContext.getCaretOffset() - ccContext.getPrefix().length();
                AngularJsElement element = new AngularJsElement(component, ElementKind.METHOD);
                result.add(new AngularJsCompletionItem.AngularFOCompletionItem(element, anchor, null));
            }
        }
        return result;
    }
}
