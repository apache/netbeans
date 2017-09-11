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

/** Describes a directed column pair.  Because of its direction, it has
 * the notion of belonging to a table.
 */
public final class ColumnPairElement extends DBMemberElement  {
	/** the local column to which this element is associated */
	private ColumnElement _localColumn;

	/** the referenced column to which this element is associated */
	private ColumnElement _referencedColumn;

	/** Create a new column pair element represented in memory.
	 */
	public ColumnPairElement() {
		this(new Memory(), null, null, null);
	}

	/** Creates a new column pair element.
	 * @param localColumn local column of this column pair
     * @param referencedColumn referenced column of this column pair
	 * @param declaringTable declaring table of this column pair, or <code>null</code>
	 */
	public ColumnPairElement (ColumnElement localColumn,  ColumnElement referencedColumn, TableElement declaringTable) {
		this(new Memory(), localColumn, referencedColumn, declaringTable);
	}

	/** Creates a new column pair element.
	 * @param impl the pluggable implementation
	 * @param localColumn local column of this column pair
     * @param referencedColumn referenced column of this column pair
	 * @param declaringTable declaring table of this column pair, or <code>null</code>
	 */
	public ColumnPairElement (ColumnPairElement.Impl impl, ColumnElement localColumn, ColumnElement referencedColumn, TableElement declaringTable) {
		super(impl, declaringTable);
		_localColumn = localColumn;
		_referencedColumn = referencedColumn;
	}

    /** Returns the implementation for the column pair.
     * @return implementation for the column pair
     */
	final Impl getColumnPairImpl() {
        return (Impl)getElementImpl();
    }

	/** Gets the local column. 
	 * @return the local column of this column pair
	 */
	public final ColumnElement getLocalColumn() {
		return _localColumn;
	}

	/** Sets the local column. 
	 * @param ce the local column
	 */
	public final void setLocalColumn (ColumnElement ce) {
		if (_localColumn == null)
            _localColumn = ce;
	}

	/** Gets the referenced column. 
	 * @return the referenced column of this column pair
	 */
	public final ColumnElement getReferencedColumn() {
		return _referencedColumn;
	}

	/** Sets the referenced column. 
	 * @param ce the referenced column
	 */
	public final void setReferencedColumn(ColumnElement ce) {
		if (_referencedColumn == null)
            _referencedColumn = ce;
	}

    /** Gets the name of this element.
     * @return the name
     */
    public DBIdentifier getName() {
        ColumnElement lce = getLocalColumn();
        ColumnElement fce = getReferencedColumn();
        
        DBIdentifier name = DBIdentifier.create(lce.getName().getFullName() + ";" + fce.getName().getFullName()); //NOI18N
        
		return name;
	}

	/** Implementation of a reference key column element.
	 * @see DBMemberElement
	 */
	public interface Impl extends DBMemberElement.Impl {
	}

	static class Memory extends DBMemberElement.Memory implements Impl {
        /** Default constructor.
         */
		Memory() {
		}

		/** Copy constructor.
		* @param column the object from which to read values
		*/
		Memory(ColumnPairElement column) {
			super(column);
		}
	}
}
