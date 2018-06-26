/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.codegen;

import java.util.Collection;
import java.util.Comparator;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TreeElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;

public final class MethodProperty extends Property {

    private final MethodElement method;
    private final TreeElement<TypeElement> enclosingType;

    public MethodProperty(MethodElement method, TreeElement<TypeElement> enclosingType) {
        super(formatName(method), method.getPhpModifiers().toFlags());

        this.method = method;
        this.enclosingType = enclosingType;

        boolean typeIsAbstract = enclosingType.getElement().getPhpModifiers().isAbstract();
        final boolean methodIsAbstract = method.isAbstract() || method.getType().isInterface();
        setSelected(!typeIsAbstract && methodIsAbstract);
    }

    public static Comparator<MethodProperty> getComparator() {
        return new Comparator<MethodProperty>() {
            @Override
            public int compare(MethodProperty o1, MethodProperty o2) {
                int retval = Boolean.valueOf(o2.getMethod().isConstructor()).compareTo(o1.getMethod().isConstructor());
                if (retval == 0) {
                    retval = Boolean.valueOf(o2.isSelected()).compareTo(o1.isSelected());
                }
                if (retval == 0) {
                    retval = Boolean.valueOf(o1.getMethod().isMagic()).compareTo(o2.getMethod().isMagic());
                }
                if (retval == 0) {
                    retval = o1.getMethod().getType().getName().compareTo(o2.getMethod().getType().getName());
                }
                if (retval == 0) {
                    retval = o1.getMethod().getName().compareTo(o2.getMethod().getName());
                }
                return retval;
            }
        };
    }

    private static String formatName(final MethodElement method) {
        Collection<TypeResolver> returnTypes = method.getReturnTypes();
        final String nameAndParams = method.asString(PrintAs.NameAndParamsDeclaration);
        final String returntypes = method.asString(PrintAs.ReturnTypes);
        final String[] split = nameAndParams.split("\\(");
        if (returnTypes.isEmpty()) {
            return String.format("<html><b>%s</b>(%s</html>", split[0], split[1]); // NOI18N
        }
        return String.format("<html><b>%s</b>(%s : %s</html>", split[0], split[1], returntypes); // NOI18N
    }

    public MethodElement getMethod() {
        return method;
    }

    public TreeElement<TypeElement> getEnclosingType() {
        return enclosingType;
    }

    @Override
    public PhpElementKind getKind() {
        return PhpElementKind.METHOD;
    }
}
