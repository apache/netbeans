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

package org.netbeans.modules.xml.axi.datatype;

import java.util.List;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.impl.DatatypeFactoryImpl;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleType;

/**
 *
 * @author Ayub Khan
 */
public abstract class DatatypeFactory {
    
    private static DatatypeFactory instance;
    
    /** Creates a new instance of DatatypeFactory */
    public static DatatypeFactory getDefault() {
        if(instance == null)
            instance = new DatatypeFactoryImpl();
        return instance;
    }
    
    /**
     * Creates an AXI Datatype, given a schema component like
     * global element, local element, local attribute, global attribute etc.
     */
    public abstract Datatype getDatatype(AXIModel axiModel, SchemaComponent component);
    
    /**
     * returns a list of Applicable Schema facets for the given primitive type
     */
    public abstract List<Class<? extends SchemaComponent>> getApplicableSchemaFacets(SimpleType st);
    
    /**
     * Creates an AXI Datatype, given a typeName (built-in types
     * like "string").
     */
    public abstract Datatype createPrimitive(String typeName);
}
