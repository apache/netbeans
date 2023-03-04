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

package org.netbeans.modules.profiler.oql.engine.api.impl;

import org.netbeans.modules.profiler.oql.engine.api.OQLEngine.OQLQuery;

/**
 * This represents a parsed OQL query
 *
 * @author A. Sundararajan
 */
public class OQLQueryImpl extends OQLQuery {
    OQLQueryImpl(String selectExpr, boolean isInstanceOf, 
             String className, String identifier, String whereExpr) {
        this.selectExpr = selectExpr;
        this.isInstanceOf = isInstanceOf;
        this.className = className;
        this.identifier = identifier;
        this.whereExpr = whereExpr;
    }

    String   selectExpr;
    boolean  isInstanceOf;
    String   className;
    String   identifier;
    String   whereExpr;
}
