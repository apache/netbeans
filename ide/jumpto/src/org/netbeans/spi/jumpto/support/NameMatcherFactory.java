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
package org.netbeans.spi.jumpto.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.util.Parameters;

/**
 * A factory that provides comparators
 * depending on SearchType
 *
 * @author Vladimir Kvashin
 */
public final class NameMatcherFactory {

    private static final Map<Character,String> RE_SPECIALS;
    static {
        final Map<Character,String> m = new HashMap<>();
        m.put('{',"\\{");         //NOI18N
        m.put('}',"\\}");         //NOI18N
        m.put('[',"\\[");         //NOI18N
        m.put(']',"\\]");         //NOI18N
        m.put('(',"\\(");         //NOI18N
        m.put(')',"\\)");         //NOI18N
        m.put('\\',"\\\\");       //NOI18N
        m.put('.', "\\.");        //NOI18N
        m.put('+',"\\+");         //NOI18N
        m.put('*', ".*" );        //NOI18N
        m.put('?', ".");          //NOI18N
        RE_SPECIALS = Collections.unmodifiableMap(m);
    }

    private NameMatcherFactory() {
    }

    private abstract static class BaseNameMatcher implements NameMatcher {

        protected final String patternText;

	protected BaseNameMatcher(String patternText) {
	    this.patternText = patternText;
	}
    }

    private static final class ExactNameMatcher extends BaseNameMatcher {

	public ExactNameMatcher(String patternText) {
	    super(patternText);
	}

        @Override
	public final boolean accept(String name) {
	    return patternText.equals(name);
	}
    }

    private static final class CaseInsensitiveExactNameMatcher extends BaseNameMatcher {

	public CaseInsensitiveExactNameMatcher(String patternText) {
	    super(patternText);
	}

        @Override
	public final boolean accept(String name) {
	    return patternText.equalsIgnoreCase(name);
	}
    }

    private static final class PrefixNameMatcher extends BaseNameMatcher {

	public PrefixNameMatcher(String patternText) {
	    super(patternText);
	}

        @Override
	public final boolean accept(String name) {
	    return name != null && name.startsWith(patternText);
	}
    }

    private static final class CaseInsensitivePrefixNameMatcher extends BaseNameMatcher {

	public CaseInsensitivePrefixNameMatcher(String patternText) {
	    super(patternText.toLowerCase());
	}

        @Override
	public final boolean accept(String name) {
	    return name != null && name.toLowerCase().startsWith(patternText);
	}
    }

    private static final class RegExpNameMatcher implements NameMatcher {

	private final Pattern pattern;

	public RegExpNameMatcher(String patternText, boolean caseSensitive) {
	    pattern = Pattern.compile(patternText, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
	}

        @Override
	public final boolean accept(String name) {
	    return name != null && pattern.matcher(name).matches();
	}
    }

    private static final class CamelCaseNameMatcher implements NameMatcher {

	private final Pattern pattern;

	public CamelCaseNameMatcher(
                @NonNull final String name,
                final boolean caseSensitive,
                @NonNull final Map<String,Object> options) {
            if (name.length() == 0) {
                throw new IllegalArgumentException ();
            }
            pattern = Pattern.compile(
                    Queries.createCamelCaseRegExp(
                            name,
                            getOption(options, Queries.OPTION_CAMEL_CASE_SEPARATOR, String.class),
                            getOption(options, Queries.OPTION_CAMEL_CASE_PART, String.class),
                            caseSensitive),
                    caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
	}

        @Override
	public final boolean accept(String name) {
	    return name != null && pattern.matcher(name).matches();
	}
    }

    /**
     * Creates a {@link NameMatcher} of given type for text.
     * @param text to create {@link NameMatcher} for
     * @param type of {@link NameMatcher}
     * @return a {@link NameMatcher}
     * @throws IllegalArgumentException when called with unsupported type or
     * regular expression for given text failed.
     */
    @NonNull
    public static NameMatcher createNameMatcher(@NonNull final String text, @NonNull final SearchType type) throws IllegalArgumentException {
        return createNameMatcher(text, type, Collections.<String,Object>emptyMap());
    }

    /**
     * Creates a {@link NameMatcher} of given type for text.
     * @param text to create {@link NameMatcher} for
     * @param type of {@link NameMatcher}
     * @param options the matcher configuration options, see {@link Queries} options
     * @return a {@link NameMatcher}
     * @throws IllegalArgumentException when called with unsupported type or
     * regular expression for given text failed.
     * @since 1.46
     */
    @NonNull
    public static NameMatcher createNameMatcher(
            @NonNull final String text,
            @NonNull final SearchType type,
            @NonNull final Map<String,Object> options) throws IllegalArgumentException {
        Parameters.notNull("text", text);   //NOI18N
        Parameters.notNull("type", type);   //NOI18N
        Parameters.notNull("options", options); //NOI18N
        try {
            switch( type ) {
                case EXACT_NAME:
                    return new ExactNameMatcher(text);
                case CASE_INSENSITIVE_EXACT_NAME:
                    return new CaseInsensitiveExactNameMatcher(text);
                case PREFIX:
                    return new PrefixNameMatcher(text);
                case REGEXP:
                    return new RegExpNameMatcher(wildcardsToRegexp(text, true), true);
                case CASE_INSENSITIVE_REGEXP:
                    return new RegExpNameMatcher(wildcardsToRegexp(text, true), false);
                case CASE_INSENSITIVE_PREFIX:
                     return new CaseInsensitivePrefixNameMatcher(text);
                case CAMEL_CASE:
                    return new CamelCaseNameMatcher(text, true, options);
                case CASE_INSENSITIVE_CAMEL_CASE:
                    return new CamelCaseNameMatcher(text, false, options);
                default:
                    throw new IllegalArgumentException("Unsupported type: " + type);  //NOI18N
            }
        }
        catch( PatternSyntaxException ex ) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Translates the wildcard pattern into regexp
     * @param pattern the wildcard pattern to be translated into regexp
     * @param prefix if true the pattern is extended by *
     * @return the regular expression
     * @since 1.20
     */
    public static String wildcardsToRegexp(final String pattern, boolean prefix) {
        final StringBuilder res = new StringBuilder();
        for (int i = 0; i< pattern.length(); i++) {
            final char c = pattern.charAt(i);
            final String r = RE_SPECIALS.get(c);
            if (r != null) {
                res.append(r);
            } else {
                res.append(c);
            }
        }
        if (prefix) {
            res.append(".*");   //NOI18N
        }
        return res.toString();
    }

    @CheckForNull
    private static <T> T getOption(
            @NonNull final Map<String,Object> options,
            @NonNull final String key,
            @NonNull final Class<T> clz) {
        final Object val = options.get(key);
        return clz.isInstance(val) ? clz.cast(val) : null;
    }
}
