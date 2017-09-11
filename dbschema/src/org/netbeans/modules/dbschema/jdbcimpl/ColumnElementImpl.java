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

package org.netbeans.modules.dbschema.jdbcimpl;

import org.netbeans.modules.dbschema.*;

public class ColumnElementImpl extends DBMemberElementImpl implements ColumnElement.Impl {

    protected int _type;
    protected boolean _isNullable;
    protected Integer _length;
    protected Integer _precision;
    protected Integer _scale;
    protected boolean _isAutoIncrement;

    /** Creates new ColumnElementImpl */
    public ColumnElementImpl() {
    }

	/** Creates new ColumnElementImpl with the specified name */
    public ColumnElementImpl (String name) {
        super(name);
	}

    /** Creates new ColumnElementImpl */
    public ColumnElementImpl(String name, String type, String isNullable, boolean isAutoIncrement, String size, String decimal) {
        super(name);
        
        _type = new Integer(type).intValue();
        _isAutoIncrement = isAutoIncrement;

        int nullable = new Integer(isNullable).intValue();
 
/*
        if (isNullable.trim().equals("YES")) //NOI18N
            _isNullable = true;
        else
            _isNullable = false;
*/
        if (nullable == 0)
            //not allows null (0)
            _isNullable = false;
        else
            //allows null (1) or nobody knows (2)
            _isNullable = true;
        
        if (size != null)
            _length = new Integer(size);
        else
            _length = null;

        if (size != null)
            _precision = new Integer(size);
        else
            _precision = new Integer(0);

        if (decimal != null)
            _scale =  new Integer(decimal);
        else
            _scale = null;
    }
  
    /** Get the value type of the column.
     * @return the type
     */
    public int getType() {
        return _type;
    }
  
    /** Set the value type of the column.
     * @param type the type
     * @throws DBException if impossible
     */
    public void setType(int type) throws DBException {
        _type = type;
    }
  
    /** Returns whether the column is nullable.
     * @return a flag representing whether the column is nullable
     */
    public boolean isNullable() {
        return _isNullable;
    }
  
    /** Set whether the column is nullable.
     * @param flag flag representing whether the column is nullable
     * @throws DBException if impossible
     */
    public void setNullable(boolean isNullable) throws DBException {
        _isNullable = isNullable;
    }
  
    /** Get the length of the column - for character type fields only.
     * @return the length, <code>null</code> if it is not a character type
     * field or there is no length.
     */
    public Integer getLength() {
        return _length;
    }
  
    /** Set the length of the column - for character type fields only.
     * @param length the length for the column if it a character type
     * @throws DBException if impossible
     */
    public void setLength(Integer length) throws DBException {
        _length = length;
    }
  
    /** Get the precision of the column - for numeric type fields only.
     * @return the precision, <code>null</code> if it is not a numeric type
     * field or there is no precision.
     */
    public Integer getPrecision() {
        return _precision;
    }
  
    /** Set the precision of the column - for numeric type fields only.
     * @param precision the precision for the column if it a numeric type
     * @throws DBException if impossible
     */
    public void setPrecision(Integer precision) throws DBException {
        _precision = precision;
    }
  
    /** Get the scale of the column - for numeric type fields only.
     * @return the scale, <code>null</code> if it is not a numeric type
     * field or there is no scale.
     */
    public Integer getScale() {
        return _scale;
    }
  
    /** Set the scale of the column - for numeric type fields only.
     * @param scale the scale for the column if it a numeric type
     * @throws DBException if impossible
     */
    public void setScale(Integer scale) throws DBException {
        _scale = scale;
    }

    public boolean isAutoIncrement() {
        return _isAutoIncrement;
    }
}
