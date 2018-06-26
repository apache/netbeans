/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.nodejs.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.model.api.IndexedElement;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
@CompletionProvider.Registration(priority = 41)
public class NodeJsCodeCompletion implements CompletionProvider {

    private static final String TEMPLATE_REQUIRE = "('${cursor}')"; //NOI18N
    

    @Override
    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        FileObject fo = ccContext.getParserResult().getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return Collections.EMPTY_LIST;
        }
        List<CompletionProposal> result = new ArrayList<CompletionProposal>();
        if (jsCompletionContext == CompletionContext.IN_STRING || jsCompletionContext == CompletionContext.EXPRESSION
                || jsCompletionContext == CompletionContext.GLOBAL) {
            TokenHierarchy<?> th = ccContext.getParserResult().getSnapshot().getTokenHierarchy();
            if (th == null) {
                return Collections.EMPTY_LIST;
            }
            int carretOffset = ccContext.getCaretOffset();
            int eOffset = ccContext.getParserResult().getSnapshot().getEmbeddedOffset(carretOffset);
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, eOffset);
            if (ts == null) {
                return Collections.EMPTY_LIST;
            }
            ts.move(eOffset);

            if (!ts.movePrevious()) {
                if (prefix.isEmpty() || NodeJsUtils.REQUIRE_METHOD_NAME.startsWith(prefix)) {
                    NodeJsElement handle = new NodeJsElement(fo, NodeJsUtils.REQUIRE_METHOD_NAME, NodeJsDataProvider.getDefault(fo).getDocumentationForGlobalObject(NodeJsUtils.REQUIRE_METHOD_NAME), TEMPLATE_REQUIRE, ElementKind.METHOD);
                    result.add(new NodeJsCompletionItem.NodeJsModuleCompletionItem(handle, eOffset - prefix.length()));
                }
                return result;
            }

            Token<? extends JsTokenId> token = null;
            if (jsCompletionContext == CompletionContext.IN_STRING || jsCompletionContext == CompletionContext.EXPRESSION || jsCompletionContext == CompletionContext.GLOBAL) {
                String wholePrefix = ts.token().id() == JsTokenId.STRING ? ts.token().text().toString().trim() : "";
                NodeJsContext nodeContext = NodeJsContext.findContext(ts, eOffset);
              
                switch (nodeContext) {
                    case MODULE_PATH:
                        if (wholePrefix.isEmpty() || (wholePrefix.charAt(0) != '.' && wholePrefix.charAt(0) != '/')) {
                            NodeJsDataProvider dataProvider = NodeJsDataProvider.getDefault(fo);
                            Collection<String> modules = dataProvider.getRuntimeModules();
                            for(String module: modules) {
                                if (module.startsWith(prefix)) {
                                    NodeJsElement handle = new NodeJsElement.NodeJsModuleElement(fo, module);
                                    result.add(new NodeJsCompletionItem.NodeJsModuleCompletionItem(handle, eOffset - prefix.length()));
                                }
                            }
                            Collection<FileObject>localModuleFolders = dataProvider.getLocalModules(fo);
                            for(FileObject module: localModuleFolders) {
                                if (module.getName().startsWith(prefix)) {
                                    NodeJsElement handle = new NodeJsElement.NodeJsLocalModuleElement(module, module.getName());
                                    result.add(new NodeJsCompletionItem.NodeJsModuleCompletionItem(handle, eOffset - prefix.length()));
                                }
                            }
                        }
                        int prefixLength = (".".equals(wholePrefix) || "..".equals(wholePrefix) || "../".equals(wholePrefix)) ? wholePrefix.length() : prefix.length();
                        result.addAll((new NodeJsCompletionItem.FilenameSupport()).getItems(ccContext.getParserResult().getSnapshot().getSource().getFileObject(), eOffset - prefixLength, ".." + prefix));
                        break;
                    case AFTER_ASSIGNMENT:
                    case GLOBAL:
                        if (prefix.isEmpty() || NodeJsUtils.REQUIRE_METHOD_NAME.startsWith(prefix)) {
                            NodeJsElement handle = new NodeJsElement(fo, NodeJsUtils.REQUIRE_METHOD_NAME, NodeJsDataProvider.getDefault(fo).getDocumentationForGlobalObject(NodeJsUtils.REQUIRE_METHOD_NAME), TEMPLATE_REQUIRE, ElementKind.METHOD);
                            result.add(new NodeJsCompletionItem.NodeJsModuleCompletionItem(handle, eOffset - prefix.length()));
                        }
                        break;
                    case ASSIGN_LISTENER:
//                        String eventEmiterName = NodeJsContext.getEventEmiterName(ts, eOffset);
//                        if (eventEmiterName != null && !eventEmiterName.isEmpty()) {
//                            Model jsModel = Model.getModel(ccContext.getParserResult());
//                            if (jsModel != null) {
//                                JsObject variable = jsModel.findVariable(eventEmiterName, eOffset);
//                                Collection<? extends TypeUsage> assignments = variable.getAssignments();
//                                if (!assignments.isEmpty()) {
//                                   List<TypeUsage> resolved = new ArrayList<>();
//                                   for (TypeUsage type : assignments) {
//                                       resolved.addAll(jsModel.resolveType(type));
//                                   }
//                                }
//                            }
//                        }
                            Map<String, Collection<String>> events = NodeJsDataProvider.getDefault(fo).getAllEvents();
                            for (Map.Entry<String, Collection<String>> event : events.entrySet()) {
                                String name = event.getKey();
                                if (name.startsWith(prefix)) {
                                    Collection<String> docs = event.getValue();
                                    StringBuilder doc = new StringBuilder();
                                    for (String text : docs) {
                                        doc.append(text);
                                        doc.append("<br/><br/>");   //NOI18N
                                    }
                                    NodeJsElement handle = new NodeJsElement(fo, name, doc.toString(), ElementKind.OTHER);
                                    result.add(new NodeJsCompletionItem.NodeJsModuleCompletionItem(handle, eOffset - prefix.length()));
                                }
                            }
                        break;
                }
            }
        }

        return result.isEmpty() ? Collections.EMPTY_LIST : result;
    }

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        if (element instanceof NodeJsElement) {
            return ((NodeJsElement)element).getDocumentation();
        }
        String fqn = null;
        if (element instanceof JsObject) {
            fqn = ((JsObject)element).getFullyQualifiedName();
        }
        if (element instanceof IndexedElement) {
            fqn = ((IndexedElement)element).getFQN();
        }
        FileObject fo = element.getFileObject();
        if (fo != null && fqn != null) {
            if (!fqn.startsWith(NodeJsUtils.FAKE_OBJECT_NAME_PREFIX)) {
                if (fo != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(NodeJsUtils.FAKE_OBJECT_NAME_PREFIX).append(fo.getName());
                    sb.append('.').append(fqn);
                    fqn = sb.toString();
                    return NodeJsDataProvider.getDefault(fo).getDocumentation(fqn);
                }
            } else {
                return NodeJsDataProvider.getDefault(fo).getDocumentation(fqn);
            }
        }
        return null;
    }

}
