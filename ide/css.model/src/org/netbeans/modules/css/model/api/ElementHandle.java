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
package org.netbeans.modules.css.model.api;

/**
 * Acts as a handle for {@link Element}s objects from {@link Model}.
 * Can be later resolved back to {@link Element}.
 *
 * @author marekfukala
 */
public interface ElementHandle {
    
    /**
     * Resolves the {@link ElementHandle} to an instance of {@link Model}.
     * 
     * @param model
     * @return instance of {@link Element} or null if no corresponding element
     * can be found in the given model.
     */
    public Element resolve(Model model);
    
}
