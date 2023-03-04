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
package org.netbeans.modules.html.editor.lib.api;

import org.netbeans.modules.html.editor.lib.api.foreign.UndeclaredContentResolver;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.lib.*;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Html syntax analyzer result.
 *
 * @author mfukala@netbeans.org
 */
public class SyntaxAnalyzerResult {
    
    /**
     * Artificial namespace for obtaining parse tree for the code filtered by
     * {@link UndeclaredContentResolver}s.
     * 
     * @since 3.25
     */
    public static final String FILTERED_CODE_NAMESPACE = "filtered_code"; //NOI18N

    private static final Logger LOG = Logger.getLogger(SyntaxAnalyzerResult.class.getSimpleName());
    /**
     * special namespace which can be used for obtaining a parse tree of tags
     * with undeclared namespace.
     */
    private AtomicReference<Declaration> declaration;
    private HtmlVersion detectedHtmlVersion;
    private HtmlParseResult htmlParseResult;
    //ns URI to AstNode map
    private Map<String, ParseResult> embeddedCodeParseResults;
    //ns URI to PREFIX map
    private Map<String, Collection<String>> namespaces;
    private ParseResult undeclaredEmbeddedCodeParseResult;
    private Set<String> allPrefixes;
    private UndeclaredContentResolver resolver;
    private HtmlSource source;
    private ElementsParserCache elementsParserCache;
    private MaskedAreas maskedAreas;
    
    private static final UndeclaredContentResolver EMPTY_RESOLVER = new EmptyUndeclaredContentResolver();

    SyntaxAnalyzerResult(HtmlSource source) {
        this(source, EMPTY_RESOLVER);
    }

    SyntaxAnalyzerResult(HtmlSource source, UndeclaredContentResolver resolver) {
        this.source = source;
        this.resolver = resolver;
    }

    public HtmlSource getSource() {
        return source;
    }

    public Iterator<Element> getElementsIterator() {
        return getElementsParserCache().createElementsIterator();
    }

    private synchronized ElementsParserCache getElementsParserCache() {
        if (elementsParserCache == null) {
            CharSequence sourceCode = source.getSourceCode();
            Snapshot snapshot = source.getSnapshot();
            TokenHierarchy hi;
            if (snapshot != null) {
                //use the snapshot's token hierarchy (cached) if possible
                hi = snapshot.getTokenHierarchy();
            } else {
                hi = TokenHierarchy.create(sourceCode, HTMLTokenId.language());
            }
            TokenSequence<HTMLTokenId> tokenSequence = hi.tokenSequence(HTMLTokenId.language());
            elementsParserCache = new ElementsParserCache(source.getSourceCode(), tokenSequence);
        }

        return elementsParserCache;
    }

    /**
     * @deprecated use {@link #getElementsIterator() } instead
     */
    @Deprecated
    public SyntaxAnalyzerElements getElements() {
        return SyntaxAnalyzer.create(source).elements();
    }

    public HtmlVersion getHtmlVersion() {
        long start = System.currentTimeMillis();
        try {
            HtmlVersion detected = getDetectedHtmlVersion();
            HtmlVersion found = HtmlSourceVersionQuery.getSourceCodeVersion(this, detected);
            if (found != null) {
                return found;
            }
            return detected != null ? detected
                    : mayBeXhtml() ? HtmlVersion.getDefaultXhtmlVersion() : HtmlVersion.getDefaultVersion(); //fallback if nothing can be determined
        } finally {
            long end = System.currentTimeMillis();
            log(String.format("getHtmlVersion() took %s ms.", (end - start)));
        }
    }

    public HtmlModel getHtmlModel() {
        return HtmlModelFactory.getModel(getHtmlVersion());
    }

    /**
     * Returns an html version for the specified parser result input. The return
     * value depends on: 1) doctype declaration content 2) if not present, xhtml
     * file extension 3) if not xhtml extension, present of default XHTML
     * namespace declaration
     *
     */
    public synchronized HtmlVersion getDetectedHtmlVersion() {
        if (detectedHtmlVersion == null) {
            detectedHtmlVersion = detectHtmlVersion();
        }
        return detectedHtmlVersion;
    }

