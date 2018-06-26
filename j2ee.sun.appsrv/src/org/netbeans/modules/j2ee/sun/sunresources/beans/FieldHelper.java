/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
