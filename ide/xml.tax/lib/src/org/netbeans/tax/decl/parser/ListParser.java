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

/** Parse list of ContentParticles. */
public abstract class ListParser extends MultiplicityParser implements ModelParser {

    /** Parse model content.
     * @param model parserreader without starting delimiter.
     */
    public TreeElementDecl.ContentType parseModel (ParserReader s) {

        ChildrenType type = createType (s);

        //first element
        type.addType (new ContentParticleParser ().parseModel (s));
        
        while ( !!! s.trim ().startsWith (")")) { // NOI18N
            
            if (s.startsWith (getSeparator ()) ) {
                type.addType (new ContentParticleParser ().parseModel (s));
                
            } else {
                //should not occure
                new RuntimeException ("Error in " + this); // NOI18N
                break;
            }
        }
        
        return type; // may be empty (e.g. (#PCDATA))
    }
    
    /**
     */
    protected abstract ChildrenType createType (ParserReader model);
    
    /**
     */
    protected abstract String getSeparator ();
    
    /**
     */
    protected boolean isEndMark (int ch) {
        switch (ch) {
            case ')': case -1:
                return true;
            default:
                return false;
        }
    }
    
}
