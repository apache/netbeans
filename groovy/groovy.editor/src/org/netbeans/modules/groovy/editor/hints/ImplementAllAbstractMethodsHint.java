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

import java.util.ArrayList;
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
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.compiler.error.CompilerErrorID;
import org.netbeans.modules.groovy.editor.compiler.error.GroovyError;
import org.netbeans.modules.groovy.editor.hints.infrastructure.GroovyErrorRule;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * This rule is used when the class is extending class/implementing interface but
 * does not contain implementation of all abstract methods from the parent. This
 * hint solves the situation by implementing all missing methods with empty body.
 * 
 * @author Martin Janicek
 */
public final class ImplementAllAbstractMethodsHint extends GroovyErrorRule {

    @Override
    public Set<CompilerErrorID> getCodes() {
        return EnumSet.of(CompilerErrorID.CLASS_DOES_NOT_IMPLEMENT_ALL_METHODS);
    }

    @Override
    public void run(RuleContext context, GroovyError error, List<Hint> result) {
        FileObject fo = context.parserResult.getSnapshot().getSource().getFileObject();
        
        AddMethodStubsFix fix = findExistingFix(result);
        if (fix == null) {
            fix = new AddMethodStubsFix(error);

            OffsetRange range = HintUtils.getLineOffset(context, error);
            if (range != null) {
                result.add(new Hint(this, error.getDescription(), fo, range, Collections.<HintFix>singletonList(fix), 100));
            }
        }
        
        fix.addMethodSignature(getMethodName(error));
    }
    
    private AddMethodStubsFix findExistingFix(List<Hint> result) {
        for (Hint existingHint : result) {
            if (existingHint.getRule() instanceof ImplementAllAbstractMethodsHint) {
                List<HintFix> fixes = existingHint.getFixes();
                
                if (fixes != null && !fixes.isEmpty()) {
                    HintFix fix = fixes.get(0);
                    if (fix instanceof AddMethodStubsFix) {
                        return (AddMethodStubsFix) fix;
                    }
                }
            }
        }
        return null;
    }
    
    @NonNull
    public String getMethodName(@NonNull GroovyError error) {
        String errorMessage = error.getDescription();
        String classNameSuffix = "' must be declared abstract or the method '"; // NOI18N
        String methodSuffix = "' must be implemented."; // NOI18N
        
        // Strip everything before the method declaration
        int startOffset = errorMessage.indexOf(classNameSuffix) + classNameSuffix.length();
        String methodName = errorMessage.substring(startOffset);
        
        // Strip everything after the method declaration
        int suffixOffset = methodName.indexOf(methodSuffix);
        String suffix = methodName.substring(suffixOffset);
        return methodName.replace(suffix, "");
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        return true;
    }

    @NbBundle.Messages("ImplementInterfaceHintDescription=Implement all abstract methods")
    @Override
    public String getDisplayName() {
        return Bundle.ImplementInterfaceHintDescription();
    }

    @Override
    public boolean showInTasklist() {
        return false;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }
    
    /**
     * Fix representing possibility to add empty method stubs directly to the source
     * code.
     */
    private static class AddMethodStubsFix implements PreviewableFix {

        private final GroovyError error;
        private final List<String> missingMethods;

        
        public AddMethodStubsFix(GroovyError error) {
            this.error = error;
            this.missingMethods = new ArrayList<>();
        }
        
        public void addMethodSignature(String methodSignature) {
            missingMethods.add(methodSignature);
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
                for (int i = 0; i < missingMethods.size(); i++) {
                    StringBuilder sb = new StringBuilder();

                    // Add one additional new line before the first method stub
                    if (i == 0) {
                        sb.append("\n"); // NOI18N
                    }
                    sb.append("public " ); // NOI18N
                    sb.append(missingMethods.get(i));
                    sb.append(" {\n"); // NOI18N

                    for (int space = 0; space < IndentUtils.indentLevelSize(baseDoc); space++) {
                        sb.append(" "); // NOI18N
                    }
                    sb.append("throw new UnsupportedOperationException(\"Not supported yet.\");\n"); // NOI18N
                    sb.append("}"); // NOI18N

                    // In the last inserted stub, add only one additional new line
                    if (i == missingMethods.size() - 1) {
                        sb.append("\n"); // NOI18N
                    } else {
                        sb.append("\n\n"); // NOI18N
                    }

                    edits.replace(getInsertPosition(baseDoc), 0, sb.toString(), true, 0);
                }
            }
            return edits;
        }

        private int getInsertPosition(BaseDocument doc) {
            TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, 1);

            while (ts.moveNext()) {
                Token t = ts.token();

                if (t.id() == GroovyTokenId.LITERAL_class) {
                    if (isCorrectClassDefinition(ts)) {
                        return findClosingCurlyOffset(ts);
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
        
        public int findClosingCurlyOffset(TokenSequence<GroovyTokenId> ts) {
            int balance = 0;

            while (ts.moveNext()) {
                Token<GroovyTokenId> token = ts.token();
                TokenId id = token.id();

                if (id == GroovyTokenId.LBRACE) {
                    balance++;
                } else if (id == GroovyTokenId.RBRACE) {
                    balance--;
                    
                    if (balance == 0) {
                        return ts.offset();
                    }
                }
            }
            return 0;
        }

        @NonNull
        private String getClassName() {
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
        
        @NbBundle.Messages("AddMissingMethodsStub=Implement all abstract methods")
        @Override
        public String getDescription() {
            return Bundle.AddMissingMethodsStub();
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
