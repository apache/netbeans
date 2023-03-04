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
/*
 * HTMLIterator.java
 *
 * Created on October 17, 2002, 7:44 PM
 */

package org.netbeans.performance.spi.html;
import java.util.*;
/** An Iterator implementation with a convenience method for getting the next
 * HTML element without casting.
 * @author  Tim Boudreau
 */
public class HTMLIterator implements Iterator  {
    Iterator i=null;
    Collection collection;
    public HTMLIterator(Collection c) {
        collection = c;
    }

    private synchronized Iterator getWrapped() {
        if (i==null) {
            i = collection.iterator();
        }
        return i;
    }

    public boolean hasNext() {
        return getWrapped().hasNext();
    }

    public Object next() {
        return getWrapped().next();
    }

    /** Get the next element of the underlying collection, cast as an HTML instance. */
    public HTML nextHTML() {
        return (HTML) getWrapped().next();
    }

    public void remove() {
        getWrapped().remove();
    }

}
