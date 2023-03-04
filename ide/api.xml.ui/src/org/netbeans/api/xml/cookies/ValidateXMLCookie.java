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
 * Validate XML document entity (in polyform manner).
 * <p>Implemenmtation must follow XML 1.0 specification for processors
 * conformance. It is allowed to extend the contract to support domain
 * specifics semantics checks (i.e. XML Schema). It must not change UI state.
 * <p>
 * It should be gracefully served by all data objects and explorer nodes
 * representing validateable XML resources.
 * <p>
 * <h3>Use Cases</h3>
 * <ul>
 * <li><b>Provider</b> needs to define domain specifics semantics checks such as
 *     XML Schema based validation, Ant script dependency cycles checking, etc.
 * <li><b>Client</b> needs to check XML document entities transparently without
 *     appriory knowledge how to perform it for specifics XML document type.
 * </ul>
 *
 * @author      Petr Kuzel
 * @see         CheckXMLCookie
 * @see         <a href="http://www.w3.org/TR/REC-xml#proc-types">XML 1.0</a>     
 */
public interface ValidateXMLCookie extends Node.Cookie {

    /**
     * Validate XML document entity.
     * @param observer Optional listener (<code>null</code> allowed)
     *               giving judgement details via {@link XMLProcessorDetail}s.
     * @return true if validity check passes.
     */
    boolean validateXML(CookieObserver observer);

}
