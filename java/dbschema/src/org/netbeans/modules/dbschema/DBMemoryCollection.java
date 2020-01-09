/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.dbschema;

import java.util.*;

/** Support class that manages set of objects and fires events
 */
class DBMemoryCollection {
	/** Object to fire info about changes to */
	protected DBElement.Memory/*TableElement.Memory*/ _memory;

	/** name of property to fire */
	private String _propertyName;

	/** array template to return */
	private Object[] _template;

    private DBElement[] _elms;

    /** Default constructor
     */
	public DBMemoryCollection() {
    }

	/** Creates a new collection.
	 * @param memory memory element to fire changes
	 * @param propertyName name of property to fire when array changes
	 * @param emptyArray emptyArray instance that provides the type of arrays that should be returned by toArray method
	 */
	public DBMemoryCollection (DBElement.Memory memory, String propertyName, Object[] emptyArray) {
		_memory = memory;
		_propertyName = propertyName;
		_template = emptyArray;
	}

	/** Changes the content of this object.
	 * @param arr array of objects to change
	 * @param action the action to do
	 */
	public void change(DBElement[] arr, int action) {
		change(Arrays.asList(arr), action);
	}

	/** Changes the content of this object.
	 * @param c collection of objects to change
	 * @param action the action to do
	 */
	protected void change(List c, int action) {
		boolean hasChange = false;

        try {
            DBElement[] oldElements = getElements();
            int oldLength = (oldElements == null) ? 0 : oldElements.length;
            int newLength = (c == null) ? 0 : c.size();
            List list = null;

            switch (action) {
                case DBElement.Impl.ADD:
                    if (newLength > 0) {
                        list = ((oldLength == 0) ? new ArrayList() : new ArrayList(Arrays.asList(oldElements)));
                        list.addAll(c);
                        hasChange = true;
                    }
                    break;
                case TableElement.Impl.SET:
                    list = c;
                    hasChange = true;
                    break;
                case TableElement.Impl.REMOVE:
                    if (newLength > 0 && oldLength > 0) {
                        list = new ArrayList(Arrays.asList(oldElements));
                        list.removeAll(c);
                        hasChange = true;
                    }
                    break;
            }
            if (hasChange)
                _elms = (DBElement[]) list.toArray(_template);
        } finally {
            if (hasChange)
                _memory.firePropertyChange(_propertyName, null, null);
        }
	}

    /** Returns an array containing all of the elements in this collection.
     * @return an array containing all of the elements in this collection
     */
    public DBElement[] getElements() {
        if (_elms != null)
            return _elms;
        else
            return Arrays.asList(_template).toArray(new DBElement[_template.length]);
    }

    /** Returns an element specified by the name from this collection.
     * @return an element
     */
    public DBElement getElement(DBIdentifier name) {
        DBElement[] elms = getElements();
        int count = ((elms != null) ? elms.length : 0);

        for (int i = 0; i < count; i++) {
            DBElement elm = elms[i];
            if (name.getName().equals(elm.getName().getName()))
                return elm;
        }

        return null;
    }

	/** Collection for members. Assignes to each class its members.
	 */
	static abstract class Member extends DBMemoryCollection {
        /** Default constructor.
         */
        public Member() {
        }

        /** Creates a new member.
         * @param memory memory element to fire changes to
         * @param propertyName name of property to fire when array changes
         * @param emptyArray emptyArray instance that provides the type of
         * arrays that should be returned by toArray method
         */
        public Member (DBElement.Memory memory, String propertyName, Object[] emptyArray) {
            super(memory, propertyName, emptyArray);
        }

        /** Gets a table element.
         * @return a table element
         */
		protected TableElement getTableElement() {
			if (_memory instanceof TableElement.Memory)
				return ((TableElement.Memory)_memory).getTableElement();
			
			if (_memory instanceof DBMemberElement.Memory)
				return ((DBMemberElement)((DBMemberElement.Memory)_memory)._element).getDeclaringTable();
			
			return null;
		}
        
