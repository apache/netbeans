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

import org.netbeans.tax.TreeEntityDecl;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.InvalidArgumentException;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public interface EntityDecl {

    //
    // Constraints
    //

    /**
     *
     */
    public static interface Constraints {

        public void checkEntityDeclName (String name) throws InvalidArgumentException;

        public boolean isValidEntityDeclName (String name);


        public void checkEntityDeclInternalText (String internalText) throws InvalidArgumentException;
        
        public boolean isValidEntityDeclInternalText (String internalText);
        
        
        public void checkEntityDeclPublicId (String publicId) throws InvalidArgumentException;
        
        public boolean isValidEntityDeclPublicId (String publicId);
        
        
        public void checkEntityDeclSystemId (String systemId) throws InvalidArgumentException;
        
        public boolean isValidEntityDeclSystemId (String systemId);
        
        
        public void checkEntityDeclNotationName (String notationName) throws InvalidArgumentException;
        
        public boolean isValidEntityDeclNotationName (String notationName);
        
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
        public TreeEntityDecl createEntityDecl (String name, String internalText);
        
        /**
         * @throws InvalidArgumentException
         */
        public TreeEntityDecl createEntityDecl (boolean parameter, String name, String internalText);
        
        /**
         * @throws InvalidArgumentException
         */
        public TreeEntityDecl createEntityDecl (String name, String publicId, String systemId);
        
        /**
         * @throws InvalidArgumentException
         */
        public TreeEntityDecl createEntityDecl (boolean parameter, String name, String publicId, String systemId);
        
        /**
         * @throws InvalidArgumentException
         */
        public TreeEntityDecl createEntityDecl (String name, String publicId, String systemId, String notationName);
        
    } // end: interface Creator
    
    
    //
    // Writer
    //
    
    /**
     *
     */
    public static interface Writer {
        
        public void writeEntityDecl (TreeEntityDecl entityDecl) throws TreeException;
        
    } // end: interface Writer
    
}
