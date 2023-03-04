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

package org.netbeans.api.xml.cookies;

import org.openide.nodes.Node;

/**
 * Fast (and preferably standalone mode) XML parsed entity syntax checker.
 * <p>Implemenmtation should follow XML specification for non-validating
 * processors. Implementation is allowed to support any XML parsed entities.
 * It must not change UI state.
 * <p>
 * It should be gracefully served by all data objects and explorer nodes
 * representing non-validateable XML resources.
 *
 * @author      Petr Kuzel
 * @see         ValidateXMLCookie
 * @see         <a href="http://www.w3.org/TR/REC-xml#proc-types">XML 1.0</a>     
 */
public interface CheckXMLCookie extends Node.Cookie {
    
    /**
     * Check XML parsed entity for syntax wellformedness.
     * @param observer optional listener (<code>null</code> allowed)
     *               giving judgement details via {@link XMLProcessorDetail}s.
     * @return <code>true</code> if syntax check passes
     */
    boolean checkXML(CookieObserver observer);
    
}
