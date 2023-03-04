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
package org.netbeans.modules.refactoring.java.api;

/**
 * @author Jan Becicka
 * @see org.netbeans.modules.refactoring.api.WhereUsedQuery#putValue
 * @see org.netbeans.modules.refactoring.api.WhereUsedQuery#getBooleanValue
 */
public enum WhereUsedQueryConstants {
    /**
     * Find overriding methods
     */
    FIND_OVERRIDING_METHODS,
    /**
     * Find All Sublcasses recursively
     */
    FIND_SUBCLASSES,
    /**
     * Find only direct subclasses
     */
    FIND_DIRECT_SUBCLASSES,
    /**
     * Search from base class
     */
    SEARCH_FROM_BASECLASS,
    /**
     * Search from base class
     * @since 1.45
     */
    SEARCH_OVERLOADED;
}
