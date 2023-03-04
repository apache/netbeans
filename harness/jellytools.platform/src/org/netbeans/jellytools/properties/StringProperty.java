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
import org.netbeans.jellytools.properties.editors.StringCustomEditorOperator;

/** Operator serving property of type String
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class StringProperty extends Property {//TextFieldProperty {

    /** Creates a new instance of StringProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name
     */
    public StringProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return StringCustomEditorOperator */    
    public StringCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new StringCustomEditorOperator(getName());
    }
    
    /** setter for String value through Custom Editor
     * @param value String */    
    public void setStringValue(String value) {
        StringCustomEditorOperator customizer=invokeCustomizer();
        customizer.setStringValue(value);
        customizer.ok();
    }    
    
    /** getter for String value through Custom Editor
     * @return String */    
    public String getStringValue() {
        StringCustomEditorOperator customizer=invokeCustomizer();
        String s=customizer.getStringValue();
        customizer.close();
        return s;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }    
}
