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
import java.util.Arrays;

import org.netbeans.modules.dbschema.*;

public class ForeignKeyElementImpl  extends KeyElementImpl implements ForeignKeyElement.Impl {

    private TableElementImpl tei;

    public ForeignKeyElementImpl() {
		this(null, null);
    }

    public ForeignKeyElementImpl(TableElementImpl tei, String name) {
		super(name);

        this.tei = tei;
    }

    protected DBElementsCollection initializeCollection() {
        return new DBElementsCollection(this, new ColumnPairElement[0]);
    }
  
    public ColumnPairElement[] getColumnPairs() {
        DBElement[] dbe = getColumnCollection().getElements();
        return (ColumnPairElement[]) Arrays.asList(dbe).toArray(new ColumnPairElement[dbe.length]);
    }
    
    public ColumnPairElement getColumnPair(DBIdentifier name) {
		return (ColumnPairElement) getColumnCollection().find(name);
    }
    
    public void changeColumnPairs(ColumnPairElement[] pairs,int action) throws DBException {
        getColumnCollection().changeElements(pairs, action);
    }
    
    public ColumnElement[] getColumns() {
        ColumnPairElement[] cpe = getColumnPairs();
        
        if (cpe == null || cpe.length == 0)
            return null;
        
        ColumnElement[] ce = new ColumnElement[cpe.length];
        
        for (int i = 0; i < cpe.length; i++) {
            String localColumn = cpe[i].getName().getFullName();
            int pos = localColumn.indexOf(";");
            localColumn = localColumn.substring(0, pos);

            ce[i] = ((ForeignKeyElement) element).getDeclaringTable().getColumn(DBIdentifier.create(localColumn));
        }
        
        return ce;
    }
    
    public ColumnElement getColumn(DBIdentifier name) {
        ColumnPairElement[] cpe = getColumnPairs();
        
        if (cpe == null || cpe.length == 0)
            return null;
        
        for (int i = 0; i < cpe.length; i++) {
            String localColumn = cpe[i].getName().getFullName();
            int pos = localColumn.indexOf(";");
            localColumn = localColumn.substring(0, pos);

            if (name.getName().equals(DBIdentifier.create(localColumn).getName())) //need to check
                return ((ForeignKeyElement) element).getDeclaringTable().getColumn(name);
        }
        
        return null;
    }
    
}
