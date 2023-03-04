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
package org.netbeans.modules.search;

import org.openide.util.NbBundle;

/**
 *
 * @author jhavlin
 */
public final class Constants {

    /**
     * maximum number of found objects
     */
    public static final int COUNT_LIMIT = Integer.getInteger("netbeans.search.count.limit", 500);
    /**
     * maximum total number of detail entries for found objects
     */
    public static final int DETAILS_COUNT_LIMIT = Integer.getInteger("netbeans.search.details.count.limit", 5000);

    public enum Limit {

        /**
         * enum items
         */
        FILES_COUNT_LIMIT("TEXT_MSG_LIMIT_REACHED_FILES_COUNT", COUNT_LIMIT),
        MATCHES_COUNT_LIMIT("TEXT_MSG_LIMIT_REACHED_MATCHES_COUNT",
        DETAILS_COUNT_LIMIT);
        /**
         * item fields
         */
        private final String bundleKey;
        private final Integer value;

        private Limit(String bundleKey, Integer limit) {
            this.bundleKey = bundleKey;
            this.value = limit;
        }

        String getDisplayName() {
            return NbBundle.getMessage(Limit.class, bundleKey, value);
        }

        public Integer getValue() {
            return this.value;
        }
    }
}