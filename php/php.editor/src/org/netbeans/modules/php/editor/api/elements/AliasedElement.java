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
package org.netbeans.modules.php.editor.api.elements;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.openide.filesystems.FileObject;

/**
 * @author Radek Matous
 */
public class AliasedElement implements FullyQualifiedElement {

    public enum Trait {
        ALIAS,
        ELEMENT
    };

    protected final FullyQualifiedElement element;
    private final AliasedName aliasedName;
    private QualifiedName aliasFqn;
    private Trait trait = Trait.ELEMENT;

    public AliasedElement(final AliasedName aliasedName, final FullyQualifiedElement element) {
        this.aliasedName = aliasedName;
        this.element = element;
    }

    public final QualifiedName getNamespaceName(final Trait traitOfName) {
        return getFullyQualifiedName(traitOfName).toNamespaceName();
    }

    public final String getName(final Trait traitOfName) {
        return getFullyQualifiedName(traitOfName).getName();
    }

    public final synchronized  QualifiedName getFullyQualifiedName(final Trait traitOfName) {
        if (traitOfName.equals(Trait.ALIAS)) {
            if (aliasFqn == null) {
                aliasFqn = element.getFullyQualifiedName();
                final LinkedList<String> originalSegments = aliasFqn.getSegments();
                final LinkedList<String> toReplaceSegments = aliasedName.getRealName().getSegments();
                final LinkedList<String> resultSegments = new LinkedList<>();
                assert toReplaceSegments.size() > 0;
                final String firstSegment = toReplaceSegments.get(0);
                for (int i = 0; i < originalSegments.size(); i++) {
                    final String nextSegment = originalSegments.get(i);
                    if (i <= (originalSegments.size() - toReplaceSegments.size()) && NameKind.exact(nextSegment).matchesName(PhpElementKind.INDEX, firstSegment)) {
                        List<String> subList = originalSegments.subList(i, i + toReplaceSegments.size());
                        if (subList.equals(toReplaceSegments)) {
                            resultSegments.add(aliasedName.getAliasName());
                            i += (subList.size() - 1);
                        } else {
                            resultSegments.add(nextSegment);
                        }
                    } else {
                        resultSegments.add(nextSegment);
                    }
                }
                aliasFqn = QualifiedName.create(true, resultSegments);
            }
            return aliasFqn;
        }
        return element.getFullyQualifiedName();
    }

    @Override
    public final QualifiedName getNamespaceName() {
        return getNamespaceName(trait);
    }

    @Override
    public final String getName() {
        return getName(trait);
    }

    public final boolean isNameAliased() {
        return !NameKind.exact(getName(Trait.ALIAS)).matchesName(element);
    }
    public final boolean isNamespaceNameAliased() {
        return !NameKind.exact(getNamespaceName(Trait.ALIAS)).matchesName(element);
    }

    @Override
    public final QualifiedName getFullyQualifiedName() {
        return getFullyQualifiedName(trait);
    }

    @Override
    public final boolean isAliased() {
        return true;
    }

    @Override
    public final ElementQuery getElementQuery() {
        return element.getElementQuery();
    }

    @Override
    public final String getFilenameUrl() {
        return element.getFilenameUrl();
    }

    @Override
    public final PhpModifiers getPhpModifiers() {
        return element.getPhpModifiers();
    }

    @Override
    public final PhpElementKind getPhpElementKind() {
        return element.getPhpElementKind();
    }

    @Override
    public final int getOffset() {
        return element.getOffset();
    }

    @Override
    public final int getFlags() {
        return element.getFlags();
    }

    @Override
    public final boolean isPlatform() {
        return element.isPlatform();
    }

    @Override
    public final boolean isDeprecated() {
        return element.isDeprecated();
    }

    @Override
    public final FileObject getFileObject() {
        return element.getFileObject();
    }

    @Override
    public final String getMimeType() {
        return element.getMimeType();
    }

    @Override
    public final String getIn() {
        return element.getIn();
    }

    @Override
    public final ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public final Set<Modifier> getModifiers() {
        return element.getModifiers();
    }

    @Override
    public final boolean signatureEquals(ElementHandle handle) {
        return element.signatureEquals(handle);
    }

    @Override
    public final OffsetRange getOffsetRange(ParserResult result) {
        return element.getOffsetRange(result);
    }

    public AliasedName getAliasedName() {
        return aliasedName;
    }

    /**
     * @return the trait
     */
    public Trait getTrait() {
        return trait;
    }

    /**
     * @param trait the trait to set
     */
    public void setTrait(Trait trait) {
        this.trait = trait;
    }
}