		/** Clones the object.
		 * @param obj object to clone
		 * @return cloned object
		 */
		protected abstract DBMemberElement clone(Object obj);
	}

	/** Collection of tables.
	 */
	static class Table extends DBMemoryCollection {
		private static final TableElement[] EMPTY = new TableElement[0];

        /** Default constructor
         */
        public Table() {
        }

		/** Creates a new table.
         * @param el table element memory impl to work in
		 */
		public Table(DBElement.Memory el) {
			super(el, DBElementProperties.PROP_TABLES, EMPTY);
		}

		/** Clones the object.
		 * @param obj object to clone
		 * @return cloned object
		 */
		protected TableElement clone (Object obj) {
            return new TableElement(new TableElement.Memory((TableElement) obj), getSchemaElement());
		}
        
        /** Gets a schema element.
         * @return a schema element
         */
		protected SchemaElement getSchemaElement () {
			if (_memory instanceof SchemaElement.Memory)
				return ((SchemaElement.Memory)_memory).getSchemaElement();
			
			if (_memory instanceof TableElement.Memory)
				return ((TableElement)((TableElement.Memory)_memory)._element).getDeclaringSchema();
			
			return null;
		}
	}
    
	/** Collection of columns.
	 */
	static class Column extends Member {
		private static final ColumnElement[] EMPTY = new ColumnElement[0];

        /** Default constructor
         */
        public Column() {
        }

		/** Creates a new column.
         * @param el table element memory impl to work in
		 */
		public Column(DBElement.Memory el) {
			super(el, DBElementProperties.PROP_COLUMNS, EMPTY);
		}

		/** Clones the object.
		 * @param obj object to clone
		 * @return cloned object
		 */
		protected DBMemberElement clone(Object obj) {
			return new ColumnElement(new ColumnElement.Memory((ColumnElement)obj), getTableElement());
		}
	}
    
	/** Collection of column pairs.
	 */
	static class ColumnPair extends Member {
		private static final ColumnPairElement[] EMPTY = new ColumnPairElement[0];

        /** Default constructor
         */
        public ColumnPair() {
        }

		/** Creates a new column pair.
         * @param el table element memory impl to work in
		 */
		public ColumnPair(DBElement.Memory el) {
			super(el, DBElementProperties.PROP_COLUMN_PAIRS, EMPTY);
		}

		/** Clones the object.
		 * @param obj object to clone
		 * @return cloned object
		 */
		protected DBMemberElement clone(Object obj) {
//			return new ColumnPairElement(new ColumnPairElement.Memory((ColumnPairElement)obj), null, null, getTableElement());
            return null;
		}
	}

	/** Collection of indexes.
	 */
	static class Index extends Member {
		private static final IndexElement[] EMPTY = new IndexElement[0];

        /** Default constructor
         */
        public Index() {
        }
        
		/** Creates a new index.
         * @param el table element memory impl to work in
		 */
		public Index(DBElement.Memory el) {
			super(el, DBElementProperties.PROP_INDEXES, EMPTY);
		}

		/** Clones the object.
		 * @param obj object to clone
		 * @return cloned object
		 */
		protected DBMemberElement clone(Object obj) {
			return new IndexElement(new IndexElement.Memory((IndexElement)obj), getTableElement());
		}
	}

	/** Collection of keys.
	 */
	static class Key extends Member {
		private static final KeyElement[] EMPTY = new KeyElement[0];

        /** Default constructor
         */
        public Key() {
        }

		/** Creates a new key.
         * @param el table element memory impl to work in
		 */
		public Key(DBElement.Memory el) {
			super(el, DBElementProperties.PROP_KEYS, EMPTY);
		}

		/** Clones the object.
		 * @param obj object to clone
		 * @return cloned object
		 */
		protected DBMemberElement clone(Object obj) {
		    return null;
        }
	}
}
