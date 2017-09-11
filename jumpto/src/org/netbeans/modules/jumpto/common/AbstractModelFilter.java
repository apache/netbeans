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

import java.util.Collections;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Tomas Zezula
 */
public abstract class AbstractModelFilter<T> implements Models.Filter<T> {

    public static final String OPTION_CLEAR = "_clear_content"; //NOI18N

    private final ChangeSupport changeSupport;
    //GuardedBy("this")
    private NameMatcher matcher;
    //GuardedBy("this")
    private String searchText;

    protected AbstractModelFilter() {
        this.changeSupport = new ChangeSupport(this);
    }

    @Override
    public final boolean accept(@NonNull final T item) {
        boolean res = true;
        final NameMatcher m = getMatcher();
        if (m != null) {
            final String itemValue = getItemValue(item);
            res = m.accept(itemValue);
            if (res) {
                update(item);
            }
        }
        return res;
    }

    public final void configure(
            @NullAllowed final SearchType searchType,
            @NullAllowed final String searchText,
            @NullAllowed final Map<String, Object> options) {
        synchronized (this) {
            this.searchText = searchText;
            if (options != null && options.get(OPTION_CLEAR) == Boolean.TRUE) {
                this.matcher = NameMatcher.NONE;
            } else {
                this.matcher = createNameMatcher(searchType, searchText, options);
            }
        }
        changeSupport.fireChange();
    }

    @Override
    public final void addChangeListener(ChangeListener listener) {
        this.changeSupport.addChangeListener(listener);
    }

    @Override
    public final void remmoveChangeListener(ChangeListener listener) {
        this.changeSupport.removeChangeListener(listener);
    }

    @NonNull
    protected abstract String getItemValue(@NonNull final T item);

    protected void update(@NonNull final T item) {
    }

    @CheckForNull
    protected synchronized final String getSearchText() {
        return searchText;
    }

    @CheckForNull
    private synchronized NameMatcher getMatcher() {
        return matcher;
    }

    @CheckForNull
    private static NameMatcher createNameMatcher (
            @NullAllowed final SearchType searchType,
            @NullAllowed final String searchText,
            @NullAllowed final Map<String,Object> options) {
        return (searchText != null && searchType != null) ?
            NameMatcherFactory.createNameMatcher(
                    searchText,
                    searchType,
                    options == null ? Collections.<String,Object>emptyMap() : options) :
            null;
    }
}
