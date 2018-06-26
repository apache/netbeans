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
package org.netbeans.modules.php.editor.elements;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ConstantElement;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ConstantDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public final class ConstantElementImpl extends FullyQualifiedElementImpl implements ConstantElement {

    public static final String IDX_FIELD = PHPIndexer.FIELD_CONST;

    private final String value;
    private ConstantElementImpl(
            final QualifiedName qualifiedName,
            final String value,
            final int offset,
            final String fileUrl,
            final ElementQuery elementQuery,
            final boolean isDeprecated) {
        super(qualifiedName.toName().toString(), qualifiedName.toNamespaceName().toString(), fileUrl, offset, elementQuery, isDeprecated);
        this.value = value;
    }

    public static Set<ConstantElement> fromSignature(final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        return fromSignature(NameKind.empty(), indexQuery, indexResult);
    }

    public static Set<ConstantElement> fromSignature(
            final NameKind query, final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        final String[] values = indexResult.getValues(IDX_FIELD);
        final Set<ConstantElement> retval = values.length > 0
                ? new HashSet<ConstantElement>() : Collections.<ConstantElement>emptySet();
        for (String val : values) {
            ConstantElement constant = fromSignature(query, indexQuery, Signature.get(val));
            if (constant != null) {
                retval.add(constant);
            }
        }
        return retval;
    }

    private static ConstantElement fromSignature(final NameKind query,
            final IndexQueryImpl indexScopeQuery, final Signature sig) {
        ConstantSignatureParser signParser = new ConstantSignatureParser(sig);
        ConstantElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new ConstantElementImpl(
                    signParser.getQualifiedName(),
                    signParser.getValue(),
                    signParser.getOffset(),
                    signParser.getFileUrl(),
                    indexScopeQuery,
                    signParser.isDeprecated());
        }
        return retval;
    }

    public static Set<ConstantElement> fromNode(final NamespaceElement namespace, final ConstantDeclaration node, final ElementQuery.File fileQuery) {
        Parameters.notNull("node", node);
        Parameters.notNull("fileQuery", fileQuery);
        final List<? extends ConstantDeclarationInfo> constants = ConstantDeclarationInfo.create(node);
        final Set<ConstantElement> retval = new HashSet<>();
        for (ConstantDeclarationInfo info : constants) {
            final QualifiedName fullyQualifiedName = namespace != null
                    ? namespace.getFullyQualifiedName() : QualifiedName.createForDefaultNamespaceName();
            retval.add(new ConstantElementImpl(
                    fullyQualifiedName.append(info.getName()),
                    info.getValue(), info.getRange().getStart(), fileQuery.getURL().toExternalForm(), fileQuery,
                    VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node)));
        }
        return retval;
    }

    private static boolean matchesQuery(final NameKind query, ConstantSignatureParser signParser) {
        Parameters.notNull("query", query);
        return (query instanceof NameKind.Empty)
                || query.matchesName(ConstantElement.KIND, signParser.getQualifiedName());
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return ConstantElement.KIND;
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Separator.SEMICOLON); //NOI18N
        sb.append(getName()).append(Separator.SEMICOLON); //NOI18N
        sb.append(getOffset()).append(Separator.SEMICOLON); //NOI18N
        QualifiedName namespaceName = getNamespaceName();
        sb.append(namespaceName.toString()).append(Separator.SEMICOLON); //NOI18N
        sb.append(getValue()).append(Separator.SEMICOLON); //NOI18N
        sb.append(isDeprecated() ? 1 : 0).append(Separator.SEMICOLON);
        sb.append(getFilenameUrl()).append(Separator.SEMICOLON);
        checkConstantSignature(sb);
        return sb.toString();
    }

    private void checkConstantSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            ConstantSignatureParser parser = new ConstantSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getQualifiedName().toName().toString());
            assert getNamespaceName().equals(parser.getQualifiedName().toNamespaceName());
            assert getOffset() == parser.getOffset();
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    private static class ConstantSignatureParser {

        private final Signature signature;

        ConstantSignatureParser(Signature signature) {
            this.signature = signature;
        }

        QualifiedName getQualifiedName() {
            return composeQualifiedName(signature.string(3), signature.string(1));
        }

        int getOffset() {
            return signature.integer(2);
        }

        String getValue() {
            return signature.string(4);
        }

        boolean isDeprecated() {
            return signature.integer(5) == 1;
        }

        String getFileUrl() {
            return signature.string(6);
        }
    }
}
