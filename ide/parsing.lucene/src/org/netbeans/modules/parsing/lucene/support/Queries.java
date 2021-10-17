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

import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.PrefixTermEnum;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.OpenBitSet;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.lucene.TermCollector;
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
        return createQueryImpl(fieldName, caseInsensitiveFieldName, value, kind, new TCQueryFactory(), options);
    }

    /**
     * Creates a FieldSelector loading the given fields.
     * @param fieldsToLoad the fields to be loaded into the document.
     * @return the created FieldSelector
     */
    public static FieldSelector createFieldSelector(final @NonNull String... fieldsToLoad) {
        return new FieldSelectorImpl(fieldsToLoad);
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
            final @NonNull String fieldName,
            final @NonNull String caseInsensitiveFieldName,
            final @NonNull String value,
            final @NonNull QueryKind kind,
            final @NonNull QueryFactory f,
            final @NonNull Map<String,Object> options) {
        switch (kind) {
            case EXACT:
                    return f.createTermQuery(fieldName, value);
            case PREFIX:
                if (value.length() == 0) {
                    return f.createAllDocsQuery(fieldName);
                }
                else {
                    return f.createPrefixQuery(fieldName, value);
                }
            case CASE_INSENSITIVE_PREFIX:
                if (value.length() == 0) {
                    return f.createAllDocsQuery(caseInsensitiveFieldName);
                }
                else {
                    return f.createPrefixQuery(caseInsensitiveFieldName, value.toLowerCase());
                }
            case CAMEL_CASE:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                } else {
                    return f.createRegExpQuery(fieldName,createCamelCaseRegExp(value, getOption(options, OPTION_CAMEL_CASE_SEPARATOR, String.class), getOption(options, OPTION_CAMEL_CASE_PART, String.class), true), true);
                }
            case CASE_INSENSITIVE_REGEXP:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                } else {
                    return f.createRegExpQuery(caseInsensitiveFieldName, value.toLowerCase(), false);
                }
            case REGEXP:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                } else {
                    return f.createRegExpQuery(fieldName, value, true);
                }
            case CASE_INSENSITIVE_CAMEL_CASE:
                if (value.length() == 0) {
                    //Special case (all) handle in different way
                    return f.createAllDocsQuery(caseInsensitiveFieldName);
                } else {
                    return f.createRegExpQuery(caseInsensitiveFieldName, createCamelCaseRegExp(value, getOption(options, OPTION_CAMEL_CASE_SEPARATOR, String.class), getOption(options, OPTION_CAMEL_CASE_PART, String.class), false), false);
                }
            default:
                throw new UnsupportedOperationException (kind.toString());
        }
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
        Matcher m;
        if ((m = separator.matcher(text)).find(offset)) {
            return m.start();
        }
        return -1;
    }

    private static abstract class TCFilter extends Filter {
        public abstract void attach (TermCollector collector);
    }

    private static abstract class AbstractTCFilter extends TCFilter {

        private  TermCollector termCollector;

        @Override
        public final DocIdSet getDocIdSet(IndexReader reader) throws IOException {
            final FilteredTermEnum enumerator = getTermEnum(reader);
            // if current term in enum is null, the enum is empty -> shortcut
            if (enumerator.term() == null) {
                return DocIdSet.EMPTY_DOCIDSET;
            }
            try {
                // else fill into a OpenBitSet
                final OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
                final int[] docs = new int[32];
                final int[] freqs = new int[32];
                final TermDocs termDocs = reader.termDocs();
                try {
                    do {
                        final Term term = enumerator.term();
                        if (term == null) {
                            break;
                        }
                        termDocs.seek(term);
                        while (true) {
                            final int count = termDocs.read(docs, freqs);
                            if (count != 0) {
                                for (int i = 0; i < count; i++) {
                                    bitSet.set(docs[i]);
                                    if (termCollector != null) {
                                        termCollector.add(docs[i], term);
                                    }
                                }
                            } else {
                                break;
                            }
                        }
                    } while (enumerator.next());
                } finally {
                    termDocs.close();
                }
                return bitSet;
            } finally {
                enumerator.close();
            }
        }

        @Override
        public final void attach(final TermCollector tc) {
            this.termCollector = tc;
        }

        protected abstract FilteredTermEnum getTermEnum(IndexReader reader) throws IOException;

    }

    private static class RegexpTermEnum extends FilteredTermEnum {

        private final String fieldName;
        private final String startPrefix;
        private final Pattern pattern;
        private boolean endEnum;

        public RegexpTermEnum(
                final IndexReader in,
                final String  fieldName,
                final Pattern pattern,
                final String  startPrefix) throws IOException {
            final Term term = new Term(fieldName,startPrefix);
            this.fieldName = term.field();
            this.pattern = pattern;
            this.startPrefix = startPrefix;
            setEnum(in.terms(term));
        }

        @Override
        protected boolean termCompare(Term term) {
            if (fieldName == term.field()) {
                String searchText = term.text();
                if (searchText.startsWith(startPrefix)) {
                    return pattern.matcher(term.text()).matches();
                }
            }
            endEnum = true;
            return false;
        }

        @Override
        public float difference() {
            return 1.0f;
        }

        @Override
        protected boolean endEnum() {
            return endEnum;
        }
    }

    static class RegexpFilter extends AbstractTCFilter {
        private static final BitSet SPECIAL_CHARS = new BitSet(126);
        static {
            final char[] specials = new char[] {'{','}','[',']','(',')','\\','.','*','?', '+'}; //NOI18N
            for (char c : specials) {
                SPECIAL_CHARS.set(c);
            }
        }
        private static final BitSet QUANTIFIER_CHARS = new BitSet(126);
        static {
            final char[] specials = new char[] {'{','*','?'}; //NOI18N
            for (char c : specials) {
                QUANTIFIER_CHARS.set(c);
            }
        }

        private final String fieldName;
        private final String startPrefix;
        private final Pattern pattern;

        public RegexpFilter(final String fieldName, final String  regexp, final boolean caseSensitive) {
            this.fieldName = fieldName;
            this.pattern = caseSensitive ? Pattern.compile(regexp) : Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
            this.startPrefix = getStartText(regexp);
        }

        protected FilteredTermEnum getTermEnum(final @NonNull IndexReader reader) throws IOException {
            return new RegexpTermEnum(reader, fieldName, pattern, startPrefix);
        }

        private static String getStartText(final String regexp) {
            final StringBuilder startBuilder = new StringBuilder ();
            boolean quoted = false;
            for (int i = 0; i < regexp.length(); i++) {
                char c = regexp.charAt(i);
                if ((!quoted) && i < (regexp.length() - 1)) {
                    char lookAhead = regexp.charAt(i + 1);
                    if (QUANTIFIER_CHARS.get(lookAhead)) {
                        break;
                    }
                }
                if (c == '\\' && (i+1) < regexp.length()) { //NOI18N
                    char cn = regexp.charAt(i+1);
                    if (!quoted && cn == 'Q') { //NOI18N
                        quoted = true;
                        i++;
                        continue;
                    } else if (cn == 'E') { //NOI18N
                        quoted = false;
                        i++;
                        continue;
                    }
                } else if (!quoted && (c == '^' || c == '$')) { //NOI18N
                    continue;
                }
                if (!quoted && SPECIAL_CHARS.get(c)) {
                    break;
                }
                startBuilder.append(c);
            }
            return startBuilder.toString();
        }
    }

    private static class PrefixFilter extends AbstractTCFilter {

        protected final Term term;

        public PrefixFilter(final @NonNull String fieldName, final @NonNull String prefix) {
            this.term = new Term(fieldName, prefix);
        }

        protected FilteredTermEnum getTermEnum(final @NonNull IndexReader reader) throws IOException {
            return new PrefixTermEnum(reader, term);
        }
    }

    private static class TermFilter extends PrefixFilter {

        public TermFilter (final String fieldName, final String value) {
            super(fieldName, value);
        }

        @Override
        protected FilteredTermEnum getTermEnum(IndexReader reader) throws IOException {
            return new PrefixTermEnum(reader, term) {

                private boolean endEnum;

                @Override
                protected boolean termCompare(Term term) {
                    if (TermFilter.this.term.field() == term.field() && TermFilter.this.term.text().equals(term.text())) {
                        return true;
                    }
                    endEnum = true;
                    return false;
                }

                @Override
                protected boolean endEnum() {
                    return endEnum;
                }



            };
        }
    }

    private static class HasFieldFilter extends PrefixFilter {

        public HasFieldFilter (final String fieldName) {
            super (fieldName, "");  //NOI18N
        }

        @Override
        protected FilteredTermEnum getTermEnum(IndexReader reader) throws IOException {
            return new PrefixTermEnum(reader, term) {

                private boolean endEnum;

                @Override
                protected boolean termCompare(Term term) {
                    if (HasFieldFilter.this.term.field() == term.field()) {
                        return true;
                    }
                    endEnum = true;
                    return false;
                }

                @Override
                protected boolean endEnum() {
                    return endEnum;
                }
            };
        }
    }

    private static class TCFilteredQuery extends FilteredQuery implements TermCollector.TermCollecting {
        private TCFilteredQuery(final Query query, final TCFilter filter) {
            super (query, filter);
        }

        @Override
        public void attach(TermCollector collector) {
            ((TCFilter)getFilter()).attach(collector);
        }
    }

    private static class TCBooleanQuery extends BooleanQuery implements TermCollector.TermCollecting {

        private TermCollector collector;

        @Override
        public void attach(TermCollector collector) {
            this.collector = collector;
            if (this.collector != null) {
                attach(this, this.collector);
            }
        }

        @Override
        public Query rewrite(IndexReader reader) throws IOException {
            final Query result =  super.rewrite(reader);
            if (this.collector != null) {
                attach(this,this.collector);
            }
            return result;
        }

        private static void attach (final BooleanQuery query, final TermCollector collector) {
            for (BooleanClause clause : query.getClauses()) {
                final Query q = clause.getQuery();
                if (!(q instanceof TermCollector.TermCollecting)) {
                    throw new IllegalArgumentException();
                }
                ((TermCollector.TermCollecting)q).attach(collector);
            }
        }
    }

    private static interface QueryFactory {
        Query createTermQuery(@NonNull String name, @NonNull String value);
        Query createPrefixQuery(@NonNull String name, @NonNull String value);
        Query createRegExpQuery(@NonNull String name, @NonNull String value, boolean caseSensitive);
        Query createAllDocsQuery(@NonNull String name);
        BooleanQuery createBooleanQuery();
    }

    private static class StandardQueryFactory implements QueryFactory {

        @Override
        public Query createTermQuery(final @NonNull String name, final @NonNull String value) {
            return new TermQuery(new Term (name, value));
        }

        @Override
        public Query createPrefixQuery(final @NonNull String name, final @NonNull String value) {
            final PrefixQuery pq = new PrefixQuery(new Term(name, value));
            pq.setRewriteMethod(PrefixQuery.CONSTANT_SCORE_FILTER_REWRITE);
            return pq;
        }

        @Override
        public Query createRegExpQuery(final @NonNull String name, final @NonNull String value, final boolean caseSensitive) {
            return new FilteredQuery(new MatchAllDocsQuery(), new RegexpFilter(name, value, caseSensitive));
        }

        @Override
        public Query createAllDocsQuery(final @NonNull String name) {
            if (name.length() == 0) {
                return new MatchAllDocsQuery();
            } else {
                return new FilteredQuery(new MatchAllDocsQuery(), new HasFieldFilter(name));
            }
        }

        @Override
        public BooleanQuery createBooleanQuery() {
            return new BooleanQuery();
        }
    }

    private static class TCQueryFactory implements QueryFactory {

        @Override
        public Query createTermQuery(final @NonNull String name, final @NonNull String value) {
            return new TCFilteredQuery(new MatchAllDocsQuery(), new TermFilter(name,value));
        }

        @Override
        public Query createPrefixQuery(final @NonNull String name, final @NonNull String value) {
            return new TCFilteredQuery(new MatchAllDocsQuery(), new PrefixFilter(name, value));
        }

        @Override
        public Query createRegExpQuery(final @NonNull String name, final @NonNull String value, final boolean caseSensitive) {
            return new TCFilteredQuery(new MatchAllDocsQuery(), new RegexpFilter(name, value, caseSensitive));
        }

        @Override
        public Query createAllDocsQuery(final @NonNull String name) {
            throw new IllegalArgumentException ();
        }

        @Override
        public BooleanQuery createBooleanQuery() {
            return new TCBooleanQuery();
        }
    }

    private static class FieldSelectorImpl implements FieldSelector {

        private final Term[] terms;

        FieldSelectorImpl(String... fieldNames) {
            terms = new Term[fieldNames.length];
            for (int i=0; i< fieldNames.length; i++) {
                terms[i] = new Term (fieldNames[i],""); //NOI18N
            }
        }

        @Override
        public FieldSelectorResult accept(String fieldName) {
            for (Term t : terms) {
                if (fieldName == t.field()) {
                    return FieldSelectorResult.LOAD;
                }
            }
            return FieldSelectorResult.NO_LOAD;
        }
    }

    private Queries() {}
    //</editor-fold>

}
