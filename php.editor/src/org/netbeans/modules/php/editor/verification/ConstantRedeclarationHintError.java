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
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
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
        }
    }

    @NbBundle.Messages({
        "# {0} - Constant name",
        "ConstantRedeclarationCustom=Constant \"{0}\" has already been declared"
    })
    private void checkTypeScopes(Collection<? extends TypeScope> typeScopes, final List<Hint> hints, FileObject fileObject) {
        for (TypeScope typeScope : typeScopes) {
            for (ClassConstantElement constant : getRedeclaredConstants(typeScope)) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                hints.add(new Hint(this, Bundle.ConstantRedeclarationCustom(constant.getName()), fileObject, constant.getNameRange(), null, 500));
            }
        }
    }

    private Set<ClassConstantElement> getRedeclaredConstants(TypeScope typeScope) {
        Collection<? extends ClassConstantElement> declaredConstants;
        if (typeScope instanceof ClassScope || typeScope instanceof InterfaceScope) {
            declaredConstants = typeScope.getDeclaredConstants();
        } else {
            return Collections.emptySet();
        }

        // mark as error other than the first declared constant
        Set<ClassConstantElement> redeclaredConstants = new HashSet<>();
        Map<String, ClassConstantElement> firstDeclaredConstants = new HashMap<>();
        for (ClassConstantElement declaredConstant : declaredConstants) {
            String constantName = declaredConstant.getName();
            ClassConstantElement firstDeclaredConstant = firstDeclaredConstants.get(constantName);
            if (firstDeclaredConstant == null) {
                firstDeclaredConstants.put(constantName, declaredConstant);
            } else if (firstDeclaredConstant.getOffset() > declaredConstant.getOffset()) {
                ClassConstantElement oldConstant = firstDeclaredConstants.replace(constantName, declaredConstant);
                redeclaredConstants.add(oldConstant);
            } else {
                redeclaredConstants.add(declaredConstant);
            }
        }
        return redeclaredConstants;
    }

}
