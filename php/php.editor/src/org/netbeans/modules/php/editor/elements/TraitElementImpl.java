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
package org.netbeans.modules.php.editor.elements;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.api.elements.TraitElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.TraitDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.openide.util.Parameters;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class TraitElementImpl extends TypeElementImpl implements TraitElement {
    public static final String IDX_FIELD = PHPIndexer.FIELD_TRAIT;
    private final Collection<QualifiedName> usedTraits;

    private TraitElementImpl(
            final QualifiedName qualifiedName,
            final int offset,
            final Collection<QualifiedName> usedTraits,
            final String fileUrl,
            final ElementQuery elementQuery,
            final boolean isDeprecated) {
        super(
                qualifiedName,
                offset,
                Collections.<QualifiedName>emptySet(),
                Collections.<QualifiedName>emptySet(),
                PhpModifiers.NO_FLAGS,
                fileUrl,
                elementQuery,
                isDeprecated);
        this.usedTraits = usedTraits;
    }

    public static Set<TraitElement> fromSignature(final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        return fromSignature(NameKind.empty(), indexScopeQuery, indexResult);
    }

    private static TraitElement fromSignature(NameKind query, IndexQueryImpl indexScopeQuery, Signature signature) {
        Parameters.notNull("query", query); //NOI18N
        TraitSignatureParser signParser = new TraitSignatureParser(signature);
        TraitElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new TraitElementImpl(
                    signParser.getQualifiedName(),
                    signParser.getOffset(),
                    signParser.getUsedTraits(),
                    signParser.getFileUrl(),
                    indexScopeQuery,
                    signParser.isDeprecated());
        }
        return retval;
    }

    public static Set<TraitElement> fromSignature(final NameKind query, final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        String[] values = indexResult.getValues(IDX_FIELD);
        Set<TraitElement> retval = values.length > 0 ? new HashSet<>() : Collections.<TraitElement>emptySet();
        for (String val : values) {
            final TraitElement trait = fromSignature(query, indexScopeQuery, Signature.get(val));
            if (trait != null) {
                retval.add(trait);
            }
        }
        return retval;
    }

    public static TraitElement fromNode(final NamespaceElement namespace, final TraitDeclaration node, final ElementQuery.File fileQuery) {
        Parameters.notNull("node", node);
        Parameters.notNull("fileQuery", fileQuery);
        TraitDeclarationInfo info = TraitDeclarationInfo.create(node);
        final QualifiedName fullyQualifiedName = namespace != null
                ? namespace.getFullyQualifiedName()
                : QualifiedName.createForDefaultNamespaceName();
        return new TraitElementImpl(
                fullyQualifiedName.append(info.getName()), info.getRange().getStart(),
                info.getUsedTraits(), fileQuery.getURL().toExternalForm(), fileQuery,
                VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node));
    }

    private static boolean matchesQuery(final NameKind query, TraitSignatureParser signParser) {
        Parameters.notNull("query", query); //NOI18N
        return (query instanceof NameKind.Empty)
                || query.matchesName(TraitElement.KIND, signParser.getQualifiedName());
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Separator.SEMICOLON);
        sb.append(getName()).append(Separator.SEMICOLON);
        sb.append(getOffset()).append(Separator.SEMICOLON);
        sb.append(getNamespaceName()).append(Separator.SEMICOLON);
        if (!usedTraits.isEmpty()) {
            StringBuilder traitSb = new StringBuilder();
            for (QualifiedName usedTrait : usedTraits) {
                if (traitSb.length() > 0) {
                    traitSb.append(Separator.COMMA);
                }
                traitSb.append(usedTrait.toString());
            }
            sb.append(traitSb);
        }
        sb.append(Separator.SEMICOLON);
        sb.append(isDeprecated() ? 1 : 0).append(Separator.SEMICOLON);
        sb.append(getFilenameUrl()).append(Separator.SEMICOLON);
        return sb.toString();
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return KIND;
    }

    @Override
    public String asString(PrintAs as) {
        String str = ""; // NOI18N
        switch (as) {
            case NameAndSuperTypes:
                str = getName();
                break;
            case SuperTypes:
                // noop
                break;
            default:
                assert false : as;
        }
        return str;
    }

    @Override
    public Collection<QualifiedName> getUsedTraits() {
        return Collections.unmodifiableCollection(usedTraits);
    }

    private static class TraitSignatureParser {

        private final Signature signature;

        TraitSignatureParser(Signature signature) {
            this.signature = signature;
        }

        QualifiedName getQualifiedName() {
            return composeQualifiedName(signature.string(3), signature.string(1));
        }

        int getOffset() {
            return signature.integer(2);
        }

        private Collection<QualifiedName> getUsedTraits() {
            Collection<QualifiedName> retval = new HashSet<>();
            String traits = signature.string(4);
            final String[] traitNames = CodeUtils.COMMA_PATTERN.split(traits);
            for (String trait : traitNames) {
                if (!trait.isEmpty()) {
                    // GH-6634
                    // avoid getting traits from the index with an empty string
                    retval.add(QualifiedName.create(trait));
                }
            }
            return retval;
        }

        boolean isDeprecated() {
            return signature.integer(5) == 1;
        }

        String getFileUrl() {
            return signature.string(6);
        }
    }

}
