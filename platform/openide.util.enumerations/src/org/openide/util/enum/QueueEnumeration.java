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
package org.openide.util.enum;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * Enumeration that represents a queue. It allows by redefining
 * method <CODE>process</CODE> each outputed object to add other to the end of
 * queue of waiting objects by a call to <CODE>put</CODE>.
 * @deprecated JDK 1.5 treats enum as a keyword so this class was
 *             replaced by {@link org.openide.util.Enumerations#queue}.
 * @author Jaroslav Tulach, Petr Hamernik
 */
public class QueueEnumeration extends Object implements Enumeration {
    /** next object to be returned */
    private ListItem next = null;

    /** last object in the queue */
    private ListItem last = null;

    /** Processes object before it is returned from nextElement method.
    * This method allows to add other object to the end of the queue
    * by a call to <CODE>put</CODE> method. This implementation does
    * nothing.
    *
    * @see #put
    * @param o the object to be processed
    */
    protected void process(Object o) {
    }

    /** Put adds new object to the end of queue.
    * @param o the object to add
    */
    public synchronized void put(Object o) {
        if (last != null) {
            ListItem li = new ListItem(o);
            last.next = li;
            last = li;
        } else {
            next = last = new ListItem(o);
        }
    }

    /** Adds array of objects into the queue.
    * @param arr array of objects to put into the queue
    */
    public synchronized void put(Object[] arr) {
        for (int i = 0; i < arr.length; i++) {
            put(arr[i]);
        }
    }

    /** Is there any next object?
    * @return true if there is next object, false otherwise
    */
    public boolean hasMoreElements() {
        return next != null;
    }

    /** @return next object in enumeration
    * @exception NoSuchElementException if there is no next object
    */
    public synchronized Object nextElement() {
        if (next == null) {
            throw new NoSuchElementException();
        }

        Object res = next.object;

        if ((next = next.next) == null) {
            last = null;
        }

        ;
        process(res);

        return res;
    }

    /** item in linked list of Objects */
    private static final class ListItem {
        Object object;
        ListItem next;

        /** @param o the object for this item */
        ListItem(Object o) {
            object = o;
        }
    }
}
