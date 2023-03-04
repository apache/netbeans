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

package org.netbeans.modules.xml.xam;

/**
 * Represents reference to a component.  On writing, this indirection help serialize
 * the referenced component as an attribute string value.  On reading, the referenced
 * can be resolved on demand.
 * <p>
 * Note: Client code should always check for brokeness before access the referenced.
 *
 * @author Chris Webster
 * @author Rico Cruz
 * @author Nam Nguyen
 */

public interface Reference<T extends Referenceable> {
    /**
     * @return the referenced component. May return null if
     * #isBroken() returns true;
     */
    T get();
    
    /**
     * Returns type of the referenced.
     */
    Class<T> getType();

    /**
     * Returns true if the reference cannot be resolved in the current document
     */
    boolean isBroken();
    
    /**
     * Returns true if this reference refers to target.
     * <p>
     * Note: In some implementation, this method could be more efficient than 
     * invoking #get() for direct checking.
     */
    boolean references(T target);
    
    /**
     * @return string to use in persiting the reference as attribute string value
     * of the containing component
     */
    String getRefString();
}
