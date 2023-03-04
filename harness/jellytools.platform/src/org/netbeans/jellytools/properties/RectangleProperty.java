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
import org.netbeans.jellytools.properties.editors.RectangleCustomEditorOperator;

/** Operator serving property of type Rectangle
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class RectangleProperty extends Property {

    /** Creates a new instance of RectangleProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name
     */
    public RectangleProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return RectangleCustomEditorOperator */    
    public RectangleCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new RectangleCustomEditorOperator(getName());
    }
    
    /** setter for Rectangle value through Custom Editor
     * @param x String x
     * @param y String y
     * @param width String width
     * @param height String height */    
    public void setRectangleValue(String x, String y, String width, String height) {
        RectangleCustomEditorOperator customizer=invokeCustomizer();
        customizer.setRectangleValue(x, y, width, height);
        customizer.ok();
    }        
    
    /** getter for Rectangle value through Custom Editor
     * @return String[4] x, y, width and height */    
    public String[] getRectangleValue() {
        String[] value=new String[4];
        RectangleCustomEditorOperator customizer=invokeCustomizer();
        value[0]=customizer.getXValue();
        value[1]=customizer.getYValue();
        value[2]=customizer.getWidthValue();
        value[3]=customizer.getHeightValue();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }        
}
