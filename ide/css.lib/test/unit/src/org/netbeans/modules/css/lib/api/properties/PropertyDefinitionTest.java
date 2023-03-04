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
package org.netbeans.modules.css.lib.api.properties;

import org.junit.Test;
import org.netbeans.modules.css.lib.CssTestBase;

/**
 *
 * @author marekfukala
 */
public class PropertyDefinitionTest extends CssTestBase {

    public PropertyDefinitionTest(String testName) {
        super(testName);
    }
    
    public void testIsAggregatedProperty() {
        assertAggregated(true, "font");
        assertAggregated(true, "background");
        
        assertAggregated(false, "padding");
        assertAggregated(false, "font-size");
        assertAggregated(false, "padding-left");
        assertAggregated(false, "background-size");
        assertAggregated(false, "background-color");
        assertAggregated(false, "azimuth");
    }
    
    private void assertAggregated(boolean expected, String propertyName) {
        PropertyDefinition propertyDefinition = Properties.getPropertyDefinition( propertyName);
        assertNotNull("Couldn't find property '" + propertyName + "'", propertyDefinition);
        assertEquals("Unexpected result for property " + propertyName, expected, Properties.isAggregatedProperty(null, propertyDefinition));
    }
}
