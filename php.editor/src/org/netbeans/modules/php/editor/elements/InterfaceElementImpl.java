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
