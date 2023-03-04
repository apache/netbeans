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
package org.netbeans.modules.php.editor.api;

import java.util.LinkedList;
import java.util.regex.Pattern;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.modules.php.editor.api.elements.AliasedElement;
import org.netbeans.modules.php.editor.api.elements.AliasedElement.Trait;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public class NameKind {

    private final QualifiedName query;
    private final QuerySupport.Kind queryKind;

    public static Exact forElement(final PhpElement element) {
        if (element instanceof FullyQualifiedElement) {
            return new Exact(((FullyQualifiedElement) element).getFullyQualifiedName());
        }
        return new Exact(QualifiedName.create(element.getName()));
    }

    public static Empty empty() {
        return new Empty(); //NOI18N
    }

    public static Exact exact(String query) {
        Parameters.notWhitespace("String query: can't be null or empty", query); //NOI18N
        return new Exact(query);
    }

    public static Prefix prefix(String query) {
        return new Prefix(query);
    }

    public static CaseInsensitivePrefix caseInsensitivePrefix(String query) {
        return new CaseInsensitivePrefix(query);
    }

    public static Exact exact(QualifiedName query) {
        return new Exact(query);
    }

    public static Prefix prefix(QualifiedName query) {
        return new Prefix(query);
    }

    public static CaseInsensitivePrefix caseInsensitivePrefix(QualifiedName query) {
        return new CaseInsensitivePrefix(query);
    }

    public static NameKind create(String query, Kind queryKind) {
        switch (queryKind) {
            case PREFIX:
                return new Prefix(query);
            case EXACT:
                Parameters.notWhitespace("String query: can't be null or empty", query); //NOI18N
                return new Exact(query);
            case CASE_INSENSITIVE_PREFIX:
                return new CaseInsensitivePrefix(query);
            default:
                //no-op
        }
        if (query == null || query.isEmpty()) {
            assert queryKind.equals(Kind.PREFIX) || queryKind.equals(Kind.CASE_INSENSITIVE_PREFIX) : queryKind.toString();
            return new Empty();
        }
        return new NameKind(query, queryKind);
    }

    public static NameKind create(QualifiedName query, Kind queryKind) {
        switch (queryKind) {
            case PREFIX:
                return new Prefix(query);
            case EXACT:
                return new Exact(query);
            case CASE_INSENSITIVE_PREFIX:
                return new CaseInsensitivePrefix(query);
            default:
                //no-op
        }
        return new NameKind(query, queryKind);
    }

    public boolean isPrefix() {
        return getQueryKind().equals(QuerySupport.Kind.PREFIX);
    }
    public boolean isExact() {
        return getQueryKind().equals(QuerySupport.Kind.EXACT);
    }
    public boolean isCaseInsensitivePrefix() {
        return getQueryKind().equals(QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);
    }
    public boolean isEmpty() {
        return getQueryName().isEmpty();
    }
    public boolean matchesName(final PhpElementKind elementKind, final QualifiedName name) {
        if (name != null && nameKindMatch(elementKind, name.getName(), queryKind, getQueryName())) {
            final QualifiedNameKind kindOfQuery = getQuery().getKind();
            if (kindOfQuery.isUnqualified()) {
                return true;
            }
            final LinkedList<String> nameSegments = name.toNamespaceName().getSegments();
            final LinkedList<String> querySegments = getQuery().toNamespaceName().getSegments();
            final int querySize = querySegments.size();
            final int nameSize = nameSegments.size();
            final int minSize = Math.min(nameSize, querySize);
            for (int i = 1; i <= minSize; i++) {
                String queryItem = querySegments.get(querySize - i);
                String nameItem = nameSegments.get(nameSize - i);
                if (!nameItem.equalsIgnoreCase(queryItem)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean matchesName(PhpElementKind elementKind, String name) {
        return name != null ? nameKindMatch(elementKind, name, queryKind, getQueryName()) : false;
    }

    public boolean matchesName(PhpElement element) {
        if (element instanceof FullyQualifiedElement) {
            FullyQualifiedElement fqe = (FullyQualifiedElement) element;
            if (fqe instanceof AliasedElement) {
                return matchesName(element.getPhpElementKind(), fqe.getFullyQualifiedName())
                        || matchesName(element.getPhpElementKind(), ((AliasedElement) fqe).getFullyQualifiedName(Trait.ALIAS));
            }
            return matchesName(element.getPhpElementKind(), fqe.getFullyQualifiedName());
        }
        return matchesName(element.getPhpElementKind(), element.getName());
    }

    public static boolean isCaseSensitive(PhpElementKind elementKind) {
        return isDollared(elementKind);
    }

    public static boolean isDollared(PhpElementKind elementKind) {
        return elementKind.equals(PhpElementKind.VARIABLE) || elementKind.equals(PhpElementKind.FIELD);
    }


    NameKind(String name, Kind kind) {
        this(QualifiedName.create(name), kind);
        Parameters.notNull("name", query); //NOI18N
    }

    NameKind(QualifiedName name, Kind kind) {
        this.query = name;
        this.queryKind = kind;
        Parameters.notNull("name", query); //NOI18N
    }

    public Kind getQueryKind() {
        return queryKind;
    }

    public QualifiedName getQuery() {
        return query;
    }

    public String getQueryName() {
        return query.getName();
    }

    @SuppressWarnings("fallthrough")
    private static boolean nameKindMatch(PhpElementKind elementKind, String nameToCheck, QuerySupport.Kind nameKind, String query) {
        boolean forceCaseInsensitivity = !isCaseSensitive(elementKind);
        boolean dollared = isDollared(elementKind);
        if (dollared) {
            nameToCheck = getName(nameToCheck, dollared);
            query = getName(query, dollared);
        }
        switch (nameKind) {
            //TODO: not reliably implemented for CASE_INSENSITIVE_CAMEL_CASE - needs review
            case CASE_INSENSITIVE_CAMEL_CASE:
                return camelCaseQueryToPattern(query.toUpperCase(), true).matcher(nameToCheck).matches();
            case CAMEL_CASE:
                return camelCaseQueryToPattern(query, false).matcher(nameToCheck).matches();
            case CASE_INSENSITIVE_REGEXP:
                return Pattern.compile(query, Pattern.CASE_INSENSITIVE).matcher(nameToCheck).matches();
            case REGEXP:
                return Pattern.compile(query).matcher(nameToCheck).matches();
            case CASE_INSENSITIVE_PREFIX:
                return nameToCheck.toLowerCase().startsWith(query.toLowerCase());
            case PREFIX:
                return (forceCaseInsensitivity)
                        ? nameToCheck.toLowerCase().startsWith(query.toLowerCase()) : nameToCheck.startsWith(query);
            case EXACT:
                return (forceCaseInsensitivity) ? nameToCheck.equalsIgnoreCase(query) : nameToCheck.equals(query);
            default:
                assert false : nameKind;
        }
        return false;
    }

    private static String getName(final String name, final boolean dollared) {
        final boolean startsWithDollar = isDollared(name);
        if (startsWithDollar == dollared) {
            return name;
        }
        return dollared ? String.format("%s%s", "$", name) : name.substring(1); //NOI18N
    }

    private static boolean isDollared(final String name) {
        return name.startsWith("$"); //NOI18N
    }

    private static Pattern camelCaseQueryToPattern(String query, boolean isCaseInsensitive) {
        StringBuilder sb = new StringBuilder();
        char[] chars = query.toCharArray();
        boolean incamel = false;
        if (!query.startsWith("$")) {
            sb.append("[$]*"); //NOI18N
        }
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '?') {
                sb.append('.');
            } else if (chars[i] == '*') {
                sb.append(".*"); //NOI18N
            } else if (Character.isUpperCase(chars[i])) {
                if (incamel) {
                    sb.append("[a-z0-9_]*"); //NOI18N
                }
                sb.append(chars[i]);
                incamel = true;
            } else if (i == 0 && chars[i] == '$') {
                sb.append('\\').append(chars[i]);
            } else {
                sb.append(Pattern.quote(String.valueOf(chars[i])));
            }
        }
        sb.append(".*"); //NOI18N
        String patternString = sb.toString();
        patternString = patternString.replaceAll(Pattern.quote(".."), "."); //NOI18N
        return isCaseInsensitive ? Pattern.compile(patternString, Pattern.CASE_INSENSITIVE)
                : Pattern.compile(patternString);
    }

    public static final class Exact extends NameKind {

        private Exact(String name) {
            super(name, Kind.EXACT);
        }

        private Exact(QualifiedName name) {
            super(name, Kind.EXACT);
        }
    }

    public static final class Prefix extends NameKind {

        private Prefix(String name) {
            super(name, Kind.PREFIX);
        }

        private Prefix(QualifiedName name) {
            super(name, Kind.PREFIX);
        }
    }

    public static final class CaseInsensitivePrefix extends NameKind {

        private CaseInsensitivePrefix(String name) {
            super(name, Kind.CASE_INSENSITIVE_PREFIX);
        }

        private CaseInsensitivePrefix(QualifiedName name) {
            super(name, Kind.CASE_INSENSITIVE_PREFIX);
        }
    }

    public static final class Empty extends NameKind {
        private Empty() {
            super("", Kind.PREFIX);
        }

    }
}
