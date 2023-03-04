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

import org.openide.actions.*;
import org.openide.nodes.*;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.dbschema.*;

/** The default implementation of the db nodes factory.
* Uses the standard node implementations in this package.
*/
public class DefaultDBFactory implements DBElementNodeFactory, IconStrings {
    public static final String WAIT =
        "org/openide/src/resources/wait"; // NOI18N

    public static final String ERROR =
        "org/openide/src/resources/error"; // NOI18N

    /** Default instance of the factory with read-write properties. */
	public static final DefaultDBFactory READ_WRITE = new DefaultDBFactory(true);

	/** Default instance of the factory with read-only properties. */
	public static final DefaultDBFactory READ_ONLY = new DefaultDBFactory(false);

	/** Should be the element nodes read-only or writeable
	 * (properties, clipboard operations,...)
	 */
	private boolean _writeable;

	/** Create a new factory.
	 * @param writeable <code>true</code> if the produced nodes
	 * should have writable properties
	 * @see DBElementNode#writeable
	 */
	public DefaultDBFactory (boolean writeable) {
		_writeable = writeable;
	}

	/* Test whether this factory produces writeable nodes.
	 * @return <code>true</code> if so
	 */
	public boolean isWriteable() {
        return _writeable;
    }
  
	/* Returns the node asociated with specified element.
	 * @return DBElementNode
	 */
	public Node createSchemaNode (final SchemaElement element) {
		return new SchemaElementNode(element, createSchemaChildren(element), isWriteable());
	}
  
	/** Create children for a schema node.
	 * Could be subclassed to customize, e.g., the ordering of children.
	 * The default implementation used {@link SchemaChildren}.
	 * @param element a schema element
	 * @return children for the schema element
	 */
	protected Children createSchemaChildren (SchemaElement element) {
		return createSchemaChildren(element, (isWriteable() ? READ_WRITE : READ_ONLY));
	}

	/** Create children for a schema node, with specified factory. 
	 * The default implementation used {@link SchemaChildren}.
	 * @param element a schema element
	 * @param factory the factory which will be used to create children
	 * @return children for the schema element
	 */
	final protected Children createSchemaChildren (SchemaElement element, DBElementNodeFactory factory) {
        SchemaChildren children = new SchemaChildren(factory, element);
		boolean writeable = isWriteable();
		
		return children;
	}

	/* Returns the node asociated with specified element.
	 * @return DBElementNode
	 */
	public Node createColumnNode (final ColumnElement element) {
		return new ColumnElementNode(element, isWriteable());
	}
    
	/* Returns the node asociated with specified element.
	 * @return DBElementNode
	 */
	public Node createColumnPairNode (final ColumnPairElement element) {
		return new ColumnPairElementNode(element, isWriteable());
	}

	/** Make a node representing an index.
	 * @param element the index
	 * @return an index node instance
	 */
	public Node createIndexNode (final IndexElement element) {
		return new IndexElementNode(element, (TableChildren) createIndexChildren(element), isWriteable());
	}
    
	/** Create children for an index node.
	 * Could be subclassed to customize, e.g., the ordering of children.
	 * The default implementation used {@link IndexChildren}.
	 * @param element a index element
	 * @return children for the index element
	 */
	protected Children createIndexChildren (IndexElement element) {
		return createIndexChildren(element, (isWriteable() ? READ_WRITE : READ_ONLY));
	}

	/** Create children for a index node, with specified factory. 
	 * The default implementation used {@link IndexChildren}.
	 * @param element a index element
	 * @param factory the factory which will be used to create children
	 * @return children for the index element
	 */
	final protected Children createIndexChildren (IndexElement element, DBElementNodeFactory factory) {
		TableChildren children = new TableChildren(factory, element);
		boolean writeable = isWriteable();

		return children;
	}


	/** Make a node representing a foreign key.
	 * @param element the foreign key
	 * @return a foreign key node instance
	 */
	public Node createForeignKeyNode (final ForeignKeyElement element) {
		return new ForeignKeyElementNode(element, (TableChildren) createForeignKeyChildren(element), isWriteable());
	}
    
	/** Create children for an index node.
	 * Could be subclassed to customize, e.g., the ordering of children.
	 * The default implementation used {@link IndexChildren}.
	 * @param element a index element
	 * @return children for the index element
	 */
	protected Children createForeignKeyChildren(ForeignKeyElement element) {
		return createForeignKeyChildren(element, (isWriteable() ? READ_WRITE : READ_ONLY));
	}

	/** Create children for a index node, with specified factory. 
	 * The default implementation used {@link IndexChildren}.
	 * @param element a index element
	 * @param factory the factory which will be used to create children
	 * @return children for the index element
	 */
	final protected Children createForeignKeyChildren (ForeignKeyElement element, DBElementNodeFactory factory) {
		TableChildren children = new TableChildren(factory, element);
		boolean writeable = isWriteable();

		return children;
	}

	/* Returns the node asociated with specified element.
	 * @return DBElementNode
	 */
	public Node createTableNode (TableElement element) {
		return new TableElementNode(element, createTableChildren(element), isWriteable());
	}

	/** Create children for a table node.
	 * Could be subclassed to customize, e.g., the ordering of children.
	 * The default implementation used {@link TableChildren}.
	 * @param element a table element
	 * @return children for the table element
	 */
	protected Children createTableChildren (TableElement element) {
		return createTableChildren(element, (isWriteable() ? READ_WRITE : READ_ONLY));
	}

