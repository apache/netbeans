/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
            // if property type-hints are added in future, FiledElement should be improved
            // e.g. check whether a field is an annotation, then add the field for it
            Collection<? extends String> defaultTypeNames = declaredField.getDefaultTypeNames();
            if (defaultTypeNames.isEmpty()) {
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
