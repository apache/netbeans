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

package org.netbeans.modules.parsing.lucene.support;

/**
 * A convertor used by the {@link Index} to convert user types
 * into or from lucene Documents, Queries, Terms.
 * The interface allows isolation of user code from the lucene
 * specific types.
 * @author Tomas Zezula
 */
public interface Convertor<P,R> {
    /**
     * Converts given object
     * @param p the object to be converted
     * @return the result of conversion
     */
    R convert (P p);
}
