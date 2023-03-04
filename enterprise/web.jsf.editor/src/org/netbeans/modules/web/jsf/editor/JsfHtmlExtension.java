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
package org.netbeans.modules.web.jsf.editor;

import java.util.Map.Entry;
import java.util.*;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.*;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.HintsProvider.HintsManager;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.parsing.api.*;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.web.jsf.api.editor.JsfFacesComponentsProvider.FacesComponentLibrary;
import org.netbeans.modules.web.jsf.editor.completion.JsfAttributesCompletionHelper;
import org.netbeans.modules.web.jsf.editor.completion.JsfCompletionItem;
import org.netbeans.modules.web.jsf.editor.facelets.AbstractFaceletsLibrary;
import org.netbeans.modules.web.jsf.editor.facelets.CompositeComponentLibrary;
import org.netbeans.modules.web.jsf.editor.hints.HintsRegistry;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryComponent;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.lexer.MutableTextInput;

/**
 * XXX should be rather done by dynamic artificial embedding creation. The
 * support then can be implemented by CSL language mapped to the language
 * mimetype.
 *
 * @author marekfukala
 */
@MimeRegistration(mimeType=org.netbeans.modules.web.jsfapi.api.JsfUtils.JSF_XHTML_FILE_MIMETYPE, service=HtmlExtension.class)
public class JsfHtmlExtension extends HtmlExtension {

    private static final String EL_ENABLED_KEY = "el_enabled"; //NOI18N

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights(HtmlParserResult result, SchedulerEvent event) {
        final Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<>();

        //highlight JSF tags
        highlightJsfTags(result, highlights);

        //check if the EL is enabled in the file and enables it if not
        checkELEnabled(result);

        return highlights;

    }

    public void checkELEnabled(HtmlParserResult result) {
        Document doc = result.getSnapshot().getSource().getDocument(true);
        InputAttributes inputAttributes = (InputAttributes) doc.getProperty(InputAttributes.class);
        if (inputAttributes == null) {
            inputAttributes = new InputAttributes();
            doc.putProperty(InputAttributes.class, inputAttributes);
        }
        Language xhtmlLang = Language.find(JsfUtils.XHTML_MIMETYPE); //NOI18N
        if (inputAttributes.getValue(LanguagePath.get(xhtmlLang), EL_ENABLED_KEY) == null) {
            inputAttributes.setValue(LanguagePath.get(xhtmlLang), EL_ENABLED_KEY, new Object(), false);

            //refresh token hierarchy so the EL becomes lexed
            recolor(doc);
        }
    }

