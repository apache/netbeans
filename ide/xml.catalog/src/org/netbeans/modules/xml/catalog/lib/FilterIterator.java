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
package org.netbeans.modules.xml.catalog.lib;

import java.util.*;

/**
 * Filter backend iterator by appling a filter rule.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class FilterIterator implements Iterator {

    private final Iterator peer;
    private final Filter filter;

    /*
     * Holds candidate for next() call. It is nulledt by the next() call.
     */
    private Object next;

    public FilterIterator(Iterator it, Filter filter) {
        if (it == null || filter == null)
            throw new IllegalArgumentException("null not allowed"); // NOI18N
        peer = it; 
        this.filter = filter;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    /*
     * Retunr <code>next</code> element if available and reset it.
     */
    public Object next() {
        if (hasNext()) {
           Object ret = next;
           next = null;
           return ret;
        } else {
           throw new NoSuchElementException();
        }
    }

    /*
     * Determine if there is a next element. Put it in <code>next</code> field.
     */
    public boolean hasNext() {
        if (next != null) return true;

        while (peer.hasNext()) {
            next = peer.next();
            if (filter.accept(next)) return true;
        }
        next = null;
        return false;
    }
    
    public static interface Filter {
        public boolean accept(Object obj);
    }
}
