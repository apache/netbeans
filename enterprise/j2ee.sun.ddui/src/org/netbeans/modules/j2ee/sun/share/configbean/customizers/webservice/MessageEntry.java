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
 * MessageEntry.java
 *
 * Created on May 17, 2006, 6:28 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.JavaMethod;
import org.netbeans.modules.j2ee.sun.dd.api.common.Message;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity;
import org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;

import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.openide.util.NbBundle;


/**
 *
 * @author  Peter Williams
 */
public class MessageEntry extends GenericTableModel.TableEntry {

    public static final String OPERATION_ENTRY = "OperationName"; // NOI18N
    public static final String METHOD_ENTRY = "MethodName"; // NOI18N
    
    /** Pattern to split a string representing a JavaMethod up into it's component parts.
     *  This simply breaks the string up on whitespace, commas, left/right parentheses
     *  and left/right square brackets.
     */
    public static final Pattern methodSplitter = Pattern.compile("[\\s,\\(\\)\\[\\]]+"); // NOI18N
    
    private boolean saveAsOperation;
    
    public MessageEntry(boolean useOperations) {
        super(null, MessageSecurity.MESSAGE, NbBundle.getBundle(
                "org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.Bundle"),
                useOperations ? OPERATION_ENTRY : METHOD_ENTRY, true, true);
        
        saveAsOperation = useOperations;
    }

    public Object getEntry(CommonDDBean parent) {
        Object result = null;

        CommonDDBean [] message = (CommonDDBean []) parent.getValues(propertyName);
        if(message != null && message.length > 0 && message[0] != null) {
            if(saveAsOperation) {
                result = message[0].getValue(Message.OPERATION_NAME);
            } else {
                result = methodToString((JavaMethod) message[0].getValue(Message.JAVA_METHOD));
                if(!Utils.notEmpty((String) result)) {
                    result = message[0].getValue(Message.OPERATION_NAME);
                }
            }
        }

        return result;
    }
    
    public void setEntry(CommonDDBean parent, Object value) {
        // Set blank strings to null.  This object also handles message-security-binding
        // though, so we have to check it out.        
        if(value instanceof String && ((String) value).length() == 0) {
            value = null;
        }

        CommonDDBean [] m = (CommonDDBean []) parent.getValues(propertyName); 
        if(value != null) {
            if(m == null || m.length == 0 || m[0] == null) {
                MessageSecurity ms = (MessageSecurity) parent;
                m = new Message [] { ms.newMessage() };
                parent.setValue(propertyName, m);
            }

            if(saveAsOperation) {
                m[0].setValue(Message.OPERATION_NAME, value);
            } else {
                m[0].setValue(Message.JAVA_METHOD, stringToMethod((Message) m[0], value.toString()));
            }
        } else {
            if(m != null) {
                parent.setValue(propertyName, null);
            }
        }
    }

    public Object getEntry(CommonDDBean parent, int row) {
        throw new UnsupportedOperationException();
    }	

    public void setEntry(CommonDDBean parent, int row, Object value) {
        throw new UnsupportedOperationException();
    }
    
    private String methodToString(JavaMethod jm) {
        StringBuffer buf = new StringBuffer(128);

        if(jm != null) {
            buf.append(jm.getMethodName());
            
            MethodParams mps = jm.getMethodParams();
            if(mps != null && mps.sizeMethodParam() > 0) {
                buf.append("(");
                String [] params = mps.getMethodParam();
                for(int i = 0; i < params.length; i++) {
                    if(i > 0) {
                        buf.append(", ");
                    }
                    
                    if(Utils.notEmpty(params[i])) {
                        buf.append(params[i]);
                    } else {
                        buf.append(" ");
                    }
                }
                buf.append(")");
            }
        }
        
        return buf.toString();
    }
    
    private JavaMethod stringToMethod(Message m, String methodDesc) {
        JavaMethod method = null;
        
        if(Utils.notEmpty(methodDesc)) {
            String [] parts = methodSplitter.split(methodDesc);
            if(parts.length > 0) {
                method = m.newJavaMethod();
                method.setMethodName(parts[0]);
                
                if(parts.length > 1) {
                    MethodParams mps = method.newMethodParams();
                    for(int i = 1; i < parts.length; i++) {
                        mps.addMethodParam(parts[i]);
                    }
                    method.setMethodParams(mps);
                }
            }
        }
        
        return method;
    }
}
