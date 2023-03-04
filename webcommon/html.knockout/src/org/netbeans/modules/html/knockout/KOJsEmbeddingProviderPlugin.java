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

import org.netbeans.modules.html.knockout.api.KODataBindTokenId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HTMLTokenId;
import static org.netbeans.api.html.lexer.HTMLTokenId.TAG_CLOSE;
import static org.netbeans.api.html.lexer.HTMLTokenId.TAG_OPEN;
import static org.netbeans.api.html.lexer.HTMLTokenId.VALUE;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.spi.embedding.JsEmbeddingProviderPlugin;
import org.netbeans.modules.html.knockout.KODataBindContext.ParentContext;
import org.netbeans.modules.html.knockout.model.KOModel;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.spi.knockout.Bindings;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 * Knockout javascript virtual source extension
 *
 * @author mfukala@netbeans.org, phejl@netbeans.org
 */
@MimeRegistration(mimeType = "text/html", service = JsEmbeddingProviderPlugin.class)
public class KOJsEmbeddingProviderPlugin extends JsEmbeddingProviderPlugin {

    private static final Logger LOGGER = Logger.getLogger(KOJsEmbeddingProviderPlugin.class.getName());

    private static final String WITH_BIND = "with";
    private static final String FOREACH_BIND = "foreach";
    private static final String TEMPLATE_BIND = "template";

    private TokenSequence<HTMLTokenId> tokenSequence;
    private Snapshot snapshot;
    private List<Embedding> embeddings;
    private final Language JS_LANGUAGE;
    private final LinkedList<StackItem> stack;
    private String lastTagOpen = null;

    private final Map<String, KOTemplateContext.TemplateUsage> templateUsages = new HashMap<>();

    private final List<TemplateBoundary> templateBoundaries = new LinkedList<>();

    private final KODataBindContext dataBindContext = new KODataBindContext();

    private final KOTemplateContext templateContext = new KOTemplateContext();

    private KODataBindContext currentTemplateContext;

    private String generatedSource;

    public KOJsEmbeddingProviderPlugin() {
        JS_LANGUAGE = Language.find(KOUtils.JAVASCRIPT_MIMETYPE); //NOI18N
        this.stack = new LinkedList<>();
    }

    @Override
    public boolean startProcessing(HtmlParserResult parserResult, Snapshot snapshot, TokenSequence<HTMLTokenId> tokenSequence, List<Embedding> embeddings) {
        this.snapshot = snapshot;
        this.tokenSequence = tokenSequence;
        this.embeddings = embeddings;

        if(!KOModel.getModel(parserResult).containsKnockout()) {
            return false;
        }

        FileObject fo = snapshot.getSource().getFileObject();
        if (fo != null) {
            generatedSource = Bindings.findBindings(fo, 1);
        }
        return true;
    }

    @Override
    public void endProcessing() {
        int offset = 0;
        // XXX JsEmbeddingProvider:179 - embeddings are cleared on cancel
        // before (!) calling endProcessing
        if (!embeddings.isEmpty()) {
            for (TemplateBoundary boundary : templateBoundaries) {
                if (boundary.isStart()) {
                    KOTemplateContext.TemplateUsage usage = templateUsages.get(boundary.getName());
                    if (usage != null) {
                        KODataBindContext context = usage.getContext();
                        String name = null;
                        Set<KOTemplateContext.TemplateUsage> hierarchy = new LinkedHashSet<KOTemplateContext.TemplateUsage>();
                        hierarchy.add(usage);
                        while (usage != null && (name = usage.getParentTemplateName()) != null) {
                            usage = templateUsages.get(name);
                            // prevents endless loops while evaluating of cycled templates
                            if (hierarchy.contains(usage)) {
                                break;
                            }
                            hierarchy.add(usage);
                            if (usage != null) {
                                context = KODataBindContext.combine(usage.getContext(), context);
                            }
                        }

                        startKnockoutSnippet(context, boundary.getPosition() + offset);
                        offset++;
                    } else {
                        LOGGER.log(Level.FINE, "No usage for template {0}", boundary.getName());
                    }
                } else {
                    endKnockoutSnippet(boundary.getPosition() + offset);
                    offset++;
                }
            }
        }
        templateUsages.clear();
        templateBoundaries.clear();
        dataBindContext.clear();
        templateContext.clear();
        stack.clear();
        lastTagOpen = null;
    }

