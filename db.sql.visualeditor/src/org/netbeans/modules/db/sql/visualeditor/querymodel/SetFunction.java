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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.db.sql.visualeditor.querymodel;

import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents a SQL Set function (AVG, COUNT, MAX, MIN, SUM)
 * Example Form: SUM(Orders.Quantity), MAX(Employee.Salary), COUNT(Employee.Name)
 */

public class SetFunction extends ColumnItem implements UnaryExpression {

    public static final int NONE = 0;
    public static final int AVG = 1;
    public static final int COUNT = 2;
    public static final int MAX = 3;
    public static final int MIN = 4;
    public static final int SUM = 5;

    private int _type;
    private ColumnNode _argument;
    private Identifier _alias;

    private SetFunction() { }

    public SetFunction(int type, ColumnNode argument, Identifier alias) {
        _type = type;
        _argument = argument;
        _alias = alias;
    }

    Column getReferencedColumn() {
        return _argument;
    }

    public void getReferencedColumns(Collection columns) {
        columns.add(_argument);
    }

    public String genText(SQLIdentifiers.Quoter quoter) {
        String funcType = null;
        switch (_type) {
            case AVG:
                funcType = "AVG(";
                break;
            case COUNT:
                funcType = "COUNT(";
                break;
            case MAX:
                funcType = "MAX(";
                break;
            case MIN:
                funcType = "MIN(";
                break;
            case SUM:
                funcType = "SUM(";
                break;
            default:
                break;
        }
        funcType += _argument.genText(quoter);
        funcType += ")";
        if (_alias != null) {
            funcType += " AS " + _alias.genText(quoter);
        }
        return funcType;
    }

    public Expression findExpression(String table1, String column1, String table2, String column2) {
        return null;
    }

    /**
     * Rename the table part of the column spec
     */
    public void renameTableSpec(String oldTableSpec, String corrName) {
        _argument.renameTableSpec(oldTableSpec, corrName);
    }

    public boolean isParameterized() {
        return false;
    }

    public Expression getOperand() {
        return _argument;
    }

}
