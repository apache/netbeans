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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.EnumElement;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.EnumDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.openide.util.Parameters;

public final class EnumElementImpl extends TypeElementImpl implements EnumElement {

    public static final String IDX_FIELD = PHPIndexer.FIELD_ENUM;

    private final Collection<QualifiedName> usedTraits;
    private final QualifiedName backingType;

    private EnumElementImpl(
            final QualifiedName qualifiedName,
            final int offset,
            final QualifiedName backingType,
            final Set<QualifiedName> ifaceNames,
            final Collection<QualifiedName> fqSuperInterfaces,
            final int flags,
            final Collection<QualifiedName> usedTraits,
            final String fileUrl,
            final ElementQuery elementQuery,
            final boolean isDeprecated
    ) {
        super(
                qualifiedName,
                offset,
                ifaceNames,
                fqSuperInterfaces,
                flags,
                fileUrl,
                elementQuery,
                isDeprecated
        );
        this.backingType = backingType;
        this.usedTraits = usedTraits;
    }

    public static Set<EnumElement> fromSignature(final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        return fromSignature(NameKind.empty(), indexScopeQuery, indexResult);
    }

    public static Set<EnumElement> fromSignature(final NameKind query, final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        String[] values = indexResult.getValues(IDX_FIELD);
        Set<EnumElement> retval = values.length > 0 ? new HashSet<>() : Collections.<EnumElement>emptySet();
        for (String val : values) {
            final EnumElement enumElement = fromSignature(query, indexScopeQuery, Signature.get(val));
            if (enumElement != null) {
                retval.add(enumElement);
            }
        }
        return retval;
    }

