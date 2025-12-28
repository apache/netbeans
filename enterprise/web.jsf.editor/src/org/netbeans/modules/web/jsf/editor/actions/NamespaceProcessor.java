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
package org.netbeans.modules.web.jsf.editor.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import static java.util.function.Predicate.not;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.AttributeFilter;
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
import org.netbeans.modules.web.jsf.editor.actions.ImportData.VariantItem;
import org.netbeans.modules.web.jsf.editor.facelets.JsfNamespaceComparator;
import org.netbeans.modules.web.jsf.editor.hints.LibraryDeclarationChecker;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;

/**
 * Computes defined namespaces, undefined prefixes etc.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
class NamespaceProcessor {

    private final HtmlParserResult parserResult;
    private final Snapshot snapshot;
    private final JsfSupport jsfSupport;
    private final ResultCollector resultCollector;
    private final Map<String, Library> supportedLibraries = new HashMap<>();
    private final List<Library> declaredLibraries;

    NamespaceProcessor(HtmlParserResult parserResult) {
        this.parserResult = parserResult;
        this.snapshot = parserResult.getSnapshot();
        this.jsfSupport = JsfSupportImpl.findFor(snapshot.getSource());
        // supported libraries by the project
        if (jsfSupport != null) {
            supportedLibraries.putAll(jsfSupport.getLibraries());
        }
        this.declaredLibraries = getDeclaredLibraries();
        this.resultCollector = new ResultCollector(parserResult);
    }

    ImportData computeImportData() {
        final ImportData importData = new ImportData();

        // unused declarations
        for (Attribute namespaceAttribute : resultCollector.getUnusedNamespaces()) {
            importData.addToRemove(namespaceAttribute);
        }

        for (String prefix : resultCollector.getUnresolvedPrefixes()) {
            importData.shouldShowNamespacesPanel = true;
            List<VariantItem> sortedVariants = getSortedVariants(prefix);
            importData.add(new ImportData.DataItem(
                    prefix,
                    sortedVariants,
                    sortedVariants.get(0)));
        }

        return importData;
    }

    private List<Library> getDeclaredLibraries() {
        List<Library> result = new ArrayList<>();
        for (String namespace : parserResult.getNamespaces().keySet()) {
            Library lib = NamespaceUtils.getForNs(supportedLibraries, namespace);
            if (lib != null) {
                result.add(lib);
            }
        }
        return result;
    }

    private List<VariantItem> getSortedVariants(String prefix) {
        List<VariantItem> items = new ArrayList<>();

        // Add variant matching the used prefix
        Set<String> namespacesForPrefix = Collections.emptySet();
        Optional<Library> libraryForPrefix = supportedLibraries.values().stream()
                .filter(entry -> entry.getDefaultPrefix().equals(prefix))
                .findFirst();
        if (libraryForPrefix.isPresent()) {
            Library library = libraryForPrefix.get();
            namespacesForPrefix = library.getValidNamespaces();
            
            namespacesForPrefix.forEach(namespace -> items.add(new VariantItem(prefix, namespace, library)));
        }

        // Add all other variants
        List<String> sortedList = new ArrayList<>(supportedLibraries.keySet());
        sortedList.sort(JsfNamespaceComparator.getInstance());

        sortedList.stream()
                .filter(not(namespacesForPrefix::contains))
                .forEach(additionalNamespaces -> items.add(new VariantItem(prefix, additionalNamespaces, supportedLibraries.get(additionalNamespaces))));

        return items;
    }

    private class ResultCollector {

        /**
         * Holds all defined prefixes and information whether they are used in the source or not.
         */
        private final Map<String, Boolean> prefixMap = new HashMap<>();
        private final HtmlParserResult parserResult;
        private final NamespaceCollector nsCollector;
        private final ComponentCollector compCollector;
        private final UnresolvedCollector unresolvedCollector;

        private ResultCollector(HtmlParserResult parserResult) {
            this.parserResult = parserResult;
            this.nsCollector = new NamespaceCollector(parserResult);
            this.compCollector = new ComponentCollector(parserResult, prefixMap);
            this.unresolvedCollector = new UnresolvedCollector(parserResult);
            initialize();
        }

        private void initialize() {
            for (String prefix : parserResult.getNamespaces().values()) {
                if (prefix != null) {
                    prefixMap.put(prefix, Boolean.FALSE);
                }
            }

            // gather usage of namespaces
            ElementUtils.visitChildren(parserResult.root(), compCollector, ElementType.OPEN_TAG);
            for (Library library : declaredLibraries) {
                Node root = JsfUtils.getRoot(parserResult, library);
                if (root != null) {
                    ElementUtils.visitChildren(root, compCollector, ElementType.OPEN_TAG);
                }
            }

            // gather unresolved prefixes
            ElementUtils.visitChildren(parserResult.rootOfUndeclaredTagsParseTree(), unresolvedCollector, ElementType.OPEN_TAG);
        }

        public List<Attribute> getUnusedNamespaces() {
            List<Attribute> toRemove = new ArrayList<>();
            for (Map.Entry<String, Boolean> prefixEntry : prefixMap.entrySet()) {
                if (!prefixEntry.getValue()) {
                    for (Map.Entry<String, String> nsEntry : nsCollector.namespaces.entrySet()) {
                        if (prefixEntry.getKey().equals(nsEntry.getValue())) {
                            toRemove.add(nsCollector.namespace2Attribute.get(nsEntry.getKey()));
                        }
                    }
                }
            }
            return toRemove;
        }

        public List<String> getUnresolvedPrefixes() {
            List<String> prefixes = new ArrayList<>(unresolvedCollector.getUndeclaredPrefixes());
            Collections.sort(prefixes);
            return prefixes;
        }
    }

    /**
     * Collects all defined prefixes and namespaces within the Facelet.
     */
    private class NamespaceCollector {

        private final Map<String, String> namespaces;
        private final Map<String, Attribute> namespace2Attribute = new HashMap<>();

        public NamespaceCollector(HtmlParserResult parserResult) {
            this.namespaces = parserResult.getNamespaces();
            initializeNs2Attrs(parserResult);
        }

        // gather namespaces mapping to attributes for the removal later
        private void initializeNs2Attrs(HtmlParserResult parserResult) {
            for (Iterator<Element> it = parserResult.getSyntaxAnalyzerResult().getElementsIterator(); it.hasNext();) {
                Element element = it.next();
                if (element.type() == ElementType.OPEN_TAG) {
                    OpenTag tag = (OpenTag) element;
                    for (Attribute attr : tag.attributes()) {
                        String attrName = attr.name().toString();
                        if (attrName.startsWith("xmlns")) { //NOI18N
                            CharSequence value = attr.unquotedValue();
                            if (value != null) {
                                String key = value.toString();
                                //do not overwrite already existing entry
                                Attribute attribute = namespace2Attribute.get(key);
                                if (attribute == null) {
                                    namespace2Attribute.put(key, attr);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Collects all defined components and prefixes usage within the Facelet.
     */
    private static class ComponentCollector implements ElementVisitor {

        private final Map<String, Boolean> prefixMap;

        public ComponentCollector(HtmlParserResult parserResult, Map<String, Boolean> prefixMap) {
            this.prefixMap = prefixMap;
        }

        @Override
        public void visit(Element node) {
            OpenTag openTag = (OpenTag) node;
            if (openTag.namespacePrefix() != null) {
                prefixMap.put(CharSequenceUtilities.toString(openTag.namespacePrefix()), Boolean.TRUE);
            }
            Collection<Attribute> compAttrs = openTag.attributes(new AttributeFilter() {
                @Override
                public boolean accepts(Attribute attribute) {
                    if (attribute.unquotedValue() == null) {
                        return false;
                    }
                    CharSequence nsPrefix = attribute.namespacePrefix();
                    if (nsPrefix == null) {
                        return false;
                    }
                    return true;
                }
            });

            // usage of jsf: and pass: namespaces
            for (Attribute attribute : compAttrs) {
                for (Map.Entry<String, Boolean> entry : prefixMap.entrySet()) {
                    if (!entry.getValue() && LexerUtils.equals(entry.getKey(), attribute.namespacePrefix(), true, true)) {
                        entry.setValue(Boolean.TRUE);
                    }
                }
            }
        }
    }

    /**
     * Collects all unresolved prefixes within the Facelet.
     */
    private class UnresolvedCollector implements ElementVisitor {

        private final HtmlParserResult parserResult;
        private final Set<Named> undeclaredNodes = new HashSet<>();

        public UnresolvedCollector(HtmlParserResult result) {
            this.parserResult = result;
        }

        @Override
        public void visit(Element node) {
            OpenTag openTag = (OpenTag) node;
            undeclaredNodes.addAll(LibraryDeclarationChecker.parseForUndeclaredElements(parserResult, openTag));
        }

         public Set<String> getUndeclaredPrefixes() {
             Set<String> result = new HashSet<>();
             for (Named named : undeclaredNodes) {
                 result.add(CharSequenceUtilities.toString(named.namespacePrefix()));
             }
             return result;
         }
    }
}
