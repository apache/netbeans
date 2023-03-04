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
package org.netbeans.modules.xml.wsdl.validator;

import java.util.Collection;
import java.util.Hashtable;

import org.netbeans.modules.xml.wsdl.validator.spi.ValidatorSchemaFactory;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

public class ValidatorSchemaFactoryRegistry {
    
    private static ValidatorSchemaFactoryRegistry registry;
    private Hashtable<String, ValidatorSchemaFactory> schemaFactories;
    
    private ValidatorSchemaFactoryRegistry() {
        initialize();
    }
    
    public static ValidatorSchemaFactoryRegistry getDefault() {
        if (registry == null) {
            registry = new ValidatorSchemaFactoryRegistry();
        }
        return registry;
    }
    
    private void initialize() {
        schemaFactories = new Hashtable<String, ValidatorSchemaFactory>();
        Result<ValidatorSchemaFactory> lookupResult = Lookup.getDefault().lookupResult(ValidatorSchemaFactory.class);
        lookupResult.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                  refreshServices();
            }
        });
        refreshServices();
    }
     
    public ValidatorSchemaFactory getValidatorSchemaFactory(String namespace) {
        return schemaFactories.get(namespace);
    }
    
    public Collection<ValidatorSchemaFactory> getAllValidatorSchemaFactories() {
        return ((Hashtable<String, ValidatorSchemaFactory>) schemaFactories.clone()).values();
    }

    private void refreshServices() {
        schemaFactories.clear();

        for (ValidatorSchemaFactory factory : Lookup.getDefault().lookupAll(ValidatorSchemaFactory.class)){
            schemaFactories.put(factory.getNamespaceURI(), factory);
        }
    }
}
