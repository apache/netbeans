/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.editor.module.main;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.CssHelpResolver;
import org.netbeans.modules.css.editor.csl.CssNodeElement;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.editor.module.spi.FutureParamTask;
import org.netbeans.modules.css.editor.module.spi.HelpResolver;
import org.netbeans.modules.css.editor.module.spi.Utilities;
import org.netbeans.modules.css.lib.api.CssModule;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.Lines;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default implementation covering the basic CSS3 features.
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = CssEditorModule.class)
public class DefaultCssEditorModule extends CssEditorModule {

    private static final String MODULE_PATH_BASE = "org/netbeans/modules/css/editor/module/main/properties/"; //NOI18N    
    private static final CssModule[] MODULE_PROPERTY_DEFINITION_FILE_NAMES = new CssModule[]{
        module("default_module", "http://www.w3.org/TR/CSS2"), //NOI18N
        module("marquee", "http://www.w3.org/TR/css3-marquee"), //NOI18N
        module("ruby", "http://www.w3.org/TR/css3-ruby"), //NOI18N
        module("multi-column_layout", "http://www.w3.org/TR/css3-multicol"), //NOI18N
        module("values_and_units", "http://www.w3.org/TR/css3-values"), //NOI18N
        module("text", "http://www.w3.org/TR/css-text-4"), //NOI18N
        module("writing_modes", "http://www.w3.org/TR/css3-writing-modes"), //NOI18N
        module("generated_content_for_paged_media", "http://www.w3.org/TR/css3-gcpm"), //NOI18N
        module("fonts", "http://www.w3.org/TR/css3-fonts"), //NOI18N
        module("basic_box_model", "http://www.w3.org/TR/css3-box"), //NOI18N
        module("speech", "http://www.w3.org/TR/css3-speech"), //NOI18N
        module("grid", "http://www.w3.org/TR/css3-grid"), //NOI18N
        module("flexible_box_layout", "http://www.w3.org/TR/css3-flexbox"), //NOI18N
        module("image_values", "http://www.w3.org/TR/css3-images"), //NOI18N
        module("animations", "http://www.w3.org/TR/css3-animations"), //NOI18N
        module("transforms_2d", "http://www.w3.org/TR/css3-2d-transforms"), //NOI18N
        module("transforms_3d", "http://www.w3.org/TR/css3-3d-transforms"), //NOI18N
        module("transitions", "http://www.w3.org/TR/css3-transitions"), //NOI18N
        module("line", "http://www.w3.org/TR/css3-linebox"), //NOI18N
        module("hyperlinks", "http://www.w3.org/TR/css3-hyperlinks"), //NOI18N
        module("presentation_levels", "http://www.w3.org/TR/css3-preslev"), //NOI18N
        module("generated_and_replaced_content", "http://www.w3.org/TR/css3-content"), //NOI18N
        module("alignment", "http://www.w3.org/TR/css-align-3"), //NOI18N
        module("fragmentation", "http://www.w3.org/TR/css-break-3"), //NOI18N
        module("positioning", "http://www.w3.org/TR/css-position-3"), //NOI18N
        module("sizing", "http://www.w3.org/TR/css-sizing-3"), //NOI18N
        module("contain", "http://www.w3.org/TR/css-contain-3"), //NOI18N
        module("other", null) //NOI18N
    };
    private static Map<String, PropertyDefinition> propertyDescriptors;

    private static CssModule module(String name, String url) {
        return new DefaultCssModule(name, url);
    }

    private static class DefaultCssModule implements CssModule {

        private final String name, url;

        public DefaultCssModule(String name, String url) {
            this.name = name;
            this.url = url;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(this.getClass(), Constants.CSS_MODULE_DISPLAYNAME_BUNDLE_KEY_PREFIX + getName());
        }

        @Override
        public String getSpecificationURL() {
            return url;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("DefaultCssModule(")
                    .append(getDisplayName())
                    .append('/')
                    .append(getSpecificationURL())
                    .append(')').toString();
        }

    }

    private static class Css21HelpResolver extends HelpResolver {

