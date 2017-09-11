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

import org.openide.nodes.Node;

import org.netbeans.modules.dbschema.*;

/** A factory used to create instances of hierarchy node implementations.
* Loaders that use the element hierarchy should implement this factory
* so as to provide their own implementations of hierarchy element nodes.
* @see TableChildren
*/
public interface DBElementNodeFactory {
	/** Make a node representing a schema.
	 * @param element the schema
	 * @return a schema node instance
	 */
	public Node createSchemaNode (SchemaElement element);
  
	/** Make a node representing a column.
	 * @param element the column
	 * @return a column node instance
	 */
	public Node createColumnNode (ColumnElement element);
    
	/** Make a node representing a column pair.
	 * @param element the column pair
	 * @return a column pair node instance
	 */
	public Node createColumnPairNode (ColumnPairElement element);

	/** Make a node representing an index.
	 * @param element the index
	 * @return an index node instance
	 */
	public Node createIndexNode (IndexElement element);

	/** Make a node representing a foreign key.
	 * @param element the foreign key
	 * @return a foreign key node instance
	 */
	public Node createForeignKeyNode (ForeignKeyElement element);

	/** Make a node representing a table.
	 * @param element the table
	 * @return a table node instance
	 */
	public Node createTableNode (TableElement element);

	/** Make a node indicating that the creation of children
	 * is still under way.
	 * It should be used when the process is slow.
	 * @return a wait node
	 */
	public Node createWaitNode ();

	/** Make a node indicating that there was an error creating
	 * the element children.
	 * @return the error node
	 */
	public Node createErrorNode ();
}
