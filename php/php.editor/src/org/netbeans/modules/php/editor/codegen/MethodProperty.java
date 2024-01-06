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
package org.netbeans.modules.php.editor.codegen;

import java.util.Collection;
import java.util.Comparator;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TreeElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.elements.TypeNameResolverImpl;

public final class MethodProperty extends Property {

    private final MethodElement method;
    private final TreeElement<TypeElement> enclosingType;

    public MethodProperty(MethodElement method, TreeElement<TypeElement> enclosingType, PhpVersion phpVersion) {
        super(formatName(method, phpVersion), method.getPhpModifiers().toFlags());

        this.method = method;
        this.enclosingType = enclosingType;

        boolean typeIsAbstract = enclosingType.getElement().getPhpModifiers().isAbstract();
        final boolean methodIsAbstract = method.isAbstract() || method.getType().isInterface();
        setSelected(!typeIsAbstract && methodIsAbstract);
    }

    public static Comparator<MethodProperty> getComparator() {
        return (MethodProperty o1, MethodProperty o2) -> {
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
        };
    }

    private static String formatName(final MethodElement method, PhpVersion phpVersion) {
        Collection<TypeResolver> returnTypes = method.getReturnTypes();
        final String nameAndParams = method.asString(PrintAs.NameAndParamsDeclaration, TypeNameResolverImpl.forNull(), phpVersion);
        final String returnTypeString = method.asString(PrintAs.ReturnTypes, TypeNameResolverImpl.forNull(), phpVersion);
        final String[] split = nameAndParams.split("\\(");
        if (returnTypes.isEmpty() || returnTypeString.isEmpty()) {
            return String.format("<html><b>%s</b>(%s</html>", split[0], split[1]); // NOI18N
        }
        return String.format("<html><b>%s</b>(%s : %s</html>", split[0], split[1], returnTypeString); // NOI18N
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
