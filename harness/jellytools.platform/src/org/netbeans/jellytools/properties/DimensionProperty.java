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
import org.netbeans.jellytools.properties.editors.DimensionCustomEditorOperator;

/** Operator serving property of type Dimension
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class DimensionProperty extends Property {

    /** Creates a new instance of DimensionProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name
     */
    public DimensionProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return DimensionCustomEditorOperator */    
    public DimensionCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new DimensionCustomEditorOperator(getName());
    }
    
    /** setter for Dimension valuethrough Custom Editor
     * @param width String width
     * @param height String height */    
    public void setDimensionValue(String width, String height) {
        DimensionCustomEditorOperator customizer=invokeCustomizer();
        customizer.setDimensionValue(width, height);
        customizer.ok();
    }        
    
    /** getter for Dimension valuethrough Custom Editor
     * @return String[2] width and height */    
    public String[] getDimensionValue() {
        String[] value=new String[2];
        DimensionCustomEditorOperator customizer=invokeCustomizer();
        value[0]=customizer.getWidthValue();
        value[1]=customizer.getHeightValue();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }         
}
