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

package org.netbeans.modules.xml.axi.sync;

import org.netbeans.modules.xml.axi.*;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;

        
/**
 * Abstract sync test case. The sync test cases update the
 * underlying schema model and then syncs up the AXI model
 * based on events obtained from schema model.
 *
 * Note: the AXI model must be initialized. See setUp().
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractSyncTestCase extends AbstractTestCase {
            
    /**
     * AbstractSyncTestCase
     */
    public AbstractSyncTestCase(String testName,
            String schemaFile, String globalElement) {
        super(testName, schemaFile, globalElement);
    }
        
    GlobalElement findGlobalElement(String name) {
        for(GlobalElement element : getSchemaModel().getSchema().getElements()) {
            if(element.getName().equals(name))
                return element;
        }
        return null;
    }
    
    GlobalComplexType findGlobalComplexType(String name) {
        for(GlobalComplexType type : getSchemaModel().getSchema().getComplexTypes()) {
            if(type.getName().equals(name))
                return type;
        }
        return null;
    }
    
    GlobalGroup findGlobalGroup(String name) {
        for(GlobalGroup group : getSchemaModel().getSchema().getGroups()) {
            if(group.getName().equals(name))
                return group;
        }
        return null;
    }

    GlobalAttribute findGlobalAttribute(String name) {
        for(GlobalAttribute attr : getSchemaModel().getSchema().getAttributes()) {
            if(attr.getName().equals(name))
                return attr;
        }
        return null;
    }
    
    GlobalAttributeGroup findGlobalAttributeGroup(String name) {
        for(GlobalAttributeGroup group : getSchemaModel().getSchema().getAttributeGroups()) {
            if(group.getName().equals(name))
                return group;
        }
        return null;
    }    
}
