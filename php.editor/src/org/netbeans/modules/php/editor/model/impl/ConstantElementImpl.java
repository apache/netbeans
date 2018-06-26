/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

class ConstantElementImpl extends ModelElementImpl implements ConstantElement, FullyQualifiedElement {
    private final String value;

    ConstantElementImpl(NamespaceScopeImpl inScope, ClassConstantDeclarationInfo node) {
        this(inScope, node.getName(), node.getValue(), inScope.getFile(), node.getRange(), inScope.isDeprecated());
    }

    ConstantElementImpl(IndexScopeImpl inScope, org.netbeans.modules.php.editor.api.elements.ConstantElement indexedConstant) {
        this(
                inScope,
                indexedConstant.getName(),
                indexedConstant.getValue(),
                Union2.<String/*url*/, FileObject>createFirst(indexedConstant.getFilenameUrl()),
                new OffsetRange(indexedConstant.getOffset(), indexedConstant.getOffset() + indexedConstant.getName().length()),
                indexedConstant.isDeprecated());
    }

    private ConstantElementImpl(ScopeImpl inScope, String name, String value, Union2<String, FileObject> file, OffsetRange offsetRange, boolean isDeprecated) {
        super(inScope, name, file, offsetRange, PhpElementKind.CONSTANT, isDeprecated);
        this.value = value;
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
        indexDocument.addPair(PHPIndexer.FIELD_CONST, getIndexSignature(), true, true);
        indexDocument.addPair(PHPIndexer.FIELD_TOP_LEVEL, getName().toLowerCase(), true, true);
    }

    private String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Signature.ITEM_DELIMITER);
        sb.append(getName()).append(Signature.ITEM_DELIMITER);
        sb.append(getOffset()).append(Signature.ITEM_DELIMITER);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        assert namespaceScope != null;
        QualifiedName qualifiedName = namespaceScope.getQualifiedName();
        sb.append(qualifiedName.toString()).append(Signature.ITEM_DELIMITER);
        sb.append(getValue() != null ? Signature.encodeItem(getValue()) : "?").append(Signature.ITEM_DELIMITER); //NOI18N
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER);
        return sb.toString();
    }

    @Override
    public String getValue() {
        return value;
    }
}
