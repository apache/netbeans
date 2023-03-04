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
/*
 * FieldHelper.java
 *
 * Created on October 5, 2002, 6:20 PM
 */
package org.netbeans.modules.j2ee.sun.sunresources.beans;

import java.util.Vector;


/**
 *
 * @author  shirleyc
 */
public class FieldHelper {

    public static String[] getFieldNames(Wizard wiz) {
        FieldGroup[] groups = wiz.getFieldGroup();
        Vector vec = new Vector();
        for (int i = 0; i < groups.length; i++) {
            Field[] fields = groups[i].getField();
            for (int j = 0; j < fields.length; j++) {
                vec.add(fields[j].getName());
            }
        }
        String[] result = new String[vec.size()];
        return (String[])vec.toArray(result);
    }
    
    public static Field getField(FieldGroup group, String fieldName) {
        Field[] fields = group.getField();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(fieldName)){
                return fields[i];
            }
        }
        return null;
    }
        
    public static String getFieldType(Field fld) {
        return fld.getAttributeValue("field-type");  //NOI18N
    }
    
    public static boolean isList(Field fld) {
        return fld.getAttributeValue("field-type").equals("list");   //NOI18N
    }
    
    public static boolean isInt(Field fld) {
        return fld.getAttributeValue("field-type").equals("int");   //NOI18N
    }
    
    public static boolean isTextArea(Field fld) {
        return fld.getAttributeValue("field-type").equals("textarea");   //NOI18N
    }    
    
    public static String[] getTags(Field fld) {
        return fld.getTag().getTagItem();
    }
    
    public static String getDefaultValue(Field fld) {
        return fld.getFieldValue().getDefaultFieldValue();
    }
        
    public static String getConditionalFieldValue(Field fld, String optionName) {
        OptionValuePair[] pairs = fld.getFieldValue().getOptionValuePair();
        for (int i = 0; i < pairs.length; i++) {
            if (pairs[i].getOptionName().equals(optionName)) {
                return pairs[i].getConditionalValue();
            }
        }
        return fld.getFieldValue().getDefaultFieldValue();
    }    
    
    public static String getOptionNameFromValue(Field urlField, String connUrl) {
        String vendorName = ""; //NOI18N
        if ((connUrl != null) && (!connUrl.equals(""))) { //NOI18N
            OptionValuePair[] options = urlField.getFieldValue().getOptionValuePair();
            for (int i = 0; i < options.length; i++) {
                String condUrl = options[i].getConditionalValue();
                if (connUrl.indexOf(condUrl) != -1) {
                    return options[i].getOptionName();
                }
            }
        }
        return vendorName;
    }
    
    public static String getReplacedConditionalFieldValue(Field fld, String optionName) {
        return getConditionalFieldValue(fld, optionName).replace('#', '<').replace('$', '>');   //NOI18N
    }
    
    public static String toUrl(String str) {
        return str.replace('#', '<').replace('$', '>');   //NOI18N
    }
        
    /*
     * return all the fields in group whose name are not in fieldNames
     */
    public static String[] getRemainingFieldNames(FieldGroup group, Vector fieldNames) {
        Field[] allFields = group.getField();
        Vector vec = new Vector();
        for (int i = 0; i < allFields.length; i++) {
//            Reporter.info(allFields[i].getName());
            if (!fieldNames.contains(allFields[i].getName())) {
//                Reporter.info("contained");   //NOI18N
                vec.add(allFields[i].getName());
            }
        }
        String[] remainingFields = new String[vec.size()];
        return (String[])vec.toArray(remainingFields);
    }
}
