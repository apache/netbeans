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

public class ChoiceParser extends ListParser {

    /** */
    TreeElementDecl.ContentType first;


    //
    // init
    //

    public ChoiceParser (TreeElementDecl.ContentType first) {
        this.first = first;
    }

    /**
     */
    public TreeElementDecl.ContentType parseModel (ParserReader s) {
        ChoiceType cht = (ChoiceType) super.parseModel (s);
        if (first != null)
            cht.addType (first);
        return cht;
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected ChildrenType createType (ParserReader model) {
        return new ChoiceType ();
    }
    
    /**
     */
    public String getSeparator () {
        return "|"; // NOI18N
    }
    
}
