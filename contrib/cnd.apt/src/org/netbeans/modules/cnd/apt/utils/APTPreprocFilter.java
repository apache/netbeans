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

import java.util.logging.Level;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;

/**
 * filter out preprocessor tokens from original token stream
 */
public class APTPreprocFilter implements TokenStream, APTTokenStream {

    private final TokenStream orig;

    /**
     * Creates a new instance of APTCommentsFilter
     */
    public APTPreprocFilter(TokenStream orig) {
        this.orig = orig;
    }

    @Override
    public APTToken nextToken() {
        try {
            APTToken next = (APTToken) orig.nextToken();
            while (APTUtils.isPreprocessorToken(next)) {
                next = (APTToken) orig.nextToken();
            }
            return next;
        } catch (TokenStreamException ex) {
            // IZ#163088 : unexpected char
            APTUtils.LOG.log(Level.SEVERE, ex.getMessage());
            return APTUtils.EOF_TOKEN;
        }
    }

    @Override
    public String toString() {
        String retValue;

        retValue = orig.toString();
        return retValue;
    }
    
}