    @Override
    public boolean processToken() {
        boolean processed = false;

        Pair<Boolean, String> templateCheck = templateContext.process(tokenSequence.token());
        if (templateCheck != null) {
            if (templateCheck.first()) {
                currentTemplateContext = new KODataBindContext();
            } else {
                currentTemplateContext = null;
            }
        }

        String tokenText = tokenSequence.token().text().toString();

        switch (tokenSequence.token().id()) {
            case TAG_OPEN:
                lastTagOpen = tokenText;
                StackItem top = stack.peek();
                if (top != null && top.tag.equals(lastTagOpen)) {
                    top.balance++;
                }
                break;
            case TAG_CLOSE:
                top = stack.peek();
                if (top != null && top.tag.equals(tokenText)) {
                    top.balance--;
                    if (top.balance == 0) {
                        processed = true;
                        stack.pop();
                        String templateId = templateContext.getCurrentScriptId();
                        if (templateId != null) {
                            currentTemplateContext.pop();
                        } else {
                            dataBindContext.pop();
                        }
                    }
                }
                break;
            case VALUE:
                TokenSequence<KODataBindTokenId> embedded = tokenSequence.embedded(KODataBindTokenId.language());
                boolean setData = false;
                boolean setTemplate = false;
                if (embedded != null) {
                    String templateId = templateContext.getCurrentScriptId();
                    if (templateId != null) {
                        templateBoundaries.add(new TemplateBoundary(
                                templateId, embeddings.size(), true));
                    }
                    embedded.moveStart();
                    Token<KODataBindTokenId> dataValue = null;
                    boolean foreach = false;
                    while (embedded.moveNext()) {
                        if (embedded.token().id() == KODataBindTokenId.KEY) {
                            if (WITH_BIND.equals(embedded.token().text().toString()) // NOI18N
                                    || FOREACH_BIND.equals(embedded.token().text().toString())) { // NOI18N
                                stack.push(new StackItem(lastTagOpen));
                                setData = true;
                                foreach = FOREACH_BIND.equals(embedded.token().text().toString()); // NOI18N
                            } else if (TEMPLATE_BIND.equals(embedded.token().text().toString())) {
                                setTemplate = true;
                            }
                        }
                        if (setData && embedded.token().id() == KODataBindTokenId.VALUE && dataValue == null) {
                            dataValue = embedded.token();
                        }
                        if (setTemplate && embedded.token().id() == KODataBindTokenId.VALUE && dataValue == null) {
                            KODataBindContext context = currentTemplateContext != null
                                    ? currentTemplateContext : dataBindContext;
                            KODataBindContext templateBindContext = new KODataBindContext(context);
                            KODataBindDescriptor desc = KODataBindDescriptor.getDataBindDescriptor(
                                    snapshot, embedded.embedded(JsTokenId.javascriptLanguage()), false);
                            if (desc != null) {
                                templateBindContext.push(desc.getData(), desc.isIsForEach(), desc.getAlias());
                                String templateName = desc.getName();
                                KOTemplateContext.TemplateUsage usage = templateUsages.get(templateName);

                                if (usage == null) {
                                    usage = new KOTemplateContext.TemplateUsage(templateBindContext);
                                    if (templateId != null) {
                                        usage.addParentTemplateName(templateId);
                                    }
                                    templateUsages.put(templateName, usage);
                                } else {
                                    KODataBindContext current = usage.getContext();
                                    if (Objects.equals(current.getOriginal(), context)) {
                                        current.setData(current.getData() + " || " + templateBindContext.getData());
                                    } else {
                                        LOGGER.log(Level.INFO, "Multiple incompatible template usage; storing the last one");
                                        usage = new KOTemplateContext.TemplateUsage(templateBindContext);
                                        if (templateId != null) {
                                            usage.addParentTemplateName(templateId);
                                        }
                                        templateUsages.put(templateName, usage);
                                    }
                                }
                            } else {
                                LOGGER.log(Level.INFO, "Cannot get the template name at design time; ignoring");
                            }
                        }
                        if (embedded.embedded(JS_LANGUAGE) != null) {
                            processed = true;

                            if (templateId == null) {
                                startKnockoutSnippet(dataBindContext);
                            }

                            String embeddedText = embedded.token().text().toString();
                            boolean putParenthesis = !embeddedText.trim().isEmpty() &&
                                    !embeddedText.trim().endsWith(";"); // NOI18N

                            if (putParenthesis) {
                                embeddings.add(snapshot.create("(", KOUtils.JAVASCRIPT_MIMETYPE)); // NOI18N
                            }
                            CharSequence seq = embedded.token().text();
                            int emptyLength = 0;
                            for (int i = 0; i < seq.length(); i++) {
                                if (Character.isWhitespace(seq.charAt(i))) {
                                    emptyLength++;
                                } else {
                                    break;
                                }
                            }
                            if (emptyLength < seq.length()) {
                                embeddings.add(snapshot.create(embedded.offset() + emptyLength,
                                        embedded.token().length() - emptyLength, KOUtils.JAVASCRIPT_MIMETYPE));
                            } else {
                                embeddings.add(snapshot.create(embedded.offset(),
                                        embedded.token().length(), KOUtils.JAVASCRIPT_MIMETYPE));
                            }
                            if (putParenthesis) {
                                embeddings.add(snapshot.create(")", KOUtils.JAVASCRIPT_MIMETYPE)); // NOI18N
                            }
                            if (putParenthesis || !embeddedText.trim().endsWith(";")) { // NOI18N
                                embeddings.add(snapshot.create(";", KOUtils.JAVASCRIPT_MIMETYPE)); // NOI18N
                            }

                            if (templateId == null) {
                                endKnockoutSnippet();
                            }
                        }
                    }
                    if (setData) {
                        if (dataValue != null) {
                            if (templateId != null) {
                                currentTemplateContext.push(dataValue.text().toString().trim(), foreach, null);
                            } else {
                                KODataBindDescriptor desc = KODataBindDescriptor.getDataBindDescriptor(
                                        snapshot, embedded.embedded(JsTokenId.javascriptLanguage()), true);
                                if (desc != null) {
                                    dataBindContext.push(desc.getData().trim(), foreach, desc.getAlias());
                                } else {
                                    dataBindContext.push(dataValue.text().toString().trim(), foreach, null);
                                }
                            }
                        } else {
                            stack.pop();
                        }
                    }
                    if (templateId != null) {
                        templateBoundaries.add(new TemplateBoundary(
                                templateId, embeddings.size(), false));
                    }
                }
                break;
            default:
                break;
        }
        return processed;
    }

