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

import org.apache.lucene.index.IndexReader;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Allows {@link IndexReader} to be passed to the convertor.
 * When implemented by the {@link Convertor} or {@link StoppableConvertor}
 * the {@link Index}'s queries set an {@link IndexReader} instance to the
 * passed convertor before calling the convert method. At the end of the query
 * the active {@link IndexReader} is replaced by null.
 * @since 2.10
 * @author Tomas Zezula
 */
public interface IndexReaderInjection {
    /**
     * Sets the {@link IndexReader} instance.
     * @param indexReader to be set or null
     */
    void setIndexReader(@NullAllowed IndexReader indexReader);
}
