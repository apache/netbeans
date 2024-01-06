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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.CaseDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration;
import org.openide.util.Parameters;
import org.netbeans.modules.php.editor.api.elements.EnumCaseElement;
import org.netbeans.modules.php.editor.api.elements.EnumElement;

public final class CaseElementImpl extends PhpElementImpl implements EnumCaseElement {

    public static final String IDX_FIELD = PHPIndexer.FIELD_ENUM_CASE;
    private final TypeElement enclosingType;
    private final String value;
    private final PhpModifiers modifiers;
    private final boolean isBacked;

    private CaseElementImpl(
            final TypeElement enclosingType,
            final String caseName,
            final String value,
            final int offset,
            final int flags,
            final String fileUrl,
            final ElementQuery elementQuery,
            final boolean isDeprecated,
            final boolean isBacked
    ) {
        super(caseName, enclosingType.getName(), fileUrl, offset, elementQuery, isDeprecated);
        this.enclosingType = enclosingType;
        this.value = value;
        this.modifiers = PhpModifiers.fromBitMask(flags);
        this.isBacked = isBacked;
    }

    public static Set<EnumCaseElement> fromSignature(
            final TypeElement type,
            final IndexQueryImpl indexScopeQuery,
            final IndexResult indexResult
    ) {
        return fromSignature(type, NameKind.empty(), indexScopeQuery, indexResult);
    }

    public static Set<EnumCaseElement> fromSignature(
            final TypeElement type,
            final NameKind query,
            final IndexQueryImpl indexScopeQuery,
            final IndexResult indexResult
    ) {
        final String[] values = indexResult.getValues(IDX_FIELD);
        final Set<EnumCaseElement> retval = values.length > 0
                ? new HashSet<>() : Collections.<EnumCaseElement>emptySet();
        for (final String val : values) {
            final EnumCaseElement enumCase = fromSignature(type, query, indexScopeQuery, Signature.get(val));
            if (enumCase != null) {
                retval.add(enumCase);
            }
        }
        return retval;
    }

    private static EnumCaseElement fromSignature(final TypeElement type, final NameKind query, final IndexQueryImpl indexScopeQuery, final Signature signature) {
        final CaseSignatureParser signParser = new CaseSignatureParser(signature);
        EnumCaseElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new CaseElementImpl(
                    type,
                    signParser.getName(),
                    signParser.getValue(),
                    signParser.getOffset(),
                    signParser.getFlags(),
                    signParser.getFileUrl(),
                    indexScopeQuery,
                    signParser.isDeprecated(),
                    signParser.isBacked()
            );
        }
        return retval;
    }

    public static EnumCaseElement fromNode(final TypeElement type, CaseDeclaration node, final ElementQuery.File fileQuery) {
        Parameters.notNull("type", type); // NOI18N
        Parameters.notNull("node", node); // NOI18N
        Parameters.notNull("fileQuery", fileQuery); // NOI18N
        CaseDeclarationInfo info = CaseDeclarationInfo.create(node);
        boolean isBacked = false;
        if (type instanceof EnumElement) {
            EnumElement enumElement = (EnumElement) type;
            isBacked = enumElement.getBackingType() != null;
        }
        return new CaseElementImpl(
                type,
                info.getName(),
                info.getValue(),
                info.getRange().getStart(),
                info.getAccessModifiers().toFlags(),
                fileQuery.getURL().toExternalForm(),
                fileQuery,
                VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node),
                isBacked
        );
    }

    private static boolean matchesQuery(final NameKind query, CaseSignatureParser signParser) {
        Parameters.notNull("query", query); // NOI18N
        return (query instanceof NameKind.Empty)
                || query.matchesName(EnumCaseElement.KIND, signParser.getName());
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Separator.SEMICOLON); // 0: lower case name
        sb.append(getName()).append(Separator.SEMICOLON); // 1: name
        sb.append(getOffset()).append(Separator.SEMICOLON); // 2: offset
        sb.append(getValue()).append(Separator.SEMICOLON); // 3: value
        sb.append(isDeprecated() ? 1 : 0).append(Separator.SEMICOLON); // 4: deprecated
        sb.append(getFilenameUrl()).append(Separator.SEMICOLON); // 5: filename url
        sb.append(getPhpModifiers().toFlags()).append(Separator.SEMICOLON); // 6: modifiers
        sb.append(isBacked() ? 1 : 0).append(Separator.SEMICOLON); // 6: isBacked
        checkSignature(sb);
        return sb.toString();
    }

    @Override
    public PhpModifiers getPhpModifiers() {
        return modifiers;
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return EnumCaseElement.KIND;
    }

    @Override
    public TypeElement getType() {
        return enclosingType;
    }

    @Override
    public boolean isBacked() {
        return isBacked;
    }

    private void checkSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            CaseSignatureParser parser = new CaseSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getName());
            assert getOffset() == parser.getOffset();
            assert getPhpModifiers().toFlags() == parser.getFlags();
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isStatic() {
        return getPhpModifiers().isStatic();
    }

    @Override
    public boolean isPublic() {
        return getPhpModifiers().isPublic();
    }

    @Override
    public boolean isProtected() {
        return getPhpModifiers().isProtected();
    }

    @Override
    public boolean isPrivate() {
        return getPhpModifiers().isPrivate();
    }

    @Override
    public boolean isFinal() {
        return getPhpModifiers().isFinal();
    }

    @Override
    public boolean isAbstract() {
        return getPhpModifiers().isAbstract();
    }

    //~ inner classes
    private enum SigElement {
        NAME_LOWERCASE(0),
        NAME(1),
        OFFSET(2),
        VALUE(3),
        DEPRECATED(4),
        FILENAME_URL(5),
        MODIFIERS(6),
        BACKED(7),
        ;
        private final int index;

        private SigElement(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    private static class CaseSignatureParser {

        private final Signature signature;

        CaseSignatureParser(Signature signature) {
            this.signature = signature;
        }

        String getName() {
            return signature.string(SigElement.NAME.getIndex());
        }

        int getOffset() {
            return signature.integer(SigElement.OFFSET.getIndex());
        }

        String getValue() {
            return signature.string(SigElement.VALUE.getIndex());
        }

        boolean isDeprecated() {
            return signature.integer(SigElement.DEPRECATED.getIndex()) == 1;
        }

        String getFileUrl() {
            return signature.string(SigElement.FILENAME_URL.getIndex());
        }

        int getFlags() {
            return signature.integer(SigElement.MODIFIERS.getIndex());
        }

        boolean isBacked() {
            return signature.integer(SigElement.BACKED.getIndex()) == 1;
        }

    }
}
