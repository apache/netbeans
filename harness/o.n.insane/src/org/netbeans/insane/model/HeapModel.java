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

package org.netbeans.insane.model;

import java.util.*;

/**
 * A simplified model of the Java heap. It represents relations between
 * Java objects, set of known root references and a set of types. It does not
 * represent values of the primitive object fields.
 *
 * @author Nenik
 */
public interface HeapModel {

    /**
     * Provides access to all known instances on the heap.
     *
     * @return Iterator of all Items
     */
    public Iterator<Item> getAllItems();

    /**
     * Provides access to all known instances of given type.
     *
     * @return Collection of Items of given type
     */    
    public Collection<Item> getObjectsOfType(String type);
    
    /**
     * Provides a collection of known root (static) references
     *
     * @return Collection of Strings representing the names
     *   of static fields
     */
    public Collection<String> getRoots();
    
    /* Access an object referenced from given field
     *
     * @param staticRefName the name of the static field as returned
     *   from getRoots() method.
     * @return Item representing the object at given static reference
     */
    public Item getObjectAt(String staticRefName);
    
    /* Access an object with a known ID
     *
     * @param id the id of the object
     * @return Item with given id, if such Item exists.
     * @throws IllegalArgumentException if no Item whith given id exists.
     */
    Item getItem(int id);    
}
