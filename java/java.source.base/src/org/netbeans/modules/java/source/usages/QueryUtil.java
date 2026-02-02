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

package org.netbeans.modules.java.source.usages;

import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.BytesRef;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClassIndex.SearchScopeType;
import org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.openide.util.Pair;
import org.openide.util.Parameters;


/**
 *
 * @author Tomas Zezula
 */
class QueryUtil {
        
    
    
    static Query createUsagesQuery(
            final @NonNull String resourceName,
            final @NonNull Set<? extends ClassIndexImpl.UsageType> mask,
            final @NonNull Occur operator) {
        Parameters.notNull("resourceName", resourceName);
        Parameters.notNull("mask", mask);
        Parameters.notNull("operator", operator);
        if (operator == Occur.SHOULD) {
            final BooleanQuery.Builder query = new BooleanQuery.Builder ();
            for (ClassIndexImpl.UsageType ut : mask) {
                final Query subQuery = new WildcardQuery(
                    DocumentUtil.referencesTerm (
                        resourceName,
                        EnumSet.of(ut),
                        false));
                query.add(subQuery, operator);
            }
            return query.build();
        } else if (operator == Occur.MUST) {
            return new WildcardQuery(
                DocumentUtil.referencesTerm (
                    resourceName,
                    mask,
                    false));
        } else {
            throw new IllegalArgumentException();
        }
    }

    @NonNull
    static Query createPackageUsagesQuery (
            @NonNull final String packageName,
            @NonNull final Set<? extends UsageType> mask,
            @NonNull Occur operator) {
        Parameters.notNull("packageName", packageName); //NOI18N
        Parameters.notNull("mask", mask); //NOI18N
        final String pattern = Pattern.quote(packageName) + "\\.[^\\.]+";   //NOI18N
        if (operator == Occur.SHOULD) {
            final BooleanQuery.Builder query = new BooleanQuery.Builder();
            for (ClassIndexImpl.UsageType ut : mask) {
                final Term t = DocumentUtil.referencesTerm (
                        pattern,
                        EnumSet.of(ut),
                        true);
                query.add(Queries.createQuery(t.field(), t.field(), t.text(), Queries.QueryKind.REGEXP), operator);
            }
            return query.build();
        } else if (operator == Occur.MUST) {
            final Term t = DocumentUtil.referencesTerm (
                    pattern,
                    mask,
                    true);
            return Queries.createQuery(t.field(), t.field(), t.text(), Queries.QueryKind.REGEXP);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @CheckForNull
    static Query scopeFilter (
            @NonNull final Query q,
            @NonNull final Set<? extends SearchScopeType> scope) {
        assert q != null;
        assert scope != null;
        TreeSet<String> pkgs = null;
        for (SearchScopeType s : scope) {
            final Set<? extends String> sp = s.getPackages();
            if (sp != null) {
                if (pkgs == null) {
                    pkgs = new TreeSet<>();
                }
                pkgs.addAll(sp);
            }
        }
        if (pkgs == null) {
            return q;
        }
        if(pkgs.isEmpty()) {
            return null;
        }

        final BooleanQuery.Builder qFiltered = new BooleanQuery.Builder();
        qFiltered.add(
            new TermInSetQuery(
                    DocumentUtil.FIELD_PACKAGE_NAME,
                    pkgs.stream().map(s -> new BytesRef(s)).collect(Collectors.toList())
            ),
            Occur.MUST);
        qFiltered.add(q, Occur.MUST);
        return qFiltered.build();

    }

    static Pair<Convertor<BytesRef,String>,String> createPackageFilter(
            final @NullAllowed String prefix,
            final boolean directOnly) {
        final Convertor<BytesRef, String> filter = new PackageFilter(prefix, directOnly);
        return Pair.of(filter, prefix);
    }

    // <editor-fold defaultstate="collapsed" desc="Private implementation">
                            
                                    
    private static final class PackageFilter implements Convertor<BytesRef, String> {
        
        private final boolean directOnly;
        private final boolean all;
        private final String value;
        
        PackageFilter(final String value, final boolean directOnly) {
            this.value = value;
            this.directOnly = directOnly;
            this.all = value.length() == 0;
        }
        
        @Override
        public String convert(BytesRef currentTerm) {
            String currentText = currentTerm.utf8ToString();
            if (all || currentText.startsWith(value)) {
                if (directOnly) {
                    int index = currentText.indexOf('.', value.length());    //NOI18N
                    if (index>0) {
                        currentText = currentText.substring(0,index);
                    }
                }
                return currentText;
            }
            return null;
        }
    }

    //</editor-fold>
}
