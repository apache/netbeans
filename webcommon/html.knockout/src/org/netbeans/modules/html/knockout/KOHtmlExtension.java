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
package org.netbeans.modules.html.knockout;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.Named;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.knockout.api.KODataBindTokenId;
import org.netbeans.modules.html.knockout.model.Binding;
import org.netbeans.modules.html.knockout.model.KOModel;
import org.netbeans.modules.javascript2.knockout.index.KnockoutCustomElement;
import org.netbeans.modules.javascript2.knockout.index.KnockoutIndex;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Knockout extension to the html editor.
 *
 * @author marekfukala
 * @author Roman Svitanic
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = "text/html", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/xhtml", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/x-jsp", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/x-tag", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/x-php5", service = HtmlExtension.class)
})
public class KOHtmlExtension extends HtmlExtension {

    @Override
    public boolean isApplicationPiece(HtmlParserResult result) {
        return KOModel.getModel(result).containsKnockout();
    }

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights(HtmlParserResult result, SchedulerEvent event) {
        final Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<>();
        KOModel model = KOModel.getModel(result);
        for (Attribute ngAttr : model.getBindings()) {
            OffsetRange dor = KOUtils.getValidDocumentOffsetRange(ngAttr.from(), ngAttr.from() + ngAttr.name().length(), result.getSnapshot());
            if (dor != null) {
                highlights.put(dor, ColoringAttributes.CONSTRUCTOR_SET);
            }
        }
        return highlights;
    }

    @Override
    public List<CompletionItem> completeAttributes(CompletionContext context) {
        KOModel model = KOModel.getModel(context.getResult());
        List<CompletionItem> items = new ArrayList<>();
        Element element = context.getCurrentNode();

        if (element != null) {
            switch (element.type()) {
                case OPEN_TAG:
                    OpenTag ot = (OpenTag) element;
                    String name = ot.unqualifiedName().toString();
                    Collection<CustomAttribute> customAttributes = getCustomAttributes(name);
                    for (CustomAttribute ca : customAttributes) {
                        items.add(new KOAttributeCompletionItem(ca, context.getCCItemStartOffset(), model.containsKnockout()));
                    }
                    // add "params" attribute only in case of custom knockout elements
                    if (isTagCustomKnockoutElement(context.getResult().getSnapshot().getSource().getFileObject(), name)) {
                        items.add(new KOAttributeCompletionItem(KO_PARAMS_CUSTOM_ATTRIBUTE, context.getCCItemStartOffset(), model.containsKnockout()));
                    }
                    break;
            }
        }

        //XXX copied - needs more elegant solution!
        if (context.getPrefix().length() > 0) {
            //filter the items according to the prefix
            Iterator<CompletionItem> itr = items.iterator();
            while (itr.hasNext()) {
                CharSequence insertPrefix = itr.next().getInsertPrefix();
                if (insertPrefix != null) {
                    if (!LexerUtils.startsWith(insertPrefix, context.getPrefix(), true, false)) {
                        itr.remove();
                    }
                }
            }
        }

        return items;
    }

