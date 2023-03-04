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
package org.netbeans.spi.search;

import java.util.List;
import org.netbeans.api.annotations.common.NonNull;

/**
 * This class defines default search options for a node and its subnodes.
 *
 * It only applies if no {@link SearchInfoDefinition} is found in the lookup of
 * node where search starts.
 *
 * If an instance of this class is found in lookup of a node it will be used for
 * setting default search options.
 *
 * It is mainly useful in project nodes to set which folders and files are
 * skipped (filtered).
 *
 * <div class="nonnormative">
 * <p>Example:</p>
 * <pre>
 * {@code 
 * 
 * public class MyNode extends AbstractNode {
 *   public MyNode() { 
 *     super(Lookups.singleton(new SubTreeSearchOptions() {
 *       public List<SearchFilterDefinition> getFilters() {
 *        // return list of SearchFilterDefinitions objects.
 *       }
 *     }));
 *   }
 *   ...
 * }}</pre>
 * </div>
 * 
 * @author jhavlin
 */
public abstract class SubTreeSearchOptions {

    /**
     * Get list of filters that will be used for searching under a node.
     */
    public abstract @NonNull
    List<SearchFilterDefinition> getFilters();
}
