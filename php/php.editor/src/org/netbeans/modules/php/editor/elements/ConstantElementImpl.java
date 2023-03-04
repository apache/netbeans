/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