    //complete keys in ko-data-bind attribute
    //<div data-bind="tex| => text:
    @Override
    public List<CompletionItem> completeAttributeValue(CompletionContext context) {
        Document document = context.getResult().getSnapshot().getSource().getDocument(true);
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(document);
        TokenSequence<HTMLTokenId> ts = LexerUtils.getTokenSequence(tokenHierarchy, context.getOriginalOffset(), HTMLTokenId.language(), false);
        if (ts != null) {
            int diff = ts.move(context.getOriginalOffset());
            if (diff == 0 && ts.movePrevious() || ts.moveNext()) {
                Token<HTMLTokenId> token = ts.token();
                if (token.id() == HTMLTokenId.VALUE) {
                    TokenSequence<KODataBindTokenId> embedded = ts.embedded(KODataBindTokenId.language());
                    if (embedded != null) {
                        if (embedded.isEmpty()) {
                            //no prefix
                            if (context.getAttributeName().equals(KOUtils.KO_PARAMS_ATTR_NAME)) {
                                // we are in params attribute of KO custom element
                                return getCustomElementParameters(
                                        context.getResult().getSnapshot().getSource().getFileObject(),
                                        context.getCurrentNode().id().toString(),
                                        "", context.getOriginalOffset());
                            } else {
                                // we are in data-bind attribute
                                return getBindingItems("", context.getOriginalOffset());
                            }
                        }
                        int ediff = embedded.move(context.getOriginalOffset());
                        if (ediff == 0 && embedded.movePrevious() || embedded.moveNext()) {
                            //we are on a token of ko-data-bind token sequence
                            Token<KODataBindTokenId> etoken = embedded.token();
                            if (context.getAttributeName().equals(KOUtils.KO_PARAMS_ATTR_NAME)) {
                                // we are in params attribute of KO custom element
                                FileObject fo = context.getResult().getSnapshot().getSource().getFileObject();
                                String elementName = context.getCurrentNode().id().toString();
                                switch (etoken.id()) {
                                    case KEY:
                                        //ke|
                                        CharSequence prefix = ediff == 0 ? etoken.text() : etoken.text().subSequence(0, ediff);
                                        if (ediff == 0 && (etoken.offset(tokenHierarchy) + etoken.length()) != context.getOriginalOffset()) {
                                            // offer all bindings in case of caret placed before binding: <body data-bind="^attr: ">
                                            prefix = ""; //NOI18N
                                        }
                                        return getCustomElementParameters(fo, elementName, prefix, embedded.offset());
                                    case COMMA:
                                        //key:value,|
                                        return getCustomElementParameters(fo, elementName, "", context.getOriginalOffset());
                                    case WS:
                                        //key: value, |
                                        if (embedded.movePrevious()) {
                                            switch (embedded.token().id()) {
                                                case COMMA:
                                                    return getCustomElementParameters(fo, elementName, "", context.getOriginalOffset());
                                            }
                                        } else {
                                            //just WS is before the caret, no token before |
                                            return getCustomElementParameters(fo, elementName, "", context.getOriginalOffset());

                                        }
                                        break;
                                }
                            } else {
                                // we are in data-bind attribute
                                switch (etoken.id()) {
                                    case KEY:
                                        //ke|
                                        CharSequence prefix = ediff == 0 ? etoken.text() : etoken.text().subSequence(0, ediff);
                                        if (ediff == 0 && (etoken.offset(tokenHierarchy) + etoken.length()) != context.getOriginalOffset()) {
                                            // offer all bindings in case of caret placed before binding: <body data-bind="^attr: ">
                                            prefix = ""; //NOI18N
                                        }
                                        return getBindingItems(prefix, embedded.offset());
                                    case COMMA:
                                        //key:value,|
                                        return getBindingItems("", context.getOriginalOffset());
                                    case WS:
                                        //key: value, |
                                        if (embedded.movePrevious()) {
                                            switch (embedded.token().id()) {
                                                case COMMA:
                                                    return getBindingItems("", context.getOriginalOffset());
                                            }
                                        } else {
                                        //just WS is before the caret, no token before
                                        //   |
                                            return getBindingItems("", context.getOriginalOffset());

                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<CompletionItem> completeOpenTags(CompletionContext context) {
        List<CompletionItem> items = new ArrayList<>();
        FileObject fo = context.getResult().getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return Collections.emptyList();
        }
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return Collections.emptyList();
        }
        Collection<KnockoutCustomElement> customElements = getAllCustomElements(project, context.getPrefix());
        List<String> alreadyAddedComponents = new ArrayList<>();
        for (KnockoutCustomElement customElement : customElements) {
            if (!alreadyAddedComponents.contains(customElement.getName())) {
                items.add(new KOTagCompletionItem(customElement, context.getCCItemStartOffset()));
                alreadyAddedComponents.add(customElement.getName());
            } else {
                for (CompletionItem ci : items) {
                    KOTagCompletionItem completionItem = (KOTagCompletionItem) ci;
                    if (completionItem.getCustomElementName().equals(customElement.getName())) {
                        // custom element with same name is already in the result, just add alternative registration location
                        completionItem.addAlternativeLocation(customElement.getDeclarationFile());
                        break;
                    }
                }
            }
        }
        return items;
    }

    private List<CompletionItem> getBindingItems(CharSequence prefix, int offset) {
        List<CompletionItem> items = new ArrayList<>();
        for (Binding b : Binding.values()) {
            String bindingName = b.getName();
            if (LexerUtils.startsWith(bindingName, prefix, true, false)) {
                items.add(new KOBindingCompletionItem(b, offset));
            }
        }
        return items;
    }

    @Override
    public boolean isCustomAttribute(Attribute attribute, HtmlSource source) {
        return KOModel.isKODataBindingAttribute(attribute) || KOModel.isKOParamsAttribute(attribute);
    }

    @Override
    public Collection<CustomAttribute> getCustomAttributes(String elementName) {
        return Collections.singleton(KO_DATA_BIND_CUSTOM_ATTRIBUTE);
    }

    @Override
    public boolean isCustomTag(Named element, HtmlSource source) {
        FileObject fo = source.getSourceFileObject();
        if (fo == null) {
            return false;
        }
        return isTagCustomKnockoutElement(fo, element.name().toString());
    }

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        final OffsetRange[] value = new OffsetRange[1];
        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<? extends HTMLTokenId> ts = LexerUtils.getTokenSequence(doc, caretOffset, HTMLTokenId.language(), false);
                if (ts == null) {
                    return;
                }

                ts.move(caretOffset);
                if (ts.moveNext()) {
                    HTMLTokenId id = ts.token().id();
                    if (id == HTMLTokenId.TAG_OPEN || id == HTMLTokenId.TAG_CLOSE) {
                        boolean customElement = isTagCustomKnockoutElement(Source.create(doc).getFileObject(), ts.token().text().toString());
                        if (customElement) {
                            value[0] = new OffsetRange(ts.offset(), ts.offset() + ts.token().length());
                        }
                    }
                }
            }
        });
        if (value[0] != null) {
            return value[0];
        }
        return OffsetRange.NONE;
    }

    @Override
    public DeclarationFinder.DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        FileObject fo = info.getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return null;
        }

        TokenSequence<? extends HTMLTokenId> ts = LexerUtils.getTokenSequence(info.getSnapshot().getTokenHierarchy(), caretOffset, HTMLTokenId.language(), false);
        if (ts == null) {
            return null;
        }

        ts.move(caretOffset);
        if (ts.moveNext()) {
            HTMLTokenId id = ts.token().id();
            if (id == HTMLTokenId.TAG_OPEN || id == HTMLTokenId.TAG_CLOSE) {
                Project project = FileOwnerQuery.getOwner(info.getSnapshot().getSource().getFileObject());
                Collection<KnockoutCustomElement> customElements = getAllCustomElements(project, ts.token().text().toString());
                DeclarationFinder.DeclarationLocation dl = null;
                for (KnockoutCustomElement kce : customElements) {
                    URI uri = null;
                    try {
                        uri = kce.getDeclarationFile().toURI();
                    } catch (URISyntaxException ex) {
                        // nothing
                    }
                    if (uri != null) {
                        File file = Utilities.toFile(uri);
                        FileObject dfo = FileUtil.toFileObject(file);
                        DeclarationFinder.DeclarationLocation dloc = new DeclarationFinder.DeclarationLocation(dfo, kce.getOffset());
                        //The main DeclarationLocation must be also added to the alternatives
                        //if there are more than one
                        if (dl == null) {
                            //DeclarationLocation alternatives handling workaround - one of the locations simply must be "main"
                            dl = dloc;
                        }
                        DeclarationFinder.AlternativeLocation aloc = new AlternativeLocationImpl(kce.getName(), dloc, new ElementHandle.UrlHandle(dfo.getPath()));
                        dl.addAlternative(aloc);
                    }
                }
                //and finally if there was just one entry, remove the "alternative"
                if (dl != null && dl.getAlternativeLocations().size() == 1) {
                    dl.getAlternativeLocations().clear();
                }

                if (dl != null) {
                    return dl;
                }
            }
        }
        return null;
    }

    private boolean isTagCustomKnockoutElement(FileObject fo, String tagName) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return false;
        }
        KnockoutIndex knockoutIndex = null;
        try {
            knockoutIndex = KnockoutIndex.get(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (knockoutIndex != null) {
            Collection<KnockoutCustomElement> customElements = knockoutIndex.getCustomElements(tagName, false);
            if (customElements == null || customElements.isEmpty()) {
                return false;
            }
            boolean customTagFound = false;
            for (KnockoutCustomElement kce : customElements) {
                if (kce.getName().equals(tagName)) {
                    customTagFound = true;
                    break;
                }
            }
            return customTagFound;
        }
        return false;
    }

    private Collection<KnockoutCustomElement> getAllCustomElements(Project project, String prefix) {
        KnockoutIndex knockoutIndex = null;
        try {
            knockoutIndex = KnockoutIndex.get(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (knockoutIndex != null) {
            return knockoutIndex.getCustomElements(prefix, false);
        }
        return Collections.emptyList();
    }

    private List<CompletionItem> getCustomElementParameters(FileObject fo, String elementName, CharSequence prefix, int offset) {
        if (fo == null) {
            return Collections.emptyList();
        }
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return Collections.emptyList();
        }
        KnockoutIndex knockoutIndex = null;
        try {
            knockoutIndex = KnockoutIndex.get(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (knockoutIndex != null) {
            List<CompletionItem> items = new ArrayList<>();
            for (String paramName : knockoutIndex.getCustomElementParameters(elementName)) {
                if (LexerUtils.startsWith(paramName, prefix, true, false)) {
                    items.add(new KOParamsCompletionItem(paramName, offset));
                }
            }
            return items;
        }
        return Collections.emptyList();
    }

    private static final String DOC_URL = "http://knockoutjs.com/documentation/binding-syntax.html"; //NOI18N
    static final KOHelpItem KO_DATA_BIND_HELP_ITEM = new KOHelpItem() {
        @Override
        public String getName() {
            return KOUtils.KO_DATA_BIND_ATTR_NAME;
        }

        @Override
        public String getExternalDocumentationURL() {
            return DOC_URL;
        }
    };
    private static final CustomAttribute KO_DATA_BIND_CUSTOM_ATTRIBUTE = new CustomAttribute() {
        @Override
        public String getName() {
            return KOUtils.KO_DATA_BIND_ATTR_NAME;
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public boolean isValueRequired() {
            return true;
        }

        @Override
        public HelpItem getHelp() {
            return new HelpItemImpl(KO_DATA_BIND_HELP_ITEM);
        }
    };
    private static final CustomAttribute KO_PARAMS_CUSTOM_ATTRIBUTE = new CustomAttribute() {
        @Override
        public String getName() {
            return KOUtils.KO_PARAMS_ATTR_NAME;
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public boolean isValueRequired() {
            return true;
        }

        @Override
        public HelpItem getHelp() {
            return null;
        }
    };

    private static class AlternativeLocationImpl implements DeclarationFinder.AlternativeLocation {

        private final DeclarationFinder.DeclarationLocation location;
        private final ElementHandle element;

        public AlternativeLocationImpl(String name, DeclarationFinder.DeclarationLocation location, ElementHandle element) {
            this.location = location;
            this.element = element;
        }

        @Override
        public ElementHandle getElement() {
            return element;
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            StringBuilder sb = new StringBuilder();
            sb.append("<font color='black'>"); //NOI18N
            sb.append(element.getName());
            sb.append("</font>"); //NOI18N
            return sb.toString();
        }

        @Override
        public DeclarationFinder.DeclarationLocation getLocation() {
            return location;
        }

        @Override
        public int compareTo(DeclarationFinder.AlternativeLocation o) {
            //compare according to the file paths
            return getComparableString(this).compareTo(getComparableString(o));
        }

        private static String getComparableString(DeclarationFinder.AlternativeLocation loc) {
            StringBuilder sb = new StringBuilder();
            sb.append(loc.getLocation().getOffset()); //offset
            FileObject fo = loc.getLocation().getFileObject();
            if (fo != null) {
                sb.append(fo.getPath()); //filename
            }
            return sb.toString();
        }
    }
}
