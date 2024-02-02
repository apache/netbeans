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
package org.netbeans.modules.php.editor.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IntersectionType;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;
import org.openide.util.Parameters;

/**
 * immutable.
 *
 * @author Radek Matous
 */
public final class QualifiedName {
    private final QualifiedNameKind kind;
    private final List<String> segments;

    public String getName() {
        return toName().toString();
    }

    public String getNamespaceName() {
        return toNamespaceName().toString();
    }

    /**
     * @return prefix name or null
     */
    public static QualifiedName getPrefix(QualifiedName fullName, final QualifiedName suffix, boolean isOverlapingRequired) {
        return getRemainingName(fullName, suffix, true, isOverlapingRequired);
    }

    /**
     * @return suffix name or null
     */
    public static QualifiedName getSuffix(QualifiedName fullName, final QualifiedName prefix, boolean isOverlapingRequired) {
        if (fullName.segments.size() >= prefix.segments.size()) {
            return getRemainingName(fullName, prefix, false, isOverlapingRequired);
        }
        return null;
    }

    private static QualifiedName getRemainingName(final QualifiedName fullName, final QualifiedName fragmentName, boolean prefixRequired, boolean isOverlapingRequired) {
        QualifiedName retval;
        List<String> fullSegments = new ArrayList<>(fullName.getSegments());
        List<String> fragmentSegments = new ArrayList<>(fragmentName.getSegments());
        if (prefixRequired) {
            Collections.reverse(fullSegments);
            Collections.reverse(fragmentSegments);
        }
        List<String> retvalSegments = new ArrayList<>();
        String lastEqualSegment = null;
        for (int i = 0; i < fullSegments.size(); i++) {
            String segment = fullSegments.get(i);
            if (i < fragmentSegments.size()) {
                lastEqualSegment = fragmentSegments.get(i);
                if (segment.equalsIgnoreCase(lastEqualSegment)) {
                    continue;
                }
            }
            if (isOverlapingRequired && retvalSegments.isEmpty() && lastEqualSegment != null) {
                retvalSegments.add(lastEqualSegment);
            }
            retvalSegments.add(segment);
        }
        if (isOverlapingRequired && retvalSegments.isEmpty() && lastEqualSegment != null) {
            retvalSegments.add(lastEqualSegment);
        }
        if (prefixRequired) {
            Collections.reverse(retvalSegments);
        }
        retval = QualifiedName.create(false, retvalSegments);

        QualifiedName test = (prefixRequired) ? retval : fragmentName;
        if (isOverlapingRequired) {
            test = test.toNamespaceName();
        }
        List<String> qnSegments = (prefixRequired) ? fragmentName.getSegments() : retval.getSegments();
        for (String qnseg : qnSegments) {
            test = test.append(qnseg);
        }

        return (fullName.toFullyQualified().equals(test.toFullyQualified())) ? retval : null;
    }

    @CheckForNull
    public static QualifiedName createUnqualifiedNameInClassContext(Expression expression, ClassScope clsScope) {
        if (expression instanceof Identifier) {
            return createUnqualifiedNameInClassContext((Identifier) expression, clsScope);
        } else if (expression instanceof NamespaceName) {
            NamespaceName namespaceName = (NamespaceName) expression;
            if (namespaceName.getSegments().size() == 1 && !namespaceName.isGlobal()) {
                return createUnqualifiedNameInClassContext(namespaceName.getSegments().get(0).getName(), clsScope);
            }
        }
        return create(expression);
    }

    @CheckForNull
    public static QualifiedName create(Expression expression) {
        Expression e;
        if (expression instanceof NullableType) {
            e = ((NullableType) expression).getType();
            assert e instanceof NamespaceName || e instanceof Identifier;
        } else {
            e = expression;
        }

        if (e instanceof NamespaceName) {
            return create((NamespaceName) e);
        } else if (e instanceof Identifier) {
            return createUnqualifiedName((Identifier) e);
        }
        return null;
    }

