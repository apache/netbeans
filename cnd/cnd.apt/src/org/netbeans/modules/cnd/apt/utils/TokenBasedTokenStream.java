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

import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;

/**
 *
 */
public final class TokenBasedTokenStream implements TokenStream, APTTokenStream {
    private APTToken token;
    private boolean first;
    
    /** Creates a new instance of TokenBasedTokenStream */
    public TokenBasedTokenStream(APTToken token) {
        if (token == null) {
            throw new NullPointerException("not possible to create token stream for null token"); // NOI18N
        }
        this.token = token;
        this.first = true;
    }

    @Override
    public APTToken nextToken() {
        APTToken ret;
        if (first) {
            ret = token;
            first = false;
        } else {
            ret = APTUtils.EOF_TOKEN;
        }
        return ret;
    }

    @Override
    public String toString() {
        String retValue;
        
        retValue = token.toString();
        return retValue;
    }    
}