    public boolean mayBeXhtml() {
        FileObject fo = getSource().getSourceFileObject();
        String mimeType = fo != null ? fo.getMIMEType() : null;
        return getHtmlTagDefaultNamespace() != null || "text/xhtml".equals(mimeType);
    }

    private HtmlVersion detectHtmlVersion() {
        Declaration doctypeDeclaration = getDoctypeDeclaration();
        if (doctypeDeclaration == null) {
            //no doctype declaration at all
            return null;
        } else {
            //found doctype declaration
            String publicId = getPublicID();
            String namespace = getHtmlTagDefaultNamespace();
            return HtmlVersion.find(publicId, namespace);
        }
    }

    public Collection<ParseResult> getAllParseResults() throws ParseException {
        Collection<ParseResult> all = new ArrayList<>();
        all.add(parseHtml());
        for (String ns : getAllDeclaredNamespaces().keySet()) {
            all.add(parseEmbeddedCode(ns));
        }
        all.add(parseUndeclaredEmbeddedCode());
        return all;
    }

    public synchronized HtmlParseResult parseHtml() throws ParseException {
        if (htmlParseResult == null) {
            htmlParseResult = doParseHtml();
        }
        return htmlParseResult;
    }

    private HtmlParser findParser() {
        HtmlVersion version = getHtmlVersion();
        HtmlParser parser = HtmlParserFactory.findParser(version);
        if (parser == null) {
            throw new IllegalStateException("Cannot find an HtmlParser implementation for "
                    + getHtmlVersion().name()); //NOI18N
        }
        return parser;
    }

    @NonNull
    private Collection<String> getNamespacePrefixes() {
        HtmlVersion version = getHtmlVersion();
        return version.getDefaultNamespace() != null
                ? getAllDeclaredNamespaces().get(version.getDefaultNamespace())
                : Collections.<String>emptyList();

    }

    private HtmlParseResult doParseHtml() throws ParseException {
        log("doParseHtml()...");

        long start = System.currentTimeMillis();
        long justParsingStart = 0;
        try {
            final Collection<String> prefixes = getNamespacePrefixes();
            HtmlParser parser = findParser();

            Iterator<Element> original = getElementsIterator();
            final Iterator<Element> filteredIterator = new FilteredIterator(original, new ElementFilter() {
                @Override
                public boolean accepts(Element node) {
                    switch (node.type()) {
                        case OPEN_TAG:
                        case CLOSE_TAG:
                            Named named = (Named) node;
                            if (resolver.isCustomTag(named, source)) {
                                return false;
                            }

                            CharSequence prefix = named.namespacePrefix();

                            if (prefix == null) {
                                return true; //default namespace, should be html in most cases
                            }
                            if (prefixes != null) {
                                if (prefixes.contains(prefix.toString())) {
                                    //the prefix is mapped to the html namespace
                                    return true;
                                }
                            }
                            break;
                        default:
                            return true;
                    }
                    return false;
                }
            });

            //create a new html source with the cleared areas
            HtmlSource newSource = new HtmlSource(
                    source.getSourceCode(),
                    source.getSnapshot(),
                    source.getSourceFileObject());

            //add the syntax elements to the lookup since the old html4 parser needs them
            InstanceContent content = new InstanceContent();

            //for html5 parser
            content.add(getMaskedAreas(source, FilteredContent.CUSTOM_TAGS, FilteredContent.DECLARED_FOREIGN_XHTML_CONTENT));

            //for SimpleXHTMLParser
            content.add(new ElementsIteratorHandle() {
                @Override
                public Iterator<Element> getIterator() {
                    return filteredIterator;
                }
            });

            Lookup lookup = new AbstractLookup(content);

            justParsingStart = System.currentTimeMillis();

            log("really parsing...");
            return parser.parse(newSource, getHtmlVersion(), lookup);

        } finally {
            long end = System.currentTimeMillis();
            log(String.format("doParseHtml() took %s ms (clear parsing time %s)", (end - start), (end - justParsingStart)));
        }

    }
    