    private void recolor(final Document doc) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NbEditorDocument nbdoc = (NbEditorDocument) doc;
                nbdoc.extWriteLock();
                try {
                    MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                    if (mti != null) {
                        mti.tokenHierarchyControl().rebuild();
                    }
                } finally {
                    nbdoc.extWriteUnlock();
                }
            }
        });
    }

    private void highlightJsfTags(HtmlParserResult result, final Map<OffsetRange, Set<ColoringAttributes>> highlights) {
        final Snapshot snapshot = result.getSnapshot();
        Source source = snapshot.getSource();
        JsfSupportImpl jsfs = JsfSupportImpl.findFor(source);
        if (jsfs == null) {
            return;
        }
        Map<String, Library> libs = jsfs.getLibraries();
        Map<String, String> nss = result.getNamespaces();

        //1. resolve which declared libraries are available on classpath

        //2. resolve which tag prefixes are registered for libraries, either available or missing
        // add hint for missing library

        for (String namespace : nss.keySet()) {
            Node root = result.root(namespace);
            if (root != null) {
                final Library tldl = NamespaceUtils.getForNs(libs, namespace);
                ElementUtils.visitChildren(root, new ElementVisitor() {
                    @Override
                    public void visit(Element element) {
                        if (element.type() == ElementType.OPEN_TAG
                                || element.type() == ElementType.CLOSE_TAG) {
                            Named named = (Named) element;

                            if (named.namespacePrefix() != null) {
                                Set<ColoringAttributes> coloring = tldl == null ? ColoringAttributes.CLASS_SET : ColoringAttributes.METHOD_SET;
                                try {
                                    highlight(snapshot, named, highlights, coloring);
                                } catch (BadLocationException ex) {
                                    //just ignore
                                }
                            }
                        }
                    }
                });
            }
        }

    }

    private void highlight(Snapshot s, Named node, Map<OffsetRange, Set<ColoringAttributes>> hls, Set<ColoringAttributes> cas) throws BadLocationException {
        // "<div" id='x'> part
        int prefixLen = node.type() == ElementType.OPEN_TAG ? 1 : 2; //"<" open; "</" close
        hls.put(getDocumentOffsetRange(s, node.from(), node.from() + node.name().length() + prefixLen /* tag open symbol len */),
                cas);
        // <div id='x'">" part
        hls.put(getDocumentOffsetRange(s, node.to() - 1, node.to()),
                cas);

    }

    private OffsetRange getDocumentOffsetRange(Snapshot s, int astFrom, int astTo) throws BadLocationException {
        int from = s.getOriginalOffset(astFrom);
        int to = s.getOriginalOffset(astTo);

        if (from == -1 || to == -1) {
            throw new BadLocationException("Cannot convert snapshot offset to document offset", -1); //NOI18N
        }

        return new OffsetRange(from, to);
    }

    @Override
    public List<CompletionItem> completeOpenTags(CompletionContext context) {
        HtmlParserResult result = context.getResult();
        Source source = result.getSnapshot().getSource();
        JsfSupportImpl jsfs = JsfSupportImpl.findFor(source);
        if (jsfs == null) {
            return Collections.emptyList();
        }
        Map<String, Library> libs = jsfs.getLibraries();
        Set<Library> librariesSet = new HashSet<>(libs.values());
        //uri to prefix map
        Map<String, String> declaredNS = result.getNamespaces();

        List<CompletionItem> items = new ArrayList<>();

        int colonIndex = context.getPrefix().indexOf(':');
        if (colonIndex == -1) {
            //editing namespace or tag w/o ns
            //offer all tags
            for (Library lib : librariesSet) {
                String declaredPrefix = NamespaceUtils.getForNs(declaredNS, lib.getNamespace());
                if (declaredPrefix == null) {
                    //undeclared prefix, try to match with default library prefix
                    if (lib.getDefaultPrefix() != null && lib.getDefaultPrefix().startsWith(context.getPrefix())) {
                        items.addAll(queryLibrary(context, lib, lib.getDefaultPrefix(), true, jsfs.isJsf22Plus()));
                    }
                } else {
                    items.addAll(queryLibrary(context, lib, declaredPrefix, false, jsfs.isJsf22Plus()));
                }
            }
        } else {
            String tagNamePrefix = context.getPrefix().substring(0, colonIndex);
            //find a namespace according to the prefix
            String namespace = getUriForPrefix(tagNamePrefix, declaredNS);
            if (namespace == null) {
                //undeclared prefix, check if a taglib contains it as
                //default prefix. If so, offer it in the cc w/ tag autoimport function
                for (Library lib : librariesSet) {
                    if (lib.getDefaultPrefix() != null && lib.getDefaultPrefix().equals(tagNamePrefix)) {
                        //match
                        items.addAll(queryLibrary(context, lib, tagNamePrefix, true, jsfs.isJsf22Plus()));
                    }
                }

            } else {
                //query only associated lib
                Library lib = NamespaceUtils.getForNs(libs, namespace);
                if (lib == null) {
                    //no such lib, exit
                    return Collections.emptyList();
                } else {
                    //query the library
                    items.addAll(queryLibrary(context, lib, tagNamePrefix, false, jsfs.isJsf22Plus()));
                }
            }
        }

        //filter the items according to the prefix
        Iterator<CompletionItem> itr = items.iterator();
        while (itr.hasNext()) {
            if (!CharSequenceUtilities.startsWith(itr.next().getInsertPrefix(), context.getPrefix())) {
                itr.remove();
            }
        }

        return items;

    }

    private String getUriForPrefix(String prefix, Map<String, String> namespaces) {
        for (Entry<String, String> entry : namespaces.entrySet()) {
            if (prefix.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Collection<CompletionItem> queryLibrary(CompletionContext context, Library lib, String nsPrefix, boolean undeclared, boolean isJsf22Plus) {
        Collection<CompletionItem> items = new ArrayList<>();
        for (LibraryComponent component : lib.getComponents()) {
            if (!(component instanceof AbstractFaceletsLibrary.Function)) {
                items.add(JsfCompletionItem.createTag(context.getCCItemStartOffset(), component, nsPrefix, undeclared, isJsf22Plus));
            }
        }

        return items;
    }

    @Override
    public List<CompletionItem> completeAttributes(CompletionContext context) {
        HtmlParserResult result = context.getResult();
        Source source = result.getSnapshot().getSource();
        JsfSupportImpl jsfs = JsfSupportImpl.findFor(source);
        if (jsfs == null) {
            return Collections.emptyList();
        }
        Map<String, Library> libs = jsfs.getLibraries();
        //uri to prefix map
        Map<String, String> declaredNS = result.getNamespaces();

        List<CompletionItem> items = new ArrayList<>();

        Element queriedNode = context.getCurrentNode();
        if (queriedNode.type() != ElementType.OPEN_TAG) {
            return Collections.emptyList();
        }
        OpenTag ot = (OpenTag) queriedNode;
        CharSequence nsPrefix = ot.namespacePrefix();
        if (nsPrefix == null) {
            // this must be at attribute from JSF namespace or not JSF tag (without the prefiex)
            String jsfPrefix = declaredNS.get(DefaultLibraryInfo.JSF.getNamespace());
            if (context.getItemText().startsWith(jsfPrefix + ":")) { //NOI18N
                Library htmlLibrary = NamespaceUtils.getForNs(libs, DefaultLibraryInfo.HTML.getNamespace());
                return JsfAttributesCompletionHelper.getJsfItemsForHtmlElement(context, htmlLibrary, context.getItemText());
            } else {
                return Collections.emptyList();
            }
        }
        String tagName = ot.unqualifiedName().toString();

        String namespace = getUriForPrefix(nsPrefix.toString(), declaredNS);
        Library flib = null;
        if (namespace == null) {
            // the namespace is not imported, try find library according the default prefix
            for (Library lib : libs.values()) {
                if (lib.getDefaultPrefix() != null && lib.getDefaultPrefix().equals(nsPrefix.toString())) {
                    flib = lib;
                    break;
                }
            }
        } else {
            flib = NamespaceUtils.getForNs(libs, namespace);
        }

        if (flib == null) {
            //The facelets library not found. This happens if one declares
            //a namespace which is not matched to any existing library
            return Collections.emptyList();
        }

        JsfAttributesCompletionHelper.completeAttributes(context, items, "", flib, tagName, context.getPrefix());

        return items;
    }

    @Override
    public List<CompletionItem> completeAttributeValue(CompletionContext context) {
        List<CompletionItem> items = new ArrayList<>();

        JsfSupportImpl jsfs = JsfSupportImpl.findFor(context.getResult().getSnapshot().getSource());
        String ns = ElementUtils.getNamespace(context.getCurrentNode());
        OpenTag openTag = context.getCurrentNode().type() == ElementType.OPEN_TAG 
                ? (OpenTag) context.getCurrentNode() : null;

        //complete xmlns attribute value
        if(jsfs != null) {
            JsfAttributesCompletionHelper.completeXMLNSAttribute(context, items, jsfs);
        }
        
        if(ns == null || openTag == null) {
            return items;
        }
        
        //first try to complete using special metadata
        JsfAttributesCompletionHelper.completeTagLibraryMetadata(context, items, ns, openTag);

        if(jsfs == null) {
            return items;
        }

        //then try to complete according to the attribute type (taken from the library descriptor)
        JsfAttributesCompletionHelper.completeValueAccordingToType(context, items, ns, openTag, jsfs);

        // completion for files in cases of ui:include src attribute
        JsfAttributesCompletionHelper.completeFaceletsFromProject(context, items, ns, openTag);

        // completion for sections in cases of ui:define name attribute
        JsfAttributesCompletionHelper.completeSectionsOfTemplate(context, items, ns, openTag);

        // completion for java classes in <cc:attribute type="com.example.|
        JsfAttributesCompletionHelper.completeJavaClasses(context, items, ns, openTag);

        //facets
        JsfAttributesCompletionHelper.completeFacetsInCCImpl(context, items, ns, openTag, jsfs);
        JsfAttributesCompletionHelper.completeFacets(context, items, ns, openTag, jsfs);

        return items;
    }

    @Override
    public DeclarationLocation findDeclaration(ParserResult result, final int caretOffset) {
        assert result instanceof HtmlParserResult;
        HtmlParserResult htmlresult = (HtmlParserResult) result;
        int embeddedOffset = result.getSnapshot().getEmbeddedOffset(caretOffset);
        if (embeddedOffset == -1) {
            return DeclarationLocation.NONE;
        }

        Element leaf = htmlresult.findByPhysicalRange(embeddedOffset, true);
        if (leaf == null || leaf.type() != ElementType.OPEN_TAG) {
            return DeclarationLocation.NONE;
        }

        JsfSupportImpl jsfs = JsfSupportImpl.findFor(result.getSnapshot().getSource());
        if (jsfs == null) {
            return DeclarationLocation.NONE;
        }

        String ns = ElementUtils.getNamespace(leaf);
        if (ns == null) {
            return DeclarationLocation.NONE;
        }

        Library lib = jsfs.getLibrary(ns);
        if (lib == null) {
            return DeclarationLocation.NONE;
        }

        TokenSequence ts = JsfNavigationHelper.getTokenSequenceAtCaret(result.getSnapshot().getTokenHierarchy(), embeddedOffset);
        if (ts == null) {
            return DeclarationLocation.NONE;
        }

        Token t = ts.token();
        if (t.id() == HTMLTokenId.VALUE) {
            String value = CharSequenceUtilities.toString(ts.token().text()).replaceAll("[\"']", ""); //NOI18N
            String attribute = ""; //NOI18N
            while (ts.movePrevious()) {
                if (ts.token().id() == HTMLTokenId.TAG_OPEN) {
                    String tag = CharSequenceUtilities.toString(ts.token().text());
                    return JsfNavigationHelper.goToReferencedFile(htmlresult, embeddedOffset, tag, attribute, value);
                } else if (ts.token().id() == HTMLTokenId.ARGUMENT && attribute.isEmpty()) {
                    attribute = CharSequenceUtilities.toString(ts.token().text());
                }
            }
        } else {
            if (lib instanceof CompositeComponentLibrary) {
                return JsfNavigationHelper.goToCompositeComponentLibrary(htmlresult, embeddedOffset, lib);
            } else if (lib instanceof FacesComponentLibrary) {
                return JsfNavigationHelper.goToFacesComponentLibrary(htmlresult, embeddedOffset, (FacesComponentLibrary) lib);
            }
        }

        return DeclarationLocation.NONE;

    }

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence ts = JsfNavigationHelper.getTokenSequenceAtCaret(th, caretOffset);
        if (ts == null) {
            return OffsetRange.NONE;
        }

        Token t = ts.token();
        if (t.id() == HTMLTokenId.TAG_OPEN) {
            if (CharSequenceUtilities.indexOf(t.text(), ':') != -1) {
                return new OffsetRange(ts.offset(), ts.offset() + t.length());
            }
        } else if (t.id() == HTMLTokenId.ARGUMENT) {
            int from = ts.offset();
            int to = from + t.text().length();
            //try to find the tag and check if there is a prefix
            while (ts.movePrevious()) {
                if (ts.token().id() == HTMLTokenId.TAG_OPEN) {
                    if (CharSequenceUtilities.indexOf(ts.token().text(), ':') != -1) {
                        return new OffsetRange(from, to);
                    } else {
                        break;
                    }
                }
            }
        } else if (t.id() == HTMLTokenId.VALUE) {
            CharSequence value = ts.token().text();
            int from = ts.offset();
            int to = from + t.text().length();
            //try to find the tag and check if there is a prefix
            while (ts.movePrevious()) {
                if (ts.token().id() == HTMLTokenId.TAG_OPEN) {
                    if (CharSequenceUtilities.indexOf(ts.token().text(), "include") != -1) {
                        if (CharSequenceUtilities.indexOf(value, "'") != -1 || CharSequenceUtilities.indexOf(value, "\"") != -1) {
                            from++; to--;
                        }
                        return new OffsetRange(from, to);
                    }
                    break;
                }
            }
        }

        return OffsetRange.NONE;
    }

    @Override
    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<Error> unhandled) {
        //just delegate to the hints registry and add all gathered results
        hints.addAll(HintsRegistry.getDefault().gatherHints(context));
    }

    @Override
    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> hints, int start, int end) {
        //inject composite component support
        Hint injectCC = InjectCompositeComponent.getHint(context, start, end);
        if (injectCC != null) {
            hints.add(injectCC);
        }
    }

}
