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

package org.netbeans.modules.jumpto.common;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public class Utils {

    private static final int MAX_INPUT_LENGTH = 1<<10;
    private static final char[] INVALID_CHARS = {
        '\n'    //NOI18N
    };

    private Utils() {
        throw new IllegalStateException();
    }

    public static int containsWildCard( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            if ( text.charAt( i ) == '?' || text.charAt( i ) == '*' ) { // NOI18N
                return i;
            }
        }
        return -1;
    }

    public static boolean isAllUpper( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            if ( !Character.isUpperCase( text.charAt( i ) ) ) {
                return false;
            }
        }

        return true;
    }

    @NonNull
    public static SearchType getSearchType(
            @NonNull final String text,
            final boolean exact,
            final boolean isCaseSensitive,
            @NullAllowed final String camelCaseSeparator,
            @NullAllowed final String camelCasePart) {
        int wildcard = Utils.containsWildCard(text);
        if (exact) {
            //nameKind = isCaseSensitive ? SearchType.EXACT_NAME : SearchType.CASE_INSENSITIVE_EXACT_NAME;
            return SearchType.EXACT_NAME;
        } else if (wildcard != -1) {
            return isCaseSensitive ? SearchType.REGEXP : SearchType.CASE_INSENSITIVE_REGEXP;
        } else if ((Utils.isAllUpper(text) && text.length() > 1) || Queries.isCamelCase(text, camelCaseSeparator, camelCasePart)) {
            return isCaseSensitive ? SearchType.CAMEL_CASE : SearchType.CASE_INSENSITIVE_CAMEL_CASE;
        } else {
            return isCaseSensitive ? SearchType.PREFIX : SearchType.CASE_INSENSITIVE_PREFIX;
        }
    }

    @NonNull
    public static SearchType toSearchType(@NonNull final QuerySupport.Kind searchType) {
        switch (searchType) {
            case CAMEL_CASE:
                return org.netbeans.spi.jumpto.type.SearchType.CAMEL_CASE;
            case CASE_INSENSITIVE_CAMEL_CASE:
                return org.netbeans.spi.jumpto.type.SearchType.CASE_INSENSITIVE_CAMEL_CASE;
            case CASE_INSENSITIVE_PREFIX:
                return org.netbeans.spi.jumpto.type.SearchType.CASE_INSENSITIVE_PREFIX;
            case CASE_INSENSITIVE_REGEXP:
                return org.netbeans.spi.jumpto.type.SearchType.CASE_INSENSITIVE_REGEXP;
            case EXACT:
                return org.netbeans.spi.jumpto.type.SearchType.EXACT_NAME;
            case PREFIX:
                return org.netbeans.spi.jumpto.type.SearchType.PREFIX;
            case REGEXP:
                return org.netbeans.spi.jumpto.type.SearchType.REGEXP;
            default:
                throw new IllegalArgumentException(String.valueOf(searchType));
        }
    }

    @NonNull
    public static QuerySupport.Kind toQueryKind(@NonNull final SearchType searchType) {
        switch (searchType) {
            case CAMEL_CASE:
                return QuerySupport.Kind.CAMEL_CASE;
            case CASE_INSENSITIVE_CAMEL_CASE:
                return QuerySupport.Kind.CASE_INSENSITIVE_CAMEL_CASE;
            case CASE_INSENSITIVE_EXACT_NAME:
            case EXACT_NAME:
                return QuerySupport.Kind.EXACT;
            case CASE_INSENSITIVE_PREFIX:
                return QuerySupport.Kind.CASE_INSENSITIVE_PREFIX;
            case CASE_INSENSITIVE_REGEXP:
                return QuerySupport.Kind.CASE_INSENSITIVE_REGEXP;
            case PREFIX:
                return QuerySupport.Kind.PREFIX;
            case REGEXP:
                return QuerySupport.Kind.REGEXP;
            default:
                throw new IllegalThreadStateException(String.valueOf(searchType));
        }
    }

    @NonNull
    public static String removeNonNeededWildCards(@NonNull final String text) {
        final StringBuilder sb = new StringBuilder();
        boolean  lastAny = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '*':   //NOI18N
                    if (!lastAny) {
                        sb.append(c);
                    }
                    lastAny = true;
                    break;
                case '?':   //NOI18N
                    if (!lastAny) {
                        sb.append(c);
                    }
                    break;
                default:
                    sb.append(c);
                    lastAny = false;
            }
        }
        return sb.toString();
    }

    public static boolean isValidInput(@NonNull final String input) {
        if (input.length() > MAX_INPUT_LENGTH) {
            return false;
        }
        for (char c : INVALID_CHARS) {
            if (input.indexOf(c) >= 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNarrowing(
        @NonNull final SearchType origSearchType,
        @NonNull final SearchType newSearchType,
        @NonNull String origText,
        @NonNull String newText) {
        final boolean origCaseSensitive = isCaseSensitive(origSearchType);
        final boolean newCaseSensitive = isCaseSensitive(newSearchType);
        if (origCaseSensitive && !newCaseSensitive) {
            return false;
        }
        if (!newCaseSensitive) {
            origText = origText.toLowerCase();
            newText = newText.toLowerCase();
        }
        if (newText.startsWith(origText)) {
            return true;
        }
        //TODO: Regexp & CamelCase can add more rules
        return false;
    }

    /**
     * Splits a searched text into name and scope.
     * Todo: Create a SPI to allow Providers to do the splitting,
     * currently the splitting is hard coded. The split character is '.'
     * @param text to split
     * @return the pair of searched name and scope. The scope may be null.
     */
    @NonNull
    public static Pair<String,String> splitNameAndScope(@NonNull final String text) {
        final String name, scope;
        int index = text.lastIndexOf('.');    //NOI18N
        if (index >= 0) {
            scope = index == 0 ? null : text.substring(0, index);
            name = text.substring(index+1);
        } else {
            scope = null;
            name = text;
        }
        return Pair.of(name,scope);
    }

    public static boolean isCaseSensitive(@NonNull final SearchType searchType) {
        switch (searchType) {
            case CAMEL_CASE:
            case EXACT_NAME:
            case PREFIX:
            case REGEXP:
                return true;
            case CASE_INSENSITIVE_CAMEL_CASE:
            case CASE_INSENSITIVE_EXACT_NAME:
            case CASE_INSENSITIVE_PREFIX:
            case CASE_INSENSITIVE_REGEXP:
                return false;
            default:
                throw new IllegalArgumentException(String.valueOf(searchType));
        }
    }

}
