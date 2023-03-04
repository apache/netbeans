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

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public class NamespaceElementImpl extends FullyQualifiedElementImpl implements NamespaceElement {
    public static final String IDX_FIELD = PHPIndexer.FIELD_NAMESPACE;

    NamespaceElementImpl(
            final QualifiedName qualifiedName,
            final int offset,
            final String fileUrl,
            final ElementQuery elementQuery,
            final boolean isDeprecated) {
        super(qualifiedName.toName().toString(), qualifiedName.toNamespaceName().toString(), fileUrl, offset, elementQuery, isDeprecated);
    }

    public static Set<NamespaceElement> fromSignature(final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        return fromSignature(NameKind.empty(), indexQuery, indexResult);
    }

    public static Set<NamespaceElement> fromSignature(final NameKind query, final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        final String[] values = indexResult.getValues(IDX_FIELD);
        final Set<NamespaceElement> retval = new HashSet<>();
        for (final String val : values) {
            final NamespaceElement namespace = fromSignature(query, indexQuery, Signature.get(val));
            if (namespace != null) {
                retval.add(namespace);
            }
        }
        return retval;
    }

    public static NamespaceElement fromSignature(final NameKind query, IndexQueryImpl indexScopeQuery, Signature sig) {
        final NamespaceSignatureParser signParser = new NamespaceSignatureParser(sig);
        NamespaceElement retval = null;
        if (matchesQuery(query, signParser)) {
                retval = new NamespaceElementImpl(signParser.getQualifiedName(),
                0, signParser.getFileUrl(),
                indexScopeQuery, signParser.isDeprecated());
        }
        return retval;
    }

    public static NamespaceElement fromNode(final NamespaceDeclaration node, final ElementQuery.File fileQuery) {
        Parameters.notNull("node", node);
        Parameters.notNull("fileQuery", fileQuery);
        NamespaceDeclarationInfo info = NamespaceDeclarationInfo.create(node);
        return new NamespaceElementImpl(
                info.getQualifiedName(), info.getRange().getStart(),
                fileQuery.getURL().toExternalForm(), fileQuery,
                VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node));
    }

    private static boolean matchesQuery(final NameKind query, NamespaceSignatureParser signParser) {
        Parameters.notNull("NameKind query: can't be null", query);
        final QualifiedName qualifiedName = signParser.getQualifiedName();
        return (query instanceof NameKind.Empty)
                || (!qualifiedName.isDefaultNamespace() && query.matchesName(NamespaceElement.KIND, qualifiedName));
    }

    @Override
    public String getSignature() {
        final StringBuilder sb = new StringBuilder();
        final QualifiedName qualifiedName = getFullyQualifiedName();
        final String name = qualifiedName.toName().toString();
        final String namespaceName = qualifiedName.toNamespaceName().toString();
        sb.append(name.toLowerCase()).append(Separator.SEMICOLON);
        sb.append(name).append(Separator.SEMICOLON);
        sb.append(namespaceName).append(Separator.SEMICOLON);
        sb.append(isDeprecated() ? 1 : 0).append(Separator.SEMICOLON);
        sb.append(getFilenameUrl()).append(Separator.SEMICOLON);
        checkSignature(sb);
        return sb.toString();
    }

    @Override
    public final PhpElementKind getPhpElementKind() {
        return NamespaceElement.KIND;
    }

    private void checkSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            NamespaceSignatureParser parser = new NamespaceSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getQualifiedName().toName().toString());
            assert getNamespaceName().equals(parser.getQualifiedName().toNamespaceName());
        }
    }

    private static class NamespaceSignatureParser {

        private final Signature signature;

        NamespaceSignatureParser(Signature signature) {
            this.signature = signature;
        }

        QualifiedName getQualifiedName() {
            return composeQualifiedName(signature.string(2), signature.string(1));
        }

        boolean isDeprecated() {
            return signature.integer(3) == 1;
        }

        String getFileUrl() {
            return signature.string(4);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NamespaceElementImpl other = (NamespaceElementImpl) obj;
        if (!this.getName().equals(other.getName())) {
            return false;
        }
        String thisNamespaceName = this.getNamespaceName().toString();
        String otherNamespaceName = other.getNamespaceName().toString();
        if (!thisNamespaceName.equals(otherNamespaceName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        String thisNamespaceName = this.getNamespaceName().toString();
        hash = 71 * hash + this.getName().hashCode();
        hash = 71 * hash + thisNamespaceName.hashCode();
        return hash;
    }



}
