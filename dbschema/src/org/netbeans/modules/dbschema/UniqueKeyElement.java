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

/** Describes a unique key in a table.
 */
public final class UniqueKeyElement extends KeyElement {
	/** the index to which this element is associated */
	private IndexElement _associatedIndex;

	/** Creates a new unique key element represented in memory.
	 */
	public UniqueKeyElement() {
		this(new Memory(), null, null);
	}

	/** Creates a new unique key element.
	 * @param impl the pluggable implementation
	 * @param declaringTable declaring table of this unique key, or <code>null</code>
     * @param associatedIndex the associated index
	 */
	public UniqueKeyElement(Impl impl, TableElement declaringTable, IndexElement associatedIndex) {
		super(impl, declaringTable);

        _associatedIndex = associatedIndex;
	}

    /** Returns the implementation for the unique key.
	 * @return implementation for the unique key
	 */
	final Impl getUniqueKeyImpl() {
        return (Impl)getElementImpl();
    }

	/** Gets the associated index of the unique key.
	 * @return the associated index for this unique key, <code>null</code>
	 * if unattached
	 */
	public IndexElement getAssociatedIndex() {
        return _associatedIndex;
    }

	/** Sets the associated index of the unique key.
	 * @param the associated index for this unique key
	 * @throws DBException if impossible
	 */
	public void setAssociatedIndex(IndexElement index) throws DBException {
		_associatedIndex = index;
	}

	/** Gets the primary key flag of the unique key.
	 * @return true if this unique key is a primary key, false otherwise
	 */
	public boolean isPrimaryKey() {
        return getUniqueKeyImpl().isPrimaryKey();
    }

	/** Sets the primary key flag of the unique key.
	 * @param flag the flag
	 * @throws DBException if impossible
	 */
	public void setPrimaryKey (boolean flag) throws DBException {
		getUniqueKeyImpl().setPrimaryKey(flag);
	}

	/** Implementation of a unique key element.
	 * @see KeyElement
	 */
	public interface Impl extends KeyElement.Impl {
		/** Gets the primary key flag of the unique key.
		 * @return true if this unique key is a primary key, false otherwise
		 */
		public boolean isPrimaryKey ();

		/** Sets the primary key flag of the unique key.
		 * @param flag the flag
		 * @throws DBException if impossible
		 */
		public void setPrimaryKey (boolean flag) throws DBException;
	}

	static class Memory extends KeyElement.Memory implements Impl {
		/** Primary key flag of key */
		private boolean _pk;

        /** Default constructor
         */
		Memory () {
			_pk = false;
		}

		/** Copy constructor.
		* @param column the object from which to read values
		*/
		Memory (UniqueKeyElement key) {
			super(key);
			_pk = key.isPrimaryKey();
		}

		/** Gets the primary key flag of the unique key.
		 * @return true if this unique key is a primary key, false otherwise
		 */
		public boolean isPrimaryKey() {
            return _pk;
        }

		/** Sets the primary key flag of the unique key.
		 * @param flag the flag
		 * @throws DBException if impossible
		 */
		public void setPrimaryKey (boolean flag) throws DBException {
			boolean old = _pk;

			_pk = flag;
			firePropertyChange(PROP_PK, Boolean.valueOf(old), Boolean.valueOf(flag));
		}
	}
}
