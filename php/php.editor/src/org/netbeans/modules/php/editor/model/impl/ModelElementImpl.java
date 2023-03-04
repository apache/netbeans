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
package org.netbeans.modules.php.editor.model.impl;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.elements.PhpElementImpl;
import org.netbeans.modules.php.editor.index.PHPElement;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Union2;

/**
 * @author Radek Matous
 */
abstract class ModelElementImpl extends PHPElement implements ModelElement {
    public static final int ANY_ATTR = 0xFFFFFFFF;
    private PhpElementKind kind;
    private String name;
    private OffsetRange offsetRange;
    private Union2<String/*url*/, FileObject> file;
    private PhpModifiers modifiers;
    private Scope inScope;
    protected ElementHandle indexedElement;
    private final String filenameUrl;
    private final boolean isDeprecated;

    //new contructors
    ModelElementImpl(Scope inScope, ASTNodeInfo info, PhpModifiers modifiers, boolean isDeprecated) {
        this(inScope, info.getName(), inScope.getFile(), info.getRange(), info.getPhpElementKind(), modifiers, isDeprecated);
    }

    ModelElementImpl(Scope inScope, PhpElement element, PhpElementKind kind) {
        this(
                inScope,
                element.getName(),
                Union2.<String, FileObject>createFirst(element.getFilenameUrl()),
                new OffsetRange(element.getOffset(), element.getOffset() + element.getName().length()),
                kind,
                PhpModifiers.fromBitMask(element.getFlags()),
                element.isDeprecated());
        this.indexedElement = element;
    }

    //old contructors
    ModelElementImpl(Scope inScope, String name, Union2<String/*url*/, FileObject> file,
            OffsetRange offsetRange, PhpElementKind kind, boolean isDeprecated) {
        this(inScope, name, file, offsetRange, kind, PhpModifiers.noModifiers(), isDeprecated);
    }

    ModelElementImpl(Scope inScope, String name,
            Union2<String/*url*/, FileObject> file, OffsetRange offsetRange, PhpElementKind kind,
            PhpModifiers modifiers, boolean isDeprecated) {
        if (name == null || file == null || kind == null || modifiers == null) {
            throw new IllegalArgumentException("null for name | fo | kind: " + name + " | " + file + " | " + kind);
        }
        assert file.hasFirst() || file.hasSecond();
        if (file.hasFirst() && file.first() != null) {
            this.filenameUrl = file.first();
        } else if (file.hasSecond() && file.second() != null) {
            this.filenameUrl = file.second().toURL().toExternalForm();
        } else {
            this.filenameUrl = "";
        }
        this.inScope = inScope;
        this.name = name;
        this.offsetRange = offsetRange;
        this.kind = kind;
        this.file = file;
        this.modifiers = modifiers;
        this.isDeprecated = isDeprecated;
        if (inScope instanceof ScopeImpl && !(this instanceof AssignmentImpl)/* && !(inScope instanceof IndexScope)*/) {
            ((ScopeImpl) inScope).addElement(this);
        }
    }


    @Override
    public final String getIn() {
        Scope retval = getInScope();
        return (retval != null) ? retval.getName() : null;
    }

    @Override
    public final Scope getInScope() {
        return inScope;
    }

    @NonNull
    @Override
    public final String getMimeType() {
        return super.getMimeType();
    }

    @Override
    public final String getName() {
        return name;
    }

    public String getNormalizedName() {
        String filePath = ""; //NOI18N
        final FileObject fileObject = getFileObject();
        if (fileObject != null) {
            filePath = fileObject.getPath();
        }
        return getNamespaceName().append(QualifiedName.create(getName())).toString().toLowerCase() + String.valueOf(offsetRange.getStart()) + filePath;
    }

    static boolean nameKindMatch(Pattern p, String text) {
        return p.matcher(text).matches();
    }

    static boolean nameKindMatchForVariable(String text, QuerySupport.Kind nameKind, String... queries) {
        return nameKindMatch(false, text, nameKind, queries);
    }

    static boolean nameKindMatch(String text, QuerySupport.Kind nameKind, String... queries) {
        return nameKindMatch(true, text, nameKind, queries);
    }

    private static boolean nameKindMatch(boolean forceCaseInsensitivity, String text, QuerySupport.Kind nameKind, String... queries) {
        boolean result = false;
        for (String query : queries) {
            switch (nameKind) {
                case CAMEL_CASE:
                    if (ModelUtils.toCamelCase(text).startsWith(query)) {
                        result = true;
                    }
                    break;
                case CASE_INSENSITIVE_PREFIX:
                    if (text.toLowerCase().startsWith(query.toLowerCase())) {
                        result = true;
                    }
                    break;
                case CASE_INSENSITIVE_REGEXP:
                    text = text.toLowerCase();
                    result = regexpMatch(text, query);
                    break;
                case REGEXP:
                    //TODO: might be perf. problem if called for large collections
                    // and ever and ever again would be compiled still the same query
                    result = regexpMatch(text, query);
                    break;
                case EXACT:
                    boolean retval = (forceCaseInsensitivity) ? text.equalsIgnoreCase(query) : text.equals(query);
                    if (retval) {
                        result = true;
                    }
                    break;
                case PREFIX:
                    if (text.startsWith(query)) {
                        result = true;
                    }
                    break;
                default:
                    //no-op
            }
        }
        return result;
    }

