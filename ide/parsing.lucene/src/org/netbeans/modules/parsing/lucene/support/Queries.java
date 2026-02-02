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

package org.netbeans.modules.parsing.lucene.support;

import org.netbeans.modules.parsing.lucene.RegexpFilter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FieldExistsQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 * A factory class for creating queries and filed selectors
 * @author Tomas Zezula
 */
public final class Queries {

    /**
     * Configuration option for non standard camel case separator.
     * @since 2.31
     */
    public static final String OPTION_CAMEL_CASE_SEPARATOR = "camelCaseSeparator";  //NOI18N
    /**
     * Configuration option for non standard camel case part.
     * @since 2.31
     */
    public static final String OPTION_CAMEL_CASE_PART = "camelCasePart";    //NOI18N

    private static final String DEFAULT_CAMEL_CASE_SEPARATOR = "\\p{javaUpperCase}";                    //NOI18N
    private static final String DEFAULT_CAMEL_CASE_PART_CASE_SENSITIVE = "\\p{javaLowerCase}|\\p{Digit}|_|\\.|\\$";    //NOI18N
    private static final String DEFAULT_CAMEL_CASE_PART_CASE_INSENSITIVE = "\\p{javaLowerCase}|\\p{Digit}|\\p{javaUpperCase}|_|\\.|\\$";    //NOI18N
    private static final String CAMEL_CASE_FORMAT =
            //Anything followowed by part suffix - once
            "(.(?:%s)*)"+        //NOI18N
            //Separator followed by part - at least once
            "(?:(?:%s)(?:%s)*){1,}"; //NOI18N
    private static final Pattern DEFAULT_CAMEL_CASE_PATTERN = Pattern.compile(
            String.format(
                CAMEL_CASE_FORMAT,
                DEFAULT_CAMEL_CASE_PART_CASE_SENSITIVE,
                DEFAULT_CAMEL_CASE_SEPARATOR,
                DEFAULT_CAMEL_CASE_PART_CASE_SENSITIVE
                ));  //NOI18N

    private static volatile Pair<Pair<String,String>,Pattern> cache;

    /**
     * Encodes a type of the query used by {@link Queries#createQuery}
     * and {@link Queries#createTermCollectingQuery}
     */
    public enum QueryKind {
        /**
         * The created query looks for exact match with given text
         */
        EXACT,

        /**
         * The given text is a prefix of requested value.
         */
        PREFIX,

        /**
         * The given text is a case insensitive prefix of requested value.
         */
        CASE_INSENSITIVE_PREFIX,

        /**
         * The given text is treated as camel case pattern used to match the value.
         */
        CAMEL_CASE,

        /**
         * The given text is treated as case insensitive camel case pattern used to match the value.
         */
        CASE_INSENSITIVE_CAMEL_CASE,

        /**
         * The given text is a regular expression used to match the value.
         */
        REGEXP,

        /**
         * The given text is a case insensitive regular expression used to match the value.
         */
        CASE_INSENSITIVE_REGEXP;
    }

    /**
     * Creates a standard lucene query querying the index for
     * documents having indexed field containing the given value
     * @param fieldName the name of the field
     * @param caseInsensitiveFieldName the name of the field containing the case insensitive value
     * @param value the value to search for
     * @param kind the type of query, {@link Queries.QueryKind}
     * @return the created query
     */
    @NonNull
    public static Query createQuery (
            final @NonNull String fieldName,
            final @NonNull String caseInsensitiveFieldName,
            final @NonNull String value,
            final @NonNull QueryKind kind) {
        return createQuery(fieldName, caseInsensitiveFieldName, value, kind, Collections.<String,Object>emptyMap());
    }

    /**
     * Creates a standard lucene query querying the index for
     * documents having indexed field containing the given value
     * @param fieldName the name of the field
     * @param caseInsensitiveFieldName the name of the field containing the case insensitive value
     * @param value the value to search for
     * @param kind the type of query, {@link Queries.QueryKind}
     * @param options the query configuration options
     * @return the created query
     * @since 2.31
     */
    @NonNull
    public static Query createQuery (
            final @NonNull String fieldName,
            final @NonNull String caseInsensitiveFieldName,
            final @NonNull String value,
            final @NonNull QueryKind kind,
            final @NonNull Map<String,Object> options) {
        Parameters.notNull("fieldName", fieldName);     //NOI18N
        Parameters.notNull("caseInsensitiveFieldName", caseInsensitiveFieldName); //NOI18N
        Parameters.notNull("value", value); //NOI18N
        Parameters.notNull("kind", kind);   //NOI18N
        Parameters.notNull("options", options); //NOI18N
        return createQueryImpl(fieldName, caseInsensitiveFieldName, value, kind, new StandardQueryFactory(), options);
    }

