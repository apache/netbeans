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
package org.netbeans.modules.cnd.refactoring.hints;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.Pair;

/**
 *
 */
public abstract class IntroduceVariableBaseFix implements Fix {
    protected final CsmOffsetable expression;
    protected final BaseDocument doc;
    protected String name;

    protected IntroduceVariableBaseFix(CsmOffsetable expression, Document doc) {
        this.expression = expression;
        this.doc = (BaseDocument) doc;
    }

    protected abstract boolean isC();

    protected abstract boolean isInstanceRename();

    protected abstract List<Pair<Integer, Integer>> replaceOccurrences();

    protected abstract String getType();

    protected String suggestName() {
        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenHierarchy<? extends Document> hi = TokenHierarchy.get(doc);
                TokenSequence<?> ts = hi.tokenSequence();
                ts.move(expression.getStartOffset());
                String lastCandidate = null;
                String bestCandidate = null;
                int parenDepth = 0;
                while (ts.moveNext()) {
                    Token<?> token = ts.token();
                    if (ts.offset() > expression.getEndOffset()) {
                        break;
                    }
                    if (CppTokenId.IDENTIFIER.equals(token.id())) {
                        lastCandidate = token.text().toString();
                    } else if (CppTokenId.LPAREN.equals(token.id())) {
                        if (parenDepth == 0) {
                            bestCandidate = lastCandidate;
                        }
                        parenDepth++;
                    } else if (CppTokenId.RPAREN.equals(token.id())) {
                        parenDepth--;
                    }
                }
                if (bestCandidate != null) {
                    name = bestCandidate;
                } else {
                    name = lastCandidate;
                }
            }
        });
        if (name == null) {
            name = "variable"; //NOI18N
        } else {
            if ((name.toLowerCase().startsWith("get") || name.toLowerCase().startsWith("has")) && name.length() > 3) { //NOI18N
                name = name.substring(3);
            } else if (name.toLowerCase().startsWith("is") && name.length() > 2) { //NOI18N
                name = name.substring(2);
            }
        }
        return name;
    }

    protected String suggestType() {
        final CharSequence typeText = getExpressionType();
        if (typeText == null || "void".contentEquals(typeText)) { //NOI18N
            return null;
        }
        return typeText.toString();
    }

    private CharSequence getExpressionType() {
        CsmCacheManager.enter();
        try {
            CharSequence typeText;
            CsmType resolveType = CsmExpressionResolver.resolveType(expression, null);
            if (resolveType == null) {
                return null;
            }
            //if (resolveType.isTemplateBased()) {
            //    CsmClassifier classifier = CsmBaseUtilities.getClassifier(resolveType, expression.getContainingFile(), expression.getStartOffset(), true);
            //    if (!CsmKindUtilities.isTemplate(classifier)) {
            //        CsmTypes.TypeDescriptor typeDescriptor = new CsmTypes.TypeDescriptor(resolveType.isConst(), resolveType.isReference(), resolveType.getPointerDepth(), resolveType.getArrayDepth());
            //        CsmTypes.OffsetDescriptor offsetDescriptor = new CsmTypes.OffsetDescriptor(expression.getContainingFile(), expression.getStartOffset(), expression.getEndOffset());
            //        resolveType = CsmTypes.createType(classifier, typeDescriptor, offsetDescriptor);
            //    }
            //}
            //typeText = CsmDisplayUtilities.getTypeText(resolveType, true, false).toString();
            typeText = CsmInstantiationProvider.getDefault().getInstantiatedText(resolveType);
            if (isC()) {
                CsmClassifier classifier = resolveType.getClassifier();
                if (classifier != null) {
                    if (classifier.getKind() == CsmDeclaration.Kind.STRUCT && !CharSequenceUtils.startsWith(typeText, "struct")) { //NOI18N
                        typeText = "struct " + typeText; //NOI18N
                    }
                }
            }
            return typeText;
        } finally {
            CsmCacheManager.leave();
        }
    }

}