    private synchronized ParseResult doParseFilteredCode() throws ParseException {
        
        Iterator<Element> original = getElementsIterator();
            final Iterator<Element> filteredIterator = new FilteredIterator(original, new ElementFilter() {
                @Override
                public boolean accepts(Element node) {
                    switch (node.type()) {
                        case OPEN_TAG:
                        case CLOSE_TAG:
                            Named named = (Named) node;
                            return resolver.isCustomTag(named, source);
                    }
                    return false;
                }
            });
            
            Node root = XmlSyntaxTreeBuilder.makeUncheckedTree(
                    source,
                    FILTERED_CODE_NAMESPACE,
                    createLookupFor(filteredIterator));

            return new DefaultParseResult(source, root, Collections.<ProblemDescription>emptyList());
            
    }

    public synchronized ParseResult parseEmbeddedCode(String namespace) throws ParseException {
        if (embeddedCodeParseResults == null) {
            embeddedCodeParseResults = new HashMap<>();
        }
        ParseResult result = embeddedCodeParseResults.get(namespace);
        if (result == null) {
            if(FILTERED_CODE_NAMESPACE.equals(namespace)) {
                result = doParseFilteredCode();
            } else {
                result = doParseEmbeddedCode(namespace);
            }
            embeddedCodeParseResults.put(namespace, result);
        }
        return result;
    }

    private ParseResult doParseEmbeddedCode(String namespace) throws ParseException {
        long start = System.currentTimeMillis();
        long justParsingStart = 0;
        try {

            final Collection<String> prefixes = getAllDeclaredNamespaces().get(namespace);
            if (prefixes == null || prefixes.isEmpty()) {
                return new EmptyResult(getSource());
            }

            Iterator<Element> original = getElementsIterator();
            final Iterator<Element> filteredIterator = new FilteredIterator(original, new ElementFilter() {
                @Override
                public boolean accepts(Element node) {
                    switch (node.type()) {
                        case OPEN_TAG:
                        case CLOSE_TAG:
                            Named named = (Named) node;
                            CharSequence prefix = named.namespacePrefix();
                            if (prefix != null && prefixes.contains(prefix.toString())) {
                                return true;
                            }
                            break;
                        default:
                            return true;
                    }
                    return false;
                }
            });

            justParsingStart = System.currentTimeMillis();

            Node root = XmlSyntaxTreeBuilder.makeUncheckedTree(
                    source,
                    namespace,
                    createLookupFor(filteredIterator));

            return new DefaultParseResult(source, root, Collections.<ProblemDescription>emptyList());

        } finally {
            long end = System.currentTimeMillis();
            log(String.format("doParseEmbeddedCode() took %s ms (clear parsing time %s)", (end - start), (end - justParsingStart)));
        }

    }

    private static Lookup createLookupFor(final Iterator<Element> elementsIterator) {
        InstanceContent ic = new InstanceContent();
        ic.add(new ElementsIteratorHandle() {
            @Override
            public Iterator<Element> getIterator() {
                return elementsIterator;
            }
        });
        return new AbstractLookup(ic);
    }

    /**
     * Parse the content as a plain xml-like tree. Any validity checks are not
     * done, just the tag elements are transformed to the tree structure.
     */
    public ParseResult parsePlain() {
        long start = System.currentTimeMillis();
        try {
            Node root = XmlSyntaxTreeBuilder.makeUncheckedTree(source, null, createLookupFor(getElementsIterator()));
            return new DefaultParseResult(source, root, Collections.<ProblemDescription>emptyList());
        } finally {
            long end = System.currentTimeMillis();
            log(String.format("parsePlain() took %s ms", (end - start)));
        }

    }

    public ParseResult parseUndeclaredEmbeddedCode() throws ParseException {
        if (undeclaredEmbeddedCodeParseResult == null) {
            undeclaredEmbeddedCodeParseResult = doParseUndeclaredEmbeddedCode();
        }
        return undeclaredEmbeddedCodeParseResult;
    }

