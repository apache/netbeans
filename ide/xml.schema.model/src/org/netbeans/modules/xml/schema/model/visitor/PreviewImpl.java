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

package org.netbeans.modules.xml.schema.model.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 *
 * @author Samaresh
 */
public class PreviewImpl implements Preview {

    /**
     * Collection of schema components, all of which,
     * reference the same global schema component.
     */
    private Map<SchemaComponent, List<SchemaComponent>> usages =
            new HashMap<SchemaComponent, List<SchemaComponent>>();
    
    /**
     * Returns a collection of schema components, all of which,
     * reference the same global schema component.
     * @return a Map of usages to their path from their respective schema's root.<br/>
     * Example: <br/>
     * <pre>
     * { 
     *    myElement : [mySchema > myElement], 
     *    myOtherElement : [myOtherSchema > myType > myOtherElement] 
     * }
     * </pre>
     */
    public Map<SchemaComponent, List<SchemaComponent>> getUsages() {
        return usages;
    }
        
    void addToUsage(SchemaComponent component) {        
        List<SchemaComponent> temp = new ArrayList<SchemaComponent>();
	SchemaComponent sc = component;
	while (sc != null) {
	    temp.add(sc);
	    sc = sc.getParent();
	};
        Collections.reverse(temp);
        usages.put(component, temp);
    }

}