    private static boolean regexpMatch(String text, String query) {
        boolean result = false;
        Pattern p = Pattern.compile(query);
        if (nameKindMatch(p, text)) {
            result = true;
        }
        return result;
    }

    @Override
    public final ElementKind getKind() {
        return getPhpElementKind().getElementKind();
    }


    @Override
    public PhpElementKind getPhpElementKind() {
        return kind;
    }

    @Override
    public int getOffset() {
        return getNameRange().getStart();
    }

    @Override
    public Union2<String, FileObject> getFile() {
        return file;
    }

    @CheckForNull
    @Override
    public FileObject getFileObject() {
        FileObject fileObject = null;
        synchronized (ModelElementImpl.class) {
            if (file != null) {
                fileObject = file.hasSecond() ? file.second() : null;
            }
        }
        if (fileObject == null && file.hasFirst()) {
            String fileUrl = file.first();
            if (StringUtils.hasText(fileUrl)) {
                fileObject = PhpElementImpl.resolveFileObject(fileUrl);
                synchronized (ModelElementImpl.class) {
                    if (fileObject != null) {
                        file = Union2.createSecond(fileObject);
                    }
                }
            }
        }
        return fileObject;
    }

    @Override
    public Set<Modifier> getModifiers() {
        assert modifiers != null;
        Set<Modifier> retval = EnumSet.noneOf(Modifier.class);
        if (modifiers.isPublic()) {
            retval.add(Modifier.PUBLIC);
        }
        if (modifiers.isProtected()) {
            retval.add(Modifier.PROTECTED);
        }
        if (modifiers.isPrivate()) {
            retval.add(Modifier.PRIVATE);
        }
        if (modifiers.isStatic()) {
            retval.add(Modifier.STATIC);
        }
        return retval;
    }

    @Override
    public PhpModifiers getPhpModifiers() {
        return modifiers;
    }

    public final boolean isScope() {
        return this instanceof ScopeImpl;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPhpElementKind().toString()).append(" ").append(getName());
        return sb.toString();
    }

    @Override
    public PHPElement getPHPElement() {
        return this;
    }

    /**
     * @return the offsetRange
     */
    @Override
    public OffsetRange getNameRange() {
        return offsetRange;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ModelElementImpl other = (ModelElementImpl) obj;
        if (this.kind != other.kind) {
            return false;
        }
        if (!this.getNormalizedName().equals(other.getNormalizedName())) {
            return false;
        }
        //TODO: classscopes from different files are not the same, but be carefull about
        // perf. problems before uncommenting it
        /*if (!this.getFileObject().equals(other.getFileObject())) {
            return false;
        }*/
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.kind != null ? this.kind.hashCode() : 0);
        hash = 89 * hash + (this.getNormalizedName() != null ? this.getNormalizedName().hashCode() : 0);
        //hash = 89 * hash + (this.getInScope() != null ? this.getInScope().hashCode() : 0);
        //hash = 89 * hash + Integer.valueOf(this.getOffset()).hashCode();
        //hash = 89 * hash + this.file.hashCode();
        return hash;
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        return getNameRange();
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
    }

    @Override
    public QualifiedName getNamespaceName() {
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        if (namespaceScope != null) {
            return namespaceScope.getQualifiedName();
        }
        return QualifiedName.createForDefaultNamespaceName();
    }

    public final QualifiedName getFullyQualifiedName() {
        return QualifiedName.createFullyQualified(getName(), getNamespaceName().toString());
    }

    @Override
    public ElementQuery getElementQuery() {
        //TODO: FileScope should implement ElementQuery
        FileScope fileScope = ModelUtils.getFileScope(this);
        if (fileScope == null && getInScope() instanceof IndexScope) {
            return ((IndexScope) getInScope()).getIndex();
        }
        assert fileScope != null : this;
        assert fileScope.getIndexScope() != null : this;
        return fileScope.getIndexScope().getIndex();
    }

    @Override
    public String getFilenameUrl() {
        return filenameUrl;
    }

    @Override
    public int getFlags() {
        return getPhpModifiers().toFlags();
    }

    @Override
    public boolean isPlatform() {
        FileObject fo = getFileObject();
        if (fo != null) {
            try {
                return FileUtil.getConfigRoot().getFileSystem().equals(fo.getFileSystem());
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    @Override
    public boolean isDeprecated() {
        return isDeprecated;
    }

    /**
     * @return the indexedElement
     */
    public ElementHandle getIndexedElement() {
        return indexedElement;
    }

    public final boolean isAliased() {
        return false;
    }
}
