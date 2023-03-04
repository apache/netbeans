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

import org.netbeans.tax.TreeAttlistDecl;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.InvalidArgumentException;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public interface AttlistDecl {

    //
    // Constraints
    //

    /**
     *
     */
    public static interface Constraints {

        public void checkAttlistDeclElementName (String elementName) throws InvalidArgumentException;

        public boolean isValidAttlistDeclElementName (String elementName);
        
        
        public void checkAttlistDeclAttributeName (String attributeName) throws InvalidArgumentException;
        
        public boolean isValidAttlistDeclAttributeName (String attributeName);
        
        
        public void checkAttlistDeclAttributeType (short type) throws InvalidArgumentException;
        
        public boolean isValidAttlistDeclAttributeType (short type);
        
        
        public void checkAttlistDeclAttributeEnumeratedType (String[] enumeratedType) throws InvalidArgumentException;
        
        public boolean isValidAttlistDeclAttributeEnumeratedType (String[] enumeratedType);
        
        
        public void checkAttlistDeclAttributeDefaultType (short defaultType) throws InvalidArgumentException;
        
        public boolean isValidAttlistDeclAttributeDefaultType (short defaultType);
        
        
        public void checkAttlistDeclAttributeDefaultValue (String defaultValue) throws InvalidArgumentException;
        
        public boolean isValidAttlistDeclAttributeDefaultValue (String defaultValue);
        
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
        public TreeAttlistDecl createAttlistDecl (String elementName);
        
    } // end: interface Creator
    
    
    //
    // Writer
    //
    
    /**
     *
     */
    public static interface Writer {
        
        public void writeAttlistDecl (TreeAttlistDecl attlistDecl) throws TreeException;
        
    } // end: interface Writer
    
}
