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
package org.netbeans.modules.css.editor.module.main;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.csl.CssElement;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.CssCompletionItem;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.editor.module.spi.Utilities;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Namespace;
import org.netbeans.modules.css.model.api.Namespaces;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = CssEditorModule.class)
public class NamespacesModule extends CssEditorModule {

    private static final String NAMESPACE_KEYWORD = "@namespace";//NOI18N
    static ElementKind NAMESPACE_ELEMENT_KIND = ElementKind.GLOBAL; //XXX fix CSL

    @Override
    @SuppressWarnings("fallthrough")
    public List<CompletionProposal> getCompletionProposals(final CompletionContext context) {
        final List<CompletionProposal> proposals = new ArrayList<>();
        Node activeNode = context.getActiveNode();
        //switch to first non error node
        boolean isError = false;
        loop:
        for (;;) {
            switch (activeNode.type()) {
                case error:
                case recovery:
                    activeNode = activeNode.parent();
                    isError = true;
                    break;
                default:
                    break loop;
            }
        }

        switch (activeNode.type()) {
            case namespacePrefix:
            case elementName:
                //already in the prefix

                //todo: rewrite to use index later
                proposals.addAll(getNamespaceCompletionProposals(context));
                break;

            case namespaces:
                //in body after namespace declaration(s), no prefix 
                proposals.addAll(getNamespaceCompletionProposals(context));
                break;

            case root:
            case styleSheet:
            case body:
                //in body, no prefix 
                proposals.addAll(getNamespaceCompletionProposals(context));
            case bodyItem:
                if (context.getActiveTokenId() == null //so the completion in empty file works
                        || context.getActiveTokenId() == CssTokenId.WS
                        || context.getActiveTokenId() == CssTokenId.AT_IDENT
                        || context.getActiveTokenId() == CssTokenId.ERROR && context.getPrefix().startsWith("@")) { //NOI18N
                CompletionProposal nsKeywordProposal
                        = CssCompletionItem.createRAWCompletionItem(new CssElement(NAMESPACE_KEYWORD), NAMESPACE_KEYWORD, ElementKind.FIELD, context.getAnchorOffset(), false);
                proposals.add(nsKeywordProposal);
            }
                break;

            case media:
            case combinator:
            case selector:
            case selectorsGroup:
                proposals.addAll(getNamespaceCompletionProposals(context));
                break;

            case typeSelector: //after class or id selector
                switch (context.getActiveTokenId()) {
                case WS:
                case NL:
                    proposals.addAll(getNamespaceCompletionProposals(context));
                case AT_SIGN:
                    if (isError) {
                    proposals.add(CssCompletionItem.createRAWCompletionItem(new CssElement(NAMESPACE_KEYWORD), NAMESPACE_KEYWORD, ElementKind.FIELD, context.getAnchorOffset(), false));
                }
            }

                break;

            case generic_at_rule:
                proposals.add(CssCompletionItem.createRAWCompletionItem(new CssElement(NAMESPACE_KEYWORD), NAMESPACE_KEYWORD, ElementKind.FIELD, context.getAnchorOffset(), false));
                break;

            case namespace:
                CssTokenId tokenId = context.getTokenSequence().token().id();
                if (tokenId == CssTokenId.NAMESPACE_SYM) {
                    CompletionProposal nsKeywordProposal
                            = CssCompletionItem.createRAWCompletionItem(new CssElement(NAMESPACE_KEYWORD), NAMESPACE_KEYWORD, ElementKind.FIELD, context.getAnchorOffset(), false);
                    proposals.add(nsKeywordProposal);
                }

            case simpleSelectorSequence:
                if (isError) {
                proposals.add(CssCompletionItem.createRAWCompletionItem(new CssElement(NAMESPACE_KEYWORD), NAMESPACE_KEYWORD, ElementKind.FIELD, context.getAnchorOffset(), false));
                Token<CssTokenId> token = context.getTokenSequence().token();
                switch (token.id()) {
                    case IDENT:
                        if (LexerUtils.followsToken(context.getTokenSequence(), EnumSet.of(CssTokenId.LBRACKET, CssTokenId.COMMA), true, true, CssTokenId.WS) != null) {
                        proposals.addAll(getNamespaceCompletionProposals(context));
                    }
                        break;
                    case LBRACKET:
                    case WS:
                        proposals.addAll(getNamespaceCompletionProposals(context));
                        break;

                }
            }
                break;

            case slAttribute:
            case slAttributeName:
            case namespacePrefixName:
                proposals.addAll(getNamespaceCompletionProposals(context));
                break;
        }

        return Utilities.filterCompletionProposals(proposals, context.getPrefix(), true);
    }

    private static List<CompletionProposal> getNamespaceCompletionProposals(final CompletionContext context) {
        final List<CompletionProposal> proposals = new ArrayList<>();
        //todo: rewrite to use index later
        Model sourceModel = context.getSourceModel();
        sourceModel.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                Namespaces namespaces = styleSheet.getNamespaces();
                if (namespaces == null) {
                    return;
                }
                for (Namespace ns : namespaces.getNamespaces()) {
                    proposals.add(
                            new NamespaceCompletionItem(ns.getNamespacePrefixName().getContent().toString(),
                            ns.getResourceIdentifier().getContent().toString(), context.getAnchorOffset()));
                }
            }
        });
        return proposals;
    }

    @Override
    public <T extends Map<OffsetRange, Set<ColoringAttributes>>> NodeVisitor<T> getSemanticHighlightingNodeVisitor(final FeatureContext context, T result) {
        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                switch (node.type()) {
                    case namespacePrefix:
                        getResult().put(Css3Utils.getDocumentOffsetRange(node, context.getSnapshot()), ColoringAttributes.CONSTRUCTOR_SET);
                        break;
                }
                return false;
            }
        };
    }

    @Override
    public <T extends Set<OffsetRange>> NodeVisitor<T> getMarkOccurrencesNodeVisitor(EditorFeatureContext context, T result) {
        return Utilities.createMarkOccurrencesNodeVisitor(context, result, NodeType.namespacePrefix);
    }

    @Override
    public <T extends List<StructureItem>> NodeVisitor<T> getStructureItemsNodeVisitor(final FeatureContext context, final T result) {
        final List<StructureItem> items = new ArrayList<>();

        return new NodeVisitor<T>() {

            private void addItem(StructureItem si) {
                if (items.isEmpty()) {
                    result.add(new TopLevelStructureItem.Namespaces(items));
                }
                items.add(si);
            }

            @Override
            public boolean visit(Node node) {
                if (node.type() == NodeType.namespace) {
                    addItem(new NamespaceStructureItem(context.getFileObject(), node));
                }

                return false;
            }
        };
    }
}
