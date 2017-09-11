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

import java.sql.Types;

import org.netbeans.modules.dbschema.util.SQLTypeUtil;

/** Describes a column in a table.
 */
public class ColumnElement extends DBMemberElement {
	/** Create a new column element represented in memory.
	 */
	public ColumnElement () {
		this(new Memory(), null);
	}

	/** Creates a new column element.
	 * @param impl the pluggable implementation
	 * @param declaringTable declaring table of this column, or <code>null</code>
	 */
	public ColumnElement (Impl impl, TableElement declaringTable) {
		super(impl, declaringTable);
	}

    /** Indicates whether some other object is "equal to" this one.
     * @param obj the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
	public boolean equals(Object obj) {
        Integer iThis, iArg;

        if(!(obj instanceof ColumnElement))
            return false;

	    ColumnElement ce = (ColumnElement) obj;
	    if(!getName().getFullName().equals(ce.getName().getFullName()))
            return false;

	    if(getType() != ce.getType())
            return false;

        if(isNullable() != ce.isNullable())
            return false;
            
        // handle length
        iThis = getLength();
        iArg = ce.getLength();
        if (iThis != null ^ iArg != null)
            // return false, if one length is null and the other is not null
            return false;

        if (iThis != null && iArg != null && iThis.compareTo(iArg) != 0)
            // return false, if both lengths are defined but do not compare equal
            return false;
        
        // handle scale
        iThis = getScale();
        iArg = ce.getScale();
        if (iThis != null ^ iArg != null)
            // return false, if one scale is null and the other is not null
            return false;

        if (iThis != null && iArg != null && iThis.compareTo(iArg) != 0)
            // return false, if both scales are defined but do not compare equal
            return false;

        // handle precision
        iThis = getPrecision();
        iArg = ce.getPrecision();
        if (iThis != null ^ iArg != null)
            // return false, if one precision is null and the other is not null
            return false;

        if (iThis != null && iArg != null && iThis.compareTo(iArg) != 0)
            // return false, if both precisions are defined but do not compare equal
            return false;

        return true;
	}

	/** Clone the column element.
	 * @return a new element that has the same values as the original but is represented in memory
	 */
	public Object clone () {
		return new ColumnElement(new Memory(this), null);
	}

    /** Returns the implementation for the column.
     * @return implementation for the column
     */
	final Impl getColumnImpl() {
        return (Impl)getElementImpl();
    }

	/** Get the value type of the column.
	 * @return the type
	 */
	public int getType () {
        return getColumnImpl().getType();
    }

	/** Set the value type of the column.
	 * @param type the type
	 * @throws DBException if impossible
	 */
	public void setType (int type) throws DBException {
        getColumnImpl().setType(type);
	}

    //convenience methods
    
	/** Returns whether the data type is numeric.
     * @return true if tha data type is numeric; false otherwise.
     */
	public boolean isNumericType () {
        return SQLTypeUtil.isNumeric(getType());
	}

	/** Returns whether the data type is character.
     * @return true if tha data type is character; false otherwise.
     */
	public boolean isCharacterType () {
        return SQLTypeUtil.isCharacter(getType());
	}
    
	/** Returns whether the data type is blob type.
     * @return true if tha data type is blob type; false otherwise.
     */
	public boolean isBlobType () {
        return SQLTypeUtil.isBlob(getType());
	}
	//end convenience methods
        
        /**
         * Returns whether the column is an auto-increment column
         * 
         * @return true if the column is an auto-increment column; false otherwise,
         * Also returns false if the underlying implementation does not provide
         * that information.
         */
        public boolean isAutoIncrement() {
            return getColumnImpl().isAutoIncrement();
        }

	/** Returns whether the column is nullable.
	 * @return a flag representing whether the column is nullable
	 */
	public boolean isNullable () {
        return getColumnImpl().isNullable();
    }

	/** Set whether the column is nullable.
	 * @param flag flag representing whether the column is nullable
	 * @throws DBException if impossible
	 */
	public void setNullable (boolean flag) throws DBException {
        getColumnImpl().setNullable(flag);
	}

	/** Get the length of the column - for character type fields only.
	 * @return the length, <code>null</code> if it is not a character type
	 * field or there is no length.
	 */
	public Integer getLength () {
        if (isCharacterType() || isBlobType())
            return getColumnImpl().getLength();
        else
            return null;
  }

	/** Set the length of the column - for character type fields only.
	 * @param length the length for the column if it a character type
	 * @throws DBException if impossible
	 */
	public void setLength (Integer length) throws DBException {
        if (isCharacterType() || isBlobType())
  		    getColumnImpl().setLength(length);
	}

	/** Get the precision of the column - for numeric type fields only.
	 * @return the precision, <code>null</code> if it is not a numeric type
	 * field or there is no precision.
	 */
	public Integer getPrecision () {
        if (isNumericType())
            return getColumnImpl().getPrecision();
        else
            return null;
    }

	/** Set the precision of the column - for numeric type fields only.
	 * @param precision the precision for the column if it a numeric type
	 * @throws DBException if impossible
	 */
	public void setPrecision (Integer precision) throws DBException {
        if (isNumericType())
      		getColumnImpl().setPrecision(precision);
	}

	/** Get the scale of the column - for numeric type fields only.
	 * @return the scale, <code>null</code> if it is not a numeric type
	 * field or there is no scale.
	 */
	public Integer getScale () {
        if (isNumericType())
            return getColumnImpl().getScale();
        else
            return null;
    }

