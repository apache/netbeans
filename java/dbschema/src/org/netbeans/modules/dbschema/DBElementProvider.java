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

package org.netbeans.modules.dbschema;

import org.openide.nodes.Node;

public class DBElementProvider implements Node.Cookie {

    private DBElement element;

    /** Default constructor.
     */
    public DBElementProvider() {
    }

    /** Creates a new element provider.
     * @param element the element which will be represented by this element provider.
     */
    public DBElementProvider(DBElement element) {
        this.element = element;
    }

	/** Gets a database element from this element provider.
	 * @param element the element
	 * @return the element or <code>null</code>
	 */
	public DBElement getDBElement() {
		return element;
	}
}