    private ParseResult doParseUndeclaredEmbeddedCode() throws ParseException {
        long start = System.currentTimeMillis();
        try {

            final Collection<String> prefixes = getAllDeclaredPrefixes();

            Iterator<Element> original = getElementsIterator();
            Iterator<Element> filteredIterator = new FilteredIterator(original, new ElementFilter() {
                @Override
                public boolean accepts(Element node) {
                    switch (node.type()) {
                        case OPEN_TAG:
                            OpenTag openTag = (OpenTag) node;
                            for (Attribute attribute : openTag.attributes(new AttributeFilter() {
                                @Override
                                public boolean accepts(Attribute attribute) {
                                    return attribute.namespacePrefix() != null
                                            && !"xmlns".equals(attribute.namespacePrefix()); //NOI18N
                                }
                            })) {
                                if (!prefixes.contains(attribute.namespacePrefix().toString())) {
                                    return true;
                                }
                            }
                            CharSequence otPrefix = openTag.namespacePrefix();
                            if (otPrefix != null && !prefixes.contains(otPrefix.toString())) {
                                return true;
                            }
                            break;
                        case CLOSE_TAG:
                            Named named = (Named) node;
                            CharSequence prefix = named.namespacePrefix();
                            if (prefix != null && !prefixes.contains(prefix.toString())) {
                                return true;
                            }
                            break;
                        default:
                            return true;
                    }
                    return false;
                }
            });


            Node root = XmlSyntaxTreeBuilder.makeUncheckedTree(
                    source,
                    null,
                    createLookupFor(filteredIterator));

            return new DefaultParseResult(source, root, Collections.<ProblemDescription>emptyList());
        } finally {
            long end = System.currentTimeMillis();
            log(String.format("doParseUndeclaredEmbeddedCode() took %s ms", (end - start)));
        }

    }

    /**
     * Gets an instance of {@link MaskedAreas} which represents parts of the
     * code which should be masked from the default html parser.
     *
     * @since 3.18
     * @return
     */
    public synchronized MaskedAreas getMaskedAreas(HtmlSource source, FilteredContent... filteredContent) {
        log("findMaskedAreas...");
        ElementContentFilter filter = new ElementContentFilter(source, filteredContent);

        long start = System.currentTimeMillis();
        try {
            //html5 parser:
            //since the nu.validator.htmlparser parser cannot properly handle the
            //'foreign' namespace content it needs to be filtered from the source
            //before running the parser on the input charsequence
            //
            //so following content needs to be filtere out:
            //1. xmlns non default declarations <html xmlns:f="http:/...
            //2. the prefixed tags and attributes <f:if ...
            //3. non prefixed foreign elements and attributes
            //
            //It looks like since ValidationContext's "filter.foreign.namespaces" 
            //feature is implemented it is no more needed to apply rules for #1 and #2 
            //as the validation will ignore such content.
            List<MaskedArea> ignoredAreas = new ArrayList<>();

            Iterator<Element> itr = getElementsIterator();
            while (itr.hasNext()) {
                Element e = itr.next();
                ignoredAreas.addAll(filter.getMasks(e));
            }

            int[] positions = new int[ignoredAreas.size()];
            int[] lens = new int[ignoredAreas.size()];
            for (int i = 0; i < positions.length; i++) {
                SyntaxAnalyzerResult.MaskedArea ia = ignoredAreas.get(i);
                positions[i] = ia.from;
                lens[i] = ia.to - ia.from;
            }

            return new MaskedAreas(positions, lens);
        } finally {
            long end = System.currentTimeMillis();
            log(String.format("findMaskedAreas() took %s ms.", (end - start)));
        }

    }

    public String getPublicID() {
        Declaration decl = getDoctypeDeclaration();
        if (decl == null) {
            return null;
        }
        CharSequence pid = getDoctypeDeclaration().publicId();
        return pid == null ? null : pid.toString();
    }

    public synchronized Declaration getDoctypeDeclaration() {
        long start = System.currentTimeMillis();
        try {
            if (declaration == null) {
                Declaration declarationElement = null;
                //typically the doctype is at the very first line of the document
                //so limit the doctype search so we do not iterate over the whole file
                //if there's no doctype
                int limitCountdown = 20;
                Iterator<Element> elementsIterator = getElementsIterator();
                while (elementsIterator.hasNext()) {
                    if (limitCountdown-- == 0) {
                        break;
                    }
                    Element e = elementsIterator.next();
                    if (e.type() == ElementType.DECLARATION) {
                        Declaration decl = (Declaration) e;
                        if (isValidDoctype(decl)) {
                            declarationElement = (Declaration) e;
                        }
                        break;
                    }
                }
                declaration = new AtomicReference<>(declarationElement);

            }
            return declaration.get();
        } finally {
            long end = System.currentTimeMillis();
            log(String.format("getDoctypeDeclaration() took %s ms.", (end - start)));
        }
    }

