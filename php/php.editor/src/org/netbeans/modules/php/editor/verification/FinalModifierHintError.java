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
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Handle errors for the final modifier.
 */
public class FinalModifierHintError extends HintErrorRule {

    private FileObject fileObject;
    private Set<TypeConstantElement> inheritedClassConstants = Collections.emptySet();
    private String classSignatureForInheritedClassConstants = ""; // NOI18N

    @Override
    @NbBundle.Messages("FinalModifierHintError.displayName=Final Modifier Errors")
    public String getDisplayName() {
        return Bundle.FinalModifierHintError_displayName();
    }

    @Override
    @NbBundle.Messages({
        "# {0} - constant name",
        "FinalModifierHintError.overridingFinalConstant.desc={0} can''t override final constant.",
    })
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileScope fileScope = context.fileScope;
        fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileScope != null && fileObject != null) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkOverridingFinalConstants(fileScope, phpParseResult, hints);
        }
    }

    private void checkOverridingFinalConstants(FileScope fileScope, PHPParseResult phpParseResult, List<Hint> hints) {
        Collection<? extends ClassScope> allClasses = ModelUtils.getDeclaredClasses(fileScope);
        List<ClassConstantElement> finalConstants = new ArrayList<>();
        for (ClassScope classScope : allClasses) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Collection<? extends ClassConstantElement> declaredConstants = classScope.getDeclaredConstants();
            for (ClassConstantElement constant : declaredConstants) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                final ElementFilter constantNameFilter = ElementFilter.forName(NameKind.exact(constant.getName()));
                Set<TypeConstantElement> overridenConstants = constantNameFilter.filter(getInheritedClassConstants(phpParseResult, constant));
                for (TypeConstantElement overridenConstant : overridenConstants) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    if (overridenConstant.getPhpModifiers().isFinal()) {
                        finalConstants.add(constant);
                    }
                }
            }
        }
        for (ClassConstantElement finalConstant : finalConstants) {
            addHint(finalConstant.getNameRange(), Bundle.FinalModifierHintError_overridingFinalConstant_desc(finalConstant.getName()), hints, Collections.emptyList());
        }
    }

    private Set<TypeConstantElement> getInheritedClassConstants(final ParserResult info, final ClassConstantElement constant) {
        Scope inScope = constant.getInScope();
        assert inScope instanceof TypeScope;
        TypeScope typeScope = (TypeScope) inScope;
        final String signature = typeScope.getIndexSignature();
        if (signature != null && !signature.equals(classSignatureForInheritedClassConstants)) {
            ElementQuery.Index index = ElementQueryFactory.getIndexQuery(info);
            inheritedClassConstants = index.getInheritedTypeConstants(typeScope);
        }
        classSignatureForInheritedClassConstants = signature;
        return Collections.unmodifiableSet(inheritedClassConstants);
    }

    private void addHint(OffsetRange offsetRange, String description, List<Hint> hints, List<HintFix> fixes) {
        hints.add(new Hint(
                this,
                description,
                fileObject,
                offsetRange,
                fixes,
                500
        ));
    }
}
