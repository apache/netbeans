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
package org.netbeans.modules.java.source.queries.api;

/**
 * Query or update function
 * todo: Move to some generic module and change to Function@lt;P,R,E extends Exception&gt;
 * @author Tomas Zezula
 */
public interface Function<P,R> {
    /**
     * Performs the function on given parameter returning a result
     * @param param the function parameter
     * @return result
     */
    R apply (P param) throws QueryException;
}
