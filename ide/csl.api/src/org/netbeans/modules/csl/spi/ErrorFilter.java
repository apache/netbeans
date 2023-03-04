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
package org.netbeans.modules.csl.spi;

import java.util.List;
import org.netbeans.modules.csl.api.Error;

/**
 * Filters out some of the parser result errors for the specified feature.
 * 
 * @author marekfukala
 */
public interface ErrorFilter {

    /**
     * Feature name representing the tasklist feature.
     */
    public static final String FEATURE_TASKLIST = "tasklist"; //NOI18N
    
    /**
     * @param parserResult an instance of ParserResult
     * 
     * @return A list of the filtered errors or null if the filter doesn't
     * want to participate on the filtering 
     */
    public List<? extends Error> filter(ParserResult parserResult);

    /**
     * TODO: Possibly use mimelookup
     * An instance of this factory for creating ErrorFilters needs to be registered as a system service.
     */
    public interface Factory {

        /**
         * 
         * @param featureName The feature name for which the ErrorFilter should be created.
         * @return
         */
        public ErrorFilter createErrorFilter(String featureName);
    
    }
    
}
