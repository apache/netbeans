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

package org.netbeans.modules.dbschema.nodes;

import org.openide.nodes.*;

import org.netbeans.modules.dbschema.*;

/** Node representing some type of member element.
 */
public abstract class DBMemberElementNode extends DBElementNode {
	/** Create a new node.
	 *
	 * @param element member element to represent
	 * @param children list of children
	 * @param writeable <code>true</code> to be writable
	 */
	public DBMemberElementNode (DBMemberElement element, Children children, boolean writeable) {
		super(element, children, writeable);
		superSetName(element.getName().getName());
	}
  
	/** Create a node property representing the element's name.
	 * @param canW if <code>false</code>, property will be read-only
	 * @return the property.
	 */
	protected Node.Property createNameProperty (boolean canW) {
		return new ElementProp(Node.PROP_NAME, String.class,canW) {
			/** Gets the value */
			public Object getValue () {
                DBMemberElement elm = (DBMemberElement) element;

                return elm.getDeclaringTable().getName().getName() + "." + elm.getName().getName(); //NOI18N
			}
		};
	}
}