    /**
     * Creates an extended lucene query querying the index for
     * documents having indexed field containing the given value.
     * This query is required by the {@link Index#queryDocTerms} method,
     * in addition to matching documents the query also collects the matched terms.
     * @param fieldName the name of the field
     * @param caseInsensitiveFieldName the name of the field containing the case insensitive value
     * @param value the value to search for
     * @param kind the type of query {@link Queries.QueryKind}
     * @return the created query
     */
    @NonNull
    public static Query createTermCollectingQuery(
            final @NonNull String fieldName,
            final @NonNull String caseInsensitiveFieldName,
            final @NonNull String value,
            final @NonNull QueryKind kind) {
        return createTermCollectingQuery(fieldName, caseInsensitiveFieldName, value, kind, Collections.<String,Object>emptyMap());
    }

    /**
     * Creates an extended lucene query querying the index for
     * documents having indexed field containing the given value.
     * This query is required by the {@link Index#queryDocTerms} method,
     * in addition to matching documents the query also collects the matched terms.
     * @param fieldName the name of the field
     * @param caseInsensitiveFieldName the name of the field containing the case insensitive value
     * @param value the value to search for
     * @param kind the type of query {@link Queries.QueryKind}
     * @param options the query configuration options
     * @return the created query
     * @since 2.31
     */
    @NonNull
    public static Query createTermCollectingQuery(
            final @NonNull String fieldName,
            final @NonNull String caseInsensitiveFieldName,
            final @NonNull String value,
            final @NonNull QueryKind kind,
            final @NonNull Map<String,Object> options) {
        Parameters.notNull("fieldName", fieldName);     //NOI18N
        Parameters.notNull("caseInsensitiveFieldName", caseInsensitiveFieldName); //NOI18N
        Parameters.notNull("value", value); //NOI18N
        Parameters.notNull("kind", kind);   //NOI18N
        Parameters.notNull("options", options); //NOI18N
        return createQueryImpl(fieldName, caseInsensitiveFieldName, value, kind, new StandardQueryFactory(), options);
    }

    /**
     * Creates a FieldSelector loading the given fields.
     * @param fieldsToLoad the fields to be loaded into the document.
     * @return the created FieldSelector
     */
    public static Set<String> createFieldSelector(final @NonNull String... fieldsToLoad) {
        return new HashSet<>(List.of(fieldsToLoad));
    }

    /**
     * Tests if given value is a camel case string.
     * Utility method to test if the value is a camel case string.
     * @param value the value to be checked
     * @param separatorRegExp the optional camel case separator pattern.
     * When null the default (upper cased letter) is used.
     * @param partRegExp the optional camel case part pattern.
     * When null the default (digit lower cased letter '.', '_','$') is used.
     * @return true if the value is a camel case string.
     * @since 2.31
     */
    public static boolean isCamelCase(
            @NonNull final String value,
            @NullAllowed String separatorRegExp,
            @NullAllowed String partRegExp) {
        if (separatorRegExp == null && partRegExp == null) {
            return DEFAULT_CAMEL_CASE_PATTERN.matcher(value).matches();
        } else {
            Pattern p;
            Pair<Pair<String,String>,Pattern> val = cache;
            if (val != null && Objects.equals(separatorRegExp, val.first().first()) && Objects.equals(partRegExp, val.first().second())) {
                p = val.second();
            } else {
                if (separatorRegExp == null) {
                    separatorRegExp = DEFAULT_CAMEL_CASE_SEPARATOR;
                }
                if (partRegExp == null) {
                    partRegExp = DEFAULT_CAMEL_CASE_PART_CASE_SENSITIVE;
                }
                p = Pattern.compile(String.format(
                        CAMEL_CASE_FORMAT,
                        partRegExp,
                        separatorRegExp,
                        partRegExp));
                cache = Pair.of(Pair.of(separatorRegExp,partRegExp), p);
            }
            return p.matcher(value).matches();
        }
    }

    /**
     * Creates camel case regular expression.
     * @param value the value to create regular expression from
     * @param separatorRegExp the optional camel case separator pattern.
     * When null the default (upper cased letter) is used.
     * @param partRegExp the optional camel case part pattern.
     * When null the default (digit lower cased letter '.', '_','$') is used.
     * @param caseSensitive true for case sensitive search
     * @return The camel case regular expression
     * @since 2.31
     */
    @NonNull
    public static String createCamelCaseRegExp(
            @NonNull final String value,
            @NullAllowed final String separatorRegExp,
            @NullAllowed final String partRegExp,
            final boolean caseSensitive) {
        final StringBuilder sb = new StringBuilder();
        final Pattern separator = separatorRegExp == null ? null : Pattern.compile(separatorRegExp);
        final String part = String.format(
                "(%s)*",    //NOI18N
                partRegExp == null ?
                    (caseSensitive ? DEFAULT_CAMEL_CASE_PART_CASE_SENSITIVE : DEFAULT_CAMEL_CASE_PART_CASE_INSENSITIVE) :
                    partRegExp);
        int lastIndex = 0;
        int index;
        do {
            index = separator == null ? findNextUpper(value, lastIndex + 1) : findNextSeparator(value, lastIndex + 1 , separator);
            String token = value.substring(lastIndex, index == -1 ? value.length(): index);
            sb.append(Pattern.quote(caseSensitive ? token : token.toLowerCase()));
            sb.append( index != -1 ?  part : ".*"); // NOI18N
            lastIndex = index;
        } while(index != -1);
        return sb.toString();
    }


