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
package org.netbeans.modules.parsing.api;

/**
 * Functional core of {@linkplain UserTask} , allow calls like:
 * {@snippet :
 *     ParserManager.parse(Set.of(source), (result) -> {});
 * }
 *
 * @since 9.31
 * @author lkishalmi
 */
@FunctionalInterface
public interface ResultProcessor {
    /**
     * Functional UserTask implementation.
     *
     * @param resultIterator  A {@linkplain ResultIterator} instance.
     * @throws Exception re-thrown by the infrastructure as a
     *                      {@link org.netbeans.modules.parsing.spi.ParseException}.
     */
    void run(ResultIterator resultIterator) throws Exception;
}
