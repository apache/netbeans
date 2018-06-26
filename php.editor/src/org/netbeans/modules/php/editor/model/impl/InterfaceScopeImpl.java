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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;

/**
 *
 * @author Radek Matous
 */
class InterfaceScopeImpl extends TypeScopeImpl implements InterfaceScope {
    InterfaceScopeImpl(Scope inScope, InterfaceDeclarationInfo nodeInfo, boolean isDeprecated) {
        super(inScope, nodeInfo, isDeprecated);
    }

    InterfaceScopeImpl(IndexScope inScope, InterfaceElement indexedIface) {
        super(inScope, indexedIface);
    }

    @Override
    public String asString(PrintAs as) {
        StringBuilder retval = new StringBuilder();
        switch (as) {
            case NameAndSuperTypes:
                retval.append(getName());
                printAsSuperTypes(retval);
                break;
            case SuperTypes:
                printAsSuperTypes(retval);
                break;
            default:
                assert false : as;
        }
        return retval.toString();
    }

    private void printAsSuperTypes(StringBuilder sb) {
        Set<QualifiedName> superIfaces = getSuperInterfaces();
        if (!superIfaces.isEmpty()) {
            sb.append(" extends "); //NOI18N
        }
        StringBuilder ifacesBuffer = new StringBuilder();
        for (QualifiedName qualifiedName : superIfaces) {
            if (ifacesBuffer.length() > 0) {
                ifacesBuffer.append(", "); //NOI18N
            }
            ifacesBuffer.append(qualifiedName.getName());
        }
        sb.append(ifacesBuffer);
    }

    @Override
    public Collection<? extends MethodScope> getInheritedMethods() {
        Set<MethodScope> allMethods = new HashSet<>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        Set<InterfaceScope> interfaceScopes = new HashSet<>();
        interfaceScopes.addAll(getSuperInterfaceScopes());
        for (InterfaceScope iface : interfaceScopes) {
            Set<MethodElement> indexedFunctions =
                    org.netbeans.modules.php.editor.api.elements.ElementFilter.forPrivateModifiers(false).filter(index.getAllMethods(iface));
            for (MethodElement classMember : indexedFunctions) {
                MethodElement indexedFunction = classMember;
                TypeElement type = indexedFunction.getType();
                if (type.isInterface()) {
                    allMethods.add(new MethodScopeImpl(new InterfaceScopeImpl(indexScope, (InterfaceElement) type), indexedFunction));
                } else {
                    allMethods.add(new MethodScopeImpl(new ClassScopeImpl(indexScope, (ClassElement) type), indexedFunction));
                }
            }
        }
        return allMethods;
    }

    @Override
    public final Collection<? extends ClassConstantElement> getInheritedConstants() {
        Set<ClassConstantElement> allConstants = new HashSet<>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        Set<InterfaceScope> interfaceScopes = new HashSet<>();
        interfaceScopes.addAll(getSuperInterfaceScopes());
        for (InterfaceScope iface : interfaceScopes) {
            Collection<TypeConstantElement> indexedConstants = index.getInheritedTypeConstants(iface);
            for (TypeConstantElement classMember : indexedConstants) {
                TypeConstantElement indexedFunction = classMember;
                allConstants.add(new ClassConstantElementImpl(iface, indexedFunction));
            }
        }
        return allConstants;
    }

    @Override
    public final Collection<? extends MethodScope> getMethods() {
        Set<MethodScope> allMethods = new HashSet<>();
        allMethods.addAll(getDeclaredMethods());
        allMethods.addAll(getInheritedMethods());
        return allMethods;
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
        indexDocument.addPair(PHPIndexer.FIELD_IFACE, getIndexSignature(), true, true);
        Set<QualifiedName> superInterfaces = getSuperInterfaces();
        for (QualifiedName superIfaceName : superInterfaces) {
            final String name = superIfaceName.getName();
            final String namespaceName = superIfaceName.toNamespaceName().toString();
            indexDocument.addPair(PHPIndexer.FIELD_SUPER_IFACE, String.format("%s;%s;%s", name.toLowerCase(), name, namespaceName), true, true); //NOI18N
        }
        indexDocument.addPair(PHPIndexer.FIELD_TOP_LEVEL, getName().toLowerCase(), true, true);
        for (MethodScope methodScope : getDeclaredMethods()) {
            methodScope.addSelfToIndex(indexDocument);
        }
        for (ClassConstantElement constantElement : getDeclaredConstants()) {
            constantElement.addSelfToIndex(indexDocument);
        }
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Signature.ITEM_DELIMITER);
        sb.append(getName()).append(Signature.ITEM_DELIMITER);
        sb.append(getOffset()).append(Signature.ITEM_DELIMITER);
        List<? extends String> superInterfaces = getSuperInterfaceNames();
        for (int i = 0; i < superInterfaces.size(); i++) {
            String iface = superInterfaces.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append(iface);
        }
        if (!superInterfaces.isEmpty()) {
            sb.append("|"); //NOI18N
            StringBuilder fqIfaceSb = new StringBuilder();
            Collection<QualifiedName> fQSuperInterfaceNames = getFQSuperInterfaceNames();
            for (QualifiedName fQSuperInterfaceName : fQSuperInterfaceNames) {
                if (fqIfaceSb.length() > 0) {
                    fqIfaceSb.append(","); //NOI18N
                }
                fqIfaceSb.append(fQSuperInterfaceName.toString()); //NOI18N
            }
            sb.append(fqIfaceSb);
        }
        sb.append(Signature.ITEM_DELIMITER);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        assert namespaceScope != null;
        QualifiedName qualifiedName = namespaceScope.getQualifiedName();
        sb.append(qualifiedName.toString()).append(Signature.ITEM_DELIMITER);
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER);
        return sb.toString();
    }

    @Override
    public QualifiedName getNamespaceName() {
        if (indexedElement instanceof InterfaceElement) {
            InterfaceElement indexedInterface = (InterfaceElement) indexedElement;
            return indexedInterface.getNamespaceName();
        }
        return super.getNamespaceName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        List<? extends InterfaceScope> implementedInterfaces = getSuperInterfaceScopes();
        if (implementedInterfaces.size() > 0) {
            sb.append(" implements ");
            for (InterfaceScope interfaceScope : implementedInterfaces) {
                sb.append(interfaceScope.getName()).append(" ");
            }
        }
        return sb.toString();
    }

    @Override
    public Collection<? extends VariableName> getDeclaredVariables() {
        return Collections.emptyList();
    }

}
