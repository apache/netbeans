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

package org.netbeans.modules.dbschema;

import java.util.ResourceBundle;

/** Describes an index in a table.
 */
public final class IndexElement extends DBMemberElement implements ColumnElementHolder {
	/** Creates a new index element represented in memory.
	 */
	public IndexElement() {
		this(new Memory(), null);
	}

	/** Creates a new index element.
	 * @param impl the pluggable implementation
	 * @param declaringTable declaring table of this index, or
	 * <code>null</code>
	 */
	public IndexElement(Impl impl, TableElement declaringTable) {
		super(impl, declaringTable);
	}

    /** Returns the implementation for the index.
     * @return implementation for the index
     */
	final Impl getIndexImpl() {
        return (Impl)getElementImpl();
    }

	/** Gets the unique flag of the index.
	 * @return true if it is a unique index, false otherwise
	 */
	public boolean isUnique() {
        return getIndexImpl().isUnique();
    }

	/** Sets the unique flag of the index.
	 * @param flag the flag
	 * @throws DBException if impossible
	 */
	public void setUnique(boolean flag) throws DBException {
        getIndexImpl().setUnique(flag);
	}

	//================== Columns ===============================

	/** Adds a new column to the index.
	 * @param el the column to add
	 * @throws DBException if impossible
	 */
	public void addColumn(ColumnElement el) throws DBException {
		addColumns(new ColumnElement[]{el});
	}

	/** Adds some new columns to the index.
	 * @param els the columns to add
	 * @throws DBException if impossible
	 */
	public void addColumns(final ColumnElement[] els) throws DBException {
		for (int i = 0; i < els.length; i++)
			if (getColumn(els[i].getName()) != null)
				throwAddException("FMT_EXC_AddColumn", els[i]); //NOI18N
	
   		getIndexImpl().changeColumns(els, TableElement.Impl.ADD);
	}

	/** Removes a column from the index.
	 * @param el the column to remove
	 * @throws DBException if impossible
	 */
	public void removeColumn (ColumnElement el) throws DBException {
		removeColumns(new ColumnElement[]{el});
	}

	/** Removes some columns from the index.
	 * @param els the columns to remove
	 * @throws DBException if impossible
	 */
	public void removeColumns (final ColumnElement[] els) throws DBException {
   		getIndexImpl().changeColumns(els, TableElement.Impl.REMOVE);
	}

	/** Sets the columns for this index.
	 * Previous columns are removed.
	 * @param els the new columns
	 * @throws DBException if impossible
	 */
	public void setColumns (ColumnElement[] els) throws DBException {
        if (els == null)
            throw new NullPointerException(ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle").getString("NulIndexes")); //NOI18N

        getIndexImpl().changeColumns(els, TableElement.Impl.SET);
    }

	/** Gets all columns in this index.
	 * @return the columns
	 */
	public ColumnElement[] getColumns () {
        return getIndexImpl().getColumns();
    }

	/** Find a column by name.
	 * @param name the name of the column for which to look
	 * @return the element or <code>null</code> if not found
	 */
	public ColumnElement getColumn (DBIdentifier name) {
        return getIndexImpl().getColumn(name);
	}
	
    /** This method just throws localized exception. It is used during
     * adding class element, which already exists in source.
     * @param formatKey The message format key to localized bundle.
     * @param element The element which can't be added
     * @exception DBException is alway thrown from this method.
     */
    private void throwAddException (String formatKey, ColumnElement element) throws DBException {
        //MessageFormat format = new MessageFormat(ElementFormat.bundle.getString(formatKey));
        String msg = /*format.format(new Object[] { */element.getName().getName();// });
        throw new DBException(msg);
    }

	/** Implementation of an index element.
	 * @see IndexElement
	 */
	public interface Impl extends DBMemberElement.Impl {
		/** Gets the unique flag of the index.
		 * @return true if it is a unique index, false otherwise
		 */
		public boolean isUnique ();

		/** Sets the unique flag of the index.
		 * @param flag the flag
		 * @throws DBException if impossible
		 */
		public void setUnique (boolean flag) throws DBException;

		/** Changes the set of columns.
		* @param elems the columns to change
		* @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
		* @exception DBException if the action cannot be handled
		*/
		public void changeColumns (ColumnElement[] elems, int action) throws DBException;

		/** Gets all columns.
		* @return the columns
		*/
		public ColumnElement[] getColumns ();

		/** Finds a column by name.
		* @param name the name for which to look
		* @return the column, or <code>null</code> if it does not exist
		*/
		public ColumnElement getColumn (DBIdentifier name);
	}

	static class Memory extends DBMemberElement.Memory implements Impl {
		/** Unique flag of index */
		private boolean _unique;

        /** collection of columns */
		private DBMemoryCollection.Column columns;

        /** Default constructor
         */
		Memory() {
            super();
			_unique = true;
		}

		/** Copy constructor.
		* @param column the object from which to read values
		*/
		Memory(IndexElement index) {
			super(index);
			_unique = index.isUnique();
		}

		/** Gets the unique flag of the index.
		 * @return true if it is a unique index, false otherwise
		 */
		public boolean isUnique() {
            return _unique;
        }

		/** Sets the unique flag of the index.
		 * @param flag the flag
		 */
		public void setUnique(boolean flag) {
			boolean old = _unique;

			_unique = flag;
			firePropertyChange(PROP_UNIQUE, Boolean.valueOf(old), Boolean.valueOf(flag));
		}

		/** Changes the set of elements.
		 * @param elems elements to change
         * @param action the action to do
		 */
		public synchronized void changeColumns(ColumnElement[] elems, int action) {
			initColumns();
			columns.change(elems, action);
		}

		/** Gets all columns.
		* @return the columns
		*/
    	public synchronized ColumnElement[] getColumns() {
			initColumns();
			return (ColumnElement[])columns.getElements();
    	}

		/** Finds a column with given name.
		 * @param name the name of column for which to look
		 * @return the element or null if column with such name does not exist
		 */
    	public synchronized ColumnElement getColumn(DBIdentifier name) {
			initColumns();
			return (ColumnElement)columns.getElement(name);
    	}

        /** Initializes the collection of columns.
         */
		void initColumns() {
			if (columns == null)
				columns = new DBMemoryCollection.Column(this);
		}
	}
}
