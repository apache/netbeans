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

package org.netbeans.spi.jumpto.type;


/**
 * This enum describes the different kinds of searches that the Go To Type
 * dialog wants to perform on a type provider.
 * 
 * 
 * @author Tor Norbye
 */
public enum SearchType {
    /**
     * A search using an exact name of the type name
     * 
     * This is not yet used but the Go To Type dialog, but it seems plausible 
     * that it could be.
     */
    EXACT_NAME, 

    /**
     * Same as EXACT NAME but case insensitive
     */
    CASE_INSENSITIVE_EXACT_NAME,
    
    /**
     * A search using a case-sensitive prefix of the type name
     */
    PREFIX,

    /**
     * A search using a case-insensitive prefix of the type name
     */
    CASE_INSENSITIVE_PREFIX,

    /**
     * A search using a camel-case reduction of the type name
     */
    CAMEL_CASE,

    /**
     * A search using a case insensitive camel-case reduction of the type name
     * @since 1.45
     */
    CASE_INSENSITIVE_CAMEL_CASE,

    /**
     * A search using a case-sensitive
     * regular expression which should match the type name
     */
    REGEXP,

    /**
     * A search using a case-insensitive
     * regular expression which should match the type name
     */
    CASE_INSENSITIVE_REGEXP
}
