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

package org.netbeans.modules.xml.wsdl.validator.spi;

import javax.xml.transform.Source;
import org.w3c.dom.ls.LSResourceResolver;


/**
 * Factory for getting the schema inputstream.
 * This will be plugged in into WSDLSchemaValidator to support 
 * extensibility element schema validation. 
 * 
 *
 * @author Shivanand Kini
 * 
 */
public abstract class ValidatorSchemaFactory {
    /**
     * Returns the targetnamespace of the schema
     */
    public abstract String getNamespaceURI();
    
  
    /**
     * Returns the Inputstream related to this schema
     */
    public abstract Source getSchemaSource();
    
    /**
     * Returns the LSResourceResolver related to this schema
     * for resolution of resources defined in schema like import location etc
     */
    public  LSResourceResolver getLSResourceResolver() {
        return null;
    }
    
    
            
}
