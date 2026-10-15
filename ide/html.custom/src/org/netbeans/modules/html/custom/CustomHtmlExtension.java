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
package org.netbeans.modules.html.custom;

import org.netbeans.modules.html.custom.hints.CheckerElementVisitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.html.custom.conf.Configuration;
import org.netbeans.modules.html.custom.conf.Tag;
import org.netbeans.modules.html.custom.hints.CustomElementHint;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.Named;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 *
 * @author marek
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = "text/html", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/x-php5", service = HtmlExtension.class)
})
public class CustomHtmlExtension extends HtmlExtension {

    private Pair<HtmlSource, Configuration> cache;

    @Override
    public boolean isCustomTag(Named element, HtmlSource source) {
        return getConfiguration(source).getTagsNames().contains(element.name().toString());
    }

    @Override
    public boolean isCustomAttribute(Attribute attribute, HtmlSource source) {
        return getConfiguration(source).getAttributesNames().contains(attribute.name().toString());
    }

    private Configuration getConfiguration(HtmlSource source) {
        if (cache == null) {
            //no cache - create
            FileObject sourceFileObject = source.getSourceFileObject();
            Project project = sourceFileObject == null ? null : FileOwnerQuery.getOwner(sourceFileObject);
            Configuration conf = project == null ? Configuration.EMPTY : Configuration.get(project);
            cache = Pair.of(source, conf);
            return cache.second();
        } else {
            //check if the current source is the cached one
            if (source == cache.first()) {
                //yes, just return cached conf
                return cache.second();
            } else {
                //no, reset cache and try again
                cache = null;
                return getConfiguration(source);
            }
        }
    }

    @Override
    public void computeSuggestions(HintsProvider.HintsManager manager, RuleContext context, List<Hint> hints, int caretOffset) {
        HtmlParserResult result = (HtmlParserResult) context.parserResult;
        Node root = result.root(SyntaxAnalyzerResult.FILTERED_CODE_NAMESPACE);
        Snapshot snapshot = result.getSnapshot();
        int embeddedCaretOffset = snapshot.getEmbeddedOffset(caretOffset);
        Element found = ElementUtils.findByPhysicalRange(root, embeddedCaretOffset, false);
        if (found != null) {
            switch (found.type()) {
                case OPEN_TAG:
                case CLOSE_TAG:
                    Named named = (Named) found;
                    String elementName = named.name().toString();
                    Configuration conf = Configuration.get(snapshot.getSource().getFileObject());
                    if (conf.getTagsNames().contains(elementName)) {
                        //custom element
                        hints.add(new CustomElementHint(elementName, context, new OffsetRange(snapshot.getOriginalOffset(found.from()), snapshot.getOriginalOffset(found.to()))));

                    }

                //TODO add check + fix for missing required attributes
            }
        }

    }

    @Override
    public void computeErrors(HintsProvider.HintsManager manager, final RuleContext context, final List<Hint> hints, List<Error> unhandled) {
        HtmlParserResult result = (HtmlParserResult) context.parserResult;
        Node root = result.root(SyntaxAnalyzerResult.FILTERED_CODE_NAMESPACE);
        final Snapshot snapshot = result.getSnapshot();
        final Configuration conf = Configuration.get(snapshot.getSource().getFileObject());
        ElementUtils.visitChildren(root, CheckerElementVisitor.getChecker(context, conf, snapshot, hints));
    }

    @Override
    public List<CompletionItem> completeOpenTags(CompletionContext context) {
        List<CompletionItem> items = new ArrayList<>();
        FileObject file = context.getResult().getSnapshot().getSource().getFileObject();
        Configuration conf = Configuration.EMPTY;
        if (file != null) {
           conf = Configuration.get(file);
        }
        for (Tag t : conf.getTags()) {
            String tagName = t.getName();
            if (tagName.startsWith(context.getPrefix())) {
                items.add(new CustomTagCompletionItem(t, context.getCCItemStartOffset()));
            }
        }
        return items;
    }

    @Override
    public List<CompletionItem> completeAttributes(CompletionContext context) {
        Element node = context.getCurrentNode();
        FileObject fileObject = context.getResult().getSnapshot().getSource().getFileObject();
        if (node.type() != ElementType.OPEN_TAG || fileObject == null) {
            return Collections.emptyList();
        }
        OpenTag ot = (OpenTag) node;
        List<CompletionItem> items = new ArrayList<>();
        Configuration conf = Configuration.get(fileObject);
        String tagName = ((OpenTag) node).name().toString();
        Tag t = conf.getTag(tagName);
        if (t != null) {
            //complete attribute specific for the element
            for (org.netbeans.modules.html.custom.conf.Attribute a : t.getAttributes()) {
                //do not complete already existing attributes
                String aName = a.getName();
                if (ot.getAttribute(aName) == null) {
                    if (aName.startsWith(context.getPrefix())) {
                        items.add(new CustomAttributeCompletionItem(a, context.getCCItemStartOffset()));
                    }
                }
            }
        }

        //complete global attributes
        for (org.netbeans.modules.html.custom.conf.Attribute a : conf.getAttributes()) {
            Collection<String> contexts = a.getContexts();
            //complete either contextfree attribute or if the current element is the attribute context
            if (contexts.isEmpty() || contexts.contains(tagName)) {
                //do not complete already existing attributes
                String aName = a.getName();
                if (ot.getAttribute(aName) == null) {
                    if (aName.startsWith(context.getPrefix())) {
                        items.add(new CustomAttributeCompletionItem(a, context.getCCItemStartOffset()));
                    }
                }
            }
        }

        return items;
    }

}