    // <editor-fold defaultstate="collapsed" desc="Private implementation">
    private static Query createQueryImpl(
            @NonNull String fieldName,
            @NonNull String caseInsensitiveFieldName,
            @NonNull String value,
            @NonNull QueryKind kind,
            @NonNull QueryFactory f,
            @NonNull Map<String,Object> options) {

        return switch (kind) {
            case EXACT -> f.createTermQuery(fieldName, value);
            case PREFIX ->
                value.isEmpty()
                    ? f.createAllDocsQuery(fieldName)
                    : f.createPrefixQuery(fieldName, value);
            case CASE_INSENSITIVE_PREFIX ->
                value.isEmpty()
                    ? f.createAllDocsQuery(caseInsensitiveFieldName)
                    : f.createPrefixQuery(caseInsensitiveFieldName, value.toLowerCase());
            case CAMEL_CASE -> {
                if (value.isEmpty()) {
                    throw new IllegalArgumentException ();
                } else {
                    yield f.createRegExpQuery(fieldName,createCamelCaseRegExp(value, getOption(options, OPTION_CAMEL_CASE_SEPARATOR, String.class), getOption(options, OPTION_CAMEL_CASE_PART, String.class), true), true);
                }
            }
            case CASE_INSENSITIVE_REGEXP -> {
                if (value.isEmpty()) {
                    throw new IllegalArgumentException ();
                } else {
                    yield f.createRegExpQuery(caseInsensitiveFieldName, value.toLowerCase(), false);
                }
            }
            case REGEXP -> {
                if (value.isEmpty()) {
                    throw new IllegalArgumentException ();
                } else {
                    yield f.createRegExpQuery(fieldName, value, true);
                }
            }
            case CASE_INSENSITIVE_CAMEL_CASE -> 
                value.isEmpty() 
                        ? f.createAllDocsQuery(caseInsensitiveFieldName) //Special case (all) handle in different way
                        : f.createRegExpQuery(caseInsensitiveFieldName, createCamelCaseRegExp(value, getOption(options, OPTION_CAMEL_CASE_SEPARATOR, String.class), getOption(options, OPTION_CAMEL_CASE_PART, String.class), false), false);
            default -> throw new UnsupportedOperationException(kind.toString());
        };
    }

    @CheckForNull
    private static <T> T getOption(
            @NonNull final Map<String,Object> options,
            @NonNull final String key,
            @NonNull final Class<T> clz) {
        final Object val = options.get(key);
        return clz.isInstance(val) ? clz.cast(val) : null;
    }

    private static int findNextUpper(String text, int offset ) {
        for( int i = offset; i < text.length(); i++ ) {
            if ( Character.isUpperCase(text.charAt(i)) ) {
                return i;
            }
        }
        return -1;
    }

    private static int findNextSeparator(
            @NonNull final String text,
            final int offset,
            @NonNull final Pattern separator) {
        Matcher m = separator.matcher(text);
        if (m.find(offset)) {
            return m.start();
        }
        return -1;
    }

    private static interface QueryFactory {
        Query createTermQuery(@NonNull String name, @NonNull String value);
        Query createPrefixQuery(@NonNull String name, @NonNull String value);
        Query createRegExpQuery(@NonNull String name, @NonNull String value, boolean caseSensitive);
        Query createAllDocsQuery(@NonNull String name);
    }

    private static class StandardQueryFactory implements QueryFactory {

        @Override
        public Query createTermQuery(final @NonNull String name, final @NonNull String value) {
            return new TermQuery(new Term (name, value));
        }

        @Override
        public Query createPrefixQuery(final @NonNull String name, final @NonNull String value) {
            final PrefixQuery pq = new PrefixQuery(new Term(name, value), PrefixQuery.CONSTANT_SCORE_REWRITE);
            return pq;
        }

        @Override
        public Query createRegExpQuery(final @NonNull String name, final @NonNull String value, final boolean caseSensitive) {
            return new RegexpFilter(name, value, caseSensitive);
        }

        @Override
        public Query createAllDocsQuery(final @NonNull String name) {
            if (name.isEmpty()) {
                return new MatchAllDocsQuery();
            } else {
                return new FieldExistsQuery(name);
            }
        }

    }

    private Queries() {}
    //</editor-fold>

}
