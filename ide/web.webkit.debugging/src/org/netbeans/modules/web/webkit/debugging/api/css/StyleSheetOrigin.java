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
package org.netbeans.modules.web.webkit.debugging.api.css;

/**
 * Stylesheet origin.
 *
 * @author Jan Stola
 */
public enum StyleSheetOrigin {
    /** User stylesheet. */
    USER,
    /** User-agent stylesheet. */
    USER_AGENT,
    /** Stylesheet created by the inspector. */
    INSPECTOR,
    /** Regular stylesheet. */
    REGULAR;

    /**
     * Returns style origin for the given code.
     *
     * @param originCode code of the style origin.
     * @return style origin matching the given code or {@code null} for
     * an unknown code.
     */
    static StyleSheetOrigin forCode(String originCode) {
        StyleSheetOrigin origin;
        if ("user".equals(originCode)) { // NOI18N
            origin = USER;
        } else if ("user-agent".equals(originCode)) { // NOI18N
            origin = USER_AGENT;
        } else if ("inspector".equals(originCode)) { // NOI18N
            origin = INSPECTOR;
        } else if ("regular".equals(originCode)) { // NOI18N
            origin = REGULAR;
        } else {
            origin = null;
        }
        return origin;
    }

}
