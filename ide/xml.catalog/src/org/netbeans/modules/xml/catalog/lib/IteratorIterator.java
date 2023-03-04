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
 * Let a list of iterators behave as a single iterator.
 *
 * @author  Petr Kuzel
 */
public final class IteratorIterator implements Iterator {

    private Vector iterators = new Vector();

    private Iterator current = null;  //current iterator;
    private Iterator it = null;       //iterators.iterator();

    /*
     * It is set by hasNext() and cleared by next() call.
     */
    private Object next = null;       //current element

    /**
     * New iterators can be added while hasNext() or prior its first call.
     * @param it iterator, never <codE>null</null>.
     */
    public void add(Iterator it) {
        assert it != null;
        iterators.add(it);
    }

    /**
     * Unsupported operation.
     */
    public void remove() {
        throw new UnsupportedOperationException(); 
    }

    public Object next() {
       if (hasNext()) {
           Object tmp = next;
           next = null;
           return tmp;
       } else {
           throw new NoSuchElementException();
       }
    }

    public boolean hasNext() {
        if (next != null) return true;
        
        if (it == null) it = iterators.iterator();
        while (current == null) {
            if (it.hasNext()) {
                current = (Iterator) it.next();
            } else {
                return false;
            }
        }
        
        while (current.hasNext() || it.hasNext()) {
            
            // fetch next iterator if necessary
            if (current.hasNext() == false) {
                current = (Iterator) it.next();
                continue;
            } else {           
                next = current.next();
                return true;
            }
        }
        next = null;
        return false;
    }  
}