    public static List<QualifiedName> create(UnionType unionType) {
        List<QualifiedName> qualifiedNames = new ArrayList<>();
        for (Expression type : unionType.getTypes()) {
            if (type instanceof IntersectionType) {
                qualifiedNames.addAll(create((IntersectionType) type));
            } else {
                QualifiedName qualifiedName = create(type);
                if (qualifiedName != null) {
                    qualifiedNames.add(qualifiedName);
                }
            }
        }
        return qualifiedNames;
    }

    public static List<QualifiedName> create(IntersectionType intersectionType) {
        List<QualifiedName> qualifiedNames = new ArrayList<>();
        for (Expression type : intersectionType.getTypes()) {
            QualifiedName qualifiedName = create(type);
            if (qualifiedName != null) {
                qualifiedNames.add(qualifiedName);
            }
        }
        return qualifiedNames;
    }

    public static QualifiedName create(boolean isFullyQualified, List<String> segments) {
        return new QualifiedName(isFullyQualified, segments);
    }

    public static QualifiedName create(NamespaceScope namespaceScope) {
        return QualifiedName.create(namespaceScope.getName());
    }

    public static QualifiedName create(NamespaceName namespaceName) {
        return new QualifiedName(namespaceName);
    }

    public static QualifiedName createUnqualifiedNameInClassContext(Identifier identifier, ClassScope clsScope) {
        return createUnqualifiedNameInClassContext(identifier.getName(), clsScope);
    }

    public static QualifiedName createUnqualifiedName(Identifier identifier) {
        return new QualifiedName(identifier);
    }

    public static QualifiedName createUnqualifiedNameInClassContext(String name, ClassScope clsScope) {
        //TODO: everywhere should be used NameKindMatcher or something like this
        if (clsScope != null) {
            switch (name) {
                case "self": //NOI18N
                    name = clsScope.getName();
                    break;
                case "parent": //NOI18N
                    String superClsName = ModelUtils.getFirst(clsScope.getSuperClassNames());
                    if (superClsName != null) {
                        name = superClsName;
                    }
                    break;
                default:
                // no-op
            }
        }
        return createUnqualifiedName(name);
    }

    public static QualifiedName createForDefaultNamespaceName() {
        return QualifiedName.createUnqualifiedName(NamespaceDeclarationInfo.DEFAULT_NAMESPACE_NAME);
    }

    public static QualifiedName createUnqualifiedName(String name) {
        QualifiedNameKind kind = QualifiedNameKind.resolveKind(name);
        assert kind.equals(QualifiedNameKind.UNQUALIFIED) : name;
        return new QualifiedName(false, Collections.singletonList(name));
    }

    public static QualifiedName createFullyQualified(String name, String namespaceName) {
        List<String> list = new ArrayList<>();
        if (name.startsWith("\\") || name.endsWith("\\")) { //NOI18N
            throw new IllegalArgumentException();
        }
        if (namespaceName != null && namespaceName.trim().length() > 0) {
            if (namespaceName.startsWith("\\") || namespaceName.endsWith("\\")) { //NOI18N
                throw new IllegalArgumentException();
            }
            final String[] segments = namespaceName.split("\\\\"); //NOI18N
            list.addAll(Arrays.asList(segments));
        }
        list.add(name);
        return new QualifiedName(true, list);

    }

    public static QualifiedName create(String name) {
        name = name.trim();
        final QualifiedNameKind kind = QualifiedNameKind.resolveKind(name);
        if (kind.isUnqualified()) {
            return createUnqualifiedName(name);
        } else if (kind.isFullyQualified()) {
            name = name.substring(1);
        }
        final String[] segments = name.split("[\\\\]+"); //NOI18N
        List<String> list;
        if (name.endsWith(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR)) {
            list = new ArrayList<>(Arrays.asList(segments));
            list.add(""); //NOI18N
        } else {
            list = Arrays.asList(segments);
        }
        return new QualifiedName(kind.isFullyQualified(), list);
    }

