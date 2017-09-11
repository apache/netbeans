/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