	/** Create children for a table node, with specified factory. 
	 * The default implementation used {@link TableChildren}.
	 * @param element a table element
	 * @param factory the factory which will be used to create children
	 * @return children for the table element
	 */
	final protected Children createTableChildren (TableElement element, DBElementNodeFactory factory) {
		TableChildren children = new TableChildren(factory, element);
		TableElementFilter filter = new TableElementFilter();
		boolean writeable = isWriteable();
		
		filter.setOrder(new int[] {TableElementFilter.TABLE, TableElementFilter.VIEW});
		children.setFilter(filter);
                String db = element.getDeclaringSchema().getDatabaseProductName();
                boolean viewSupport =false;
                if (db!=null){
                    db = db.toLowerCase();
                    viewSupport = (db.indexOf("oracle") != -1 || db.indexOf("microsoft sql server") != -1) ? true : false;
                }

        if (((TableElement) element).isTableOrView() || viewSupport)
//		if (element.isTableOrView())
			children.add(new Node[] {
				new ElementCategoryNode(0, factory, element, writeable),
				new ElementCategoryNode(1, factory, element, writeable),
				new ElementCategoryNode(2, factory, element, writeable),
			});
		else
			children.add(new Node[] {
				new ElementCategoryNode(0, factory, element, writeable),
			});
		
		return children;
	}
  

    /* Creates and returns the instance of the node
     * representing the status 'WAIT' of the DataNode.
     * It is used when it spent more time to create elements hierarchy.
     * @return the wait node.
     */
        public Node createWaitNode() {
            AbstractNode n = new AbstractNode(Children.LEAF);
            n.setName(NbBundle.getMessage(DefaultDBFactory.class,"Wait"));
            n.setIconBase(WAIT);
            return n;
        }
        
    /* Creates and returns the instance of the node
     * representing the status 'ERROR' of the DataNode
     * @return the error node.
     */
        public Node createErrorNode() {
            AbstractNode n = new AbstractNode(Children.LEAF);
            n.setName(NbBundle.getMessage(DefaultDBFactory.class,"Error")); // NO18N
            n.setIconBase(ERROR);
            return n;
        }
	/** Array of the actions of the category nodes. */
	private static final SystemAction[] CATEGORY_ACTIONS = new SystemAction[] {
		SystemAction.get(ToolsAction.class),
	};

	/** The names of the category nodes */
	static final String[] NAMES = new String[] {
        NbBundle.getMessage(DefaultDBFactory.class, "Columns"), //NOI18N
		NbBundle.getMessage(DefaultDBFactory.class, "Indexes"), //NOI18N
		NbBundle.getMessage(DefaultDBFactory.class, "FKs"), //NOI18N
		NbBundle.getMessage(DefaultDBFactory.class, "Tables") //NOI18N
	};

	/** Filters under each category node */
	static final int[][] FILTERS = new int[][] {
		{ TableElementFilter.COLUMN },
		{ TableElementFilter.INDEX },
		{ TableElementFilter.FK },
		{ SchemaElementFilter.TABLE },
	};

    /** Array of the icons used for category nodes */
	static final String[] CATEGORY_ICONS = new String[] {
		COLUMNS_CATEGORY, INDEXES_CATEGORY, FKS_CATEGORY, TABLE
	};

	/**
	 * Category node - represents one section under table element node -
	 * columns, indexes, fks.
	 */
	static class ElementCategoryNode extends AbstractNode {
		/** The table element for this node */
		DBElement element;

		/** The type of the category node - for new types. */
		int newTypeIndex;

		/** Create new element category node for the specific category.
		* @param index The index of type (0=columns, 1=indexes, 2=fks)
		* @param factory The factory which is passed down to the table children
		* object 
		* @param element the table element for which this node is created
		*/
		ElementCategoryNode (int index, DBElementNodeFactory factory, TableElement element, boolean writeable) {
			this(index, new TableChildren(factory, element));
			this.element = element;
			newTypeIndex = writeable ? index : -1;
			switch (index) {
				case 0: setName(NbBundle.getMessage(DefaultDBFactory.class, "Columns")); break; //NOI18N
				case 1: setName(NbBundle.getMessage(DefaultDBFactory.class, "Indexes")); break; //NOI18N
				case 2: setName(NbBundle.getMessage(DefaultDBFactory.class, "FKs")); break; //NOI18N
			}
		}
    
		ElementCategoryNode (int index, DBElementNodeFactory factory, SchemaElement element, boolean writeable) {
			this(index, new SchemaChildren(factory, element));
			this.element = element;
			newTypeIndex = writeable ? index : -1;
			setName(NbBundle.getMessage(DefaultDBFactory.class, "Tables")); //NOI18N
		}

		/** Create new element node.
		 * @param index The index of type (0=columns, 1=indexes, 2=fks)
		 * @param children the table children of this node
		 */
		private ElementCategoryNode (int index, TableChildren children) {
			super(children);
			setDisplayName(NAMES[index]);
			systemActions = CATEGORY_ACTIONS;
			TableElementFilter filter = new TableElementFilter();
			filter.setOrder(FILTERS[index]);
			children.setFilter(filter);
			setIconBase(CATEGORY_ICONS[index]);
		}
    
		private ElementCategoryNode (int index, SchemaChildren children) {
			super(children);
    	    setDisplayName(NAMES[index]);
			systemActions = CATEGORY_ACTIONS;
			setIconBase(CATEGORY_ICONS[index]);
		}

		public HelpCtx getHelpCtx () {
            return new HelpCtx("dbschema_ctxhelp_wizard"); //NOI18N
		}
	}
}
