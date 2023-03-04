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
package org.netbeans.modules.web.jsf.api.facesmodel;

import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public interface ContractMapping extends DescriptionGroup, IdentifiableElement {

    /**
     * Property name of &lt;url-pattern&gt; element.
     * Specifies the collection of views in this application that
     * are allowed to use the corresponding contracts.
     */
    static final String URL_PATTERN = JSFConfigQNames.URL_PATTERN.getLocalName();

    /**
     * Property name of &lt;contracts&gt; element.
     * Comma separated list of resource library contracts that, if available to
     * the application, may be used by the views matched by the corresponding "url-pattern".
     */
    static final String CONTRACTS = JSFConfigQNames.CONTRACTS.getLocalName();

    UrlPattern getUrlPattern();
    void setUrlPattern(UrlPattern urlPatternType);

    String getContracts();
    void setContracts(String contracts);
}
