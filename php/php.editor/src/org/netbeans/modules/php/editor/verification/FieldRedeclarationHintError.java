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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class FieldRedeclarationHintError extends HintErrorRule {

    @Override
    @NbBundle.Messages("FieldRedeclarationHintErrorDisplayName=Field Redeclaration")
    public String getDisplayName() {
        return Bundle.FieldRedeclarationHintErrorDisplayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileScope fileScope = context.fileScope;
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileScope != null && fileObject != null) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkTypeScopes(ModelUtils.getDeclaredClasses(fileScope), hints, fileObject);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkTypeScopes(ModelUtils.getDeclaredTraits(fileScope), hints, fileObject);
        }
    }

    @NbBundle.Messages({
        "# {0} - Field name",
        "FieldRedeclarationCustom=Field \"{0}\" has already been declared"
    })
    private void checkTypeScopes(Collection<? extends TypeScope> typeScopes, final List<Hint> hints, FileObject fileObject) {
        for (TypeScope typeScope : typeScopes) {
            for (FieldElement field : getRedeclaredFields(typeScope)) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                hints.add(new Hint(this, Bundle.FieldRedeclarationCustom(field.getName()), fileObject, field.getNameRange(), null, 500));
            }
        }
    }

    private Set<FieldElement> getRedeclaredFields(TypeScope typeScope) {
        Collection<? extends FieldElement> declaredFields;
        if (typeScope instanceof ClassScope) {
            declaredFields = ((ClassScope) typeScope).getDeclaredFields();
        } else if (typeScope instanceof TraitScope) {
            declaredFields = ((TraitScope) typeScope).getDeclaredFields();
        } else {
            return Collections.emptySet();
        }

        // mark as error other than the first declared filed
        Set<FieldElement> redeclaredFields = new HashSet<>();
        Map<String, FieldElement> firstDeclaredFields = new HashMap<>();
        for (FieldElement declaredField : declaredFields) {
            if (CancelSupport.getDefault().isCancelled()) {
                return Collections.emptySet();
            }
            // some declaredFields may be annotations e.g. @property int $count Description
            // So, check whether a field is an annotation
            if (!declaredField.isAnnotation()) {
                String fieldName = declaredField.getName();
                FieldElement firstDeclaredField = firstDeclaredFields.get(fieldName);
                if (firstDeclaredField == null) {
                    firstDeclaredFields.put(fieldName, declaredField);
                } else if (firstDeclaredField.getOffset() > declaredField.getOffset()) {
                    FieldElement oldField = firstDeclaredFields.replace(fieldName, declaredField);
                    redeclaredFields.add(oldField);
                } else {
                    redeclaredFields.add(declaredField);
                }
            }
        }
        return redeclaredFields;
    }

}