    private static boolean isValidDoctype(Declaration decl) {
        return "doctype".equalsIgnoreCase(decl.declarationName().toString()) && decl.rootElementName() != null; //NOI18N
    }

    /**
     * Returns a map of namespace URI to prefix used in the document Not only
     * globaly registered namespace (root tag) are taken into account.
     */
    // URI to prefix map
    @Deprecated
    public Map<String, String> getDeclaredNamespaces() {
        Map<String, Collection<String>> all = new HashMap<>(getAllDeclaredNamespaces());
        Map<String, String> firstPrefixOnly = new HashMap<>();
        for (Map.Entry<String, Collection<String>> entry : all.entrySet()) {
            String namespace = entry.getKey();
            Collection<String> prefixes = entry.getValue();
            if (prefixes != null && prefixes.size() > 0) {
                firstPrefixOnly.put(namespace, prefixes.iterator().next());
            }
        }
        return firstPrefixOnly;
    }

    public synchronized Set<String> getAllDeclaredPrefixes() {
        if (allPrefixes == null) {
            allPrefixes = findAllDeclaredPrefixes();
        }
        return allPrefixes;
    }

    private Set<String> findAllDeclaredPrefixes() {
        HashSet<String> all = new HashSet<>();
        for (Collection<String> prefixes : getAllDeclaredNamespaces().values()) {
            all.addAll(prefixes);
        }
        return all;
    }

    public String getHtmlTagDefaultNamespace() {
        log("getHtmlTagDefaultNamespace()...");
        long start = System.currentTimeMillis();
        try {
            //typically the html root element is at the beginning of the file
            int limitCountdown = 100;
            Iterator<Element> elementsIterator = getElementsIterator();
            while (elementsIterator.hasNext()) {
                if (limitCountdown-- == 0) {
                    break;
                }
                Element se = elementsIterator.next();
                if (se.type() == ElementType.OPEN_TAG) {
                    //look for the xmlns attribute only in the first tag
                    OpenTag tag = (OpenTag) se;
                    Attribute xmlns = tag.getAttribute("xmlns");
                    if (xmlns != null) {
                        CharSequence value = xmlns.unquotedValue();
                        if (value != null) {
                            return value.toString();
                        }
                    }
                }
            }
            return null;
        } finally {
            long end = System.currentTimeMillis();
            log(String.format("getHtmlTagDefaultNamespace() took %s ms.", (end - start)));
        }
    }