    private QualifiedName(NamespaceName namespaceName) {
        this.kind = QualifiedNameKind.resolveKind(namespaceName);
        segments = new ArrayList<>();
        for (Identifier identifier : namespaceName.getSegments()) {
            segments.add(identifier.getName());
        }
    }

    private QualifiedName(Identifier identifier) {
        this.kind = QualifiedNameKind.resolveKind(identifier);
        segments = new ArrayList<>(Collections.singleton(identifier.getName()));
        assert kind.isUnqualified();
    }

    private QualifiedName(boolean isFullyQualified, List<String> segments) {
        this.segments = new ArrayList<>(segments.isEmpty() ? Collections.singleton(NamespaceDeclarationInfo.DEFAULT_NAMESPACE_NAME) : segments);
        this.kind = isFullyQualified ? QualifiedNameKind.FULLYQUALIFIED : QualifiedNameKind.resolveKind(this.segments);
    }

    public LinkedList<String> getSegments() {
        return new LinkedList<>(this.segments);
    }

    /**
     * @return the kind
     */
    public QualifiedNameKind getKind() {
        return kind;
    }

    /**
     * @return the internalName
     */
    @Override
    public String toString() {
        return toString(segments.size() - 1);
    }

    public String toString(int numberOfSegments) {
        if (numberOfSegments >= segments.size()) {
            throw new IllegalArgumentException("n >= segments.size()");
        }

        StringBuilder sb = new StringBuilder();
        QualifiedNameKind k = getKind();
        for (int i = 0; i <= numberOfSegments; i++) {
            String oneSegment = segments.get(i);
            if (sb.length() > 0 || (k != null && k.isFullyQualified())) {
                sb.append("\\"); //NOI18N
            }
            sb.append(oneSegment);
        }
        return sb.toString();
    }

    public QualifiedName append(String name) {
        return append(createUnqualifiedName(name));
    }

    public QualifiedName append(QualifiedName qualifiedName) {
        return append(qualifiedName, getKind().isFullyQualified());
    }

    private QualifiedName append(QualifiedName qualifiedName, boolean isFullyQualified) {
        List<String> list = isDefaultNamespace() ? new ArrayList<String>() : new ArrayList<>(getSegments());
        list.addAll(qualifiedName.getSegments());
        return new QualifiedName(isFullyQualified, list);
    }

    public QualifiedName toFullyQualified() {
        return (getKind().isFullyQualified()) ? this : new QualifiedName(true, getSegments());
    }

    public QualifiedName toNotFullyQualified() {
        return (getKind().isFullyQualified()) ? new QualifiedName(false, getSegments()) : this;
    }

    @CheckForNull
    public QualifiedName toFullyQualified(QualifiedName namespaceName) {
        Parameters.notNull("namespaceName", namespaceName); //NOI18N
        return namespaceName.append(this, true);
    }

    @CheckForNull
    public QualifiedName toFullyQualified(NamespaceScope namespaceScope) {
        Parameters.notNull("namespaceScope", namespaceScope); //NOI18N
        return (getKind().isFullyQualified()) ? this : namespaceScope.getQualifiedName().append(this).toFullyQualified();
    }

    public QualifiedName toName() {
        return createUnqualifiedName(getSegments().getLast());
    }

    public QualifiedName toNamespaceName(boolean fullyQualified) {
        LinkedList<String> list = new LinkedList<>(getSegments());
        list.removeLast();
        return new QualifiedName(fullyQualified, list);
    }

    public QualifiedName toNamespaceName() {
        return toNamespaceName(false);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QualifiedName other = (QualifiedName) obj;
        if (this.kind != other.kind) {
            return false;
        }
        if (this.segments.size() != other.segments.size()) {
            return false;
        }
        return this.segments.equals(other.segments);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + this.kind.hashCode();
        hash = 73 * hash + (this.segments != null ? this.segments.hashCode() : 0);
        return hash;
    }

    public boolean isDefaultNamespace() {
        return getSegments().size() == 1 && getSegments().get(0).equals(NamespaceDeclarationInfo.DEFAULT_NAMESPACE_NAME);
    }

}
