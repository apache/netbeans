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
package org.netbeans.modules.php.editor.elements;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
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
    private Collection<QualifiedName> usedTraits;

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
        Set<TraitElement> retval = values.length > 0 ? new HashSet<TraitElement>() : Collections.<TraitElement>emptySet();
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
        return usedTraits;
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
            final String[] traitNames = traits.split(Separator.COMMA.toString());
            for (String trait : traitNames) {
                retval.add(QualifiedName.create(trait));
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
