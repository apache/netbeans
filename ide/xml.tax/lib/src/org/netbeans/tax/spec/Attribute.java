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
package org.netbeans.tax.spec;

import org.netbeans.tax.TreeAttribute;
import org.netbeans.tax.TreeName;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.InvalidArgumentException;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public interface Attribute {

    //
    // Constraints
    //

    /**
     *
     */
    public static interface Constraints {

        public void checkAttributeName (TreeName treeName) throws InvalidArgumentException;

        public boolean isValidAttributeName (TreeName treeName);
        
        
        public void checkAttributeValue (String value) throws InvalidArgumentException;
        
        public boolean isValidAttributeValue (String value);
        
    } // end: interface Constraints
    
    
    //
    // Creator
    //
    
    /**
     *
     */
    public static interface Creator {
        
        /**
         * @throws InvalidArgumentException
         */
        public TreeAttribute createAttribute (String name, String value);
        
        
    } // end: interface Creator
    
    
    //
    // Writer
    //
    
    /**
     *
     */
    public static interface Writer {
        
        public void writeAttribute (TreeAttribute attribute) throws TreeException;
        
    } // end: interface Writer
    
    
    //
    // Value
    //
    
    /**
     *
     */
    public static interface Value {
        
    } // end: intereface Value
    
}
