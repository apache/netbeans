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
package org.netbeans.modules.css.indexing.api;

import java.util.Collection;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;

/**
 * A factory for {@link CssIndexModel} to be registered in the global lookup.
 *
 * @since 1.47
 * @author marekfukala
 */
public abstract class CssIndexModelFactory<T extends CssIndexModel> {
   
    /**
     * Creates an instance of {@link CssIndexModel} for the given {@link CssParserResult}.
     * @param result
     * @return non null instance of the model
     */
    public abstract T getModel(CssParserResult result);

    /**
     * Builds an instance of {@link CssIndexModel} from the given {@link IndexResult}
     * @param result
     * @return non null instance of the model
     */
    public abstract T loadFromIndex(IndexResult result);
    
    /**
     * Gets a collection of all index keys which are stored by the {@link #loadFromIndex(org.netbeans.modules.parsing.spi.indexing.support.IndexResult) }
     * @return non null collection of index keys
     */
    public abstract Collection<String> getIndexKeys();
}
