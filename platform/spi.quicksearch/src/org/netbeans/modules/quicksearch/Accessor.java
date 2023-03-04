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

package org.netbeans.modules.quicksearch;

import java.util.List;
import javax.swing.KeyStroke;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

/**
 * API trampoline pattern to hide constructors of 
 * SearchRequest and SearchResponse.
 * 
 * @author Dafe Simonek
 */
public abstract class Accessor {
    
    public static Accessor DEFAULT;
    
    static {
        // invokes static initializer of SearchRequest class
        // that will assign value to the DEFAULT field above
        Class<SearchRequest> c = SearchRequest.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
//        assert DEFAULT != null : "The DEFAULT field must be initialized";
    }
    
    public abstract SearchRequest createRequest (String text, List<? extends KeyStroke> stroke);
    
    public abstract SearchResponse createResponse (CategoryResult catResult, SearchRequest sRequest);
        
}

