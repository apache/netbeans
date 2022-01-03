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

import java.util.Iterator;
import java.util.LinkedList;
import org.netbeans.modules.cnd.antlr.TokenStream;
import java.util.List;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;

/**
 * implementation of TokenStream based on list
 * passed list is unchanged
 */
public final class LinkedListBasedTokenStream implements TokenStream, APTTokenStream {
    private final List<APTToken> tokens;
    private final Iterator<APTToken> iterator;
    /** Creates a new instance of ListBasedTokenStream */
    public LinkedListBasedTokenStream(List<APTToken> tokens) {
        assert(tokens != null) : "not valid to pass null list"; // NOI18N
        assert(tokens.getClass() == LinkedList.class || tokens.isEmpty()) : "Only linked list";
        this.tokens = tokens;
        iterator = tokens.iterator();
    }

    @Override
    public APTToken nextToken() {
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return APTUtils.EOF_TOKEN;
    }   

    @Override
    public String toString() {
        return APTUtils.debugString(new LinkedListBasedTokenStream(tokens)).toString();
    }
    
    //public List<APTToken> getList() {
    //    return tokens;
    //}
}