    /**
     *
     * @return map of namespace URI to a List of prefixes. The prefixes in the
     * list are sorted according to their occurrences in the document.
     */
    public synchronized Map<String, Collection<String>> getAllDeclaredNamespaces() {
        log("getAllDeclaredNamespaces...");
        long start = System.currentTimeMillis();
        try {
            if (namespaces == null) {
                this.namespaces = new HashMap<>();

                //add the artificial namespaces to prefix map to the physically declared results
                if (resolver != null) {
                    namespaces.putAll(resolver.getUndeclaredNamespaces(getSource()));
                }

                Iterator<Element> iterator = getElementsIterator();
                while (iterator.hasNext()) {
                    Element se = iterator.next();
                    if (se.type() == ElementType.OPEN_TAG) {
                        OpenTag tag = (OpenTag) se;
                        for (Attribute attr : tag.attributes()) {
                            String attrName = attr.name().toString();
                            if (attrName.startsWith("xmlns")) { //NOI18N
                                int colonIndex = attrName.indexOf(':'); //NOI18N
                                String nsPrefix = colonIndex == -1 ? null : attrName.substring(colonIndex + 1);
                                CharSequence value = attr.unquotedValue();
                                if (value != null) {
                                    String key = value.toString();
                                    //do not overwrite already existing entry
                                    Collection<String> prefixes = namespaces.get(key);
                                    if (prefixes == null) {
                                        prefixes = new LinkedList<>();
                                        prefixes.add(nsPrefix);
                                        namespaces.put(key, prefixes);
                                    } else {
                                        //already existing list of prefixes for the namespace
                                        if (prefixes.contains(key)) {
                                            //just relax
                                        } else {
                                            prefixes.add(nsPrefix);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return namespaces;

        } finally {
            long end = System.currentTimeMillis();
            log(String.format("getAllDeclaredNamespaces() took %s ms.", (end - start)));
        }
    }

    private void log(String message) {
        LOG.log(Level.FINE, new StringBuilder().append("HtmlSource(").append(source.hashCode()).append("):").append(message).toString());
    }
    
    /**
     * @since 3.18
     */
    public static enum FilteredContent {
        /**
         * Filter out custom tags (injected by {@link UndeclaredContentResolver}).
         * 
         * Example: AngularJS custom attributes: <div ng-click="..."/>
         */
        CUSTOM_TAGS,
        
        /**
         * Filter out declared foreign XHTML content.
         * 
         * Example: Facelets tags: <h:panelGroup .../> where h is declared as xmlns:h="http://java.sun.com/jsf/html"
         */
        DECLARED_FOREIGN_XHTML_CONTENT
    }

    private class ElementContentFilter {

        private final Collection<String> prefixes;
        private final Set<FilteredContent> conf;
        private final HtmlSource source;

        public ElementContentFilter(HtmlSource source, FilteredContent... conf) {
            this.source = source;
            this.prefixes = getNamespacePrefixes();
            this.conf = EnumSet.of(conf[0], conf);
        }

        @NonNull
        public Collection<MaskedArea> getMasks(Element element) {
            switch (element.type()) {
                case OPEN_TAG:
                    OpenTag openTag = (OpenTag) element;
                    if (shouldBeFiltered(openTag)) {
                        return Collections.singleton(new MaskedArea(element.from(), element.to()));
                    } else {
                        //should not be masked, but still it may contain
                        //some foreign attributes which needs to be masked
                            List<MaskedArea> masks = null;
                            for (Attribute attr : openTag.attributes()) {
                                String name = attr.unqualifiedName().toString();
                                //1. check for xmlns prefixed attrs
                                //2. check for custom attributes
                                if (LexerUtils.startsWith(name, "xmlns:", true, false)
                                    || resolver.isCustomAttribute(attr, source)) {
                                    if (masks == null) {
                                        masks = new ArrayList<>();
                                    }
                                    masks.add(new MaskedArea(attr.from(), attr.to()));
                                }
                            }
                            return masks == null ? Collections.<MaskedArea>emptySet() : masks;
                    }

                case CLOSE_TAG:
                    if (shouldBeFiltered((Named) element)) {
                        return Collections.singleton(new MaskedArea(element.from(), element.to()));
                    }
                    break;
            }
            return Collections.emptySet(); //do not mask
        }

        private boolean shouldBeFiltered(Named named) {
            //1. first check the custom tags, can be with or w/o ns prefix
            if (conf.contains(FilteredContent.CUSTOM_TAGS) && resolver.isCustomTag(named, source)) {
                return true;
            }

            //2. then if the element is w/o prefix, do not filter it out
            CharSequence nsPrefix = named.namespacePrefix();
            if (nsPrefix == null) {
                return false;
            }
            
            //3. finally when we have element w/ prefix check if the prefix 
            //is mapped to the default namespace
            if(conf.contains(FilteredContent.DECLARED_FOREIGN_XHTML_CONTENT)) {
                if(prefixes == null || !prefixes.contains(nsPrefix.toString())) {
                    return true;
                }
            }

            //do not filter the element
            return false;
        }
    }

    private static class FilteredIterator implements Iterator<Element> {

        private Iterator<Element> source;
        private ElementFilter filter;
        private Element next;

        public FilteredIterator(Iterator<Element> source, ElementFilter filter) {
            this.source = source;
            this.filter = filter;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = source.hasNext();
            if (!hasNext) {
                return false;
            } else {
                while (source.hasNext()) {
                    next = source.next();
                    if (filter.accepts(next)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Element next() {
            return next;
        }

        @Override
        public void remove() {
            //no-op
        }
    }

    private static class MaskedArea {

        int from, to;

        public MaskedArea(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }
    
    private static final class EmptyUndeclaredContentResolver implements UndeclaredContentResolver {

        @Override
        public Map<String, List<String>> getUndeclaredNamespaces(HtmlSource source) {
            return Collections.emptyMap();
        }

        @Override
        public boolean isCustomTag(Named element, HtmlSource source) {
            return false;
        }

        @Override
        public boolean isCustomAttribute(Attribute attribute, HtmlSource source) {
            return false;
        }
    
    }
}
