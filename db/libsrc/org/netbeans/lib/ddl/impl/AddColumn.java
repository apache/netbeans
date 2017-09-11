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

package org.netbeans.lib.ddl.impl;

import java.util.*;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;

/**
* Interface of database action command. Instances should remember connection
* information of DatabaseSpecification and use it in execute() method. This is a base interface
* used heavily for sub-interfacing (it is not subclassing :)
*
* @author Slavek Psenicka
*/

public class AddColumn extends ColumnListCommand
{
    static final long serialVersionUID =8822373040877885121L;
    public AbstractTableColumn specifyColumn(String type, String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        return specifyColumn(type, name, Specification.ADD_COLUMN, false, true);
    }

    public AbstractTableColumn createColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        return specifyColumn(TableColumn.COLUMN, name, 
            Specification.ADD_COLUMN, false, true);
    }

    public TableColumn createUniqueColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.UNIQUE, name, 
            Specification.ADD_COLUMN, false, true);
        col.setObjectName(name+"_UQ"); // NOI18N
        return col;
    }

    public TableColumn createPrimaryKeyColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.PRIMARY_KEY, name, 
            Specification.ADD_COLUMN, false, true);
        col.setObjectName(name+"_PK"); // NOI18N
        return col;
    }

    public TableColumn createCheckColumn(String name, String expression)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.CHECK, name, 
            Specification.ADD_COLUMN, false, true);
        col.setObjectName(name+"_CH"); // NOI18N
        col.setCheckCondition(expression);
        return col;
    }

    public TableColumn createCheckConstraint(String name, String expression)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn col = specifyColumn(TableColumn.CHECK_CONSTRAINT, name, 
            Specification.ADD_COLUMN, false, true);
        col.setObjectName(name+"_CH"); // NOI18N
        col.setCheckCondition(expression);
        return col;
    }
}

/*
* <<Log>>
*  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         9/10/99  Slavek Psenicka 
*  4    Gandalf   1.3         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/