    private void startKnockoutSnippet(KODataBindContext context) {
        startKnockoutSnippet(context, null);
    }

    private void startKnockoutSnippet(KODataBindContext context, Integer position) {
        StringBuilder sb = new StringBuilder();
        sb.append("(function(){\n"); // NOI18N

        if (generatedSource != null) {
            sb.append(generatedSource).append("\n"); //NOI18N
        }

        // for now this is actually just a placeholder
        sb.append("var $element;\n");

        // define root as reference
        sb.append("var $root = ko.$bindings;\n"); // NOI18N

        if (context.isInForEach()) {
            sb.append("var $index = 0;\n");
        }

        // define data object
        String currentData = context.getData();
        if (currentData == null) {
            currentData = "$root"; // NOI18N
        }

        sb.append("var $parentContext = ");
        generateContext(sb, context.getParents());
        sb.append(";\n");

        sb.append("var $context = ");
        List<ParentContext> current = new ArrayList<>(context.getParents());
        current.add(new ParentContext(currentData, context.isInForEach(), context.getAlias()));
        generateContext(sb, current);
        sb.append(";\n");
        generateParentAndContextData("$context.", sb, context.getParents());

        generateParents(sb, context.getParents());

        generateWithHierarchyStart(sb, context.getParents());

        String dataValue = currentData;
        if ("$root".equals(currentData)) {
            dataValue = "ko.$bindings";
        }
        // may happen if enclosing with/foreach is empty - user is
        // going to fill it
        if (dataValue.trim().isEmpty()) {
            dataValue = "undefined";
        }
        sb.append("var $data = ").append(dataValue).append(";\n");
        if (context.getAlias() != null) {
            sb.append("var ").append(context.getAlias()).append(" = ").append(dataValue).append(";\n");
        }
        generateWithHierarchyEnd(sb, context.getParents());

        sb.append("with ($data) {\n");

        if (position == null) {
            embeddings.add(snapshot.create(sb.toString(), KOUtils.JAVASCRIPT_MIMETYPE));
        } else {
            embeddings.add(position, snapshot.create(sb.toString(), KOUtils.JAVASCRIPT_MIMETYPE));
        }
    }

