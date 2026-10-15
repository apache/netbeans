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
package org.netbeans.modules.php.editor.completion;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;
import org.netbeans.modules.php.editor.completion.PHPCompletionItem.CompletionRequest;
import org.netbeans.modules.php.editor.index.PHPDOCTagElement;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.project.api.PhpAnnotations;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.openide.util.ImageUtilities;

/**
 *
 * @author tomslot
 */
public final class PHPDOCCodeCompletion {

    private static final String TAG_PREFIX = "@"; //NOI18N

    private PHPDOCCodeCompletion() {
    }

    private interface FetchedTextCallback {
        void call(String fetchedText);
    }

    private static final class TypeContextChecker implements FetchedTextCallback {
        private boolean isTypeContext = true;

        @Override
        public void call(String fetchedTxt) {
            // remove nullable type prefixes
            String fetchedText = CodeUtils.removeNullableTypePrefix(fetchedTxt);
            if (fetchedText.endsWith(CodeUtils.NULLABLE_TYPE_PREFIX)) {
                fetchedText = fetchedText.substring(0, fetchedText.length() - 1);
            }

            String trimmedText = fetchedText.trim();
            if (!trimmedText.isEmpty() && fetchedText.charAt(fetchedText.length() - 1) == '|') { //NOI18N
                // expect that user wants to complete mixed type
                boolean textPartHasWhitespace = false;
                for (int i = 0; i < trimmedText.length(); i++) {
                    if (Character.isWhitespace(trimmedText.charAt(i))) {
                        textPartHasWhitespace = true;
                        break;
                    }
                }
                isTypeContext = !textPartHasWhitespace;
            } else {
                for (int i = 0; i < fetchedText.length(); i++) {
                    if (!Character.isWhitespace(fetchedText.charAt(i))) {
                        isTypeContext = false;
                        break;
                    }
                }
            }
        }

        public boolean isTypeContext() {
            return isTypeContext;
        }

    }

    private static final class MemberCompletion implements FetchedTextCallback {
        private final PHPCompletionResult completionResult;
        private final CompletionRequest request;

        public MemberCompletion(PHPCompletionResult completionResult, CompletionRequest request) {
            this.completionResult = completionResult;
            this.request = request;
        }

        @Override
        public void call(String possibleType) {
            if (!possibleType.trim().isEmpty() && possibleType.endsWith("::")) { //NOI18N
                String type = possibleType.substring(0, possibleType.length() - 2);
                Set<TypeElement> types = request.index.getTypes(NameKind.exact(type));
                TypeElement typeElement = ModelUtils.getFirst(types);
                if (typeElement != null) {
                    completeTypeMembers(typeElement);
                }
            }
        }

        private void completeTypeMembers(TypeElement typeElement) {
            Set<TypeMemberElement> accessibleTypeMembers = request.index.getAccessibleTypeMembers(typeElement, null);
            for (TypeMemberElement typeMemberElement : accessibleTypeMembers) {
                completeTypeMember(typeMemberElement);
            }
        }

        private void completeTypeMember(TypeMemberElement typeMemberElement) {
            if (typeMemberElement instanceof MethodElement) {
                completeMethods((MethodElement) typeMemberElement);
            } else if (typeMemberElement instanceof FieldElement) {
                completeFields((FieldElement) typeMemberElement);
            } else if (typeMemberElement instanceof TypeConstantElement) {
                completeConstants((TypeConstantElement) typeMemberElement);
            }
        }

        private void completeMethods(MethodElement method) {
            List<PHPCompletionItem.MethodElementItem> items = PHPCompletionItem.MethodElementItem.getItems(method, request);
            for (PHPCompletionItem.MethodElementItem methodItem : items) {
                completionResult.add(methodItem);
            }
        }

        private void completeFields(FieldElement field) {
            PHPCompletionItem.FieldItem fieldItem = PHPCompletionItem.FieldItem.getItem(field, request, true);
            completionResult.add(fieldItem);
        }

        private void completeConstants(TypeConstantElement constant) {
            PHPCompletionItem.TypeConstantItem constantItem = PHPCompletionItem.TypeConstantItem.getItem(constant, request);
            completionResult.add(constantItem);
        }

    }

