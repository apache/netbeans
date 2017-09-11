/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.notifications.filter;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.notifications.NotificationImpl;

/**
 *
 * @author S. Aubrecht
 * @author jpeska
 */
public class NotificationFilter {

    public static final NotificationFilter EMPTY = new EmptyNotificationFilter();
    private String name;
    private CategoryFilter categoryFilter = new CategoryFilter();

    NotificationFilter(String name) {
        this.name = name;
    }

    NotificationFilter() {
    }

    private NotificationFilter(NotificationFilter src) {
        this.name = src.name;
        categoryFilter = null == src.categoryFilter ? null : src.categoryFilter.clone();
    }

    public boolean isEnabled(NotificationImpl notification) {
        return categoryFilter == null ? true : categoryFilter.isEnabled(notification.getCategory().getName());
    }

    public String getName() {
        return name;
    }

    void setName(String newName) {
        this.name = newName;
    }

    CategoryFilter getCategoryFilter() {
        return categoryFilter;
    }

    void setCategoryFilter(CategoryFilter categoryFilter) {
        this.categoryFilter = categoryFilter;
    }

    @Override
    public Object clone() {
        return new NotificationFilter(this);
    }

    @Override
    public String toString() {
        return name;
    }

    void load(Preferences prefs, String prefix) throws BackingStoreException {
        name = prefs.get(prefix + "_name", "Filter"); //NOI18N //NOI18N
        if (prefs.getBoolean(prefix + "_types", false)) { //NOI18N
            categoryFilter = new CategoryFilter();
            categoryFilter.load(prefs, prefix + "_types"); //NOI18N
        } else {
            categoryFilter = null;
        }
    }

    void save(Preferences prefs, String prefix) throws BackingStoreException {
        prefs.put(prefix + "_name", name); //NOI18N

        if (null != categoryFilter) {
            prefs.putBoolean(prefix + "_types", true); //NOI18N
            categoryFilter.save(prefs, prefix + "_types"); //NOI18N
        } else {
            prefs.putBoolean(prefix + "_types", false); //NOI18N
        }
    }

    private static class EmptyNotificationFilter extends NotificationFilter {

        public EmptyNotificationFilter() {
            super(Util.getString("no-filter")); //NOI18N
        }

        @Override
        public boolean isEnabled(NotificationImpl notification) {
            return true;
        }
    }
}