    private void endKnockoutSnippet() {
        endKnockoutSnippet(null);
    }

    private void endKnockoutSnippet(Integer position) {
        StringBuilder sb = new StringBuilder();
        sb.append("}\n");
        sb.append("});\n");
        if (position == null) {
            embeddings.add(snapshot.create(sb.toString(), KOUtils.JAVASCRIPT_MIMETYPE));
        } else {
            embeddings.add(position, snapshot.create(sb.toString(), KOUtils.JAVASCRIPT_MIMETYPE));
        }
    }

    private static void generateContext(StringBuilder sb, List<ParentContext> parents) {
        if (parents.isEmpty()) {
            sb.append("undefined");
        } else {
            sb.append("{\n");
            sb.append("$parentContext :");
            generateContext(sb, parents.subList(0, parents.size() - 1));
            ParentContext parent = parents.get(parents.size() - 1);
            sb.append(",\n");
            sb.append("$root : ko.$bindings,\n");
            if (parent.isInForEach()) {
                sb.append("$index : 0,\n");
            }
            sb.append("}");
        }
    }

    private static void generateParentAndContextData(String additionalPrefix,
            StringBuilder sb, List<ParentContext> parents) {

        if (parents.isEmpty()) {
            if (additionalPrefix != null) {
                sb.append(additionalPrefix).append("$parentContext.$data = undefined;\n");
            }
            sb.append("$parentContext.$data = undefined;\n");
            sb.append("var $parent = undefined;\n");
            return;
        }
        StringBuilder prefix = new StringBuilder("$parentContext.");
        for (int i = 0; i < parents.size() - 1; i++) {
            sb.append("with (").append(parents.get(i).getValue()).append(") {\n");
        }
        sb.append("var $parent = ").append(parents.get(parents.size() - 1).getValue()).append(";\n");
        for (int i = parents.size() - 2; i >= 0; i--) {
            if (additionalPrefix != null) {
                sb.append(additionalPrefix).append(prefix).append("$data = ").append(parents.get(i + 1).getValue()).append(";\n");
            }
            sb.append(prefix).append("$data = ").append(parents.get(i + 1).getValue()).append(";\n");
            prefix.append("$parentContext.");
            sb.append("}\n");
        }
        if (additionalPrefix != null) {
            sb.append(additionalPrefix).append(prefix).append("$data = ko.$bindings;\n");
        }
        sb.append(prefix).append("$data = ko.$bindings;\n");
    }

    private static void generateParents(StringBuilder sb, List<ParentContext> parents) {
        sb.append("var $parents = ["); // NOI18N
        int pos = sb.length();
        StringBuilder prefix = new StringBuilder("$parentContext.");
        for (int i = 0; i < parents.size(); i++) {
            sb.insert(pos, ",");
            sb.insert(pos, "$data");
            sb.insert(pos, prefix);
            prefix.append("$parentContext.");
        }
        if (!parents.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("];\n"); // NOI18N
    }

    private static void generateWithHierarchyStart(StringBuilder sb, List<ParentContext> parents) {
        for (ParentContext context : parents) {
            if (context.getAlias() != null) {
                sb.append("var ").append(context.getAlias()).append(" = ").append(context.getValue()).append(";\n");
            }
            sb.append("with (").append(context.getValue()).append(") {\n");
        }
    }

    private static void generateWithHierarchyEnd(StringBuilder sb, List<ParentContext> parents) {
        for (int i = 0; i < parents.size(); i++) {
            sb.append("}\n");
        }
    }

    private static class StackItem {

        final String tag;
        
        int balance;

        public StackItem(String tag) {
            this.tag = tag;
            this.balance = 1;
        }
    }

    private static class TemplateBoundary {

        private final String name;

        private final int position;

        private final boolean start;

        public TemplateBoundary(String name, int position, boolean start) {
            this.name = name;
            this.position = position;
            this.start = start;
        }

        public String getName() {
            return name;
        }

        public int getPosition() {
            return position;
        }

        public boolean isStart() {
            return start;
        }
    }

}
