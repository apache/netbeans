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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.model.ClassMemberElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class ConstantRedeclarationHintError extends HintErrorRule {

    @Override
    @NbBundle.Messages("ConstantRedeclarationHintErrorDisplayName=Constant Redeclaration")
    public String getDisplayName() {
        return Bundle.ConstantRedeclarationHintErrorDisplayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        FileScope fileScope = context.fileScope;
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileScope != null && fileObject != null) {
            checkTypeScopes(ModelUtils.getDeclaredClasses(fileScope), hints, fileObject);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkTypeScopes(ModelUtils.getDeclaredInterfaces(fileScope), hints, fileObject);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkTypeScopes(ModelUtils.getDeclaredTraits(fileScope), hints, fileObject);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkTypeScopes(ModelUtils.getDeclaredEnums(fileScope), hints, fileObject);
        }
    }

    @NbBundle.Messages({
        "# {0} - Constant name",
        "ConstantRedeclarationCustom=Constant \"{0}\" has already been declared"
    })
    private void checkTypeScopes(Collection<? extends TypeScope> typeScopes, final List<Hint> hints, FileObject fileObject) {
        for (TypeScope typeScope : typeScopes) {
            for (ClassMemberElement constant : getRedeclaredConstants(typeScope)) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                hints.add(new Hint(this, Bundle.ConstantRedeclarationCustom(constant.getName()), fileObject, constant.getNameRange(), null, 500));
            }
        }
    }

    private Set<ClassMemberElement> getRedeclaredConstants(TypeScope typeScope) {
        List<ClassMemberElement> declaredConstants = new ArrayList<>();
        if (canDeclareConstants(typeScope)) {
            declaredConstants.addAll(typeScope.getDeclaredConstants());
        } else if (typeScope instanceof EnumScope) {
            declaredConstants.addAll(((EnumScope) typeScope).getDeclaredEnumCases());
            declaredConstants.addAll(((EnumScope) typeScope).getDeclaredConstants());
        } else {
            return Collections.emptySet();
        }

        // mark constants other than the first declared one as errors
        Set<ClassMemberElement> redeclaredConstants = new HashSet<>();
        Map<String, ClassMemberElement> firstDeclaredConstants = new HashMap<>();
        for (ClassMemberElement declaredConstant : declaredConstants) {
            String constantName = declaredConstant.getName();
            ClassMemberElement firstDeclaredConstant = firstDeclaredConstants.get(constantName);
            if (firstDeclaredConstant == null) {
                firstDeclaredConstants.put(constantName, declaredConstant);
            } else if (firstDeclaredConstant.getOffset() > declaredConstant.getOffset()) {
                ClassMemberElement oldConstant = firstDeclaredConstants.replace(constantName, declaredConstant);
                redeclaredConstants.add(oldConstant);
            } else {
                redeclaredConstants.add(declaredConstant);
            }
        }
        return redeclaredConstants;
    }

    private boolean canDeclareConstants(TypeScope typeScope) {
        return typeScope instanceof ClassScope
                || typeScope instanceof InterfaceScope
                || typeScope instanceof TraitScope;
    }
}
