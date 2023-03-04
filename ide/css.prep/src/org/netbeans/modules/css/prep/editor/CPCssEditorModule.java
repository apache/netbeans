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
package org.netbeans.modules.css.prep.editor;

import java.io.IOException;
import org.netbeans.modules.css.prep.editor.model.CPModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.editor.module.spi.FutureParamTask;
import org.netbeans.modules.css.editor.module.spi.SemanticAnalyzer;
import org.netbeans.modules.css.editor.module.spi.Utilities;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.prep.editor.model.CPElement;
import org.netbeans.modules.css.prep.editor.model.CPElementHandle;
import org.netbeans.modules.css.prep.editor.model.CPElementType;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.DependencyType;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.Lines;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 * CSS preprocessor {@link CssEditorModule} implementation.
 *
 * TODO fix the instant rename and the mark occurrences - they are pretty naive
 * - not scoped at all :-)
 *
 * @author marekfukala
 */
@ServiceProvider(service = CssEditorModule.class)
public class CPCssEditorModule extends CssEditorModule {

    private final SemanticAnalyzer semanticAnalyzer = new CPSemanticAnalyzer();
    private static Map<NodeType, ColoringAttributes> COLORINGS;
    private static final Collection<String> PSEUDO_CLASSES = Arrays.asList(new String[]{
                "extend" //NOI18N
            });

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return semanticAnalyzer;
    }

    @Override
    @SuppressWarnings("fallthrough")
    public List<CompletionProposal> getCompletionProposals(final CompletionContext context) {
        final List<CompletionProposal> proposals = new ArrayList<>();

        CPModel model = CPModel.getModel(context.getParserResult());
        if (model == null) {
            return Collections.emptyList();
        }
        List<CompletionProposal> allVars = new ArrayList<>(getVariableCompletionProposals(context, model));

        //errorneous source
        TokenSequence<CssTokenId> ts = context.getTokenSequence();
        Token<CssTokenId> token = ts.token();
        if (token == null) {
            return Collections.emptyList();
        }
        CssTokenId tid = token.id();
        CharSequence ttext = token.text();
        char first = ttext.charAt(0);

        switch (tid) {
            case ERROR:
                switch (first) {
                case '$':
                    //"$" as a prefix - user likely wants to type variable
                    //check context
                    if (NodeUtil.getAncestorByType(context.getActiveTokenNode(), NodeType.rule) != null
                            || NodeUtil.getAncestorByType(context.getActiveTokenNode(), NodeType.cp_mixin_block) != null) {
                    //in declarations node -> offer all vars
                    return Utilities.filterCompletionProposals(allVars, context.getPrefix(), true);
                }
                    break;
            }
            case AT_SIGN:
                switch (first) {
                case '@':
                    //may be:
                    //1. @-rule beginning
                    //2. less variable

                    //1.@-rule
                    proposals.addAll(Utilities.createRAWCompletionProposals(model.getDirectives(), ElementKind.KEYWORD, context.getAnchorOffset()));

                    //2.less variables
                    if (model.getPreprocessorType() == CPType.LESS) {
                        proposals.addAll(allVars);
                    }
                    return Utilities.filterCompletionProposals(proposals, context.getPrefix(), true);
            }
                break;

            case SASS_VAR:
                //sass variable: $v|
                if (model.getPreprocessorType() == CPType.SCSS) {
                return Utilities.filterCompletionProposals(allVars, context.getPrefix(), true);
            }

            case AT_IDENT:
                //not complete keyword (complete keyword have their own token types,
                //but no need to complete them except documentation completion request
                List<CompletionProposal> props = Utilities.createRAWCompletionProposals(model.getDirectives(), ElementKind.KEYWORD, context.getAnchorOffset());

                //less variable: @va|
                if (model.getPreprocessorType() == CPType.LESS) {
                    return Utilities.filterCompletionProposals(allVars, context.getPrefix(), true);
                }
                return Utilities.filterCompletionProposals(props, context.getPrefix(), true);
        }

        Node activeNode = context.getActiveNode();
        boolean isError = false;
        //skip to first non error or recovery parent
        while (activeNode.type() == NodeType.error || activeNode.type() == NodeType.recovery) {
            isError = true;
            activeNode = activeNode.parent();
        }
//        NodeUtil.dumpTree(context.getParseTreeRoot());

        switch (activeNode.type()) {
            case bodyItem:
            case mediaBody:
            case mediaBodyItem:
            case cssClass:
                switch (tid) {
                case WS:
                    //in stylesheet main body: @include |
                    //check the previous token
                    if (ts.movePrevious()) {
                    Token<CssTokenId> previousToken = ts.token();
                    if (previousToken.id() == CssTokenId.SASS_INCLUDE) {
                        //add all mixins
                        proposals.addAll(getMixinsCompletionProposals(context, model));
                    }
                }
                    break;

                case IDENT:
                    if (LexerUtils.followsToken(ts, CssTokenId.SASS_INCLUDE, true, true, CssTokenId.WS) != null) {
                        //in stylesheet main body: @include mix|
                        //ok so the ident if preceeded by WS and then by SASS_INCLUDE token
                        proposals.addAll(getMixinsCompletionProposals(context, model));
                    } else if (LexerUtils.followsToken(ts, CssTokenId.DOT, true, true, CssTokenId.WS) != null) {
                        //in stylesheet main body: .mix| --> less mixins
                        proposals.addAll(getMixinsCompletionProposals(context, model));
                    }
                    break;
                case DOT:
                    proposals.addAll(getMixinsCompletionProposals(context, model));
                break;
            }
                break;
            case cp_mixin_call:
            //@include |
            case cp_mixin_name:
                //@include mymi|
                proposals.addAll(getMixinsCompletionProposals(context, model));
                break;

            case cp_variable:
                //already in the prefix
                proposals.addAll(allVars);
                break;
            case propertyValue:
                //just $ or @ prefix
                if (context.getPrefix().length() == 1 && context.getPrefix().charAt(0) == model.getPreprocessorType().getVarPrefix()) {
                proposals.addAll(allVars);
            }
                break;
            case declaration:
                switch (tid) {
                case DOT:
                    //div { .| } -- less mixin call
                    proposals.addAll(getMixinsCompletionProposals(context, model));
                    break;
                case WS:
                    //go back and find first non white token
                    while (ts.movePrevious()
                            && (ts.token().id() == CssTokenId.WS
                            || ts.token().id() == CssTokenId.NL)) {
                    //skip ws backward
                }
                    switch (ts.token().id()) {
                        case SASS_INCLUDE:
                            //completion at: @include |
                            proposals.addAll(getMixinsCompletionProposals(context, model));
                            break;

                    }

            }
                break;
            case selectorsGroup:
                switch (ts.token().id()) {
                    case DOT:
                        //.| in body => less mixins
                        proposals.addAll(getMixinsCompletionProposals(context, model));
                }
                break;

        }
        return Utilities.filterCompletionProposals(proposals, context.getPrefix(), true);
    }

    private static Collection<CompletionProposal> getVariableCompletionProposals(final CompletionContext context, CPModel model) {
        //filter the variable at the current location (being typed)
        Collection<CompletionProposal> proposals = new LinkedHashSet<>();
        for (CPElement var : model.getVariables(context.getCaretOffset())) {
            if (var.getType() != CPElementType.VARIABLE_USAGE && !var.getRange().containsInclusive(context.getCaretOffset())) {
                ElementHandle handle = new CPCslElementHandle(context.getFileObject(), var.getName());
                VariableCompletionItem item = new VariableCompletionItem(
                        handle,
                        var.getHandle(),
                        context.getAnchorOffset(),
                        null); //no origin for current file
//                        var.getFile() == null ? null : var.getFile().getNameExt());

                proposals.add(item);
            }
        }
        try {
            //now gather global vars from all linked sheets
            FileObject file = context.getFileObject();
            if (file != null) {
                Map<FileObject, CPCssIndexModel> indexModels = CPUtils.getIndexModels(file, DependencyType.REFERRING_AND_REFERRED, true);
                for (Entry<FileObject, CPCssIndexModel> entry : indexModels.entrySet()) {
                    FileObject reff = entry.getKey();
                    CPCssIndexModel cpIndexModel = entry.getValue();
                    Collection<org.netbeans.modules.css.prep.editor.model.CPElementHandle> variables = cpIndexModel.getVariables();
                    for (org.netbeans.modules.css.prep.editor.model.CPElementHandle var : variables) {
                        if (var.getType() == CPElementType.VARIABLE_GLOBAL_DECLARATION) {
                            ElementHandle handle = new CPCslElementHandle(context.getFileObject(), var.getName());
                            VariableCompletionItem item = new VariableCompletionItem(
                                    handle,
                                    var,
                                    context.getAnchorOffset(),
                                    reff.getNameExt());

                            proposals.add(item);
                        }

                    }

                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return proposals;
    }

    private static Collection<CompletionProposal> getMixinsCompletionProposals(final CompletionContext context, CPModel model) {
        //filter the variable at the current location (being typed)
        Collection<CompletionProposal> proposals = new LinkedHashSet<>();
        for (CPElement mixin : model.getMixins()) {
            if (mixin.getType() == CPElementType.MIXIN_DECLARATION) {
                ElementHandle handle = new CPCslElementHandle(context.getFileObject(), mixin.getName());
                MixinCompletionItem item = new MixinCompletionItem(
                        handle,
                        mixin.getHandle(),
                        context.getAnchorOffset(),
                        null); //no origin for current file
//                        var.getFile() == null ? null : var.getFile().getNameExt());

                proposals.add(item);
            }
        }
        try {
            //now gather global vars from all linked sheets
            FileObject file = context.getFileObject();
            if (file != null) {
                Map<FileObject, CPCssIndexModel> indexModels = CPUtils.getIndexModels(file, DependencyType.REFERRING_AND_REFERRED, true);
                for (Entry<FileObject, CPCssIndexModel> entry : indexModels.entrySet()) {
                    FileObject reff = entry.getKey();
                    CPCssIndexModel cpIndexModel = entry.getValue();
                    Collection<org.netbeans.modules.css.prep.editor.model.CPElementHandle> mixins = cpIndexModel.getMixins();
                    for (org.netbeans.modules.css.prep.editor.model.CPElementHandle mixin : mixins) {
                        if (mixin.getType() == CPElementType.MIXIN_DECLARATION) {
                            ElementHandle handle = new CPCslElementHandle(context.getFileObject(), mixin.getName());
                            MixinCompletionItem item = new MixinCompletionItem(
                                    handle,
                                    mixin,
                                    context.getAnchorOffset(),
                                    reff.getNameExt());

                            proposals.add(item);
                        }

                    }

                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return proposals;
    }

    @Override
    public <T extends Map<OffsetRange, Set<ColoringAttributes>>> NodeVisitor<T> getSemanticHighlightingNodeVisitor(FeatureContext context, T result) {
        final Snapshot snapshot = context.getSnapshot();
        return new NodeVisitor<T>(result) {
            @Override
            public boolean visit(Node node) {
                ColoringAttributes coloring = getColorings().get(node.type());
                if (coloring != null) {
                    int dso = snapshot.getOriginalOffset(node.from());
                    int deo = snapshot.getOriginalOffset(node.to());
                    if (dso >= 0 && deo >= 0) { //filter virtual nodes
                        //check vendor speficic property
                        OffsetRange range = new OffsetRange(dso, deo);
                        getResult().put(range, Collections.singleton(coloring));
                    }
                }
                return false;
            }
        };
    }

    private static Map<NodeType, ColoringAttributes> getColorings() {
        if (COLORINGS == null) {
            COLORINGS = new EnumMap<>(NodeType.class);
            COLORINGS.put(NodeType.cp_variable, ColoringAttributes.LOCAL_VARIABLE);
            COLORINGS.put(NodeType.cp_mixin_name, ColoringAttributes.PRIVATE);
        }
        return COLORINGS;
    }

    @Override
    public <T extends Set<OffsetRange>> NodeVisitor<T> getMarkOccurrencesNodeVisitor(EditorFeatureContext context, T result) {
        return Utilities.createMarkOccurrencesNodeVisitor(context, result, NodeType.cp_variable, NodeType.cp_mixin_name);
    }

    @Override
    public boolean isInstantRenameAllowed(EditorFeatureContext context) {
        TokenSequence<CssTokenId> tokenSequence = context.getTokenSequence();
        int diff = tokenSequence.move(context.getCaretOffset());
        if (diff > 0 && tokenSequence.moveNext() || diff == 0 && tokenSequence.movePrevious()) {
            Token<CssTokenId> token = tokenSequence.token();
            return token.id() == CssTokenId.AT_IDENT //less
                    || token.id() == CssTokenId.SASS_VAR //sass
                    || token.id() == CssTokenId.IDENT; //sass/less mixin name

        }
        return false;
    }

    @Override
    public <T extends Set<OffsetRange>> NodeVisitor<T> getInstantRenamerVisitor(EditorFeatureContext context, T result) {
        TokenSequence<CssTokenId> tokenSequence = context.getTokenSequence();
        int diff = tokenSequence.move(context.getCaretOffset());
        if (diff > 0 && tokenSequence.moveNext() || diff == 0 && tokenSequence.movePrevious()) {
            Token<CssTokenId> token = tokenSequence.token();
            final CharSequence elementName = token.text();
            return new NodeVisitor<T>(result) {
                @Override
                public boolean visit(Node node) {
                    switch (node.type()) {
                        case cp_mixin_name:
                        case cp_variable:
                            if (LexerUtils.equals(elementName, node.image(), false, false)) {
                            OffsetRange range = new OffsetRange(node.from(), node.to());
                            getResult().add(range);
                            break;
                        }
                    }
                    return false;
                }
            };

        }
        return null;
    }

    @Override
    public Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>> getDeclaration(Document document, int caretOffset) {
        //first try to find the reference span
        TokenSequence<CssTokenId> ts = LexerUtils.getJoinedTokenSequence(document, caretOffset, CssTokenId.language());
        if (ts == null) {
            return null;
        }

        OffsetRange foundRange = null;
        Token<CssTokenId> token = ts.token();
        int quotesDiff = WebUtils.isValueQuoted(ts.token().text().toString()) ? 1 : 0;
        OffsetRange range = new OffsetRange(ts.offset() + quotesDiff, ts.offset() + ts.token().length() - quotesDiff);
        CharSequence mixinName;

        //MIXINs go to declaration
        switch (token.id()) {
            case IDENT:
                mixinName = token.text();

                //check if there is @import token before
                while (ts.movePrevious() && ts.token().id() == CssTokenId.WS) {
                }

                Token t = ts.token();
                if (t != null) {
                    if (t.id() == CssTokenId.DOT || t.id() == CssTokenId.SASS_INCLUDE) {
                        //gotcha!
                        //@import xxx --sass
                        //.xxx --less
                        foundRange = range;
                    }
                }
                if (foundRange == null) {
                    return null;
                }

                final CharSequence searchedMixinName = mixinName;
                FutureParamTask<DeclarationLocation, EditorFeatureContext> callable = new FutureParamTask<DeclarationLocation, EditorFeatureContext>() {
                    @Override
                    public DeclarationLocation run(EditorFeatureContext context) {
                        final Collection<Pair<CPCslElementHandle, Snapshot>> locations = new ArrayList<>();
                        //first look at the current file
                        CPModel model = CPModel.getModel(context.getParserResult());
                        for (CPElement mixin : model.getMixins()) {
                            if (mixin.getType() == CPElementType.MIXIN_DECLARATION) {
                                if (LexerUtils.equals(searchedMixinName, mixin.getName(), false, false)) {
                                    locations.add(
                                            Pair.of(new CPCslElementHandle(
                                            context.getFileObject(), mixin.getName(), mixin.getRange(), mixin.getType()),
                                            context.getSnapshot()));
                                }
                            }
                        }

                        //then look at the referred files
                        try {
                            Map<FileObject, CPCssIndexModel> indexModels = CPUtils.getIndexModels(context.getFileObject(), DependencyType.REFERRING_AND_REFERRED, true);
                            for (Entry<FileObject, CPCssIndexModel> entry : indexModels.entrySet()) {
                                final CPCssIndexModel im = entry.getValue();
                                final FileObject file = entry.getKey();
                                Source source = Source.create(file);
                                ParserManager.parse(Collections.singleton(source), new UserTask() {
                                    @Override
                                    public void run(ResultIterator resultIterator) throws Exception {
                                        ResultIterator cssRI = WebUtils.getResultIterator(resultIterator, "text/css");
                                        if (cssRI != null) {
                                            Parser.Result parserResult = cssRI.getParserResult();
                                            if (parserResult instanceof CssParserResult) {
                                                CssParserResult result = (CssParserResult) parserResult;
                                                CPModel model = CPModel.getModel(result);
                                                for (CPElementHandle mixin : im.getMixins()) {
                                                    if (mixin.getType() == CPElementType.MIXIN_DECLARATION
                                                            && LexerUtils.equals(searchedMixinName, mixin.getName(), false, false)) {
                                                        CPElement element = mixin.resolve(model);
                                                        if (element != null) {
                                                            locations.add(Pair.of(new CPCslElementHandle(
                                                                    file, mixin.getName(), element.getRange(), mixin.getType()),
                                                                    result.getSnapshot()));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        } catch (ParseException | IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        if (locations.isEmpty()) {
                            return DeclarationLocation.NONE;
                        } else {
                            Iterator<Pair<CPCslElementHandle, Snapshot>> itr = locations.iterator();
                            DeclarationLocation main = null;

                            while (itr.hasNext()) {
                                Pair<CPCslElementHandle, Snapshot> item = itr.next();
                                CPCslElementHandle handle = item.first();
                                Snapshot snapshot = item.second();
                                Lines lines = new Lines(snapshot.getText());
                                DeclarationLocation location = new DeclarationLocation(
                                        handle.getFileObject(), handle.getOffsetRange(null).getStart());
                                if (main == null) {
                                    main = location;
                                }
                                DeclarationFinder.AlternativeLocation alternative
                                        = new CpAlternativeLocation(handle, location, snapshot, lines, handle.getFileObject().equals(context.getSource().getFileObject()));
                                main.addAlternative(alternative);
                            }
                            return main;
                        }

                    }
                };
                return Pair.<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>>of(foundRange, callable);

            case SASS_VAR:
            case AT_IDENT: //less var //TODO - add default directives - see the css grammar file comment about that
                //cp variable
                final String varName = token.text().toString();
                foundRange = new OffsetRange(ts.offset(), ts.offset() + ts.token().length());

                callable = new FutureParamTask<DeclarationLocation, EditorFeatureContext>() {
                    @Override
                    public DeclarationLocation run(EditorFeatureContext context) {
                        final Collection<Pair<CPCslElementHandle, Snapshot>> locations = new ArrayList<>();
                        //first look at the current file
                        CPModel model = CPModel.getModel(context.getParserResult());
                        for (CPElement var : model.getVariables()) {
                            if (var.getType().isOfTypes(CPElementType.VARIABLE_GLOBAL_DECLARATION, CPElementType.VARIABLE_LOCAL_DECLARATION, CPElementType.VARIABLE_DECLARATION_IN_BLOCK_CONTROL)) {
                                if (LexerUtils.equals(varName, var.getName(), false, false)) {
                                    locations.add(
                                            Pair.of(
                                            new CPCslElementHandle(
                                            context.getFileObject(),
                                            var.getName(),
                                            var.getRange(),
                                            var.getType()),
                                            context.getSnapshot()));
                                }
                            }
                        }
                        try {
                            //then look at the referred files
                            Map<FileObject, CPCssIndexModel> indexModels = CPUtils.getIndexModels(context.getFileObject(), DependencyType.REFERRING_AND_REFERRED, true);
                            for (Entry<FileObject, CPCssIndexModel> entry : indexModels.entrySet()) {
                                final CPCssIndexModel im = entry.getValue();
                                final FileObject file = entry.getKey();
                                Source source = Source.create(file);
                                ParserManager.parse(Collections.singleton(source), new UserTask() {
                                    @Override
                                    public void run(ResultIterator resultIterator) throws Exception {
                                        ResultIterator cssRI = WebUtils.getResultIterator(resultIterator, "text/css");
                                        if (cssRI != null) {
                                            CssParserResult result = (CssParserResult) cssRI.getParserResult();
                                            CPModel model = CPModel.getModel(result);
                                            for (CPElementHandle var : im.getVariables()) {
                                                if (var.getType() == CPElementType.VARIABLE_GLOBAL_DECLARATION && var.getName().equals(varName)) {
                                                    CPElement element = var.resolve(CPModel.getModel(file));
                                                    if (element != null) {
                                                        locations.add(
                                                                Pair.of(
                                                                new CPCslElementHandle(
                                                                file,
                                                                var.getName(),
                                                                element.getRange(),
                                                                var.getType()),
                                                                result.getSnapshot()
                                                                ));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        } catch (ParseException | IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        if (locations.isEmpty()) {
                            return DeclarationLocation.NONE;
                        } else {
                            Iterator<Pair<CPCslElementHandle, Snapshot>> itr = locations.iterator();
                            DeclarationLocation main = null;

                            while (itr.hasNext()) {
                                Pair<CPCslElementHandle, Snapshot> item = itr.next();
                                CPCslElementHandle handle = item.first();
                                Snapshot snapshot = item.second();
                                Lines lines = new Lines(snapshot.getText());
                                DeclarationLocation location = new DeclarationLocation(
                                        handle.getFileObject(), handle.getOffsetRange(null).getStart());
                                if (main == null) {
                                    main = location;
                                }
                                DeclarationFinder.AlternativeLocation alternative
                                        = new CpAlternativeLocation(handle, location, snapshot, lines, handle.getFileObject().equals(context.getSource().getFileObject()));
                                main.addAlternative(alternative);
                            }
                            return main;
                        }
                    }
                };
                return Pair.<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>>of(foundRange, callable);

            default:
                return null;
        }

    }

    @Override
    public <T extends Map<String, List<OffsetRange>>> NodeVisitor<T> getFoldsNodeVisitor(FeatureContext context, T result) {
        final Snapshot snapshot = context.getSnapshot();
        final Lines lines = new Lines(snapshot.getText());

        return new NodeVisitor<T>(result) {
            @Override
            public boolean visit(Node node) {
                switch (node.type()) {
                    case sass_control_block:
                    case cp_mixin_block:
                    case sass_map:
                    case sass_function_declaration:
                        //find the ruleSet curly brackets and create the fold between them inclusive
                        int from = node.from();
                        int to = node.to();
                        try {
                            //do not creare one line folds
                            if (lines.getLineIndex(from) < lines.getLineIndex(to)) {
                                List<OffsetRange> codeblocks = getResult().get("codeblocks"); //NOI18N
                                if (codeblocks == null) {
                                    codeblocks = new ArrayList<>();
                                    getResult().put("codeblocks", codeblocks); //NOI18N
                                }

                                codeblocks.add(new OffsetRange(from, to));
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                }
                return false;
            }
        };

    }

    @Override
    public <T extends List<StructureItem>> NodeVisitor<T> getStructureItemsNodeVisitor(FeatureContext context, T result) {

        final Set<StructureItem> vars = new HashSet<>();
        final Set<StructureItem> mixins = new HashSet<>();

        CPModel model = CPModel.getModel(context.getParserResult());
        for (CPElement element : model.getElements()) {
            switch (element.getType()) {
                case MIXIN_DECLARATION:
                    mixins.add(new CPStructureItem.Mixin(element));
                    break;
//                case VARIABLE_DECLARATION_IN_BLOCK_CONTROL:
                case VARIABLE_GLOBAL_DECLARATION:
//                case VARIABLE_LOCAL_DECLARATION:
                    vars.add(new CPStructureItem.Variable(element));
                    break;
            }
        }

        if (!vars.isEmpty()) {
            result.add(new CPCategoryStructureItem.Variables(vars, context));
        }
        if (!mixins.isEmpty()) {
            result.add(new CPCategoryStructureItem.Mixins(mixins, context));
        }

        //XXX ugly - we need no visitor, but still forced to return one
        return new NodeVisitor<T>() {
            @Override
            public boolean visit(Node node) {
                return true;
            }
        };

    }

    @Override
    public Collection<String> getPseudoClasses(EditorFeatureContext context) {
        Collection<String> result = null;
        if (CPUtils.LESS_FILE_MIMETYPE.equals(context.getSource().getMimeType())) {
            result = PSEUDO_CLASSES;
        }
        return result;
    }

    private static class CpAlternativeLocation implements DeclarationFinder.AlternativeLocation {

        private static final int TEXT_MAX_LENGTH = 50;

        private final CPCslElementHandle handle;
        private final DeclarationLocation location;
        private int lineIndex = -1;
        private String lineText;
        private final boolean currentFile;

        public CpAlternativeLocation(CPCslElementHandle handle, DeclarationLocation location, Snapshot snapshot, Lines lines, boolean currentFile) {
            this.handle = handle;
            this.location = location;
            this.currentFile = currentFile;
            try {
                lineIndex = lines.getLineIndex(location.getOffset());
                //line bounds
                int from = lines.getLineOffset(lineIndex);
                int to = lines.getLinesCount() > (lineIndex + 1)
                        ? lines.getLineOffset(lineIndex + 1)
                        : snapshot.getText().length();
                lineText = snapshot.getText().subSequence(from, to).toString();
            } catch (BadLocationException ex) {
                Logger.getLogger(CpAlternativeLocation.class.getName()).log(Level.INFO, null, ex);
            }
        }

        @Override
        public ElementHandle getElement() {
            return handle;
        }

        @Override
        public String getDisplayHtml(HtmlFormatter b) {
            //line text section
            if (lineText != null) {
                //split the text to three parts: the element text itself, its prefix and postfix
                //then render the element test in bold
                String elementText = handle.getName();

                String prefix = "";
                String postfix = "";
                //strip the line to the body start
                int elementIndex = lineText.indexOf(elementText);
                if (elementIndex >= 0) {
                    //find the closest opening curly bracket or NL forward
                    int to;
                    for (to = elementIndex; to < lineText.length(); to++) {
                        char c = lineText.charAt(to);
                        if (c == '{' || c == '\n') {
                            break;
                        }
                    }
                    //now find nearest closing curly bracket or newline backward
                    int from;
                    for (from = elementIndex; from >= 0; from--) {
                        char ch = lineText.charAt(from);
                        if (ch == '}' || ch == '\n') {
                            break;
                        }
                    }

                    prefix = lineText.substring(from + 1, elementIndex).trim();
                    postfix = lineText.substring(elementIndex + elementText.length(), to).trim();

                    //now strip the prefix and postfix so the whole text is not longer than SELECTOR_TEXT_MAX_LENGTH
                    int overlap = (prefix.length() + elementText.length() + postfix.length()) - TEXT_MAX_LENGTH;
                    if (overlap > 0) {
                        //strip
                        int stripFromPrefix = Math.min(overlap / 2, prefix.length());
                        prefix = ".." + prefix.substring(stripFromPrefix);
                        int stripFromPostfix = Math.min(overlap - stripFromPrefix, postfix.length());
                        postfix = postfix.substring(0, postfix.length() - stripFromPostfix) + "..";
                    }
                }

                b.appendHtml("<span>");//NOI18N
                b.appendText(prefix);
                b.appendText(" "); //NOI18N
                b.appendHtml("<b>"); //NOI18N
                b.appendText(elementText);
                b.appendHtml("</b>"); //NOI18N
                b.appendText(" "); //NOI18N
                b.appendText(postfix);
                b.appendHtml("</span>"); //NOI18N
            }

            //file:offset section
            b.appendHtml("<span>");
            if (!isLocalDeclaration()) {
                //add a link to the file relative to the web root
                FileObject file = location.getFileObject();
                FileObject pathRoot = ProjectWebRootQuery.getWebRoot(file);

                String path = null;
                String resolveTo = null;
                if (pathRoot != null) {
                    path = FileUtil.getRelativePath(pathRoot, file); //this may also return null
                }
                if (path == null) {
                    //the file cannot be resolved relatively to the webroot or no webroot found
                    //try to resolve relative path to the project's root folder
                    Project project = FileOwnerQuery.getOwner(file);
                    if (project != null) {
                        pathRoot = project.getProjectDirectory();
                        path = FileUtil.getRelativePath(pathRoot, file); //this may also return null
                        if (path != null) {
                            resolveTo = "${project.home}/"; //NOI18N
                        }
                    }
                }

                if (path == null) {
                    //if everything fails, just use the absolute path
                    path = file.getPath();
                }
                b.appendText(" in ");
                if (resolveTo != null) {
                    b.appendHtml("<i>"); //NOI18N
                    b.appendText(resolveTo);
                    b.appendHtml("</i>"); //NOI18N
                }
                b.appendText(path);
                if (lineIndex != -1) {
                    b.appendText(":"); //NOI18N
                    b.appendText(Integer.toString(lineIndex + 1)); //line offsets are counted from zero, but in editor lines starts with one.
                }

            } else {
                b.appendText(" at line ");
                b.appendText(Integer.toString((lineIndex + 1)));
            }
            b.appendHtml("</span>");

            return b.getText();
        }

        @Override
        public DeclarationLocation getLocation() {
            return location;
        }

        @Override
        public int compareTo(DeclarationFinder.AlternativeLocation o) {
            CpAlternativeLocation cpal = (CpAlternativeLocation) o;

            if (isLocalDeclaration() == cpal.isLocalDeclaration()) {
                return location.getFileObject().getPath().compareTo(o.getLocation().getFileObject().getPath());
            } else {
                return isLocalDeclaration() && !cpal.isLocalDeclaration() ? -1 : +1;
            }
        }

        /**
         * Is the declaration location in the current file?
         */
        private boolean isLocalDeclaration() {
            return currentFile;
        }
    }

}
