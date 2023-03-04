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

import org.netbeans.tax.*;
import org.netbeans.tax.decl.*;

public class SequenceParser extends ListParser {

    /** */
    TreeElementDecl.ContentType first;

    /** */
    SequenceType seqType;

    //
    // init
    //

    /** */
    public SequenceParser (TreeElementDecl.ContentType first) {
        this.first = first;
    }


    //
    // itself
    //

    /**
     */
    public TreeElementDecl.ContentType parseModel (ParserReader s) {

        if (first != null) //prepend first el of seq
            getSequenceType ().addType (first);
        
        return super.parseModel (s);
    }
    
    /**
     */
    protected ChildrenType createType (ParserReader s) {
        return getSequenceType ();
    }
    
    /**
     */
    private synchronized SequenceType getSequenceType () {
        if (seqType == null)
            seqType = new SequenceType ();
        
        return seqType;
    }
    
    /**
     */
    public String getSeparator () {
        return ","; // NOI18N
    }
    
}
