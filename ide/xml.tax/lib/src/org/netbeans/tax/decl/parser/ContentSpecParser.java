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
package org.netbeans.tax.decl.parser;

import org.netbeans.tax.TreeElementDecl;
import org.netbeans.tax.decl.*;

/** Root parser */
public class ContentSpecParser extends MultiplicityParser implements ModelParser {

    /**
     */
    public TreeElementDecl.ContentType parseModel (ParserReader model) {
        ParserReader s = model.trim ();
        if (s.startsWith ("EMPTY")) { // NOI18N
            return new EMPTYType ();
        } else if (s.startsWith ("ANY")) { // NOI18N
            return new ANYType ();
        } else if (s.startsWith ("(")) { // NOI18N
            if (s.trim ().startsWith ("#PCDATA")) { // NOI18N
                return new MixedParser ().parseModel (s);
            } else {
                return new ChildrenParser ().parseModel (s);
            }
        } else {
            //grammar does not allow it!!!
            //let others skip it
            return null;
        }
    }
    
}
