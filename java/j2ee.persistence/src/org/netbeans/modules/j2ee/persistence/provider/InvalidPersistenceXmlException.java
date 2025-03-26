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

package org.netbeans.modules.j2ee.persistence.provider;

/**
 * Thrown to indicate that a persistence.xml file is not valid, e.g.
 * it could not be recognized correctly.
 * 
 * @author Erno Mononen
 */
public class InvalidPersistenceXmlException extends Exception{
    
    /**
     * The path to the invalid persistence.xml file. 
     */
    private final String path;
    
    /**
     * Creates a new instance of InvalidPersistenceXmlException
     * @parameter path the path to the invalid persistence.xml file
     */
    public InvalidPersistenceXmlException(String path) {
        this.path = path;
    }
    
    /**
     * Creates a new instance of InvalidPersistenceXmlException
     * @parameter message the detail message for the exception
     * @parameter path the path to the invalid persistence.xml file
     */
    public InvalidPersistenceXmlException(String message, String path) {
        super(message);
        this.path = path;
    }
    
    /**
     * @return the path to the invalid persistence.xml file.
     */ 
    public String getPath(){
        return path;
    }
    
    @Override
    public String toString(){
        return getClass().getName() + "[path: " + getPath() + "]";
    }
    
}
