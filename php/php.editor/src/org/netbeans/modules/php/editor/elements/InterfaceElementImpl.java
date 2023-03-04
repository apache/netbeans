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
import java.util.StringTokenizer;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public final class InterfaceElementImpl extends TypeElementImpl implements InterfaceElement {

    public static final String IDX_FIELD = PHPIndexer.FIELD_IFACE;

    private InterfaceElementImpl(
            final QualifiedName qualifiedName,
            final int offset,
            final Set<QualifiedName> ifaceNames,
            final Collection<QualifiedName> fqSuperInterfaces,
            final String fileUrl,
            final ElementQuery elementQuery,
            final boolean isDeprecated) {
        super(qualifiedName, offset, ifaceNames, fqSuperInterfaces,
                PhpModifiers.noModifiers().toFlags(), fileUrl, elementQuery, isDeprecated);
    }

    public static Set<InterfaceElement> fromSignature(IndexQueryImpl indexScopeQuery, IndexResult indexResult) {
        return fromSignature(NameKind.empty(), indexScopeQuery, indexResult);
    }

    public static Set<InterfaceElement> fromSignature(final NameKind query,
            final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        String[] values = indexResult.getValues(IDX_FIELD);
        Set<InterfaceElement> retval = values.length > 0 ? new HashSet<InterfaceElement>() : Collections.<InterfaceElement>emptySet();
        for (String val : values) {
            final InterfaceElement iface = fromSignature(query, indexScopeQuery, Signature.get(val));
            if (iface != null) {
                retval.add(iface);
            }
        }
        return retval;
    }

    private static InterfaceElement fromSignature(final NameKind query, final IndexQueryImpl indexScopeQuery,
            final Signature signature) {
        Parameters.notNull("query", query); //NOI18N
        InterfaceSignatureParser signParser = new InterfaceSignatureParser(signature);
        InterfaceElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new InterfaceElementImpl(signParser.getQualifiedName(), signParser.getOffset(),
                    signParser.getSuperInterfaces(), signParser.getFQSuperInterfaces(),
                    signParser.getFileUrl(), indexScopeQuery, signParser.isDeprecated());
        }
        return retval;
    }

    public static InterfaceElement fromNode(final NamespaceElement namespace, final InterfaceDeclaration node, final ElementQuery.File fileQuery) {
        Parameters.notNull("node", node);
        Parameters.notNull("fileQuery", fileQuery);
        InterfaceDeclarationInfo info = InterfaceDeclarationInfo.create(node);
        final QualifiedName fullyQualifiedName = namespace != null
                ? namespace.getFullyQualifiedName()
                : QualifiedName.createForDefaultNamespaceName();
        return new InterfaceElementImpl(
                fullyQualifiedName.append(info.getName()), info.getRange().getStart(),
                info.getInterfaceNames(), Collections.<QualifiedName>emptySet(),
                fileQuery.getURL().toExternalForm(), fileQuery, VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node));
    }

    private static boolean matchesQuery(final NameKind query, InterfaceSignatureParser signParser) {
        Parameters.notNull("query", query); //NOI18N
        return (query instanceof NameKind.Empty) || query.matchesName(InterfaceElement.KIND, signParser.getQualifiedName());
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Separator.SEMICOLON); //NOI18N
        sb.append(getName()).append(Separator.SEMICOLON); //NOI18N
        sb.append(getOffset()).append(Separator.SEMICOLON); //NOI18N
        StringBuilder ifaceSb = new StringBuilder();
        for (QualifiedName ifaceName : getSuperInterfaces()) {
            if (ifaceSb.length() > 0) {
                ifaceSb.append(Separator.COMMA); //NOI18N
            }
            ifaceSb.append(ifaceName.toString()); //NOI18N
        }
        sb.append(ifaceSb);
        sb.append(Separator.SEMICOLON); //NOI18N
        QualifiedName namespaceName = getNamespaceName();
        sb.append(namespaceName.toString()).append(Separator.SEMICOLON); //NOI18N
        sb.append(isDeprecated() ? 1 : 0).append(Separator.SEMICOLON);
        sb.append(getFilenameUrl()).append(Separator.SEMICOLON);
        checkInterfaceSignature(sb);
        return sb.toString();
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return InterfaceElement.KIND;
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

    private void checkInterfaceSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            InterfaceSignatureParser parser = new InterfaceSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getQualifiedName().toName().toString());
            assert getNamespaceName().equals(parser.getQualifiedName().toNamespaceName());
            assert getOffset() == parser.getOffset();
            assert getSuperInterfaces().size() == parser.getSuperInterfaces().size();
        }
    }

    private static class InterfaceSignatureParser {

        private final Signature signature;

        InterfaceSignatureParser(Signature signature) {
            this.signature = signature;
        }

        QualifiedName getQualifiedName() {
            return composeQualifiedName(signature.string(4), signature.string(1));
        }

        public Set<QualifiedName> getSuperInterfaces() {
            Set<QualifiedName> ifaces = Collections.emptySet();
            String separatedIfaces = signature.string(3);
            if (separatedIfaces != null && separatedIfaces.length() > 0) {
                int index = separatedIfaces.indexOf('|');
                if (index > 0) {
                    String field = separatedIfaces.substring(0, index);
                    ifaces = new HashSet<>(); //NOI18N
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
            String separatedIfaces = signature.string(3);
            if (separatedIfaces != null) { //NOI18N
                int index = separatedIfaces.indexOf('|');
                if (index > 0) {
                    String field = separatedIfaces.substring(index + 1);
                    retval = new ArrayList<>();
                    for (StringTokenizer st = new StringTokenizer(field, ","); st.hasMoreTokens();) { //NOI18N
                        String token = st.nextToken();
                        retval.add(QualifiedName.create(token));
                    }
                }
            }
            return retval;
        }

        int getOffset() {
            return signature.integer(2);
        }

        boolean isDeprecated() {
            return signature.integer(5) == 1;
        }

        String getFileUrl() {
            return signature.string(6);
        }
    }
}
