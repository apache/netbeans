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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public final class ClassElementImpl extends TypeElementImpl implements ClassElement {

    public static final String IDX_FIELD = PHPIndexer.FIELD_CLASS;

    @NullAllowed
    private final QualifiedName superClass;
    private final Collection<QualifiedName> possibleFQSuperClassNames;
    private final Collection<QualifiedName> fqMixinClassNames;
    private final Collection<QualifiedName> usedTraits;

    private ClassElementImpl(
            final QualifiedName qualifiedName,
            final int offset,
            final QualifiedName superClsName,
            final Collection<QualifiedName> possibleFQSuperClassNames,
            final Set<QualifiedName> ifaceNames,
            final Collection<QualifiedName> fqSuperInterfaces,
            final int flags,
            final Collection<QualifiedName> usedTraits,
            final String fileUrl,
            final ElementQuery elementQuery,
            final boolean isDeprecated,
            final Collection<QualifiedName> fqMixinClassNames) {
        super(qualifiedName, offset, ifaceNames, fqSuperInterfaces, flags, fileUrl, elementQuery, isDeprecated);
        this.superClass = superClsName;
        this.possibleFQSuperClassNames = possibleFQSuperClassNames;
        this.usedTraits = usedTraits;
        this.fqMixinClassNames = fqMixinClassNames;
        checkTypeNames();
    }

    private void checkTypeNames() {
        // GH-6634
        // avoid getting types from the index with an empty string
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            if (superClass != null) {
                assert !superClass.getName().isEmpty();
            }
            for (QualifiedName name : possibleFQSuperClassNames) {
                assert !name.getName().isEmpty();
            }
            for (QualifiedName usedTrait : usedTraits) {
                assert !usedTrait.getName().isEmpty();
            }
            for (QualifiedName className : fqMixinClassNames) {
                assert !className.getName().isEmpty();
            }
        }
    }

    public static Set<ClassElement> fromSignature(final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        return fromSignature(NameKind.empty(), indexScopeQuery, indexResult);
    }

    public static Set<ClassElement> fromSignature(final NameKind query,
            final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        String[] values = indexResult.getValues(IDX_FIELD);
        Set<ClassElement> retval = values.length > 0 ? new HashSet<>() : Collections.<ClassElement>emptySet();
        for (String val : values) {
            final ClassElement clz = fromSignature(query, indexScopeQuery, Signature.get(val));
            if (clz != null) {
                retval.add(clz);
            }
        }
        return retval;
    }

    private static ClassElement fromSignature(final NameKind query,
            final IndexQueryImpl indexScopeQuery, final Signature clsSignature) {
        Parameters.notNull("query", query);
        ClassSignatureParser signParser = new ClassSignatureParser(clsSignature);
        ClassElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new ClassElementImpl(signParser.getQualifiedName(), signParser.getOffset(),
                    signParser.getSuperClassName(), signParser.getPossibleFQSuperClassName(),
                    signParser.getSuperInterfaces(), signParser.getFQSuperInterfaces(), signParser.getFlags(),
                    signParser.getUsedTraits(), signParser.getFileUrl(), indexScopeQuery,
                    signParser.isDeprecated(), signParser.getFQMixinClassNames());
        }
        return retval;
    }

    public static ClassElement fromNode(final NamespaceElement namespace, final ClassDeclaration node, final ElementQuery.File fileQuery) {
        Parameters.notNull("node", node);
        Parameters.notNull("fileQuery", fileQuery);
        ClassDeclarationInfo info = ClassDeclarationInfo.create(node);
        final QualifiedName fullyQualifiedName = namespace != null ? namespace.getFullyQualifiedName() : QualifiedName.createForDefaultNamespaceName();
        // XXX mixin
        return new ClassElementImpl(
                fullyQualifiedName.append(info.getName()), info.getRange().getStart(),
                info.getSuperClassName(), Collections.<QualifiedName>emptySet(), info.getInterfaceNames(),
                Collections.<QualifiedName>emptySet(), info.getAccessModifiers().toFlags(), info.getUsedTraits(),
                fileQuery.getURL().toExternalForm(), fileQuery, VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node),
                Collections.<QualifiedName>emptySet());
    }

    public static ClassElement fromFrameworks(final PhpClass clz, final ElementQuery elementQuery) {
        Parameters.notNull("clz", clz);
        Parameters.notNull("elementQuery", elementQuery);
        String fullyQualifiedName = clz.getFullyQualifiedName();
        ClassElementImpl retval = new ClassElementImpl(QualifiedName.create(fullyQualifiedName == null ? clz.getName() : fullyQualifiedName),
                clz.getOffset(), null, Collections.<QualifiedName>emptySet(), Collections.<QualifiedName>emptySet(),
                Collections.<QualifiedName>emptySet(), PhpModifiers.NO_FLAGS, Collections.<QualifiedName>emptySet(), null, elementQuery, false,
                Collections.<QualifiedName>emptySet());
        retval.setFileObject(clz.getFile());
        return retval;
    }

    private static boolean matchesQuery(final NameKind query, ClassSignatureParser signParser) {
        Parameters.notNull("query", query);
        return (query instanceof NameKind.Empty) || query.matchesName(ClassElement.KIND, signParser.getQualifiedName());
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return KIND;
    }

    @CheckForNull
    @Override
    public QualifiedName getSuperClassName() {
        return superClass;
    }

    @Override
    public Collection<QualifiedName> getPossibleFQSuperClassNames() {
        return Collections.unmodifiableCollection(possibleFQSuperClassNames);
    }

    @Override
    public Collection<QualifiedName> getFQMixinClassNames() {
        return Collections.unmodifiableCollection(fqMixinClassNames);
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Separator.SEMICOLON); //NOI18N
        sb.append(getName()).append(Separator.SEMICOLON); //NOI18N
        sb.append(getOffset()).append(Separator.SEMICOLON); //NOI18N
        QualifiedName superClassName = getSuperClassName();
        if (superClassName != null) {
            sb.append(superClassName.toString());
            sb.append(Type.SEPARATOR);
            boolean first = true;
            for (QualifiedName qualifiedName : possibleFQSuperClassNames) {
                if (!first) {
                    sb.append(',');
                } else {
                    first = true;
                }
                sb.append(qualifiedName.toString());
            }
        }
        sb.append(Separator.SEMICOLON); //NOI18N
        QualifiedName namespaceName = getNamespaceName();
        sb.append(namespaceName.toString()).append(Separator.SEMICOLON); //NOI18N
        StringBuilder ifaceSb = new StringBuilder();
        for (QualifiedName ifaceName : getSuperInterfaces()) {
            if (ifaceSb.length() > 0) {
                ifaceSb.append(Separator.COMMA); //NOI18N
            }
            ifaceSb.append(ifaceName.toString()); //NOI18N
        }
        sb.append(ifaceSb);
        sb.append(Separator.SEMICOLON); //NOI18N
        sb.append(getPhpModifiers().toFlags()).append(Separator.SEMICOLON);
        if (!usedTraits.isEmpty()) {
            StringBuilder traitSb = new StringBuilder();
            for (QualifiedName usedTrait : usedTraits) {
                if (traitSb.length() > 0) {
                    traitSb.append(","); //NOI18N
                }
                traitSb.append(usedTrait.toString());
            }
            sb.append(traitSb);
        }
        sb.append(";"); //NOI18N
        sb.append(isDeprecated() ? 1 : 0).append(";"); //NOI18N
        sb.append(getFilenameUrl()).append(Separator.SEMICOLON);
        StringBuilder mixinSb = new StringBuilder();
        fqMixinClassNames.forEach((mixinClassName) -> {
            if (mixinSb.length() > 0) {
                mixinSb.append(Separator.COMMA);
            }
            mixinSb.append(mixinClassName.toString());
        });
        sb.append(mixinSb.toString());
        sb.append(Separator.SEMICOLON);
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
        QualifiedName superClassName = getSuperClassName();
        if (superClassName != null) {
            sb.append(" extends  "); //NOI18N
            sb.append(superClassName.getName());
        }
        Set<QualifiedName> superIfaces = getSuperInterfaces();
        if (!superIfaces.isEmpty()) {
            sb.append(" implements "); //NOI18N
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

    private void checkClassSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            ClassSignatureParser parser = new ClassSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getQualifiedName().toName().toString());
            assert getNamespaceName().equals(parser.getQualifiedName().toNamespaceName());
            assert getOffset() == parser.getOffset();
            assert getPhpModifiers().toFlags() == parser.getFlags();
            QualifiedName superClassName = getSuperClassName();
            if (superClassName != null) {
                assert superClassName.equals(parser.getSuperClassName());
            }
            assert getSuperInterfaces().size() == parser.getSuperInterfaces().size();
        }
    }

    @Override
    public boolean isFinal() {
        return getPhpModifiers().isFinal();
    }

    @Override
    public boolean isAbstract() {
        return getPhpModifiers().isAbstract();
    }

    @Override
    public boolean isReadonly() {
        return getPhpModifiers().isReadonly();
    }

    @Override
    public boolean isAnonymous() {
        return CodeUtils.isSyntheticTypeName(getName());
    }

    @Override
    public Collection<QualifiedName> getUsedTraits() {
        return Collections.unmodifiableCollection(usedTraits);
    }

    private static class ClassSignatureParser {

        private final Signature signature;

        ClassSignatureParser(Signature signature) {
            this.signature = signature;
        }

        QualifiedName getQualifiedName() {
            return composeQualifiedName(signature.string(4), signature.string(1));
        }

        @CheckForNull
        QualifiedName getSuperClassName() {
            String name = signature.string(3);
            if (name != null) {
                int index = name.indexOf('|');
                if (index > 0) {
                    name = name.substring(0, index);
                }
            }
            return name.trim().length() == 0 ? null : QualifiedName.create(name);
        }

        Collection<QualifiedName> getPossibleFQSuperClassName() {
            String field = signature.string(3);
            Collection<QualifiedName> retval = Collections.emptyList();
            if (field != null) {
                int index = field.indexOf('|');
                if (index > 0) {
                    field = field.substring(index + 1);
                    retval = new ArrayList<>();
                    for (StringTokenizer st = new StringTokenizer(field, ","); st.hasMoreTokens();) {
                        String token = st.nextToken();
                        retval.add(QualifiedName.create(token));
                    }
                }
            }
            return retval;
        }

        public Set<QualifiedName> getSuperInterfaces() {
            Set<QualifiedName> ifaces = Collections.emptySet();
            String separatedIfaces = signature.string(5);
            if (separatedIfaces != null && separatedIfaces.length() > 0) {
                int index = separatedIfaces.indexOf('|');
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
            String separatedIfaces = signature.string(5);
            if (separatedIfaces != null) {
                int index = separatedIfaces.indexOf('|');
                if (index > 0) {
                    String field = separatedIfaces.substring(index + 1);
                    retval = new ArrayList<QualifiedName>();
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

        int getFlags() {
            return signature.integer(6);
        }

        public Collection<QualifiedName> getUsedTraits() {
            Collection<QualifiedName> retval = new HashSet<>();
            String traits = signature.string(7);
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
            return signature.integer(8) == 1;
        }

        String getFileUrl() {
            return signature.string(9);
        }

        public Collection<QualifiedName> getFQMixinClassNames() {
            Collection<QualifiedName> retval = new HashSet<>();
            String mixins = signature.string(10);
            final String[] mixinNames = CodeUtils.COMMA_PATTERN.split(mixins);
            for (String mixinName : mixinNames) {
                if (!mixinName.isEmpty()) {
                    // GH-6634
                    // avoid getting mixins from the index with an empty string
                    retval.add(QualifiedName.create(mixinName));
                }
            }
            return retval;
        }
    }
}
