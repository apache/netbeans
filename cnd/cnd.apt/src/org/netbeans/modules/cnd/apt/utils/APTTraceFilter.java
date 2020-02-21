/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.apt.utils;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.util.logging.Level;

/**
 * filter to print stream's tokens
 */
public class APTTraceFilter implements TokenStream {
    private final TokenStream orig;
    private final String name;
    
    public APTTraceFilter(TokenStream orig) {
        this("<unnamed filter", orig); // NOI18N
    }
    
    public APTTraceFilter(String name, TokenStream orig) {
        this.orig = orig;
        this.name = name;        
    }
    
    @Override
    public Token nextToken() throws TokenStreamException {
        Token token = orig.nextToken();
        APTUtils.LOG.log(Level.INFO, "{0} : {1}\n", new Object[] { name, token}); // NOI18N
        return token;
    }
    
    @Override
    public String toString() {
        String retValue;
        
        retValue = orig.toString();
        return retValue;
    }    
}
