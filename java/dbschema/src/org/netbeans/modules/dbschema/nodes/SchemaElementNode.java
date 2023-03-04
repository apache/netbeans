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

public class SchemaElementNode extends DBElementNode {
    /** Creates new SchemaElementNode */
    public SchemaElementNode(SchemaElement element, Children children, boolean writeable) {
        super(element, children, writeable);
    }

    /* Resolve the current icon base.
    * @return icon base string.
    */
    protected String resolveIconBase() {
        return SCHEMA;
    }

    /* Creates property set for this node */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        ps.put(createNameProperty(writeable));
        ps.put(createSchemaProperty(writeable));
        ps.put(createCatalogProperty(writeable));
        ps.put(createDatabaseProductNameProperty(writeable));
        ps.put(createDatabaseProductVersionProperty(writeable));
        ps.put(createDriverNameProperty(writeable));
        ps.put(createDriverVersionProperty(writeable));
        ps.put(createDriverProperty(writeable));
        ps.put(createUrlProperty(writeable));
        ps.put(createUsernameProperty(writeable));

        return sheet;
    }

	/** Create a property for the schema schema.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createSchemaProperty (boolean canW) {
		return new ElementProp(PROP_SCHEMA, String.class, canW) {
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getSchema().getName();
			}
		};
	}
    
	/** Create a property for the schema catalog.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createCatalogProperty (boolean canW) {
		return new ElementProp(PROP_CATALOG, String.class, canW) {
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getCatalog().getName();
			}
		};
	}
    
	/** Create a property for the schema database product name.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createDatabaseProductNameProperty (boolean canW) {
		return new ElementProp("databaseProductName", String.class, canW) {  //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getDatabaseProductName();
			}
		};
	}

	/** Create a property for the schema database product version.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createDatabaseProductVersionProperty (boolean canW) {
		return new ElementProp("databaseProductVersion", String.class, canW) { //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getDatabaseProductVersion();
			}
		};
	}

	/** Create a property for the schema driver name.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createDriverNameProperty (boolean canW) {
		return new ElementProp("driverName", String.class, canW) {  //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getDriverName();
			}
		};
	}

	/** Create a property for the schema driver version.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createDriverVersionProperty (boolean canW) {
		return new ElementProp("driverVersion", String.class, canW) { //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getDriverVersion();
			}
		};
	}

	/** Create a property for the schema driver URL.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createDriverProperty (boolean canW) {
		return new ElementProp("driver", String.class, canW) { //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getDriver();
			}
		};
	}

    /** Create a property for the schema url.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createUrlProperty (boolean canW) {
		return new ElementProp("url", String.class, canW) { //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getUrl();
			}
		};
	}

	/** Create a property for the schema username.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createUsernameProperty (boolean canW) {
		return new ElementProp("username", String.class, canW) { //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getUsername();
			}
        };
	}

}
