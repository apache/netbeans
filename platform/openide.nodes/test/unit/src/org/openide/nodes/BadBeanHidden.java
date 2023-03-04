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

package org.openide.nodes;

import java.beans.*;

/**
 * @author  phrebejk
 */
public class BadBeanHidden extends Object implements java.io.Serializable {

    /** Holds value of property indexedProperty. */
    private String[] indexedProperty;

    /** Creates new BadBeanHidden */
    public BadBeanHidden() {

    }

    /** Indexed getter for property indexedProperty.
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE>.
     */
    public String getIndexedProperty(int index) {
        return this.indexedProperty[index];
    }    
    
    /** Indexed setter for property indexedProperty.
     * @param index Index of the property.
     * @param indexedProperty New value of the property at <CODE>index</CODE>.
     
    public void setIndexedProperty(int index, String indexedProperty) {
        this.indexedProperty[index] = indexedProperty;
    }
    */    
    
}
