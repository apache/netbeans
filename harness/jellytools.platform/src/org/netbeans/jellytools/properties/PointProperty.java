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
package org.netbeans.jellytools.properties;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.editors.PointCustomEditorOperator;

/** Operator serving property of type Point */
public class PointProperty extends Property {

    /** Creates a new instance of PointProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name
     */
    public PointProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return PointCustomEditorOperator */    
    public PointCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new PointCustomEditorOperator(getName());
    }
    
    /** setter for Point value through Custom Editor
     * @param x String x
     * @param y String y */    
    public void setPointValue(String x, String y) {
        PointCustomEditorOperator customizer=invokeCustomizer();
        customizer.setPointValue(x, y);
        customizer.ok();
    }        
    
    /** getter for Point value through Custom Editor
     * @return String[2] x and y coordinates */    
    public String[] getPointValue() {
        String[] value=new String[2];
        PointCustomEditorOperator customizer=invokeCustomizer();
        value[0]=customizer.getXValue();
        value[1]=customizer.getYValue();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }        
}
