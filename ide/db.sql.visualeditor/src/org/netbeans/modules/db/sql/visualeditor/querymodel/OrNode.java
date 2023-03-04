/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.db.sql.visualeditor.querymodel;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents a SQL Or term in a clause
 * Example Form: ((a.x = b.y) OR (c.w = d.v))
 */
public class OrNode extends BooleanExpressionList implements Or {

    //
    // Constructors
    //
    public OrNode(List expressions) {
       _expressions = new ArrayList();
       BooleanExpressionList.flattenExpression(expressions, OrNode.class, _expressions);
    }

    //
    // Methods
    //

    // Return the Where clause as a SQL string
    public String genText(SQLIdentifiers.Quoter quoter) {
        if (_expressions==null)
            return "";    // NOI18N
        String res = " ( " + ((Expression)_expressions.get(0)).genText(quoter);    // NOI18N

        for (int i=1; i<_expressions.size(); i++)
            res += " OR " + ((Expression)_expressions.get(i)).genText(quoter);  // NOI18N

        res += " ) ";    // NOI18N

        return res;
    }

    public String toString() {
        return "";    // NOI18N
    }

}
