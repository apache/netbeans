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
import java.util.List;
import java.util.LinkedList;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;

/**
 * implementation of TokenStream based on list
 * passed list is unchanged
 */
public final class ListBasedTokenStream implements TokenStream, APTTokenStream {
    private final List<APTToken> tokens;
    private final int size;
    private int position;
    /** Creates a new instance of ListBasedTokenStream */
    public ListBasedTokenStream(List<APTToken> tokens) {
        assert(tokens != null) : "not valid to pass null list"; // NOI18N
        assert(tokens.getClass() != LinkedList.class) : "Only list";
        this.tokens = tokens;
        this.size = tokens.size();
        position = 0;
    }

    @Override
    public APTToken nextToken() {
        if (position < size) {
            return tokens.get(position++);
        } else {
            return APTUtils.EOF_TOKEN;
        }
    }   

    @Override
    public String toString() {
        return APTUtils.debugString(new ListBasedTokenStream(tokens)).toString();
    }
    
    //public List<APTToken> getList() {
    //    return tokens;
    //}
}
