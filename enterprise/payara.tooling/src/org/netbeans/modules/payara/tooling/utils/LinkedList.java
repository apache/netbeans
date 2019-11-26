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
package org.netbeans.modules.payara.tooling.utils;

/**
 * Doubly linked list with internal iterator.
 * <p/>
 * This linked list implementation allows to work with individual list elements
 * on fly without the need of external iterator.
 * <p/>
 * List is not thread safe.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class LinkedList <V> {

    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Linked list element containing stored value and links to neighbor nodes.
     */
    public static class Element<V> {

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Pointer to previous element in list. */
        private Element<V> previous;

        /** Pointer to next element in list. */
        private Element<V> next;

        /** Stored value object. */
        private V value;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Constructs an instance of linked list element and sets it's stored
         * <code>value</code> object.
         * <p/>
         * Pointers to <code>previous</code> and <code>next</code> element
         * are set to <code>null</code>.
         * <p/>
         * @param value Stored value object.
         */
        Element(V value) {
            this(value, null, null);
        }

        /**
         * Constructs an instance of linked list element and sets it's stored
         * <code>value</code> object and pointers to <code>previous</code>
         * and <code>next/<code> element.
         * <p/>
         * @param value Stored value object.
         */
        Element(V value, Element<V> previous, Element<V> next) {
            this.value = value;
            this.previous = previous;
            this.next = next;
        } 

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Clear all internal attributes (set them to <code>null</code>).
         * <p/>
         * Garbage collector helper.
         */
        void clear() {
            previous = null;
            next = null;
            value = null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** List head (first element in the list). List should be empty when this
     *  pointer is <code>null</code>. */
    private Element<V> head;

    /** List tail (last element in the list). List should be empty when this
     *  pointer is <code>null</code>. */
    private Element<V> tail;

    /** Current element in the list used by internal iterator. List should
     *  be empty when this pointer is <code>null</code>. */
    private Element<V> current;
    
    /** List size. */
    private int size;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of linked list.
     * <p/>
     * List is initialized as empty.
     */
    public LinkedList() {
        head = tail = current = null;
        size = 0;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Add element at the end (after tail) of the list.
     * <p/>
     * Current element<ul>
     * <li>won't be changed when list is not empty</li> or
     * <li>will be set to newly added element when list is empty.</li>
     * </ul>
     * @param value New value to be added at the end of the list.
     */
    public void addLast(V value) {
        if (tail == null) {
            head = tail = current = new Element<>(value);
        } else {
            tail.next = new Element<>(value, tail, null);
            tail = tail.next;
        }
        size++;
    }
    
    /**
     * Add element at the beginning (before head) of the list.
     * <p/>
     * Current element<ul>
     * <li>won't be changed when list is not empty</li> or
     * <li>will be set to newly added element when list is empty.</li>
     * </ul>
     * @param value New value to be added at the end of the list.
     */
    public void addFirst(V value) {
        if (tail == null) {
            head = tail = current = new Element<>(value);
        } else {
            head.previous = new Element<>(value, null, head);
            head = tail.previous;
        }
        size++;
    }

    /**
     * Remove last (tail) element from the list.
     * <p/>
     * Current element<ul>
     * <li>won't be changed when it is not tail of the list</li> or
     * <li>will be set to previous element when it is tail of the list.</li>
     * </ul>
     * @return Data stored in removed last element of the list. Returns
     *         <code>null</code> when list is empty.
     */
    public V removeLast() {
        if (tail == null) {
            return null;
        } else {
            Element<V> remove = tail;
            V value = tail.value;
            if (tail.previous != null) {
                tail.previous.next = null;
            } else {
                head = null;
            }
            if (current == tail) {
                current = current.previous;
            }
            tail = tail.previous;
            remove.clear();
            size--;
            return value;
        }
    }

    /**
     * Remove first (head) element from the list.
     * <p/>
     * Current element<ul>
     * <li>won't be changed when it is not head of the list</li> or
     * <li>will be set to next element when it is head of the list.</li>
     * </ul>
     * @return Data stored in removed last element of the list. Returns
     *         <code>null</code> when list is empty.
     */
    public V removeFirst() {
        if (head == null) {
            return null;
        } else {
            Element<V> remove = head;
            V value = head.value;
            if (head.next != null) {
                head.next.previous = null;
            } else {
                tail = null;
            }
            if (current == head) {
                current = current.next;
            }
            head = head.next;
            remove.clear();
            size--;
            return value;
        }
    }

    /**
     * Add element after current element in the list.
     * <p/>
     * Current element<ul>
     * <li>won't be changed when list is not empty</li> or
     * <li>will be set to newly added element when list is empty.</li>
     * </ul>
     * Tail is moved to newly added element when current element is tail.
     * <p/>
     * @param value New value to be added at the end of the list.
     */
    public void addNext(V value) {
        if (current == null) {
            head = tail = current = new Element(value);
        } else {
            Element<V> add = new Element<>(value, current, current.next);
            if (current.next != null) {
                current.next.previous = add;
            } else {
                tail = add;
            }
            current.next = add;
        }
        size++;
    }
    
    /**
     * Add element before current element in the list.
     * <p/>
     * Current element<ul>
     * <li>won't be changed when list is not empty</li> or
     * <li>will be set to newly added element when list is empty.</li>
     * </ul>
     * Head is moved to newly added element when current element is head.
     * <p/>
     * @param value New value to be added at the end of the list.
     */
    public void addPrevious(V value) {
        if (current == null) {
            head = tail = current = new Element(value);
        } else {
            Element<V> add = new Element<>(value, current.previous, current);
            if (current.previous != null) {
                current.previous.next = add;
            } else {
                head = add;
            }
            current.previous = add;
        }
        size++;
    }

    /**
     * Remove current element and set current element to previous one
     * if exists or <code>null</code> if there is no previous element.
     * <p/>
     * @return Data stored in removed current element of the list. Returns
     *         <code>null</code> when list is empty.
     */
    public V removeAndPrevious() {
        if (current == null) {
            return null;
        } else {
            Element<V> remove = current;
            V value = current.value;
            if (remove.previous != null) {
                remove.previous.next = remove.next;
                current = remove.previous;
            } else {
                head = remove.next;
                current = null;
            }
            if (remove.next != null) {
                remove.next.previous = remove.previous;
            } else {
                tail = remove.previous;
            }
            remove.clear();
            size--;
            return value;
        }
    }

    /**
     * Remove current element and set current element to next one
     * if exists or <code>null</code> if there is no next element.
     * <p/>
     * @return Data stored in removed current element of the list. Returns
     *         <code>null</code> when list is empty.
     */
    public V removeAndNext() {
        if (current == null) {
            return null;
        } else {
            Element<V> remove = current;
            V value = current.value;
            if (remove.next != null) {
                remove.next.previous = remove.previous;
                current = remove.next;
            } else {
                tail = remove.previous;
                current = null;
            }
            if (remove.previous != null) {
                remove.previous.next = remove.next;
            } else {
                head = remove.next;
            }
            remove.clear();
            size--;
            return value;
        }
    }

    /**
     * Remove current element and set current element to previous one
     * if exists or next element as fallback option.
     * <p/>
     * Current element should not be <code>null</code> except when list
     * is empty. Additional check may be required to see which direction
     * current element was moved.
     * <p/>
     * @return Data stored in removed current element of the list. Returns
     *         <code>null</code> when list is empty.
     */
    public V removeAndPreviousOrNext() {
        if (current == null) {
            return null;
        } else {
            Element<V> remove = current;
            V value = current.value;
            if (remove.previous != null) {
                remove.previous.next = remove.next;
                current = remove.previous;
            } else {
                head = remove.next;
                current = remove.next;
            }
            if (remove.next != null) {
                remove.next.previous = remove.previous;
            } else {
                tail = remove.previous;
            }
            remove.clear();
            size--;
            return value;
        }
    }

    /**
     * Remove current element and set current element to next one
     * if exists or previous element as fallback option.
     * <p/>
     * Current element should not be <code>null</code> except when list
     * is empty. Additional check may be required to see which direction
     * current element was moved.
     * <p/>
     * @return Data stored in removed current element of the list. Returns
     *         <code>null</code> when list is empty.
     */
    public V removeAndNextOrPrevious() {
        if (current == null) {
            return null;
        } else {
            Element<V> remove = current;
            V value = current.value;
            if (remove.next != null) {
                remove.next.previous = remove.previous;
                current = remove.next;
            } else {
                tail = remove.previous;
                current = remove.previous;
            }
            if (remove.previous != null) {
                remove.previous.next = remove.next;
            } else {
                head = remove.next;
            }
            remove.clear();
            size--;
            return value;
        }
    }

    /**
     * Set current element to the first (head) element.
     * <p/>
     * @return Value of <code>true</code> if first element exists
     *         or <code>false</code> otherwise.
     */
    public boolean first() {
        return (current = head) != null;
    }

    /**
     * Set current element to the last (tail) element.
     * <p/>
     * @return Value of <code>true</code> if last element exists
     *         or <code>false</code> otherwise.
     */
    public boolean last() {
        return (current = tail) != null;
    }

    /**
     * Attempt to move current pointer to next element.
     * <p/>
     * Current pointer will be moved only if there is an element after current
     * one.
     * <p/>
     * @return Value of <code>true</code> if current pointer was moved
     *         to next element or <code>false</code> otherwise.
     */
    public boolean next() {
        if (current != null && current.next != null) {
            current = current.next;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Attempt to move current pointer to previous element.
     * <p/>
     * Current pointer will be moved only if there is an element before current
     * one.
     * <p/>
     * @return Value of <code>true</code> if current pointer was moved
     *         to previous element or <code>false</code> otherwise.
     */
    public boolean previous() {
        if (current != null && current.previous != null) {
            current = current.previous;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if list is empty.
     * <p/>
     * @return Value of <code>true</code> when list is empty
     *         (contains no elements) or <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Check if there is a current element.
     * <p/>
     * @return Value of <code>true</code> when there is a current element
     *         or <code>false</code> otherwise.
     */
    public boolean isCurrent() {
        return current != null;
    }

    /**
     * Check if there is an element after current element.
     * <p/>
     * @return Value of <code>true</code> if there is an element after current
     *         element or <code>false</code> otherwise.
     */
    public boolean isNext() {
        return current != null && current.next != null;
    }

    /**
     * Check if there is an element before current element.
     * <p/>
     * @return Value of <code>true</code> if there is an element before current
     *         element or <code>false</code> otherwise.
     */
    public boolean isPrevious() {
        return current != null && current.previous != null;
    }

    /**
     * Get current size of list.
     * <p/>
     * @return Current size of list.
     */
    public int size() {
        return size;
    }

    /**
     * Get value stored in current element.
     * <p/>
     * @return Value stored in current element or <code>null</code> when list
     *         is empty.
     */
    public V getCurrent() {
        if (current != null) {
            return current.value;
        } else {
            return null;
        }
    }

    /**
     * Get value stored in first (head) element.
     * <p/>
     * @return Value stored in first (head) element or <code>null</code> when
     *         list is empty.
     */
    public V getFirst() {
        if (head != null) {
            return head.value;
        } else {
            return null;
        }
    }

    /**
     * Get value stored in last (tail) element.
     * <p/>
     * @return Value stored in last (tail) element or <code>null</code> when
     *         list is empty.
     */
    public V getLast() {
        if (tail != null) {
            return tail.value;
        } else {
            return null;
        }
    }

    /**
     * Get value stored in next element (after current).
     * <p/>
     * @return Value stored in next element or <code>null</code> when list
     *         is empty or current element is tail.
     */
    public V getNext() {
        if (current != null && current.next != null) {
            return current.next.value;
        } else {
            return null;
        }
    }

    /**
     * Get value stored in previous element (before current).
     * <p/>
     * @return Value stored in previous element or <code>null</code> when list
     *         is empty or current element is head.
     */
    public V getPrevious() {
        if (current != null && current.previous != null) {
            return current.previous.value;
        } else {
            return null;
        }
    }

    /**
     * Get string representation of list.
     * </P>
     * This iteration over the list won't affect current pointer.
     * <p/>
     * @return <code>String</code> representation of all list element from head
     *         to tail.
     */
    @Override
    public String toString() {
        Element<V> element = head;
        StringBuilder sb = new StringBuilder("[");
        while (element != null) {
            sb.append(element.value != null
                    ?  element.value.toString() : "null");
            element = element.next;
            if (element != null) {
                sb.append(',');
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Clear linked list content.
     * <p/>
     * Removes all elements from the list and sets it's <code>size</code>
     * to <code>0</code>. Also all individual elements are cleared to help
     * garbage collector.
     */
    public void clear() {
        current = head;
        while (current != null) {
            Element<V> delete = current;
            current = current.next;
            delete.clear();
        }
        head = tail = current = null;
        size = 0;
    }

}
