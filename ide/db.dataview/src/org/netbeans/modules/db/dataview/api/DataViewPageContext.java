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
