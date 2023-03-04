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

package org.netbeans.modules.groovy.editor.hints;

import org.netbeans.modules.groovy.editor.hints.utils.HintUtils;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.compiler.error.CompilerErrorID;
import org.netbeans.modules.groovy.editor.compiler.error.GroovyError;
import org.netbeans.modules.groovy.editor.hints.infrastructure.GroovyErrorRule;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import org.openide.util.NbBundle;

/**
 * This rule is used when the class is extending class/implementing interface but
 * does not contain implementation of all abstract methods from it's parent. This
 * hint solves the situation by adding abstract keyword into the class definition.
 * 
 * @author Martin Janicek
 */
public final class MakeClassAbstractHint extends GroovyErrorRule {
    
    @Override
    public Set<CompilerErrorID> getCodes() {
        return EnumSet.of(CompilerErrorID.CLASS_DOES_NOT_IMPLEMENT_ALL_METHODS);
    }

    @Override
    public void run(RuleContext context, GroovyError error, List<Hint> result) {
        for (Hint existingHint : result) {
            if (existingHint.getRule() instanceof MakeClassAbstractHint) {
                return;
            }
        }
        
        OffsetRange range = HintUtils.getLineOffset(context, error);
        if (range != null) {
            HintFix fix = new MakeClassAbstractFix(error);
            
            result.add(new Hint(this, error.getDescription(), error.getFile(), range, Collections.singletonList(fix), 200));
        }
    }
    
    @Override
    public boolean appliesTo(RuleContext context) {
        return true;
    }

    @Override
    @NbBundle.Messages({"Test=test"})
    public String getDisplayName() {
        return Bundle.Test();
    }

    @Override
    public boolean showInTasklist() {
        return false;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }
    
    private static class MakeClassAbstractFix implements PreviewableFix {

        private final GroovyError error;

        
        public MakeClassAbstractFix(GroovyError error) {
            this.error = error;
        }

        @Override
        public void implement() throws Exception {
            getEditList().apply();
        }

        @Override
        public EditList getEditList() throws Exception {
            BaseDocument baseDoc = LexUtilities.getDocument(error.getFile(), true);
            EditList edits = new EditList(baseDoc);
            if (baseDoc != null) {
                int classPosition = getInsertPosition(baseDoc);
                if (classPosition != 0) {
                    edits.replace(classPosition, 0, "abstract ", false, 0);
                }
            }
            return edits;
        }

        private int getInsertPosition(BaseDocument doc) {
            TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, 1);

            while (ts.moveNext()) {
                Token t = ts.token();

                if (t.id() == GroovyTokenId.LITERAL_class) {
                    int offset = ts.offset();
                    if (isCorrectClassDefinition(ts)) {
                        return offset;
                    }
                }
            }

            return 0;
        }
        
        private boolean isCorrectClassDefinition(TokenSequence<GroovyTokenId> ts) {
            while (ts.moveNext()) {
                Token<GroovyTokenId> token = ts.token();
                TokenId id = token.id();
                
                if (id == GroovyTokenId.IDENTIFIER) {
                    String identifierName = token.text().toString();
                    
                    if (identifierName.equals(getClassName())) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        }

        @Override
        @NbBundle.Messages({"# {0} - class name", "MakeClassAbstract=Make class {0} abstract"})
        public String getDescription() {
            return Bundle.MakeClassAbstract(getClassName());
        }
        
        @NonNull
        public String getClassName() {
            String errorMessage = error.getDescription();
            String classNamePrefix = "Can't have an abstract method in a non-abstract class. The class '"; // NOI18N
            String classNameSuffix = "' must be declared abstract or the method '"; // NOI18N

            int endOffset = errorMessage.indexOf(classNameSuffix);
            // Strip everything after the fqn definition
            String fqName = errorMessage.substring(0, endOffset);
            // Strip prefix before the fqn definition
            fqName = fqName.replace(classNamePrefix, "");
            
            return GroovyUtils.stripPackage(fqName);
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

        @Override
        public boolean canPreview() {
            return true;
        }
    }
}
