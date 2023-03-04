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


package org.netbeans.modules.i18n.java;

import java.util.Map;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;

/**
 * This is <code>I18nString</code> for java sources.
 *
 * @author  Peter Zavadsky
 * @author  Petr Kuzel
 */
public class JavaI18nString extends I18nString {

    /**
     * Arguments used by creation replacing code enclapsulating
     * in java.util.MessageFormat.format method call.
     */
    protected String[] arguments;

    /** Creates 'empty' <code>JavaI18nString</code>.*/
    public JavaI18nString(I18nSupport i18nSupport) {
        super(i18nSupport);
    }

    /**
     * Copy contructor.
     */
    protected JavaI18nString(JavaI18nString copy) {
        super(copy);
        if (copy.arguments == null) {
            return;
        }
        this.arguments = copy.arguments.clone();
    }
    
    @Override
    public void become(I18nString i18nString) {
        super.become(i18nString);        
        if(i18nString instanceof JavaI18nString) {
            JavaI18nString peer = (JavaI18nString) i18nString;
            this.setArguments(peer.arguments);    
        }        
    }   
    
    @Deprecated
    public void become(JavaI18nString i18nString) {
        super.become(i18nString);        
        
        JavaI18nString peer = i18nString;
            this.setArguments(peer.arguments);    
        }        
    
    @Override
    public Object clone() {
        return new JavaI18nString(this);
    }
    
    /** Getter for property arguments.
     * @return Value of property arguments.
     */
    public String[] getArguments() {
        if (arguments == null) {
            arguments = new String[0];
        }
        return arguments;
    }
    
    /** Setter for property arguments.
     * @param arguments New value of property arguments.
     */
    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
    
    /** 
     * Add java specific replacing values. 
     */
    @Override
    protected void fillFormatMap(Map<String,String> map) {
        map.put("identifier", ((JavaI18nSupport) getSupport()).getIdentifier()); // NOI18N

        // Arguments.
        String[] arguments = getArguments();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("new Object[] {"); // NOI18N
        
        for(int i=0; i<arguments.length; i++) {
            stringBuffer.append(arguments[i]);
            
            if(i<arguments.length - 1)
                stringBuffer.append(", "); // NOI18N
        }
        
        stringBuffer.append("}"); // NOI18N
        
        map.put("arguments", stringBuffer.toString());
    }
    
}
