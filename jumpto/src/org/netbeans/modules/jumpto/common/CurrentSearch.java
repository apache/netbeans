/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jumpto.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class CurrentSearch<T> {

    private final Callable<AbstractModelFilter<T>> filterFactory;

    //@GuardedBy("this")
    private String currentText;
    //@GuardedBy("this")
    private SearchType currentType;
    //@GuardedBy("this")
    private String currentScope;
    //@GuardedBy("this")
    private AbstractModelFilter<T> filter;

    private final Map</*@GuardedBy("this")*/Class<?>,Object> attrs;

    public CurrentSearch(@NonNull final Callable<AbstractModelFilter<T>> filterFactory) {
        Parameters.notNull("filterFactory", filterFactory); //NOI18N
        this.filterFactory = filterFactory;
        this.attrs = new HashMap<>();
        resetFilter();
    }

    public synchronized boolean isNarrowing(
            @NonNull final SearchType searchType,
            @NonNull final String searchText,
            @NullAllowed final String searchScope,
            final boolean correctCase) {
        if (currentType == null || currentText == null) {
            return false;
        }
        return Objects.equals(currentScope, searchScope) &&
            (correctCase || Utils.isCaseSensitive(currentType) == Utils.isCaseSensitive(searchType)) &&
            Utils.isNarrowing(
                currentType,
                searchType,
                currentText,
                searchText);
    }

    public synchronized void filter(
            @NonNull final SearchType searchType,
            @NonNull final String searchText,
            @NullAllowed Map<String,Object> options) {
        this.filter.configure(searchType, searchText, options);
    }

    @NonNull
    public synchronized Models.Filter<T> resetFilter() {
        this.currentType = null;
        this.currentText = null;
        try {
            this.filter = filterFactory.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this.filter;
    }

    public synchronized void searchCompleted(
            @NonNull final SearchType searchType,
            @NonNull final String searchText,
            @NullAllowed final String searchScope) {
        this.currentType = searchType;
        this.currentText = searchText;
        this.currentScope = searchScope;
    }

    @CheckForNull
    public synchronized <T> T setAttribute(@NonNull final Class<T> clz, @NullAllowed final T instance) {
        Parameters.notNull("cls", clz); //NOI18N
        if (instance == null) {
            return clz.cast(attrs.remove(clz));
        }
        return clz.cast(attrs.put(clz, instance));
    }

    @CheckForNull
    public synchronized <T> T getAttribute(@NonNull final Class<T> clz) {
        Parameters.notNull("clz", clz); //NOI18N
        return clz.cast(attrs.get(clz));
    }
}
