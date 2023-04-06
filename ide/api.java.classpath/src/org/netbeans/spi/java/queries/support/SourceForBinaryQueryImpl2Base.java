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

package org.netbeans.spi.java.queries.support;

import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.queries.SFBQImpl2Result;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.util.Parameters;

/**
 * Base class for {@link SourceForBinaryQueryImplementation2} which need to delegate
 * to other {@link org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation}.
 * @since 1.16
 * @author Tomas Zezula
 */
public abstract class SourceForBinaryQueryImpl2Base implements SourceForBinaryQueryImplementation2 {

    /**
     * Creates a wrapper for {@link org.netbeans.api.java.queries.SourceForBinaryQuery.Result}. This method
     * should be used by delegating {@link SourceForBinaryQueryImplementation2}
     * which need to delegate to {@link org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation}. 
     * @param result returned by {@link org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation},
     * When result is already instanceof {@link org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2.Result}
     * it's returned without wrapping.
     * @return a {@link org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2.Result}.
     */
    protected final Result asResult (SourceForBinaryQuery.Result result) {
        Parameters.notNull("result", result);   //NOI18N
        if (result instanceof Result) {
            return (Result) result;
        }
        else {
            return new SFBQImpl2Result(result);
        }
    }
}