        @Override
        public String getHelp(FileObject context, PropertyDefinition property) {
            return CssHelpResolver.instance().getPropertyHelp(property.getName());
        }

        @Override
        public URL resolveLink(FileObject context, PropertyDefinition property, String link) {
//            return CssHelpResolver.getHelpZIPURLasString() == null ? null :
//            new ElementHandle.UrlHandle(CssHelpResolver.getHelpZIPURLasString() +
//                    normalizeLink( elementHandle, link));
            return null;
        }

        @Override
        public int getPriority() {
            return 100;
        }
    }

    private synchronized Map<String, PropertyDefinition> getProperties() {
        if (propertyDescriptors == null) {
            propertyDescriptors = new HashMap<>();
            for (CssModule module : MODULE_PROPERTY_DEFINITION_FILE_NAMES) {
                String path = MODULE_PATH_BASE + module.getName();
                propertyDescriptors.putAll(Utilities.parsePropertyDefinitionFile(path, module));
            }

        }
        return propertyDescriptors;
    }

    @Override
    public Collection<String> getPropertyNames(FileObject file) {
        return getProperties().keySet();
    }

    @Override
    public PropertyDefinition getPropertyDefinition(String propertyName) {
        PropertyDefinition pd = getProperties().get(propertyName);
        if (pd != null || propertyName == null) {
            return pd;
        } else {
            return getProperties().get(propertyName.toLowerCase());
        }
    }

    @Override
    public Collection<HelpResolver> getHelpResolvers(FileObject context) {
        //CSS2.1 legacy help - to be removed
        return Arrays.asList(new HelpResolver[]{
            new Css21HelpResolver(),
            new PropertyCompatibilityHelpResolver(),
            new StandardPropertiesHelpResolver()
        });
    }

