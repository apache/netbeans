/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
