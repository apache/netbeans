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
package org.openide.cookies;

import org.openide.nodes.Node;


/** Cookie for node groups which can somehow be filtered.
 * This would be applied to a subclass of {@link org.openide.nodes.Children Children}.
* @deprecated Use Looks instead.
* @author Jaroslav Tulach, Jan Jancura, Dafe Simonek
*/
@Deprecated
public interface FilterCookie extends Node.Cookie {
    /** Get the declared filter (super-)class.
     * @return the class, or may be <code>null</code> if no filter is currently in use
    */
    public Class getFilterClass();

    /** Get the current filter.
     * @return the filter, or <code>null</code> if none is currently in use
    */
    public Object getFilter();

    /** Set the current filter.
    * @param filter the filter, or <code>null</code> if none should be used
    */
    public void setFilter(Object filter);
}
