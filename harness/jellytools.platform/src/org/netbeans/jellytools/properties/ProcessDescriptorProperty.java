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
import org.netbeans.jellytools.properties.editors.ProcessDescriptorCustomEditorOperator;

/** Operator serving property of type ProcessDescriptor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ProcessDescriptorProperty extends Property {

    /** Creates a new instance of ProcessDescriptorProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name
     */
    public ProcessDescriptorProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return ProcessDescriptorCustomEditorOperator */    
    public ProcessDescriptorCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new ProcessDescriptorCustomEditorOperator(getName());
    }
    
    /** setter for ProcessDescriptor value through Custom Editor
     * @param process String process command line
     * @param arguments String process arguments */    
    public void setProcessDescriptorValue(String process, String arguments) {
        ProcessDescriptorCustomEditorOperator customizer=invokeCustomizer();
        customizer.setProcess(process);
        customizer.setArguments(arguments);
        customizer.ok();
    }        
    
    /** getter for ProcessDescriptor value through Custom Editor
     * @return String[3] process command line, process arguments, arguments keys */    
    public String[] getProcessDescriptorValue() {
        String[] value=new String[3];
        ProcessDescriptorCustomEditorOperator customizer=invokeCustomizer();
        value[0]=customizer.getProcess();
        value[1]=customizer.getArguments();
        value[2]=customizer.getArgumentKey();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }        
}
