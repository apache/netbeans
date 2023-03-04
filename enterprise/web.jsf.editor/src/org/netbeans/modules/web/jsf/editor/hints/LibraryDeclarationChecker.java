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
package org.netbeans.modules.web.jsf.editor.hints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.html.editor.api.gsf.HtmlErrorFilterContext;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.AttributeFilter;
import org.netbeans.modules.html.editor.lib.api.elements.CloseTag;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor;
import org.netbeans.modules.html.editor.lib.api.elements.Named;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.jsf.editor.JsfSupportImpl;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.netbeans.modules.web.jsf.editor.PositionRange;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class LibraryDeclarationChecker extends HintsProvider {
    private static final String SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance"; //NOI18N

    private static final Logger LOG = Logger.getLogger(LibraryDeclarationChecker.class.getName());

    @Override
    public List<Hint> compute(RuleContext context) {
        List<Hint> hints = new ArrayList<>();

        checkLibraryDeclarations(hints, context);

        return hints;
    }

    //check the namespaces declaration:
    //1. if the declared library is available
    //2. if the declared library is used + remove unused declaration hint
    //3. if there are usages of undeclared library 
    //    + hint to add the declaration (if library available)
    //        - by default prefix
    //        - or search all the libraries for such component and offer the match/es
    //
    private void checkLibraryDeclarations(final List<Hint> hints, final RuleContext context) {
        final HtmlParserResult result = (HtmlParserResult) context.parserResult;
        final Snapshot snapshot = result.getSnapshot();
        int errorType = 0;
        if (context instanceof HtmlErrorFilterContext) {
            errorType = ((HtmlErrorFilterContext)context).isOnlyBadging() ? 2 : 1;
        }

        //find all usages of composite components tags for this page
        Collection<String> declaredNamespaces = result.getNamespaces().keySet();
        final Collection<Library> declaredLibraries = new ArrayList<>();
        final JsfSupportImpl jsfSupport = JsfSupportImpl.findFor(context.parserResult.getSnapshot().getSource());
        Map<String, Library> libs = Collections.emptyMap();
        if (jsfSupport != null) {
            libs = jsfSupport.getLibraries();
        }

        //Find the namespaces declarations itself
        //a.take the html AST & the AST for undeclared components
        //b.search for nodes with xmlns attribute
        //ugly, grr, the whole namespace support needs to be fixed
        final Map<String, Attribute> namespace2Attribute = new HashMap<>();
        Node root = result.root();
        if (root.children().isEmpty()) {
            Node faceletsRoot = result.root(DefaultLibraryInfo.FACELETS.getLegacyNamespace());
            root = !faceletsRoot.children().isEmpty() ? faceletsRoot : result.root(DefaultLibraryInfo.FACELETS.getNamespace());
        }
        final CharSequence docText = getSourceText(snapshot.getSource());
        final String jsfNsPrefix = NamespaceUtils.getForNs(result.getNamespaces(), DefaultLibraryInfo.JSF.getNamespace());
        final String passthroughNsPrefix = NamespaceUtils.getForNs(result.getNamespaces(), DefaultLibraryInfo.PASSTHROUGH.getNamespace());
        final boolean[] jsfUsage = new boolean[1];
        final List<Named> wrongJsfNsUsages = new ArrayList<>();

        // collects all prefixes with prefix xmlns (namespaces) and jsf (JSF2.2 prefix definable for any HTML element)
        ElementVisitor prefixCollector = new ElementVisitor() {
            @Override
            public void visit(Element node) {
                OpenTag openTag = (OpenTag)node;
                //put all NS attributes to the namespace2Attribute map for #1 and check usage of jsf: prefixes.
                Collection<Attribute> nsAttrs = openTag.attributes(new AttributeFilter() {

                    @Override
                    public boolean accepts(Attribute attribute) {
                        if(attribute.unquotedValue() == null) {
                            return false;
                        }
                        CharSequence nsPrefix = attribute.namespacePrefix();
                        if(nsPrefix == null) {
                            return false;
                        }
                        
                        return LexerUtils.equals("xmlns", nsPrefix, true, true) //NOI18N
                                || jsfNsPrefix != null && LexerUtils.equals(jsfNsPrefix, nsPrefix, true, true);
                    }
                });
                
                for (Attribute attr : nsAttrs) {
                    if (LexerUtils.equals("xmlns", attr.namespacePrefix(), true, true)) { //NOI18N
                        // collect namespaces
                        namespace2Attribute.put(attr.unquotedValue().toString(), attr);
                    } else {
                        // mark jsf facelet library as used
                        jsfUsage[0] = true;
                    }
                }
            }
        };

        ElementUtils.visitChildren(root, prefixCollector, ElementType.OPEN_TAG);
        Node undeclaredComponentsTreeRoot = result.rootOfUndeclaredTagsParseTree();
        if (undeclaredComponentsTreeRoot != null && errorType != 1) {
            ElementUtils.visitChildren(undeclaredComponentsTreeRoot, prefixCollector, ElementType.OPEN_TAG);

            //check for undeclared tags
            ElementUtils.visitChildren(undeclaredComponentsTreeRoot, new ElementVisitor() {

                @Override
                public void visit(Element node) {
                    OpenTag openTag = (OpenTag) node;
                    Set<Named> undeclaredNodes = parseForUndeclaredElements(result, openTag);

                    //3. check for undeclared components
                    List<HintFix> fixes = new ArrayList<>();
                    Set<Library> libs = getLibsByPrefixes(context, getUndeclaredNamespaces(undeclaredNodes));
                    for (Library lib : libs) {
                        FixLibDeclaration fix = new FixLibDeclaration(context.doc, lib.getDefaultPrefix(), lib, jsfSupport.isJsf22Plus());
                        fixes.add(fix);
                    }

                    //this itself means that the node is undeclared since
                    //otherwise it wouldn't appear in the pure html parse tree
                    for (Named undeclaredEntry : undeclaredNodes) {
                        hints.add(new Hint(ERROR_RULE_BADGING,
                                NbBundle.getMessage(HintsProvider.class, "MSG_UNDECLARED_COMPONENT", undeclaredEntry.image()), //NOI18N
                                context.parserResult.getSnapshot().getSource().getFileObject(),
                                JsfUtils.createOffsetRange(snapshot, docText, undeclaredEntry.from(), undeclaredEntry.from() + undeclaredEntry.name().length() + 1 /* "<".length */),
                                new ArrayList<>(fixes), DEFAULT_ERROR_HINT_PRIORITY));

                        //put the hint to the close tag as well
                        CloseTag matchingCloseTag = openTag.matchingCloseTag();
                        if(undeclaredEntry.equals(openTag) && matchingCloseTag != null) {
                            hints.add(new Hint(ERROR_RULE_BADGING,
                                    NbBundle.getMessage(HintsProvider.class, "MSG_UNDECLARED_COMPONENT", openTag.name().toString()), //NOI18N
                                    context.parserResult.getSnapshot().getSource().getFileObject(),
                                    JsfUtils.createOffsetRange(snapshot, docText, matchingCloseTag.from(), matchingCloseTag.to()),
                                    new ArrayList<>(fixes), DEFAULT_ERROR_HINT_PRIORITY));
                        }
                        // apply the fixed only once, to prevent fixes duplication
                        fixes.clear();
                    }
                }
            }, ElementType.OPEN_TAG);
        }

        for (String namespace : declaredNamespaces) {
            if(SCHEMA_INSTANCE.equals(namespace)) {
                continue;
            }

            Library lib = NamespaceUtils.getForNs(libs, namespace);
            if (lib != null) {
                // http://java.sun.com/jsf/passthrough usage needs to be resolved on base of all declared libraries
                if (!(DefaultLibraryInfo.PASSTHROUGH.getNamespace().equals(lib.getNamespace())
                        || DefaultLibraryInfo.PASSTHROUGH.getLegacyNamespace().equals(lib.getNamespace()))) {
                    declaredLibraries.add(lib);
                }
            } else {
                //1. report error - missing library for the declaration
                Attribute attr = namespace2Attribute.get(namespace);
                if (attr != null && errorType != 1) {
                    //found the declaration, mark as error
                    Hint hint = new Hint(ERROR_RULE_BADGING,
                            NbBundle.getMessage(HintsProvider.class, "MSG_MISSING_LIBRARY", namespace), //NOI18N
                            context.parserResult.getSnapshot().getSource().getFileObject(),
                            JsfUtils.createOffsetRange(snapshot, docText, attr.nameOffset(), attr.valueOffset() + attr.value().length()),
                            Collections.<HintFix>emptyList(), DEFAULT_ERROR_HINT_PRIORITY);
                    hints.add(hint);
                }
            }
        }

        if (errorType < 2) {
            //2. find for unused declarations
            final boolean declaredPassthroughOrJsf = NamespaceUtils.containsNsOf(declaredNamespaces, DefaultLibraryInfo.JSF)
                    || NamespaceUtils.containsNsOf(declaredNamespaces, DefaultLibraryInfo.PASSTHROUGH);
            final boolean[] passthroughUsage = new boolean[1];
            final Collection<OffsetRange> ranges = new ArrayList<>();
            for (Library lib : declaredLibraries) {
                Node rootNode = result.root(lib.getNamespace());
                if (lib.getLegacyNamespace() != null && (rootNode == null || rootNode.children().isEmpty())) {
                    rootNode = result.root(lib.getLegacyNamespace());
                }
                if (rootNode == null) {
                    continue; //no parse result for this namespace, the namespace is not declared
                }
                final int[] usages = new int[1];
                ElementUtils.visitChildren(rootNode, new ElementVisitor() {
                    @Override
                    public void visit(Element node) {
                        usages[0]++;
                        if (declaredPassthroughOrJsf) {
                            OpenTag ot = (OpenTag) node;
                            for (Attribute attribute : ot.attributes(new AttributeFilter() {
                                @Override
                                public boolean accepts(Attribute attribute) {
                                    return attribute.namespacePrefix() != null;
                                }
                            })) {
                                if (passthroughNsPrefix != null && LexerUtils.equals(passthroughNsPrefix, attribute.namespacePrefix(), true, true)) {
                                    // http://java.sun.com/jsf/passthrough or http://xmlns.jcp.org/jsf/passthrough used
                                    passthroughUsage[0] = true;
                                } else if (jsfNsPrefix != null && ot.namespacePrefix() != null
                                        && LexerUtils.equals(jsfNsPrefix, attribute.namespacePrefix(), true, true)) {
                                    // http://java.sun.com/jsf used at JSF-aware tag
                                    wrongJsfNsUsages.add(attribute);
                                }
                            }
                        }
                    }
                }, ElementType.OPEN_TAG);

                usages[0] += isFunctionLibraryPrefixUsedInEL(context, lib, docText) ? 1 : 0;

                // http://java.sun.com/jsf namespace handling
                usages[0] += (DefaultLibraryInfo.JSF.getNamespace().equals(lib.getNamespace()) || DefaultLibraryInfo.JSF.getLegacyNamespace().equals(lib.getNamespace())) && jsfUsage[0] ? 1 : 0;

                if (usages[0] == 0) {
                    //unused declaration
                    addUnusedLibrary(ranges, namespace2Attribute, lib.getNamespace(), snapshot, docText);
                }
            }

            //2b. find for unused declaration of http://java.sun.com/jsf/passthrough
            if (NamespaceUtils.containsNsOf(declaredNamespaces, DefaultLibraryInfo.PASSTHROUGH) && !passthroughUsage[0]) {
                addUnusedLibrary(ranges, namespace2Attribute, DefaultLibraryInfo.PASSTHROUGH.getNamespace(), snapshot, docText);
            }

            //generate remove all unused declarations
            for (OffsetRange range : ranges) {
                List<HintFix> fixes;
                try {
                    //do not create any fixes if there's no document
                    fixes = context.doc != null
                            ? Arrays.asList(new HintFix[]{
                                new RemoveUnusedLibraryDeclarationHintFix(context.doc, createPositionRange(context, range)), //the only occurance
                                new RemoveUnusedLibrariesDeclarationHintFix(context.doc, createPositionRanges(context, ranges))
                            }) //remove all
                            : Collections.<HintFix>emptyList();
                } catch (BadLocationException ex) {
                    //ignore
                    fixes = Collections.emptyList();
                }

                Hint hint = new Hint(DEFAULT_WARNING_RULE,
                        NbBundle.getMessage(HintsProvider.class, "MSG_UNUSED_LIBRARY_DECLARATION", docText.subSequence(range.getStart(), range.getEnd())), //NOI18N
                        context.parserResult.getSnapshot().getSource().getFileObject(),
                        range,
                        fixes, DEFAULT_ERROR_HINT_PRIORITY);

                hints.add(hint);
            }
            
            //generate errors - http://java.sun.com/jsf namespace used at JSF-aware markup
            for (Named attr : wrongJsfNsUsages) {
                Hint hint = new Hint(DEFAULT_ERROR_RULE,
                        NbBundle.getMessage(HintsProvider.class, "MSG_JSF_NS_USED_IN_JSF_AWARE_TAG"), //NOI18N
                        context.parserResult.getSnapshot().getSource().getFileObject(),
                        JsfUtils.createOffsetRange(snapshot, docText, attr.from(), attr.from() + attr.name().length() + 1),
                        Collections.<HintFix>emptyList(), DEFAULT_ERROR_HINT_PRIORITY);
                hints.add(hint);
            }
        }
    }

    private void addUnusedLibrary(Collection<OffsetRange> ranges, Map<String, Attribute> namespace2Attribute, String namespace, Snapshot snapshot, CharSequence docText) {
        Attribute declAttr = NamespaceUtils.getForNs(namespace2Attribute, namespace);
        if (declAttr != null) {
            int from = declAttr.nameOffset();
            int to = declAttr.valueOffset() + declAttr.value().length();

            if (from < to && to > 0 && to < docText.length()) {
                OffsetRange documentRange = JsfUtils.createOffsetRange(snapshot, docText, from, to);
                ranges.add(documentRange);
            } else {
                // removes issues #228866 from release builds, logging tell us more here
                LOG.log(Level.WARNING, "Range definition out of bounds of the source: from={0},to={1},text={2}", new Object[]{from, to, docText});
            }
        }
    }

    private static Set<String> getUndeclaredNamespaces(Set<Named> undeclaredEntries) {
        Set<String> undeclaredNamespaces = new HashSet<>();
        for (Named named : undeclaredEntries) {
            undeclaredNamespaces.add(named.namespacePrefix().toString());
        }
        return undeclaredNamespaces;
    }

    public static Set<Named> parseForUndeclaredElements(HtmlParserResult result, OpenTag openTag) {
        Set<Named> undeclaredEntries = new HashSet<>();

        // don't check root tags - issue #231536
        for (Map.Entry<String, Node> entry : result.roots().entrySet()) {
            for (Element element : entry.getValue().children()) {
                if (elementEqualsOpenTag(element, openTag)) {
                    return undeclaredEntries;
                }
            }
        }

        // undeclared tag prefix
        if (openTag.namespacePrefix() != null
                && !result.getNamespaces().containsValue(openTag.namespacePrefix().toString())) {
            undeclaredEntries.add(openTag);
        }

        for (Attribute attribute : openTag.attributes(new AttributeFilter() {
            @Override
            public boolean accepts(Attribute attribute) {
                return attribute.namespacePrefix() != null;
            }
        })) {
            // undeclared attribute prefix
            if (!result.getNamespaces().containsValue(attribute.namespacePrefix().toString())) {
                undeclaredEntries.add(attribute);
            }
        }

        return undeclaredEntries;
    }

    private static boolean elementEqualsOpenTag(Element element, OpenTag openTag) {
        return element.type() == ElementType.OPEN_TAG
                && element.from() == openTag.from()
                && element.to() == openTag.to();
    }

    private static PositionRange createPositionRange(RuleContext context, OffsetRange offsetRange) throws BadLocationException {
        return new PositionRange(context.doc, offsetRange.getStart(), offsetRange.getEnd());
    }
    
    private static Collection<PositionRange> createPositionRanges(RuleContext context, Collection<OffsetRange> offsetRanges) throws BadLocationException {
        Collection<PositionRange> ranges = new ArrayList<>();
        for(OffsetRange offsetRange : offsetRanges) {
            ranges.add(createPositionRange(context, offsetRange));
        }
        return ranges;
    }
    
    private static Set<Library> getLibsByPrefixes(RuleContext context, Set<String> prefixes){
        Set<Library> libs = new HashSet<>();
        JsfSupportImpl sup = JsfSupportImpl.findFor(context.parserResult.getSnapshot().getSource());

        if (sup != null){
            //eliminate the library duplicities - see the sup.getLibraries() doc
            for (Library lib : new HashSet<>(sup.getLibraries().values())){
                if (prefixes.contains(lib.getDefaultPrefix())){
                    libs.add(lib);
                }
            }
        }

        return libs;
    }

    //find all embedded EL token sequences in the source code and check if the
    //prefix is used in any of them
    private static boolean isFunctionLibraryPrefixUsedInEL(RuleContext context, Library lib, CharSequence sourceText) {
        String libraryPrefix = NamespaceUtils.getForNs(((HtmlParserResult)context.parserResult).getNamespaces(), lib.getNamespace());
        TokenHierarchy<CharSequence> th = TokenHierarchy.create(sourceText, Language.find("text/xhtml"));
        TokenSequence<?> ts = th.tokenSequence();
        ts.moveStart();
        while(ts.moveNext()) {
            TokenSequence<ELTokenId> elts = ts.embeddedJoined(ELTokenId.language());
            if(elts != null) {
                //check the EL expression for the function library prefix usages
                elts.moveStart();
                while(elts.moveNext()) {
                    if(elts.token().id() == ELTokenId.TAG_LIB_PREFIX && CharSequenceUtilities.equals(libraryPrefix, elts.token().text())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static class RemoveUnusedLibraryDeclarationHintFix extends RemoveUnusedLibrariesDeclarationHintFix {

        public RemoveUnusedLibraryDeclarationHintFix(BaseDocument document, PositionRange range) {
            super(document, Collections.<PositionRange>singletonList(range));
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(HintsProvider.class, "MSG_HINTFIX_REMOVE_UNUSED_LIBRARY_DECLARATION");
        }

    }

    private static class RemoveUnusedLibrariesDeclarationHintFix implements HintFix {

        protected Collection<PositionRange> ranges = new ArrayList<>();
        protected BaseDocument document;

        public RemoveUnusedLibrariesDeclarationHintFix(BaseDocument document, Collection<PositionRange> ranges) {
            this.document = document;
            this.ranges = ranges;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(HintsProvider.class, "MSG_HINTFIX_REMOVE_ALL_UNUSED_LIBRARIES_DECLARATION");
        }

        @Override
        public void implement() throws Exception {
            document.runAtomic(new Runnable() {

                @Override
                public void run() {
                    try {
                        for (PositionRange range : ranges) {
                            int from = range.getFrom();
                            int to = range.getTo();
                            //check if the line before the area is white
                            int lineBeginning = Utilities.getRowStart(document, from);
                            int firstNonWhite = Utilities.getFirstNonWhiteBwd(document, from);
                            if (lineBeginning > firstNonWhite) {
                                //delete the white content before the area inclusing the newline
                                from = lineBeginning - 1; // (-1 => includes the line end)
                            }
                            document.remove(from, to - from);
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
            });
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }

}