	/** Set the scale of the column - for numeric type fields only.
	 * @param scale the scale for the column if it a numeric type
	 * @throws DBException if impossible
	 */
	public void setScale (Integer scale) throws DBException {
        if (isNumericType())
  		    getColumnImpl().setScale(scale);
	}

    /** Returns a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString() {
        return getName().toString();
    }

	/** Implementation of a column element.
	 * @see ColumnElement
	 */
	public interface Impl extends DBMemberElement.Impl {
		/** Get the value type of the column.
		 * @return the type
		 */
		public int getType ();

                public boolean isAutoIncrement();

		/** Set the value type of the column.
		* @param type the type
		* @throws DBException if impossible
		*/
		public void setType (int type) throws DBException;

		/** Returns whether the column is nullable.
		 * @return a flag representing whether the column is nullable
		 */
		public boolean isNullable ();

		/** Set whether the column is nullable.
		 * @param flag flag representing whether the column is nullable
		 * @throws DBException if impossible
		 */
		public void setNullable (boolean flag) throws DBException;

		/** Get the length of the column - for character type fields only.
		 * @return the length, <code>null</code> if it is not a character type
		 * field or there is no length.
		 */
		public Integer getLength ();

		/** Set the length of the column - for character type fields only.
		 * @param length the length for the column if it a character type
		 * @throws DBException if impossible
		 */
		public void setLength (Integer length) throws DBException;

		/** Get the precision of the column - for numeric type fields only.
		 * @return the precision, <code>null</code> if it is not a numeric type
		 * field or there is no precision.
		 */
		public Integer getPrecision ();

		/** Set the precision of the column - for numeric type fields only.
		 * @param precision the precision for the column if it a numeric type
		 * @throws DBException if impossible
		 */
		public void setPrecision (Integer precision) throws DBException;

		/** Get the scale of the column - for numeric type fields only.
		 * @return the scale, <code>null</code> if it is not a numeric type
		 * field or there is no scale.
		 */
		public Integer getScale ();

		/** Set the scale of the column - for numeric type fields only.
		 * @param scale the scale for the column if it a numeric type
		 * @throws DBException if impossible
		 */
		public void setScale (Integer scale) throws DBException;
	}

	static class Memory extends DBMemberElement.Memory implements Impl {
		/** Type of column */
		private int _type;

		/** Nullability flag */
		private boolean _nullable;

		/** Length of column */
		private Integer _length;

		/** Precision of column */
		private Integer _precision;

		/** Scale of column */
		private Integer _scale;
                
                /** Whether column is auto-increment */
                private boolean _autoIncrement;

        /** Default constructor.
         */
		Memory () {
            super();
			_type = Types.NULL;
		}

		/** Copy constructor.
		* @param column the object from which to read values
		*/
		Memory (ColumnElement column) {
			super(column);
			_type = column.getType();
			_nullable = column.isNullable();
			_length = column.getLength();
			_precision = column.getPrecision();
			_scale = column.getScale();
                        _autoIncrement = column.isAutoIncrement();
		}

		/** Type of the column.
		* @return the type
		*/
		public int getType () {
            return _type;
        }

		/** Setter for type of the column.
		* @param type the column type
		*/
		public void setType (int type) {
			int old = _type;

			_type = type;
			firePropertyChange (PROP_TYPE, new Integer(old), new Integer(type));
		}

		/** Returns whether the column is nullable.
		 * @return a flag representing whether the column is nullable
		 */
		public boolean isNullable () {
            return _nullable;
        }

		/** Set whether the column is nullable.
		 * @param flag flag representing whether the column is nullable
		 * @throws DBException if impossible
		 */
		public void setNullable (boolean flag) throws DBException {
			boolean old = _nullable;

			_nullable = flag;
			firePropertyChange (PROP_NULLABLE, Boolean.valueOf(old), Boolean.valueOf(flag));
		}

		/** Get the length of the column - for character type fields only.
		 * @return the length, <code>null</code> if it is not a character type
		 * field or there is no length.
		 */
		public Integer getLength () {
            return _length;
        }

		/** Set the length of the column - for character type fields only.
		 * @param length the length for the column if it a character type
		 * @throws DBException if impossible
		 */
		public void setLength (Integer length) throws DBException {
			Integer old = _length;

			_length = length;
			firePropertyChange (PROP_LENGTH, old, length);
		}

		/** Get the precision of the column - for numeric type fields only.
		 * @return the precision, <code>null</code> if it is not a numeric type
		 * field or there is no precision.
		 */
		public Integer getPrecision () {
            return _precision;
        }

		/** Set the precision of the column - for numeric type fields only.
		 * @param precision the precision for the column if it a numeric type
		 * @throws DBException if impossible
		 */
		public void setPrecision (Integer precision) throws DBException {
			Integer old = _precision;

			_precision = precision;
			firePropertyChange (PROP_PRECISION, old, precision);
		}

		/** Get the scale of the column - for numeric type fields only.
		 * @return the scale, <code>null</code> if it is not a numeric type
		 * field or there is no scale.
		 */
		public Integer getScale () {
            return _scale;
        }

		/** Set the scale of the column - for numeric type fields only.
		 * @param scale the scale for the column if it a numeric type
		 * @throws DBException if impossible
		 */
		public void setScale (Integer scale) throws DBException {
			Integer old = _scale;

			_scale = scale;
			firePropertyChange (PROP_SCALE, old, scale);
		}

                public boolean isAutoIncrement() {
                    return _autoIncrement;
                }
	}
}
