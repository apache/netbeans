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
package org.netbeans.modules.j2ee.dd.api.web;

/**
 * Interface for WebFragment element.<br>
 * The WebFragment object is the root of bean graph generated<br>
 * for deployment descriptor(web-fragment.xml) file.<br>
 * For getting the root (WebFragment object) use the {@link WebFragmentProvider#getDDRoot} method.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface WebFragment extends org.netbeans.modules.j2ee.dd.api.common.RootInterface, WebApp {

    // For now, the interface inherits from WebApp interface
    // Later, it can be changed to separate interface
    // (It will require rewriting of a lot of code -- all GUI editors, etc.)


    // Methods specific for WebFragment

    RelativeOrdering newRelativeOrdering();
    RelativeOrdering[] getOrdering();
    void setOrdering(RelativeOrdering[] value);

}
