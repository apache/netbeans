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
package org.netbeans.modules.cnd.apt.support.spi;

/**
 * When indexing a file, determines, which tokens to index and how.
 * Initial need is Tuxedo, which needs to search for functions in tpcall() parameter strings.
 * Later use could be: search in comments
 */
public interface CndTextIndexFilter {
    /**
     * Is called for each string token of a file in order to determine
     * whether to index it and get text to put into index.
     * @param stringTokenText text of string token
     * @return if non-null, the returned text should be stored in index
     */
    CharSequence getStringIndexText(CharSequence stringTokenText);
}
