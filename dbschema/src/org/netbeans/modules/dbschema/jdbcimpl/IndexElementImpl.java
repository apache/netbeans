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

import java.sql.*;
import java.util.*;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.util.*;

public class IndexElementImpl extends DBMemberElementImpl implements IndexElement.Impl {

    private DBElementsCollection columns;
    private TableElementImpl tei;
    private boolean _unique;

    /** Creates new IndexElementImpl */
    public IndexElementImpl() {
       this(null, null, false);
    }

    public IndexElementImpl(TableElementImpl tei, String name, boolean unique) {
		super(name);
		columns = new DBElementsCollection(tei, new ColumnElement[0]);
        
        //workaround for bug #4396371
        //http://andorra.eng:8080/cgi-bin/ws.exe/bugtraq/bug.hts?where=bugid_value%3D4396371
        Object hc = String.valueOf(columns.hashCode());
        while (DBElementsCollection.instances.contains(hc)) {
    		columns = new DBElementsCollection(tei, new ColumnElement[0]);
            hc = String.valueOf(columns.hashCode());
        }
        DBElementsCollection.instances.add(hc);

		this.tei = tei;
		_unique = unique;
    }
  
    /** Get the unique flag of the index.
     * @return true if it is a unique index, false otherwise
     */
    public boolean isUnique() {
        return _unique;
    }
  
    /** Set the unique flag of the index.
     * @param unique the flag
     * @throws DBException if impossible
     */
    public void setUnique(boolean unique) throws DBException {
        _unique = unique;
    }
  
    /** Change the set of columns.
     * @param elems the columns to change
     * @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
     * @exception DBException if the action cannot be handled
     */
    public void changeColumns(ColumnElement[] elems,int action) throws DBException {
        columns.changeElements(elems, action);
    }
  
    /** Get all columns.
     * @return the columns
     */
    public ColumnElement[] getColumns() {
        DBElement[] dbe = columns.getElements();
        return (ColumnElement[]) Arrays.asList(dbe).toArray(new ColumnElement[dbe.length]);
    }
  
    /** Find a column by name.
     * @param name the name for which to look
     * @return the column, or <code>null</code> if it does not exist
     */
    public ColumnElement getColumn(DBIdentifier name) {
		return (ColumnElement) columns.find(name);
    }
  
    protected void initColumns(LinkedList idxs) {
        LinkedList columnsList = new LinkedList();
        String name, info;
        int start, end;
        
        try {
            for (int i = 0; i < idxs.size(); i++) {
                info = idxs.get(i).toString();
                start = info.indexOf('.');
                end = info.lastIndexOf('.');

                name = info.substring(0, start);                    
                if (name.equals(this.getName().getName()))
                    columnsList.add(info.substring(start + 1, end));
            }

            for (int i = 0; i < columnsList.size(); i++) {
                ColumnElement c = ((IndexElement) element).getDeclaringTable().getColumn(DBIdentifier.create(columnsList.get(i).toString()));
                if (c != null)
                    changeColumns(new ColumnElement[] {c}, DBElement.Impl.ADD);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    
	/** Returns the table collection of this schema element.  This method
	 * should only be used internally and for cloning and archiving.
	 * @return the table collection of this schema element
	 */
	public DBElementsCollection getColumnCollection () {
		return columns;
	}

	/** Set the table collection of this claschemass element to the supplied
	 * collection.  This method should only be used internally and for
	 * cloning and archiving.
	 * @param collection the table collection of this schema element
	 */
	public void setColumnCollection (DBElementsCollection collection) {
		columns = collection;
	}

}
