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

import java.beans.*;

import org.openide.nodes.*;

import org.netbeans.modules.dbschema.*;

/** Node representing a foreign key.
 * @see ForeignKeyElement
 */
public class ForeignKeyElementNode extends DBMemberElementNode {
	/** Create a new foreign key node.
	 * @param element foreign key element to represent
	 * @param writeable <code>true</code> to be writable
	 */
	public ForeignKeyElementNode(ForeignKeyElement element, TableChildren children, boolean writeable) {
		super(element, children, writeable);
		TableElementFilter filter = new TableElementFilter();
		filter.setOrder(new int[] {TableElementFilter.COLUMN});
		children.setFilter(filter);
	}

	/* Resolve the current icon base.
	 * @return icon base string.
	 */
	protected String resolveIconBase () {
		return FK;
	}

	/* Creates property set for this node */
	protected Sheet createSheet () {
		Sheet sheet = Sheet.createDefault();
		Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

		ps.put(createNameProperty(writeable));

		return sheet;
	}
}
