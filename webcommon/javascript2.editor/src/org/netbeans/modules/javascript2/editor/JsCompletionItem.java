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
package org.netbeans.modules.javascript2.editor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.model.api.IndexedElement;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.editor.options.OptionsUtils;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.editor.spi.ElementDocumentation;
import org.netbeans.modules.javascript2.editor.spi.ProposalRequest;
import org.netbeans.modules.javascript2.model.api.Index;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Petr Pisl
 */
public class JsCompletionItem implements CompletionProposal {

    protected final CompletionRequest request;
    protected final ElementHandle element;

    protected JsCompletionItem(ElementHandle element, CompletionRequest request) {
        this.element = element;
        this.request = request;
    }

    @Override
    public int getAnchorOffset() {
        return LexUtilities.getLexerOffset((JsParserResult)request.info, request.anchor);
    }

    @Override
    public ElementHandle getElement() {
        return element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getInsertPrefix() {
        return element.getName();
    }

    @Override
    public String getSortText() {
        StringBuilder sb = new StringBuilder();
        if (element != null) {
            FileObject sourceFo = request.result.getSnapshot().getSource().getFileObject();
            FileObject elementFo = element.getFileObject();
            if (elementFo != null && sourceFo != null && sourceFo.equals(elementFo)) {
                sb.append("1");     //NOI18N
            } else {
                if (OffsetRange.NONE.equals(element.getOffsetRange(request.result))) {
                    sb.append("8");
                } else {
                    sb.append("9");     //NOI18N
                }
            }
        }
        sb.append(getName());
        return sb.toString();
    }

    protected boolean isDeprecated() {
        return element.getModifiers().contains(Modifier.DEPRECATED);
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatName(formatter);
        return formatter.getText();
    }

    protected void formatName(HtmlFormatter formatter) {
        if (isDeprecated()) {
            formatter.deprecated(true);
            formatter.appendText(getName());
            formatter.deprecated(false);
        } else {
            formatter.appendText(getName());
        }
    }

    @Messages("JsCompletionItem.lbl.js.platform=JS Platform")
    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        String location = null;
        if (element instanceof JsElement jsElement) {
            if (jsElement.isPlatform()) {
                location = Bundle.JsCompletionItem_lbl_js_platform();
            } else if (jsElement.getSourceLabel() != null) {
                location = jsElement.getSourceLabel();
            }
        }
        if (location == null) {
            location = getFileNameURL();
        }
        if (location == null) {
            return null;
        }

        formatter.reset();
        boolean isgues = OffsetRange.NONE.equals(element.getOffsetRange(request.result));
        if (isgues) {
            formatter.appendHtml("<font color=#999999>");
        }
        formatter.appendText(location);
        if (isgues) {
            formatter.appendHtml("</font>");
        }
        return formatter.getText();
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        Set<Modifier> modifiers;

        if (getElement() == null || getElement().getModifiers().isEmpty()) {
            modifiers = Collections.emptySet();
        } else {
            modifiers = EnumSet.noneOf(Modifier.class);
            modifiers.addAll(getElement().getModifiers());
        }

        if (modifiers.contains(Modifier.PRIVATE) && (modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PROTECTED))) {
            modifiers.remove(Modifier.PUBLIC);
            modifiers.remove(Modifier.PROTECTED);
        }
        return modifiers;
    }

    @Override
    public boolean isSmart() {
        // TODO implemented properly
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        int order = 100;
        if (element instanceof JsElement jsElement) {
            if (jsElement.isPlatform()) {
                if (ModelUtils.PROTOTYPE.equals(element.getName())) { //NOI18N
                    order = 1;
                } else {
                    order = 0;
                }
            }
            if (OffsetRange.NONE.equals(element.getOffsetRange(request.result))) {
                order = 120;
            }
        }
        return order;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }

    @CheckForNull
    public final String getFileNameURL() {
        ElementHandle elem = getElement();
        if (elem == null) {
            return null;
        }
        FileObject fo = elem.getFileObject();
        if (fo != null) {
            return fo.getNameExt();
        }
        return getName();
     }

    public static class CompletionRequest {
        public int anchor;
        public JsParserResult result;
        public ParserResult info;
        public String prefix;
        public CompletionContext completionContext;
        public boolean addHtmlTagAttributes;
        public CancelSupport cancelSupport;
        public Collection<String> fqnTypes = new LinkedHashSet<>();
    }

    private static ImageIcon priviligedIcon = null;
    private static ImageIcon publicGenerator = null;
    private static ImageIcon privateGenerator = null;
    private static ImageIcon priviligedGenerator = null;

    public static class JsFunctionCompletionItem extends JsCompletionItem {

        private final Set<String> returnTypes;
        private final Map<String, Set<String>> parametersTypes;
        JsFunctionCompletionItem(ElementHandle element, CompletionRequest request, Set<String> resolvedReturnTypes, Map<String, Set<String>> parametersTypes) {
            super(element, request);
            this.returnTypes = resolvedReturnTypes != null ? resolvedReturnTypes : Collections.emptySet();
            this.parametersTypes = parametersTypes != null ? parametersTypes : Collections.<String, Set<String>>emptyMap();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.setMaxLength(OptionsUtils.forLanguage(JsTokenId.javascriptLanguage()).getCodeCompletionItemSignatureWidth());
            formatter.emphasis(true);
            formatName(formatter);
            formatter.emphasis(false);
            if (!asObject()) {
                formatter.appendText("(");  //NOI18N
                appendParamsStr(formatter);
                formatter.appendText(")");  //NOI18N
                appendReturnTypes(formatter);
            }
            return formatter.getText();
        }

        private void appendParamsStr(HtmlFormatter formatter){
            for (Iterator<Map.Entry<String, Set<String>>> it = parametersTypes.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Set<String>> entry = it.next();
                formatter.parameters(true);
                formatter.appendText(entry.getKey());
                formatter.parameters(false);
                Collection<String> types = entry.getValue();
                if (!types.isEmpty()) {
                    formatter.type(true);
                    formatter.appendText(": ");  //NOI18N
                    for (Iterator<String> itTypes = types.iterator(); itTypes.hasNext();) {
                        formatter.appendText(itTypes.next());
                        if (itTypes.hasNext()) {
                            formatter.appendText("|");   //NOI18N
                        }
                    }
                    formatter.type(false);
                }
                if (it.hasNext()) {
                    formatter.appendText(", ");  //NOI18N
                }
            }
        }

        private void appendReturnTypes(HtmlFormatter formatter) {
            if (!returnTypes.isEmpty()) {
                formatter.appendText(": "); //NOI18N
                formatter.type(true);
                for (Iterator<String> it = returnTypes.iterator(); it.hasNext();) {
                    formatter.appendText(it.next());
                    if (it.hasNext()) {
                        formatter.appendText("|"); //NOI18N
                    }
                }
                formatter.type(false);
            }
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder template = new StringBuilder();
            template.append(getName());
            if (!asObject()) {
                if (parametersTypes.isEmpty()) {
                    template.append("()${cursor}");     //NOI18N
                } else {
                    template.append("(${cursor})");     //NOI18N
                }
            } else {
                template.append("${cursor}");       //NOI18N
            }
            return template.toString();
        }

        @Override
        public ImageIcon getIcon() {
            if (getModifiers().contains(Modifier.PROTECTED)) {
                if(priviligedIcon == null) {
                    priviligedIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/javascript2/editor/resources/methodPriviliged.png", false); //NOI18N
                }
                return priviligedIcon;
            }
            return super.getIcon(); //To change body of generated methods, choose Tools | Templates.
        }

        private boolean isAfterNewKeyword() {
            boolean isAfterNew = false;
            Snapshot snapshot = request.result.getSnapshot();
            int offset = request.anchor;
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(snapshot, snapshot.getOriginalOffset(offset));
            if (ts != null) {
                ts.move(offset);
                if (ts.moveNext()) {
                    Token<? extends JsTokenId> token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.IDENTIFIER, JsTokenId.PRIVATE_IDENTIFIER, JsTokenId.OPERATOR_DOT, JsTokenId.OPERATOR_OPTIONAL_ACCESS, JsTokenId.BLOCK_COMMENT, JsTokenId.WHITESPACE, JsTokenId.LINE_COMMENT, JsTokenId.EOL));
                    if (token.id() == JsTokenId.KEYWORD_NEW) {
                        isAfterNew = true;
                    }
                }
            }
            return isAfterNew;
        }

        /**
         *
         * @return true if the element should be treated as an object or function in the context
         */
        private boolean asObject() {
            boolean result = false;
            char firstChar = getName().charAt(0);
            JsElement.Kind jsKind = null;
            if (element instanceof JsElement jsElement) {
                jsKind = jsElement.getJSKind();
            }
            if ((jsKind != null && jsKind == JsElement.Kind.CONSTRUCTOR) || Character.isUpperCase(firstChar)) {
                boolean isAfterNew = isAfterNewKeyword();
                if (!isAfterNew) {
                    // check return types, whether it can be really constructor
                    for (String type : returnTypes) {
                        if (type.endsWith(element.getName())) {
                            return true;
                        }
                    }
                    if (returnTypes.isEmpty()) {
                        result = true;
                    } else if (returnTypes.size() == 1) {
                        String type = returnTypes.iterator().next();
                        firstChar = type.charAt(0);
                        if (Character.isUpperCase(firstChar) && !(Type.NUMBER.equals(type) || Type.BOOLEAN.equals(type)
                                || Type.STRING.equals(type) || Type.ARRAY.equals(type))) {
                            result = true;
                        }
                    }
                }
            }
            return result;
        }
    }

    public static class JsGeneratorCompletionItem extends JsFunctionCompletionItem {

        public JsGeneratorCompletionItem(ElementHandle element, CompletionRequest request, Set<String> resolvedReturnTypes, Map<String, Set<String>> parametersTypes) {
            super(element, request, resolvedReturnTypes, parametersTypes);
        }

        @Override
        public ImageIcon getIcon() {
            if (getModifiers().contains(Modifier.PUBLIC)) {
                if (publicGenerator == null) {
                    publicGenerator = ImageUtilities.loadImageIcon("org/netbeans/modules/javascript2/editor/resources/generatorPublic.png", false); //NOI18N
                }
                return publicGenerator;
            } else if (getModifiers().contains(Modifier.PRIVATE)) {
                if (privateGenerator == null) {
                    privateGenerator = ImageUtilities.loadImageIcon("org/netbeans/modules/javascript2/editor/resources/generatorPrivate.png", false); //NOI18N
                }
                return privateGenerator;
            } else if (getModifiers().contains(Modifier.PROTECTED)) {
                if (priviligedGenerator == null) {
                    priviligedGenerator = ImageUtilities.loadImageIcon("org/netbeans/modules/javascript2/editor/resources/generatorPriviliged.png", false); //NOI18N
                }
                return priviligedGenerator;
            }
            return super.getIcon();
        }

    }

    public static class JsCallbackCompletionItem extends JsCompletionItem {
        private static ImageIcon callbackIcon = null;
        private final IndexedElement.FunctionIndexedElement function;

        public JsCallbackCompletionItem(IndexedElement.FunctionIndexedElement element, CompletionRequest request) {
            super(element, request);
            function = element;
        }

        @Override
        public ImageIcon getIcon() {
            if (callbackIcon == null) {
                callbackIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/javascript2/editor/resources/methodCallback.png", false); //NOI18N
            }
            return callbackIcon;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.setMaxLength(OptionsUtils.forLanguage(JsTokenId.javascriptLanguage()).getCodeCompletionItemSignatureWidth());
            formatter.name(ElementKind.KEYWORD, true);
            formatter.appendText("function");   //NOI18N
            formatter.name(ElementKind.KEYWORD, false);
            formatter.appendText(" (");  //NOI18N
            appendParamsStr(formatter);
            formatter.appendText(")");  //NOI18N
            return formatter.getText();
        }

        @Override
        public int getSortPrioOverride() {
            return 90;      // display as first items?
        }


        private void appendParamsStr(HtmlFormatter formatter){
            for (Iterator<Map.Entry<String, Collection<String>>> it = function.getParameters().entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Collection<String>> entry = it.next();
                formatter.parameters(true);
                formatter.appendText(entry.getKey());
                formatter.parameters(false);
                Collection<String> types = entry.getValue();
                if (!types.isEmpty()) {
                    formatter.type(true);
                    formatter.appendText(": ");  //NOI18N
                    for (Iterator<String> itTypes = types.iterator(); itTypes.hasNext();) {
                        formatter.appendText(itTypes.next());
                        if (itTypes.hasNext()) {
                            formatter.appendText("|");   //NOI18N
                        }
                    }
                    formatter.type(false);
                }
                if (it.hasNext()) {
                    formatter.appendText(", ");  //NOI18N
                }
            }
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder template = new StringBuilder();
            template.append(" \n /** ");    //NOI18N
            for (Iterator<Map.Entry<String, Collection<String>>> it = function.getParameters().entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Collection<String>> entry = it.next();
                Collection<String> types = entry.getValue();
                template.append("\n * @param {");//NOI18N
                if (!types.isEmpty()) {
                    for (Iterator<String> itTypes = types.iterator(); itTypes.hasNext();) {
                        template.append(itTypes.next());
                        if (itTypes.hasNext()) {
                            template.append("|");   //NOI18N
                        }
                    }
                } else {
                    template.append("Object");//NOI18N
                }
                template.append("} ");//NOI18N
                template.append(entry.getKey());
            }
            template.append("\n */");//NOI18N
            template.append("\nfunction (");//NOI18N
            for (Iterator<Map.Entry<String, Collection<String>>> it = function.getParameters().entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Collection<String>> entry = it.next();
                template.append(entry.getKey());
                if (it.hasNext()) {
                    template.append(", ");  //NOI18N
                }
            }
            template.append(") {\n ${cursor}\n}");//NOI18N
            return template.toString();
        }

        @Override
        public String getName() {
            return "function";
        }

    }

    static class KeywordItem extends JsCompletionItem {

        private static  ImageIcon keywordIcon = null;

        private final String keyword;

        private final JsKeywords.CompletionDescription description;

        public KeywordItem(String keyword, JsKeywords.CompletionDescription description, CompletionRequest request) {
            super(null, request);
            this.keyword = keyword;
            this.description = description;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);
            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            JsVersion since = description.getVersion();
            if (since != null) {
                formatter.appendText(since.getDisplayName());
                return formatter.getText();
            }
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            if (keywordIcon == null) {
                keywordIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/javascript2/editor/resources/javascript.png", false); //NOI18N
            }
            return keywordIcon;
        }

        @Override
        public String getInsertPrefix() {
            return getName();
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder builder = new StringBuilder();

            JsKeywords.CompletionType type = description.getType();
            if (type == null) {
                return getName();
            }

            switch(type) {
                case SIMPLE -> {
                    builder.append(getName());
                }
                case ENDS_WITH_SPACE -> {
                    builder.append(getName());
                    builder.append(" ${cursor}"); //NOI18N
                }
                case CURSOR_INSIDE_BRACKETS -> {
                    builder.append(getName());
                    builder.append("(${cursor})"); //NOI18N
                }
                case ENDS_WITH_CURLY_BRACKETS -> {
                    builder.append(getName());
                    builder.append(" {${cursor}}"); //NOI18N
                }
                case ENDS_WITH_SEMICOLON -> {
                    builder.append(getName());
                    CharSequence text = request.info.getSnapshot().getText();
                    int index = request.anchor + request.prefix.length();
                    if (index == text.length() || ';' != text.charAt(index)) { //NOI18N
                        builder.append(";"); //NOI18N
                    }
                }
                case ENDS_WITH_COLON -> {
                    builder.append(getName());
                    builder.append(" ${cursor}:"); //NOI18N
                }
                case ENDS_WITH_DOT -> {
                    builder.append(getName());
                    builder.append(".${cursor}"); //NOI18N
                }
                default -> {
                    assert false : type.toString();
                }
            }
            return builder.toString();
        }

        @Override
        public int getSortPrioOverride() {
            return 130;
        }
    }

    public static class JsPropertyCompletionItem extends JsCompletionItem {

        private final Set<String> resolvedTypes;

        JsPropertyCompletionItem(ElementHandle element, CompletionRequest request, Set<String> resolvedTypes) {
            super(element, request);
            this.resolvedTypes = resolvedTypes != null ? resolvedTypes : Collections.emptySet();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatName(formatter);
            if (!resolvedTypes.isEmpty()) {
                formatter.type(true);
                formatter.appendText(": ");  //NOI18N
                for (Iterator<String> it = resolvedTypes.iterator(); it.hasNext();) {
                    formatter.appendText(it.next());
                    if (it.hasNext()) {
                        formatter.appendText("|");   //NOI18N
                    }
                }
                formatter.type(false);
            }
            return formatter.getText();
        }

        @Override
        public String getCustomInsertTemplate() {
            if (request.completionContext == CompletionContext.OBJECT_PROPERTY_NAME) {
                return getName() + ": ${cursor}"; // NOI18N
            }
            return super.getCustomInsertTemplate(); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public static class Factory {

        public static void create( Map<String, List<JsElement>> items, CompletionRequest request, List<CompletionProposal> result) {
            CancelSupport cancelSupport = request.cancelSupport;
            if (cancelSupport.isCancelled()) {
                return;
            }
            // This maps unresolved types to the display name of the resolved type.
            // It should save time to not resolve one type more times
            HashMap<String, Set<String>> resolvedTypes = new HashMap<>();

            for (Map.Entry<String, List<JsElement>> entry: items.entrySet()) {

                // this helps to eleminate items that will look as the same items in the cc
                HashMap<String, JsCompletionItem> signatures = new HashMap<>();
                Index jsIndex = null;
                if (OptionsUtils.forLanguage(JsTokenId.javascriptLanguage()).autoCompletionTypeResolution()) {
                    jsIndex = Index.get(request.info.getSnapshot().getSource().getFileObject());
                }
                for (JsElement element : entry.getValue()) {
                    if (cancelSupport.isCancelled()) {
                        return;
                    }
                    switch (element.getJSKind()) {
                        case CONSTRUCTOR, FUNCTION, METHOD, GENERATOR, ARROW_FUNCTION -> {
                            Set<String> returnTypes = new HashSet<>();
                            HashMap<String, Set<String>> allParameters = new LinkedHashMap<>();
                            if (element instanceof JsFunction) {
                                // count return types
                                Collection<TypeUsage> resolveTypes = ModelUtils.resolveTypes(((JsFunction) element).getReturnTypes(),
                                        Model.getModel(request.info, false),
                                        jsIndex, false);
                                returnTypes.addAll(Utils.getDisplayNames(resolveTypes));
                                // count parameters type
                                for (JsObject jsObject : ((JsFunction) element).getParameters()) {
                                    Set<String> paramTypes = new HashSet<>();
                                    for (TypeUsage type : jsObject.getAssignmentForOffset(jsObject.getOffset() + 1)) {
                                        Set<String> resolvedType = resolvedTypes.get(type.getType());
                                        if (resolvedType == null) {
                                            resolvedType = new HashSet<>(1);
                                            String displayName = ModelUtils.getDisplayName(type);
                                            if (!displayName.isEmpty()) {
                                                resolvedType.add(displayName);
                                            }
                                            resolvedTypes.put(type.getType(), resolvedType);
                                        }
                                        paramTypes.addAll(resolvedType);
                                    }
                                    allParameters.put(jsObject.getName(), paramTypes);
                                }
                            } else if (element instanceof IndexedElement.FunctionIndexedElement functionIndexedElement) {
                                // count return types
                                HashSet<TypeUsage> returnTypeUsages = new HashSet<>();
                                for (String type : functionIndexedElement.getReturnTypes()) {
                                    returnTypeUsages.add(new TypeUsage(type, -1, false));
                                }
                                Collection<TypeUsage> resolveTypes = ModelUtils.resolveTypes(returnTypeUsages,
                                        Model.getModel(request.info, false),
                                        jsIndex, false);
                                returnTypes.addAll(Utils.getDisplayNames(resolveTypes));
                                // count parameters type
                                LinkedHashMap<String, Collection<String>> parameters = functionIndexedElement.getParameters();
                                for (Map.Entry<String, Collection<String>> paramEntry : parameters.entrySet()) {
                                    Set<String> paramTypes = new HashSet<>();
                                    for (String type : paramEntry.getValue()) {
                                        Set<String> resolvedType = resolvedTypes.get(type);
                                        if (resolvedType == null) {
                                            resolvedType = new HashSet<>(1);
                                            String displayName = ModelUtils.getDisplayName(type);
                                            if (!displayName.isEmpty()) {
                                                resolvedType.add(displayName);
                                            }
                                            resolvedTypes.put(type, resolvedType);
                                        }
                                        paramTypes.addAll(resolvedType);
                                    }
                                    allParameters.put(paramEntry.getKey(), paramTypes);
                                }
                            }
                            // create signature
                            String signature = createFnSignature(entry.getKey(), allParameters, returnTypes);
                            if (!signatures.containsKey(signature)) {
                                JsCompletionItem item = element.getJSKind() != JsElement.Kind.GENERATOR
                                        ? new JsFunctionCompletionItem(element, request, returnTypes, allParameters)
                                        : new JsGeneratorCompletionItem(element, request, returnTypes, allParameters);
                                signatures.put(signature, item);
                            }
                        }
                        case PARAMETER, PROPERTY, PROPERTY_GETTER, PROPERTY_SETTER, FIELD, VARIABLE -> {
                            Set<String> typesToDisplay = new HashSet<>();
                            Collection<? extends TypeUsage> assignment = null;
                            if (element instanceof JsObject jsObject) {
                                assignment = jsObject.getAssignments();
                            } else if (element instanceof IndexedElement iElement) {
                                assignment = iElement.getAssignments();
                            }
                            if (assignment != null && !assignment.isEmpty()) {
                                HashSet<TypeUsage> toResolve = new HashSet<>();
                                for (TypeUsage type : assignment) {
                                    if (type.isResolved()) {
                                        if (!Type.UNDEFINED.equals(type.getType())) {
                                            typesToDisplay.add(ModelUtils.getDisplayName(type));
                                        }
                                    } else {
                                        Set<String> resolvedType = resolvedTypes.get(type.getType());
                                        if (resolvedType == null) {
                                            toResolve.clear();
                                            toResolve.add(type);
                                            resolvedType = new HashSet<>(1);
                                            Collection<TypeUsage> resolved = ModelUtils.resolveTypes(toResolve,
                                                    Model.getModel(request.result, false),
                                                    jsIndex, false);
                                            for (TypeUsage rType : resolved) {
                                                String displayName = ModelUtils.getDisplayName(rType);
                                                if (!displayName.isEmpty()) {
                                                    resolvedType.add(displayName);
                                                }
                                            }
                                            resolvedTypes.put(type.getType(), resolvedType);
                                        }
                                        typesToDisplay.addAll(resolvedType);
                                    }
                                }
                            }
                            // signatures
                            String signature = element.getName() + ":" + createTypeSignature(typesToDisplay);
                            if (!signatures.containsKey(signature)) {
                                // add the item to the cc only if doesn't exist any similar
                                JsCompletionItem item = new JsPropertyCompletionItem(element, request, typesToDisplay);
                                signatures.put(signature, item);
                            }
                        }
                        default -> {
                            String signature = element.getName();
                            if (!signatures.containsKey(signature)) {
                                JsCompletionItem item = new JsCompletionItem(element, request);
                                signatures.put(signature, item);
                            }
                        }
                    }
                }
                for (JsCompletionItem item: signatures.values()) {
                    result.add(item);
                }
            }
        }

        private static String createFnSignature(String name, HashMap<String, Set<String>> params, Set<String> returnTypes) {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append('(');
            for (Map.Entry<String, Set<String>> entry : params.entrySet()) {
                sb.append(entry.getKey()).append(':');
                sb.append(createTypeSignature(entry.getValue()));
                sb.append(',');
            }
            sb.append(')');
            sb.append(createTypeSignature(returnTypes));
            return sb.toString();
        }

        private static String createTypeSignature(Set<String> types) {
            StringBuilder sb = new StringBuilder();
            for(String name: types){
                sb.append(name).append('|');
            }
            return sb.toString();
        }
    }

    public abstract static class SimpleDocElement implements ElementHandle, ElementDocumentation {

        private final String name;
        private final ElementKind kind;

        public SimpleDocElement(String name, ElementKind kind) {
            this.name = name;
            this.kind = kind;
        }


        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return "";
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getIn() {
            return "";
        }

        @Override
        public ElementKind getKind() {
            return kind;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.<Modifier>emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }
    }

    /**
     * Creates default ProposalRequest. Used by compatibility bridge in new CC interface.
     * @param ccContext parser context
     * @param jsCompletionContext js completion type
     * @param prefix typed prefix
     * @return initialized ProposalRequest
     */
    public static ProposalRequest createRequest(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        int caretOffset = ccContext.getParserResult().getSnapshot().getEmbeddedOffset(ccContext.getCaretOffset());
        String pref = ccContext.getPrefix();
        int offset = pref == null ? caretOffset : caretOffset
                    // can't just use 'prefix.getLength()' here cos it might have been calculated with
                    // the 'upToOffset' flag set to false
                    - pref.length();
        return new ProposalRequest(ccContext, jsCompletionContext, null, offset);
    }
}
