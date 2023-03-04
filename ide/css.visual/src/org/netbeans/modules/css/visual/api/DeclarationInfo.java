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
package org.netbeans.modules.css.visual.api;

import org.netbeans.modules.css.model.api.PropertyDeclaration;

/**
 * Provides additional information for a {@link Declaration}.
 * 
 * @author marekfukala
 */
public enum DeclarationInfo {
    
    /**
     * Indicates the declaration is overridden by another declaration.
     */
    OVERRIDDEN,
    
    /**
     * Flags inactive declaration.
     * 
     * CSS rule that affects an element can either match the element or may match one
     * of its parents (and be inherited). Moreover, not all properties (only those
     * marked as inherited by the corresponding CSS spec.) from an inherited rule
     * affect the element. Hence, I am marking properties (from an inherited rule)
     * that are not inherited by DeclarationInfo.INACTIVE to emphasize that they are
     * not affecting the selected element. In summary, a property marked by this flag
     * is not inherited property from an inherited rule.
     * 
     * For the sake of completeness I have to add that I mark by this flag also
     * another group of properties (but I don't think that this must be covered by the
     * tooltip): the ones that use star or underscore CSS hack to affect some versions
     * of Internet Explorer only. These properties also do not affect the rendered
     * element because the inspected page runs in Chrome or in WebView (i.e. not in
     * IE). 
     */
    INACTIVE,
    
    /**
     * Flags erroneous declaration.
     */
    ERRONEOUS;
    
}
