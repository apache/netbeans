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

package org.netbeans.modules.gsf.testrunner.api;

/**
 * Object wrapper which allows to assign a name to an object.
 */
public final class NamedObject {

    /** name of the object */
    public String  name;
    /** object wrapper wrapped by this <code>NamedObject</code> */
    public Object  object;

    /**
     * Creates an instance of <code>NamedObject</code>
     *
     * @param  object  object to be wrapped by this object
     * @param  name    name of this object
     */
    public NamedObject(Object object, String name) {
        if ((object == null) || (name == null)) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        this.object = object;
        this.name = name;
    }
    
    /**
     * Returns a string representation of this object.
     *
     * @return  name of the object
     */
    public String toString() {
        return name;
    }
    
    /**
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!o.getClass().equals(NamedObject.class)) {
            return false;
        }
        final NamedObject otherNamed = (NamedObject) o;
        return name.equals(otherNamed.name)
               && object.equals(otherNamed.object);
    }
    
    /**
     */
    public int hashCode() {
        return name.hashCode() + object.hashCode();
    }
    
}
