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

package org.netbeans.spi.navigator;

/** Hint for Navigator clients to link <code>Lookup</code> of their
 * <code>TopComponent</code> with Navigator content type.<p></p>
 *
 * Usage: Implementation of this interface should be inserted into
 * client's specific topComponent's lookup, see
 * <a href="@org-openide-windows@/org/openide/windows/TopComponent.html#getLookup()">TopComponent.getLookup()</a>
 * method. When mentioned <code>TopComponent</code> gets active in the system, system will
 * ask <code>NavigatorLookupHint</code> implementation for content type
 * to show in Navigator UI.
 *
 * @author Dafe Simonek
 */
public interface NavigatorLookupHint {

    /** Hint for content type that should be used in Navigator 
     * 
     * @return String representation of content type (in mime-type style)
     */
    public String getContentType ();
    
}
