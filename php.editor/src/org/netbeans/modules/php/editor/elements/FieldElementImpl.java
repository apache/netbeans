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
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.FileElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleFieldDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public final class FieldElementImpl extends PhpElementImpl implements FieldElement {

    public static final String IDX_FIELD = PHPIndexer.FIELD_FIELD;

    private final PhpModifiers modifiers;
    private final TypeElement enclosingType;
    private final Set<TypeResolver> instanceTypes;
    private final Set<TypeResolver> instanceFQTypes;

    private FieldElementImpl(
            final TypeElement enclosingType,
            final String fieldName,
            final int offset,
            final int flags,
            final String fileUrl,
            final ElementQuery elementQuery,
            final Set<TypeResolver> instanceTypes,
            final Set<TypeResolver> instanceFQTypes,
            final boolean isDeprecated) {
        super(FieldElementImpl.getName(fieldName, true), enclosingType.getName(), fileUrl, offset, elementQuery, isDeprecated);
        this.modifiers = PhpModifiers.fromBitMask(flags);
        this.enclosingType = enclosingType;
        this.instanceTypes = instanceTypes;
        this.instanceFQTypes = instanceFQTypes;
    }

    public static Set<FieldElement> fromSignature(final TypeElement type,
            final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        return fromSignature(type, NameKind.empty(), indexQuery, indexResult);
    }

    public static Set<FieldElement> fromSignature(final TypeElement type, final NameKind query,
            final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        final String[] values = indexResult.getValues(IDX_FIELD);
        final Set<FieldElement> retval = values.length > 0
                ? new HashSet<FieldElement>() : Collections.<FieldElement>emptySet();
        for (String val : values) {
            final FieldElement field = fromSignature(type, query, indexQuery, Signature.get(val));
            if (field != null) {
                retval.add(field);
            }
        }
        return retval;
    }

    public static FieldElement fromSignature(final TypeElement type, final NameKind query,
            final IndexQueryImpl indexScopeQuery, final Signature sig) {
        Parameters.notNull("query", query); //NOI18N
        final FieldSignatureParser signParser = new FieldSignatureParser(sig);
        FieldElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new FieldElementImpl(type, signParser.getFieldName(),
                    signParser.getOffset(), signParser.getFlags(), signParser.getFileUrl(),
                    indexScopeQuery, signParser.getTypes(), signParser.getFQTypes(), signParser.isDeprecated());

        }
        return retval;
    }

    public static Set<FieldElement> fromNode(TypeElement type, FieldsDeclaration node, ElementQuery.File fileQuery) {
        Parameters.notNull("type", type);
        Parameters.notNull("node", node);
        Parameters.notNull("fileQuery", fileQuery);
        final List<? extends SingleFieldDeclarationInfo> fields = SingleFieldDeclarationInfo.create(node);
        final Set<FieldElement> retval = new HashSet<>();
        for (SingleFieldDeclarationInfo info : fields) {
            final String returnType = VariousUtils.getFieldTypeFromPHPDoc(fileQuery.getResult().getProgram(), info.getOriginalNode());
            Set<TypeResolver> types = returnType != null ? TypeResolverImpl.parseTypes(returnType) : null;
            retval.add(new FieldElementImpl(type, info.getName(), info.getRange().getStart(),
                    info.getAccessModifiers().toFlags(), fileQuery.getURL().toString(), fileQuery,
                    types, types, VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node)));
        }
        return retval;
    }

    public static FieldElement fromNode(final TypeElement type, final FieldAccess node,
            final Set<TypeResolver> resolvers, final FileElementQuery fileQuery) {
        Parameters.notNull("type", type);
        Parameters.notNull("resolvers", resolvers);
        Parameters.notNull("node", node);
        Parameters.notNull("fileQuery", fileQuery);
        final ASTNodeInfo<FieldAccess> info = ASTNodeInfo.create(node);
        return new FieldElementImpl(
                type,
                info.getName(),
                info.getRange().getStart(),
                PhpModifiers.PUBLIC,
                fileQuery.getURL().toString(),
                fileQuery,
                resolvers,
                resolvers,
                VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node));
    }

    static FieldElement fromFrameworks(final TypeElement type, final PhpType.Field field, final ElementQuery elementQuery) {
        Parameters.notNull("field", field);
        Parameters.notNull("elementQuery", elementQuery);
        // XXX check nullable type?
        final PhpType fldType = field.getType();
        final Set<TypeResolver> typeResolvers = fldType != null
                ? Collections.<TypeResolver>singleton(new TypeResolverImpl(fldType.getFullyQualifiedName(), false))
                : Collections.<TypeResolver>emptySet();
        FieldElementImpl retval = new FieldElementImpl(type, field.getName(), field.getOffset(),
                PhpModifiers.NO_FLAGS, null, elementQuery, typeResolvers, typeResolvers, false);
        retval.setFileObject(field.getFile());
        return retval;
    }

    private static boolean matchesQuery(final NameKind query, FieldSignatureParser signParser) {
        Parameters.notNull("NameKind query: can't be null", query);
        return (query instanceof NameKind.Empty)
                || query.matchesName(FieldElement.KIND, signParser.getFieldName());
    }


    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        final String noDollarName = getName().substring(1);
        sb.append(noDollarName.toLowerCase()).append(Separator.SEMICOLON); //NOI18N
        sb.append(noDollarName).append(Separator.SEMICOLON); //NOI18N
        sb.append(getOffset()).append(Separator.SEMICOLON); //NOI18N
        sb.append(getPhpModifiers().toFlags()).append(Separator.SEMICOLON);
        for (TypeResolver typeResolver : getInstanceTypes()) {
            TypeResolverImpl resolverImpl = (TypeResolverImpl) typeResolver;
            sb.append(resolverImpl.getSignature());
        }
        sb.append(Separator.SEMICOLON); //NOI18N
        sb.append(isDeprecated() ? 1 : 0).append(Separator.SEMICOLON);
        sb.append(getFilenameUrl()).append(Separator.SEMICOLON);
        checkSignature(sb);
        return sb.toString();
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return FieldElement.KIND;
    }

    @Override
    public PhpModifiers getPhpModifiers() {
        return modifiers;
    }

    @Override
    public TypeElement getType() {
        return enclosingType;
    }

    @Override
    public Set<TypeResolver> getInstanceTypes() {
        return instanceTypes;
    }

    @Override
    public Set<TypeResolver> getInstanceFQTypes() {
        return instanceFQTypes;
    }

    private void checkSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            FieldSignatureParser parser = new FieldSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getFieldName());
            assert getOffset() == parser.getOffset();
            assert getPhpModifiers().toFlags() == parser.getFlags();
            assert getInstanceTypes().size() == parser.getTypes().size();
            assert getInstanceFQTypes().size() == parser.getFQTypes().size();
        }
    }

    @Override
    public String getName(final boolean dollared) {
        return getName(getName(), dollared);
    }

    private static String getName(final String name, final boolean dollared) {
        final boolean startsWithDollar = name.startsWith(VariableElementImpl.DOLLAR_PREFIX);
        if (startsWithDollar == dollared) {
            return name;
        }
        return dollared ? String.format("%s%s", VariableElementImpl.DOLLAR_PREFIX, name) : name.substring(1); //NOI18N
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

    private static class FieldSignatureParser {

        private final Signature signature;

        FieldSignatureParser(Signature signature) {
            this.signature = signature;
        }

        String getFieldName() {
            return signature.string(1);
        }

        int getOffset() {
            return signature.integer(2);
        }

        int getFlags() {
            return signature.integer(3);
        }

        Set<TypeResolver> getTypes() {
            return TypeResolverImpl.parseTypes(signature.string(4));
        }

        Set<TypeResolver> getFQTypes() {
            return TypeResolverImpl.parseTypes(signature.string(5));
        }

        boolean isDeprecated() {
            return signature.integer(6) == 1;
        }

        String getFileUrl() {
            return signature.string(7);
        }
    }
}
