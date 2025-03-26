/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.form.editors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.form.FormAwareEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Property editor for single-dimensional arrays of primitive types
 *
 * @author Jiri Vagner
 * 
 */
public class PrimitiveTypeArrayEditor extends PropertyEditorSupport 
        implements FormAwareEditor, XMLPropertyEditor{
    
    private Class valueType;    // type of edited form property
    private FormProperty formProperty;  // edited form property
    
    private static String ARR_BEGIN = "["; // NOI18N
    private static String ARR_END = "]"; // NOI18N
    private static String NULL_STR = "null"; // NOI18N    
    private static String XML_PROP_NAME = "PropertyValue"; // NOI18N
    private static String XML_ATT_NAME = "value"; // NOI18N
    
    // arrays for easy converting escape sequences
    private char[] escChars = {'\t','\b','\n','\r','\f','\'','\"','\\'}; // NOI18N
    private String[] escCharsStr = {"\\t","\\b","\\n","\\r","\\f","\\'","\\\"","\\\\"}; // NOI18N
    
    
    /** 
     *  Splits string from inplace editor into char array
     */
    private String[] splitCharArray(String body) {
            boolean reading = false;
            String tempVal = ""; // NOI18N
            char prevChar = ' '; // NOI18N
            char prevPrevChar = ' '; // NOI18N            
            ArrayList<String> list = new ArrayList<>();
            
            for (int i = 0; i < body.length(); i++) {
                char actChar = body.charAt(i);
                
                if ((actChar == '\'') && (prevChar != '\\')) {
                    if (!reading) {
                        reading = true;
                        tempVal = ""; // NOI18N
                    } else {
                        reading = false;
                        list.add(tempVal);
                        tempVal = ""; // NOI18N
                    }
                } else if ((actChar == '\'') && (prevChar == '\\') 
                        && (prevPrevChar == '\\')) {
                        // special '\\' case ...
                        reading = false;
                        list.add(tempVal);
                        tempVal = ""; // NOI18N
                } else {
                    if (reading) {
                        tempVal += actChar;
                    }
                } // NOI18N
                prevPrevChar = prevChar;
                prevChar = actChar;
            }
            return list.toArray(new String[0]);
    }
    
    /**
     *  Converts char[] into String[] with custom conversion
     */
    private Object[] toCharObjectArray(Object array) {
        Object[] result;
        char[] source = (char[]) array;
        result = new String[source.length];
        for (int i = 0; i < source.length; i++) {
            result[i] = convertChar2String(source[i]);
        } 
        return result;
    }    
    
    /**
     *  Converts char into string, takes care about escape sequencies 
     */
    private String convertChar2String(char source) {
        for (int i = 0; i < escChars.length; i++) {
            if (source == escChars[i]) {
                return "'" + escCharsStr[i] + "'"; // NOI18N
            }
        }
        return "'" + String.valueOf(source) + "'"; // NOI18N
    }

    /**
     *  Converts string into char, takes care about escape sequencies 
     */
    private char convertString2Char(String source) throws ParseException {
        if (source.length() > 1)  {
            for (int i = 0; i < escCharsStr.length; i++) {
                if (source .equals(escCharsStr[i])) {
                    return escChars[i];
                }
            }
            throw new ParseException("",0); // NOI18N
        } else if (source.length() == 1) {
            return source.charAt(0);                
        } else {
            throw new ParseException("",0); // NOI18N
        }
    }
    
    /**
     * Converts array of objects into string for inplace editor content
     */
    private String arr2Text(Object[] arr, boolean justContent) {
        StringBuilder strBuild = new StringBuilder();

        if (!justContent) {
            strBuild.append(ARR_BEGIN);            
        }

        for(int i = 0; i < arr.length; i++) {
            Object act = arr[i];
            strBuild.append(act);
            
            if (justContent && this.valueType.equals(float[].class)) {
                strBuild.append("f"); // NOI18N
            }
            if (i != (arr.length - 1)) {
                strBuild.append(", "); // NOI18N
            }
        }

        if (!justContent) {
            strBuild.append(ARR_END);
        }
        return strBuild.toString();
    }
    
    /**
     * Converts text to array of objects
     */
    private Object text2Arr(String text) throws ParseException {
        String[] parts;
        String trimText = text.trim();
        
        if ((trimText.length() == 0) || trimText.equalsIgnoreCase(NULL_STR)) {
            return null;
        }

        String body;
        if ((trimText.charAt(0) == '[') && (trimText.charAt(trimText.length()-1) == ']')) {
            body = trimText.substring(1, trimText.length()-1);
        } else {
            body = trimText; // Issue 202075
        }

        if (!valueType.equals(char[].class)) {
            List<String> tokens = new LinkedList<String>();
            StringTokenizer st = new StringTokenizer(body, ","); // NOI18N
            while (st.hasMoreTokens()) {
                tokens.add(st.nextToken());
            }
            parts = tokens.toArray(new String[0]);
        } else {
            parts = splitCharArray(body);
        }
        
        if (valueType.equals(boolean[].class)) {
            boolean[] result = new boolean[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Boolean.parseBoolean(parts[i].trim());
            }
            return result;
        } else if (valueType.equals(byte[].class)) {
            byte[] result = new byte[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Byte.parseByte(parts[i].trim());
            }
            return result;
        } else if (valueType.equals(short[].class)) {
            short[] result = new short[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Short.parseShort(parts[i].trim());
            }
            return result;
        } else if (valueType.equals(int[].class)) {
            int[] result = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Integer.parseInt(parts[i].trim());
            }
            return result;
        } else if (valueType.equals(long[].class)) {
            long[] result = new long[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Long.parseLong(parts[i].trim());
            }
            return result;
        } else if (valueType.equals(float[].class)) {
            float[] result = new float[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Float.parseFloat(parts[i].trim());
            }
            return result;
        } else if (valueType.equals(double[].class)) {
            double[] result = new double[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Double.parseDouble(parts[i].trim());
            }
            return result;
        } else if (valueType.equals(char[].class)) {
            char[] result = new char[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = convertString2Char(parts[i]);
            }
            return result;
        }
        return null;
    }
    
    /**
     * Creates array of object wrappers for easier manipulation with arrray
     * and converts it into text value
     * 
     */
    @Override
    public String getAsText() {
        if (this.getValue() != null) {
            Object[] result = new Object[0];
            if (valueType.equals(char[].class)) {
                result = toCharObjectArray(this.getValue());
            } else {
                result = Utilities.toObjectArray(getValue());                            
            }
            return arr2Text(result, false);
        } else {
            return NULL_STR;
        }
    }

    /**
     *  Sets value of edited property, shows information dialog 
     *  about parsing troubles
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(text2Arr(text));
        } catch (Exception e) {
            String msg = NbBundle.getBundle(
                    PrimitiveTypeArrayEditor.class).getString(
                    "MSG_ERR_ParseError"); // NOI18N
            throw new IllegalArgumentException(
                    String.format(msg, text, this.valueType.getSimpleName()),
                    e);
        }
    }

    /**
     * Sets edited form property and value type
     */
    @Override
    public void setContext(FormModel formModel, FormProperty property) {
        this.formProperty = property;
        if (property != null) {
            setValueType(property.getValueType());
        }
    }

    protected void setValueType(Class valueType) {
        this.valueType = valueType;
    }

    @Override
    public void updateFormVersionLevel() {
        formProperty.getPropertyContext().getFormModel()
        .raiseVersionLevel(FormModel.FormVersion.NB65,FormModel.FormVersion.NB65);
    }

    @Override
    public String getJavaInitializationString() {
        Object[] valObj;

        if (valueType.equals(char[].class)) {
            valObj = toCharObjectArray(getValue());
        } else {
            valObj = Utilities.toObjectArray(getValue());            
        }

        return "new "+valueType.getSimpleName()+" {"+arr2Text(valObj, true)+"}"; // NOI18N
    }
    
    // XMLPropertyEditor implementation    
    
    @Override
    public void readFromXML(Node element) throws IOException {
        if (!XML_PROP_NAME.equals(element.getNodeName())) {
            throw new java.io.IOException();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        try {
            String value = attributes.getNamedItem(XML_ATT_NAME).getNodeValue();
            setAsText(value);
        } catch (Exception e) {
            throw new java.io.IOException();
        }
    }

    @Override
    public Node storeToXML(Document doc) {
        org.w3c.dom.Element el = doc.createElement(XML_PROP_NAME);
        el.setAttribute(XML_ATT_NAME, getAsText());
        return el;
    }
}
