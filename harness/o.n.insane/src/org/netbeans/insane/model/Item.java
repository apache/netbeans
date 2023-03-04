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

/** A representation of a single Object on a heap.
 *
 * @author Nenik
 */
public interface Item {

    /**
     *  @return the type (class name) of the object.
     */
    public String getType();

    /**
     * @return the estimated size of the object represented by this Item
     */
    public int getSize();

    /**
     * @return the value of the object. For Items representing object of type
     * "[C" (char array), returns the approximate content of the array. For
     * other types returns null.
     */
    public String getValue();
    
    /**
     * Provides an enumeration of all outgoing references, that is, content of
     * all non-null reference field of the represented object.
     *
     * @return Enumeration of Items representing objects referenced from object
     * of this Item.
     */
    public Enumeration<Item> outgoing();
    
    /**
     * Provides an enumeration of all incomming references.
     *
     * @return Enumeration of Items representing objects that references object
     * of this Item or String representing static fields.
     */
    public Enumeration<Object> incomming();
    
    
    /**
     * Provides an unique integer identification of given object.
     *
     * @return unique identification of the object represented by this Item.
     */
    public int getId();
}
