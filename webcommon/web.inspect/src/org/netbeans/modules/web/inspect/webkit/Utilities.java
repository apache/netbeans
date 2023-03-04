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
package org.netbeans.modules.web.inspect.webkit;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.AtRule;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.BodyItem;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Element;
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.MediaBody;
import org.netbeans.modules.css.model.api.MediaQueryList;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.PropertyValue;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.webkit.debugging.api.css.Property;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.netbeans.modules.web.webkit.debugging.api.css.SourceRange;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetBody;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetOrigin;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 * WebKit-related utility methods that don't fit well anywhere else.
 *
 * @author Jan Stola
 */
public class Utilities {

    /**
     * Finds the specified WebKit rule in the source file.
     *
     * @param sourceModel source model where the rule should be found.
     * @param styleSheet style sheet where the rule should be found.
     * @param rule rule to find.
     * @return source model representation of the specified WebKit rule.
     */
    public static org.netbeans.modules.css.model.api.Rule findRuleInStyleSheet(
            final Model sourceModel, StyleSheet styleSheet, Rule rule) {
        String selector = CSSUtils.normalizeSelector(rule.getSelector());
        String mediaQuery = null;
        for (org.netbeans.modules.web.webkit.debugging.api.css.Media media : rule.getMedia()) {
            if (media.getSource() == org.netbeans.modules.web.webkit.debugging.api.css.Media.Source.MEDIA_RULE) {
                mediaQuery = media.getText();
                mediaQuery = CSSUtils.normalizeMediaQuery(mediaQuery);
            }
        }
        Set<String> properties = new HashSet<String>();
        for (Property property : rule.getStyle().getProperties()) {
            if (property.getText() == null) {
                // longhand property that is included in the rule
                // indirectly through the corresponding shorthand property
                continue;
            }
            String propertyName = property.getName();
            properties.add(propertyName.trim());
        }
        int sourceLine = rule.getSourceLine();
        SourceRange range = rule.getSelectorRange();
        int startOffset = (range == null) ? Short.MIN_VALUE : range.getStart();
        if (startOffset == -1) {
            int line = range.getStartLine();
            if (line != -1) {
                try {
                    startOffset = LexerUtils.getLineBeginningOffset(sourceModel.getModelSource(), line);
                    startOffset += range.getStartColumn();
                } catch (BadLocationException blex) {
                    Exceptions.printStackTrace(blex);
                }
            }
        }
        org.netbeans.modules.css.model.api.Rule result = findRuleInStyleSheet0(
                sourceModel, styleSheet, selector, mediaQuery, properties,
                sourceLine, startOffset);
        if (result == null) {
            // rule.getSelector() sometimes returns value that differs slightly
            // from the selector in the source file. Besides whitespace changes
            // (that we attempt to handle using CSSUtils.normalizeSelector())
            // there are changes like replacement of a colon in pseudo-elements
            // by a double color (i.e. :after becomes ::after) etc. That's why
            // the rule may not be found despite being in the source file.
            // We attempt to run the search again with the real selector
            // from the source file in this case. Unfortunately, getSelectorRange()
            // method sometimes returns incorrect values. That's why we use
            // it as a fallback only.
            StyleSheetBody parentStyleSheet = rule.getParentStyleSheet();
            if (parentStyleSheet != null && range != null) {
                String styleSheetText = parentStyleSheet.getText();
                if (styleSheetText != null) {
                    int start = range.getStart();
                    int end = range.getEnd();
                    int startLine = range.getStartLine();
                    if (start == -1 && startLine != -1) {
                        try {
                            styleSheetText = styleSheetText.replace("\r", ""); // NOI18N
                            start = LexerUtils.getLineBeginningOffset(sourceModel.getModelSource(), startLine);
                            start += range.getStartColumn();
                            end = LexerUtils.getLineBeginningOffset(sourceModel.getModelSource(), range.getEndLine());
                            end += range.getEndColumn();
                        } catch (BadLocationException blex) {
                            Exceptions.printStackTrace(blex);
                        }
                    }
                    selector = styleSheetText.substring(start, end);
                    selector = CSSUtils.normalizeSelector(selector);
                    result = findRuleInStyleSheet0(sourceModel, styleSheet,
                            selector, mediaQuery, properties, sourceLine, startOffset);
                    if ((result == null) && !rule.getMedia().isEmpty() && (range.getStart() == 0)) {
                        // Workaround for a bug in WebKit (already fixed in the latest
                        // versions of Chrome, but still present in WebView)
                        boolean inLiteral = false;
                        int index = selector.length()-1;
                        outer: while (index >= 0) {
                            char c = selector.charAt(index);
                            switch (c) {
                                case '"':
                                    inLiteral = !inLiteral; break;
                                case '{':
                                case '}':
                                    if (inLiteral) {
                                        break;
                                    } else {
                                        break outer;
                                    }
                                default:
                            }
                            index--;
                        }
                        if (index != -1) {
                            selector = selector.substring(index+1);
                            selector = CSSUtils.normalizeSelector(selector);
                            result = findRuleInStyleSheet0(sourceModel,
                                    styleSheet, selector, mediaQuery,
                                    properties, sourceLine, startOffset);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Finds a rule with the specified selector in the source file.
     *
     * @param sourceModel source model where the rule should be found.
     * @param styleSheet style sheet where the rule should be found.
     * @param selector selector of the rule to find.
     * @param mediaQuery media query of the rule (can be {@code null}).
     * @param properties name of the properties in the rule.
     * @param sourceLine (the first) source line of the rule.
     * @param startOffset starting offset of the rule.
     * @return source model representation of a rule with the specified selector.
     */
    private static org.netbeans.modules.css.model.api.Rule findRuleInStyleSheet0(
            final Model sourceModel, StyleSheet styleSheet,
            final String selector, final String mediaQuery,
            final Set<String> properties, final int sourceLine,
            final int startOffset) {
        final org.netbeans.modules.css.model.api.Rule[] result =
                new org.netbeans.modules.css.model.api.Rule[1];

        styleSheet.accept(new ModelVisitor.Adapter() {
            /** Value of the best matching rule so far. */
            private int bestMatchValue = 0;

            @Override
            public void visitRule(org.netbeans.modules.css.model.api.Rule rule) {
                SelectorsGroup selectorGroup = rule.getSelectorsGroup();
                CharSequence image = sourceModel.getElementSource(selectorGroup);
                Element parent = rule.getParent();
                if (parent instanceof MediaBody) {
                    parent = parent.getParent();
                // } else if (parent instanceof MediaBodyItem) {
                } else if (parent != null && parent.getParent() instanceof MediaBody) {
                    parent = parent.getParent().getParent();
                }
                String queryListText = null;
                if (parent instanceof Media) {
                    Media media = (Media)parent;
                    MediaQueryList queryList = media.getMediaQueryList();
                    queryListText = sourceModel.getElementSource(queryList).toString();
                    queryListText = CSSUtils.normalizeMediaQuery(queryListText);
                }
                String selectorInFile = CSSUtils.normalizeSelector(image.toString());
                if (selector.equals(selectorInFile) &&
                        ((mediaQuery == null) ? (queryListText == null) : mediaQuery.equals(queryListText))) {
                    int matchValue = matchValue(rule);
                    if (matchValue >= bestMatchValue) {
                        bestMatchValue = matchValue;
                        result[0] = rule;
                    }
                }
            }

            /**
             * Determines how well the properties in the specified rule
             * match to the properties of the rule we are searching for.
             * 
             * @param rule rule to check.
             * @return value of the matching (the higher the better match).
             */
            private int matchValue(org.netbeans.modules.css.model.api.Rule rule) {
                int value = 0;
                Declarations declarations = rule.getDeclarations();
                if (declarations != null) {
                    for (Declaration declaration : declarations.getDeclarations()) {
                        org.netbeans.modules.css.model.api.Property modelProperty = declaration.getPropertyDeclaration().getProperty();
                        String modelPropertyName = modelProperty.getContent().toString().trim();
                        if (properties.contains(modelPropertyName)) {
                            value += 2;
                        }
                    }
                }
                int offset = rule.getStartOffset();
                // The CSS model never uses CR+LF line ends, but the browser
                // does. Hence, the second part of the following check.
                if ((offset == startOffset) || (offset+sourceLine == startOffset)) {
                    try {
                        int line = LexerUtils.getLineOffset(sourceModel.getModelSource(), offset);
                        if (line == sourceLine) {
                            value += 1;
                        }
                    } catch (BadLocationException blex) {}
                }
                return value;
            }
        });

        return result[0];
    }

    /**
     * Jumps into the meta-source of the specified rule. It does nothing
     * if there is no meta-source of the rule, i.e., when the rule comes
     * directly from some CSS stylesheet.
     * 
     * @param rule rule to jump to.
     * @return {@code true} when the rule comes from some meta-source
     * and the corresponding source file was opened successfully,
     * returns {@code false} otherwise.
     */
    public static boolean goToMetaSource(org.netbeans.modules.css.model.api.Rule rule) {
        Element parent = rule.getParent();
        if (parent instanceof BodyItem) {
            BodyItem bodyItem = (BodyItem)parent;
            parent = bodyItem.getParent();
            if (parent instanceof Body) {
                Body body = (Body)parent;
                List<BodyItem> bodyItems = body.getBodyItems();
                int index = bodyItems.indexOf(bodyItem);
                if (index > 0) {
                    BodyItem previousBodyItem = bodyItems.get(index-1);
                    Element element = previousBodyItem.getElement();
                    if (element instanceof AtRule) {
                        element = ((AtRule)element).getElement();
                    }
                    if (element instanceof Media) {
                        Media media = (Media)element;
                        if (isMetaSourceInfo(media)) {
                            return goToMetaSource(media);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines whether the given media represents information
     * about the meta-source of the next rule.
     * 
     * @param media media to check.
     * @return {@code true} when the given media holds source-related
     * information, returns {@code false} otherwise.
     */
    private static boolean isMetaSourceInfo(Media media) {
        MediaQueryList queryList = media.getMediaQueryList();
        Model sourceModel = media.getModel();
        String queryListText = sourceModel.getElementSource(queryList).toString();
        return "-sass-debug-info".equals(queryListText); // NOI18N
    }

    private static Collection<org.netbeans.modules.css.model.api.Rule> getRules(Media media) {
        return media.getMediaBody() == null ? Collections.<org.netbeans.modules.css.model.api.Rule>emptySet() : media.getMediaBody().getRules();
    }
    
    /**
     * Jumps into the meta-source of the rule that follows the given media
     * (that holds the information about the meta-source).
     * 
     * @param media media holding the source-related information.
     * @return {@code true} when the source file was opened successfully,
     * returns {@code false} otherwise.
     */
    private static boolean goToMetaSource(Media media) {
        String originalFileName = null;
        int originalLineNumber = -1;
        for (org.netbeans.modules.css.model.api.Rule rule : getRules(media)) {
            SelectorsGroup selectorGroup = rule.getSelectorsGroup();
            Model sourceModel = media.getModel();
            CharSequence image = sourceModel.getElementSource(selectorGroup);
            String selector = image.toString();
            if ("filename".equals(selector)) { // NOI18N
                String value = propertyValue(rule, "font-family"); // NOI18N
                if (value != null) {
                    StringBuilder sb = new StringBuilder();
                    boolean slash = false;
                    for (int i=0; i<value.length(); i++) {
                        char c = value.charAt(i);
                        if (slash && (c != ':' && c != '/' && c != '.')) {
                            sb.append('\\');
                        }
                        slash = !slash && (c == '\\');
                        if (!slash) {
                            sb.append(c);
                        }
                    }
                    originalFileName = sb.toString();
                    String prefix = "file://"; // NOI18N
                    if (originalFileName.startsWith(prefix)) {
                        originalFileName = originalFileName.substring(prefix.length());
                    }
                }
            } else if ("line".equals(selector)) { // NOI18N
                String value = propertyValue(rule, "font-family"); // NOI18N
                String prefix = "\\00003"; // NOI18N
                if (value != null && value.startsWith(prefix)) {
                    String lineTxt = value.substring(prefix.length());
                    try {
                        originalLineNumber = Integer.parseInt(lineTxt);
                    } catch (NumberFormatException nfex) {
                        Logger.getLogger(Utilities.class.getName()).log(Level.INFO, null, nfex);
                    }
                }
            }
        }
        if (originalFileName != null && originalLineNumber != -1) {
            File file = new File(originalFileName);
            file = FileUtil.normalizeFile(file);
            final FileObject fob = FileUtil.toFileObject(file);
            if (fob != null) {
                final int lineNo = originalLineNumber - 1;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        CSSUtils.openAtLine(fob, lineNo);
                    }
                });
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value of the specified property in the given rule.
     * 
     * @param rule rule to extract the value from.
     * @param propertyName name of the property whose value should be returned.
     * @return value of the property or {@code null} when there is no
     * property with the specified name in the given rule.
     */
    private static String propertyValue(org.netbeans.modules.css.model.api.Rule rule, String propertyName) {
        String propertyValue = null;
        Declarations declarations = rule.getDeclarations();
        if (declarations != null) {
            for (Declaration declaration : declarations.getDeclarations()) {
                org.netbeans.modules.css.model.api.Property modelProperty = declaration.getPropertyDeclaration().getProperty();
                String modelPropertyName = modelProperty.getContent().toString().trim();
                if (propertyName.equals(modelPropertyName)) {
                    PropertyValue value = declaration.getPropertyDeclaration().getPropertyValue();
                    propertyValue = value.getExpression().getContent().toString();
                }
            }
        }
        return propertyValue;
    }

    /**
     * Determines whether the specified rule should be shown in CSS Styles view.
     * 
     * @param rule rule to check.
     * @return {@code true} when the rule should be shown in CSS Styles view,
     * returns {@code false} otherwise.
     */
    public static boolean showInCSSStyles(Rule rule) {
        return (rule.getOrigin() != StyleSheetOrigin.USER_AGENT);
    }

    /**
     * Finds a node that represents the specified rule in a tree
     * represented by the given root node.
     *
     * @param root root of a tree to search.
     * @param rule rule to find.
     * @return node that represents the rule or {@code null}.
     */
    public static Node findRule(Node root, Rule rule) {
        Rule candidate = root.getLookup().lookup(Rule.class);
        if (candidate != null && Objects.equals(rule.getId(), candidate.getId())
                && Objects.equals(rule.getSourceURL(), candidate.getSourceURL())
                && Objects.equals(rule.getSelector(), candidate.getSelector())) {
            return root;
        }
        for (Node node : root.getChildren().getNodes()) {
            Node result = findRule(node, rule);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns {@code CssParserResult}s (including the embedded ones) that
     * correspond to the given {@code ResultIterator}.
     * 
     * @param resultIterator {@code ResultIterator} to process.
     * @return {@code CssParserResult}s contained in the given {@code ResultIterator}.
     * @throws ParseException when there is a parsing problem.
     */
    public static List<CssParserResult> cssParserResults(ResultIterator resultIterator)
            throws ParseException {
        List<ResultIterator> resultIterators = new ArrayList<ResultIterator>();
        resultIterators.add(resultIterator);
        for (Embedding embedding : resultIterator.getEmbeddings()) {
            String mimeType = embedding.getMimeType();
            if ("text/css".equals(mimeType)) { // NOI18N
                resultIterators.add(resultIterator.getResultIterator(embedding));
            }
        }
        List<CssParserResult> parserResults = new ArrayList<CssParserResult>(resultIterators.size());
        for (ResultIterator iterator : resultIterators) {
            Parser.Result parserResult = iterator.getParserResult();
            if (parserResult instanceof CssParserResult) {
                parserResults.add((CssParserResult)parserResult);
            }
        }
        return parserResults;
    }

    /**
     * Returns name of the resource relative to the project directory.
     *
     * @param resourceUrl absolute name/URL of the resource.
     * @param project project owning the resource.
     * @return relative name of the resource.
     */
    public static String relativeResourceName(String resourceUrl, Project project) {
        String name = resourceUrl;
        if (project != null) {
            FileObject fob = new Resource(project, resourceUrl).toFileObject();
            if (fob != null) {
                FileObject projectDir = project.getProjectDirectory();
                String relativePath = FileUtil.getRelativePath(projectDir, fob);
                if (relativePath != null) {
                    name = relativePath;
                }
            }
        }
        return name;
    }

    /**
     * Finds a source node that corresponds to the specified DOM node.
     * 
     * @param result parsing result of the source file.
     * @param node DOM node whose counterpart should be found.
     * @return the best source approximation of the specified DOM node.
     */
    public static org.netbeans.modules.html.editor.lib.api.elements.Node findNode(HtmlParsingResult result,
            org.netbeans.modules.web.webkit.debugging.api.dom.Node node) {
        org.netbeans.modules.web.webkit.debugging.api.dom.Node domParent = node;
        org.netbeans.modules.html.editor.lib.api.elements.Node root = result.root();
        org.netbeans.modules.html.editor.lib.api.elements.Node nearestNode = root;

        // Find a root of our search. The possible roots
        // are either the HTML tag or nodes with ID.
        while (domParent != null) {
            String tagName = domParent.getNodeName();
            org.netbeans.modules.web.webkit.debugging.api.dom.Node.Attribute attribute
                    = domParent.getAttribute("id"); // NOI18N
            String id = (attribute == null) ? null : attribute.getValue();
            org.netbeans.modules.html.editor.lib.api.elements.Node sourceParent;
            if (id != null) {
                sourceParent = findElementByID(root, id);
                if (sourceParent != null) {
                    nearestNode = findNode(node, domParent, sourceParent);
                    break;
                }
            }
            if ("html".equalsIgnoreCase(tagName)) { // NOI18N
                sourceParent = findElementByTagName(root, "html"); // NOI18N
                nearestNode = findNode(node, domParent, sourceParent);
                break;
            }
            domParent = domParent.getParent();
        }
        return nearestNode;
    }

    /**
     * Returns the source node with the specified ID.
     * 
     * @param root root of the source tree to search.
     * @param id ID of the desired source node.
     * @return source node with the specified ID or {@code null}
     * if such a node doesn't exist.
     */
    private static org.netbeans.modules.html.editor.lib.api.elements.Node findElementByID(org.netbeans.modules.html.editor.lib.api.elements.Node root, String id) {
        org.netbeans.modules.html.editor.lib.api.elements.Node result = null;
        if (root instanceof OpenTag) {
            OpenTag tag = (OpenTag)root;
            Attribute attr = tag.getAttribute("id"); // NOI18N
            CharSequence seq = (attr == null) ? null : attr.unquotedValue();
            String nodeId = (seq == null) ? null : seq.toString();
            if (id.equals(nodeId)) {
                result = root;
            } else {
                for (org.netbeans.modules.html.editor.lib.api.elements.Element element : root.children(ElementType.OPEN_TAG)) {
                    result = findElementByID((org.netbeans.modules.html.editor.lib.api.elements.Node)element, id);
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns some source node with the specified tag name.
     * 
     * @param root root of the source tree to search.
     * @param tagName tag name of the desired source node.
     * @return source node with the specified tag name or {@code null}
     * if such node doesn't exist.
     */
    private static org.netbeans.modules.html.editor.lib.api.elements.Node findElementByTagName(org.netbeans.modules.html.editor.lib.api.elements.Node root, String tagName) {
        org.netbeans.modules.html.editor.lib.api.elements.Node result = null;
        if (root instanceof OpenTag) {
            OpenTag tag = ((OpenTag)root);
            String name = tag.name().toString();
            if (tagName.equalsIgnoreCase(name)) {
                result = root;
            } else {
                for (org.netbeans.modules.html.editor.lib.api.elements.Element element : root.children(ElementType.OPEN_TAG)) {
                    result = findElementByTagName((org.netbeans.modules.html.editor.lib.api.elements.Node)element, tagName);
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a source node that corresponds to the specified DOM node.
     * 
     * @param node DOM node whose counterpart should be found.
     * @param domParent node from the parent-chain of the DOM node.
     * @param sourceParent source node that corresponds to {@code domParent}.
     * @return the best source approximation of the specified DOM node.
     */
    private static org.netbeans.modules.html.editor.lib.api.elements.Node findNode(org.netbeans.modules.web.webkit.debugging.api.dom.Node node,
            org.netbeans.modules.web.webkit.debugging.api.dom.Node domParent,
            org.netbeans.modules.html.editor.lib.api.elements.Node sourceParent) {
        List<org.netbeans.modules.web.webkit.debugging.api.dom.Node> parentChain
                = new LinkedList<org.netbeans.modules.web.webkit.debugging.api.dom.Node>();
        org.netbeans.modules.web.webkit.debugging.api.dom.Node parent = node;
        while (parent != domParent) {
            parentChain.add(0, parent);
            parent = parent.getParent();
        }
        parentChain.add(0, parent);
        return findNode(parentChain, sourceParent);
    }

    /**
     * Returns a source node that corresponds to the last element of
     * the DOM parent chain collection.
     * 
     * @param parentChain DOM parent chain of the node whose counterpart
     * should be found (the node itself is the last element of this collection).
     * @param sourceParent source node that corresponds to the first
     * element of the DOM parent chain collection.
     * @return the best source approximation of the specified DOM node.
     */
    private static org.netbeans.modules.html.editor.lib.api.elements.Node findNode(List<org.netbeans.modules.web.webkit.debugging.api.dom.Node> parentChain,
            org.netbeans.modules.html.editor.lib.api.elements.Node sourceParent) {
        if (parentChain.size() == 1) {
            return sourceParent;
        }
        org.netbeans.modules.html.editor.lib.api.elements.Node nextParent = null;
        parentChain.remove(0);
        org.netbeans.modules.web.webkit.debugging.api.dom.Node domChild = parentChain.get(0);
        int domIndex = elementIndexInParent(domChild);
        if (domIndex == -1) {
            return sourceParent;
        }
        List<org.netbeans.modules.html.editor.lib.api.elements.Element> children = new ArrayList<org.netbeans.modules.html.editor.lib.api.elements.Element>(sourceParent.children(ElementType.OPEN_TAG));
        // Try the candidates according to their distance from the original
        // position of the corresponding DOM node.
        for (int i=0; i<=Math.max(domIndex, children.size()-1); i++) {
            int index = domIndex+i;
            if (index < children.size()) {
                org.netbeans.modules.html.editor.lib.api.elements.Node candidate = (org.netbeans.modules.html.editor.lib.api.elements.Node)children.get(index);
                if (match(domChild, candidate)) {
                    nextParent = candidate;
                    break;
                }
            }
            if (i != 0) {
                index = domIndex-i;
                if ((0 <= index) && (index < children.size())) {
                    org.netbeans.modules.html.editor.lib.api.elements.Node candidate = (org.netbeans.modules.html.editor.lib.api.elements.Node)children.get(index);
                    if (match(domChild, candidate)) {
                        nextParent = candidate;
                    }
                }
            }
        }
        return (nextParent == null) ? sourceParent : findNode(parentChain, nextParent);
    }

    /**
     * Determines whether the specified DOM node matches the given
     * source node. The comparison is based on these nodes only
     * (i.e. the surrounding nodes are not considered).
     * 
     * @param domNode DOM node to compare.
     * @param sourceNode source node to compare.
     * @return {@code true} is these given nodes matches,
     * returns {@code false} otherwise.
     */
    private static boolean match(org.netbeans.modules.web.webkit.debugging.api.dom.Node domNode, org.netbeans.modules.html.editor.lib.api.elements.Node sourceNode) {
        if (sourceNode instanceof OpenTag) {
            OpenTag tag = (OpenTag)sourceNode;

            // Check the tag names
            String sourceTagName = tag.name().toString();
            if (!domNode.getNodeName().equalsIgnoreCase(sourceTagName)) {
                return false;
            }
            // Some tags are unique - no need to check anything besides their name.
            if ("html".equalsIgnoreCase(sourceTagName) // NOI18N
                    || "body".equalsIgnoreCase(sourceTagName) // NOI18N
                    || "head".equalsIgnoreCase(sourceTagName) // NOI18N
                    || "title".equalsIgnoreCase(sourceTagName)) { // NOI18N
                return true;
            }

            // Check the ID
            org.netbeans.modules.web.webkit.debugging.api.dom.Node.Attribute domAttr = domNode.getAttribute("id"); // NOI18N
            String domID = (domAttr == null) ? null : domAttr.getValue();
            Attribute sourceAttr = tag.getAttribute("id"); // NOI18N
            CharSequence seq = (sourceAttr == null) ? null : sourceAttr.unquotedValue(); 
            String sourceID = (seq == null) ? null : seq.toString();
            if ((domID == null) != (sourceID == null)) {
                return false;
            }
            if ((domID != null) && domID.equals(sourceID)) {
                return true;
            }

            // Check if attributes are the same
            Map<String,String> attributes = new HashMap<String,String>();
            for (org.netbeans.modules.web.webkit.debugging.api.dom.Node.Attribute attribute : domNode.getAttributes()) {
                String name = attribute.getName().toUpperCase();
                if (ignoreWhenMatching(name)) {
                    continue;
                }
                attributes.put(name, attribute.getValue());
            }
            for (Attribute attribute : tag.attributes()) {
                String name = attribute.name().toString().toUpperCase();
                if (ignoreWhenMatching(name)) {
                    continue;
                }
                String domValue = attributes.get(name);
                CharSequence sourceSeq = attribute.unquotedValue();
                String sourceValue = (sourceSeq == null) ? "" : sourceSeq.toString(); // NOI18N
                if (domValue == null || !domValue.equals(sourceValue)) {
                    return false;
                }
                attributes.remove(name);
            }
            return attributes.isEmpty();
        } else {
            return false;
        }
    }

    /**
     * Determines whether to ignore attribute with the specified name
     * when determining whether two elements match.
     * 
     * @param attribute attribute to check.
     * @return {@code true} when the attribute should be ignore,
     * returns {@code false} otherwise.
     */
    private static boolean ignoreWhenMatching(String attribute) {
        // Ignore STYLE and CLASS attributes - they are often modified
        // dynamically during JavaScript-based styling.
        // DATA- attributes are used to store various data.
        return ("STYLE".equals(attribute) || "CLASS".equals(attribute) || attribute.startsWith("DATA-")); // NOI18N
    }

    /**
     * Returns the index of the specified element in its parent
     * among other elements under this parent.
     * 
     * @param element element whose index should be returned.
     * @return index of the specified element.
     */
    private static int elementIndexInParent(org.netbeans.modules.web.webkit.debugging.api.dom.Node element) {
        int index = 0;
        org.netbeans.modules.web.webkit.debugging.api.dom.Node parent = element.getParent();
        for (org.netbeans.modules.web.webkit.debugging.api.dom.Node child : parent.getChildren()) {
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                if (child == element) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

}