    private static void fetchDocText(CompletionRequest request, FetchedTextCallback callback) {
        TokenHierarchy<?> th = request.info.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> phpTS = (th != null) ? LexUtilities.getPHPTokenSequence(th, request.anchor) : null;
        if (phpTS != null) {
            phpTS.move(request.anchor);
            if (phpTS.moveNext()) {
                TokenSequence<PHPDocCommentTokenId> tokenSequence = phpTS.embedded(PHPDocCommentTokenId.language());
                if (tokenSequence != null) {
                    tokenSequence.move(request.anchor);
                    if (tokenSequence.movePrevious()) {
                        int offset = tokenSequence.offset() + tokenSequence.token().length();
                        if (tokenSequence.moveNext()) {
                            CharSequence tokenText = tokenSequence.token().text();
                            String text = tokenText.subSequence(0, request.anchor - offset).toString().trim();
                            callback.call(text);
                        }
                    }
                }
            }
        }
    }

    static boolean isTypeCtx(PHPCompletionItem.CompletionRequest request) {
        TypeContextChecker typeContextChecker = new TypeContextChecker();
        fetchDocText(request, typeContextChecker);
        return typeContextChecker.isTypeContext();
    }

    public static void complete(final PHPCompletionResult completionResult, final CompletionRequest request) {
        if (request.prefix.startsWith(TAG_PREFIX)) {
            completeAnnotation(completionResult, request);
        }
        fetchDocText(request, new MemberCompletion(completionResult, request));
    }

    private static void completeAnnotation(final PHPCompletionResult completionResult, final CompletionRequest request) {
        String prefix = request.prefix.substring(TAG_PREFIX.length());
        List<AnnotationCompletionTagProvider> providers = PhpAnnotations.getDefault().getCompletionTagProviders(request.info.getSnapshot().getSource().getFileObject());
        ASTNode nodeAfterOffset = Utils.getNodeAfterOffset(request.result, request.anchor);
        int priority = 0;
        for (AnnotationCompletionTagProvider annotationProvider : providers) {
            priority++;
            List<AnnotationCompletionTag> annotations;
            if (nodeAfterOffset instanceof TypeDeclaration) {
                annotations = annotationProvider.getTypeAnnotations();
            } else if (nodeAfterOffset instanceof MethodDeclaration) {
                annotations = annotationProvider.getMethodAnnotations();
            } else if (nodeAfterOffset instanceof FunctionDeclaration) {
                annotations = annotationProvider.getFunctionAnnotations();
            } else if (nodeAfterOffset instanceof FieldsDeclaration) {
                annotations = annotationProvider.getFieldAnnotations();
            } else {
                annotations = annotationProvider.getAnnotations();
            }
            for (AnnotationCompletionTag tag : annotations) {
                if (tag.getName().startsWith(prefix)) {
                    completionResult.add(new PHPDOCCodeCompletionItem(request.anchor, tag, annotationProvider.getName(), priority));
                }
            }
        }
    }

    public static class PHPDOCCodeCompletionItem implements CompletionProposal {
        private static final String PHP_ANNOTATION_ICON = "org/netbeans/modules/php/editor/resources/annotation.png"; //NOI18N
        private static final ImageIcon ANNOTATION_ICON = ImageUtilities.loadImageIcon(PHP_ANNOTATION_ICON, false);
        private final AnnotationCompletionTag tag;
        private final int anchorOffset;
        private final PHPDOCTagElement elem;
        private final String providerName;
        private final int priority;

        public PHPDOCCodeCompletionItem(int anchorOffset, AnnotationCompletionTag tag, String providerName, int priority) {
            this.tag = tag;
            this.anchorOffset = anchorOffset;
            this.providerName = providerName;
            this.priority = priority;
            elem = new PHPDOCTagElement(tag.getName(), tag.getDocumentation());
        }

        @Override
        public int getAnchorOffset() {
            return anchorOffset;
        }

        @Override
        public ElementHandle getElement() {
            return elem;
        }

        @Override
        public String getName() {
            return TAG_PREFIX + tag.getName(); //NOI18N
        }

        @Override
        public String getInsertPrefix() {
            return getName();
        }

        @Override
        public String getSortText() {
            return priority + providerName + getName();
        }

        @Override
        public int getSortPrioOverride() {
            return 0;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);
            tag.formatParameters(formatter);
            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return providerName;
        }

        @Override
        public ElementKind getKind() {
            return elem.getKind();
        }

        @Override
        public ImageIcon getIcon() {
            return ANNOTATION_ICON;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.<Modifier>emptySet();
        }

        @Override
        public boolean isSmart() {
            return false;
        }

        @Override
        public String getCustomInsertTemplate() {
            return tag.getInsertTemplate();
        }
    }
}