    @CheckForNull
    private static EnumElement fromSignature(final NameKind query, final IndexQueryImpl indexScopeQuery, final Signature enumSignature) {
        Parameters.notNull("query", query); // NOI18N
        EnumSignatureParser signParser = new EnumSignatureParser(enumSignature);
        EnumElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new EnumElementImpl(
                    signParser.getQualifiedName(),
                    signParser.getOffset(),
                    QualifiedName.create(signParser.getBackingType()),
                    signParser.getSuperInterfaces(),
                    signParser.getFQSuperInterfaces(),
                    signParser.getFlags(),
                    signParser.getUsedTraits(),
                    signParser.getFileUrl(),
                    indexScopeQuery,
                    signParser.isDeprecated()
            );
        }
        return retval;
    }

    public static EnumElement fromNode(final NamespaceElement namespace, final EnumDeclaration node, final ElementQuery.File fileQuery) {
        Parameters.notNull("node", node); // NOI18N
        Parameters.notNull("fileQuery", fileQuery); // NOI18N
        EnumDeclarationInfo info = EnumDeclarationInfo.create(node);
        final QualifiedName fullyQualifiedName = namespace != null ? namespace.getFullyQualifiedName() : QualifiedName.createForDefaultNamespaceName();
        return new EnumElementImpl(
                fullyQualifiedName.append(info.getName()),
                info.getRange().getStart(),
                info.getBackingType(),
                info.getInterfaceNames(),
                Collections.<QualifiedName>emptySet(),
                PhpModifiers.NO_FLAGS,
                info.getUsedTraits(),
                fileQuery.getURL().toExternalForm(),
                fileQuery,
                VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node)
        );
    }

    // XXX
    public static EnumElement fromFrameworks(final PhpClass clz, final ElementQuery elementQuery) {
        Parameters.notNull("clz", clz); // NOI18N
        Parameters.notNull("elementQuery", elementQuery); // NOI18N
        String fullyQualifiedName = clz.getFullyQualifiedName();
        EnumElementImpl retval = new EnumElementImpl(
                QualifiedName.create(fullyQualifiedName == null ? clz.getName() : fullyQualifiedName),
                clz.getOffset(),
                null,
                Collections.<QualifiedName>emptySet(),
                Collections.<QualifiedName>emptySet(),
                PhpModifiers.NO_FLAGS,
                Collections.<QualifiedName>emptySet(),
                null,
                elementQuery,
                false
        );
        retval.setFileObject(clz.getFile());
        return retval;
    }

    private static boolean matchesQuery(final NameKind query, EnumSignatureParser signParser) {
        Parameters.notNull("query", query); // NOI18N
        return (query instanceof NameKind.Empty) || query.matchesName(EnumElement.KIND, signParser.getQualifiedName());
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return KIND;
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Separator.SEMICOLON); // 0: lower case name
        sb.append(getName()).append(Separator.SEMICOLON); // 1: name
        sb.append(getOffset()).append(Separator.SEMICOLON); // 2: offset
        QualifiedName namespaceName = getNamespaceName();
        sb.append(namespaceName.toString()).append(Separator.SEMICOLON); // 3:namespace name
        sb.append(getBackingTypeName()).append(Separator.SEMICOLON); // 4: backing type
        StringBuilder ifaceSb = new StringBuilder();
        for (QualifiedName ifaceName : getSuperInterfaces()) {
            if (ifaceSb.length() > 0) {
                ifaceSb.append(Separator.COMMA);
            }
            ifaceSb.append(ifaceName.toString());
        }
        sb.append(ifaceSb); // 5: interfaces
        sb.append(getPhpModifiers().toFlags()).append(Separator.SEMICOLON); // 6: modifiers
        sb.append(Separator.SEMICOLON);
        if (!usedTraits.isEmpty()) {
            StringBuilder traitSb = new StringBuilder();
            for (QualifiedName usedTrait : usedTraits) {
                if (traitSb.length() > 0) {
                    traitSb.append(Separator.COMMA);
                }
                traitSb.append(usedTrait.toString());
            }
            sb.append(traitSb); // 7: used traits
        }
        sb.append(Separator.SEMICOLON);
        sb.append(isDeprecated() ? 1 : 0).append(Separator.SEMICOLON); // 8: deprecated
        sb.append(getFilenameUrl()).append(Separator.SEMICOLON); // 9: filename url
        checkClassSignature(sb);
        return sb.toString();
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
            sb.append(" implements "); // NOI18N
        }
        StringBuilder ifacesBuffer = new StringBuilder();
        for (QualifiedName qualifiedName : superIfaces) {
            if (ifacesBuffer.length() > 0) {
                ifacesBuffer.append(", "); // NOI18N
            }
            ifacesBuffer.append(qualifiedName.getName());
        }
        sb.append(ifacesBuffer);
    }

    private void checkClassSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            EnumSignatureParser parser = new EnumSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getQualifiedName().toName().toString());
            assert getNamespaceName().equals(parser.getQualifiedName().toNamespaceName());
            assert getOffset() == parser.getOffset();
            assert getPhpModifiers().toFlags() == parser.getFlags();
            assert getSuperInterfaces().size() == parser.getSuperInterfaces().size();
            assert getBackingTypeName().equals(parser.getBackingType());
        }
    }

    @Override
    public Collection<QualifiedName> getUsedTraits() {
        return Collections.unmodifiableCollection(usedTraits);
    }

    @Override
    @CheckForNull
    public QualifiedName getBackingType() {
        return backingType;
    }

    @NonNull
    private String getBackingTypeName() {
        return backingType == null ? "" : backingType.toString(); // NOI18N
    }

    //~ inner classes
    private enum SigElement {
        NAME_LOWERCASE(0),
        NAME(1),
        OFFSET(2),
        NAMESPACE_NAME(3),
        BACKING_TYPE(4),
        INTERFACES(5),
        MODIFIERS(6),
        USED_TRAITS(7),
        DEPRECATED(8),
        FILENAME_URL(9),
        ;
        private final int index;

        private SigElement(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    // see also EnumScopeImpl
    private static class EnumSignatureParser {

        private final Signature signature;

        EnumSignatureParser(Signature signature) {
            this.signature = signature;
        }

        QualifiedName getQualifiedName() {
            return composeQualifiedName(getNamespaceName(), getName());
        }

        public Set<QualifiedName> getSuperInterfaces() {
            Set<QualifiedName> ifaces = Collections.emptySet();
            String separatedIfaces = getInterfaces();
            if (separatedIfaces.length() > 0) {
                int index = separatedIfaces.indexOf(Separator.PIPE.toString());
                if (index > 0) {
                    String field = separatedIfaces.substring(0, index);
                    ifaces = new HashSet<>();
                    final String[] ifaceNames = field.split(Separator.COMMA.toString());
                    for (String ifName : ifaceNames) {
                        ifaces.add(QualifiedName.create(ifName));
                    }
                }
            }
            return ifaces;
        }

        public Collection<QualifiedName> getFQSuperInterfaces() {
            Collection<QualifiedName> retval = Collections.<QualifiedName>emptySet();
            String separatedIfaces = getInterfaces();
            int index = separatedIfaces.indexOf(Separator.PIPE.toString());
            if (index > 0) {
                String fqInterfaces = separatedIfaces.substring(index + 1);
                String[] fqInterfaceNames = fqInterfaces.split(Separator.COMMA.toString());
                retval = new ArrayList<>();
                for (String interfaceName : fqInterfaceNames) {
                    retval.add(QualifiedName.create(interfaceName));
                }
            }
            return retval;
        }

        @NonNull
        String getName() {
            return signature.string(SigElement.NAME.getIndex());
        }

        int getOffset() {
            return signature.integer(SigElement.OFFSET.getIndex());
        }

        @NonNull
        String getNamespaceName() {
            return signature.string(SigElement.NAMESPACE_NAME.getIndex());
        }

        @NonNull
        String getBackingType() {
            return signature.string(SigElement.BACKING_TYPE.getIndex());
        }

        @NonNull
        String getInterfaces() {
            return signature.string(SigElement.INTERFACES.getIndex());
        }

        int getFlags() {
            return signature.integer(SigElement.MODIFIERS.getIndex());
        }

        @NonNull
        String getTraits() {
            return signature.string(SigElement.USED_TRAITS.getIndex());
        }

        int getDeprecated() {
            return signature.integer(SigElement.DEPRECATED.getIndex());
        }

        public Collection<QualifiedName> getUsedTraits() {
            Collection<QualifiedName> retval = new HashSet<>();
            String traits = getTraits();
            final String[] traitNames = traits.split(Separator.COMMA.toString());
            for (String trait : traitNames) {
                retval.add(QualifiedName.create(trait));
            }
            return retval;
        }

        boolean isDeprecated() {
            return getDeprecated() == 1;
        }

        @NonNull
        String getFileUrl() {
            return signature.string(SigElement.FILENAME_URL.getIndex());
        }

    }
}
