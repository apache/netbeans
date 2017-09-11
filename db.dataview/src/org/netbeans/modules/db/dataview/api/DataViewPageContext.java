/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.dataview.api;

import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/** A helper which can return current page size for given data view.
 *
 * If values are changed here - preferences property or default page size,
 * update QueryAction in DB Explorer also.
 * 
 * @author Jiri Rechtacek
 * @since 1.4
 */
public final class DataViewPageContext {

    /**
     * Default page size (number of rows shown in query results table).
     *
     * @since 1.20
     */
    public static final int DEFAULT_PAGE_SIZE = 100;
    private static final String PROP_STORED_PAGE_SIZE =
            "storedPageSize"; //NOI18N
    private static int defaultPageSize = -1;

    private DataViewPageContext() {}

    /** Returns current page size for given view
     *
     * @param data view
     * @return a page size or -1 if unknown
     */
    public static int getPageSize(DataView view) {
        return view.delegate.getPageSize();
    }

    /**
     * Get stored or default page size.
     *
     * @return Page size, positive integer, not zero;
     * @since 1.20
     */
    public static int getStoredPageSize() {
        if (defaultPageSize < 0) {
            defaultPageSize = NbPreferences.forModule(
                    DataViewPageContext.class).getInt(
                    PROP_STORED_PAGE_SIZE, DEFAULT_PAGE_SIZE);
            if (defaultPageSize < 0) {
                defaultPageSize = DEFAULT_PAGE_SIZE;
                setStoredPageSize(defaultPageSize);
            }
        }
        return defaultPageSize;
    }

    /**
     * Store page size.
     *
     * @param pageSize Page size, positive integer, not zero.
     * @since 1.20
     */
    public static void setStoredPageSize(final int pageSize) {
        if (pageSize < 0) {
            throw new IllegalArgumentException("Negative pageSize");    //NOI18N
        }
        DataViewPageContext.defaultPageSize = pageSize;
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                NbPreferences.forModule(DataViewPageContext.class).putInt(
                        PROP_STORED_PAGE_SIZE, pageSize);
            }
        });
    }
}
