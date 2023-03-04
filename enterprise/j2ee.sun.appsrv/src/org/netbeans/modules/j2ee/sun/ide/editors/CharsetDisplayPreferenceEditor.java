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
 * CharsetDisplayPreferenceEditor.java
 *
 * Created on March 19, 2004, 1:17 PM
 */

package org.netbeans.modules.j2ee.sun.ide.editors;
import org.openide.util.NbBundle;

/**
 *
 * @author  vkraemer
 */
public class CharsetDisplayPreferenceEditor extends LogLevelEditor{

    public static Integer DEFAULT_PREF_VAL = Integer.valueOf("1"); // NOI18N

    private Integer val = DEFAULT_PREF_VAL;


    /** Creates a new instance of CharsetDisplayPreferenceEditor */
    public CharsetDisplayPreferenceEditor() {
    }

    static String[] choices = {
        NbBundle.getMessage(CharsetDisplayPreferenceEditor.class,"VAL_CANONICAL"), // NOI18N
        NbBundle.getMessage(CharsetDisplayPreferenceEditor.class,"VAL_ALIAS_ASIDE"), // NOI18N
        NbBundle.getMessage(CharsetDisplayPreferenceEditor.class,"VAL_ALIAS"),    // NOI18N
    };
    
    public String[] getTags() {
        return choices;
    }
        
    public String getAsText() {
        return choices[val];
    }
    
    public void setAsText(String string) throws IllegalArgumentException {
        int intVal = 1; 
        if((string==null)||(string.equals(""))) // NOI18N
            throw new IllegalArgumentException();
        else
            intVal = java.util.Arrays.binarySearch(choices,string); 
        if (intVal < 0) {
            intVal = 1;
        }
        if (intVal > 2){
            intVal = 1;
        }
        String valS = String.valueOf(intVal);
        val = Integer.valueOf(valS);
        this.firePropertyChange();
    }
    
    public void setValue(Object val) {
        if (val==null){
            val=DEFAULT_PREF_VAL;
        }
        if (! (val instanceof Integer)) {
            throw new IllegalArgumentException();
        }
        
        this.val = (Integer) val;
        int ival = this.val;
        if (ival < 0 || ival > 2){
            this.val = DEFAULT_PREF_VAL;
        }
//        super.setValue(this.val);
    }
    
    public Object getValue() {
        return this.val;
    }
}
