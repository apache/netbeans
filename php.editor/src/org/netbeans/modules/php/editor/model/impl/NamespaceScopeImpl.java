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
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.VariableElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.GroupUseScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.ConstantDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.FunctionDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.GroupUseStatementPartInfo;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleUseStatementPartInfo;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

/**
 *
 * @author Radek Matous
 */
final class NamespaceScopeImpl extends ScopeImpl implements NamespaceScope, VariableNameFactory {

    private final boolean isDefault;

    @Override
    public VariableNameImpl createElement(Variable variable) {
        VariableNameImpl retval = new VariableNameImpl(this, variable, true);
        return retval;
    }

    public VariableNameImpl createElement(VariableElement variable) {
        VariableNameImpl retval = new VariableNameImpl(this, variable);
        return retval;
    }

    ScalarConstantElementImpl createConstantElement(final ASTNodeInfo<Scalar> node, final String value) {
        return new ScalarConstantElementImpl(this, node, value);
    }
    ConstantElementImpl createElement(ConstantDeclarationInfo node) {
        ConstantElementImpl retval = new ConstantElementImpl(this, node);
        return retval;
    }
    UseScopeImpl createUseStatementPart(SingleUseStatementPartInfo node) {
        UseScopeImpl retval = new UseScopeImpl(this, node);
        return retval;
    }

    GroupUseScopeImpl createUseStatementPart(GroupUseStatementPartInfo useStatementPartInfo) {
        return new GroupUseScopeImpl(this, useStatementPartInfo);
    }

    FunctionScopeImpl createElement(Program program, FunctionDeclaration node) {
        FunctionScopeImpl retval = new FunctionScopeImpl(this, FunctionDeclarationInfo.create(program, node),
                VariousUtils.getReturnType(program, node), VariousUtils.isDeprecatedFromPHPDoc(program, node));
        return retval;
    }

    NamespaceScopeImpl(FileScopeImpl inScope, NamespaceDeclarationInfo info) {
        super(inScope, info, PhpModifiers.fromBitMask(PhpModifiers.PUBLIC), info.getOriginalNode().getBody(), inScope.isDeprecated());
        isDefault = false;
    }

    NamespaceScopeImpl(FileScopeImpl inScope) {
        super(
                inScope,
                NamespaceDeclarationInfo.DEFAULT_NAMESPACE_NAME,
                inScope.getFile(),
                inScope.getNameRange(),
                PhpElementKind.NAMESPACE_DECLARATION,
                inScope.isDeprecated());
        isDefault = true;
    }

    @Override
    void addElement(ModelElementImpl element) {
        super.addElement(element);
    }

    @Override
    public Collection<? extends ClassScopeImpl> getDeclaredClasses() {
        return filter(getElements(), new ElementFilter<ClassScopeImpl>() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.CLASS);
            }
        });
    }

    @Override
    public Collection<? extends InterfaceScope> getDeclaredInterfaces() {
        return filter(getElements(), new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.IFACE);
            }
        });
    }

    @Override
    public Collection<? extends TraitScope> getDeclaredTraits() {
        return filter(getElements(), new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.TRAIT);
            }
        });
    }

    @Override
    public Collection<? extends ConstantElement> getDeclaredConstants() {
        return filter(getElements(), new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.CONSTANT);
            }
        });
    }

    @Override
    public Collection<? extends FunctionScope> getDeclaredFunctions() {
        return filter(getElements(), new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.FUNCTION);
            }
        });
    }

    @Override
    public Collection<? extends UseScope> getAllDeclaredSingleUses() {
        return getDeclaredSingleUses(true);
    }

    @Override
    public Collection<? extends UseScope> getDeclaredSingleUses() {
        return getDeclaredSingleUses(false);
    }

    private Collection<? extends UseScope> getDeclaredSingleUses(final boolean includingGroupUses) {
        return filter(getElements(), new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                if (!element.getPhpElementKind().equals(PhpElementKind.USE_STATEMENT)) {
                    return false;
                }
                if (includingGroupUses) {
                    return true;
                }
                UseScope useScope = (UseScope) element;
                return !useScope.isPartOfGroupUse();
            }
        });
    }

    @Override
    public Collection<? extends GroupUseScope> getDeclaredGroupUses() {
        return filter(getElements(), new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.GROUP_USE_STATEMENT);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<? extends TypeScope> getDeclaredTypes() {
        Collection<? extends ClassScope> classes = getDeclaredClasses();
        Collection<? extends InterfaceScope> interfaces = getDeclaredInterfaces();
        Collection<? extends TraitScope> traits = getDeclaredTraits();
        return ModelUtils.merge(classes, interfaces, traits);
    }


    @Override
    public Collection<? extends VariableName> getDeclaredVariables() {
        return filter(getElements(), new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.VARIABLE);
            }
        });
    }

    @Override
    public boolean isDefaultNamespace() {
        return this.isDefault;
    }

    @Override
    public FileScopeImpl getFileScope() {
        return (FileScopeImpl) getInScope();
    }

    @Override
    public QualifiedName getQualifiedName() {
        QualifiedName qualifiedName = QualifiedName.create(this);
        return qualifiedName;
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
        Collection<? extends VariableName> declaredVariables = getDeclaredVariables();
        for (VariableName variableName : declaredVariables) {
            variableName.addSelfToIndex(indexDocument);
        }
        if (!isDefaultNamespace()) {
            indexDocument.addPair(PHPIndexer.FIELD_NAMESPACE, getIndexSignature(), true, true);
            indexDocument.addPair(PHPIndexer.FIELD_TOP_LEVEL, getName().toLowerCase(), true, true);
        }
    }

    private String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        QualifiedName qualifiedName = getQualifiedName();
        String name = qualifiedName.toName().toString();
        String namespaceName = qualifiedName.toNamespaceName().toString();
        sb.append(name.toLowerCase()).append(Signature.ITEM_DELIMITER);
        sb.append(name).append(Signature.ITEM_DELIMITER);
        sb.append(namespaceName).append(Signature.ITEM_DELIMITER);
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER);
        return sb.toString();
    }

}
