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

package org.netbeans.modules.db.test.jdbcstub;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.j2ee.persistence.editor.completion.*;
import org.netbeans.test.stub.api.StubDelegate;

/**
 *
 * @author Andrei Badea
 */
public class ResultSetImpl extends StubDelegate {
    
    private List/*<List<Object>>*/ columns;
    private Map/*<String, List<Object>*/ names2iterators = new HashMap();
    private Map/*<String, Object>*/ names2values; // current row values

    public ResultSetImpl(List columns) {
        this.columns = columns;

        for (Iterator it = columns.iterator(); it.hasNext();) {
            List column = (List)it.next();
            Iterator columnIterator = column.iterator();
            String columnName = columnIterator.next().toString();
            names2iterators.put(columnName, columnIterator);
        }
    }

    public boolean next() {
        if (names2values != null) {
            names2values.clear();
        } else {
            names2values = new HashMap();
        }
        
        Iterator it = names2iterators.entrySet().iterator();
        if (!it.hasNext()) {
            return false;
        }

        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String columnName = (String)entry.getKey();
            Iterator columnIterator = (Iterator)entry.getValue();

            if (!columnIterator.hasNext()) {
                return false;
            }

            Object value = columnIterator.next();
            names2values.put(columnName, value);
        }
        
        return true;
    }
    
    public Object getObject(String columnName) throws SQLException {
        if (names2values == null) {
            throw new SQLException("The next() method has not been called yet");
        }
        if (!names2values.containsKey(columnName)) {
            throw new SQLException("Unknown column name " + columnName + ".");
        }
        return names2values.get(columnName);
    }
    
    public short getShort(String columnName) throws SQLException {
        Object value = getObject(columnName);
        if (value instanceof Short) {
            return ((Short)value).shortValue();
        } else {
            throw new SQLException(value + "is not a short.");
        }
    }
    
    public int getInt(String columnName) throws SQLException {
        Object value = getObject(columnName);
        if (value instanceof Integer){
            return ((Integer)value).intValue();
        } else {
            throw new SQLException(value + " is not an int.");
        }
    }
    
    public boolean getBoolean(String columnName) throws SQLException {
        Object value = getObject(columnName);
        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        } else {
            throw new SQLException(value + " is not a boolean.");
        }
    }

    public String getString(String columnName) throws SQLException {
        Object value = getObject(columnName);
        return value != null ? value.toString() : null;
    }
    
    public void close() {
    }
}
