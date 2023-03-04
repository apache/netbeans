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
import org.netbeans.jellytools.properties.editors.FontCustomEditorOperator;

/** Operator serving property of type Font
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class FontProperty extends Property {

    /** String constant for plain font style */
    public static final String STYLE_PLAIN = FontCustomEditorOperator.STYLE_PLAIN;
    /** String constant for bold font style */
    public static final String STYLE_BOLD = FontCustomEditorOperator.STYLE_BOLD; 
    /** String constant for italic font style */    
    public static final String STYLE_ITALIC = FontCustomEditorOperator.STYLE_ITALIC; 
    /** String constant for bold italic font style */    
    public static final String STYLE_BOLDITALIC = FontCustomEditorOperator.STYLE_BOLDITALIC;
   
    /** Creates a new instance of FontProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name 
     */
    public FontProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return FontCustomEditorOperator */    
    public FontCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new FontCustomEditorOperator(getName());
    }
    
    /** setter for Font value through Custom Editor
     * @param fontName String font name
     * @param fontStyle String font style
     * @param fontSize String font size */    
    public void setFontValue(String fontName, String fontStyle, String fontSize) {
        FontCustomEditorOperator customizer=invokeCustomizer();
        if (fontName!=null)
            customizer.setFontName(fontName);
        if (fontStyle!=null)
            customizer.setFontStyle(fontStyle);
        if (fontSize!=null)
            customizer.setFontSize(fontSize);
        customizer.ok();
    }        
    
    /** getter for Font value through Custom Editor
     * @return String[3] font name, font style and font size */    
    public String[] getFontValue() {
        String[] value=new String[3];
        FontCustomEditorOperator customizer=invokeCustomizer();
        value[0]=customizer.getFontName();
        value[1]=customizer.getFontStyle();
        value[2]=customizer.getFontSize();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }        
}
