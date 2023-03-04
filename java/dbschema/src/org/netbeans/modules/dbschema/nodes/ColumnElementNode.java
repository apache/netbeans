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
import org.netbeans.modules.dbschema.util.SQLTypeUtil;

/** Node representing a column.
 * @see ColumnElement
 */
public class ColumnElementNode extends DBMemberElementNode {
	/** Create a new column node.
	 * @param element column element to represent
	 * @param writeable <code>true</code> to be writable
	 */
	public ColumnElementNode (ColumnElement element, boolean writeable) {
		super(element, Children.LEAF, writeable);
	}

	/* Resolve the current icon base.
	 * @return icon base string.
	 */
	protected String resolveIconBase () {
		return COLUMN;
	}

	/* Creates property set for this node */
	protected Sheet createSheet () {
		Sheet sheet = Sheet.createDefault();
		Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

		ps.put(createNameProperty(writeable));
		ps.put(createTypeProperty(writeable));
		ps.put(createNullableProperty(writeable));
		ps.put(createLengthProperty(writeable));
		ps.put(createPrecisionProperty(writeable));
		ps.put(createScaleProperty(writeable));

		return sheet;
	}

	/** Create a property for the column type.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createTypeProperty (boolean canW) {
		return new ElementProp(PROP_TYPE, /*Integer.TYPE*/String.class, canW) {
			/** Gets the value */
			public Object getValue () {
                return SQLTypeUtil.getSqlTypeString(((ColumnElement) element).getType());
			}
        };
	}
    
	/** Create a property for the column nullable.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createNullableProperty (boolean canW) {
		return new ElementProp(PROP_NULLABLE, Boolean.TYPE, canW) {
			/** Gets the value */
			public Object getValue () {
				return Boolean.valueOf(((ColumnElement)element).isNullable());
			}
        };
	}
    
	/** Create a property for the column length.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createLengthProperty (boolean canW) {
		return new ElementProp(PROP_LENGTH, Integer.TYPE, canW) {
			/** Gets the value */
			public Object getValue () {
				return ((ColumnElement)element).getLength();
			}
        };
	}
    
	/** Create a property for the column precision.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createPrecisionProperty (boolean canW) {
		return new ElementProp(PROP_PRECISION, Integer.TYPE, canW) {
			/** Gets the value */
			public Object getValue () {
				return ((ColumnElement)element).getPrecision();
			}
        };
	}
    
	/** Create a property for the column scale.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createScaleProperty (boolean canW) {
		return new ElementProp(PROP_SCALE, Integer.TYPE, canW) {
			/** Gets the value */
			public Object getValue () {
				return ((ColumnElement)element).getScale();
			}
        };
    }
}