    @Override
    public <T extends Map<OffsetRange, Set<ColoringAttributes>>> NodeVisitor<T> getSemanticHighlightingNodeVisitor(FeatureContext context, T result) {
        final Snapshot snapshot = context.getSnapshot();
        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                switch (node.type()) {
                    case elementName:
                    case cssId:
                    case cssClass:
                        //Bug 207764 - error syntax highlight
                        //filter out virtual selectors (@@@)
                        if (LexerUtils.equals(
                                org.netbeans.modules.web.common.api.Constants.LANGUAGE_SNIPPET_SEPARATOR,
                                node.image(), false, false)) {
                            break; //@@@ node
                        }

                        int dso = snapshot.getOriginalOffset(node.from());
                        if (dso == -1) {
                            //try next offset - for virtually created class and id
                            //selectors the . an # prefix are virtual code and is not
                            //a part of the source document, try to highlight just
                            //the class or id name
                            dso = snapshot.getOriginalOffset(node.from() + 1);
                        }
                        int deo = snapshot.getOriginalOffset(node.to());
                        //filter out generated and inlined style definitions - they have just virtual selector which
                        //is mapped to empty string
                        if (dso >= 0 && deo >= 0) {
                            OffsetRange range = new OffsetRange(dso, deo);
                            getResult().put(range, ColoringAttributes.METHOD_SET);
                        }
                        break;

                    case property:
                        //do not overlap with sass interpolation expression sem.coloring
                        //xxx this needs to be solved somehow grafefully!
                        if (NodeUtil.getChildByType(node, NodeType.token) == null || node.children().size() > 1) {
                        break; //not "normal" property which can only have one GEN or IDENT child token node
                    }

                        dso = snapshot.getOriginalOffset(node.from());
                        deo = snapshot.getOriginalOffset(node.to());
                        if (dso >= 0 && deo >= 0) { //filter virtual nodes
                            //check vendor speficic property
                            OffsetRange range = new OffsetRange(dso, deo);

                            CharSequence propertyName = node.unescapedImage();
                            if (Css3Utils.containsGeneratedCode(propertyName)) {
                                return false;
                            }

                            Set<ColoringAttributes> ca;
                            if (Css3Utils.isVendorSpecificProperty(propertyName)) {
                                //special highlight for vend. spec. properties
                                ca = ColoringAttributes.CUSTOM2_SET;
                            } else {
                                //normal property
                                ca = ColoringAttributes.CUSTOM1_SET;
                            }
                            getResult().put(range, ca);

                        }
                        break;
                    case slAttributeName: //attribute name in selector
                    case fnAttributeName: //attribute name in css function
                        OffsetRange range = Css3Utils.getDocumentOffsetRange(node, snapshot);
                        if (Css3Utils.isValidOffsetRange(range)) {
                            getResult().put(range, ColoringAttributes.CUSTOM1_SET);

                        }

                }
                return false;
            }
        };
    }

    @Override
    public <T extends Set<OffsetRange>> NodeVisitor<T> getMarkOccurrencesNodeVisitor(EditorFeatureContext context, T result) {

        final Snapshot snapshot = context.getSnapshot();

        int astCaretOffset = snapshot.getEmbeddedOffset(context.getCaretOffset());
        if (astCaretOffset == -1) {
            return null;
        }

        Node realCurrent = NodeUtil.findNodeAtOffset(context.getParseTreeRoot(), astCaretOffset);

        Node current = NodeUtil.findNonTokenNodeAtOffset(context.getParseTreeRoot(), astCaretOffset);
//        if (current == null) {
//            //this may happen if the offset falls to the area outside the selectors rule node.
//            //(for example when the stylesheet starts or ends with whitespaces or comment and
//            //and the offset falls there).
//            //In such case root node (with null parent) is returned from NodeUtil.findNodeAtOffset()
//            return null;
//        }

        //process only some interesting nodes
        final NodeType currentNodeType = current != null ? current.type() : null;
        final CharSequence currentNodeImage = current != null ? current.image() : null;
        final NodeType realCurrentNodeType = realCurrent.type();
        final CharSequence realCurrentNodeImage = realCurrent.image();

        if (current != null && NodeUtil.isSelectorNode(current)) {
            return new NodeVisitor<T>(result) {
                @Override
                public boolean visit(Node node) {
                    if (currentNodeType == node.type()) {
                        boolean ignoreCase = currentNodeType == NodeType.hexColor;
                        if (LexerUtils.equals(currentNodeImage, node.image(), ignoreCase, false)) {

                            int[] trimmedNodeRange = NodeUtil.getTrimmedNodeRange(node);
                            int docFrom = snapshot.getOriginalOffset(trimmedNodeRange[0]);

                            //virtual class or id handling - the class and id elements inside
                            //html tag's CLASS or ID attribute has the dot or hash prefix just virtual
                            //so if we want to highlight such occurances we need to increment the
                            //start offset by one
                            if (docFrom == -1 && (node.type() == NodeType.cssClass || node.type() == NodeType.cssId)) {
                                docFrom = snapshot.getOriginalOffset(trimmedNodeRange[0] + 1); //lets try +1 offset
                            }

                            int docTo = snapshot.getOriginalOffset(trimmedNodeRange[1]);

                            if (docFrom != -1 && docTo != -1) {
                                getResult().add(new OffsetRange(docFrom, docTo));
                            }
                        }
                    }

                    return false;

                }
            };
        } else if (realCurrent.type() == NodeType.token
            && NodeUtil.getTokenNodeTokenId(realCurrent) == CssTokenId.VARIABLE) {
            return new NodeVisitor<T>(result) {
                @Override
                public boolean visit(Node node) {
                    if (realCurrentNodeType == node.type()
                        && NodeUtil.getTokenNodeTokenId(node) == CssTokenId.VARIABLE
                        && realCurrentNodeImage.equals(node.image())) {
                        int[] trimmedNodeRange = NodeUtil.getTrimmedNodeRange(node);
                        int docFrom = snapshot.getOriginalOffset(trimmedNodeRange[0]);
                        int docTo = snapshot.getOriginalOffset(trimmedNodeRange[1]);

                        if (docFrom != -1 && docTo != -1) {
                            getResult().add(new OffsetRange(docFrom, docTo));
                        }
                    }

                    return false;

                }
            };
        }

        return null;
    }

    @Override
    public <T extends Map<String, List<OffsetRange>>> NodeVisitor<T> getFoldsNodeVisitor(FeatureContext context, T result) {
        final Snapshot snapshot = context.getSnapshot();
        final Lines lines = new Lines(snapshot.getText());

        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                int from = -1, to = -1;
                switch (node.type()) {
                    case rule:
                    case media:
                    case page:
                    case webkitKeyframes:
                    case generic_at_rule:
                    case vendorAtRule:
                        //find the ruleSet curly brackets and create the fold between them inclusive
                        Node[] tokenNodes = NodeUtil.getChildrenByType(node, NodeType.token);
                        for (Node leafNode : tokenNodes) {
                            if (CharSequenceUtilities.equals("{", leafNode.image())) {
                                from = leafNode.from();
                            } else if (CharSequenceUtilities.equals("}", leafNode.image())) {
                                to = leafNode.to();
                            }
                        }

                        if (from != -1 && to != -1) {
                            int doc_from = snapshot.getOriginalOffset(from);
                            int doc_to = snapshot.getOriginalOffset(to);

                            try {
                                //check the boundaries a bit
                                if (doc_from >= 0 && doc_to >= 0) {
                                    //do not creare one line folds
                                    if (lines.getLineIndex(from) < lines.getLineIndex(to)) {

                                        List<OffsetRange> codeblocks = getResult().get("codeblocks"); //NOI18N
                                        if (codeblocks == null) {
                                            codeblocks = new ArrayList<>();
                                            getResult().put("codeblocks", codeblocks); //NOI18N
                                        }

                                        codeblocks.add(new OffsetRange(doc_from, doc_to));
                                    }
                                }
                            } catch (BadLocationException ex) {
                                //ignore
                            }
                        }

                }
                return false;
            }
        };

    }

    @Override
    public Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>> getDeclaration(final Document document, final int caretOffset) {
        //first try to find the reference span
        final AtomicReference<OffsetRange> result = new AtomicReference<>();
        document.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<CssTokenId> ts = LexerUtils.getJoinedTokenSequence(document, caretOffset, CssTokenId.language());
                if (ts == null) {
                    return;
                }

                Token<CssTokenId> token = ts.token();
                switch (token.id()) {
                    case STRING:
                        //check if there is @import token before
                        if (LexerUtils.followsToken(ts, CssTokenId.IMPORT_SYM, true, true, CssTokenId.WS, CssTokenId.NL) != null) {
                        int quotesDiff = WebUtils.isValueQuoted(ts.token().text().toString()) ? 1 : 0;
                        OffsetRange range = new OffsetRange(ts.offset() + quotesDiff, ts.offset() + ts.token().length() - quotesDiff);
                        result.set(range);
                    }
                        break;
                    case URI:
                        Matcher m = Css3Utils.URI_PATTERN.matcher(ts.token().text());
                        if (m.matches()) {
                            int groupIndex = 1;
                            String value = m.group(groupIndex);
                            int quotesDiff = WebUtils.isValueQuoted(value) ? 1 : 0;
                            result.set(new OffsetRange(ts.offset() + m.start(groupIndex) + quotesDiff, ts.offset() + m.end(groupIndex) - quotesDiff));
                        }

                }
            }

        });

        if (result.get() == null) {
            return null;
        }

        //if span found then create the future task which will finally create the declaration location
        //possibly using also parser result
        FutureParamTask<DeclarationLocation, EditorFeatureContext> callable = new FutureParamTask<DeclarationLocation, EditorFeatureContext>() {

            @Override
            public DeclarationLocation run(final EditorFeatureContext context) {
                final AtomicReference<DeclarationLocation> result = new AtomicReference<>();
                context.getDocument().render(new Runnable() {

                    @Override
                    public void run() {
                        final TokenSequence<CssTokenId> ts = LexerUtils.getJoinedTokenSequence(context.getDocument(), context.getCaretOffset(), CssTokenId.language());
                        if (ts == null) {
                            return;
                        }

                        Token<CssTokenId> valueToken = ts.token();
                        String valueText = valueToken.text().toString();

                        //adjust the value if a part of an URI
                        if (valueToken.id() == CssTokenId.URI) {
                            Matcher m = Css3Utils.URI_PATTERN.matcher(valueToken.text());
                            if (m.matches()) {
                                int groupIndex = 1;
                                valueText = m.group(groupIndex);
                            }
                        }

                        valueText = WebUtils.unquotedValue(valueText);

                        FileObject resolved = WebUtils.resolve(context.getSource().getFileObject(), valueText);
                        result.set(resolved != null ? new DeclarationLocation(resolved, 0) : DeclarationLocation.NONE);
                    }
                });
                return result.get();

            }
        };

        return Pair.<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>>of(result.get(), callable);
    }

    @Override
    public <T extends List<StructureItem>> NodeVisitor<T> getStructureItemsNodeVisitor(final FeatureContext context, final T result) {

        final List<StructureItem> imports = new ArrayList<>();
        final List<StructureItem> rules = new ArrayList<>();
        final List<StructureItem> atrules = new ArrayList<>();
        final Set<StructureItem> classes = new HashSet<>();
        final Set<StructureItem> ids = new HashSet<>();
        final Set<StructureItem> elements = new HashSet<>();

        final Snapshot snapshot = context.getSnapshot();
        final FileObject file = context.getFileObject();

        return new NodeVisitor<T>() {

            private void addElement(StructureItem si) {
                if (elements.isEmpty()) {
                    result.add(new TopLevelStructureItem.Elements(elements));
                }
                elements.add(si);
            }

            private void addRule(StructureItem si) {
                if (rules.isEmpty()) {
                    result.add(new TopLevelStructureItem.Rules(rules, context));
                }
                rules.add(si);
            }

            private void addAtRule(StructureItem si) {
                if (atrules.isEmpty()) {
                    result.add(new TopLevelStructureItem.AtRules(atrules));
                }
                atrules.add(si);
            }

            private void addId(StructureItem si) {
                if (ids.isEmpty()) {
                    result.add(new TopLevelStructureItem.Ids(ids));
                }
                ids.add(si);
            }

            private void addClass(StructureItem si) {
                if (classes.isEmpty()) {
                    result.add(new TopLevelStructureItem.Classes(classes));
                }
                classes.add(si);
            }

            private void addImport(StructureItem si) {
                if (imports.isEmpty()) {
                    result.add(new TopLevelStructureItem.Imports(imports));
                }
                imports.add(si);
            }

            @Override
            public boolean visit(Node node) {
                assert(node.from() != -1 && node.to() != -1);
                if (node.from() == -1 || node.to() == -1) {
                    return false;
                }
                switch (node.type()) {
                    case selectorsGroup: //rules
                        //get parent - ruleSet to obtain the { ... } range 
                        Node ruleNode = node.parent();
                        if (ruleNode.type() == NodeType.rule) {
                            int so = snapshot.getOriginalOffset(ruleNode.from());
                            int eo = snapshot.getOriginalOffset(ruleNode.to());
                            if (eo > so) {
                                //todo: filter out virtual selectors
                                StructureItem item = new CssRuleStructureItem(node.unescapedImage(), CssNodeElement.createElement(file, ruleNode), snapshot);
                                addRule(item);
                            }
                        }
                        break;
                    case elementName: //element
                        addElement(new CssRuleStructureItemHashableByName(node.unescapedImage(), CssNodeElement.createElement(file, node), snapshot));
                        break;
                    case cssClass:
                        addClass(new CssRuleStructureItemHashableByName(node.unescapedImage(), CssNodeElement.createElement(file, node), snapshot));
                        break;
                    case cssId:
                        addId(new CssRuleStructureItemHashableByName(node.unescapedImage(), CssNodeElement.createElement(file, node), snapshot));
                        break;
                    case charSet:
                    case imports:
                    case namespace:
                        addAtRule(new CssRuleStructureItem(node.unescapedImage(), CssNodeElement.createElement(file, node), snapshot));
                        break;
                    case fontFace:
                        Node tokenNode = NodeUtil.getChildTokenNode(node, CssTokenId.FONT_FACE_SYM);
                        addAtRule(new CssRuleStructureItem(tokenNode.unescapedImage(), CssNodeElement.createElement(file, node), snapshot));
                        break;
                    case mediaQueryList:
                        Node mediaNode = node.parent();
                        StringBuilder image = new StringBuilder();
                        if (mediaNode.type() == NodeType.media) {
                            image.append("@media "); //NOI18N
                            image.append(node.unescapedImage());
                            addAtRule(new CssRuleStructureItem(image, CssNodeElement.createElement(file, mediaNode), snapshot));
                        }
                        break;
                    case page:
                        Node pageSymbolNode = NodeUtil.getChildTokenNode(node, CssTokenId.PAGE_SYM);
                        Node lbraceSymbolNode = NodeUtil.getChildTokenNode(node, CssTokenId.LBRACE);
                        if (pageSymbolNode != null && lbraceSymbolNode != null) {
                            CharSequence headingAreaImage = snapshot.getText().subSequence(pageSymbolNode.from(), lbraceSymbolNode.from());
                            addAtRule(new CssRuleStructureItem(headingAreaImage, CssNodeElement.createElement(file, node), snapshot));
                        }
                        break;
                    case counterStyle:
                        Node identNode = NodeUtil.getChildTokenNode(node, CssTokenId.IDENT);
                        if (identNode != null) {
                            image = new StringBuilder();
                            image.append("@counter-style "); //NOI18N
                            image.append(identNode.unescapedImage());
                            addAtRule(new CssRuleStructureItem(image, CssNodeElement.createElement(file, node), snapshot));
                        }
                        break;
                    case importItem:
                        Node[] resourceIdentifiers = NodeUtil.getChildrenByType(node, NodeType.resourceIdentifier);
                        for (Node ri : resourceIdentifiers) {
                            addImport(new CssRuleStructureItem(WebUtils.unquotedValue(ri.unescapedImage()), CssNodeElement.createElement(file, ri), snapshot));
                        }
                        break;

                }

                return false;
            }
        };

    }

    @Override
    public boolean isInstantRenameAllowed(EditorFeatureContext context) {
        CssParserResult result = context.getParserResult();
        Snapshot snapshot = result.getSnapshot();
        Node node = NodeUtil.findNodeAtOffset(result.getParseTree(), snapshot.getEmbeddedOffset(context.getCaretOffset()));
        return node.type() == NodeType.token && NodeUtil.getTokenNodeTokenId(node) == CssTokenId.VARIABLE;
    }

    @Override
    public <T extends Set<OffsetRange>> NodeVisitor<T> getInstantRenamerVisitor(final EditorFeatureContext context, final T result) {
        CssParserResult parserResult = context.getParserResult();
        final Snapshot snapshot = parserResult.getSnapshot();
        Node node = NodeUtil.findNodeAtOffset(parserResult.getParseTree(), snapshot.getEmbeddedOffset(context.getCaretOffset()));

        final NodeType targetNodeType;
        final CharSequence targetImage;

        if(node.type() == NodeType.token && NodeUtil.getTokenNodeTokenId(node) == CssTokenId.VARIABLE) {
            targetNodeType = node.type();
            targetImage = node.image();
        } else {
            targetNodeType = null;
            targetImage = null;
        }

        return new NodeVisitor<T>(result) {
            @Override
            public boolean visit(Node node) {
                if(targetNodeType == null || targetImage == null) {
                    return true;
                }

                if (node.type() == targetNodeType && targetImage.equals(node.image())) {
                    int[] trimmedNodeRange = NodeUtil.getTrimmedNodeRange(node);
                    int docFrom = snapshot.getOriginalOffset(trimmedNodeRange[0]);
                    int docTo = snapshot.getOriginalOffset(trimmedNodeRange[1]);

                    if (docFrom != -1 && docTo != -1) {
                        getResult().add(new OffsetRange(docFrom, docTo));
                    }
                }

                return false;
            }
        };
    }

    @Override
    public List<CompletionProposal> getCompletionProposals(CompletionContext context) {
        if(context.getActiveTokenId() == null) {
            return Collections.emptyList();
        }

        // This sequence implements variable completion proposals. That is
        // the completion items consist of the variables declared in the file.
        TokenSequence ts = context.getTokenSequence();

        // Build prefix from the context
        boolean prefixRead = false;
        StringBuilder prefixBuilder = new StringBuilder();

        // If the current token is already recoginized as a VARIABLE, the context
        // contains the slice of the variable from the start to the position of
        // the caret.
        if(ts.token().id() == CssTokenId.VARIABLE) {
            prefixBuilder.append(context.getPrefix());
            prefixRead = true;
            ts.movePrevious();
        }

        // We scan backwards in the token stream till we reach a (, which
        // is a potential function start. While scanning backwards, we skip
        // three elements:
        // - whitespace (does not matter when parsing)
        // - a single - (indicated by a MINUS token
        // - two -- (indicated by a MINUS token and an ERROR token
        // The latter two must be considered as prefixes of the value to search
        // and for the anchor calculation
        Token openParen = null;
        do {
            if(ts.token().id() == CssTokenId.LPAREN) {
                openParen = ts.token();
                break;
            } else if (ts.token().id() == CssTokenId.WS) {
                prefixRead = true;
            } else if ((! prefixRead) && (ts.token().id() == CssTokenId.MINUS || ts.token().id() == CssTokenId.ERROR)) {
                prefixBuilder.insert(0, ts.token().text());
            }
        } while((ts.token().id() == CssTokenId.WS
            || ts.token().id() == CssTokenId.MINUS
            || ts.token().id() == CssTokenId.ERROR) && ts.movePrevious());

        String prefix = prefixBuilder.toString();

        // If we found an open parenthesis, we speculate, that we are in a var
        // function
        if(openParen != null) {
            // scan further back, skipping whitespace to find the next identifier
            // if it is "var", we complete the first position of a var function
            Token ident = LexerUtils.followsToken(
                ts,
                Collections.singleton(CssTokenId.IDENT),
                true,
                false,
                false,
                CssTokenId.WS
            );
            if(ident != null && "var".equals(ident.text())) {
                // There are two sources for "variable" names:
                // - declared custom properties are of course considered
                // - all variable uses are also considered, that way we can
                //   support variables from imports, if they are at least once
                //   manually used.

                Set<String> variables = new TreeSet<>();

                variables.addAll(NodeUtil
                    .getChildrenRecursivelyByType(context.getParseTreeRoot(), NodeType.property)
                    .stream()
                    .map(Node::image)
                    .map(CharSequence::toString)
                    .filter(s -> s.startsWith("--"))
                    .filter(s -> s.startsWith(prefix))
                    .collect(Collectors.toList()));

                variables.addAll(NodeUtil
                    .getChildrenRecursivelyByType(context.getParseTreeRoot(), NodeType.token)
                    .stream()
                    .filter(n -> NodeUtil.getTokenNodeTokenId(n) == CssTokenId.VARIABLE)
                    .map(Node::image)
                    .map(CharSequence::toString)
                    .filter(s -> s.startsWith("--"))
                    .filter(s -> s.startsWith(prefix))
                    .collect(Collectors.toList()));

                // the currently modified variable can be part of the found items
                // this assumes, that we don't want to complete to ourself
                variables.remove(prefix);

                context.restoreTokenSequence();

                return Utilities.createRAWCompletionProposals(
                    variables,
                    ElementKind.VARIABLE,
                    context.getAnchorOffset() - prefix.length() + context.getPrefix().length());
            }
        }
        context.restoreTokenSequence();
        return Collections.emptyList();
    }

    private static class CssRuleStructureItemHashableByName extends CssRuleStructureItem {

        public CssRuleStructureItemHashableByName(CharSequence name, CssNodeElement element, Snapshot source) {
            super(name, element, source);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CssRuleStructureItemHashableByName other = (CssRuleStructureItemHashableByName) obj;
            if (this.getName() != other.getName() && (this.getName() == null || !this.getName().equals(other.getName()))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
            return hash;
        }
    }
}
