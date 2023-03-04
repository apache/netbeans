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

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents an identifier (schema/table/column name)
 */
public class Identifier {
    
    // Fields
    private String      _name;
    private boolean     _delimited;
    
    // Constructors
    
    // Create an Identifier with delimiter status explicitly specified.
    // Only occurs when the parser has that information
    public Identifier(String name, boolean delimited) {
        _name = name;
        _delimited = delimited;
    }
    
    
    // Create an Identifier with delimiter status decided heuristically,
    // depending whether the name contains any special characters
    public Identifier(String name) {
        _name=name;
        _delimited = needsDelimited(name);
    }
    
    
    // Accessors
    
    public String genText(SQLIdentifiers.Quoter quoter) {
        return quoter.quoteIfNeeded(_name);
        
//        if (_delimited) {
//	    String delimiter = qbMetaData.getIdentifierQuoteString();
//	    return delimiter + _name + delimiter;
//	} else {
//            return _name;
//	}
    }
    
    
    public String getName() {
        return _name;
    }
    
    
    /**
     * Returns true if the argument contains any non-word characters, which
     * will require it to be delimited.
     */
    private boolean needsDelimited(String name) {
        //        String[] split=name.split("\\W");
        //        return (split.length>1);
        // For consistency with Netbeans, mark all Identifiers as delimited for now.  
        // See IZ# 87920.
        return true;
    }
    
}

